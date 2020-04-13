package net.minecraft.client.world;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.particle.FireworksSparkParticle;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.sound.EntityTrackingSoundInstance;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.RegistryTagManager;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.CuboidBlockIterator;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.LightType;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;

@Environment(EnvType.CLIENT)
public class ClientWorld extends World {
   private final List<Entity> globalEntities = Lists.newArrayList();
   private final Int2ObjectMap<Entity> regularEntities = new Int2ObjectOpenHashMap();
   private final ClientPlayNetworkHandler netHandler;
   private final WorldRenderer worldRenderer;
   private final MinecraftClient client = MinecraftClient.getInstance();
   private final List<AbstractClientPlayerEntity> players = Lists.newArrayList();
   private int ticksUntilCaveAmbientSound;
   private Scoreboard scoreboard;
   private final Map<String, MapState> mapStates;
   private int lightningTicksLeft;
   private final Object2ObjectArrayMap<ColorResolver, BiomeColorCache> colorCache;

   public ClientWorld(ClientPlayNetworkHandler clientPlayNetworkHandler, LevelInfo levelInfo, DimensionType dimensionType, int chunkLoadDistance, Profiler profiler, WorldRenderer worldRenderer) {
      super(new LevelProperties(levelInfo, "MpServer"), dimensionType, (world, dimension) -> {
         return new ClientChunkManager((ClientWorld)world, chunkLoadDistance);
      }, profiler, true);
      this.ticksUntilCaveAmbientSound = this.random.nextInt(12000);
      this.scoreboard = new Scoreboard();
      this.mapStates = Maps.newHashMap();
      this.colorCache = (Object2ObjectArrayMap)Util.make(new Object2ObjectArrayMap(3), (object2ObjectArrayMap) -> {
         object2ObjectArrayMap.put(BiomeColors.GRASS_COLOR, new BiomeColorCache());
         object2ObjectArrayMap.put(BiomeColors.FOLIAGE_COLOR, new BiomeColorCache());
         object2ObjectArrayMap.put(BiomeColors.WATER_COLOR, new BiomeColorCache());
      });
      this.netHandler = clientPlayNetworkHandler;
      this.worldRenderer = worldRenderer;
      this.setSpawnPos(new BlockPos(8, 64, 8));
      this.calculateAmbientDarkness();
      this.initWeatherGradients();
   }

   public void tick(BooleanSupplier booleanSupplier) {
      this.getWorldBorder().tick();
      this.tickTime();
      this.getProfiler().push("blocks");
      this.chunkManager.tick(booleanSupplier);
      this.tickCaveAmbientSound();
      this.getProfiler().pop();
   }

   public Iterable<Entity> getEntities() {
      return Iterables.concat(this.regularEntities.values(), this.globalEntities);
   }

   public void tickEntities() {
      Profiler profiler = this.getProfiler();
      profiler.push("entities");
      profiler.push("global");

      for(int i = 0; i < this.globalEntities.size(); ++i) {
         Entity entity = (Entity)this.globalEntities.get(i);
         this.tickEntity((entityx) -> {
            ++entityx.age;
            entityx.tick();
         }, entity);
         if (entity.removed) {
            this.globalEntities.remove(i--);
         }
      }

      profiler.swap("regular");
      ObjectIterator objectIterator = this.regularEntities.int2ObjectEntrySet().iterator();

      while(objectIterator.hasNext()) {
         Entry<Entity> entry = (Entry)objectIterator.next();
         Entity entity2 = (Entity)entry.getValue();
         if (!entity2.hasVehicle()) {
            profiler.push("tick");
            if (!entity2.removed) {
               this.tickEntity(this::tickEntity, entity2);
            }

            profiler.pop();
            profiler.push("remove");
            if (entity2.removed) {
               objectIterator.remove();
               this.finishRemovingEntity(entity2);
            }

            profiler.pop();
         }
      }

      profiler.pop();
      this.tickBlockEntities();
      profiler.pop();
   }

   public void tickEntity(Entity entity) {
      if (entity instanceof PlayerEntity || this.getChunkManager().shouldTickEntity(entity)) {
         entity.resetPosition(entity.getX(), entity.getY(), entity.getZ());
         entity.prevYaw = entity.yaw;
         entity.prevPitch = entity.pitch;
         if (entity.updateNeeded || entity.isSpectator()) {
            ++entity.age;
            this.getProfiler().push(() -> {
               return Registry.ENTITY_TYPE.getId(entity.getType()).toString();
            });
            entity.tick();
            this.getProfiler().pop();
         }

         this.checkChunk(entity);
         if (entity.updateNeeded) {
            Iterator var2 = entity.getPassengerList().iterator();

            while(var2.hasNext()) {
               Entity entity2 = (Entity)var2.next();
               this.tickPassenger(entity, entity2);
            }
         }

      }
   }

