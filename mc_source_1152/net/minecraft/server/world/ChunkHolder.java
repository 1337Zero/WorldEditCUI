package net.minecraft.server.world;

import com.mojang.datafixers.util.Either;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.network.packet.BlockEntityUpdateS2CPacket;
import net.minecraft.client.network.packet.BlockUpdateS2CPacket;
import net.minecraft.client.network.packet.ChunkDataS2CPacket;
import net.minecraft.client.network.packet.ChunkDeltaUpdateS2CPacket;
import net.minecraft.client.network.packet.LightUpdateS2CPacket;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;

public class ChunkHolder {
   public static final Either<Chunk, ChunkHolder.Unloaded> UNLOADED_CHUNK;
   public static final CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> UNLOADED_CHUNK_FUTURE;
   public static final Either<WorldChunk, ChunkHolder.Unloaded> UNLOADED_WORLD_CHUNK;
   private static final CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> UNLOADED_WORLD_CHUNK_FUTURE;
   private static final List<ChunkStatus> CHUNK_STATUSES;
   private static final ChunkHolder.LevelType[] LEVEL_TYPES;
   private final AtomicReferenceArray<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> futuresByStatus;
   private volatile CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> borderFuture;
   private volatile CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> tickingFuture;
   private volatile CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> entityTickingFuture;
   private CompletableFuture<Chunk> future;
   private int lastTickLevel;
   private int level;
   private int completedLevel;
   private final ChunkPos pos;
   private final short[] blockUpdatePositions;
   private int blockUpdateCount;
   private int sectionsNeedingUpdateMask;
   private int lightSentWithBlocksBits;
   private int blockLightUpdateBits;
   private int skyLightUpdateBits;
   private final LightingProvider lightingProvider;
   private final ChunkHolder.LevelUpdateListener levelUpdateListener;
   private final ChunkHolder.PlayersWatchingChunkProvider playersWatchingChunkProvider;
   private boolean field_19238;

   public ChunkHolder(ChunkPos pos, int level, LightingProvider lightingProvider, ChunkHolder.LevelUpdateListener levelUpdateListener, ChunkHolder.PlayersWatchingChunkProvider playersWatchingChunkProvider) {
      this.futuresByStatus = new AtomicReferenceArray(CHUNK_STATUSES.size());
      this.borderFuture = UNLOADED_WORLD_CHUNK_FUTURE;
      this.tickingFuture = UNLOADED_WORLD_CHUNK_FUTURE;
      this.entityTickingFuture = UNLOADED_WORLD_CHUNK_FUTURE;
      this.future = CompletableFuture.completedFuture((Object)null);
      this.blockUpdatePositions = new short[64];
      this.pos = pos;
      this.lightingProvider = lightingProvider;
      this.levelUpdateListener = levelUpdateListener;
      this.playersWatchingChunkProvider = playersWatchingChunkProvider;
      this.lastTickLevel = ThreadedAnvilChunkStorage.MAX_LEVEL + 1;
      this.level = this.lastTickLevel;
      this.completedLevel = this.lastTickLevel;
      this.setLevel(level);
   }

   public CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> getFuture(ChunkStatus leastStatus) {
      CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture = (CompletableFuture)this.futuresByStatus.get(leastStatus.getIndex());
      return completableFuture == null ? UNLOADED_CHUNK_FUTURE : completableFuture;
   }

   public CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> method_21737(ChunkStatus chunkStatus) {
      return getTargetGenerationStatus(this.level).isAtLeast(chunkStatus) ? this.getFuture(chunkStatus) : UNLOADED_CHUNK_FUTURE;
   }

   public CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> getTickingFuture() {
      return this.tickingFuture;
   }

   public CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> getEntityTickingFuture() {
      return this.entityTickingFuture;
   }

   public CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> method_20725() {
      return this.borderFuture;
   }

