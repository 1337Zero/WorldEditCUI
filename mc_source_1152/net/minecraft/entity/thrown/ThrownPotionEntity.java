package net.minecraft.entity.thrown;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.api.EnvironmentInterfaces;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@EnvironmentInterfaces({@EnvironmentInterface(
   value = EnvType.CLIENT,
   itf = FlyingItemEntity.class
)})
public class ThrownPotionEntity extends ThrownEntity implements FlyingItemEntity {
   private static final TrackedData<ItemStack> ITEM_STACK;
   private static final Logger LOGGER;
   public static final Predicate<LivingEntity> WATER_HURTS;

   public ThrownPotionEntity(EntityType<? extends ThrownPotionEntity> entityType, World world) {
      super(entityType, world);
   }

   public ThrownPotionEntity(World world, LivingEntity livingEntity) {
      super(EntityType.POTION, livingEntity, world);
   }

   public ThrownPotionEntity(World world, double x, double y, double d) {
      super(EntityType.POTION, x, y, d, world);
   }

   protected void initDataTracker() {
      this.getDataTracker().startTracking(ITEM_STACK, ItemStack.EMPTY);
   }

   public ItemStack getStack() {
      ItemStack itemStack = (ItemStack)this.getDataTracker().get(ITEM_STACK);
      if (itemStack.getItem() != Items.SPLASH_POTION && itemStack.getItem() != Items.LINGERING_POTION) {
         if (this.world != null) {
            LOGGER.error("ThrownPotion entity {} has no item?!", this.getEntityId());
         }

         return new ItemStack(Items.SPLASH_POTION);
      } else {
         return itemStack;
      }
   }

   public void setItemStack(ItemStack itemStack) {
      this.getDataTracker().set(ITEM_STACK, itemStack.copy());
   }

   protected float getGravity() {
      return 0.05F;
   }

   protected void onCollision(HitResult hitResult) {
      if (!this.world.isClient) {
         ItemStack itemStack = this.getStack();
         Potion potion = PotionUtil.getPotion(itemStack);
         List<StatusEffectInstance> list = PotionUtil.getPotionEffects(itemStack);
         boolean bl = potion == Potions.WATER && list.isEmpty();
         if (hitResult.getType() == HitResult.Type.BLOCK && bl) {
            BlockHitResult blockHitResult = (BlockHitResult)hitResult;
            Direction direction = blockHitResult.getSide();
            BlockPos blockPos = blockHitResult.getBlockPos().offset(direction);
            this.extinguishFire(blockPos, direction);
            this.extinguishFire(blockPos.offset(direction.getOpposite()), direction);
            Iterator var9 = Direction.Type.HORIZONTAL.iterator();

            while(var9.hasNext()) {
               Direction direction2 = (Direction)var9.next();
               this.extinguishFire(blockPos.offset(direction2), direction2);
            }
         }

         if (bl) {
            this.damageEntitiesHurtByWater();
         } else if (!list.isEmpty()) {
            if (this.isLingering()) {
               this.applyLingeringPotion(itemStack, potion);
            } else {
               this.applySplashPotion(list, hitResult.getType() == HitResult.Type.ENTITY ? ((EntityHitResult)hitResult).getEntity() : null);
            }
         }

         int i = potion.hasInstantEffect() ? 2007 : 2002;
         this.world.playLevelEvent(i, new BlockPos(this), PotionUtil.getColor(itemStack));
         this.remove();
      }
   }

   private void damageEntitiesHurtByWater() {
      Box box = this.getBoundingBox().expand(4.0D, 2.0D, 4.0D);
      List<LivingEntity> list = this.world.getEntities(LivingEntity.class, box, WATER_HURTS);
      if (!list.isEmpty()) {
         Iterator var3 = list.iterator();

         while(var3.hasNext()) {
            LivingEntity livingEntity = (LivingEntity)var3.next();
            double d = this.squaredDistanceTo(livingEntity);
            if (d < 16.0D && doesWaterHurt(livingEntity)) {
               livingEntity.damage(DamageSource.magic(livingEntity, this.getOwner()), 1.0F);
            }
         }
      }

   }

