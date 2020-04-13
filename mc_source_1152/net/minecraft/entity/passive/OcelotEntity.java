package net.minecraft.entity.passive;

import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.AttackGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class OcelotEntity extends AnimalEntity {
   private static final Ingredient TAMING_INGREDIENT;
   private static final TrackedData<Boolean> TRUSTING;
   private OcelotEntity.FleeGoal<PlayerEntity> fleeGoal;
   private OcelotEntity.OcelotTemptGoal temptGoal;

   public OcelotEntity(EntityType<? extends OcelotEntity> entityType, World world) {
      super(entityType, world);
      this.updateFleeing();
   }

   private boolean isTrusting() {
      return (Boolean)this.dataTracker.get(TRUSTING);
   }

   private void setTrusting(boolean trusting) {
      this.dataTracker.set(TRUSTING, trusting);
      this.updateFleeing();
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      tag.putBoolean("Trusting", this.isTrusting());
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      this.setTrusting(tag.getBoolean("Trusting"));
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(TRUSTING, false);
   }

   protected void initGoals() {
      this.temptGoal = new OcelotEntity.OcelotTemptGoal(this, 0.6D, TAMING_INGREDIENT, true);
      this.goalSelector.add(1, new SwimGoal(this));
      this.goalSelector.add(3, this.temptGoal);
      this.goalSelector.add(7, new PounceAtTargetGoal(this, 0.3F));
      this.goalSelector.add(8, new AttackGoal(this));
      this.goalSelector.add(9, new AnimalMateGoal(this, 0.8D));
      this.goalSelector.add(10, new WanderAroundFarGoal(this, 0.8D, 1.0000001E-5F));
      this.goalSelector.add(11, new LookAtEntityGoal(this, PlayerEntity.class, 10.0F));
      this.targetSelector.add(1, new FollowTargetGoal(this, ChickenEntity.class, false));
      this.targetSelector.add(1, new FollowTargetGoal(this, TurtleEntity.class, 10, false, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
   }

   public void mobTick() {
      if (this.getMoveControl().isMoving()) {
         double d = this.getMoveControl().getSpeed();
         if (d == 0.6D) {
            this.setPose(EntityPose.CROUCHING);
            this.setSprinting(false);
         } else if (d == 1.33D) {
            this.setPose(EntityPose.STANDING);
            this.setSprinting(true);
         } else {
            this.setPose(EntityPose.STANDING);
            this.setSprinting(false);
         }
      } else {
         this.setPose(EntityPose.STANDING);
         this.setSprinting(false);
      }

   }

   public boolean canImmediatelyDespawn(double distanceSquared) {
      return !this.isTrusting() && this.age > 2400;
   }

   protected void initAttributes() {
      super.initAttributes();
      this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(10.0D);
      this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.30000001192092896D);
      this.getAttributes().register(EntityAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
   }

   public boolean handleFallDamage(float fallDistance, float damageMultiplier) {
      return false;
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_OCELOT_AMBIENT;
   }

   public int getMinAmbientSoundDelay() {
      return 900;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_OCELOT_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_OCELOT_DEATH;
   }

   private float method_22329() {
      return (float)this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).getValue();
   }

   public boolean tryAttack(Entity target) {
      return target.damage(DamageSource.mob(this), this.method_22329());
   }

   public boolean damage(DamageSource source, float amount) {
      return this.isInvulnerableTo(source) ? false : super.damage(source, amount);
   }

   public boolean interactMob(PlayerEntity player, Hand hand) {
      ItemStack itemStack = player.getStackInHand(hand);
      if ((this.temptGoal == null || this.temptGoal.isActive()) && !this.isTrusting() && this.isBreedingItem(itemStack) && player.squaredDistanceTo(this) < 9.0D) {
         this.eat(player, itemStack);
         if (!this.world.isClient) {
            if (this.random.nextInt(3) == 0) {
               this.setTrusting(true);
               this.showEmoteParticle(true);
               this.world.sendEntityStatus(this, (byte)41);
            } else {
               this.showEmoteParticle(false);
               this.world.sendEntityStatus(this, (byte)40);
            }
         }

         return true;
      } else {
         return super.interactMob(player, hand);
      }
   }

   @Environment(EnvType.CLIENT)
   public void handleStatus(byte status) {
      if (status == 41) {
         this.showEmoteParticle(true);
      } else if (status == 40) {
         this.showEmoteParticle(false);
      } else {
         super.handleStatus(status);
      }

   }

   private void showEmoteParticle(boolean positive) {
      ParticleEffect particleEffect = ParticleTypes.HEART;
      if (!positive) {
         particleEffect = ParticleTypes.SMOKE;
      }

      for(int i = 0; i < 7; ++i) {
         double d = this.random.nextGaussian() * 0.02D;
         double e = this.random.nextGaussian() * 0.02D;
         double f = this.random.nextGaussian() * 0.02D;
         this.world.addParticle(particleEffect, this.getParticleX(1.0D), this.getRandomBodyY() + 0.5D, this.getParticleZ(1.0D), d, e, f);
      }

   }

   protected void updateFleeing() {
      if (this.fleeGoal == null) {
         this.fleeGoal = new OcelotEntity.FleeGoal(this, PlayerEntity.class, 16.0F, 0.8D, 1.33D);
      }

      this.goalSelector.remove(this.fleeGoal);
      if (!this.isTrusting()) {
         this.goalSelector.add(4, this.fleeGoal);
      }

   }

   public OcelotEntity createChild(PassiveEntity passiveEntity) {
      return (OcelotEntity)EntityType.OCELOT.create(this.world);
   }

   public boolean isBreedingItem(ItemStack stack) {
      return TAMING_INGREDIENT.test(stack);
   }

   public static boolean canSpawn(EntityType<OcelotEntity> type, IWorld world, SpawnType spawnType, BlockPos pos, Random random) {
      return random.nextInt(3) != 0;
   }

   public boolean canSpawn(WorldView world) {
      if (world.intersectsEntities(this) && !world.containsFluid(this.getBoundingBox())) {
         BlockPos blockPos = new BlockPos(this);
         if (blockPos.getY() < world.getSeaLevel()) {
            return false;
         }

         BlockState blockState = world.getBlockState(blockPos.down());
         Block block = blockState.getBlock();
         if (block == Blocks.GRASS_BLOCK || blockState.matches(BlockTags.LEAVES)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public net.minecraft.entity.EntityData initialize(IWorld world, LocalDifficulty difficulty, SpawnType spawnType, @Nullable net.minecraft.entity.EntityData entityData, @Nullable CompoundTag entityTag) {
      if (entityData == null) {
         entityData = new PassiveEntity.EntityData();
         ((PassiveEntity.EntityData)entityData).setBabyChance(1.0F);
      }

      return super.initialize(world, difficulty, spawnType, (net.minecraft.entity.EntityData)entityData, entityTag);
   }

   static {
      TAMING_INGREDIENT = Ingredient.ofItems(Items.COD, Items.SALMON);
      TRUSTING = DataTracker.registerData(OcelotEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }

   static class OcelotTemptGoal extends TemptGoal {
      private final OcelotEntity ocelot;

      public OcelotTemptGoal(OcelotEntity ocelot, double speed, Ingredient food, boolean canBeScared) {
         super(ocelot, speed, food, canBeScared);
         this.ocelot = ocelot;
      }

      protected boolean canBeScared() {
         return super.canBeScared() && !this.ocelot.isTrusting();
      }
   }

   static class FleeGoal<T extends LivingEntity> extends FleeEntityGoal<T> {
      private final OcelotEntity ocelot;

      public FleeGoal(OcelotEntity ocelot, Class<T> fleeFromType, float distance, double slowSpeed, double fastSpeed) {
         Predicate var10006 = EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR;
         super(ocelot, fleeFromType, distance, slowSpeed, fastSpeed, var10006::test);
         this.ocelot = ocelot;
      }

      public boolean canStart() {
         return !this.ocelot.isTrusting() && super.canStart();
      }

      public boolean shouldContinue() {
         return !this.ocelot.isTrusting() && super.shouldContinue();
      }
   }
}
