package net.minecraft.entity.passive;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.GoToEntityTargetGoal;
import net.minecraft.entity.ai.goal.IronGolemLookGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.TrackIronGolemTargetGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.WanderAroundPointOfInterestGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class IronGolemEntity extends GolemEntity {
   protected static final TrackedData<Byte> IRON_GOLEM_FLAGS;
   private int attackTicksLeft;
   private int lookingAtVillagerTicksLeft;

   public IronGolemEntity(EntityType<? extends IronGolemEntity> entityType, World world) {
      super(entityType, world);
      this.stepHeight = 1.0F;
   }

   protected void initGoals() {
      this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0D, true));
      this.goalSelector.add(2, new GoToEntityTargetGoal(this, 0.9D, 32.0F));
      this.goalSelector.add(2, new WanderAroundPointOfInterestGoal(this, 0.6D));
      this.goalSelector.add(3, new MoveThroughVillageGoal(this, 0.6D, false, 4, () -> {
         return false;
      }));
      this.goalSelector.add(5, new IronGolemLookGoal(this));
      this.goalSelector.add(6, new WanderAroundFarGoal(this, 0.6D));
      this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
      this.goalSelector.add(8, new LookAroundGoal(this));
      this.targetSelector.add(1, new TrackIronGolemTargetGoal(this));
      this.targetSelector.add(2, new RevengeGoal(this, new Class[0]));
      this.targetSelector.add(3, new FollowTargetGoal(this, MobEntity.class, 5, false, false, (livingEntity) -> {
         return livingEntity instanceof Monster && !(livingEntity instanceof CreeperEntity);
      }));
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(IRON_GOLEM_FLAGS, (byte)0);
   }

   protected void initAttributes() {
      super.initAttributes();
      this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(100.0D);
      this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
      this.getAttributeInstance(EntityAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
      this.getAttributes().register(EntityAttributes.ATTACK_DAMAGE).setBaseValue(15.0D);
   }

   protected int getNextAirUnderwater(int air) {
      return air;
   }

   protected void pushAway(Entity entity) {
      if (entity instanceof Monster && !(entity instanceof CreeperEntity) && this.getRandom().nextInt(20) == 0) {
         this.setTarget((LivingEntity)entity);
      }

      super.pushAway(entity);
   }

   public void tickMovement() {
      super.tickMovement();
      if (this.attackTicksLeft > 0) {
         --this.attackTicksLeft;
      }

      if (this.lookingAtVillagerTicksLeft > 0) {
         --this.lookingAtVillagerTicksLeft;
      }

      if (squaredHorizontalLength(this.getVelocity()) > 2.500000277905201E-7D && this.random.nextInt(5) == 0) {
         int i = MathHelper.floor(this.getX());
         int j = MathHelper.floor(this.getY() - 0.20000000298023224D);
         int k = MathHelper.floor(this.getZ());
         BlockState blockState = this.world.getBlockState(new BlockPos(i, j, k));
         if (!blockState.isAir()) {
            this.world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, blockState), this.getX() + ((double)this.random.nextFloat() - 0.5D) * (double)this.getWidth(), this.getY() + 0.1D, this.getZ() + ((double)this.random.nextFloat() - 0.5D) * (double)this.getWidth(), 4.0D * ((double)this.random.nextFloat() - 0.5D), 0.5D, ((double)this.random.nextFloat() - 0.5D) * 4.0D);
         }
      }

   }

   public boolean canTarget(EntityType<?> type) {
      if (this.isPlayerCreated() && type == EntityType.PLAYER) {
         return false;
      } else {
         return type == EntityType.CREEPER ? false : super.canTarget(type);
      }
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      tag.putBoolean("PlayerCreated", this.isPlayerCreated());
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      this.setPlayerCreated(tag.getBoolean("PlayerCreated"));
   }

   private float getAttackDamage() {
      return (float)this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).getValue();
   }

   public boolean tryAttack(Entity target) {
      this.attackTicksLeft = 10;
      this.world.sendEntityStatus(this, (byte)4);
      float f = this.getAttackDamage();
      float g = f > 0.0F ? f / 2.0F + (float)this.random.nextInt((int)f) : 0.0F;
      boolean bl = target.damage(DamageSource.mob(this), g);
      if (bl) {
         target.setVelocity(target.getVelocity().add(0.0D, 0.4000000059604645D, 0.0D));
         this.dealDamage(this, target);
      }

      this.playSound(SoundEvents.ENTITY_IRON_GOLEM_ATTACK, 1.0F, 1.0F);
      return bl;
   }

   public boolean damage(DamageSource source, float amount) {
      IronGolemEntity.Crack crack = this.getCrack();
      boolean bl = super.damage(source, amount);
      if (bl && this.getCrack() != crack) {
         this.playSound(SoundEvents.ENTITY_IRON_GOLEM_DAMAGE, 1.0F, 1.0F);
      }

      return bl;
   }

   public IronGolemEntity.Crack getCrack() {
      return IronGolemEntity.Crack.from(this.getHealth() / this.getMaximumHealth());
   }

   @Environment(EnvType.CLIENT)
   public void handleStatus(byte status) {
      if (status == 4) {
         this.attackTicksLeft = 10;
         this.playSound(SoundEvents.ENTITY_IRON_GOLEM_ATTACK, 1.0F, 1.0F);
      } else if (status == 11) {
         this.lookingAtVillagerTicksLeft = 400;
      } else if (status == 34) {
         this.lookingAtVillagerTicksLeft = 0;
      } else {
         super.handleStatus(status);
      }

   }

   @Environment(EnvType.CLIENT)
   public int getAttackTicksLeft() {
      return this.attackTicksLeft;
   }

   public void setLookingAtVillager(boolean lookingAtVillager) {
      if (lookingAtVillager) {
         this.lookingAtVillagerTicksLeft = 400;
         this.world.sendEntityStatus(this, (byte)11);
      } else {
         this.lookingAtVillagerTicksLeft = 0;
         this.world.sendEntityStatus(this, (byte)34);
      }

   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_IRON_GOLEM_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_IRON_GOLEM_DEATH;
   }

   protected boolean interactMob(PlayerEntity player, Hand hand) {
      ItemStack itemStack = player.getStackInHand(hand);
      Item item = itemStack.getItem();
      if (item != Items.IRON_INGOT) {
         return false;
      } else {
         float f = this.getHealth();
         this.heal(25.0F);
         if (this.getHealth() == f) {
            return false;
         } else {
            float g = 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F;
            this.playSound(SoundEvents.ENTITY_IRON_GOLEM_REPAIR, 1.0F, g);
            if (!player.abilities.creativeMode) {
               itemStack.decrement(1);
            }

            return true;
         }
      }
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.ENTITY_IRON_GOLEM_STEP, 1.0F, 1.0F);
   }

   @Environment(EnvType.CLIENT)
   public int getLookingAtVillagerTicks() {
      return this.lookingAtVillagerTicksLeft;
   }

   public boolean isPlayerCreated() {
      return ((Byte)this.dataTracker.get(IRON_GOLEM_FLAGS) & 1) != 0;
   }

   public void setPlayerCreated(boolean playerCrated) {
      byte b = (Byte)this.dataTracker.get(IRON_GOLEM_FLAGS);
      if (playerCrated) {
         this.dataTracker.set(IRON_GOLEM_FLAGS, (byte)(b | 1));
      } else {
         this.dataTracker.set(IRON_GOLEM_FLAGS, (byte)(b & -2));
      }

   }

   public void onDeath(DamageSource source) {
      super.onDeath(source);
   }

   public boolean canSpawn(WorldView world) {
      BlockPos blockPos = new BlockPos(this);
      BlockPos blockPos2 = blockPos.down();
      BlockState blockState = world.getBlockState(blockPos2);
      if (!blockState.hasSolidTopSurface(world, blockPos2, this)) {
         return false;
      } else {
         for(int i = 1; i < 3; ++i) {
            BlockPos blockPos3 = blockPos.up(i);
            BlockState blockState2 = world.getBlockState(blockPos3);
            if (!SpawnHelper.isClearForSpawn(world, blockPos3, blockState2, blockState2.getFluidState())) {
               return false;
            }
         }

         return SpawnHelper.isClearForSpawn(world, blockPos, world.getBlockState(blockPos), Fluids.EMPTY.getDefaultState()) && world.intersectsEntities(this);
      }
   }

   static {
      IRON_GOLEM_FLAGS = DataTracker.registerData(IronGolemEntity.class, TrackedDataHandlerRegistry.BYTE);
   }

   public static enum Crack {
      NONE(1.0F),
      LOW(0.75F),
      MEDIUM(0.5F),
      HIGH(0.25F);

      private static final List<IronGolemEntity.Crack> VALUES = (List)Stream.of(values()).sorted(Comparator.comparingDouble((crack) -> {
         return (double)crack.maxHealthFraction;
      })).collect(ImmutableList.toImmutableList());
      private final float maxHealthFraction;

      private Crack(float maxHealthFraction) {
         this.maxHealthFraction = maxHealthFraction;
      }

      public static IronGolemEntity.Crack from(float healthFraction) {
         Iterator var1 = VALUES.iterator();

         IronGolemEntity.Crack crack;
         do {
            if (!var1.hasNext()) {
               return NONE;
            }

            crack = (IronGolemEntity.Crack)var1.next();
         } while(healthFraction >= crack.maxHealthFraction);

         return crack;
      }
   }
}