   private void applySplashPotion(List<StatusEffectInstance> list, @Nullable Entity entity) {
      Box box = this.getBoundingBox().expand(4.0D, 2.0D, 4.0D);
      List<LivingEntity> list2 = this.world.getNonSpectatingEntities(LivingEntity.class, box);
      if (!list2.isEmpty()) {
         Iterator var5 = list2.iterator();

         while(true) {
            LivingEntity livingEntity;
            double d;
            do {
               do {
                  if (!var5.hasNext()) {
                     return;
                  }

                  livingEntity = (LivingEntity)var5.next();
               } while(!livingEntity.isAffectedBySplashPotions());

               d = this.squaredDistanceTo(livingEntity);
            } while(d >= 16.0D);

            double e = 1.0D - Math.sqrt(d) / 4.0D;
            if (livingEntity == entity) {
               e = 1.0D;
            }

            Iterator var11 = list.iterator();

            while(var11.hasNext()) {
               StatusEffectInstance statusEffectInstance = (StatusEffectInstance)var11.next();
               StatusEffect statusEffect = statusEffectInstance.getEffectType();
               if (statusEffect.isInstant()) {
                  statusEffect.applyInstantEffect(this, this.getOwner(), livingEntity, statusEffectInstance.getAmplifier(), e);
               } else {
                  int i = (int)(e * (double)statusEffectInstance.getDuration() + 0.5D);
                  if (i > 20) {
                     livingEntity.addStatusEffect(new StatusEffectInstance(statusEffect, i, statusEffectInstance.getAmplifier(), statusEffectInstance.isAmbient(), statusEffectInstance.shouldShowParticles()));
                  }
               }
            }
         }
      }
   }

   private void applyLingeringPotion(ItemStack itemStack, Potion potion) {
      AreaEffectCloudEntity areaEffectCloudEntity = new AreaEffectCloudEntity(this.world, this.getX(), this.getY(), this.getZ());
      areaEffectCloudEntity.setOwner(this.getOwner());
      areaEffectCloudEntity.setRadius(3.0F);
      areaEffectCloudEntity.setRadiusOnUse(-0.5F);
      areaEffectCloudEntity.setWaitTime(10);
      areaEffectCloudEntity.setRadiusGrowth(-areaEffectCloudEntity.getRadius() / (float)areaEffectCloudEntity.getDuration());
      areaEffectCloudEntity.setPotion(potion);
      Iterator var4 = PotionUtil.getCustomPotionEffects(itemStack).iterator();

      while(var4.hasNext()) {
         StatusEffectInstance statusEffectInstance = (StatusEffectInstance)var4.next();
         areaEffectCloudEntity.addEffect(new StatusEffectInstance(statusEffectInstance));
      }

      CompoundTag compoundTag = itemStack.getTag();
      if (compoundTag != null && compoundTag.contains("CustomPotionColor", 99)) {
         areaEffectCloudEntity.setColor(compoundTag.getInt("CustomPotionColor"));
      }

      this.world.spawnEntity(areaEffectCloudEntity);
   }

   private boolean isLingering() {
      return this.getStack().getItem() == Items.LINGERING_POTION;
   }

   private void extinguishFire(BlockPos blockPos, Direction direction) {
      BlockState blockState = this.world.getBlockState(blockPos);
      Block block = blockState.getBlock();
      if (block == Blocks.FIRE) {
         this.world.extinguishFire((PlayerEntity)null, blockPos.offset(direction), direction.getOpposite());
      } else if (block == Blocks.CAMPFIRE && (Boolean)blockState.get(CampfireBlock.LIT)) {
         this.world.playLevelEvent((PlayerEntity)null, 1009, blockPos, 0);
         this.world.setBlockState(blockPos, (BlockState)blockState.with(CampfireBlock.LIT, false));
      }

   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      ItemStack itemStack = ItemStack.fromTag(tag.getCompound("Potion"));
      if (itemStack.isEmpty()) {
         this.remove();
      } else {
         this.setItemStack(itemStack);
      }

   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      ItemStack itemStack = this.getStack();
      if (!itemStack.isEmpty()) {
         tag.put("Potion", itemStack.toTag(new CompoundTag()));
      }

   }

   private static boolean doesWaterHurt(LivingEntity entityHit) {
      return entityHit instanceof EndermanEntity || entityHit instanceof BlazeEntity;
   }

   static {
      ITEM_STACK = DataTracker.registerData(ThrownPotionEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
      LOGGER = LogManager.getLogger();
      WATER_HURTS = ThrownPotionEntity::doesWaterHurt;
   }
}
