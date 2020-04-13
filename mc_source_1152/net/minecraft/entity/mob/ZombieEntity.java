package net.minecraft.entity.mob;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.datafixer.NbtOps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.ai.goal.BreakDoorGoal;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.StepAndDestroyBlockGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AbstractTraderEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;

public class ZombieEntity extends HostileEntity {
   protected static final EntityAttribute SPAWN_REINFORCEMENTS = (new ClampedEntityAttribute((EntityAttribute)null, "zombie.spawnReinforcements", 0.0D, 0.0D, 1.0D)).setName("Spawn Reinforcements Chance");
   private static final UUID BABY_SPEED_ID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
   private static final EntityAttributeModifier BABY_SPEED_BONUS;
   private static final TrackedData<Boolean> BABY;
   private static final TrackedData<Integer> field_7427;
   private static final TrackedData<Boolean> CONVERTING_IN_WATER;
   private static final Predicate<Difficulty> DOOR_BREAK_DIFFICULTY_CHECKER;
   private final BreakDoorGoal breakDoorsGoal;
   private boolean canBreakDoors;
   private int inWaterTime;
   private int ticksUntilWaterConversion;

   public ZombieEntity(EntityType<? extends ZombieEntity> type, World world) {
      super(type, world);
      this.breakDoorsGoal = new BreakDoorGoal(this, DOOR_BREAK_DIFFICULTY_CHECKER);
   }

   public ZombieEntity(World world) {
      this(EntityType.ZOMBIE, world);
   }

   protected void initGoals() {
      this.goalSelector.add(4, new ZombieEntity.DestroyEggGoal(this, 1.0D, 3));
      this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.add(8, new LookAroundGoal(this));
      this.initCustomGoals();
   }

