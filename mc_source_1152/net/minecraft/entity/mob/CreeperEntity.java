package net.minecraft.entity.mob;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.api.EnvironmentInterfaces;
import net.minecraft.client.render.entity.feature.SkinOverlayOwner;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.CreeperIgniteGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

@EnvironmentInterfaces({@EnvironmentInterface(
   value = EnvType.CLIENT,
   itf = SkinOverlayOwner.class
)})
public class CreeperEntity extends HostileEntity implements SkinOverlayOwner {
   private static final TrackedData<Integer> FUSE_SPEED;
   private static final TrackedData<Boolean> CHARGED;
   private static final TrackedData<Boolean> IGNITED;
   private int lastFuseTime;
   private int currentFuseTime;
   private int fuseTime = 30;
   private int explosionRadius = 3;
   private int headsDropped;

   public CreeperEntity(EntityType<? extends CreeperEntity> entityType, World world) {
      super(entityType, world);
   }

   protected void initGoals() {
      this.goalSelector.add(1, new SwimGoal(this));
      this.goalSelector.add(2, new CreeperIgniteGoal(this));
      this.goalSelector.add(3, new FleeEntityGoal(this, OcelotEntity.class, 6.0F, 1.0D, 1.2D));
      this.goalSelector.add(3, new FleeEntityGoal(this, CatEntity.class, 6.0F, 1.0D, 1.2D));
      this.goalSelector.add(4, new MeleeAttackGoal(this, 1.0D, false));
      this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8D));
      this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.add(6, new LookAroundGoal(this));
      this.targetSelector.add(1, new FollowTargetGoal(this, PlayerEntity.class, true));
      this.targetSelector.add(2, new RevengeGoal(this, new Class[0]));
   }

   protected void initAttributes() {
      super.initAttributes();
      this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
   }

   public int getSafeFallDistance() {
      return this.getTarget() == null ? 3 : 3 + (int)(this.getHealth() - 1.0F);
   }

   public boolean handleFallDamage(float fallDistance, float damageMultiplier) {
      boolean bl = super.handleFallDamage(fallDistance, damageMultiplier);
      this.currentFuseTime = (int)((float)this.currentFuseTime + fallDistance * 1.5F);
      if (this.currentFuseTime > this.fuseTime - 5) {
         this.currentFuseTime = this.fuseTime - 5;
      }

      return bl;
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(FUSE_SPEED, -1);
      this.dataTracker.startTracking(CHARGED, false);
      this.dataTracker.startTracking(IGNITED, false);
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      if ((Boolean)this.dataTracker.get(CHARGED)) {
         tag.putBoolean("powered", true);
      }

      tag.putShort("Fuse", (short)this.fuseTime);
      tag.putByte("ExplosionRadius", (byte)this.explosionRadius);
      tag.putBoolean("ignited", this.getIgnited());
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      this.dataTracker.set(CHARGED, tag.getBoolean("powered"));
      if (tag.contains("Fuse", 99)) {
         this.fuseTime = tag.getShort("Fuse");
      }

      if (tag.contains("ExplosionRadius", 99)) {
         this.explosionRadius = tag.getByte("ExplosionRadius");
      }

      if (tag.getBoolean("ignited")) {
         this.setIgnited();
      }

   }

   public void tick() {
      if (this.isAlive()) {
         this.lastFuseTime = this.currentFuseTime;
         if (this.getIgnited()) {
            this.setFuseSpeed(1);
         }

         int i = this.getFuseSpeed();
         if (i > 0 && this.currentFuseTime == 0) {
            this.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1.0F, 0.5F);
         }

         this.currentFuseTime += i;
         if (this.currentFuseTime < 0) {
            this.currentFuseTime = 0;
         }

         if (this.currentFuseTime >= this.fuseTime) {
            this.currentFuseTime = this.fuseTime;
            this.explode();
         }
      }

      super.tick();
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_CREEPER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_CREEPER_DEATH;
   }

   protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
      super.dropEquipment(source, lootingMultiplier, allowDrops);
      Entity entity = source.getAttacker();
      if (entity != this && entity instanceof CreeperEntity) {
         CreeperEntity creeperEntity = (CreeperEntity)entity;
         if (creeperEntity.shouldDropHead()) {
            creeperEntity.onHeadDropped();
            this.dropItem(Items.CREEPER_HEAD);
         }
      }

   }

   public boolean tryAttack(Entity target) {
      return true;
   }

   public boolean shouldRenderOverlay() {
      return (Boolean)this.dataTracker.get(CHARGED);
   }

   @Environment(EnvType.CLIENT)
   public float getClientFuseTime(float timeDelta) {
      return MathHelper.lerp(timeDelta, (float)this.lastFuseTime, (float)this.currentFuseTime) / (float)(this.fuseTime - 2);
   }

   public int getFuseSpeed() {
      return (Integer)this.dataTracker.get(FUSE_SPEED);
   }

   public void setFuseSpeed(int fuseSpeed) {
      this.dataTracker.set(FUSE_SPEED, fuseSpeed);
   }

   public void onStruckByLightning(LightningEntity lightning) {
      super.onStruckByLightning(lightning);
      this.dataTracker.set(CHARGED, true);
   }

   protected boolean interactMob(PlayerEntity player, Hand hand) {
      ItemStack itemStack = player.getStackInHand(hand);
      if (itemStack.getItem() == Items.FLINT_AND_STEEL) {
         this.world.playSound(player, this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_FLINTANDSTEEL_USE, this.getSoundCategory(), 1.0F, this.random.nextFloat() * 0.4F + 0.8F);
         if (!this.world.isClient) {
            this.setIgnited();
            itemStack.damage(1, (LivingEntity)player, (Consumer)((playerEntity) -> {
               playerEntity.sendToolBreakStatus(hand);
            }));
         }

         return true;
      } else {
         return super.interactMob(player, hand);
      }
   }

   private void explode() {
      if (!this.world.isClient) {
         Explosion.DestructionType destructionType = this.world.getGameRules().getBoolean(GameRules.MOB_GRIEFING) ? Explosion.DestructionType.DESTROY : Explosion.DestructionType.NONE;
         float f = this.shouldRenderOverlay() ? 2.0F : 1.0F;
         this.dead = true;
         this.world.createExplosion(this, this.getX(), this.getY(), this.getZ(), (float)this.explosionRadius * f, destructionType);
         this.remove();
         this.spawnEffectsCloud();
      }

   }

   private void spawnEffectsCloud() {
      Collection<StatusEffectInstance> collection = this.getStatusEffects();
      if (!collection.isEmpty()) {
         AreaEffectCloudEntity areaEffectCloudEntity = new AreaEffectCloudEntity(this.world, this.getX(), this.getY(), this.getZ());
         areaEffectCloudEntity.setRadius(2.5F);
         areaEffectCloudEntity.setRadiusOnUse(-0.5F);
         areaEffectCloudEntity.setWaitTime(10);
         areaEffectCloudEntity.setDuration(areaEffectCloudEntity.getDuration() / 2);
         areaEffectCloudEntity.setRadiusGrowth(-areaEffectCloudEntity.getRadius() / (float)areaEffectCloudEntity.getDuration());
         Iterator var3 = collection.iterator();

         while(var3.hasNext()) {
            StatusEffectInstance statusEffectInstance = (StatusEffectInstance)var3.next();
            areaEffectCloudEntity.addEffect(new StatusEffectInstance(statusEffectInstance));
         }

         this.world.spawnEntity(areaEffectCloudEntity);
      }

   }

   public boolean getIgnited() {
      return (Boolean)this.dataTracker.get(IGNITED);
   }

   public void setIgnited() {
      this.dataTracker.set(IGNITED, true);
   }

   public boolean shouldDropHead() {
      return this.shouldRenderOverlay() && this.headsDropped < 1;
   }

   public void onHeadDropped() {
      ++this.headsDropped;
   }

   static {
      FUSE_SPEED = DataTracker.registerData(CreeperEntity.class, TrackedDataHandlerRegistry.INTEGER);
      CHARGED = DataTracker.registerData(CreeperEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      IGNITED = DataTracker.registerData(CreeperEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }
}
