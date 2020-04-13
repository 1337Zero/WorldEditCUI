package net.minecraft.entity.passive;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ChickenEntity extends AnimalEntity {
   private static final Ingredient BREEDING_INGREDIENT;
   public float field_6741;
   public float field_6743;
   public float field_6738;
   public float field_6736;
   public float field_6737 = 1.0F;
   public int eggLayTime;
   public boolean jockey;

   public ChickenEntity(EntityType<? extends ChickenEntity> entityType, World world) {
      super(entityType, world);
      this.eggLayTime = this.random.nextInt(6000) + 6000;
      this.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
   }

   protected void initGoals() {
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(1, new EscapeDangerGoal(this, 1.4D));
      this.goalSelector.add(2, new AnimalMateGoal(this, 1.0D));
      this.goalSelector.add(3, new TemptGoal(this, 1.0D, false, BREEDING_INGREDIENT));
      this.goalSelector.add(4, new FollowParentGoal(this, 1.1D));
      this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0D));
      this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
      this.goalSelector.add(7, new LookAroundGoal(this));
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return this.isBaby() ? dimensions.height * 0.85F : dimensions.height * 0.92F;
   }

   protected void initAttributes() {
      super.initAttributes();
      this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(4.0D);
      this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
   }

   public void tickMovement() {
      super.tickMovement();
      this.field_6736 = this.field_6741;
      this.field_6738 = this.field_6743;
      this.field_6743 = (float)((double)this.field_6743 + (double)(this.onGround ? -1 : 4) * 0.3D);
      this.field_6743 = MathHelper.clamp(this.field_6743, 0.0F, 1.0F);
      if (!this.onGround && this.field_6737 < 1.0F) {
         this.field_6737 = 1.0F;
      }

      this.field_6737 = (float)((double)this.field_6737 * 0.9D);
      Vec3d vec3d = this.getVelocity();
      if (!this.onGround && vec3d.y < 0.0D) {
         this.setVelocity(vec3d.multiply(1.0D, 0.6D, 1.0D));
      }

      this.field_6741 += this.field_6737 * 2.0F;
      if (!this.world.isClient && this.isAlive() && !this.isBaby() && !this.hasJockey() && --this.eggLayTime <= 0) {
         this.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
         this.dropItem(Items.EGG);
         this.eggLayTime = this.random.nextInt(6000) + 6000;
      }

   }

   public boolean handleFallDamage(float fallDistance, float damageMultiplier) {
      return false;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_CHICKEN_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_CHICKEN_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_CHICKEN_DEATH;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.ENTITY_CHICKEN_STEP, 0.15F, 1.0F);
   }

   public ChickenEntity createChild(PassiveEntity passiveEntity) {
      return (ChickenEntity)EntityType.CHICKEN.create(this.world);
   }

   public boolean isBreedingItem(ItemStack stack) {
      return BREEDING_INGREDIENT.test(stack);
   }

   protected int getCurrentExperience(PlayerEntity player) {
      return this.hasJockey() ? 10 : super.getCurrentExperience(player);
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      this.jockey = tag.getBoolean("IsChickenJockey");
      if (tag.contains("EggLayTime")) {
         this.eggLayTime = tag.getInt("EggLayTime");
      }

   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      tag.putBoolean("IsChickenJockey", this.jockey);
      tag.putInt("EggLayTime", this.eggLayTime);
   }

   public boolean canImmediatelyDespawn(double distanceSquared) {
      return this.hasJockey() && !this.hasPassengers();
   }

   public void updatePassengerPosition(Entity passenger) {
      super.updatePassengerPosition(passenger);
      float f = MathHelper.sin(this.bodyYaw * 0.017453292F);
      float g = MathHelper.cos(this.bodyYaw * 0.017453292F);
      float h = 0.1F;
      float i = 0.0F;
      passenger.updatePosition(this.getX() + (double)(0.1F * f), this.getBodyY(0.5D) + passenger.getHeightOffset() + 0.0D, this.getZ() - (double)(0.1F * g));
      if (passenger instanceof LivingEntity) {
         ((LivingEntity)passenger).bodyYaw = this.bodyYaw;
      }

   }

   public boolean hasJockey() {
      return this.jockey;
   }

   public void setHasJockey(boolean hasJockey) {
      this.jockey = hasJockey;
   }

   static {
      BREEDING_INGREDIENT = Ingredient.ofItems(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);
   }
}
