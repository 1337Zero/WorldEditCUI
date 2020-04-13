package net.minecraft.entity.passive;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class PolarBearEntity extends AnimalEntity {
   private static final TrackedData<Boolean> WARNING;
   private float lastWarningAnimationProgress;
   private float warningAnimationProgress;
   private int warningSoundCooldown;

   public PolarBearEntity(EntityType<? extends PolarBearEntity> entityType, World world) {
      super(entityType, world);
   }

   public PassiveEntity createChild(PassiveEntity mate) {
      return (PassiveEntity)EntityType.POLAR_BEAR.create(this.world);
   }

   public boolean isBreedingItem(ItemStack stack) {
      return false;
   }

   protected void initGoals() {
      super.initGoals();
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(1, new PolarBearEntity.AttackGoal());
      this.goalSelector.add(1, new PolarBearEntity.PolarBearEscapeDangerGoal());
      this.goalSelector.add(4, new FollowParentGoal(this, 1.25D));
      this.goalSelector.add(5, new WanderAroundGoal(this, 1.0D));
      this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
      this.goalSelector.add(7, new LookAroundGoal(this));
      this.targetSelector.add(1, new PolarBearEntity.PolarBearRevengeGoal());
      this.targetSelector.add(2, new PolarBearEntity.FollowPlayersGoal());
      this.targetSelector.add(3, new FollowTargetGoal(this, FoxEntity.class, 10, true, true, (Predicate)null));
   }

   protected void initAttributes() {
      super.initAttributes();
      this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(30.0D);
      this.getAttributeInstance(EntityAttributes.FOLLOW_RANGE).setBaseValue(20.0D);
      this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
      this.getAttributes().register(EntityAttributes.ATTACK_DAMAGE);
      this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
   }

   public static boolean canSpawn(EntityType<PolarBearEntity> type, IWorld world, SpawnType spawnType, BlockPos pos, Random random) {
      Biome biome = world.getBiome(pos);
      if (biome != Biomes.FROZEN_OCEAN && biome != Biomes.DEEP_FROZEN_OCEAN) {
         return isValidNaturalSpawn(type, world, spawnType, pos, random);
      } else {
         return world.getBaseLightLevel(pos, 0) > 8 && world.getBlockState(pos.down()).getBlock() == Blocks.ICE;
      }
   }

   protected SoundEvent getAmbientSound() {
      return this.isBaby() ? SoundEvents.ENTITY_POLAR_BEAR_AMBIENT_BABY : SoundEvents.ENTITY_POLAR_BEAR_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_POLAR_BEAR_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_POLAR_BEAR_DEATH;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.ENTITY_POLAR_BEAR_STEP, 0.15F, 1.0F);
   }

   protected void playWarningSound() {
      if (this.warningSoundCooldown <= 0) {
         this.playSound(SoundEvents.ENTITY_POLAR_BEAR_WARNING, 1.0F, this.getSoundPitch());
         this.warningSoundCooldown = 40;
      }

   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(WARNING, false);
   }

   public void tick() {
      super.tick();
      if (this.world.isClient) {
         if (this.warningAnimationProgress != this.lastWarningAnimationProgress) {
            this.calculateDimensions();
         }

         this.lastWarningAnimationProgress = this.warningAnimationProgress;
         if (this.isWarning()) {
            this.warningAnimationProgress = MathHelper.clamp(this.warningAnimationProgress + 1.0F, 0.0F, 6.0F);
         } else {
            this.warningAnimationProgress = MathHelper.clamp(this.warningAnimationProgress - 1.0F, 0.0F, 6.0F);
         }
      }

      if (this.warningSoundCooldown > 0) {
         --this.warningSoundCooldown;
      }

   }

   public EntityDimensions getDimensions(EntityPose pose) {
      if (this.warningAnimationProgress > 0.0F) {
         float f = this.warningAnimationProgress / 6.0F;
         float g = 1.0F + f;
         return super.getDimensions(pose).scaled(1.0F, g);
      } else {
         return super.getDimensions(pose);
      }
   }

   public boolean tryAttack(Entity target) {
      boolean bl = target.damage(DamageSource.mob(this), (float)((int)this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).getValue()));
      if (bl) {
         this.dealDamage(this, target);
      }

      return bl;
   }

   public boolean isWarning() {
      return (Boolean)this.dataTracker.get(WARNING);
   }

   public void setWarning(boolean warning) {
      this.dataTracker.set(WARNING, warning);
   }

   @Environment(EnvType.CLIENT)
   public float getWarningAnimationProgress(float tickDelta) {
      return MathHelper.lerp(tickDelta, this.lastWarningAnimationProgress, this.warningAnimationProgress) / 6.0F;
   }

   protected float getBaseMovementSpeedMultiplier() {
      return 0.98F;
   }

   public net.minecraft.entity.EntityData initialize(IWorld world, LocalDifficulty difficulty, SpawnType spawnType, @Nullable net.minecraft.entity.EntityData entityData, @Nullable CompoundTag entityTag) {
      if (entityData == null) {
         entityData = new PassiveEntity.EntityData();
         ((PassiveEntity.EntityData)entityData).setBabyChance(1.0F);
      }

      return super.initialize(world, difficulty, spawnType, (net.minecraft.entity.EntityData)entityData, entityTag);
   }

   static {
      WARNING = DataTracker.registerData(PolarBearEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }

   class PolarBearEscapeDangerGoal extends EscapeDangerGoal {
      public PolarBearEscapeDangerGoal() {
         super(PolarBearEntity.this, 2.0D);
      }

      public boolean canStart() {
         return !PolarBearEntity.this.isBaby() && !PolarBearEntity.this.isOnFire() ? false : super.canStart();
      }
   }

   class AttackGoal extends MeleeAttackGoal {
      public AttackGoal() {
         super(PolarBearEntity.this, 1.25D, true);
      }

      protected void attack(LivingEntity target, double squaredDistance) {
         double d = this.getSquaredMaxAttackDistance(target);
         if (squaredDistance <= d && this.ticksUntilAttack <= 0) {
            this.ticksUntilAttack = 20;
            this.mob.tryAttack(target);
            PolarBearEntity.this.setWarning(false);
         } else if (squaredDistance <= d * 2.0D) {
            if (this.ticksUntilAttack <= 0) {
               PolarBearEntity.this.setWarning(false);
               this.ticksUntilAttack = 20;
            }

            if (this.ticksUntilAttack <= 10) {
               PolarBearEntity.this.setWarning(true);
               PolarBearEntity.this.playWarningSound();
            }
         } else {
            this.ticksUntilAttack = 20;
            PolarBearEntity.this.setWarning(false);
         }

      }

      public void stop() {
         PolarBearEntity.this.setWarning(false);
         super.stop();
      }

      protected double getSquaredMaxAttackDistance(LivingEntity entity) {
         return (double)(4.0F + entity.getWidth());
      }
   }

   class FollowPlayersGoal extends FollowTargetGoal<PlayerEntity> {
      public FollowPlayersGoal() {
         super(PolarBearEntity.this, PlayerEntity.class, 20, true, true, (Predicate)null);
      }

      public boolean canStart() {
         if (PolarBearEntity.this.isBaby()) {
            return false;
         } else {
            if (super.canStart()) {
               List<PolarBearEntity> list = PolarBearEntity.this.world.getNonSpectatingEntities(PolarBearEntity.class, PolarBearEntity.this.getBoundingBox().expand(8.0D, 4.0D, 8.0D));
               Iterator var2 = list.iterator();

               while(var2.hasNext()) {
                  PolarBearEntity polarBearEntity = (PolarBearEntity)var2.next();
                  if (polarBearEntity.isBaby()) {
                     return true;
                  }
               }
            }

            return false;
         }
      }

      protected double getFollowRange() {
         return super.getFollowRange() * 0.5D;
      }
   }

   class PolarBearRevengeGoal extends RevengeGoal {
      public PolarBearRevengeGoal() {
         super(PolarBearEntity.this);
      }

      public void start() {
         super.start();
         if (PolarBearEntity.this.isBaby()) {
            this.callSameTypeForRevenge();
            this.stop();
         }

      }

      protected void setMobEntityTarget(MobEntity mob, LivingEntity target) {
         if (mob instanceof PolarBearEntity && !mob.isBaby()) {
            super.setMobEntityTarget(mob, target);
         }

      }
   }
}
