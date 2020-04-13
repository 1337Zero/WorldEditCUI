package net.minecraft.server.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.DebugRendererInfoManager;
import net.minecraft.client.network.packet.ChunkDataS2CPacket;
import net.minecraft.client.network.packet.ChunkRenderDistanceCenterS2CPacket;
import net.minecraft.client.network.packet.EntityAttachS2CPacket;
import net.minecraft.client.network.packet.EntityPassengersSetS2CPacket;
import net.minecraft.client.network.packet.LightUpdateS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.CsvWriter;
import net.minecraft.util.TypeFilterableList;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.thread.MessageListener;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.GameRules;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.SessionLockException;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThreadedAnvilChunkStorage extends VersionedChunkStorage implements ChunkHolder.PlayersWatchingChunkProvider {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final int MAX_LEVEL = 33 + ChunkStatus.getMaxTargetGenerationRadius();
   private final Long2ObjectLinkedOpenHashMap<ChunkHolder> currentChunkHolders = new Long2ObjectLinkedOpenHashMap();
   private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkHolders;
   private final Long2ObjectLinkedOpenHashMap<ChunkHolder> field_18807;
   private final LongSet loadedChunks;
   private final ServerWorld world;
   private final ServerLightingProvider serverLightingProvider;
   private final ThreadExecutor<Runnable> mainThreadExecutor;
   private final ChunkGenerator<?> chunkGenerator;
   private final Supplier<PersistentStateManager> persistentStateManagerFactory;
   private final PointOfInterestStorage pointOfInterestStorage;
   private final LongSet unloadedChunks;
   private boolean chunkHolderListDirty;
   private final ChunkTaskPrioritySystem chunkTaskPrioritySystem;
   private final MessageListener<ChunkTaskPrioritySystem.Task<Runnable>> worldgenExecutor;
   private final MessageListener<ChunkTaskPrioritySystem.Task<Runnable>> mainExecutor;
   private final WorldGenerationProgressListener worldGenerationProgressListener;
   private final ThreadedAnvilChunkStorage.TicketManager ticketManager;
   private final AtomicInteger totalChunksLoadedCount;
   private final StructureManager structureManager;
   private final File saveDir;
   private final PlayerChunkWatchingManager playerChunkWatchingManager;
   private final Int2ObjectMap<ThreadedAnvilChunkStorage.EntityTracker> entityTrackers;
   private final Queue<Runnable> field_19343;
   private int watchDistance;

   public ThreadedAnvilChunkStorage(ServerWorld world, File file, DataFixer dataFixer, StructureManager structureManager, Executor workerExecutor, ThreadExecutor<Runnable> mainThreadExecutor, ChunkProvider chunkProvider, ChunkGenerator<?> chunkGenerator, WorldGenerationProgressListener worldGenerationProgressListener, Supplier<PersistentStateManager> supplier, int i) {
      super(new File(world.getDimension().getType().getSaveDirectory(file), "region"), dataFixer);
      this.chunkHolders = this.currentChunkHolders.clone();
      this.field_18807 = new Long2ObjectLinkedOpenHashMap();
      this.loadedChunks = new LongOpenHashSet();
      this.unloadedChunks = new LongOpenHashSet();
      this.totalChunksLoadedCount = new AtomicInteger();
      this.playerChunkWatchingManager = new PlayerChunkWatchingManager();
      this.entityTrackers = new Int2ObjectOpenHashMap();
      this.field_19343 = Queues.newConcurrentLinkedQueue();
      this.structureManager = structureManager;
      this.saveDir = world.getDimension().getType().getSaveDirectory(file);
      this.world = world;
      this.chunkGenerator = chunkGenerator;
      this.mainThreadExecutor = mainThreadExecutor;
      TaskExecutor<Runnable> taskExecutor = TaskExecutor.create(workerExecutor, "worldgen");
      mainThreadExecutor.getClass();
      MessageListener<Runnable> messageListener = MessageListener.create("main", mainThreadExecutor::send);
      this.worldGenerationProgressListener = worldGenerationProgressListener;
      TaskExecutor<Runnable> taskExecutor2 = TaskExecutor.create(workerExecutor, "light");
      this.chunkTaskPrioritySystem = new ChunkTaskPrioritySystem(ImmutableList.of(taskExecutor, messageListener, taskExecutor2), workerExecutor, Integer.MAX_VALUE);
      this.worldgenExecutor = this.chunkTaskPrioritySystem.createExecutor(taskExecutor, false);
      this.mainExecutor = this.chunkTaskPrioritySystem.createExecutor(messageListener, false);
      this.serverLightingProvider = new ServerLightingProvider(chunkProvider, this, this.world.getDimension().hasSkyLight(), taskExecutor2, this.chunkTaskPrioritySystem.createExecutor(taskExecutor2, false));
      this.ticketManager = new ThreadedAnvilChunkStorage.TicketManager(workerExecutor, mainThreadExecutor);
      this.persistentStateManagerFactory = supplier;
      this.pointOfInterestStorage = new PointOfInterestStorage(new File(this.saveDir, "poi"), dataFixer);
      this.setViewDistance(i);
   }

   private static double getSquaredDistance(ChunkPos pos, Entity entity) {
      double d = (double)(pos.x * 16 + 8);
      double e = (double)(pos.z * 16 + 8);
      double f = d - entity.getX();
      double g = e - entity.getZ();
      return f * f + g * g;
   }

   private static int getChebyshevDistance(ChunkPos pos, ServerPlayerEntity player, boolean useCameraPosition) {
      int k;
      int l;
      if (useCameraPosition) {
         ChunkSectionPos chunkSectionPos = player.getCameraPosition();
         k = chunkSectionPos.getSectionX();
         l = chunkSectionPos.getSectionZ();
      } else {
         k = MathHelper.floor(player.getX() / 16.0D);
         l = MathHelper.floor(player.getZ() / 16.0D);
      }

      return getChebyshevDistance(pos, k, l);
   }

   private static int getChebyshevDistance(ChunkPos pos, int x, int z) {
      int i = pos.x - x;
      int j = pos.z - z;
      return Math.max(Math.abs(i), Math.abs(j));
   }

   protected ServerLightingProvider getLightProvider() {
      return this.serverLightingProvider;
   }

   @Nullable
   protected ChunkHolder getCurrentChunkHolder(long pos) {
      return (ChunkHolder)this.currentChunkHolders.get(pos);
   }

   @Nullable
   protected ChunkHolder getChunkHolder(long pos) {
      return (ChunkHolder)this.chunkHolders.get(pos);
   }

   protected IntSupplier getCompletedLevelSupplier(long pos) {
      return () -> {
         ChunkHolder chunkHolder = this.getChunkHolder(pos);
         return chunkHolder == null ? LevelPrioritizedQueue.LEVEL_COUNT - 1 : Math.min(chunkHolder.getCompletedLevel(), LevelPrioritizedQueue.LEVEL_COUNT - 1);
      };
   }

   @Environment(EnvType.CLIENT)
   public String method_23272(ChunkPos chunkPos) {
      ChunkHolder chunkHolder = this.getChunkHolder(chunkPos.toLong());
      if (chunkHolder == null) {
         return "null";
      } else {
         String string = chunkHolder.getLevel() + "\n";
         ChunkStatus chunkStatus = chunkHolder.method_23270();
         Chunk chunk = chunkHolder.getCompletedChunk();
         if (chunkStatus != null) {
            string = string + "St: §" + chunkStatus.getIndex() + chunkStatus + '§' + "r\n";
         }

         if (chunk != null) {
            string = string + "Ch: §" + chunk.getStatus().getIndex() + chunk.getStatus() + '§' + "r\n";
         }

         ChunkHolder.LevelType levelType = chunkHolder.method_23271();
         string = string + "§" + levelType.ordinal() + levelType;
         return string + '§' + "r";
      }
   }

   private CompletableFuture<Either<List<Chunk>, ChunkHolder.Unloaded>> createChunkRegionFuture(ChunkPos centerChunk, int margin, IntFunction<ChunkStatus> distanceToStatus) {
      List<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> list = Lists.newArrayList();
      int i = centerChunk.x;
      int j = centerChunk.z;

      for(int k = -margin; k <= margin; ++k) {
         for(int l = -margin; l <= margin; ++l) {
            int m = Math.max(Math.abs(l), Math.abs(k));
            final ChunkPos chunkPos = new ChunkPos(i + l, j + k);
            long n = chunkPos.toLong();
            ChunkHolder chunkHolder = this.getCurrentChunkHolder(n);
            if (chunkHolder == null) {
               return CompletableFuture.completedFuture(Either.right(new ChunkHolder.Unloaded() {
                  public String toString() {
                     return "Unloaded " + chunkPos.toString();
                  }
               }));
            }

            ChunkStatus chunkStatus = (ChunkStatus)distanceToStatus.apply(m);
            CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture = chunkHolder.createFuture(chunkStatus, this);
            list.add(completableFuture);
         }
      }

      CompletableFuture<List<Either<Chunk, ChunkHolder.Unloaded>>> completableFuture2 = Util.combine(list);
      return completableFuture2.thenApply((listx) -> {
         List<Chunk> list2 = Lists.newArrayList();
         final int l = 0;

         for(Iterator var7 = listx.iterator(); var7.hasNext(); ++l) {
            final Either<Chunk, ChunkHolder.Unloaded> either = (Either)var7.next();
            Optional<Chunk> optional = either.left();
            if (!optional.isPresent()) {
               return Either.right(new ChunkHolder.Unloaded() {
                  public String toString() {
                     return "Unloaded " + new ChunkPos(i + l % (j * 2 + 1), k + l / (j * 2 + 1)) + " " + ((ChunkHolder.Unloaded)either.right().get()).toString();
                  }
               });
            }

            list2.add(optional.get());
         }

         return Either.left(list2);
      });
   }

   public CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> createEntityTickingChunkFuture(ChunkPos pos) {
      return this.createChunkRegionFuture(pos, 2, (i) -> {
         return ChunkStatus.FULL;
      }).thenApplyAsync((either) -> {
         return either.mapLeft((list) -> {
            return (WorldChunk)list.get(list.size() / 2);
         });
      }, this.mainThreadExecutor);
   }

   @Nullable
   private ChunkHolder setLevel(long pos, int level, @Nullable ChunkHolder holder, int i) {
      if (i > MAX_LEVEL && level > MAX_LEVEL) {
         return holder;
      } else {
         if (holder != null) {
            holder.setLevel(level);
         }

         if (holder != null) {
            if (level > MAX_LEVEL) {
               this.unloadedChunks.add(pos);
            } else {
               this.unloadedChunks.remove(pos);
            }
         }

         if (level <= MAX_LEVEL && holder == null) {
            holder = (ChunkHolder)this.field_18807.remove(pos);
            if (holder != null) {
               holder.setLevel(level);
            } else {
               holder = new ChunkHolder(new ChunkPos(pos), level, this.serverLightingProvider, this.chunkTaskPrioritySystem, this);
            }

            this.currentChunkHolders.put(pos, holder);
            this.chunkHolderListDirty = true;
         }

         return holder;
      }
   }

   public void close() throws IOException {
      try {
         this.chunkTaskPrioritySystem.close();
         this.pointOfInterestStorage.close();
      } finally {
         super.close();
      }

   }

   protected void save(boolean flush) {
      if (flush) {
         List<ChunkHolder> list = (List)this.chunkHolders.values().stream().filter(ChunkHolder::method_20384).peek(ChunkHolder::method_20385).collect(Collectors.toList());
         MutableBoolean mutableBoolean = new MutableBoolean();

         do {
            mutableBoolean.setFalse();
            list.stream().map((chunkHolder) -> {
               CompletableFuture completableFuture;
               do {
                  completableFuture = chunkHolder.getFuture();
                  this.mainThreadExecutor.runTasks(completableFuture::isDone);
               } while(completableFuture != chunkHolder.getFuture());

               return (Chunk)completableFuture.join();
            }).filter((chunk) -> {
               return chunk instanceof ReadOnlyChunk || chunk instanceof WorldChunk;
            }).filter(this::save).forEach((chunk) -> {
               mutableBoolean.setTrue();
            });
         } while(mutableBoolean.isTrue());

         this.method_20605(() -> {
            return true;
         });
         this.completeAll();
         LOGGER.info("ThreadedAnvilChunkStorage ({}): All chunks are saved", this.saveDir.getName());
      } else {
         this.chunkHolders.values().stream().filter(ChunkHolder::method_20384).forEach((chunkHolder) -> {
            Chunk chunk = (Chunk)chunkHolder.getFuture().getNow((Object)null);
            if (chunk instanceof ReadOnlyChunk || chunk instanceof WorldChunk) {
               this.save(chunk);
               chunkHolder.method_20385();
            }

         });
      }

   }

   protected void tick(BooleanSupplier shouldKeepTicking) {
      Profiler profiler = this.world.getProfiler();
      profiler.push("poi");
      this.pointOfInterestStorage.tick(shouldKeepTicking);
      profiler.swap("chunk_unload");
      if (!this.world.isSavingDisabled()) {
         this.method_20605(shouldKeepTicking);
      }

      profiler.pop();
   }

   private void method_20605(BooleanSupplier booleanSupplier) {
      LongIterator longIterator = this.unloadedChunks.iterator();

      for(int i = 0; longIterator.hasNext() && (booleanSupplier.getAsBoolean() || i < 200 || this.unloadedChunks.size() > 2000); longIterator.remove()) {
         long l = longIterator.nextLong();
         ChunkHolder chunkHolder = (ChunkHolder)this.currentChunkHolders.remove(l);
         if (chunkHolder != null) {
            this.field_18807.put(l, chunkHolder);
            this.chunkHolderListDirty = true;
            ++i;
            this.method_20458(l, chunkHolder);
         }
      }

      Runnable runnable;
      while((booleanSupplier.getAsBoolean() || this.field_19343.size() > 2000) && (runnable = (Runnable)this.field_19343.poll()) != null) {
         runnable.run();
      }

   }

   private void method_20458(long l, ChunkHolder chunkHolder) {
      CompletableFuture<Chunk> completableFuture = chunkHolder.getFuture();
      Consumer var10001 = (chunk) -> {
         CompletableFuture<Chunk> completableFuture2 = chunkHolder.getFuture();
         if (completableFuture2 != completableFuture) {
            this.method_20458(l, chunkHolder);
         } else {
            if (this.field_18807.remove(l, chunkHolder) && chunk != null) {
               if (chunk instanceof WorldChunk) {
                  ((WorldChunk)chunk).setLoadedToWorld(false);
               }

               this.save(chunk);
               if (this.loadedChunks.remove(l) && chunk instanceof WorldChunk) {
                  WorldChunk worldChunk = (WorldChunk)chunk;
                  this.world.unloadEntities(worldChunk);
               }

               this.serverLightingProvider.updateChunkStatus(chunk.getPos());
               this.serverLightingProvider.tick();
               this.worldGenerationProgressListener.setChunkStatus(chunk.getPos(), (ChunkStatus)null);
            }

         }
      };
      Queue var10002 = this.field_19343;
      var10002.getClass();
      completableFuture.thenAcceptAsync(var10001, var10002::add).whenComplete((var1, throwable) -> {
         if (throwable != null) {
            LOGGER.error("Failed to save chunk " + chunkHolder.getPos(), throwable);
         }

      });
   }

   protected boolean updateHolderMap() {
      if (!this.chunkHolderListDirty) {
         return false;
      } else {
         this.chunkHolders = this.currentChunkHolders.clone();
         this.chunkHolderListDirty = false;
         return true;
      }
   }

   public CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> createChunkFuture(ChunkHolder chunkHolder, ChunkStatus chunkStatus) {
      ChunkPos chunkPos = chunkHolder.getPos();
      if (chunkStatus == ChunkStatus.EMPTY) {
         return this.method_20619(chunkPos);
      } else {
         CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture = chunkHolder.createFuture(chunkStatus.getPrevious(), this);
         return completableFuture.thenComposeAsync((either) -> {
            Optional<Chunk> optional = either.left();
            if (!optional.isPresent()) {
               return CompletableFuture.completedFuture(either);
            } else {
               if (chunkStatus == ChunkStatus.LIGHT) {
                  this.ticketManager.addTicketWithLevel(ChunkTicketType.LIGHT, chunkPos, 33 + ChunkStatus.getTargetGenerationRadius(ChunkStatus.FEATURES), chunkPos);
               }

               Chunk chunk = (Chunk)optional.get();
               if (chunk.getStatus().isAtLeast(chunkStatus)) {
                  CompletableFuture completableFuture2;
                  if (chunkStatus == ChunkStatus.LIGHT) {
                     completableFuture2 = this.method_20617(chunkHolder, chunkStatus);
                  } else {
                     completableFuture2 = chunkStatus.runNoGenTask(this.world, this.structureManager, this.serverLightingProvider, (chunkx) -> {
                        return this.convertToFullChunk(chunkHolder);
                     }, chunk);
                  }

                  this.worldGenerationProgressListener.setChunkStatus(chunkPos, chunkStatus);
                  return completableFuture2;
               } else {
                  return this.method_20617(chunkHolder, chunkStatus);
               }
            }
         }, this.mainThreadExecutor);
      }
   }

   private CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> method_20619(ChunkPos chunkPos) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            this.world.getProfiler().method_24270("chunkLoad");
            CompoundTag compoundTag = this.getUpdatedChunkTag(chunkPos);
            if (compoundTag != null) {
               boolean bl = compoundTag.contains("Level", 10) && compoundTag.getCompound("Level").contains("Status", 8);
               if (bl) {
                  Chunk chunk = ChunkSerializer.deserialize(this.world, this.structureManager, this.pointOfInterestStorage, chunkPos, compoundTag);
                  chunk.setLastSaveTime(this.world.getTime());
                  return Either.left(chunk);
               }

               LOGGER.error("Chunk file at {} is missing level data, skipping", chunkPos);
            }
         } catch (CrashException var5) {
            Throwable throwable = var5.getCause();
            if (!(throwable instanceof IOException)) {
               throw var5;
            }

            LOGGER.error("Couldn't load chunk {}", chunkPos, throwable);
         } catch (Exception var6) {
            LOGGER.error("Couldn't load chunk {}", chunkPos, var6);
         }

         return Either.left(new ProtoChunk(chunkPos, UpgradeData.NO_UPGRADE_DATA));
      }, this.mainThreadExecutor);
   }

   private CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> method_20617(ChunkHolder chunkHolder, ChunkStatus chunkStatus) {
      ChunkPos chunkPos = chunkHolder.getPos();
      CompletableFuture<Either<List<Chunk>, ChunkHolder.Unloaded>> completableFuture = this.createChunkRegionFuture(chunkPos, chunkStatus.getTaskMargin(), (i) -> {
         return this.getRequiredStatusForGeneration(chunkStatus, i);
      });
      this.world.getProfiler().method_24271(() -> {
         return "chunkGenerate " + chunkStatus.getId();
      });
      return completableFuture.thenComposeAsync((either) -> {
         return (CompletableFuture)either.map((list) -> {
            try {
               CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture = chunkStatus.runTask(this.world, this.chunkGenerator, this.structureManager, this.serverLightingProvider, (chunk) -> {
                  return this.convertToFullChunk(chunkHolder);
               }, list);
               this.worldGenerationProgressListener.setChunkStatus(chunkPos, chunkStatus);
               return completableFuture;
            } catch (Exception var8) {
               CrashReport crashReport = CrashReport.create(var8, "Exception generating new chunk");
               CrashReportSection crashReportSection = crashReport.addElement("Chunk to be generated");
               crashReportSection.add("Location", (Object)String.format("%d,%d", chunkPos.x, chunkPos.z));
               crashReportSection.add("Position hash", (Object)ChunkPos.toLong(chunkPos.x, chunkPos.z));
               crashReportSection.add("Generator", (Object)this.chunkGenerator);
               throw new CrashException(crashReport);
            }
         }, (unloaded) -> {
            this.releaseLightTicket(chunkPos);
            return CompletableFuture.completedFuture(Either.right(unloaded));
         });
      }, (runnable) -> {
         this.worldgenExecutor.send(ChunkTaskPrioritySystem.createMessage(chunkHolder, runnable));
      });
   }

   protected void releaseLightTicket(ChunkPos pos) {
      this.mainThreadExecutor.send(Util.debugRunnable(() -> {
         this.ticketManager.removeTicketWithLevel(ChunkTicketType.LIGHT, pos, 33 + ChunkStatus.getTargetGenerationRadius(ChunkStatus.FEATURES), pos);
      }, () -> {
         return "release light ticket " + pos;
      }));
   }

   private ChunkStatus getRequiredStatusForGeneration(ChunkStatus centerChunkTargetStatus, int distance) {
      ChunkStatus chunkStatus2;
      if (distance == 0) {
         chunkStatus2 = centerChunkTargetStatus.getPrevious();
      } else {
         chunkStatus2 = ChunkStatus.getTargetGenerationStatus(ChunkStatus.getTargetGenerationRadius(centerChunkTargetStatus) + distance);
      }

      return chunkStatus2;
   }

   private CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> convertToFullChunk(ChunkHolder chunkHolder) {
      CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture = chunkHolder.getFuture(ChunkStatus.FULL.getPrevious());
      return completableFuture.thenApplyAsync((either) -> {
         ChunkStatus chunkStatus = ChunkHolder.getTargetGenerationStatus(chunkHolder.getLevel());
         return !chunkStatus.isAtLeast(ChunkStatus.FULL) ? ChunkHolder.UNLOADED_CHUNK : either.mapLeft((chunk) -> {
            ChunkPos chunkPos = chunkHolder.getPos();
            WorldChunk worldChunk2;
            if (chunk instanceof ReadOnlyChunk) {
               worldChunk2 = ((ReadOnlyChunk)chunk).getWrappedChunk();
            } else {
               worldChunk2 = new WorldChunk(this.world, (ProtoChunk)chunk);
               chunkHolder.method_20456(new ReadOnlyChunk(worldChunk2));
            }

            worldChunk2.setLevelTypeProvider(() -> {
               return ChunkHolder.getLevelType(chunkHolder.getLevel());
            });
            worldChunk2.loadToWorld();
            if (this.loadedChunks.add(chunkPos.toLong())) {
               worldChunk2.setLoadedToWorld(true);
               this.world.addBlockEntities(worldChunk2.getBlockEntities().values());
               List<Entity> list = null;
               TypeFilterableList[] var6 = worldChunk2.getEntitySectionArray();
               int var7 = var6.length;

               for(int var8 = 0; var8 < var7; ++var8) {
                  TypeFilterableList<Entity> typeFilterableList = var6[var8];
                  Iterator var10 = typeFilterableList.iterator();

                  while(var10.hasNext()) {
                     Entity entity = (Entity)var10.next();
                     if (!(entity instanceof PlayerEntity) && !this.world.loadEntity(entity)) {
                        if (list == null) {
                           list = Lists.newArrayList(new Entity[]{entity});
                        } else {
                           list.add(entity);
                        }
                     }
                  }
               }

               if (list != null) {
                  list.forEach(worldChunk2::remove);
               }
            }

            return worldChunk2;
         });
      }, (runnable) -> {
         MessageListener var10000 = this.mainExecutor;
         long var10002 = chunkHolder.getPos().toLong();
         chunkHolder.getClass();
         var10000.send(ChunkTaskPrioritySystem.createMessage(runnable, var10002, chunkHolder::getLevel));
      });
   }

   public CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> createTickingFuture(ChunkHolder chunkHolder) {
      ChunkPos chunkPos = chunkHolder.getPos();
      CompletableFuture<Either<List<Chunk>, ChunkHolder.Unloaded>> completableFuture = this.createChunkRegionFuture(chunkPos, 1, (i) -> {
         return ChunkStatus.FULL;
      });
      CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> completableFuture2 = completableFuture.thenApplyAsync((either) -> {
         return either.flatMap((list) -> {
            WorldChunk worldChunk = (WorldChunk)list.get(list.size() / 2);
            worldChunk.runPostProcessing();
            return Either.left(worldChunk);
         });
      }, (runnable) -> {
         this.mainExecutor.send(ChunkTaskPrioritySystem.createMessage(chunkHolder, runnable));
      });
      completableFuture2.thenAcceptAsync((either) -> {
         either.mapLeft((worldChunk) -> {
            this.totalChunksLoadedCount.getAndIncrement();
            Packet<?>[] packets = new Packet[2];
            this.getPlayersWatchingChunk(chunkPos, false).forEach((serverPlayerEntity) -> {
               this.sendChunkDataPackets(serverPlayerEntity, packets, worldChunk);
            });
            return Either.left(worldChunk);
         });
      }, (runnable) -> {
         this.mainExecutor.send(ChunkTaskPrioritySystem.createMessage(chunkHolder, runnable));
      });
      return completableFuture2;
   }

   public CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> createBorderFuture(ChunkHolder chunkHolder) {
      return chunkHolder.createFuture(ChunkStatus.FULL, this).thenApplyAsync((either) -> {
         return either.mapLeft((chunk) -> {
            WorldChunk worldChunk = (WorldChunk)chunk;
            worldChunk.disableTickSchedulers();
            return worldChunk;
         });
      }, (runnable) -> {
         this.mainExecutor.send(ChunkTaskPrioritySystem.createMessage(chunkHolder, runnable));
      });
   }

   public int getTotalChunksLoadedCount() {
      return this.totalChunksLoadedCount.get();
   }

   private boolean save(Chunk chunk) {
      this.pointOfInterestStorage.method_20436(chunk.getPos());
      if (!chunk.needsSaving()) {
         return false;
      } else {
         try {
            this.world.checkSessionLock();
         } catch (SessionLockException var6) {
            LOGGER.error("Couldn't save chunk; already in use by another instance of Minecraft?", var6);
            return false;
         }

         chunk.setLastSaveTime(this.world.getTime());
         chunk.setShouldSave(false);
         ChunkPos chunkPos = chunk.getPos();

         try {
            ChunkStatus chunkStatus = chunk.getStatus();
            CompoundTag compoundTag;
            if (chunkStatus.getChunkType() != ChunkStatus.ChunkType.LEVELCHUNK) {
               compoundTag = this.getUpdatedChunkTag(chunkPos);
               if (compoundTag != null && ChunkSerializer.getChunkType(compoundTag) == ChunkStatus.ChunkType.LEVELCHUNK) {
                  return false;
               }

               if (chunkStatus == ChunkStatus.EMPTY && chunk.getStructureStarts().values().stream().noneMatch(StructureStart::hasChildren)) {
                  return false;
               }
            }

            this.world.getProfiler().method_24270("chunkSave");
            compoundTag = ChunkSerializer.serialize(this.world, chunk);
            this.setTagAt(chunkPos, compoundTag);
            return true;
         } catch (Exception var5) {
            LOGGER.error("Failed to save chunk {},{}", chunkPos.x, chunkPos.z, var5);
            return false;
         }
      }
   }

   protected void setViewDistance(int watchDistance) {
      int i = MathHelper.clamp(watchDistance + 1, 3, 33);
      if (i != this.watchDistance) {
         int j = this.watchDistance;
         this.watchDistance = i;
         this.ticketManager.setWatchDistance(this.watchDistance);
         ObjectIterator var4 = this.currentChunkHolders.values().iterator();

         while(var4.hasNext()) {
            ChunkHolder chunkHolder = (ChunkHolder)var4.next();
            ChunkPos chunkPos = chunkHolder.getPos();
            Packet<?>[] packets = new Packet[2];
            this.getPlayersWatchingChunk(chunkPos, false).forEach((serverPlayerEntity) -> {
               int jx = getChebyshevDistance(chunkPos, serverPlayerEntity, true);
               boolean bl = jx <= j;
               boolean bl2 = jx <= this.watchDistance;
               this.sendWatchPackets(serverPlayerEntity, chunkPos, packets, bl, bl2);
            });
         }
      }

   }

   protected void sendWatchPackets(ServerPlayerEntity player, ChunkPos pos, Packet<?>[] packets, boolean withinMaxWatchDistance, boolean withinViewDistance) {
      if (player.world == this.world) {
         if (withinViewDistance && !withinMaxWatchDistance) {
            ChunkHolder chunkHolder = this.getChunkHolder(pos.toLong());
            if (chunkHolder != null) {
               WorldChunk worldChunk = chunkHolder.getWorldChunk();
               if (worldChunk != null) {
                  this.sendChunkDataPackets(player, packets, worldChunk);
               }

               DebugRendererInfoManager.method_19775(this.world, pos);
            }
         }

         if (!withinViewDistance && withinMaxWatchDistance) {
            player.sendUnloadChunkPacket(pos);
         }

      }
   }

   public int getLoadedChunkCount() {
      return this.chunkHolders.size();
   }

   protected ThreadedAnvilChunkStorage.TicketManager getTicketManager() {
      return this.ticketManager;
   }

   protected Iterable<ChunkHolder> entryIterator() {
      return Iterables.unmodifiableIterable(this.chunkHolders.values());
   }

   void exportChunks(Writer writer) throws IOException {
      CsvWriter csvWriter = CsvWriter.makeHeader().addColumn("x").addColumn("z").addColumn("level").addColumn("in_memory").addColumn("status").addColumn("full_status").addColumn("accessible_ready").addColumn("ticking_ready").addColumn("entity_ticking_ready").addColumn("ticket").addColumn("spawning").addColumn("entity_count").addColumn("block_entity_count").startBody(writer);
      ObjectBidirectionalIterator var3 = this.chunkHolders.long2ObjectEntrySet().iterator();

      while(var3.hasNext()) {
         Entry<ChunkHolder> entry = (Entry)var3.next();
         ChunkPos chunkPos = new ChunkPos(entry.getLongKey());
         ChunkHolder chunkHolder = (ChunkHolder)entry.getValue();
         Optional<Chunk> optional = Optional.ofNullable(chunkHolder.getCompletedChunk());
         Optional<WorldChunk> optional2 = optional.flatMap((chunk) -> {
            return chunk instanceof WorldChunk ? Optional.of((WorldChunk)chunk) : Optional.empty();
         });
         csvWriter.printRow(chunkPos.x, chunkPos.z, chunkHolder.getLevel(), optional.isPresent(), optional.map(Chunk::getStatus).orElse((Object)null), optional2.map(WorldChunk::getLevelType).orElse((Object)null), method_21676(chunkHolder.method_20725()), method_21676(chunkHolder.getTickingFuture()), method_21676(chunkHolder.getEntityTickingFuture()), this.ticketManager.method_21623(entry.getLongKey()), !this.isTooFarFromPlayersToSpawnMobs(chunkPos), optional2.map((worldChunk) -> {
            return Stream.of(worldChunk.getEntitySectionArray()).mapToInt(TypeFilterableList::size).sum();
         }).orElse(0), optional2.map((worldChunk) -> {
            return worldChunk.getBlockEntities().size();
         }).orElse(0));
      }

   }

   private static String method_21676(CompletableFuture<Either<WorldChunk, ChunkHolder.Unloaded>> completableFuture) {
      try {
         Either<WorldChunk, ChunkHolder.Unloaded> either = (Either)completableFuture.getNow((Object)null);
         return either != null ? (String)either.map((worldChunk) -> {
            return "done";
         }, (unloaded) -> {
            return "unloaded";
         }) : "not completed";
      } catch (CompletionException var2) {
         return "failed " + var2.getCause().getMessage();
      } catch (CancellationException var3) {
         return "cancelled";
      }
   }

   @Nullable
   private CompoundTag getUpdatedChunkTag(ChunkPos pos) throws IOException {
      CompoundTag compoundTag = this.getNbt(pos);
      return compoundTag == null ? null : this.updateChunkTag(this.world.getDimension().getType(), this.persistentStateManagerFactory, compoundTag);
   }

   boolean isTooFarFromPlayersToSpawnMobs(ChunkPos chunkPos) {
      long l = chunkPos.toLong();
      return !this.ticketManager.method_20800(l) ? true : this.playerChunkWatchingManager.getPlayersWatchingChunk(l).noneMatch((serverPlayerEntity) -> {
         return !serverPlayerEntity.isSpectator() && getSquaredDistance(chunkPos, serverPlayerEntity) < 16384.0D;
      });
   }

   private boolean doesNotGenerateChunks(ServerPlayerEntity player) {
      return player.isSpectator() && !this.world.getGameRules().getBoolean(GameRules.SPECTATORS_GENERATE_CHUNKS);
   }

   void handlePlayerAddedOrRemoved(ServerPlayerEntity player, boolean added) {
      boolean bl = this.doesNotGenerateChunks(player);
      boolean bl2 = this.playerChunkWatchingManager.method_21715(player);
      int i = MathHelper.floor(player.getX()) >> 4;
      int j = MathHelper.floor(player.getZ()) >> 4;
      if (added) {
         this.playerChunkWatchingManager.add(ChunkPos.toLong(i, j), player, bl);
         this.method_20726(player);
         if (!bl) {
            this.ticketManager.handleChunkEnter(ChunkSectionPos.from((Entity)player), player);
         }
      } else {
         ChunkSectionPos chunkSectionPos = player.getCameraPosition();
         this.playerChunkWatchingManager.remove(chunkSectionPos.toChunkPos().toLong(), player);
         if (!bl2) {
            this.ticketManager.handleChunkLeave(chunkSectionPos, player);
         }
      }

      for(int k = i - this.watchDistance; k <= i + this.watchDistance; ++k) {
         for(int l = j - this.watchDistance; l <= j + this.watchDistance; ++l) {
            ChunkPos chunkPos = new ChunkPos(k, l);
            this.sendWatchPackets(player, chunkPos, new Packet[2], !added, added);
         }
      }

   }

   private ChunkSectionPos method_20726(ServerPlayerEntity serverPlayerEntity) {
      ChunkSectionPos chunkSectionPos = ChunkSectionPos.from((Entity)serverPlayerEntity);
      serverPlayerEntity.setCameraPosition(chunkSectionPos);
      serverPlayerEntity.networkHandler.sendPacket(new ChunkRenderDistanceCenterS2CPacket(chunkSectionPos.getSectionX(), chunkSectionPos.getSectionZ()));
      return chunkSectionPos;
   }

   public void updateCameraPosition(ServerPlayerEntity player) {
      ObjectIterator var2 = this.entityTrackers.values().iterator();

      while(var2.hasNext()) {
         ThreadedAnvilChunkStorage.EntityTracker entityTracker = (ThreadedAnvilChunkStorage.EntityTracker)var2.next();
         if (entityTracker.entity == player) {
            entityTracker.updateCameraPosition(this.world.getPlayers());
         } else {
            entityTracker.updateCameraPosition(player);
         }
      }

      int i = MathHelper.floor(player.getX()) >> 4;
      int j = MathHelper.floor(player.getZ()) >> 4;
      ChunkSectionPos chunkSectionPos = player.getCameraPosition();
      ChunkSectionPos chunkSectionPos2 = ChunkSectionPos.from((Entity)player);
      long l = chunkSectionPos.toChunkPos().toLong();
      long m = chunkSectionPos2.toChunkPos().toLong();
      boolean bl = this.playerChunkWatchingManager.isWatchDisabled(player);
      boolean bl2 = this.doesNotGenerateChunks(player);
      boolean bl3 = chunkSectionPos.asLong() != chunkSectionPos2.asLong();
      if (bl3 || bl != bl2) {
         this.method_20726(player);
         if (!bl) {
            this.ticketManager.handleChunkLeave(chunkSectionPos, player);
         }

         if (!bl2) {
            this.ticketManager.handleChunkEnter(chunkSectionPos2, player);
         }

         if (!bl && bl2) {
            this.playerChunkWatchingManager.disableWatch(player);
         }

         if (bl && !bl2) {
            this.playerChunkWatchingManager.enableWatch(player);
         }

         if (l != m) {
            this.playerChunkWatchingManager.movePlayer(l, m, player);
         }
      }

      int k = chunkSectionPos.getSectionX();
      int n = chunkSectionPos.getSectionZ();
      int w;
      int x;
      if (Math.abs(k - i) <= this.watchDistance * 2 && Math.abs(n - j) <= this.watchDistance * 2) {
         w = Math.min(i, k) - this.watchDistance;
         x = Math.min(j, n) - this.watchDistance;
         int q = Math.max(i, k) + this.watchDistance;
         int r = Math.max(j, n) + this.watchDistance;

         for(int s = w; s <= q; ++s) {
            for(int t = x; t <= r; ++t) {
               ChunkPos chunkPos = new ChunkPos(s, t);
               boolean bl4 = getChebyshevDistance(chunkPos, k, n) <= this.watchDistance;
               boolean bl5 = getChebyshevDistance(chunkPos, i, j) <= this.watchDistance;
               this.sendWatchPackets(player, chunkPos, new Packet[2], bl4, bl5);
            }
         }
      } else {
         ChunkPos chunkPos3;
         boolean bl8;
         boolean bl9;
         for(w = k - this.watchDistance; w <= k + this.watchDistance; ++w) {
            for(x = n - this.watchDistance; x <= n + this.watchDistance; ++x) {
               chunkPos3 = new ChunkPos(w, x);
               bl8 = true;
               bl9 = false;
               this.sendWatchPackets(player, chunkPos3, new Packet[2], true, false);
            }
         }

         for(w = i - this.watchDistance; w <= i + this.watchDistance; ++w) {
            for(x = j - this.watchDistance; x <= j + this.watchDistance; ++x) {
               chunkPos3 = new ChunkPos(w, x);
               bl8 = false;
               bl9 = true;
               this.sendWatchPackets(player, chunkPos3, new Packet[2], false, true);
            }
         }
      }

   }

   public Stream<ServerPlayerEntity> getPlayersWatchingChunk(ChunkPos chunkPos, boolean onlyOnWatchDistanceEdge) {
      return this.playerChunkWatchingManager.getPlayersWatchingChunk(chunkPos.toLong()).filter((serverPlayerEntity) -> {
         int i = getChebyshevDistance(chunkPos, serverPlayerEntity, true);
         if (i > this.watchDistance) {
            return false;
         } else {
            return !onlyOnWatchDistanceEdge || i == this.watchDistance;
         }
      });
   }

   protected void loadEntity(Entity entity) {
      if (!(entity instanceof EnderDragonPart)) {
         if (!(entity instanceof LightningEntity)) {
            EntityType<?> entityType = entity.getType();
            int i = entityType.getMaxTrackDistance() * 16;
            int j = entityType.getTrackTickInterval();
            if (this.entityTrackers.containsKey(entity.getEntityId())) {
               throw (IllegalStateException)Util.throwOrPause(new IllegalStateException("Entity is already tracked!"));
            } else {
               ThreadedAnvilChunkStorage.EntityTracker entityTracker = new ThreadedAnvilChunkStorage.EntityTracker(entity, i, j, entityType.alwaysUpdateVelocity());
               this.entityTrackers.put(entity.getEntityId(), entityTracker);
               entityTracker.updateCameraPosition(this.world.getPlayers());
               if (entity instanceof ServerPlayerEntity) {
                  ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
                  this.handlePlayerAddedOrRemoved(serverPlayerEntity, true);
                  ObjectIterator var7 = this.entityTrackers.values().iterator();

                  while(var7.hasNext()) {
                     ThreadedAnvilChunkStorage.EntityTracker entityTracker2 = (ThreadedAnvilChunkStorage.EntityTracker)var7.next();
                     if (entityTracker2.entity != serverPlayerEntity) {
                        entityTracker2.updateCameraPosition(serverPlayerEntity);
                     }
                  }
               }

            }
         }
      }
   }

   protected void unloadEntity(Entity entity) {
      if (entity instanceof ServerPlayerEntity) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
         this.handlePlayerAddedOrRemoved(serverPlayerEntity, false);
         ObjectIterator var3 = this.entityTrackers.values().iterator();

         while(var3.hasNext()) {
            ThreadedAnvilChunkStorage.EntityTracker entityTracker = (ThreadedAnvilChunkStorage.EntityTracker)var3.next();
            entityTracker.stopTracking(serverPlayerEntity);
         }
      }

      ThreadedAnvilChunkStorage.EntityTracker entityTracker2 = (ThreadedAnvilChunkStorage.EntityTracker)this.entityTrackers.remove(entity.getEntityId());
      if (entityTracker2 != null) {
         entityTracker2.stopTracking();
      }

   }

   protected void tickPlayerMovement() {
      List<ServerPlayerEntity> list = Lists.newArrayList();
      List<ServerPlayerEntity> list2 = this.world.getPlayers();

      ObjectIterator var3;
      ThreadedAnvilChunkStorage.EntityTracker entityTracker2;
      for(var3 = this.entityTrackers.values().iterator(); var3.hasNext(); entityTracker2.entry.tick()) {
         entityTracker2 = (ThreadedAnvilChunkStorage.EntityTracker)var3.next();
         ChunkSectionPos chunkSectionPos = entityTracker2.lastCameraPosition;
         ChunkSectionPos chunkSectionPos2 = ChunkSectionPos.from(entityTracker2.entity);
         if (!Objects.equals(chunkSectionPos, chunkSectionPos2)) {
            entityTracker2.updateCameraPosition(list2);
            Entity entity = entityTracker2.entity;
            if (entity instanceof ServerPlayerEntity) {
               list.add((ServerPlayerEntity)entity);
            }

            entityTracker2.lastCameraPosition = chunkSectionPos2;
         }
      }

      if (!list.isEmpty()) {
         var3 = this.entityTrackers.values().iterator();

         while(var3.hasNext()) {
            entityTracker2 = (ThreadedAnvilChunkStorage.EntityTracker)var3.next();
            entityTracker2.updateCameraPosition((List)list);
         }
      }

   }

   protected void sendToOtherNearbyPlayers(Entity entity, Packet<?> packet) {
      ThreadedAnvilChunkStorage.EntityTracker entityTracker = (ThreadedAnvilChunkStorage.EntityTracker)this.entityTrackers.get(entity.getEntityId());
      if (entityTracker != null) {
         entityTracker.sendToOtherNearbyPlayers(packet);
      }

   }

   protected void sendToNearbyPlayers(Entity entity, Packet<?> packet) {
      ThreadedAnvilChunkStorage.EntityTracker entityTracker = (ThreadedAnvilChunkStorage.EntityTracker)this.entityTrackers.get(entity.getEntityId());
      if (entityTracker != null) {
         entityTracker.sendToNearbyPlayers(packet);
      }

   }

   private void sendChunkDataPackets(ServerPlayerEntity player, Packet<?>[] packets, WorldChunk chunk) {
      if (packets[0] == null) {
         packets[0] = new ChunkDataS2CPacket(chunk, 65535);
         packets[1] = new LightUpdateS2CPacket(chunk.getPos(), this.serverLightingProvider);
      }

      player.sendInitialChunkPackets(chunk.getPos(), packets[0], packets[1]);
      DebugRendererInfoManager.method_19775(this.world, chunk.getPos());
      List<Entity> list = Lists.newArrayList();
      List<Entity> list2 = Lists.newArrayList();
      ObjectIterator var6 = this.entityTrackers.values().iterator();

      while(var6.hasNext()) {
         ThreadedAnvilChunkStorage.EntityTracker entityTracker = (ThreadedAnvilChunkStorage.EntityTracker)var6.next();
         Entity entity = entityTracker.entity;
         if (entity != player && entity.chunkX == chunk.getPos().x && entity.chunkZ == chunk.getPos().z) {
            entityTracker.updateCameraPosition(player);
            if (entity instanceof MobEntity && ((MobEntity)entity).getHoldingEntity() != null) {
               list.add(entity);
            }

            if (!entity.getPassengerList().isEmpty()) {
               list2.add(entity);
            }
         }
      }

      Iterator var9;
      Entity entity3;
      if (!list.isEmpty()) {
         var9 = list.iterator();

         while(var9.hasNext()) {
            entity3 = (Entity)var9.next();
            player.networkHandler.sendPacket(new EntityAttachS2CPacket(entity3, ((MobEntity)entity3).getHoldingEntity()));
         }
      }

      if (!list2.isEmpty()) {
         var9 = list2.iterator();

         while(var9.hasNext()) {
            entity3 = (Entity)var9.next();
            player.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(entity3));
         }
      }

   }

   protected PointOfInterestStorage getPointOfInterestStorage() {
      return this.pointOfInterestStorage;
   }

   public CompletableFuture<Void> method_20576(WorldChunk worldChunk) {
      return this.mainThreadExecutor.submit(() -> {
         worldChunk.enableTickSchedulers(this.world);
      });
   }

   class EntityTracker {
      private final EntityTrackerEntry entry;
      private final Entity entity;
      private final int maxDistance;
      private ChunkSectionPos lastCameraPosition;
      private final Set<ServerPlayerEntity> playersTracking = Sets.newHashSet();

      public EntityTracker(Entity maxDistance, int tickInterval, int i, boolean bl) {
         this.entry = new EntityTrackerEntry(ThreadedAnvilChunkStorage.this.world, maxDistance, i, bl, this::sendToOtherNearbyPlayers);
         this.entity = maxDistance;
         this.maxDistance = tickInterval;
         this.lastCameraPosition = ChunkSectionPos.from(maxDistance);
      }

      public boolean equals(Object o) {
         if (o instanceof ThreadedAnvilChunkStorage.EntityTracker) {
            return ((ThreadedAnvilChunkStorage.EntityTracker)o).entity.getEntityId() == this.entity.getEntityId();
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.entity.getEntityId();
      }

      public void sendToOtherNearbyPlayers(Packet<?> packet) {
         Iterator var2 = this.playersTracking.iterator();

         while(var2.hasNext()) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var2.next();
            serverPlayerEntity.networkHandler.sendPacket(packet);
         }

      }

      public void sendToNearbyPlayers(Packet<?> packet) {
         this.sendToOtherNearbyPlayers(packet);
         if (this.entity instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity)this.entity).networkHandler.sendPacket(packet);
         }

      }

      public void stopTracking() {
         Iterator var1 = this.playersTracking.iterator();

         while(var1.hasNext()) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var1.next();
            this.entry.stopTracking(serverPlayerEntity);
         }

      }

      public void stopTracking(ServerPlayerEntity serverPlayerEntity) {
         if (this.playersTracking.remove(serverPlayerEntity)) {
            this.entry.stopTracking(serverPlayerEntity);
         }

      }

      public void updateCameraPosition(ServerPlayerEntity player) {
         if (player != this.entity) {
            Vec3d vec3d = player.getPos().subtract(this.entry.getLastPos());
            int i = Math.min(this.getMaxTrackDistance(), (ThreadedAnvilChunkStorage.this.watchDistance - 1) * 16);
            boolean bl = vec3d.x >= (double)(-i) && vec3d.x <= (double)i && vec3d.z >= (double)(-i) && vec3d.z <= (double)i && this.entity.canBeSpectated(player);
            if (bl) {
               boolean bl2 = this.entity.teleporting;
               if (!bl2) {
                  ChunkPos chunkPos = new ChunkPos(this.entity.chunkX, this.entity.chunkZ);
                  ChunkHolder chunkHolder = ThreadedAnvilChunkStorage.this.getChunkHolder(chunkPos.toLong());
                  if (chunkHolder != null && chunkHolder.getWorldChunk() != null) {
                     bl2 = ThreadedAnvilChunkStorage.getChebyshevDistance(chunkPos, player, false) <= ThreadedAnvilChunkStorage.this.watchDistance;
                  }
               }

               if (bl2 && this.playersTracking.add(player)) {
                  this.entry.startTracking(player);
               }
            } else if (this.playersTracking.remove(player)) {
               this.entry.stopTracking(player);
            }

         }
      }

      private int getMaxTrackDistance() {
         Collection<Entity> collection = this.entity.getPassengersDeep();
         int i = this.maxDistance;
         Iterator var3 = collection.iterator();

         while(var3.hasNext()) {
            Entity entity = (Entity)var3.next();
            int j = entity.getType().getMaxTrackDistance() * 16;
            if (j > i) {
               i = j;
            }
         }

         return i;
      }

      public void updateCameraPosition(List<ServerPlayerEntity> players) {
         Iterator var2 = players.iterator();

         while(var2.hasNext()) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var2.next();
            this.updateCameraPosition(serverPlayerEntity);
         }

      }
   }

   class TicketManager extends ChunkTicketManager {
      protected TicketManager(Executor mainThreadExecutor, Executor executor) {
         super(mainThreadExecutor, executor);
      }

      protected boolean isUnloaded(long pos) {
         return ThreadedAnvilChunkStorage.this.unloadedChunks.contains(pos);
      }

      @Nullable
      protected ChunkHolder getChunkHolder(long pos) {
         return ThreadedAnvilChunkStorage.this.getCurrentChunkHolder(pos);
      }

      @Nullable
      protected ChunkHolder setLevel(long pos, int level, @Nullable ChunkHolder holder, int i) {
         return ThreadedAnvilChunkStorage.this.setLevel(pos, level, holder, i);
      }
   }
}