   public void tickPassenger(Entity entity, Entity passenger) {
      if (!passenger.removed && passenger.getVehicle() == entity) {
         if (passenger instanceof PlayerEntity || this.getChunkManager().shouldTickEntity(passenger)) {
            passenger.resetPosition(passenger.getX(), passenger.getY(), passenger.getZ());
            passenger.prevYaw = passenger.yaw;
            passenger.prevPitch = passenger.pitch;
            if (passenger.updateNeeded) {
               ++passenger.age;
               passenger.tickRiding();
            }

            this.checkChunk(passenger);
            if (passenger.updateNeeded) {
               Iterator var3 = passenger.getPassengerList().iterator();

               while(var3.hasNext()) {
                  Entity entity2 = (Entity)var3.next();
                  this.tickPassenger(passenger, entity2);
               }
            }

         }
      } else {
         passenger.stopRiding();
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

   public void unloadBlockEntities(WorldChunk chunk) {
      this.unloadedBlockEntities.addAll(chunk.getBlockEntities().values());
      this.chunkManager.getLightingProvider().setLightEnabled(chunk.getPos(), false);
   }

   public void resetChunkColor(int i, int j) {
      this.colorCache.forEach((colorResolver, biomeColorCache) -> {
         biomeColorCache.reset(i, j);
      });
   }

   public void reloadColor() {
      this.colorCache.forEach((colorResolver, biomeColorCache) -> {
         biomeColorCache.reset();
      });
   }

   public boolean isChunkLoaded(int chunkX, int chunkZ) {
      return true;
   }

   private void tickCaveAmbientSound() {
      if (this.client.player != null) {
         if (this.ticksUntilCaveAmbientSound > 0) {
            --this.ticksUntilCaveAmbientSound;
         } else {
            BlockPos blockPos = new BlockPos(this.client.player);
            BlockPos blockPos2 = blockPos.add(4 * (this.random.nextInt(3) - 1), 4 * (this.random.nextInt(3) - 1), 4 * (this.random.nextInt(3) - 1));
            double d = blockPos.getSquaredDistance(blockPos2);
            if (d >= 4.0D && d <= 256.0D) {
               BlockState blockState = this.getBlockState(blockPos2);
               if (blockState.isAir() && this.getBaseLightLevel(blockPos2, 0) <= this.random.nextInt(8) && this.getLightLevel(LightType.SKY, blockPos2) <= 0) {
                  this.playSound((double)blockPos2.getX() + 0.5D, (double)blockPos2.getY() + 0.5D, (double)blockPos2.getZ() + 0.5D, SoundEvents.AMBIENT_CAVE, SoundCategory.AMBIENT, 0.7F, 0.8F + this.random.nextFloat() * 0.2F, false);
                  this.ticksUntilCaveAmbientSound = this.random.nextInt(12000) + 6000;
               }
            }

         }
      }
   }

   public int getRegularEntityCount() {
      return this.regularEntities.size();
   }

   public void addLightning(LightningEntity lightning) {
      this.globalEntities.add(lightning);
   }

   public void addPlayer(int id, AbstractClientPlayerEntity player) {
      this.addEntityPrivate(id, player);
      this.players.add(player);
   }

   public void addEntity(int id, Entity entity) {
      this.addEntityPrivate(id, entity);
   }

   private void addEntityPrivate(int id, Entity entity) {
      this.removeEntity(id);
      this.regularEntities.put(id, entity);
      this.getChunkManager().getChunk(MathHelper.floor(entity.getX() / 16.0D), MathHelper.floor(entity.getZ() / 16.0D), ChunkStatus.FULL, true).addEntity(entity);
   }

   public void removeEntity(int i) {
      Entity entity = (Entity)this.regularEntities.remove(i);
      if (entity != null) {
         entity.remove();
         this.finishRemovingEntity(entity);
      }

   }

   private void finishRemovingEntity(Entity entity) {
      entity.detach();
      if (entity.updateNeeded) {
         this.getChunk(entity.chunkX, entity.chunkZ).remove(entity);
      }

      this.players.remove(entity);
   }

   public void addEntitiesToChunk(WorldChunk chunk) {
      ObjectIterator var2 = this.regularEntities.int2ObjectEntrySet().iterator();

      while(var2.hasNext()) {
         Entry<Entity> entry = (Entry)var2.next();
         Entity entity = (Entity)entry.getValue();
         int i = MathHelper.floor(entity.getX() / 16.0D);
         int j = MathHelper.floor(entity.getZ() / 16.0D);
         if (i == chunk.getPos().x && j == chunk.getPos().z) {
            chunk.addEntity(entity);
         }
      }

   }

   @Nullable
   public Entity getEntityById(int id) {
      return (Entity)this.regularEntities.get(id);
   }

   public void setBlockStateWithoutNeighborUpdates(BlockPos blockPos, BlockState blockState) {
      this.setBlockState(blockPos, blockState, 19);
   }

   public void disconnect() {
      this.netHandler.getConnection().disconnect(new TranslatableText("multiplayer.status.quitting", new Object[0]));
   }

   public void doRandomBlockDisplayTicks(int xCenter, int yCenter, int i) {
      int j = true;
      Random random = new Random();
      boolean bl = false;
      if (this.client.interactionManager.getCurrentGameMode() == GameMode.CREATIVE) {
         Iterator var7 = this.client.player.getItemsHand().iterator();

         while(var7.hasNext()) {
            ItemStack itemStack = (ItemStack)var7.next();
            if (itemStack.getItem() == Blocks.BARRIER.asItem()) {
               bl = true;
               break;
            }
         }
      }

      BlockPos.Mutable mutable = new BlockPos.Mutable();

      for(int k = 0; k < 667; ++k) {
         this.randomBlockDisplayTick(xCenter, yCenter, i, 16, random, bl, mutable);
         this.randomBlockDisplayTick(xCenter, yCenter, i, 32, random, bl, mutable);
      }

   }

   public void randomBlockDisplayTick(int xCenter, int yCenter, int zCenter, int radius, Random random, boolean spawnBarrierParticles, BlockPos.Mutable mutable) {
      int i = xCenter + this.random.nextInt(radius) - this.random.nextInt(radius);
      int j = yCenter + this.random.nextInt(radius) - this.random.nextInt(radius);
      int k = zCenter + this.random.nextInt(radius) - this.random.nextInt(radius);
      mutable.set(i, j, k);
      BlockState blockState = this.getBlockState(mutable);
      blockState.getBlock().randomDisplayTick(blockState, this, mutable, random);
      FluidState fluidState = this.getFluidState(mutable);
      if (!fluidState.isEmpty()) {
         fluidState.randomDisplayTick(this, mutable, random);
         ParticleEffect particleEffect = fluidState.getParticle();
         if (particleEffect != null && this.random.nextInt(10) == 0) {
            boolean bl = blockState.isSideSolidFullSquare(this, mutable, Direction.DOWN);
            BlockPos blockPos = mutable.down();
            this.addParticle(blockPos, this.getBlockState(blockPos), particleEffect, bl);
         }
      }

      if (spawnBarrierParticles && blockState.getBlock() == Blocks.BARRIER) {
         this.addParticle(ParticleTypes.BARRIER, (double)i + 0.5D, (double)j + 0.5D, (double)k + 0.5D, 0.0D, 0.0D, 0.0D);
      }

   }

   private void addParticle(BlockPos pos, BlockState state, ParticleEffect parameters, boolean bl) {
      if (state.getFluidState().isEmpty()) {
         VoxelShape voxelShape = state.getCollisionShape(this, pos);
         double d = voxelShape.getMaximum(Direction.Axis.Y);
         if (d < 1.0D) {
            if (bl) {
               this.addParticle((double)pos.getX(), (double)(pos.getX() + 1), (double)pos.getZ(), (double)(pos.getZ() + 1), (double)(pos.getY() + 1) - 0.05D, parameters);
            }
         } else if (!state.matches(BlockTags.IMPERMEABLE)) {
            double e = voxelShape.getMinimum(Direction.Axis.Y);
            if (e > 0.0D) {
               this.addParticle(pos, parameters, voxelShape, (double)pos.getY() + e - 0.05D);
            } else {
               BlockPos blockPos = pos.down();
               BlockState blockState = this.getBlockState(blockPos);
               VoxelShape voxelShape2 = blockState.getCollisionShape(this, blockPos);
               double f = voxelShape2.getMaximum(Direction.Axis.Y);
               if (f < 1.0D && blockState.getFluidState().isEmpty()) {
                  this.addParticle(pos, parameters, voxelShape, (double)pos.getY() - 0.05D);
               }
            }
         }

      }
   }

   private void addParticle(BlockPos pos, ParticleEffect parameters, VoxelShape shape, double y) {
      this.addParticle((double)pos.getX() + shape.getMinimum(Direction.Axis.X), (double)pos.getX() + shape.getMaximum(Direction.Axis.X), (double)pos.getZ() + shape.getMinimum(Direction.Axis.Z), (double)pos.getZ() + shape.getMaximum(Direction.Axis.Z), y, parameters);
   }

   private void addParticle(double minX, double maxX, double minZ, double maxZ, double y, ParticleEffect parameters) {
      this.addParticle(parameters, MathHelper.lerp(this.random.nextDouble(), minX, maxX), y, MathHelper.lerp(this.random.nextDouble(), minZ, maxZ), 0.0D, 0.0D, 0.0D);
   }

   public void finishRemovingEntities() {
      ObjectIterator objectIterator = this.regularEntities.int2ObjectEntrySet().iterator();

      while(objectIterator.hasNext()) {
         Entry<Entity> entry = (Entry)objectIterator.next();
         Entity entity = (Entity)entry.getValue();
         if (entity.removed) {
            objectIterator.remove();
            this.finishRemovingEntity(entity);
         }
      }

   }

   public CrashReportSection addDetailsToCrashReport(CrashReport report) {
      CrashReportSection crashReportSection = super.addDetailsToCrashReport(report);
      crashReportSection.add("Server brand", () -> {
         return this.client.player.getServerBrand();
      });
      crashReportSection.add("Server type", () -> {
         return this.client.getServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server";
      });
      return crashReportSection;
   }

   public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
      if (player == this.client.player) {
         this.playSound(x, y, z, sound, category, volume, pitch, false);
      }

   }

   public void playSoundFromEntity(@Nullable PlayerEntity playerEntity, Entity entity, SoundEvent soundEvent, SoundCategory soundCategory, float volume, float pitch) {
      if (playerEntity == this.client.player) {
         this.client.getSoundManager().play(new EntityTrackingSoundInstance(soundEvent, soundCategory, entity));
      }

   }

   public void playSound(BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance) {
      this.playSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, sound, category, volume, pitch, useDistance);
   }

