package net.minecraft.entity;

import com.mojang.datafixers.DataFixUtils;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.Schemas;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EnderCrystalEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.LeadKnotEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.StrayEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.entity.mob.ZombiePigmanEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.SalmonEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.TraderLlamaEntity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.entity.thrown.SnowballEntity;
import net.minecraft.entity.thrown.ThrownEggEntity;
import net.minecraft.entity.thrown.ThrownEnderpearlEntity;
import net.minecraft.entity.thrown.ThrownExperienceBottleEntity;
import net.minecraft.entity.thrown.ThrownPotionEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.entity.vehicle.SpawnerMinecartEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tag.Tag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityType<T extends Entity> {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final EntityType<AreaEffectCloudEntity> AREA_EFFECT_CLOUD;
   public static final EntityType<ArmorStandEntity> ARMOR_STAND;
   public static final EntityType<ArrowEntity> ARROW;
   public static final EntityType<BatEntity> BAT;
   public static final EntityType<BeeEntity> BEE;
   public static final EntityType<BlazeEntity> BLAZE;
   public static final EntityType<BoatEntity> BOAT;
   public static final EntityType<CatEntity> CAT;
   public static final EntityType<CaveSpiderEntity> CAVE_SPIDER;
   public static final EntityType<ChickenEntity> CHICKEN;
   public static final EntityType<CodEntity> COD;
   public static final EntityType<CowEntity> COW;
   public static final EntityType<CreeperEntity> CREEPER;
   public static final EntityType<DonkeyEntity> DONKEY;
   public static final EntityType<DolphinEntity> DOLPHIN;
   public static final EntityType<DragonFireballEntity> DRAGON_FIREBALL;
   public static final EntityType<DrownedEntity> DROWNED;
   public static final EntityType<ElderGuardianEntity> ELDER_GUARDIAN;
   public static final EntityType<EnderCrystalEntity> END_CRYSTAL;
   public static final EntityType<EnderDragonEntity> ENDER_DRAGON;
   public static final EntityType<EndermanEntity> ENDERMAN;
   public static final EntityType<EndermiteEntity> ENDERMITE;
   public static final EntityType<EvokerFangsEntity> EVOKER_FANGS;
   public static final EntityType<EvokerEntity> EVOKER;
   public static final EntityType<ExperienceOrbEntity> EXPERIENCE_ORB;
   public static final EntityType<EnderEyeEntity> EYE_OF_ENDER;
   public static final EntityType<FallingBlockEntity> FALLING_BLOCK;
   public static final EntityType<FireworkEntity> FIREWORK_ROCKET;
   public static final EntityType<FoxEntity> FOX;
   public static final EntityType<GhastEntity> GHAST;
   public static final EntityType<GiantEntity> GIANT;
   public static final EntityType<GuardianEntity> GUARDIAN;
   public static final EntityType<HorseEntity> HORSE;
   public static final EntityType<HuskEntity> HUSK;
   public static final EntityType<IllusionerEntity> ILLUSIONER;
   public static final EntityType<ItemEntity> ITEM;
   public static final EntityType<ItemFrameEntity> ITEM_FRAME;
   public static final EntityType<FireballEntity> FIREBALL;
   public static final EntityType<LeadKnotEntity> LEASH_KNOT;
   public static final EntityType<LlamaEntity> LLAMA;
   public static final EntityType<LlamaSpitEntity> LLAMA_SPIT;
   public static final EntityType<MagmaCubeEntity> MAGMA_CUBE;
   public static final EntityType<MinecartEntity> MINECART;
   public static final EntityType<ChestMinecartEntity> CHEST_MINECART;
   public static final EntityType<CommandBlockMinecartEntity> COMMAND_BLOCK_MINECART;
   public static final EntityType<FurnaceMinecartEntity> FURNACE_MINECART;
   public static final EntityType<HopperMinecartEntity> HOPPER_MINECART;
   public static final EntityType<SpawnerMinecartEntity> SPAWNER_MINECART;
   public static final EntityType<TntMinecartEntity> TNT_MINECART;
   public static final EntityType<MuleEntity> MULE;
   public static final EntityType<MooshroomEntity> MOOSHROOM;
   public static final EntityType<OcelotEntity> OCELOT;
   public static final EntityType<PaintingEntity> PAINTING;
   public static final EntityType<PandaEntity> PANDA;
   public static final EntityType<ParrotEntity> PARROT;
   public static final EntityType<PigEntity> PIG;
   public static final EntityType<PufferfishEntity> PUFFERFISH;
   public static final EntityType<ZombiePigmanEntity> ZOMBIE_PIGMAN;
   public static final EntityType<PolarBearEntity> POLAR_BEAR;
   public static final EntityType<TntEntity> TNT;
   public static final EntityType<RabbitEntity> RABBIT;
   public static final EntityType<SalmonEntity> SALMON;
   public static final EntityType<SheepEntity> SHEEP;
   public static final EntityType<ShulkerEntity> SHULKER;
   public static final EntityType<ShulkerBulletEntity> SHULKER_BULLET;
   public static final EntityType<SilverfishEntity> SILVERFISH;
   public static final EntityType<SkeletonEntity> SKELETON;
   public static final EntityType<SkeletonHorseEntity> SKELETON_HORSE;
   public static final EntityType<SlimeEntity> SLIME;
   public static final EntityType<SmallFireballEntity> SMALL_FIREBALL;
   public static final EntityType<SnowGolemEntity> SNOW_GOLEM;
   public static final EntityType<SnowballEntity> SNOWBALL;
   public static final EntityType<SpectralArrowEntity> SPECTRAL_ARROW;
   public static final EntityType<SpiderEntity> SPIDER;
   public static final EntityType<SquidEntity> SQUID;
   public static final EntityType<StrayEntity> STRAY;
   public static final EntityType<TraderLlamaEntity> TRADER_LLAMA;
   public static final EntityType<TropicalFishEntity> TROPICAL_FISH;
   public static final EntityType<TurtleEntity> TURTLE;
   public static final EntityType<ThrownEggEntity> EGG;
   public static final EntityType<ThrownEnderpearlEntity> ENDER_PEARL;
   public static final EntityType<ThrownExperienceBottleEntity> EXPERIENCE_BOTTLE;
   public static final EntityType<ThrownPotionEntity> POTION;
   public static final EntityType<TridentEntity> TRIDENT;
   public static final EntityType<VexEntity> VEX;
   public static final EntityType<VillagerEntity> VILLAGER;
   public static final EntityType<IronGolemEntity> IRON_GOLEM;
   public static final EntityType<VindicatorEntity> VINDICATOR;
   public static final EntityType<PillagerEntity> PILLAGER;
   public static final EntityType<WanderingTraderEntity> WANDERING_TRADER;
   public static final EntityType<WitchEntity> WITCH;
   public static final EntityType<WitherEntity> WITHER;
   public static final EntityType<WitherSkeletonEntity> WITHER_SKELETON;
   public static final EntityType<WitherSkullEntity> WITHER_SKULL;
   public static final EntityType<WolfEntity> WOLF;
   public static final EntityType<ZombieEntity> ZOMBIE;
   public static final EntityType<ZombieHorseEntity> ZOMBIE_HORSE;
   public static final EntityType<ZombieVillagerEntity> ZOMBIE_VILLAGER;
   public static final EntityType<PhantomEntity> PHANTOM;
   public static final EntityType<RavagerEntity> RAVAGER;
   public static final EntityType<LightningEntity> LIGHTNING_BOLT;
   public static final EntityType<PlayerEntity> PLAYER;
   public static final EntityType<FishingBobberEntity> FISHING_BOBBER;
   private final EntityType.EntityFactory<T> factory;
   private final EntityCategory category;
   private final boolean saveable;
   private final boolean summonable;
   private final boolean fireImmune;
   private final boolean spawnableFarFromPlayer;
   @Nullable
   private String translationKey;
   @Nullable
   private Text name;
   @Nullable
   private Identifier lootTableId;
   private final EntityDimensions dimensions;

   private static <T extends Entity> EntityType<T> register(String id, EntityType.Builder<T> type) {
      return (EntityType)Registry.register(Registry.ENTITY_TYPE, (String)id, type.build(id));
   }

   public static Identifier getId(EntityType<?> type) {
      return Registry.ENTITY_TYPE.getId(type);
   }

   public static Optional<EntityType<?>> get(String id) {
      return Registry.ENTITY_TYPE.getOrEmpty(Identifier.tryParse(id));
   }

   public EntityType(EntityType.EntityFactory<T> factory, EntityCategory category, boolean saveable, boolean summonable, boolean fireImmune, boolean spawnableFarFromPlayer, EntityDimensions dimensions) {
      this.factory = factory;
      this.category = category;
      this.spawnableFarFromPlayer = spawnableFarFromPlayer;
      this.saveable = saveable;
      this.summonable = summonable;
      this.fireImmune = fireImmune;
      this.dimensions = dimensions;
   }

   @Nullable
   public Entity spawnFromItemStack(World world, @Nullable ItemStack stack, @Nullable PlayerEntity player, BlockPos pos, SpawnType spawnType, boolean alignPosition, boolean invertY) {
      return this.spawn(world, stack == null ? null : stack.getTag(), stack != null && stack.hasCustomName() ? stack.getName() : null, player, pos, spawnType, alignPosition, invertY);
   }

   @Nullable
   public T spawn(World world, @Nullable CompoundTag itemTag, @Nullable Text name, @Nullable PlayerEntity player, BlockPos pos, SpawnType spawnType, boolean alignPosition, boolean invertY) {
      T entity = this.create(world, itemTag, name, player, pos, spawnType, alignPosition, invertY);
      world.spawnEntity(entity);
      return entity;
   }

   @Nullable
   public T create(World world, @Nullable CompoundTag itemTag, @Nullable Text name, @Nullable PlayerEntity player, BlockPos pos, SpawnType spawnType, boolean alignPosition, boolean invertY) {
      T entity = this.create(world);
      if (entity == null) {
         return null;
      } else {
         double e;
         if (alignPosition) {
            entity.updatePosition((double)pos.getX() + 0.5D, (double)(pos.getY() + 1), (double)pos.getZ() + 0.5D);
            e = getOriginY(world, pos, invertY, entity.getBoundingBox());
         } else {
            e = 0.0D;
         }

         entity.refreshPositionAndAngles((double)pos.getX() + 0.5D, (double)pos.getY() + e, (double)pos.getZ() + 0.5D, MathHelper.wrapDegrees(world.random.nextFloat() * 360.0F), 0.0F);
         if (entity instanceof MobEntity) {
            MobEntity mobEntity = (MobEntity)entity;
            mobEntity.headYaw = mobEntity.yaw;
            mobEntity.bodyYaw = mobEntity.yaw;
            mobEntity.initialize(world, world.getLocalDifficulty(new BlockPos(mobEntity)), spawnType, (EntityData)null, itemTag);
            mobEntity.playAmbientSound();
         }

         if (name != null && entity instanceof LivingEntity) {
            entity.setCustomName(name);
         }

         loadFromEntityTag(world, player, entity, itemTag);
         return entity;
      }
   }

   protected static double getOriginY(WorldView worldView, BlockPos pos, boolean invertY, Box boundingBox) {
      Box box = new Box(pos);
      if (invertY) {
         box = box.stretch(0.0D, -1.0D, 0.0D);
      }

      Stream<VoxelShape> stream = worldView.getCollisions((Entity)null, box, Collections.emptySet());
      return 1.0D + VoxelShapes.calculateMaxOffset(Direction.Axis.Y, boundingBox, stream, invertY ? -2.0D : -1.0D);
   }

   public static void loadFromEntityTag(World world, @Nullable PlayerEntity player, @Nullable Entity entity, @Nullable CompoundTag itemTag) {
      if (itemTag != null && itemTag.contains("EntityTag", 10)) {
         MinecraftServer minecraftServer = world.getServer();
         if (minecraftServer != null && entity != null) {
            if (world.isClient || !entity.entityDataRequiresOperator() || player != null && minecraftServer.getPlayerManager().isOperator(player.getGameProfile())) {
               CompoundTag compoundTag = entity.toTag(new CompoundTag());
               UUID uUID = entity.getUuid();
               compoundTag.copyFrom(itemTag.getCompound("EntityTag"));
               entity.setUuid(uUID);
               entity.fromTag(compoundTag);
            }
         }
      }
   }

   public boolean isSaveable() {
      return this.saveable;
   }

   public boolean isSummonable() {
      return this.summonable;
   }

   public boolean isFireImmune() {
      return this.fireImmune;
   }

   public boolean isSpawnableFarFromPlayer() {
      return this.spawnableFarFromPlayer;
   }

   public EntityCategory getCategory() {
      return this.category;
   }

   public String getTranslationKey() {
      if (this.translationKey == null) {
         this.translationKey = Util.createTranslationKey("entity", Registry.ENTITY_TYPE.getId(this));
      }

      return this.translationKey;
   }

   public Text getName() {
      if (this.name == null) {
         this.name = new TranslatableText(this.getTranslationKey(), new Object[0]);
      }

      return this.name;
   }

   public Identifier getLootTableId() {
      if (this.lootTableId == null) {
         Identifier identifier = Registry.ENTITY_TYPE.getId(this);
         this.lootTableId = new Identifier(identifier.getNamespace(), "entities/" + identifier.getPath());
      }

      return this.lootTableId;
   }

   public float getWidth() {
      return this.dimensions.width;
   }

   public float getHeight() {
      return this.dimensions.height;
   }

   @Nullable
   public T create(World world) {
      return this.factory.create(this, world);
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   public static Entity createInstanceFromId(int type, World world) {
      return newInstance(world, (EntityType)Registry.ENTITY_TYPE.get(type));
   }

   public static Optional<Entity> getEntityFromTag(CompoundTag tag, World world) {
      return Util.ifPresentOrElse(fromTag(tag).map((entityType) -> {
         return entityType.create(world);
      }), (entity) -> {
         entity.fromTag(tag);
      }, () -> {
         LOGGER.warn("Skipping Entity with id {}", tag.getString("id"));
      });
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   private static Entity newInstance(World world, @Nullable EntityType<?> type) {
      return type == null ? null : type.create(world);
   }

   public Box createSimpleBoundingBox(double feetX, double feetY, double feetZ) {
      float f = this.getWidth() / 2.0F;
      return new Box(feetX - (double)f, feetY, feetZ - (double)f, feetX + (double)f, feetY + (double)this.getHeight(), feetZ + (double)f);
   }

   public EntityDimensions getDimensions() {
      return this.dimensions;
   }

   public static Optional<EntityType<?>> fromTag(CompoundTag compoundTag) {
      return Registry.ENTITY_TYPE.getOrEmpty(new Identifier(compoundTag.getString("id")));
   }

   @Nullable
   public static Entity loadEntityWithPassengers(CompoundTag compoundTag, World world, Function<Entity, Entity> entityProcessor) {
      return (Entity)loadEntityFromTag(compoundTag, world).map(entityProcessor).map((entity) -> {
         if (compoundTag.contains("Passengers", 9)) {
            ListTag listTag = compoundTag.getList("Passengers", 10);

            for(int i = 0; i < listTag.size(); ++i) {
               Entity entity2 = loadEntityWithPassengers(listTag.getCompound(i), world, entityProcessor);
               if (entity2 != null) {
                  entity2.startRiding(entity, true);
               }
            }
         }

         return entity;
      }).orElse((Object)null);
   }

   private static Optional<Entity> loadEntityFromTag(CompoundTag compoundTag, World world) {
      try {
         return getEntityFromTag(compoundTag, world);
      } catch (RuntimeException var3) {
         LOGGER.warn("Exception loading entity: ", var3);
         return Optional.empty();
      }
   }

   public int getMaxTrackDistance() {
      if (this == PLAYER) {
         return 32;
      } else if (this == END_CRYSTAL) {
         return 16;
      } else if (this != ENDER_DRAGON && this != TNT && this != FALLING_BLOCK && this != ITEM_FRAME && this != LEASH_KNOT && this != PAINTING && this != ARMOR_STAND && this != EXPERIENCE_ORB && this != AREA_EFFECT_CLOUD && this != EVOKER_FANGS) {
         return this != FISHING_BOBBER && this != ARROW && this != SPECTRAL_ARROW && this != TRIDENT && this != SMALL_FIREBALL && this != DRAGON_FIREBALL && this != FIREBALL && this != WITHER_SKULL && this != SNOWBALL && this != LLAMA_SPIT && this != ENDER_PEARL && this != EYE_OF_ENDER && this != EGG && this != POTION && this != EXPERIENCE_BOTTLE && this != FIREWORK_ROCKET && this != ITEM ? 5 : 4;
      } else {
         return 10;
      }
   }

   public int getTrackTickInterval() {
      if (this != PLAYER && this != EVOKER_FANGS) {
         if (this == EYE_OF_ENDER) {
            return 4;
         } else if (this == FISHING_BOBBER) {
            return 5;
         } else if (this != SMALL_FIREBALL && this != DRAGON_FIREBALL && this != FIREBALL && this != WITHER_SKULL && this != SNOWBALL && this != LLAMA_SPIT && this != ENDER_PEARL && this != EGG && this != POTION && this != EXPERIENCE_BOTTLE && this != FIREWORK_ROCKET && this != TNT) {
            if (this != ARROW && this != SPECTRAL_ARROW && this != TRIDENT && this != ITEM && this != FALLING_BLOCK && this != EXPERIENCE_ORB) {
               return this != ITEM_FRAME && this != LEASH_KNOT && this != PAINTING && this != AREA_EFFECT_CLOUD && this != END_CRYSTAL ? 3 : Integer.MAX_VALUE;
            } else {
               return 20;
            }
         } else {
            return 10;
         }
      } else {
         return 2;
      }
   }

   public boolean alwaysUpdateVelocity() {
      return this != PLAYER && this != LLAMA_SPIT && this != WITHER && this != BAT && this != ITEM_FRAME && this != LEASH_KNOT && this != PAINTING && this != END_CRYSTAL && this != EVOKER_FANGS;
   }

   public boolean isTaggedWith(Tag<EntityType<?>> tag) {
      return tag.contains(this);
   }

   static {
      AREA_EFFECT_CLOUD = register("area_effect_cloud", EntityType.Builder.create(AreaEffectCloudEntity::new, EntityCategory.MISC).makeFireImmune().setDimensions(6.0F, 0.5F));
      ARMOR_STAND = register("armor_stand", EntityType.Builder.create(ArmorStandEntity::new, EntityCategory.MISC).setDimensions(0.5F, 1.975F));
      ARROW = register("arrow", EntityType.Builder.create(ArrowEntity::new, EntityCategory.MISC).setDimensions(0.5F, 0.5F));
      BAT = register("bat", EntityType.Builder.create(BatEntity::new, EntityCategory.AMBIENT).setDimensions(0.5F, 0.9F));
      BEE = register("bee", EntityType.Builder.create(BeeEntity::new, EntityCategory.CREATURE).setDimensions(0.7F, 0.6F));
      BLAZE = register("blaze", EntityType.Builder.create(BlazeEntity::new, EntityCategory.MONSTER).makeFireImmune().setDimensions(0.6F, 1.8F));
      BOAT = register("boat", EntityType.Builder.create(BoatEntity::new, EntityCategory.MISC).setDimensions(1.375F, 0.5625F));
      CAT = register("cat", EntityType.Builder.create(CatEntity::new, EntityCategory.CREATURE).setDimensions(0.6F, 0.7F));
      CAVE_SPIDER = register("cave_spider", EntityType.Builder.create(CaveSpiderEntity::new, EntityCategory.MONSTER).setDimensions(0.7F, 0.5F));
      CHICKEN = register("chicken", EntityType.Builder.create(ChickenEntity::new, EntityCategory.CREATURE).setDimensions(0.4F, 0.7F));
      COD = register("cod", EntityType.Builder.create(CodEntity::new, EntityCategory.WATER_CREATURE).setDimensions(0.5F, 0.3F));
      COW = register("cow", EntityType.Builder.create(CowEntity::new, EntityCategory.CREATURE).setDimensions(0.9F, 1.4F));
      CREEPER = register("creeper", EntityType.Builder.create(CreeperEntity::new, EntityCategory.MONSTER).setDimensions(0.6F, 1.7F));
      DONKEY = register("donkey", EntityType.Builder.create(DonkeyEntity::new, EntityCategory.CREATURE).setDimensions(1.3964844F, 1.5F));
      DOLPHIN = register("dolphin", EntityType.Builder.create(DolphinEntity::new, EntityCategory.WATER_CREATURE).setDimensions(0.9F, 0.6F));
      DRAGON_FIREBALL = register("dragon_fireball", EntityType.Builder.create(DragonFireballEntity::new, EntityCategory.MISC).setDimensions(1.0F, 1.0F));
      DROWNED = register("drowned", EntityType.Builder.create(DrownedEntity::new, EntityCategory.MONSTER).setDimensions(0.6F, 1.95F));
      ELDER_GUARDIAN = register("elder_guardian", EntityType.Builder.create(ElderGuardianEntity::new, EntityCategory.MONSTER).setDimensions(1.9975F, 1.9975F));
      END_CRYSTAL = register("end_crystal", EntityType.Builder.create(EnderCrystalEntity::new, EntityCategory.MISC).setDimensions(2.0F, 2.0F));
      ENDER_DRAGON = register("ender_dragon", EntityType.Builder.create(EnderDragonEntity::new, EntityCategory.MONSTER).makeFireImmune().setDimensions(16.0F, 8.0F));
      ENDERMAN = register("enderman", EntityType.Builder.create(EndermanEntity::new, EntityCategory.MONSTER).setDimensions(0.6F, 2.9F));
      ENDERMITE = register("endermite", EntityType.Builder.create(EndermiteEntity::new, EntityCategory.MONSTER).setDimensions(0.4F, 0.3F));
      EVOKER_FANGS = register("evoker_fangs", EntityType.Builder.create(EvokerFangsEntity::new, EntityCategory.MISC).setDimensions(0.5F, 0.8F));
      EVOKER = register("evoker", EntityType.Builder.create(EvokerEntity::new, EntityCategory.MONSTER).setDimensions(0.6F, 1.95F));
      EXPERIENCE_ORB = register("experience_orb", EntityType.Builder.create(ExperienceOrbEntity::new, EntityCategory.MISC).setDimensions(0.5F, 0.5F));
      EYE_OF_ENDER = register("eye_of_ender", EntityType.Builder.create(EnderEyeEntity::new, EntityCategory.MISC).setDimensions(0.25F, 0.25F));
      FALLING_BLOCK = register("falling_block", EntityType.Builder.create(FallingBlockEntity::new, EntityCategory.MISC).setDimensions(0.98F, 0.98F));
      FIREWORK_ROCKET = register("firework_rocket", EntityType.Builder.create(FireworkEntity::new, EntityCategory.MISC).setDimensions(0.25F, 0.25F));
      FOX = register("fox", EntityType.Builder.create(FoxEntity::new, EntityCategory.CREATURE).setDimensions(0.6F, 0.7F));
      GHAST = register("ghast", EntityType.Builder.create(GhastEntity::new, EntityCategory.MONSTER).makeFireImmune().setDimensions(4.0F, 4.0F));
      GIANT = register("giant", EntityType.Builder.create(GiantEntity::new, EntityCategory.MONSTER).setDimensions(3.6F, 12.0F));
      GUARDIAN = register("guardian", EntityType.Builder.create(GuardianEntity::new, EntityCategory.MONSTER).setDimensions(0.85F, 0.85F));
      HORSE = register("horse", EntityType.Builder.create(HorseEntity::new, EntityCategory.CREATURE).setDimensions(1.3964844F, 1.6F));
      HUSK = register("husk", EntityType.Builder.create(HuskEntity::new, EntityCategory.MONSTER).setDimensions(0.6F, 1.95F));
      ILLUSIONER = register("illusioner", EntityType.Builder.create(IllusionerEntity::new, EntityCategory.MONSTER).setDimensions(0.6F, 1.95F));
      ITEM = register("item", EntityType.Builder.create(ItemEntity::new, EntityCategory.MISC).setDimensions(0.25F, 0.25F));
      ITEM_FRAME = register("item_frame", EntityType.Builder.create(ItemFrameEntity::new, EntityCategory.MISC).setDimensions(0.5F, 0.5F));
      FIREBALL = register("fireball", EntityType.Builder.create(FireballEntity::new, EntityCategory.MISC).setDimensions(1.0F, 1.0F));
      LEASH_KNOT = register("leash_knot", EntityType.Builder.create(LeadKnotEntity::new, EntityCategory.MISC).disableSaving().setDimensions(0.5F, 0.5F));
      LLAMA = register("llama", EntityType.Builder.create(LlamaEntity::new, EntityCategory.CREATURE).setDimensions(0.9F, 1.87F));
      LLAMA_SPIT = register("llama_spit", EntityType.Builder.create(LlamaSpitEntity::new, EntityCategory.MISC).setDimensions(0.25F, 0.25F));
      MAGMA_CUBE = register("magma_cube", EntityType.Builder.create(MagmaCubeEntity::new, EntityCategory.MONSTER).makeFireImmune().setDimensions(2.04F, 2.04F));
      MINECART = register("minecart", EntityType.Builder.create(MinecartEntity::new, EntityCategory.MISC).setDimensions(0.98F, 0.7F));
      CHEST_MINECART = register("chest_minecart", EntityType.Builder.create(ChestMinecartEntity::new, EntityCategory.MISC).setDimensions(0.98F, 0.7F));
      COMMAND_BLOCK_MINECART = register("command_block_minecart", EntityType.Builder.create(CommandBlockMinecartEntity::new, EntityCategory.MISC).setDimensions(0.98F, 0.7F));
      FURNACE_MINECART = register("furnace_minecart", EntityType.Builder.create(FurnaceMinecartEntity::new, EntityCategory.MISC).setDimensions(0.98F, 0.7F));
      HOPPER_MINECART = register("hopper_minecart", EntityType.Builder.create(HopperMinecartEntity::new, EntityCategory.MISC).setDimensions(0.98F, 0.7F));
      SPAWNER_MINECART = register("spawner_minecart", EntityType.Builder.create(SpawnerMinecartEntity::new, EntityCategory.MISC).setDimensions(0.98F, 0.7F));
      TNT_MINECART = register("tnt_minecart", EntityType.Builder.create(TntMinecartEntity::new, EntityCategory.MISC).setDimensions(0.98F, 0.7F));
      MULE = register("mule", EntityType.Builder.create(MuleEntity::new, EntityCategory.CREATURE).setDimensions(1.3964844F, 1.6F));
      MOOSHROOM = register("mooshroom", EntityType.Builder.create(MooshroomEntity::new, EntityCategory.CREATURE).setDimensions(0.9F, 1.4F));
      OCELOT = register("ocelot", EntityType.Builder.create(OcelotEntity::new, EntityCategory.CREATURE).setDimensions(0.6F, 0.7F));
      PAINTING = register("painting", EntityType.Builder.create(PaintingEntity::new, EntityCategory.MISC).setDimensions(0.5F, 0.5F));
      PANDA = register("panda", EntityType.Builder.create(PandaEntity::new, EntityCategory.CREATURE).setDimensions(1.3F, 1.25F));
      PARROT = register("parrot", EntityType.Builder.create(ParrotEntity::new, EntityCategory.CREATURE).setDimensions(0.5F, 0.9F));
      PIG = register("pig", EntityType.Builder.create(PigEntity::new, EntityCategory.CREATURE).setDimensions(0.9F, 0.9F));
      PUFFERFISH = register("pufferfish", EntityType.Builder.create(PufferfishEntity::new, EntityCategory.WATER_CREATURE).setDimensions(0.7F, 0.7F));
      ZOMBIE_PIGMAN = register("zombie_pigman", EntityType.Builder.create(ZombiePigmanEntity::new, EntityCategory.MONSTER).makeFireImmune().setDimensions(0.6F, 1.95F));
      POLAR_BEAR = register("polar_bear", EntityType.Builder.create(PolarBearEntity::new, EntityCategory.CREATURE).setDimensions(1.4F, 1.4F));
      TNT = register("tnt", EntityType.Builder.create(TntEntity::new, EntityCategory.MISC).makeFireImmune().setDimensions(0.98F, 0.98F));
      RABBIT = register("rabbit", EntityType.Builder.create(RabbitEntity::new, EntityCategory.CREATURE).setDimensions(0.4F, 0.5F));
      SALMON = register("salmon", EntityType.Builder.create(SalmonEntity::new, EntityCategory.WATER_CREATURE).setDimensions(0.7F, 0.4F));
      SHEEP = register("sheep", EntityType.Builder.create(SheepEntity::new, EntityCategory.CREATURE).setDimensions(0.9F, 1.3F));
      SHULKER = register("shulker", EntityType.Builder.create(ShulkerEntity::new, EntityCategory.MONSTER).makeFireImmune().spawnableFarFromPlayer().setDimensions(1.0F, 1.0F));
      SHULKER_BULLET = register("shulker_bullet", EntityType.Builder.create(ShulkerBulletEntity::new, EntityCategory.MISC).setDimensions(0.3125F, 0.3125F));
      SILVERFISH = register("silverfish", EntityType.Builder.create(SilverfishEntity::new, EntityCategory.MONSTER).setDimensions(0.4F, 0.3F));
      SKELETON = register("skeleton", EntityType.Builder.create(SkeletonEntity::new, EntityCategory.MONSTER).setDimensions(0.6F, 1.99F));
      SKELETON_HORSE = register("skeleton_horse", EntityType.Builder.create(SkeletonHorseEntity::new, EntityCategory.CREATURE).setDimensions(1.3964844F, 1.6F));
      SLIME = register("slime", EntityType.Builder.create(SlimeEntity::new, EntityCategory.MONSTER).setDimensions(2.04F, 2.04F));
      SMALL_FIREBALL = register("small_fireball", EntityType.Builder.create(SmallFireballEntity::new, EntityCategory.MISC).setDimensions(0.3125F, 0.3125F));
      SNOW_GOLEM = register("snow_golem", EntityType.Builder.create(SnowGolemEntity::new, EntityCategory.MISC).setDimensions(0.7F, 1.9F));
      SNOWBALL = register("snowball", EntityType.Builder.create(SnowballEntity::new, EntityCategory.MISC).setDimensions(0.25F, 0.25F));
      SPECTRAL_ARROW = register("spectral_arrow", EntityType.Builder.create(SpectralArrowEntity::new, EntityCategory.MISC).setDimensions(0.5F, 0.5F));
      SPIDER = register("spider", EntityType.Builder.create(SpiderEntity::new, EntityCategory.MONSTER).setDimensions(1.4F, 0.9F));
      SQUID = register("squid", EntityType.Builder.create(SquidEntity::new, EntityCategory.WATER_CREATURE).setDimensions(0.8F, 0.8F));
      STRAY = register("stray", EntityType.Builder.create(StrayEntity::new, EntityCategory.MONSTER).setDimensions(0.6F, 1.99F));
      TRADER_LLAMA = register("trader_llama", EntityType.Builder.create(TraderLlamaEntity::new, EntityCategory.CREATURE).setDimensions(0.9F, 1.87F));
      TROPICAL_FISH = register("tropical_fish", EntityType.Builder.create(TropicalFishEntity::new, EntityCategory.WATER_CREATURE).setDimensions(0.5F, 0.4F));
      TURTLE = register("turtle", EntityType.Builder.create(TurtleEntity::new, EntityCategory.CREATURE).setDimensions(1.2F, 0.4F));
      EGG = register("egg", EntityType.Builder.create(ThrownEggEntity::new, EntityCategory.MISC).setDimensions(0.25F, 0.25F));
      ENDER_PEARL = register("ender_pearl", EntityType.Builder.create(ThrownEnderpearlEntity::new, EntityCategory.MISC).setDimensions(0.25F, 0.25F));
      EXPERIENCE_BOTTLE = register("experience_bottle", EntityType.Builder.create(ThrownExperienceBottleEntity::new, EntityCategory.MISC).setDimensions(0.25F, 0.25F));
      POTION = register("potion", EntityType.Builder.create(ThrownPotionEntity::new, EntityCategory.MISC).setDimensions(0.25F, 0.25F));
      TRIDENT = register("trident", EntityType.Builder.create(TridentEntity::new, EntityCategory.MISC).setDimensions(0.5F, 0.5F));
      VEX = register("vex", EntityType.Builder.create(VexEntity::new, EntityCategory.MONSTER).makeFireImmune().setDimensions(0.4F, 0.8F));
      VILLAGER = register("villager", EntityType.Builder.create(VillagerEntity::new, EntityCategory.MISC).setDimensions(0.6F, 1.95F));
      IRON_GOLEM = register("iron_golem", EntityType.Builder.create(IronGolemEntity::new, EntityCategory.MISC).setDimensions(1.4F, 2.7F));
      VINDICATOR = register("vindicator", EntityType.Builder.create(VindicatorEntity::new, EntityCategory.MONSTER).setDimensions(0.6F, 1.95F));
      PILLAGER = register("pillager", EntityType.Builder.create(PillagerEntity::new, EntityCategory.MONSTER).spawnableFarFromPlayer().setDimensions(0.6F, 1.95F));
      WANDERING_TRADER = register("wandering_trader", EntityType.Builder.create(WanderingTraderEntity::new, EntityCategory.CREATURE).setDimensions(0.6F, 1.95F));
      WITCH = register("witch", EntityType.Builder.create(WitchEntity::new, EntityCategory.MONSTER).setDimensions(0.6F, 1.95F));
      WITHER = register("wither", EntityType.Builder.create(WitherEntity::new, EntityCategory.MONSTER).makeFireImmune().setDimensions(0.9F, 3.5F));
      WITHER_SKELETON = register("wither_skeleton", EntityType.Builder.create(WitherSkeletonEntity::new, EntityCategory.MONSTER).makeFireImmune().setDimensions(0.7F, 2.4F));
      WITHER_SKULL = register("wither_skull", EntityType.Builder.create(WitherSkullEntity::new, EntityCategory.MISC).setDimensions(0.3125F, 0.3125F));
      WOLF = register("wolf", EntityType.Builder.create(WolfEntity::new, EntityCategory.CREATURE).setDimensions(0.6F, 0.85F));
      ZOMBIE = register("zombie", EntityType.Builder.create(ZombieEntity::new, EntityCategory.MONSTER).setDimensions(0.6F, 1.95F));
      ZOMBIE_HORSE = register("zombie_horse", EntityType.Builder.create(ZombieHorseEntity::new, EntityCategory.CREATURE).setDimensions(1.3964844F, 1.6F));
      ZOMBIE_VILLAGER = register("zombie_villager", EntityType.Builder.create(ZombieVillagerEntity::new, EntityCategory.MONSTER).setDimensions(0.6F, 1.95F));
      PHANTOM = register("phantom", EntityType.Builder.create(PhantomEntity::new, EntityCategory.MONSTER).setDimensions(0.9F, 0.5F));
      RAVAGER = register("ravager", EntityType.Builder.create(RavagerEntity::new, EntityCategory.MONSTER).setDimensions(1.95F, 2.2F));
      LIGHTNING_BOLT = register("lightning_bolt", EntityType.Builder.create(EntityCategory.MISC).disableSaving().setDimensions(0.0F, 0.0F));
      PLAYER = register("player", EntityType.Builder.create(EntityCategory.MISC).disableSaving().disableSummon().setDimensions(0.6F, 1.8F));
      FISHING_BOBBER = register("fishing_bobber", EntityType.Builder.create(EntityCategory.MISC).disableSaving().disableSummon().setDimensions(0.25F, 0.25F));
   }

   public interface EntityFactory<T extends Entity> {
      T create(EntityType<T> type, World world);
   }

   public static class Builder<T extends Entity> {
      private final EntityType.EntityFactory<T> factory;
      private final EntityCategory category;
      private boolean saveable = true;
      private boolean summonable = true;
      private boolean fireImmune;
      private boolean spawnableFarFromPlayer;
      private EntityDimensions size = EntityDimensions.changing(0.6F, 1.8F);

      private Builder(EntityType.EntityFactory<T> factory, EntityCategory category) {
         this.factory = factory;
         this.category = category;
         this.spawnableFarFromPlayer = category == EntityCategory.CREATURE || category == EntityCategory.MISC;
      }

      public static <T extends Entity> EntityType.Builder<T> create(EntityType.EntityFactory<T> factory, EntityCategory category) {
         return new EntityType.Builder(factory, category);
      }

      public static <T extends Entity> EntityType.Builder<T> create(EntityCategory category) {
         return new EntityType.Builder((entityType, world) -> {
            return null;
         }, category);
      }

      public EntityType.Builder<T> setDimensions(float width, float height) {
         this.size = EntityDimensions.changing(width, height);
         return this;
      }

      public EntityType.Builder<T> disableSummon() {
         this.summonable = false;
         return this;
      }

      public EntityType.Builder<T> disableSaving() {
         this.saveable = false;
         return this;
      }

      public EntityType.Builder<T> makeFireImmune() {
         this.fireImmune = true;
         return this;
      }

      public EntityType.Builder<T> spawnableFarFromPlayer() {
         this.spawnableFarFromPlayer = true;
         return this;
      }

      public EntityType<T> build(String id) {
         if (this.saveable) {
            try {
               Schemas.getFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getGameVersion().getWorldVersion())).getChoiceType(TypeReferences.ENTITY_TREE, id);
            } catch (IllegalStateException var3) {
               if (SharedConstants.isDevelopment) {
                  throw var3;
               }

               EntityType.LOGGER.warn("No data fixer registered for entity {}", id);
            }
         }

         return new EntityType(this.factory, this.category, this.saveable, this.summonable, this.fireImmune, this.spawnableFarFromPlayer, this.size);
      }
   }
}