   @Nullable
   public WorldChunk getWorldChunk() {
      CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> completableFuture = this.getTickingFuture();
      Either<WorldChunk, ChunkHolder.Unloaded> either = (Either)completableFuture.getNow((Object)null);
      return either == null ? null : (WorldChunk)either.left().orElse((Object)null);
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   public ChunkStatus method_23270() {
      for(int i = CHUNK_STATUSES.size() - 1; i >= 0; --i) {
         ChunkStatus chunkStatus = (ChunkStatus)CHUNK_STATUSES.get(i);
         CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture = this.getFuture(chunkStatus);
         if (((Either)completableFuture.getNow(UNLOADED_CHUNK)).left().isPresent()) {
            return chunkStatus;
         }
      }

      return null;
   }

   @Nullable
   public Chunk getCompletedChunk() {
      for(int i = CHUNK_STATUSES.size() - 1; i >= 0; --i) {
         ChunkStatus chunkStatus = (ChunkStatus)CHUNK_STATUSES.get(i);
         CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture = this.getFuture(chunkStatus);
         if (!completableFuture.isCompletedExceptionally()) {
            Optional<Chunk> optional = ((Either)completableFuture.getNow(UNLOADED_CHUNK)).left();
            if (optional.isPresent()) {
               return (Chunk)optional.get();
            }
         }
      }

      return null;
   }

   public CompletableFuture<Chunk> getFuture() {
      return this.future;
   }

   public void markForBlockUpdate(int x, int y, int z) {
      WorldChunk worldChunk = this.getWorldChunk();
      if (worldChunk != null) {
         this.sectionsNeedingUpdateMask |= 1 << (y >> 4);
         if (this.blockUpdateCount < 64) {
            short s = (short)(x << 12 | z << 8 | y);

            for(int i = 0; i < this.blockUpdateCount; ++i) {
               if (this.blockUpdatePositions[i] == s) {
                  return;
               }
            }

            this.blockUpdatePositions[this.blockUpdateCount++] = s;
         }

      }
   }

   public void markForLightUpdate(LightType type, int y) {
      WorldChunk worldChunk = this.getWorldChunk();
      if (worldChunk != null) {
         worldChunk.setShouldSave(true);
         if (type == LightType.SKY) {
            this.skyLightUpdateBits |= 1 << y - -1;
         } else {
            this.blockLightUpdateBits |= 1 << y - -1;
         }

      }
   }

   public void flushUpdates(WorldChunk worldChunk) {
      if (this.blockUpdateCount != 0 || this.skyLightUpdateBits != 0 || this.blockLightUpdateBits != 0) {
         World world = worldChunk.getWorld();
         if (this.blockUpdateCount == 64) {
            this.lightSentWithBlocksBits = -1;
         }

         int n;
         int o;
         if (this.skyLightUpdateBits != 0 || this.blockLightUpdateBits != 0) {
            this.sendPacketToPlayersWatching(new LightUpdateS2CPacket(worldChunk.getPos(), this.lightingProvider, this.skyLightUpdateBits & ~this.lightSentWithBlocksBits, this.blockLightUpdateBits & ~this.lightSentWithBlocksBits), true);
            n = this.skyLightUpdateBits & this.lightSentWithBlocksBits;
            o = this.blockLightUpdateBits & this.lightSentWithBlocksBits;
            if (n != 0 || o != 0) {
               this.sendPacketToPlayersWatching(new LightUpdateS2CPacket(worldChunk.getPos(), this.lightingProvider, n, o), false);
            }

            this.skyLightUpdateBits = 0;
            this.blockLightUpdateBits = 0;
            this.lightSentWithBlocksBits &= ~(this.skyLightUpdateBits & this.blockLightUpdateBits);
         }

         int p;
         if (this.blockUpdateCount == 1) {
            n = (this.blockUpdatePositions[0] >> 12 & 15) + this.pos.x * 16;
            o = this.blockUpdatePositions[0] & 255;
            p = (this.blockUpdatePositions[0] >> 8 & 15) + this.pos.z * 16;
            BlockPos blockPos = new BlockPos(n, o, p);
            this.sendPacketToPlayersWatching(new BlockUpdateS2CPacket(world, blockPos), false);
            if (world.getBlockState(blockPos).getBlock().hasBlockEntity()) {
               this.sendBlockEntityUpdatePacket(world, blockPos);
            }
         } else if (this.blockUpdateCount == 64) {
            this.sendPacketToPlayersWatching(new ChunkDataS2CPacket(worldChunk, this.sectionsNeedingUpdateMask), false);
         } else if (this.blockUpdateCount != 0) {
            this.sendPacketToPlayersWatching(new ChunkDeltaUpdateS2CPacket(this.blockUpdateCount, this.blockUpdatePositions, worldChunk), false);

            for(n = 0; n < this.blockUpdateCount; ++n) {
               o = (this.blockUpdatePositions[n] >> 12 & 15) + this.pos.x * 16;
               p = this.blockUpdatePositions[n] & 255;
               int q = (this.blockUpdatePositions[n] >> 8 & 15) + this.pos.z * 16;
               BlockPos blockPos2 = new BlockPos(o, p, q);
               if (world.getBlockState(blockPos2).getBlock().hasBlockEntity()) {
                  this.sendBlockEntityUpdatePacket(world, blockPos2);
               }
            }
         }

         this.blockUpdateCount = 0;
         this.sectionsNeedingUpdateMask = 0;
      }
   }

   private void sendBlockEntityUpdatePacket(World world, BlockPos pos) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity != null) {
         BlockEntityUpdateS2CPacket blockEntityUpdateS2CPacket = blockEntity.toUpdatePacket();
         if (blockEntityUpdateS2CPacket != null) {
            this.sendPacketToPlayersWatching(blockEntityUpdateS2CPacket, false);
         }
      }

   }

   private void sendPacketToPlayersWatching(Packet<?> packet, boolean onlyOnWatchDistanceEdge) {
      this.playersWatchingChunkProvider.getPlayersWatchingChunk(this.pos, onlyOnWatchDistanceEdge).forEach((serverPlayerEntity) -> {
         serverPlayerEntity.networkHandler.sendPacket(packet);
      });
   }

   public CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> createFuture(ChunkStatus targetStatus, ThreadedAnvilChunkStorage chunkStorage) {
      int i = targetStatus.getIndex();
      CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture = (CompletableFuture)this.futuresByStatus.get(i);
      if (completableFuture != null) {
         Either<Chunk, ChunkHolder.Unloaded> either = (Either)completableFuture.getNow((Object)null);
         if (either == null || either.left().isPresent()) {
            return completableFuture;
         }
      }

      if (getTargetGenerationStatus(this.level).isAtLeast(targetStatus)) {
         CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture2 = chunkStorage.createChunkFuture(this, targetStatus);
         this.updateFuture(completableFuture2);
         this.futuresByStatus.set(i, completableFuture2);
         return completableFuture2;
      } else {
         return completableFuture == null ? UNLOADED_CHUNK_FUTURE : completableFuture;
      }
   }

   private void updateFuture(CompletableFuture<? extends Either<? extends Chunk, ChunkHolder.Unloaded>> newChunkFuture) {
      this.future = this.future.thenCombine(newChunkFuture, (chunk, either) -> {
         return (Chunk)either.map((chunkx) -> {
            return chunkx;
         }, (unloaded) -> {
            return chunk;
         });
      });
   }

   @Environment(EnvType.CLIENT)
   public ChunkHolder.LevelType method_23271() {
      return getLevelType(this.level);
   }

   public ChunkPos getPos() {
      return this.pos;
   }

   public int getLevel() {
      return this.level;
   }

   public int getCompletedLevel() {
      return this.completedLevel;
   }

   private void setCompletedLevel(int level) {
      this.completedLevel = level;
   }

   public void setLevel(int level) {
      this.level = level;
   }

   protected void tick(ThreadedAnvilChunkStorage chunkStorage) {
      ChunkStatus chunkStatus = getTargetGenerationStatus(this.lastTickLevel);
      ChunkStatus chunkStatus2 = getTargetGenerationStatus(this.level);
      boolean bl = this.lastTickLevel <= ThreadedAnvilChunkStorage.MAX_LEVEL;
      boolean bl2 = this.level <= ThreadedAnvilChunkStorage.MAX_LEVEL;
      ChunkHolder.LevelType levelType = getLevelType(this.lastTickLevel);
      ChunkHolder.LevelType levelType2 = getLevelType(this.level);
      CompletableFuture completableFuture;
      if (bl) {
         Either<Chunk, ChunkHolder.Unloaded> either = Either.right(new ChunkHolder.Unloaded() {
            public String toString() {
               return "Unloaded ticket level " + ChunkHolder.this.pos.toString();
            }
         });

         for(int i = bl2 ? chunkStatus2.getIndex() + 1 : 0; i <= chunkStatus.getIndex(); ++i) {
            completableFuture = (CompletableFuture)this.futuresByStatus.get(i);
            if (completableFuture != null) {
               completableFuture.complete(either);
            } else {
               this.futuresByStatus.set(i, CompletableFuture.completedFuture(either));
            }
         }
      }

      boolean bl3 = levelType.isAfter(ChunkHolder.LevelType.BORDER);
      boolean bl4 = levelType2.isAfter(ChunkHolder.LevelType.BORDER);
      this.field_19238 |= bl4;
      if (!bl3 && bl4) {
         this.borderFuture = chunkStorage.createBorderFuture(this);
         this.updateFuture(this.borderFuture);
      }

      if (bl3 && !bl4) {
         completableFuture = this.borderFuture;
         this.borderFuture = UNLOADED_WORLD_CHUNK_FUTURE;
         this.updateFuture(completableFuture.thenApply((eitherx) -> {
            chunkStorage.getClass();
            return eitherx.ifLeft(chunkStorage::method_20576);
         }));
      }

      boolean bl5 = levelType.isAfter(ChunkHolder.LevelType.TICKING);
      boolean bl6 = levelType2.isAfter(ChunkHolder.LevelType.TICKING);
      if (!bl5 && bl6) {
         this.tickingFuture = chunkStorage.createTickingFuture(this);
         this.updateFuture(this.tickingFuture);
      }

      if (bl5 && !bl6) {
         this.tickingFuture.complete(UNLOADED_WORLD_CHUNK);
         this.tickingFuture = UNLOADED_WORLD_CHUNK_FUTURE;
      }

      boolean bl7 = levelType.isAfter(ChunkHolder.LevelType.ENTITY_TICKING);
      boolean bl8 = levelType2.isAfter(ChunkHolder.LevelType.ENTITY_TICKING);
      if (!bl7 && bl8) {
         if (this.entityTickingFuture != UNLOADED_WORLD_CHUNK_FUTURE) {
            throw (IllegalStateException)Util.throwOrPause(new IllegalStateException());
         }

         this.entityTickingFuture = chunkStorage.createEntityTickingChunkFuture(this.pos);
         this.updateFuture(this.entityTickingFuture);
      }

      if (bl7 && !bl8) {
         this.entityTickingFuture.complete(UNLOADED_WORLD_CHUNK);
         this.entityTickingFuture = UNLOADED_WORLD_CHUNK_FUTURE;
      }

      this.levelUpdateListener.updateLevel(this.pos, this::getCompletedLevel, this.level, this::setCompletedLevel);
      this.lastTickLevel = this.level;
   }

   public static ChunkStatus getTargetGenerationStatus(int level) {
      return level < 33 ? ChunkStatus.FULL : ChunkStatus.getTargetGenerationStatus(level - 33);
   }

   public static ChunkHolder.LevelType getLevelType(int distance) {
      return LEVEL_TYPES[MathHelper.clamp(33 - distance + 1, 0, LEVEL_TYPES.length - 1)];
   }

   public boolean method_20384() {
      return this.field_19238;
   }

   public void method_20385() {
      this.field_19238 = getLevelType(this.level).isAfter(ChunkHolder.LevelType.BORDER);
   }

   public void method_20456(ReadOnlyChunk readOnlyChunk) {
      for(int i = 0; i < this.futuresByStatus.length(); ++i) {
         CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture = (CompletableFuture)this.futuresByStatus.get(i);
         if (completableFuture != null) {
            Optional<Chunk> optional = ((Either)completableFuture.getNow(UNLOADED_CHUNK)).left();
            if (optional.isPresent() && optional.get() instanceof ProtoChunk) {
               this.futuresByStatus.set(i, CompletableFuture.completedFuture(Either.left(readOnlyChunk)));
            }
         }
      }

      this.updateFuture(CompletableFuture.completedFuture(Either.left(readOnlyChunk.getWrappedChunk())));
   }

   static {
      UNLOADED_CHUNK = Either.right(ChunkHolder.Unloaded.INSTANCE);
      UNLOADED_CHUNK_FUTURE = CompletableFuture.completedFuture(UNLOADED_CHUNK);
      UNLOADED_WORLD_CHUNK = Either.right(ChunkHolder.Unloaded.INSTANCE);
      UNLOADED_WORLD_CHUNK_FUTURE = CompletableFuture.completedFuture(UNLOADED_WORLD_CHUNK);
      CHUNK_STATUSES = ChunkStatus.createOrderedList();
      LEVEL_TYPES = ChunkHolder.LevelType.values();
   }

   public interface PlayersWatchingChunkProvider {
      Stream<ServerPlayerEntity> getPlayersWatchingChunk(ChunkPos chunkPos, boolean onlyOnWatchDistanceEdge);
   }

   public interface LevelUpdateListener {
      void updateLevel(ChunkPos pos, IntSupplier levelGetter, int targetLevel, IntConsumer levelSetter);
   }

   public interface Unloaded {
      ChunkHolder.Unloaded INSTANCE = new ChunkHolder.Unloaded() {
         public String toString() {
            return "UNLOADED";
         }
      };
   }

   public static enum LevelType {
      INACCESSIBLE,
      BORDER,
      TICKING,
      ENTITY_TICKING;

      public boolean isAfter(ChunkHolder.LevelType levelType) {
         return this.ordinal() >= levelType.ordinal();
      }
   }
}
