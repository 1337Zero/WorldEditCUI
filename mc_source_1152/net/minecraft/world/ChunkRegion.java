package net.minecraft.world;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;
import net.minecraft.world.level.LevelProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkRegion implements IWorld {
   private static final Logger LOGGER = LogManager.getLogger();
   private final List<Chunk> chunks;
   private final int centerChunkX;
   private final int centerChunkZ;
   private final int width;
   private final ServerWorld world;
   private final long seed;
   private final int seaLevel;
   private final LevelProperties levelProperties;
   private final Random random;
   private final Dimension dimension;
   private final ChunkGeneratorConfig generatorSettings;
   private final TickScheduler<Block> blockTickScheduler = new MultiTickScheduler((blockPos) -> {
      return this.getChunk(blockPos).getBlockTickScheduler();
   });
   private final TickScheduler<Fluid> fluidTickScheduler = new MultiTickScheduler((blockPos) -> {
      return this.getChunk(blockPos).getFluidTickScheduler();
   });
   private final BiomeAccess biomeAccess;

   public ChunkRegion(ServerWorld world, List<Chunk> chunks) {
      int i = MathHelper.floor(Math.sqrt((double)chunks.size()));
      if (i * i != chunks.size()) {
         throw (IllegalStateException)Util.throwOrPause(new IllegalStateException("Cache size is not a square."));
      } else {
         ChunkPos chunkPos = ((Chunk)chunks.get(chunks.size() / 2)).getPos();
         this.chunks = chunks;
         this.centerChunkX = chunkPos.x;
         this.centerChunkZ = chunkPos.z;
         this.width = i;
         this.world = world;
         this.seed = world.getSeed();
         this.generatorSettings = world.getChunkManager().getChunkGenerator().getConfig();
         this.seaLevel = world.getSeaLevel();
         this.levelProperties = world.getLevelProperties();
         this.random = world.getRandom();
         this.dimension = world.getDimension();
         this.biomeAccess = new BiomeAccess(this, LevelProperties.sha256Hash(this.seed), this.dimension.getType().getBiomeAccessType());
      }
   }

   public int getCenterChunkX() {
      return this.centerChunkX;
   }

   public int getCenterChunkZ() {
      return this.centerChunkZ;
   }

   public Chunk getChunk(int chunkX, int chunkZ) {
      return this.getChunk(chunkX, chunkZ, ChunkStatus.EMPTY);
   }

   @Nullable
   public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
      Chunk chunk2;
      if (this.isChunkLoaded(chunkX, chunkZ)) {
         ChunkPos chunkPos = ((Chunk)this.chunks.get(0)).getPos();
         int i = chunkX - chunkPos.x;
         int j = chunkZ - chunkPos.z;
         chunk2 = (Chunk)this.chunks.get(i + j * this.width);
         if (chunk2.getStatus().isAtLeast(leastStatus)) {
            return chunk2;
         }
      } else {
         chunk2 = null;
      }

      if (!create) {
         return null;
      } else {
         Chunk chunk3 = (Chunk)this.chunks.get(0);
         Chunk chunk4 = (Chunk)this.chunks.get(this.chunks.size() - 1);
         LOGGER.error("Requested chunk : {} {}", chunkX, chunkZ);
         LOGGER.error("Region bounds : {} {} | {} {}", chunk3.getPos().x, chunk3.getPos().z, chunk4.getPos().x, chunk4.getPos().z);
         if (chunk2 != null) {
            throw (RuntimeException)Util.throwOrPause(new RuntimeException(String.format("Chunk is not of correct status. Expecting %s, got %s | %s %s", leastStatus, chunk2.getStatus(), chunkX, chunkZ)));
         } else {
            throw (RuntimeException)Util.throwOrPause(new RuntimeException(String.format("We are asking a region for a chunk out of bound | %s %s", chunkX, chunkZ)));
         }
      }
   }

   public boolean isChunkLoaded(int chunkX, int chunkZ) {
      Chunk chunk = (Chunk)this.chunks.get(0);
      Chunk chunk2 = (Chunk)this.chunks.get(this.chunks.size() - 1);
      return chunkX >= chunk.getPos().x && chunkX <= chunk2.getPos().x && chunkZ >= chunk.getPos().z && chunkZ <= chunk2.getPos().z;
   }

   public BlockState getBlockState(BlockPos pos) {
      return this.getChunk(pos.getX() >> 4, pos.getZ() >> 4).getBlockState(pos);
   }

   public FluidState getFluidState(BlockPos pos) {
      return this.getChunk(pos).getFluidState(pos);
   }

   @Nullable
   public PlayerEntity getClosestPlayer(double x, double y, double z, double maxDistance, Predicate<Entity> targetPredicate) {
      return null;
   }

   public int getAmbientDarkness() {
      return 0;
   }

   public BiomeAccess getBiomeAccess() {
      return this.biomeAccess;
   }

   public Biome getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
      return this.world.getGeneratorStoredBiome(biomeX, biomeY, biomeZ);
   }

   public LightingProvider getLightingProvider() {
      return this.world.getLightingProvider();
   }

   public boolean breakBlock(BlockPos pos, boolean drop, @Nullable Entity breakingEntity) {
      BlockState blockState = this.getBlockState(pos);
      if (blockState.isAir()) {
         return false;
      } else {
         if (drop) {
            BlockEntity blockEntity = blockState.getBlock().hasBlockEntity() ? this.getBlockEntity(pos) : null;
            Block.dropStacks(blockState, this.world, pos, blockEntity, breakingEntity, ItemStack.EMPTY);
         }

         return this.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
      }
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pos) {
      Chunk chunk = this.getChunk(pos);
      BlockEntity blockEntity = chunk.getBlockEntity(pos);
      if (blockEntity != null) {
         return blockEntity;
      } else {
         CompoundTag compoundTag = chunk.getBlockEntityTagAt(pos);
         if (compoundTag != null) {
            if ("DUMMY".equals(compoundTag.getString("id"))) {
               Block block = this.getBlockState(pos).getBlock();
               if (!(block instanceof BlockEntityProvider)) {
                  return null;
               }

               blockEntity = ((BlockEntityProvider)block).createBlockEntity(this.world);
            } else {
               blockEntity = BlockEntity.createFromTag(compoundTag);
            }

            if (blockEntity != null) {
               chunk.setBlockEntity(pos, blockEntity);
               return blockEntity;
            }
         }

         if (chunk.getBlockState(pos).getBlock() instanceof BlockEntityProvider) {
            LOGGER.warn("Tried to access a block entity before it was created. {}", pos);
         }

         return null;
      }
   }

   public boolean setBlockState(BlockPos pos, BlockState state, int flags) {
      Chunk chunk = this.getChunk(pos);
      BlockState blockState = chunk.setBlockState(pos, state, false);
      if (blockState != null) {
         this.world.onBlockChanged(pos, blockState, state);
      }

      Block block = state.getBlock();
      if (block.hasBlockEntity()) {
         if (chunk.getStatus().getChunkType() == ChunkStatus.ChunkType.LEVELCHUNK) {
            chunk.setBlockEntity(pos, ((BlockEntityProvider)block).createBlockEntity(this));
         } else {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putInt("x", pos.getX());
            compoundTag.putInt("y", pos.getY());
            compoundTag.putInt("z", pos.getZ());
            compoundTag.putString("id", "DUMMY");
            chunk.addPendingBlockEntityTag(compoundTag);
         }
      } else if (blockState != null && blockState.getBlock().hasBlockEntity()) {
         chunk.removeBlockEntity(pos);
      }

      if (state.shouldPostProcess(this, pos)) {
         this.markBlockForPostProcessing(pos);
      }

      return true;
   }

   private void markBlockForPostProcessing(BlockPos blockPos) {
      this.getChunk(blockPos).markBlockForPostProcessing(blockPos);
   }

   public boolean spawnEntity(Entity entity) {
      int i = MathHelper.floor(entity.getX() / 16.0D);
      int j = MathHelper.floor(entity.getZ() / 16.0D);
      this.getChunk(i, j).addEntity(entity);
      return true;
   }

   public boolean removeBlock(BlockPos pos, boolean move) {
      return this.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
   }

   public WorldBorder getWorldBorder() {
      return this.world.getWorldBorder();
   }

   public boolean isClient() {
      return false;
   }

   @Deprecated
   public ServerWorld getWorld() {
      return this.world;
   }

   public LevelProperties getLevelProperties() {
      return this.levelProperties;
   }

   public LocalDifficulty getLocalDifficulty(BlockPos pos) {
      if (!this.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
         throw new RuntimeException("We are asking a region for a chunk out of bound");
      } else {
         return new LocalDifficulty(this.world.getDifficulty(), this.world.getTimeOfDay(), 0L, this.world.getMoonSize());
      }
   }

   public ChunkManager getChunkManager() {
      return this.world.getChunkManager();
   }

   public long getSeed() {
      return this.seed;
   }

   public TickScheduler<Block> getBlockTickScheduler() {
      return this.blockTickScheduler;
   }

   public TickScheduler<Fluid> getFluidTickScheduler() {
      return this.fluidTickScheduler;
   }

   public int getSeaLevel() {
      return this.seaLevel;
   }

   public Random getRandom() {
      return this.random;
   }

   public void updateNeighbors(BlockPos pos, Block block) {
   }

   public int getTopY(Heightmap.Type heightmap, int x, int z) {
      return this.getChunk(x >> 4, z >> 4).sampleHeightmap(heightmap, x & 15, z & 15) + 1;
   }

   public void playSound(@Nullable PlayerEntity player, BlockPos blockPos, SoundEvent soundEvent, SoundCategory soundCategory, float volume, float pitch) {
   }

   public void addParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
   }

   public void playLevelEvent(@Nullable PlayerEntity player, int eventId, BlockPos blockPos, int data) {
   }

   @Environment(EnvType.CLIENT)
   public BlockPos getSpawnPos() {
      return this.world.getSpawnPos();
   }

   public Dimension getDimension() {
      return this.dimension;
   }

   public boolean testBlockState(BlockPos blockPos, Predicate<BlockState> state) {
      return state.test(this.getBlockState(blockPos));
   }

   public <T extends Entity> List<T> getEntities(Class<? extends T> entityClass, Box box, @Nullable Predicate<? super T> predicate) {
      return Collections.emptyList();
   }

   public List<Entity> getEntities(@Nullable Entity except, Box box, @Nullable Predicate<? super Entity> predicate) {
      return Collections.emptyList();
   }

   public List<PlayerEntity> getPlayers() {
      return Collections.emptyList();
   }
}
