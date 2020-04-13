package net.minecraft.entity.mob;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.SkeletonHorseTrapTriggerGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class SkeletonHorseEntity extends HorseBaseEntity {
   private final SkeletonHorseTrapTriggerGoal field_7003 = new SkeletonHorseTrapTriggerGoal(this);
   private boolean trapped;
   private int trapTime;

   public SkeletonHorseEntity(EntityType<? extends SkeletonHorseEntity> entityType, World world) {
      super(entityType, world);
   }

   protected void initAttributes() {
      super.initAttributes();
      this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(15.0D);
      this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.20000000298023224D);
      this.getAttributeInstance(JUMP_STRENGTH).setBaseValue(this.getChildJumpStrengthBonus());
   }

   protected void initCustomGoals() {
   }

   protected SoundEvent getAmbientSound() {
      super.getAmbientSound();
      return this.isInFluid(FluidTags.WATER) ? SoundEvents.ENTITY_SKELETON_HORSE_AMBIENT_WATER : SoundEvents.ENTITY_SKELETON_HORSE_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      super.getDeathSound();
      return SoundEvents.ENTITY_SKELETON_HORSE_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      super.getHurtSound(source);
      return SoundEvents.ENTITY_SKELETON_HORSE_HURT;
   }

   protected SoundEvent getSwimSound() {
      if (this.onGround) {
         if (!this.hasPassengers()) {
            return SoundEvents.ENTITY_SKELETON_HORSE_STEP_WATER;
         }

         ++this.soundTicks;
         if (this.soundTicks > 5 && this.soundTicks % 3 == 0) {
            return SoundEvents.ENTITY_SKELETON_HORSE_GALLOP_WATER;
         }

         if (this.soundTicks <= 5) {
            return SoundEvents.ENTITY_SKELETON_HORSE_STEP_WATER;
         }
      }

      return SoundEvents.ENTITY_SKELETON_HORSE_SWIM;
   }

   protected void playSwimSound(float volume) {
      if (this.onGround) {
         super.playSwimSound(0.3F);
      } else {
         super.playSwimSound(Math.min(0.1F, volume * 25.0F));
      }

   }

   protected void playJumpSound() {
      if (this.isTouchingWater()) {
         this.playSound(SoundEvents.ENTITY_SKELETON_HORSE_JUMP_WATER, 0.4F, 1.0F);
      } else {
         super.playJumpSound();
      }

   }

   public EntityGroup getGroup() {
      return EntityGroup.UNDEAD;
   }

   public double getMountedHeightOffset() {
      return super.getMountedHeightOffset() - 0.1875D;
   }

   public void tickMovement() {
      super.tickMovement();
      if (this.isTrapped() && this.trapTime++ >= 18000) {
         this.remove();
      }

   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      tag.putBoolean("SkeletonTrap", this.isTrapped());
      tag.putInt("SkeletonTrapTime", this.trapTime);
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      this.setTrapped(tag.getBoolean("SkeletonTrap"));
      this.trapTime = tag.getInt("SkeletonTrapTime");
   }

   public boolean canBeRiddenInWater() {
      return true;
   }

   protected float getBaseMovementSpeedMultiplier() {
      return 0.96F;
   }

   public boolean isTrapped() {
      return this.trapped;
   }

   public void setTrapped(boolean trapped) {
      if (trapped != this.trapped) {
         this.trapped = trapped;
         if (trapped) {
            this.goalSelector.add(1, this.field_7003);
         } else {
            this.goalSelector.remove(this.field_7003);
         }

      }
   }

   @Nullable
   public PassiveEntity createChild(PassiveEntity mate) {
      return (PassiveEntity)EntityType.SKELETON_HORSE.create(this.world);
   }

   public boolean interactMob(PlayerEntity player, Hand hand) {
      ItemStack itemStack = player.getStackInHand(hand);
      if (itemStack.getItem() instanceof SpawnEggItem) {
         return super.interactMob(player, hand);
      } else if (!this.isTame()) {
         return false;
      } else if (this.isBaby()) {
         return super.interactMob(player, hand);
      } else if (player.shouldCancelInteraction()) {
         this.openInventory(player);
         return true;
      } else if (this.hasPassengers()) {
         return super.interactMob(player, hand);
      } else {
         if (!itemStack.isEmpty()) {
            if (itemStack.getItem() == Items.SADDLE && !this.isSaddled()) {
               this.openInventory(player);
               return true;
            }

            if (itemStack.useOnEntity(player, this, hand)) {
               return true;
            }
         }

         this.putPlayerOnBack(player);
         return true;
      }
   }
}