   public void playSound(double x, double y, double z, SoundEvent sound, SoundCategory soundCategory, float f, float g, boolean bl) {
      double d = this.client.gameRenderer.getCamera().getPos().squaredDistanceTo(x, y, z);
      PositionedSoundInstance positionedSoundInstance = new PositionedSoundInstance(sound, soundCategory, f, g, (float)x, (float)y, (float)z);
      if (bl && d > 100.0D) {
         double e = Math.sqrt(d) / 40.0D;
         this.client.getSoundManager().play(positionedSoundInstance, (int)(e * 20.0D));
      } else {
         this.client.getSoundManager().play(positionedSoundInstance);
      }

   }

   public void addFireworkParticle(double x, double y, double z, double velocityX, double velocityY, double velocityZ, @Nullable CompoundTag tag) {
      this.client.particleManager.addParticle(new FireworksSparkParticle.FireworkParticle(this, x, y, z, velocityX, velocityY, velocityZ, this.client.particleManager, tag));
   }

   public void sendPacket(Packet<?> packet) {
      this.netHandler.sendPacket(packet);
   }

   public RecipeManager getRecipeManager() {
      return this.netHandler.getRecipeManager();
   }

   public void setScoreboard(Scoreboard scoreboard) {
      this.scoreboard = scoreboard;
   }

