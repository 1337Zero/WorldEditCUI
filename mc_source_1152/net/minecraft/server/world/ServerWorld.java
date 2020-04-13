package net.minecraft.server.world;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.network.DebugRendererInfoManager;
import net.minecraft.client.network.packet.BlockActionS2CPacket;
import net.minecraft.client.network.packet.BlockBreakingProgressS2CPacket;
import net.minecraft.client.network.packet.EntitySpawnGlobalS2CPacket;
import net.minecraft.client.network.packet.EntityStatusS2CPacket;
import net.minecraft.client.network.packet.ExplosionS2CPacket;
import net.minecraft.client.network.packet.GameStateChangeS2CPacket;
import net.minecraft.client.network.packet.ParticleS2CPacket;
import net.minecraft.client.network.packet.PlaySoundFromEntityS2CPacket;
import net.minecraft.client.network.packet.PlaySoundS2CPacket;
import net.minecraft.client.network.packet.WorldEventS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityInteraction;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.InteractionObserver;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Npc;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.raid.Raid;
import net.minecraft.entity.raid.RaidManager;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.map.MapState;
import net.minecraft.network.Packet;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.structure.StructureManager;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.RegistryTagManager;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.BooleanBiFunction;
import net.minecraft.util.CsvWriter;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.TypeFilterableList;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.ForcedChunkState;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.IdCountsState;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PortalForcer;
import net.minecraft.world.ScheduledTick;
import net.minecraft.world.SessionLockException;
import net.minecraft.world.WanderingTraderManager;
import net.minecraft.world.World;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerWorld extends World {
   private static final Logger LOGGER = LogManager.getLogger();
   private final List<Entity> globalEntities = Lists.newArrayList();
   private final Int2ObjectMap<Entity> entitiesById = new Int2ObjectLinkedOpenHashMap();
   private final Map<UUID, Entity> entitiesByUuid = Maps.newHashMap();
   private final Queue<Entity> entitiesToLoad = Queues.newArrayDeque();
   private final List<ServerPlayerEntity> players = Lists.newArrayList();
   boolean ticking;
   private final MinecraftServer server;
   private final WorldSaveHandler worldSaveHandler;
   public boolean savingDisabled;
   private boolean allPlayersSleeping;
   private int idleTimeout;
   private final PortalForcer portalForcer;
   private final ServerTickScheduler<Block> blockTickScheduler;
   private final ServerTickScheduler<Fluid> fluidTickScheduler;
   private final Set<EntityNavigation> entityNavigations;
   protected final RaidManager raidManager;
   private final ObjectLinkedOpenHashSet<BlockAction> pendingBlockActions;
   private boolean insideTick;
   @Nullable
   private final WanderingTraderManager wanderingTraderManager;

   public ServerWorld(MinecraftServer server, Executor workerExecutor, WorldSaveHandler worldSaveHandler, LevelProperties properties, DimensionType dimensionType, Profiler profiler, WorldGenerationProgressListener worldGenerationProgressListener) {
      super(properties, dimensionType, (world, dimension) -> {
         return new ServerChunkManager((ServerWorld)world, worldSaveHandler.getWorldDir(), worldSaveHandler.getDataFixer(), worldSaveHandler.getStructureManager(), workerExecutor, dimension.createChunkGenerator(), server.getPlayerManager().getViewDistance(), worldGenerationProgressListener, () -> {
            return server.getWorld(DimensionType.OVERWORLD).getPersistentStateManager();
         });
      }, profiler, false);
      this.blockTickScheduler = new ServerTickScheduler(this, (block) -> {
         return block == null || block.getDefaultState().isAir();
      }, Registry.BLOCK::getId, Registry.BLOCK::get, this::tickBlock);
      this.fluidTickScheduler = new ServerTickScheduler(this, (fluid) -> {
         return fluid == null || fluid == Fluids.EMPTY;
      }, Registry.FLUID::getId, Registry.FLUID::get, this::tickFluid);
      this.entityNavigations = Sets.newHashSet();
      this.pendingBlockActions = new ObjectLinkedOpenHashSet();
      this.worldSaveHandler = worldSaveHandler;
      this.server = server;
      this.portalForcer = new PortalForcer(this);
      this.calculateAmbientDarkness();
      this.initWeatherGradients();
      this.getWorldBorder().setMaxWorldBorderRadius(server.getMaxWorldBorderRadius());
      this.raidManager = (RaidManager)this.getPersistentStateManager().getOrCreate(() -> {
         return new RaidManager(this);
      }, RaidManager.nameFor(this.dimension));
      if (!server.isSinglePlayer()) {
         this.getLevelProperties().setGameMode(server.getDefaultGameMode());
      }

      this.wanderingTraderManager = this.dimension.getType() == DimensionType.OVERWORLD ? new WanderingTraderManager(this) : null;
   }

   public Biome getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
      return this.getChunkManager().getChunkGenerator().getBiomeSource().getBiomeForNoiseGen(biomeX, biomeY, biomeZ);
   }

   public void tick(BooleanSupplier shouldKeepTicking) {
      Profiler profiler = this.getProfiler();
      this.insideTick = true;
      profiler.push("world border");
      this.getWorldBorder().tick();
      profiler.swap("weather");
      boolean bl = this.isRaining();
      int m;
      if (this.dimension.hasSkyLight()) {
         if (this.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE)) {
            int i = this.properties.getClearWeatherTime();
            m = this.properties.getThunderTime();
            int k = this.properties.getRainTime();
            boolean bl2 = this.properties.isThundering();
            boolean bl3 = this.properties.isRaining();
            if (i > 0) {
               --i;
               m = bl2 ? 0 : 1;
               k = bl3 ? 0 : 1;
               bl2 = false;
               bl3 = false;
            } else {
               if (m > 0) {
                  --m;
                  if (m == 0) {
                     bl2 = !bl2;
                  }
               } else if (bl2) {
                  m = this.random.nextInt(12000) + 3600;
               } else {
                  m = this.random.nextInt(168000) + 12000;
               }

               if (k > 0) {
                  --k;
                  if (k == 0) {
                     bl3 = !bl3;
                  }
               } else if (bl3) {
                  k = this.random.nextInt(12000) + 12000;
               } else {
                  k = this.random.nextInt(168000) + 12000;
               }
            }

            this.properties.setThunderTime(m);
            this.properties.setRainTime(k);
            this.properties.setClearWeatherTime(i);
            this.properties.setThundering(bl2);
            this.properties.setRaining(bl3);
         }

         this.thunderGradientPrev = this.thunderGradient;
         if (this.properties.isThundering()) {
            this.thunderGradient = (float)((double)this.thunderGradient + 0.01D);
         } else {
            this.thunderGradient = (float)((double)this.thunderGradient - 0.01D);
         }

         this.thunderGradient = MathHelper.clamp(this.thunderGradient, 0.0F, 1.0F);
         this.rainGradientPrev = this.rainGradient;
         if (this.properties.isRaining()) {
            this.rainGradient = (float)((double)this.rainGradient + 0.01D);
         } else {
            this.rainGradient = (float)((double)this.rainGradient - 0.01D);
         }

         this.rainGradient = MathHelper.clamp(this.rainGradient, 0.0F, 1.0F);
      }

      if (this.rainGradientPrev != this.rainGradient) {
         this.server.getPlayerManager().sendToDimension(new GameStateChangeS2CPacket(7, this.rainGradient), this.dimension.getType());
      }

      if (this.thunderGradientPrev != this.thunderGradient) {
         this.server.getPlayerManager().sendToDimension(new GameStateChangeS2CPacket(8, this.thunderGradient), this.dimension.getType());
      }

      if (bl != this.isRaining()) {
         if (bl) {
            this.server.getPlayerManager().sendToAll((Packet)(new GameStateChangeS2CPacket(2, 0.0F)));
         } else {
            this.server.getPlayerManager().sendToAll((Packet)(new GameStateChangeS2CPacket(1, 0.0F)));
         }

         this.server.getPlayerManager().sendToAll((Packet)(new GameStateChangeS2CPacket(7, this.rainGradient)));
         this.server.getPlayerManager().sendToAll((Packet)(new GameStateChangeS2CPacket(8, this.thunderGradient)));
      }

      if (this.getLevelProperties().isHardcore() && this.getDifficulty() != Difficulty.HARD) {
         this.getLevelProperties().setDifficulty(Difficulty.HARD);
      }

      if (this.allPlayersSleeping && this.players.stream().noneMatch((serverPlayerEntity) -> {
         return !serverPlayerEntity.isSpectator() && !serverPlayerEntity.isSleepingLongEnough();
      })) {
         this.allPlayersSleeping = false;
         if (this.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
            long l = this.properties.getTimeOfDay() + 24000L;
            this.setTimeOfDay(l - l % 24000L);
         }

         this.method_23660();
         if (this.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE)) {
            this.resetWeather();
         }
      }

      this.calculateAmbientDarkness();
      this.tickTime();
      profiler.swap("chunkSource");
      this.getChunkManager().tick(shouldKeepTicking);
      profiler.swap("tickPending");
      if (this.properties.getGeneratorType() != LevelGeneratorType.DEBUG_ALL_BLOCK_STATES) {
         this.blockTickScheduler.tick();
         this.fluidTickScheduler.tick();
      }

      profiler.swap("raid");
      this.raidManager.tick();
      if (this.wanderingTraderManager != null) {
         this.wanderingTraderManager.tick();
      }

      profiler.swap("blockEvents");
      this.sendBlockActions();
      this.insideTick = false;
      profiler.swap("entities");
      boolean bl4 = !this.players.isEmpty() || !this.getForcedChunks().isEmpty();
      if (bl4) {
         this.resetIdleTimeout();
      }

      if (bl4 || this.idleTimeout++ < 300) {
         this.dimension.update();
         profiler.push("global");

         Entity entity4;
         for(m = 0; m < this.globalEntities.size(); ++m) {
            entity4 = (Entity)this.globalEntities.get(m);
            this.tickEntity((entity) -> {
               ++entity.age;
               entity.tick();
            }, entity4);
            if (entity4.removed) {
               this.globalEntities.remove(m--);
            }
         }

         profiler.swap("regular");
         this.ticking = true;
         ObjectIterator objectIterator = this.entitiesById.int2ObjectEntrySet().iterator();

         label174:
         while(true) {
            Entity entity2;
            while(true) {
               if (!objectIterator.hasNext()) {
                  this.ticking = false;

                  while((entity4 = (Entity)this.entitiesToLoad.poll()) != null) {
                     this.loadEntityUnchecked(entity4);
                  }

                  profiler.pop();
                  this.tickBlockEntities();
                  break label174;
               }

               Entry<Entity> entry = (Entry)objectIterator.next();
               entity2 = (Entity)entry.getValue();
               Entity entity3 = entity2.getVehicle();
               if (!this.server.shouldSpawnAnimals() && (entity2 instanceof AnimalEntity || entity2 instanceof WaterCreatureEntity)) {
                  entity2.remove();
               }

               if (!this.server.shouldSpawnNpcs() && entity2 instanceof Npc) {
                  entity2.remove();
               }

               profiler.push("checkDespawn");
               if (!entity2.removed) {
                  entity2.checkDespawn();
               }

               profiler.pop();
               if (entity3 == null) {
                  break;
               }

               if (entity3.removed || !entity3.hasPassenger(entity2)) {
                  entity2.stopRiding();
                  break;
               }
            }

            profiler.push("tick");
            if (!entity2.removed && !(entity2 instanceof EnderDragonPart)) {
               this.tickEntity(this::tickEntity, entity2);
            }

            profiler.pop();
            profiler.push("remove");
            if (entity2.removed) {
               this.removeEntityFromChunk(entity2);
               objectIterator.remove();
               this.unloadEntity(entity2);
            }

            profiler.pop();
         }
      }

      profiler.pop();
   }

   private void method_23660() {
      ((List)this.players.stream().filter(LivingEntity::isSleeping).collect(Collectors.toList())).forEach((serverPlayerEntity) -> {
         serverPlayerEntity.wakeUp(false, false);
      });
   }

   public void tickChunk(WorldChunk chunk, int randomTickSpeed) {
      ChunkPos chunkPos = chunk.getPos();
      boolean bl = this.isRaining();
      int i = chunkPos.getStartX();
      int j = chunkPos.getStartZ();
      Profiler profiler = this.getProfiler();
      profiler.push("thunder");
      BlockPos blockPos2;
      if (bl && this.isThundering() && this.random.nextInt(100000) == 0) {
         blockPos2 = this.method_18210(this.getRandomPosInChunk(i, 0, j, 15));
         if (this.hasRain(blockPos2)) {
            LocalDifficulty localDifficulty = this.getLocalDifficulty(blockPos2);
            boolean bl2 = this.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING) && this.random.nextDouble() < (double)localDifficulty.getLocalDifficulty() * 0.01D;
            if (bl2) {
               SkeletonHorseEntity skeletonHorseEntity = (SkeletonHorseEntity)EntityType.SKELETON_HORSE.create(this);
               skeletonHorseEntity.setTrapped(true);
               skeletonHorseEntity.setBreedingAge(0);
               skeletonHorseEntity.updatePosition((double)blockPos2.getX(), (double)blockPos2.getY(), (double)blockPos2.getZ());
               this.spawnEntity(skeletonHorseEntity);
            }

            this.addLightning(new LightningEntity(this, (double)blockPos2.getX() + 0.5D, (double)blockPos2.getY(), (double)blockPos2.getZ() + 0.5D, bl2));
         }
      }

      profiler.swap("iceandsnow");
      if (this.random.nextInt(16) == 0) {
         blockPos2 = this.getTopPosition(Heightmap.Type.MOTION_BLOCKING, this.getRandomPosInChunk(i, 0, j, 15));
         BlockPos blockPos3 = blockPos2.down();
         Biome biome = this.getBiome(blockPos2);
         if (biome.canSetSnow(this, blockPos3)) {
            this.setBlockState(blockPos3, Blocks.ICE.getDefaultState());
         }

         if (bl && biome.canSetIce(this, blockPos2)) {
            this.setBlockState(blockPos2, Blocks.SNOW.getDefaultState());
         }

         if (bl && this.getBiome(blockPos3).getPrecipitation() == Biome.Precipitation.RAIN) {
            this.getBlockState(blockPos3).getBlock().rainTick(this, blockPos3);
         }
      }

      profiler.swap("tickBlocks");
      if (randomTickSpeed > 0) {
         ChunkSection[] var17 = chunk.getSectionArray();
         int var19 = var17.length;

         for(int var21 = 0; var21 < var19; ++var21) {
            ChunkSection chunkSection = var17[var21];
            if (chunkSection != WorldChunk.EMPTY_SECTION && chunkSection.hasRandomTicks()) {
               int k = chunkSection.getYOffset();

               for(int l = 0; l < randomTickSpeed; ++l) {
                  BlockPos blockPos4 = this.getRandomPosInChunk(i, k, j, 15);
                  profiler.push("randomTick");
                  BlockState blockState = chunkSection.getBlockState(blockPos4.getX() - i, blockPos4.getY() - k, blockPos4.getZ() - j);
                  if (blockState.hasRandomTicks()) {
                     blockState.randomTick(this, blockPos4, this.random);
                  }

                  FluidState fluidState = blockState.getFluidState();
                  if (fluidState.hasRandomTicks()) {
                     fluidState.onRandomTick(this, blockPos4, this.random);
                  }

                  profiler.pop();
               }
            }
         }
      }

      profiler.pop();
   }

   protected BlockPos method_18210(BlockPos pos) {
      BlockPos blockPos = this.getTopPosition(Heightmap.Type.MOTION_BLOCKING, pos);
      Box box = (new Box(blockPos, new BlockPos(blockPos.getX(), this.getHeight(), blockPos.getZ()))).expand(3.0D);
      List<LivingEntity> list = this.getEntities(LivingEntity.class, box, (livingEntity) -> {
         return livingEntity != null && livingEntity.isAlive() && this.isSkyVisible(livingEntity.getBlockPos());
      });
      if (!list.isEmpty()) {
         return ((LivingEntity)list.get(this.random.nextInt(list.size()))).getBlockPos();
      } else {
         if (blockPos.getY() == -1) {
            blockPos = blockPos.up(2);
         }

         return blockPos;
      }
   }

   public boolean isInsideTick() {
      return this.insideTick;
   }

   public void updatePlayersSleeping() {
      this.allPlayersSleeping = false;
      if (!this.players.isEmpty()) {
         int i = 0;
         int j = 0;
         Iterator var3 = this.players.iterator();

         while(var3.hasNext()) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var3.next();
            if (serverPlayerEntity.isSpectator()) {
               ++i;
            } else if (serverPlayerEntity.isSleeping()) {
               ++j;
            }
         }

         this.allPlayersSleeping = j > 0 && j >= this.players.size() - i;
      }

   }

   public ServerScoreboard getScoreboard() {
      return this.server.getScoreboard();
   }

   private void resetWeather() {
      this.properties.setRainTime(0);
      this.properties.setRaining(false);
      this.properties.setThunderTime(0);
      this.properties.setThundering(false);
   }

   @Environment(EnvType.CLIENT)
   public void setDefaultSpawnClient() {
      if (this.properties.getSpawnY() <= 0) {
         this.properties.setSpawnY(this.getSeaLevel() + 1);
      }

      int i = this.properties.getSpawnX();
      int j = this.properties.getSpawnZ();
      int k = 0;

      while(this.getTopNonAirState(new BlockPos(i, 0, j)).isAir()) {
         i += this.random.nextInt(8) - this.random.nextInt(8);
         j += this.random.nextInt(8) - this.random.nextInt(8);
         ++k;
         if (k == 10000) {
            break;
         }
      }

      this.properties.setSpawnX(i);
      this.properties.setSpawnZ(j);
   }

   public void resetIdleTimeout() {
      this.idleTimeout = 0;
   }

   private void tickFluid(ScheduledTick<Fluid> tick) {
      FluidState fluidState = this.getFluidState(tick.pos);
      if (fluidState.getFluid() == tick.getObject()) {
         fluidState.onScheduledTick(this, tick.pos);
      }

   }

   private void tickBlock(ScheduledTick<Block> tick) {
      BlockState blockState = this.getBlockState(tick.pos);
      if (blockState.getBlock() == tick.getObject()) {
         blockState.scheduledTick(this, tick.pos, this.random);
      }

   }

   public void tickEntity(Entity entity) {
      if (entity instanceof PlayerEntity || this.getChunkManager().shouldTickEntity(entity)) {
         entity.resetPosition(entity.getX(), entity.getY(), entity.getZ());
         entity.prevYaw = entity.yaw;
         entity.prevPitch = entity.pitch;
         if (entity.updateNeeded) {
            ++entity.age;
            Profiler profiler = this.getProfiler();
            profiler.push(() -> {
               return Registry.ENTITY_TYPE.getId(entity.getType()).toString();
            });
            profiler.method_24270("tickNonPassenger");
            entity.tick();
            profiler.pop();
         }

         this.checkChunk(entity);
         if (entity.updateNeeded) {
            Iterator var4 = entity.getPassengerList().iterator();

            while(var4.hasNext()) {
               Entity entity2 = (Entity)var4.next();
               this.method_18763(entity, entity2);
            }
         }

      }
   }

   public void method_18763(Entity entity, Entity entity2) {
      if (!entity2.removed && entity2.getVehicle() == entity) {
         if (entity2 instanceof PlayerEntity || this.getChunkManager().shouldTickEntity(entity2)) {
            entity2.resetPosition(entity2.getX(), entity2.getY(), entity2.getZ());
            entity2.prevYaw = entity2.yaw;
            entity2.prevPitch = entity2.pitch;
            if (entity2.updateNeeded) {
               ++entity2.age;
               Profiler profiler = this.getProfiler();
               profiler.push(() -> {
                  return Registry.ENTITY_TYPE.getId(entity2.getType()).toString();
               });
               profiler.method_24270("tickPassenger");
               entity2.tickRiding();
               profiler.pop();
            }

            this.checkChunk(entity2);
            if (entity2.updateNeeded) {
               Iterator var5 = entity2.getPassengerList().iterator();

               while(var5.hasNext()) {
                  Entity entity3 = (Entity)var5.next();
                  this.method_18763(entity2, entity3);
               }
            }

         }
      } else {
         entity2.stopRiding();
      }
   }

   public void checkChunk(Entity entity) {
      this.getProfiler().push("chunkCheck");
      int i = MathHelper.floor(entity.getX() / 16.0D);
      int j = MathHelper.floor(entity.getY() / 16.0D);
      int k = MathHelper.floor(entity.getZ() / 16.0D);
      if (!entity.updateNeeded || entity.chunkX != i || entity.chunkY != j || entity.chunkZ != k) {
         if (entity.updateNeeded && this.isChunkLoaded(entity.chunkX, entity.chunkZ)) {
            this.getChunk(entity.chunkX, entity.chunkZ).remove(entity, entity.chunkY);
         }

         if (!entity.teleportRequested() && !this.isChunkLoaded(i, k)) {
            entity.updateNeeded = false;
         } else {
            this.getChunk(i, k).addEntity(entity);
         }
      }

      this.getProfiler().pop();
   }

   public boolean canPlayerModifyAt(PlayerEntity player, BlockPos pos) {
      return !this.server.isSpawnProtected(this, pos, player) && this.getWorldBorder().contains(pos);
   }

   public void init(LevelInfo levelInfo) {
      if (!this.dimension.canPlayersSleep()) {
         this.properties.setSpawnPos(BlockPos.ORIGIN.up(this.getChunkManager().getChunkGenerator().getSpawnHeight()));
      } else if (this.properties.getGeneratorType() == LevelGeneratorType.DEBUG_ALL_BLOCK_STATES) {
         this.properties.setSpawnPos(BlockPos.ORIGIN.up());
      } else {
         BiomeSource biomeSource = this.getChunkManager().getChunkGenerator().getBiomeSource();
         List<Biome> list = biomeSource.getSpawnBiomes();
         Random random = new Random(this.getSeed());
         BlockPos blockPos = biomeSource.locateBiome(0, this.getSeaLevel(), 0, 256, list, random);
         ChunkPos chunkPos = blockPos == null ? new ChunkPos(0, 0) : new ChunkPos(blockPos);
         if (blockPos == null) {
            LOGGER.warn("Unable to find spawn biome");
         }

         boolean bl = false;
         Iterator var8 = BlockTags.VALID_SPAWN.values().iterator();

         while(var8.hasNext()) {
            Block block = (Block)var8.next();
            if (biomeSource.getTopMaterials().contains(block.getDefaultState())) {
               bl = true;
               break;
            }
         }

         this.properties.setSpawnPos(chunkPos.getCenterBlockPos().add(8, this.getChunkManager().getChunkGenerator().getSpawnHeight(), 8));
         int i = 0;
         int j = 0;
         int k = 0;
         int l = -1;
         int m = true;

         for(int n = 0; n < 1024; ++n) {
            if (i > -16 && i <= 16 && j > -16 && j <= 16) {
               BlockPos blockPos2 = this.dimension.getSpawningBlockInChunk(new ChunkPos(chunkPos.x + i, chunkPos.z + j), bl);
               if (blockPos2 != null) {
                  this.properties.setSpawnPos(blockPos2);
                  break;
               }
            }

            if (i == j || i < 0 && i == -j || i > 0 && i == 1 - j) {
               int o = k;
               k = -l;
               l = o;
            }

            i += k;
            j += l;
         }

         if (levelInfo.hasBonusChest()) {
            this.placeBonusChest();
         }

      }
   }

   protected void placeBonusChest() {
      ConfiguredFeature<?, ?> configuredFeature = Feature.BONUS_CHEST.configure(FeatureConfig.DEFAULT);
      configuredFeature.generate(this, this.getChunkManager().getChunkGenerator(), this.random, new BlockPos(this.properties.getSpawnX(), this.properties.getSpawnY(), this.properties.getSpawnZ()));
   }

   @Nullable
   public BlockPos getForcedSpawnPoint() {
      return this.dimension.getForcedSpawnPoint();
   }

   public void save(@Nullable ProgressListener progressListener, boolean flush, boolean bl) throws SessionLockException {
      ServerChunkManager serverChunkManager = this.getChunkManager();
      if (!bl) {
         if (progressListener != null) {
            progressListener.method_15412(new TranslatableText("menu.savingLevel", new Object[0]));
         }

         this.saveLevel();
         if (progressListener != null) {
            progressListener.method_15414(new TranslatableText("menu.savingChunks", new Object[0]));
         }

         serverChunkManager.save(flush);
      }
   }

   protected void saveLevel() throws SessionLockException {
      this.checkSessionLock();
      this.dimension.saveWorldData();
      this.getChunkManager().getPersistentStateManager().save();
   }

   public List<Entity> getEntities(@Nullable EntityType<?> entityType, Predicate<? super Entity> predicate) {
      List<Entity> list = Lists.newArrayList();
      ServerChunkManager serverChunkManager = this.getChunkManager();
      ObjectIterator var5 = this.entitiesById.values().iterator();

      while(true) {
         Entity entity;
         do {
            if (!var5.hasNext()) {
               return list;
            }

            entity = (Entity)var5.next();
         } while(entityType != null && entity.getType() != entityType);

         if (serverChunkManager.isChunkLoaded(MathHelper.floor(entity.getX()) >> 4, MathHelper.floor(entity.getZ()) >> 4) && predicate.test(entity)) {
            list.add(entity);
         }
      }
   }

   public List<EnderDragonEntity> getAliveEnderDragons() {
      List<EnderDragonEntity> list = Lists.newArrayList();
      ObjectIterator var2 = this.entitiesById.values().iterator();

      while(var2.hasNext()) {
         Entity entity = (Entity)var2.next();
         if (entity instanceof EnderDragonEntity && entity.isAlive()) {
            list.add((EnderDragonEntity)entity);
         }
      }

      return list;
   }

   public List<ServerPlayerEntity> getPlayers(Predicate<? super ServerPlayerEntity> predicate) {
      List<ServerPlayerEntity> list = Lists.newArrayList();
      Iterator var3 = this.players.iterator();

      while(var3.hasNext()) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var3.next();
         if (predicate.test(serverPlayerEntity)) {
            list.add(serverPlayerEntity);
         }
      }

      return list;
   }

   @Nullable
   public ServerPlayerEntity getRandomAlivePlayer() {
      List<ServerPlayerEntity> list = this.getPlayers(LivingEntity::isAlive);
      return list.isEmpty() ? null : (ServerPlayerEntity)list.get(this.random.nextInt(list.size()));
   }

   public Object2IntMap<EntityCategory> getMobCountsByCategory() {
      Object2IntMap<EntityCategory> object2IntMap = new Object2IntOpenHashMap();
      ObjectIterator var2 = this.entitiesById.values().iterator();

      while(true) {
         Entity entity;
         MobEntity mobEntity;
         do {
            if (!var2.hasNext()) {
               return object2IntMap;
            }

            entity = (Entity)var2.next();
            if (!(entity instanceof MobEntity)) {
               break;
            }

            mobEntity = (MobEntity)entity;
         } while(mobEntity.isPersistent() || mobEntity.cannotDespawn());

         EntityCategory entityCategory = entity.getType().getCategory();
         if (entityCategory != EntityCategory.MISC && this.getChunkManager().method_20727(entity)) {
            object2IntMap.mergeInt(entityCategory, 1, Integer::sum);
         }
      }
   }

   public boolean spawnEntity(Entity entity) {
      return this.addEntity(entity);
   }

   public boolean tryLoadEntity(Entity entity) {
      return this.addEntity(entity);
   }

   public void onDimensionChanged(Entity entity) {
      boolean bl = entity.teleporting;
      entity.teleporting = true;
      this.tryLoadEntity(entity);
      entity.teleporting = bl;
      this.checkChunk(entity);
   }

   public void onPlayerTeleport(ServerPlayerEntity player) {
      this.addPlayer(player);
      this.checkChunk(player);
   }

   public void onPlayerChangeDimension(ServerPlayerEntity player) {
      this.addPlayer(player);
      this.checkChunk(player);
   }

   public void onPlayerConnected(ServerPlayerEntity player) {
      this.addPlayer(player);
   }

   public void onPlayerRespawned(ServerPlayerEntity player) {
      this.addPlayer(player);
   }

   private void addPlayer(ServerPlayerEntity player) {
      Entity entity = (Entity)this.entitiesByUuid.get(player.getUuid());
      if (entity != null) {
         LOGGER.warn("Force-added player with duplicate UUID {}", player.getUuid().toString());
         entity.detach();
         this.removePlayer((ServerPlayerEntity)entity);
      }

      this.players.add(player);
      this.updatePlayersSleeping();
      Chunk chunk = this.getChunk(MathHelper.floor(player.getX() / 16.0D), MathHelper.floor(player.getZ() / 16.0D), ChunkStatus.FULL, true);
      if (chunk instanceof WorldChunk) {
         chunk.addEntity(player);
      }

      this.loadEntityUnchecked(player);
   }

   private boolean addEntity(Entity entity) {
      if (entity.removed) {
         LOGGER.warn("Tried to add entity {} but it was marked as removed already", EntityType.getId(entity.getType()));
         return false;
      } else if (this.checkUuid(entity)) {
         return false;
      } else {
         Chunk chunk = this.getChunk(MathHelper.floor(entity.getX() / 16.0D), MathHelper.floor(entity.getZ() / 16.0D), ChunkStatus.FULL, entity.teleporting);
         if (!(chunk instanceof WorldChunk)) {
            return false;
         } else {
            chunk.addEntity(entity);
            this.loadEntityUnchecked(entity);
            return true;
         }
      }
   }

   public boolean loadEntity(Entity entity) {
      if (this.checkUuid(entity)) {
         return false;
      } else {
         this.loadEntityUnchecked(entity);
         return true;
      }
   }

   private boolean checkUuid(Entity entity) {
      Entity entity2 = (Entity)this.entitiesByUuid.get(entity.getUuid());
      if (entity2 == null) {
         return false;
      } else {
         LOGGER.warn("Keeping entity {} that already exists with UUID {}", EntityType.getId(entity2.getType()), entity.getUuid().toString());
         return true;
      }
   }

   public void unloadEntities(WorldChunk chunk) {
      this.unloadedBlockEntities.addAll(chunk.getBlockEntities().values());
      TypeFilterableList[] var2 = chunk.getEntitySectionArray();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         TypeFilterableList<Entity> typeFilterableList = var2[var4];
         Iterator var6 = typeFilterableList.iterator();

         while(var6.hasNext()) {
            Entity entity = (Entity)var6.next();
            if (!(entity instanceof ServerPlayerEntity)) {
               if (this.ticking) {
                  throw (IllegalStateException)Util.throwOrPause(new IllegalStateException("Removing entity while ticking!"));
               }

               this.entitiesById.remove(entity.getEntityId());
               this.unloadEntity(entity);
            }
         }
      }

   }

   public void unloadEntity(Entity entity) {
      if (entity instanceof EnderDragonEntity) {
         EnderDragonPart[] var2 = ((EnderDragonEntity)entity).getBodyParts();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            EnderDragonPart enderDragonPart = var2[var4];
            enderDragonPart.remove();
         }
      }

      this.entitiesByUuid.remove(entity.getUuid());
      this.getChunkManager().unloadEntity(entity);
      if (entity instanceof ServerPlayerEntity) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
         this.players.remove(serverPlayerEntity);
      }

      this.getScoreboard().resetEntityScore(entity);
      if (entity instanceof MobEntity) {
         this.entityNavigations.remove(((MobEntity)entity).getNavigation());
      }

   }

   private void loadEntityUnchecked(Entity entity) {
      if (this.ticking) {
         this.entitiesToLoad.add(entity);
      } else {
         this.entitiesById.put(entity.getEntityId(), entity);
         if (entity instanceof EnderDragonEntity) {
            EnderDragonPart[] var2 = ((EnderDragonEntity)entity).getBodyParts();
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
               EnderDragonPart enderDragonPart = var2[var4];
               this.entitiesById.put(enderDragonPart.getEntityId(), enderDragonPart);
            }
         }

         this.entitiesByUuid.put(entity.getUuid(), entity);
         this.getChunkManager().loadEntity(entity);
         if (entity instanceof MobEntity) {
            this.entityNavigations.add(((MobEntity)entity).getNavigation());
         }
      }

   }

   public void removeEntity(Entity entity) {
      if (this.ticking) {
         throw (IllegalStateException)Util.throwOrPause(new IllegalStateException("Removing entity while ticking!"));
      } else {
         this.removeEntityFromChunk(entity);
         this.entitiesById.remove(entity.getEntityId());
         this.unloadEntity(entity);
      }
   }

   private void removeEntityFromChunk(Entity entity) {
      Chunk chunk = this.getChunk(entity.chunkX, entity.chunkZ, ChunkStatus.FULL, false);
      if (chunk instanceof WorldChunk) {
         ((WorldChunk)chunk).remove(entity);
      }

   }

   public void removePlayer(ServerPlayerEntity player) {
      player.remove();
      this.removeEntity(player);
      this.updatePlayersSleeping();
   }

   public void addLightning(LightningEntity lightningEntity) {
      this.globalEntities.add(lightningEntity);
      this.server.getPlayerManager().sendToAround((PlayerEntity)null, lightningEntity.getX(), lightningEntity.getY(), lightningEntity.getZ(), 512.0D, this.dimension.getType(), new EntitySpawnGlobalS2CPacket(lightningEntity));
   }

   public void setBlockBreakingInfo(int entityId, BlockPos pos, int progress) {
      Iterator var4 = this.server.getPlayerManager().getPlayerList().iterator();

      while(var4.hasNext()) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var4.next();
         if (serverPlayerEntity != null && serverPlayerEntity.world == this && serverPlayerEntity.getEntityId() != entityId) {
            double d = (double)pos.getX() - serverPlayerEntity.getX();
            double e = (double)pos.getY() - serverPlayerEntity.getY();
            double f = (double)pos.getZ() - serverPlayerEntity.getZ();
            if (d * d + e * e + f * f < 1024.0D) {
               serverPlayerEntity.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(entityId, pos, progress));
            }
         }
      }

   }

   public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
      this.server.getPlayerManager().sendToAround(player, x, y, z, volume > 1.0F ? (double)(16.0F * volume) : 16.0D, this.dimension.getType(), new PlaySoundS2CPacket(sound, category, x, y, z, volume, pitch));
   }

   public void playSoundFromEntity(@Nullable PlayerEntity playerEntity, Entity entity, SoundEvent soundEvent, SoundCategory soundCategory, float volume, float pitch) {
      this.server.getPlayerManager().sendToAround(playerEntity, entity.getX(), entity.getY(), entity.getZ(), volume > 1.0F ? (double)(16.0F * volume) : 16.0D, this.dimension.getType(), new PlaySoundFromEntityS2CPacket(soundEvent, soundCategory, entity, volume, pitch));
   }

   public void playGlobalEvent(int type, BlockPos pos, int data) {
      this.server.getPlayerManager().sendToAll((Packet)(new WorldEventS2CPacket(type, pos, data, true)));
   }

   public void playLevelEvent(@Nullable PlayerEntity player, int eventId, BlockPos blockPos, int data) {
      this.server.getPlayerManager().sendToAround(player, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), 64.0D, this.dimension.getType(), new WorldEventS2CPacket(eventId, blockPos, data, false));
   }

   public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
      this.getChunkManager().markForUpdate(pos);
      VoxelShape voxelShape = oldState.getCollisionShape(this, pos);
      VoxelShape voxelShape2 = newState.getCollisionShape(this, pos);
      if (VoxelShapes.matchesAnywhere(voxelShape, voxelShape2, BooleanBiFunction.NOT_SAME)) {
         Iterator var7 = this.entityNavigations.iterator();

         while(var7.hasNext()) {
            EntityNavigation entityNavigation = (EntityNavigation)var7.next();
            if (!entityNavigation.shouldRecalculatePath()) {
               entityNavigation.method_18053(pos);
            }
         }

      }
   }

   public void sendEntityStatus(Entity entity, byte status) {
      this.getChunkManager().sendToNearbyPlayers(entity, new EntityStatusS2CPacket(entity, status));
   }

   public ServerChunkManager getChunkManager() {
      return (ServerChunkManager)super.getChunkManager();
   }

   public Explosion createExplosion(@Nullable Entity entity, @Nullable DamageSource damageSource, double x, double y, double z, float power, boolean createFire, Explosion.DestructionType destructionType) {
      Explosion explosion = new Explosion(this, entity, x, y, z, power, createFire, destructionType);
      if (damageSource != null) {
         explosion.setDamageSource(damageSource);
      }

      explosion.collectBlocksAndDamageEntities();
      explosion.affectWorld(false);
      if (destructionType == Explosion.DestructionType.NONE) {
         explosion.clearAffectedBlocks();
      }

      Iterator var13 = this.players.iterator();

      while(var13.hasNext()) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var13.next();
         if (serverPlayerEntity.squaredDistanceTo(x, y, z) < 4096.0D) {
            serverPlayerEntity.networkHandler.sendPacket(new ExplosionS2CPacket(x, y, z, power, explosion.getAffectedBlocks(), (Vec3d)explosion.getAffectedPlayers().get(serverPlayerEntity)));
         }
      }

      return explosion;
   }

   public void addBlockAction(BlockPos pos, Block block, int type, int data) {
      this.pendingBlockActions.add(new BlockAction(pos, block, type, data));
   }

   private void sendBlockActions() {
      while(!this.pendingBlockActions.isEmpty()) {
         BlockAction blockAction = (BlockAction)this.pendingBlockActions.removeFirst();
         if (this.method_14174(blockAction)) {
            this.server.getPlayerManager().sendToAround((PlayerEntity)null, (double)blockAction.getPos().getX(), (double)blockAction.getPos().getY(), (double)blockAction.getPos().getZ(), 64.0D, this.dimension.getType(), new BlockActionS2CPacket(blockAction.getPos(), blockAction.getBlock(), blockAction.getType(), blockAction.getData()));
         }
      }

   }

   private boolean method_14174(BlockAction blockAction) {
      BlockState blockState = this.getBlockState(blockAction.getPos());
      return blockState.getBlock() == blockAction.getBlock() ? blockState.onBlockAction(this, blockAction.getPos(), blockAction.getType(), blockAction.getData()) : false;
   }

   public ServerTickScheduler<Block> getBlockTickScheduler() {
      return this.blockTickScheduler;
   }

   public ServerTickScheduler<Fluid> getFluidTickScheduler() {
      return this.fluidTickScheduler;
   }

   @Nonnull
   public MinecraftServer getServer() {
      return this.server;
   }

   public PortalForcer getPortalForcer() {
      return this.portalForcer;
   }

   public StructureManager getStructureManager() {
      return this.worldSaveHandler.getStructureManager();
   }

   public <T extends ParticleEffect> int spawnParticles(T particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed) {
      ParticleS2CPacket particleS2CPacket = new ParticleS2CPacket(particle, false, x, y, z, (float)deltaX, (float)deltaY, (float)deltaZ, (float)speed, count);
      int i = 0;

      for(int j = 0; j < this.players.size(); ++j) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)this.players.get(j);
         if (this.sendToPlayerIfNearby(serverPlayerEntity, false, x, y, z, particleS2CPacket)) {
            ++i;
         }
      }

      return i;
   }

   public <T extends ParticleEffect> boolean spawnParticles(ServerPlayerEntity viewer, T particle, boolean force, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed) {
      Packet<?> packet = new ParticleS2CPacket(particle, force, x, y, z, (float)deltaX, (float)deltaY, (float)deltaZ, (float)speed, count);
      return this.sendToPlayerIfNearby(viewer, force, x, y, z, packet);
   }

   private boolean sendToPlayerIfNearby(ServerPlayerEntity player, boolean force, double x, double y, double z, Packet<?> packet) {
      if (player.getServerWorld() != this) {
         return false;
      } else {
         BlockPos blockPos = player.getBlockPos();
         if (blockPos.isWithinDistance(new Vec3d(x, y, z), force ? 512.0D : 32.0D)) {
            player.networkHandler.sendPacket(packet);
            return true;
         } else {
            return false;
         }
      }
   }

   @Nullable
   public Entity getEntityById(int id) {
      return (Entity)this.entitiesById.get(id);
   }

   @Nullable
   public Entity getEntity(UUID uUID) {
      return (Entity)this.entitiesByUuid.get(uUID);
   }

   @Nullable
   public BlockPos locateStructure(String string, BlockPos blockPos, int i, boolean bl) {
      return this.getChunkManager().getChunkGenerator().locateStructure(this, string, blockPos, i, bl);
   }

   public RecipeManager getRecipeManager() {
      return this.server.getRecipeManager();
   }

   public RegistryTagManager getTagManager() {
      return this.server.getTagManager();
   }

   public void setTime(long time) {
      super.setTime(time);
      this.properties.getScheduledEvents().processEvents(this.server, time);
   }

   public boolean isSavingDisabled() {
      return this.savingDisabled;
   }

   public void checkSessionLock() throws SessionLockException {
      this.worldSaveHandler.checkSessionLock();
   }

   public WorldSaveHandler getSaveHandler() {
      return this.worldSaveHandler;
   }

   public PersistentStateManager getPersistentStateManager() {
      return this.getChunkManager().getPersistentStateManager();
   }

   @Nullable
   public MapState getMapState(String id) {
      return (MapState)this.getServer().getWorld(DimensionType.OVERWORLD).getPersistentStateManager().get(() -> {
         return new MapState(id);
      }, id);
   }

   public void putMapState(MapState mapState) {
      this.getServer().getWorld(DimensionType.OVERWORLD).getPersistentStateManager().set(mapState);
   }

   public int getNextMapId() {
      return ((IdCountsState)this.getServer().getWorld(DimensionType.OVERWORLD).getPersistentStateManager().getOrCreate(IdCountsState::new, "idcounts")).getNextMapId();
   }

   public void setSpawnPos(BlockPos pos) {
      ChunkPos chunkPos = new ChunkPos(new BlockPos(this.properties.getSpawnX(), 0, this.properties.getSpawnZ()));
      super.setSpawnPos(pos);
      this.getChunkManager().removeTicket(ChunkTicketType.START, chunkPos, 11, Unit.INSTANCE);
      this.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(pos), 11, Unit.INSTANCE);
   }

   public LongSet getForcedChunks() {
      ForcedChunkState forcedChunkState = (ForcedChunkState)this.getPersistentStateManager().get(ForcedChunkState::new, "chunks");
      return (LongSet)(forcedChunkState != null ? LongSets.unmodifiable(forcedChunkState.getChunks()) : LongSets.EMPTY_SET);
   }

   public boolean setChunkForced(int x, int z, boolean forced) {
      ForcedChunkState forcedChunkState = (ForcedChunkState)this.getPersistentStateManager().getOrCreate(ForcedChunkState::new, "chunks");
      ChunkPos chunkPos = new ChunkPos(x, z);
      long l = chunkPos.toLong();
      boolean bl2;
      if (forced) {
         bl2 = forcedChunkState.getChunks().add(l);
         if (bl2) {
            this.getChunk(x, z);
         }
      } else {
         bl2 = forcedChunkState.getChunks().remove(l);
      }

      forcedChunkState.setDirty(bl2);
      if (bl2) {
         this.getChunkManager().setChunkForced(chunkPos, forced);
      }

      return bl2;
   }

   public List<ServerPlayerEntity> getPlayers() {
      return this.players;
   }

   public void onBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock) {
      Optional<PointOfInterestType> optional = PointOfInterestType.from(oldBlock);
      Optional<PointOfInterestType> optional2 = PointOfInterestType.from(newBlock);
      if (!Objects.equals(optional, optional2)) {
         BlockPos blockPos = pos.toImmutable();
         optional.ifPresent((pointOfInterestType) -> {
            this.getServer().execute(() -> {
               this.getPointOfInterestStorage().remove(blockPos);
               DebugRendererInfoManager.method_19777(this, blockPos);
            });
         });
         optional2.ifPresent((pointOfInterestType) -> {
            this.getServer().execute(() -> {
               this.getPointOfInterestStorage().add(blockPos, pointOfInterestType);
               DebugRendererInfoManager.method_19776(this, blockPos);
            });
         });
      }
   }

   public PointOfInterestStorage getPointOfInterestStorage() {
      return this.getChunkManager().getPointOfInterestStorage();
   }

   public boolean isNearOccupiedPointOfInterest(BlockPos pos) {
      return this.isNearOccupiedPointOfInterest(pos, 1);
   }

   public boolean isNearOccupiedPointOfInterest(ChunkSectionPos chunkSectionPos) {
      return this.isNearOccupiedPointOfInterest(chunkSectionPos.getCenterPos());
   }

   public boolean isNearOccupiedPointOfInterest(BlockPos pos, int maxDistance) {
      if (maxDistance > 6) {
         return false;
      } else {
         return this.getOccupiedPointOfInterestDistance(ChunkSectionPos.from(pos)) <= maxDistance;
      }
   }

   public int getOccupiedPointOfInterestDistance(ChunkSectionPos pos) {
      return this.getPointOfInterestStorage().getDistanceFromNearestOccupied(pos);
   }

   public RaidManager getRaidManager() {
      return this.raidManager;
   }

   @Nullable
   public Raid getRaidAt(BlockPos pos) {
      return this.raidManager.getRaidAt(pos, 9216);
   }

   public boolean hasRaidAt(BlockPos pos) {
      return this.getRaidAt(pos) != null;
   }

   public void handleInteraction(EntityInteraction interaction, Entity entity, InteractionObserver observer) {
      observer.onInteractionWith(interaction, entity);
   }

   public void method_21625(Path path) throws IOException {
      ThreadedAnvilChunkStorage threadedAnvilChunkStorage = this.getChunkManager().threadedAnvilChunkStorage;
      Writer writer = Files.newBufferedWriter(path.resolve("stats.txt"));
      Throwable var4 = null;

      try {
         writer.write(String.format("spawning_chunks: %d\n", threadedAnvilChunkStorage.getTicketManager().getLevelCount()));
         ObjectIterator var5 = this.getMobCountsByCategory().object2IntEntrySet().iterator();

         while(true) {
            if (!var5.hasNext()) {
               writer.write(String.format("entities: %d\n", this.entitiesById.size()));
               writer.write(String.format("block_entities: %d\n", this.blockEntities.size()));
               writer.write(String.format("block_ticks: %d\n", this.getBlockTickScheduler().method_20825()));
               writer.write(String.format("fluid_ticks: %d\n", this.getFluidTickScheduler().method_20825()));
               writer.write("distance_manager: " + threadedAnvilChunkStorage.getTicketManager().method_21683() + "\n");
               writer.write(String.format("pending_tasks: %d\n", this.getChunkManager().method_21694()));
               break;
            }

            it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<EntityCategory> entry = (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry)var5.next();
            writer.write(String.format("spawn_count.%s: %d\n", ((EntityCategory)entry.getKey()).getName(), entry.getIntValue()));
         }
      } catch (Throwable var164) {
         var4 = var164;
         throw var164;
      } finally {
         if (writer != null) {
            if (var4 != null) {
               try {
                  writer.close();
               } catch (Throwable var153) {
                  var4.addSuppressed(var153);
               }
            } else {
               writer.close();
            }
         }

      }

      CrashReport crashReport = new CrashReport("Level dump", new Exception("dummy"));
      this.addDetailsToCrashReport(crashReport);
      Writer writer2 = Files.newBufferedWriter(path.resolve("example_crash.txt"));
      Throwable var168 = null;

      try {
         writer2.write(crashReport.asString());
      } catch (Throwable var158) {
         var168 = var158;
         throw var158;
      } finally {
         if (writer2 != null) {
            if (var168 != null) {
               try {
                  writer2.close();
               } catch (Throwable var150) {
                  var168.addSuppressed(var150);
               }
            } else {
               writer2.close();
            }
         }

      }

      Path path2 = path.resolve("chunks.csv");
      Writer writer3 = Files.newBufferedWriter(path2);
      Throwable var171 = null;

      try {
         threadedAnvilChunkStorage.exportChunks(writer3);
      } catch (Throwable var157) {
         var171 = var157;
         throw var157;
      } finally {
         if (writer3 != null) {
            if (var171 != null) {
               try {
                  writer3.close();
               } catch (Throwable var148) {
                  var171.addSuppressed(var148);
               }
            } else {
               writer3.close();
            }
         }

      }

      Path path3 = path.resolve("entities.csv");
      Writer writer4 = Files.newBufferedWriter(path3);
      Throwable var7 = null;

      try {
         exportEntities(writer4, this.entitiesById.values());
      } catch (Throwable var156) {
         var7 = var156;
         throw var156;
      } finally {
         if (writer4 != null) {
            if (var7 != null) {
               try {
                  writer4.close();
               } catch (Throwable var149) {
                  var7.addSuppressed(var149);
               }
            } else {
               writer4.close();
            }
         }

      }

      Path path4 = path.resolve("global_entities.csv");
      Writer writer5 = Files.newBufferedWriter(path4);
      Throwable var8 = null;

      try {
         exportEntities(writer5, this.globalEntities);
      } catch (Throwable var155) {
         var8 = var155;
         throw var155;
      } finally {
         if (writer5 != null) {
            if (var8 != null) {
               try {
                  writer5.close();
               } catch (Throwable var151) {
                  var8.addSuppressed(var151);
               }
            } else {
               writer5.close();
            }
         }

      }

      Path path5 = path.resolve("block_entities.csv");
      Writer writer6 = Files.newBufferedWriter(path5);
      Throwable var9 = null;

      try {
         this.exportBlockEntities(writer6);
      } catch (Throwable var154) {
         var9 = var154;
         throw var154;
      } finally {
         if (writer6 != null) {
            if (var9 != null) {
               try {
                  writer6.close();
               } catch (Throwable var152) {
                  var9.addSuppressed(var152);
               }
            } else {
               writer6.close();
            }
         }

      }

   }

   private static void exportEntities(Writer writer, Iterable<Entity> iterable) throws IOException {
      CsvWriter csvWriter = CsvWriter.makeHeader().addColumn("x").addColumn("y").addColumn("z").addColumn("uuid").addColumn("type").addColumn("alive").addColumn("display_name").addColumn("custom_name").startBody(writer);
      Iterator var3 = iterable.iterator();

      while(var3.hasNext()) {
         Entity entity = (Entity)var3.next();
         Text text = entity.getCustomName();
         Text text2 = entity.getDisplayName();
         csvWriter.printRow(entity.getX(), entity.getY(), entity.getZ(), entity.getUuid(), Registry.ENTITY_TYPE.getId(entity.getType()), entity.isAlive(), text2.getString(), text != null ? text.getString() : null);
      }

   }

   private void exportBlockEntities(Writer writer) throws IOException {
      CsvWriter csvWriter = CsvWriter.makeHeader().addColumn("x").addColumn("y").addColumn("z").addColumn("type").startBody(writer);
      Iterator var3 = this.blockEntities.iterator();

      while(var3.hasNext()) {
         BlockEntity blockEntity = (BlockEntity)var3.next();
         BlockPos blockPos = blockEntity.getPos();
         csvWriter.printRow(blockPos.getX(), blockPos.getY(), blockPos.getZ(), Registry.BLOCK_ENTITY_TYPE.getId(blockEntity.getType()));
      }

   }

   @VisibleForTesting
   public void method_23658(BlockBox blockBox) {
      this.pendingBlockActions.removeIf((blockAction) -> {
         return blockBox.contains(blockAction.getPos());
      });
   }
}