   protected void initCustomGoals() {
      this.goalSelector.add(2, new ZombieAttackGoal(this, 1.0D, false));
      this.goalSelector.add(6, new MoveThroughVillageGoal(this, 1.0D, true, 4, this::canBreakDoors));
      this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0D));
      this.targetSelector.add(1, (new RevengeGoal(this, new Class[0])).setGroupRevenge(ZombiePigmanEntity.class));
      this.targetSelector.add(2, new FollowTargetGoal(this, PlayerEntity.class, true));
      this.targetSelector.add(3, new FollowTargetGoal(this, AbstractTraderEntity.class, false));
      this.targetSelector.add(3, new FollowTargetGoal(this, IronGolemEntity.class, true));
      this.targetSelector.add(5, new FollowTargetGoal(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
   }

   protected void initAttributes() {
      super.initAttributes();
      this.getAttributeInstance(EntityAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
      this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.23000000417232513D);
      this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
      this.getAttributeInstance(EntityAttributes.ARMOR).setBaseValue(2.0D);
      this.getAttributes().register(SPAWN_REINFORCEMENTS).setBaseValue(this.random.nextDouble() * 0.10000000149011612D);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.getDataTracker().startTracking(BABY, false);
      this.getDataTracker().startTracking(field_7427, 0);
      this.getDataTracker().startTracking(CONVERTING_IN_WATER, false);
   }

   public boolean isConvertingInWater() {
      return (Boolean)this.getDataTracker().get(CONVERTING_IN_WATER);
   }

   public boolean canBreakDoors() {
      return this.canBreakDoors;
   }

   public void setCanBreakDoors(boolean canBreakDoors) {
      if (this.shouldBreakDoors()) {
         if (this.canBreakDoors != canBreakDoors) {
            this.canBreakDoors = canBreakDoors;
            ((MobNavigation)this.getNavigation()).setCanPathThroughDoors(canBreakDoors);
            if (canBreakDoors) {
               this.goalSelector.add(1, this.breakDoorsGoal);
            } else {
               this.goalSelector.remove(this.breakDoorsGoal);
            }
         }
      } else if (this.canBreakDoors) {
         this.goalSelector.remove(this.breakDoorsGoal);
         this.canBreakDoors = false;
      }

   }

   protected boolean shouldBreakDoors() {
      return true;
   }

   public boolean isBaby() {
      return (Boolean)this.getDataTracker().get(BABY);
   }

   protected int getCurrentExperience(PlayerEntity player) {
      if (this.isBaby()) {
         this.experiencePoints = (int)((float)this.experiencePoints * 2.5F);
      }

      return super.getCurrentExperience(player);
   }

   public void setBaby(boolean baby) {
      this.getDataTracker().set(BABY, baby);
      if (this.world != null && !this.world.isClient) {
         EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
         entityAttributeInstance.removeModifier(BABY_SPEED_BONUS);
         if (baby) {
            entityAttributeInstance.addModifier(BABY_SPEED_BONUS);
         }
      }

   }

   public void onTrackedDataSet(TrackedData<?> data) {
      if (BABY.equals(data)) {
         this.calculateDimensions();
      }

      super.onTrackedDataSet(data);
   }

   protected boolean canConvertInWater() {
      return true;
   }

   public void tick() {
      if (!this.world.isClient && this.isAlive()) {
         if (this.isConvertingInWater()) {
            --this.ticksUntilWaterConversion;
            if (this.ticksUntilWaterConversion < 0) {
               this.convertInWater();
            }
         } else if (this.canConvertInWater()) {
            if (this.isInFluid(FluidTags.WATER)) {
               ++this.inWaterTime;
               if (this.inWaterTime >= 600) {
                  this.setTicksUntilWaterConversion(300);
               }
            } else {
               this.inWaterTime = -1;
            }
         }
      }

      super.tick();
   }

   public void tickMovement() {
      if (this.isAlive()) {
         boolean bl = this.burnsInDaylight() && this.isInDaylight();
         if (bl) {
            ItemStack itemStack = this.getEquippedStack(EquipmentSlot.HEAD);
            if (!itemStack.isEmpty()) {
               if (itemStack.isDamageable()) {
                  itemStack.setDamage(itemStack.getDamage() + this.random.nextInt(2));
                  if (itemStack.getDamage() >= itemStack.getMaxDamage()) {
                     this.sendEquipmentBreakStatus(EquipmentSlot.HEAD);
                     this.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
                  }
               }

               bl = false;
            }

            if (bl) {
               this.setOnFireFor(8);
            }
         }
      }

      super.tickMovement();
   }

   private void setTicksUntilWaterConversion(int ticksUntilWaterConversion) {
      this.ticksUntilWaterConversion = ticksUntilWaterConversion;
      this.getDataTracker().set(CONVERTING_IN_WATER, true);
   }

   protected void convertInWater() {
      this.convertTo(EntityType.DROWNED);
      this.world.playLevelEvent((PlayerEntity)null, 1040, new BlockPos(this), 0);
   }

   protected void convertTo(EntityType<? extends ZombieEntity> entityType) {
      if (!this.removed) {
         ZombieEntity zombieEntity = (ZombieEntity)entityType.create(this.world);
         zombieEntity.copyPositionAndRotation(this);
         zombieEntity.setCanPickUpLoot(this.canPickUpLoot());
         zombieEntity.setCanBreakDoors(zombieEntity.shouldBreakDoors() && this.canBreakDoors());
         zombieEntity.applyAttributeModifiers(zombieEntity.world.getLocalDifficulty(new BlockPos(zombieEntity)).getClampedLocalDifficulty());
         zombieEntity.setBaby(this.isBaby());
         zombieEntity.setAiDisabled(this.isAiDisabled());
         EquipmentSlot[] var3 = EquipmentSlot.values();
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            EquipmentSlot equipmentSlot = var3[var5];
            ItemStack itemStack = this.getEquippedStack(equipmentSlot);
            if (!itemStack.isEmpty()) {
               zombieEntity.equipStack(equipmentSlot, itemStack.copy());
               zombieEntity.setEquipmentDropChance(equipmentSlot, this.getDropChance(equipmentSlot));
               itemStack.setCount(0);
            }
         }

         if (this.hasCustomName()) {
            zombieEntity.setCustomName(this.getCustomName());
            zombieEntity.setCustomNameVisible(this.isCustomNameVisible());
         }

         if (this.isPersistent()) {
            zombieEntity.setPersistent();
         }

         zombieEntity.setInvulnerable(this.isInvulnerable());
         this.world.spawnEntity(zombieEntity);
         this.remove();
      }
   }

   public boolean interactMob(PlayerEntity player, Hand hand) {
      ItemStack itemStack = player.getStackInHand(hand);
      Item item = itemStack.getItem();
      if (item instanceof SpawnEggItem && ((SpawnEggItem)item).isOfSameEntityType(itemStack.getTag(), this.getType())) {
         if (!this.world.isClient) {
            ZombieEntity zombieEntity = (ZombieEntity)this.getType().create(this.world);
            if (zombieEntity != null) {
               zombieEntity.setBaby(true);
               zombieEntity.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
               this.world.spawnEntity(zombieEntity);
               if (itemStack.hasCustomName()) {
                  zombieEntity.setCustomName(itemStack.getName());
               }

               if (!player.abilities.creativeMode) {
                  itemStack.decrement(1);
               }
            }
         }

         return true;
      } else {
         return super.interactMob(player, hand);
      }
   }

   protected boolean burnsInDaylight() {
      return true;
   }

   public boolean damage(DamageSource source, float amount) {
      if (super.damage(source, amount)) {
         LivingEntity livingEntity = this.getTarget();
         if (livingEntity == null && source.getAttacker() instanceof LivingEntity) {
            livingEntity = (LivingEntity)source.getAttacker();
         }

         if (livingEntity != null && this.world.getDifficulty() == Difficulty.HARD && (double)this.random.nextFloat() < this.getAttributeInstance(SPAWN_REINFORCEMENTS).getValue() && this.world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)) {
            int i = MathHelper.floor(this.getX());
            int j = MathHelper.floor(this.getY());
            int k = MathHelper.floor(this.getZ());
            ZombieEntity zombieEntity = new ZombieEntity(this.world);

            for(int l = 0; l < 50; ++l) {
               int m = i + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
               int n = j + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
               int o = k + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
               BlockPos blockPos = new BlockPos(m, n - 1, o);
               if (this.world.getBlockState(blockPos).hasSolidTopSurface(this.world, blockPos, zombieEntity) && this.world.getLightLevel(new BlockPos(m, n, o)) < 10) {
                  zombieEntity.updatePosition((double)m, (double)n, (double)o);
                  if (!this.world.isPlayerInRange((double)m, (double)n, (double)o, 7.0D) && this.world.intersectsEntities(zombieEntity) && this.world.doesNotCollide(zombieEntity) && !this.world.containsFluid(zombieEntity.getBoundingBox())) {
                     this.world.spawnEntity(zombieEntity);
                     zombieEntity.setTarget(livingEntity);
                     zombieEntity.initialize(this.world, this.world.getLocalDifficulty(new BlockPos(zombieEntity)), SpawnType.REINFORCEMENT, (EntityData)null, (CompoundTag)null);
                     this.getAttributeInstance(SPAWN_REINFORCEMENTS).addModifier(new EntityAttributeModifier("Zombie reinforcement caller charge", -0.05000000074505806D, EntityAttributeModifier.Operation.ADDITION));
                     zombieEntity.getAttributeInstance(SPAWN_REINFORCEMENTS).addModifier(new EntityAttributeModifier("Zombie reinforcement callee charge", -0.05000000074505806D, EntityAttributeModifier.Operation.ADDITION));
                     break;
                  }
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean tryAttack(Entity target) {
      boolean bl = super.tryAttack(target);
      if (bl) {
         float f = this.world.getLocalDifficulty(new BlockPos(this)).getLocalDifficulty();
         if (this.getMainHandStack().isEmpty() && this.isOnFire() && this.random.nextFloat() < f * 0.3F) {
            target.setOnFireFor(2 * (int)f);
         }
      }

      return bl;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_ZOMBIE_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_ZOMBIE_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_ZOMBIE_DEATH;
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ENTITY_ZOMBIE_STEP;
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   public EntityGroup getGroup() {
      return EntityGroup.UNDEAD;
   }

   protected void initEquipment(LocalDifficulty difficulty) {
      super.initEquipment(difficulty);
      if (this.random.nextFloat() < (this.world.getDifficulty() == Difficulty.HARD ? 0.05F : 0.01F)) {
         int i = this.random.nextInt(3);
         if (i == 0) {
            this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
         } else {
            this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
         }
      }

   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      if (this.isBaby()) {
         tag.putBoolean("IsBaby", true);
      }

      tag.putBoolean("CanBreakDoors", this.canBreakDoors());
      tag.putInt("InWaterTime", this.isTouchingWater() ? this.inWaterTime : -1);
      tag.putInt("DrownedConversionTime", this.isConvertingInWater() ? this.ticksUntilWaterConversion : -1);
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      if (tag.getBoolean("IsBaby")) {
         this.setBaby(true);
      }

      this.setCanBreakDoors(tag.getBoolean("CanBreakDoors"));
      this.inWaterTime = tag.getInt("InWaterTime");
      if (tag.contains("DrownedConversionTime", 99) && tag.getInt("DrownedConversionTime") > -1) {
         this.setTicksUntilWaterConversion(tag.getInt("DrownedConversionTime"));
      }

   }

   public void onKilledOther(LivingEntity other) {
      super.onKilledOther(other);
      if ((this.world.getDifficulty() == Difficulty.NORMAL || this.world.getDifficulty() == Difficulty.HARD) && other instanceof VillagerEntity) {
         if (this.world.getDifficulty() != Difficulty.HARD && this.random.nextBoolean()) {
            return;
         }

         VillagerEntity villagerEntity = (VillagerEntity)other;
         ZombieVillagerEntity zombieVillagerEntity = (ZombieVillagerEntity)EntityType.ZOMBIE_VILLAGER.create(this.world);
         zombieVillagerEntity.copyPositionAndRotation(villagerEntity);
         villagerEntity.remove();
         zombieVillagerEntity.initialize(this.world, this.world.getLocalDifficulty(new BlockPos(zombieVillagerEntity)), SpawnType.CONVERSION, new ZombieEntity.Data(false), (CompoundTag)null);
         zombieVillagerEntity.setVillagerData(villagerEntity.getVillagerData());
         zombieVillagerEntity.method_21649((Tag)villagerEntity.method_21651().serialize(NbtOps.INSTANCE).getValue());
         zombieVillagerEntity.setOfferData(villagerEntity.getOffers().toTag());
         zombieVillagerEntity.setXp(villagerEntity.getExperience());
         zombieVillagerEntity.setBaby(villagerEntity.isBaby());
         zombieVillagerEntity.setAiDisabled(villagerEntity.isAiDisabled());
         if (villagerEntity.hasCustomName()) {
            zombieVillagerEntity.setCustomName(villagerEntity.getCustomName());
            zombieVillagerEntity.setCustomNameVisible(villagerEntity.isCustomNameVisible());
         }

         if (this.isPersistent()) {
            zombieVillagerEntity.setPersistent();
         }

         zombieVillagerEntity.setInvulnerable(this.isInvulnerable());
         this.world.spawnEntity(zombieVillagerEntity);
         this.world.playLevelEvent((PlayerEntity)null, 1026, new BlockPos(this), 0);
      }

   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return this.isBaby() ? 0.93F : 1.74F;
   }

   protected boolean canPickupItem(ItemStack stack) {
      return stack.getItem() == Items.EGG && this.isBaby() && this.hasVehicle() ? false : super.canPickupItem(stack);
   }

   @Nullable
   public EntityData initialize(IWorld world, LocalDifficulty difficulty, SpawnType spawnType, @Nullable EntityData entityData, @Nullable CompoundTag entityTag) {
      EntityData entityData = super.initialize(world, difficulty, spawnType, entityData, entityTag);
      float f = difficulty.getClampedLocalDifficulty();
      this.setCanPickUpLoot(this.random.nextFloat() < 0.55F * f);
      if (entityData == null) {
         entityData = new ZombieEntity.Data(world.getRandom().nextFloat() < 0.05F);
      }

      if (entityData instanceof ZombieEntity.Data) {
         ZombieEntity.Data data = (ZombieEntity.Data)entityData;
         if (data.baby) {
            this.setBaby(true);
            if ((double)world.getRandom().nextFloat() < 0.05D) {
               List<ChickenEntity> list = world.getEntities(ChickenEntity.class, this.getBoundingBox().expand(5.0D, 3.0D, 5.0D), EntityPredicates.NOT_MOUNTED);
               if (!list.isEmpty()) {
                  ChickenEntity chickenEntity = (ChickenEntity)list.get(0);
                  chickenEntity.setHasJockey(true);
                  this.startRiding(chickenEntity);
               }
            } else if ((double)world.getRandom().nextFloat() < 0.05D) {
               ChickenEntity chickenEntity2 = (ChickenEntity)EntityType.CHICKEN.create(this.world);
               chickenEntity2.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.yaw, 0.0F);
               chickenEntity2.initialize(world, difficulty, SpawnType.JOCKEY, (EntityData)null, (CompoundTag)null);
               chickenEntity2.setHasJockey(true);
               world.spawnEntity(chickenEntity2);
               this.startRiding(chickenEntity2);
            }
         }

         this.setCanBreakDoors(this.shouldBreakDoors() && this.random.nextFloat() < f * 0.1F);
         this.initEquipment(difficulty);
         this.updateEnchantments(difficulty);
      }

      if (this.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
         LocalDate localDate = LocalDate.now();
         int i = localDate.get(ChronoField.DAY_OF_MONTH);
         int j = localDate.get(ChronoField.MONTH_OF_YEAR);
         if (j == 10 && i == 31 && this.random.nextFloat() < 0.25F) {
            this.equipStack(EquipmentSlot.HEAD, new ItemStack(this.random.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
            this.armorDropChances[EquipmentSlot.HEAD.getEntitySlotId()] = 0.0F;
         }
      }

      this.applyAttributeModifiers(f);
      return (EntityData)entityData;
   }

   protected void applyAttributeModifiers(float chanceMultiplier) {
      this.getAttributeInstance(EntityAttributes.KNOCKBACK_RESISTANCE).addModifier(new EntityAttributeModifier("Random spawn bonus", this.random.nextDouble() * 0.05000000074505806D, EntityAttributeModifier.Operation.ADDITION));
      double d = this.random.nextDouble() * 1.5D * (double)chanceMultiplier;
      if (d > 1.0D) {
         this.getAttributeInstance(EntityAttributes.FOLLOW_RANGE).addModifier(new EntityAttributeModifier("Random zombie-spawn bonus", d, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
      }

      if (this.random.nextFloat() < chanceMultiplier * 0.05F) {
         this.getAttributeInstance(SPAWN_REINFORCEMENTS).addModifier(new EntityAttributeModifier("Leader zombie bonus", this.random.nextDouble() * 0.25D + 0.5D, EntityAttributeModifier.Operation.ADDITION));
         this.getAttributeInstance(EntityAttributes.MAX_HEALTH).addModifier(new EntityAttributeModifier("Leader zombie bonus", this.random.nextDouble() * 3.0D + 1.0D, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
         this.setCanBreakDoors(this.shouldBreakDoors());
      }

   }

   public double getHeightOffset() {
      return this.isBaby() ? 0.0D : -0.45D;
   }

   protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
      super.dropEquipment(source, lootingMultiplier, allowDrops);
      Entity entity = source.getAttacker();
      if (entity instanceof CreeperEntity) {
         CreeperEntity creeperEntity = (CreeperEntity)entity;
         if (creeperEntity.shouldDropHead()) {
            creeperEntity.onHeadDropped();
            ItemStack itemStack = this.getSkull();
            if (!itemStack.isEmpty()) {
               this.dropStack(itemStack);
            }
         }
      }

   }

   protected ItemStack getSkull() {
      return new ItemStack(Items.ZOMBIE_HEAD);
   }

   static {
      BABY_SPEED_BONUS = new EntityAttributeModifier(BABY_SPEED_ID, "Baby speed boost", 0.5D, EntityAttributeModifier.Operation.MULTIPLY_BASE);
      BABY = DataTracker.registerData(ZombieEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      field_7427 = DataTracker.registerData(ZombieEntity.class, TrackedDataHandlerRegistry.INTEGER);
      CONVERTING_IN_WATER = DataTracker.registerData(ZombieEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      DOOR_BREAK_DIFFICULTY_CHECKER = (difficulty) -> {
         return difficulty == Difficulty.HARD;
      };
   }

   class DestroyEggGoal extends StepAndDestroyBlockGoal {
      DestroyEggGoal(MobEntityWithAi mob, double speed, int maxYDifference) {
         super(Blocks.TURTLE_EGG, mob, speed, maxYDifference);
      }

      public void tickStepping(IWorld world, BlockPos pos) {
         world.playSound((PlayerEntity)null, pos, SoundEvents.ENTITY_ZOMBIE_DESTROY_EGG, SoundCategory.HOSTILE, 0.5F, 0.9F + ZombieEntity.this.random.nextFloat() * 0.2F);
      }

      public void onDestroyBlock(World world, BlockPos pos) {
         world.playSound((PlayerEntity)null, pos, SoundEvents.ENTITY_TURTLE_EGG_BREAK, SoundCategory.BLOCKS, 0.7F, 0.9F + world.random.nextFloat() * 0.2F);
      }

      public double getDesiredSquaredDistanceToTarget() {
         return 1.14D;
      }
   }

   public class Data implements EntityData {
      public final boolean baby;

      private Data(boolean baby) {
         this.baby = baby;
      }
   }
}