   public void setTimeOfDay(long time) {
      if (time < 0L) {
         time = -time;
         ((GameRules.BooleanRule)this.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE)).set(false, (MinecraftServer)null);
      } else {
         ((GameRules.BooleanRule)this.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE)).set(true, (MinecraftServer)null);
      }

      super.setTimeOfDay(time);
   }

   public TickScheduler<Block> getBlockTickScheduler() {
      return DummyClientTickScheduler.get();
   }

   public TickScheduler<Fluid> getFluidTickScheduler() {
      return DummyClientTickScheduler.get();
   }

   public ClientChunkManager getChunkManager() {
      return (ClientChunkManager)super.getChunkManager();
   }

   @Nullable
   public MapState getMapState(String id) {
      return (MapState)this.mapStates.get(id);
   }

   public void putMapState(MapState mapState) {
      this.mapStates.put(mapState.getId(), mapState);
   }

   public int getNextMapId() {
      return 0;
   }

   public Scoreboard getScoreboard() {
      return this.scoreboard;
   }

   public RegistryTagManager getTagManager() {
      return this.netHandler.getTagManager();
   }

   public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
      this.worldRenderer.updateBlock(this, pos, oldState, newState, flags);
   }

   public void checkBlockRerender(BlockPos pos, BlockState old, BlockState updated) {
      this.worldRenderer.checkBlockRerender(pos, old, updated);
   }

   public void scheduleBlockRenders(int x, int y, int z) {
      this.worldRenderer.scheduleBlockRenders(x, y, z);
   }

   public void setBlockBreakingInfo(int entityId, BlockPos pos, int progress) {
      this.worldRenderer.setBlockBreakingInfo(entityId, pos, progress);
   }

   public void playGlobalEvent(int type, BlockPos pos, int data) {
      this.worldRenderer.playGlobalEvent(type, pos, data);
   }

   public void playLevelEvent(@Nullable PlayerEntity player, int eventId, BlockPos blockPos, int data) {
      try {
         this.worldRenderer.playLevelEvent(player, eventId, blockPos, data);
      } catch (Throwable var8) {
         CrashReport crashReport = CrashReport.create(var8, "Playing level event");
         CrashReportSection crashReportSection = crashReport.addElement("Level event being played");
         crashReportSection.add("Block coordinates", (Object)CrashReportSection.createPositionString(blockPos));
         crashReportSection.add("Event source", (Object)player);
         crashReportSection.add("Event type", (Object)eventId);
         crashReportSection.add("Event data", (Object)data);
         throw new CrashException(crashReport);
      }
   }

   public void addParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      this.worldRenderer.addParticle(parameters, parameters.getType().shouldAlwaysSpawn(), x, y, z, velocityX, velocityY, velocityZ);
   }

   public void addParticle(ParticleEffect parameters, boolean alwaysSpawn, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      this.worldRenderer.addParticle(parameters, parameters.getType().shouldAlwaysSpawn() || alwaysSpawn, x, y, z, velocityX, velocityY, velocityZ);
   }

   public void addImportantParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      this.worldRenderer.addParticle(parameters, false, true, x, y, z, velocityX, velocityY, velocityZ);
   }

   public void addImportantParticle(ParticleEffect parameters, boolean alwaysSpawn, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      this.worldRenderer.addParticle(parameters, parameters.getType().shouldAlwaysSpawn() || alwaysSpawn, true, x, y, z, velocityX, velocityY, velocityZ);
   }

   public List<AbstractClientPlayerEntity> getPlayers() {
      return this.players;
   }

   public Biome getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
      return Biomes.PLAINS;
   }

   public float method_23783(float f) {
      float g = this.getSkyAngle(f);
      float h = 1.0F - (MathHelper.cos(g * 6.2831855F) * 2.0F + 0.2F);
      h = MathHelper.clamp(h, 0.0F, 1.0F);
      h = 1.0F - h;
      h = (float)((double)h * (1.0D - (double)(this.getRainGradient(f) * 5.0F) / 16.0D));
      h = (float)((double)h * (1.0D - (double)(this.getThunderGradient(f) * 5.0F) / 16.0D));
      return h * 0.8F + 0.2F;
   }

   public Vec3d method_23777(BlockPos blockPos, float f) {
      float g = this.getSkyAngle(f);
      float h = MathHelper.cos(g * 6.2831855F) * 2.0F + 0.5F;
      h = MathHelper.clamp(h, 0.0F, 1.0F);
      Biome biome = this.getBiome(blockPos);
      int i = biome.getSkyColor();
      float j = (float)(i >> 16 & 255) / 255.0F;
      float k = (float)(i >> 8 & 255) / 255.0F;
      float l = (float)(i & 255) / 255.0F;
      j *= h;
      k *= h;
      l *= h;
      float m = this.getRainGradient(f);
      float p;
      float s;
      if (m > 0.0F) {
         p = (j * 0.3F + k * 0.59F + l * 0.11F) * 0.6F;
         s = 1.0F - m * 0.75F;
         j = j * s + p * (1.0F - s);
         k = k * s + p * (1.0F - s);
         l = l * s + p * (1.0F - s);
      }

      p = this.getThunderGradient(f);
      if (p > 0.0F) {
         s = (j * 0.3F + k * 0.59F + l * 0.11F) * 0.2F;
         float r = 1.0F - p * 0.75F;
         j = j * r + s * (1.0F - r);
         k = k * r + s * (1.0F - r);
         l = l * r + s * (1.0F - r);
      }

      if (this.lightningTicksLeft > 0) {
         s = (float)this.lightningTicksLeft - f;
         if (s > 1.0F) {
            s = 1.0F;
         }

         s *= 0.45F;
         j = j * (1.0F - s) + 0.8F * s;
         k = k * (1.0F - s) + 0.8F * s;
         l = l * (1.0F - s) + 1.0F * s;
      }

      return new Vec3d((double)j, (double)k, (double)l);
   }

   public Vec3d getCloudsColor(float tickDelta) {
      float f = this.getSkyAngle(tickDelta);
      float g = MathHelper.cos(f * 6.2831855F) * 2.0F + 0.5F;
      g = MathHelper.clamp(g, 0.0F, 1.0F);
      float h = 1.0F;
      float i = 1.0F;
      float j = 1.0F;
      float k = this.getRainGradient(tickDelta);
      float n;
      float o;
      if (k > 0.0F) {
         n = (h * 0.3F + i * 0.59F + j * 0.11F) * 0.6F;
         o = 1.0F - k * 0.95F;
         h = h * o + n * (1.0F - o);
         i = i * o + n * (1.0F - o);
         j = j * o + n * (1.0F - o);
      }

      h *= g * 0.9F + 0.1F;
      i *= g * 0.9F + 0.1F;
      j *= g * 0.85F + 0.15F;
      n = this.getThunderGradient(tickDelta);
      if (n > 0.0F) {
         o = (h * 0.3F + i * 0.59F + j * 0.11F) * 0.2F;
         float p = 1.0F - n * 0.95F;
         h = h * p + o * (1.0F - p);
         i = i * p + o * (1.0F - p);
         j = j * p + o * (1.0F - p);
      }

      return new Vec3d((double)h, (double)i, (double)j);
   }

   public Vec3d getFogColor(float tickDelta) {
      float f = this.getSkyAngle(tickDelta);
      return this.dimension.getFogColor(f, tickDelta);
   }

   public float method_23787(float f) {
      float g = this.getSkyAngle(f);
      float h = 1.0F - (MathHelper.cos(g * 6.2831855F) * 2.0F + 0.25F);
      h = MathHelper.clamp(h, 0.0F, 1.0F);
      return h * h * 0.5F;
   }

   public double getSkyDarknessHeight() {
      return this.properties.getGeneratorType() == LevelGeneratorType.FLAT ? 0.0D : 63.0D;
   }

   public int getLightningTicksLeft() {
      return this.lightningTicksLeft;
   }

   public void setLightningTicksLeft(int lightningTicksLeft) {
      this.lightningTicksLeft = lightningTicksLeft;
   }

   public int getColor(BlockPos pos, ColorResolver colorResolver) {
      BiomeColorCache biomeColorCache = (BiomeColorCache)this.colorCache.get(colorResolver);
      return biomeColorCache.getBiomeColor(pos, () -> {
         return this.calculateColor(pos, colorResolver);
      });
   }

   public int calculateColor(BlockPos pos, ColorResolver colorResolver) {
      int i = MinecraftClient.getInstance().options.biomeBlendRadius;
      if (i == 0) {
         return colorResolver.getColor(this.getBiome(pos), (double)pos.getX(), (double)pos.getZ());
      } else {
         int j = (i * 2 + 1) * (i * 2 + 1);
         int k = 0;
         int l = 0;
         int m = 0;
         CuboidBlockIterator cuboidBlockIterator = new CuboidBlockIterator(pos.getX() - i, pos.getY(), pos.getZ() - i, pos.getX() + i, pos.getY(), pos.getZ() + i);

         int n;
         for(BlockPos.Mutable mutable = new BlockPos.Mutable(); cuboidBlockIterator.step(); m += n & 255) {
            mutable.set(cuboidBlockIterator.getX(), cuboidBlockIterator.getY(), cuboidBlockIterator.getZ());
            n = colorResolver.getColor(this.getBiome(mutable), (double)mutable.getX(), (double)mutable.getZ());
            k += (n & 16711680) >> 16;
            l += (n & '\uff00') >> 8;
         }

         return (k / j & 255) << 16 | (l / j & 255) << 8 | m / j & 255;
      }
   }
}
