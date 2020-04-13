package net.minecraft.entity.mob;

import com.google.common.collect.Maps;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AbstractTraderEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.raid.Raid;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;

public class VindicatorEntity extends IllagerEntity {
   private static final Predicate<Difficulty> DIFFICULTY_ALLOWS_DOOR_BREAKING_PREDICATE = (difficulty) -> {
      return difficulty == Difficulty.NORMAL || difficulty == Difficulty.HARD;
   };
   private boolean isJohnny;

   public VindicatorEntity(EntityType<? extends VindicatorEntity> entityType, World world) {
      super(entityType, world);
   }

   protected void initGoals() {
      super.initGoals();
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(1, new VindicatorEntity.BreakDoorGoal(this));
      this.goalSelector.add(2, new IllagerEntity.LongDoorInteractGoal(this));
      this.goalSelector.add(3, new RaiderEntity.PatrolApproachGoal(this, 10.0F));
      this.goalSelector.add(4, new VindicatorEntity.AttackGoal(this));
      this.targetSelector.add(1, (new RevengeGoal(this, new Class[]{RaiderEntity.class})).setGroupRevenge());
      this.targetSelector.add(2, new FollowTargetGoal(this, PlayerEntity.class, true));
      this.targetSelector.add(3, new FollowTargetGoal(this, AbstractTraderEntity.class, true));
      this.targetSelector.add(3, new FollowTargetGoal(this, IronGolemEntity.class, true));
      this.targetSelector.add(4, new VindicatorEntity.FollowEntityGoal(this));
      this.goalSelector.add(8, new WanderAroundGoal(this, 0.6D));
      this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 3.0F, 1.0F));
      this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
   }

   protected void mobTick() {
      if (!this.isAiDisabled()) {
         EntityNavigation entityNavigation = this.getNavigation();
         if (entityNavigation instanceof MobNavigation) {
            boolean bl = ((ServerWorld)this.world).hasRaidAt(new BlockPos(this));
            ((MobNavigation)entityNavigation).setCanPathThroughDoors(bl);
         }
      }

      super.mobTick();
   }

   protected void initAttributes() {
      super.initAttributes();
      this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.3499999940395355D);
      this.getAttributeInstance(EntityAttributes.FOLLOW_RANGE).setBaseValue(12.0D);
      this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(24.0D);
      this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      if (this.isJohnny) {
         tag.putBoolean("Johnny", true);
      }

   }

   @Environment(EnvType.CLIENT)
   public IllagerEntity.State getState() {
      if (this.isAttacking()) {
         return IllagerEntity.State.ATTACKING;
      } else {
         return this.isCelebrating() ? IllagerEntity.State.CELEBRATING : IllagerEntity.State.CROSSED;
      }
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      if (tag.contains("Johnny", 99)) {
         this.isJohnny = tag.getBoolean("Johnny");
      }

   }

   public SoundEvent getCelebratingSound() {
      return SoundEvents.ENTITY_VINDICATOR_CELEBRATE;
   }

   @Nullable
   public EntityData initialize(IWorld world, LocalDifficulty difficulty, SpawnType spawnType, @Nullable EntityData entityData, @Nullable CompoundTag entityTag) {
      EntityData entityData2 = super.initialize(world, difficulty, spawnType, entityData, entityTag);
      ((MobNavigation)this.getNavigation()).setCanPathThroughDoors(true);
      this.initEquipment(difficulty);
      this.updateEnchantments(difficulty);
      return entityData2;
   }

   protected void initEquipment(LocalDifficulty difficulty) {
      if (this.getRaid() == null) {
         this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
      }

   }

   public boolean isTeammate(Entity other) {
      if (super.isTeammate(other)) {
         return true;
      } else if (other instanceof LivingEntity && ((LivingEntity)other).getGroup() == EntityGroup.ILLAGER) {
         return this.getScoreboardTeam() == null && other.getScoreboardTeam() == null;
      } else {
         return false;
      }
   }

   public void setCustomName(@Nullable Text name) {
      super.setCustomName(name);
      if (!this.isJohnny && name != null && name.getString().equals("Johnny")) {
         this.isJohnny = true;
      }

   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_VINDICATOR_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_VINDICATOR_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_VINDICATOR_HURT;
   }

   public void addBonusForWave(int wave, boolean unused) {
      ItemStack itemStack = new ItemStack(Items.IRON_AXE);
      Raid raid = this.getRaid();
      int i = 1;
      if (wave > raid.getMaxWaves(Difficulty.NORMAL)) {
         i = 2;
      }

      boolean bl = this.random.nextFloat() <= raid.getEnchantmentChance();
      if (bl) {
         Map<Enchantment, Integer> map = Maps.newHashMap();
         map.put(Enchantments.SHARPNESS, Integer.valueOf(i));
         EnchantmentHelper.set(map, itemStack);
      }

      this.equipStack(EquipmentSlot.MAINHAND, itemStack);
   }

   static class FollowEntityGoal extends FollowTargetGoal<LivingEntity> {
      public FollowEntityGoal(VindicatorEntity vindicator) {
         super(vindicator, LivingEntity.class, 0, true, true, LivingEntity::method_6102);
      }

      public boolean canStart() {
         return ((VindicatorEntity)this.mob).isJohnny && super.canStart();
      }

      public void start() {
         super.start();
         this.mob.setDespawnCounter(0);
      }
   }

   static class BreakDoorGoal extends net.minecraft.entity.ai.goal.BreakDoorGoal {
      public BreakDoorGoal(MobEntity mobEntity) {
         super(mobEntity, 6, VindicatorEntity.DIFFICULTY_ALLOWS_DOOR_BREAKING_PREDICATE);
         this.setControls(EnumSet.of(Goal.Control.MOVE));
      }

      public boolean shouldContinue() {
         VindicatorEntity vindicatorEntity = (VindicatorEntity)this.mob;
         return vindicatorEntity.hasActiveRaid() && super.shouldContinue();
      }

      public boolean canStart() {
         VindicatorEntity vindicatorEntity = (VindicatorEntity)this.mob;
         return vindicatorEntity.hasActiveRaid() && vindicatorEntity.random.nextInt(10) == 0 && super.canStart();
      }

      public void start() {
         super.start();
         this.mob.setDespawnCounter(0);
      }
   }

   class AttackGoal extends MeleeAttackGoal {
      public AttackGoal(VindicatorEntity vindicator) {
         super(vindicator, 1.0D, false);
      }

      protected double getSquaredMaxAttackDistance(LivingEntity entity) {
         if (this.mob.getVehicle() instanceof RavagerEntity) {
            float f = this.mob.getVehicle().getWidth() - 0.1F;
            return (double)(f * 2.0F * f * 2.0F + entity.getWidth());
         } else {
            return super.getSquaredMaxAttackDistance(entity);
         }
      }
   }
}
