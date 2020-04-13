package net.minecraft.server.world;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.thread.MessageListener;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.light.LightingProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerLightingProvider extends LightingProvider implements AutoCloseable {
   private static final Logger LOGGER = LogManager.getLogger();
   private final TaskExecutor<Runnable> processor;
   private final ObjectList<Pair<ServerLightingProvider.Stage, Runnable>> pendingTasks = new ObjectArrayList();
   private final ThreadedAnvilChunkStorage chunkStorage;
   private final MessageListener<ChunkTaskPrioritySystem.Task<Runnable>> executor;
   private volatile int taskBatchSize = 5;
   private final AtomicBoolean field_18812 = new AtomicBoolean();

   public ServerLightingProvider(ChunkProvider chunkProvider, ThreadedAnvilChunkStorage chunkStorage, boolean bl, TaskExecutor<Runnable> processor, MessageListener<ChunkTaskPrioritySystem.Task<Runnable>> executor) {
      super(chunkProvider, true, bl);
      this.chunkStorage = chunkStorage;
      this.executor = executor;
      this.processor = processor;
   }

   public void close() {
   }

   public int doLightUpdates(int maxUpdateCount, boolean doSkylight, boolean skipEdgeLightPropagation) {
      throw (UnsupportedOperationException)Util.throwOrPause(new UnsupportedOperationException("Ran authomatically on a different thread!"));
   }

   public void addLightSource(BlockPos pos, int level) {
      throw (UnsupportedOperationException)Util.throwOrPause(new UnsupportedOperationException("Ran authomatically on a different thread!"));
   }

   public void checkBlock(BlockPos pos) {
      BlockPos blockPos = pos.toImmutable();
      this.enqueue(pos.getX() >> 4, pos.getZ() >> 4, ServerLightingProvider.Stage.POST_UPDATE, Util.debugRunnable(() -> {
         super.checkBlock(blockPos);
      }, () -> {
         return "checkBlock " + blockPos;
      }));
   }

   protected void updateChunkStatus(ChunkPos pos) {
      this.enqueue(pos.x, pos.z, () -> {
         return 0;
      }, ServerLightingProvider.Stage.PRE_UPDATE, Util.debugRunnable(() -> {
         super.setRetainData(pos, false);
         super.setLightEnabled(pos, false);

         int j;
         for(j = -1; j < 17; ++j) {
            super.queueData(LightType.BLOCK, ChunkSectionPos.from(pos, j), (ChunkNibbleArray)null);
            super.queueData(LightType.SKY, ChunkSectionPos.from(pos, j), (ChunkNibbleArray)null);
         }

         for(j = 0; j < 16; ++j) {
            super.updateSectionStatus(ChunkSectionPos.from(pos, j), true);
         }

      }, () -> {
         return "updateChunkStatus " + pos + " " + true;
      }));
   }

   public void updateSectionStatus(ChunkSectionPos pos, boolean status) {
      this.enqueue(pos.getSectionX(), pos.getSectionZ(), () -> {
         return 0;
      }, ServerLightingProvider.Stage.PRE_UPDATE, Util.debugRunnable(() -> {
         super.updateSectionStatus(pos, status);
      }, () -> {
         return "updateSectionStatus " + pos + " " + status;
      }));
   }

   public void setLightEnabled(ChunkPos pos, boolean lightEnabled) {
      this.enqueue(pos.x, pos.z, ServerLightingProvider.Stage.PRE_UPDATE, Util.debugRunnable(() -> {
         super.setLightEnabled(pos, lightEnabled);
      }, () -> {
         return "enableLight " + pos + " " + lightEnabled;
      }));
   }

   public void queueData(LightType lightType, ChunkSectionPos chunkSectionPos, @Nullable ChunkNibbleArray chunkNibbleArray) {
      this.enqueue(chunkSectionPos.getSectionX(), chunkSectionPos.getSectionZ(), () -> {
         return 0;
      }, ServerLightingProvider.Stage.PRE_UPDATE, Util.debugRunnable(() -> {
         super.queueData(lightType, chunkSectionPos, chunkNibbleArray);
      }, () -> {
         return "queueData " + chunkSectionPos;
      }));
   }

   private void enqueue(int x, int z, ServerLightingProvider.Stage stage, Runnable task) {
      this.enqueue(x, z, this.chunkStorage.getCompletedLevelSupplier(ChunkPos.toLong(x, z)), stage, task);
   }

   private void enqueue(int x, int z, IntSupplier completedLevelSupplier, ServerLightingProvider.Stage stage, Runnable task) {
      this.executor.send(ChunkTaskPrioritySystem.createMessage(() -> {
         this.pendingTasks.add(Pair.of(stage, task));
         if (this.pendingTasks.size() >= this.taskBatchSize) {
            this.runTasks();
         }

      }, ChunkPos.toLong(x, z), completedLevelSupplier));
   }

   public void setRetainData(ChunkPos pos, boolean retainData) {
      this.enqueue(pos.x, pos.z, () -> {
         return 0;
      }, ServerLightingProvider.Stage.PRE_UPDATE, Util.debugRunnable(() -> {
         super.setRetainData(pos, retainData);
      }, () -> {
         return "retainData " + pos;
      }));
   }

   public CompletableFuture<Chunk> light(Chunk chunk, boolean bl) {
      ChunkPos chunkPos = chunk.getPos();
      chunk.setLightOn(false);
      this.enqueue(chunkPos.x, chunkPos.z, ServerLightingProvider.Stage.PRE_UPDATE, Util.debugRunnable(() -> {
         ChunkSection[] chunkSections = chunk.getSectionArray();

         for(int i = 0; i < 16; ++i) {
            ChunkSection chunkSection = chunkSections[i];
            if (!ChunkSection.isEmpty(chunkSection)) {
               super.updateSectionStatus(ChunkSectionPos.from(chunkPos, i), false);
            }
         }

         super.setLightEnabled(chunkPos, true);
         if (!bl) {
            chunk.getLightSourcesStream().forEach((blockPos) -> {
               super.addLightSource(blockPos, chunk.getLuminance(blockPos));
            });
         }

         this.chunkStorage.releaseLightTicket(chunkPos);
      }, () -> {
         return "lightChunk " + chunkPos + " " + bl;
      }));
      return CompletableFuture.supplyAsync(() -> {
         chunk.setLightOn(true);
         super.setRetainData(chunkPos, false);
         return chunk;
      }, (runnable) -> {
         this.enqueue(chunkPos.x, chunkPos.z, ServerLightingProvider.Stage.POST_UPDATE, runnable);
      });
   }

   public void tick() {
      if ((!this.pendingTasks.isEmpty() || super.hasUpdates()) && this.field_18812.compareAndSet(false, true)) {
         this.processor.send(() -> {
            this.runTasks();
            this.field_18812.set(false);
         });
      }

   }

   private void runTasks() {
      int i = Math.min(this.pendingTasks.size(), this.taskBatchSize);
      ObjectListIterator<Pair<ServerLightingProvider.Stage, Runnable>> objectListIterator = this.pendingTasks.iterator();

      int j;
      Pair pair2;
      for(j = 0; objectListIterator.hasNext() && j < i; ++j) {
         pair2 = (Pair)objectListIterator.next();
         if (pair2.getFirst() == ServerLightingProvider.Stage.PRE_UPDATE) {
            ((Runnable)pair2.getSecond()).run();
         }
      }

      objectListIterator.back(j);
      super.doLightUpdates(Integer.MAX_VALUE, true, true);

      for(j = 0; objectListIterator.hasNext() && j < i; ++j) {
         pair2 = (Pair)objectListIterator.next();
         if (pair2.getFirst() == ServerLightingProvider.Stage.POST_UPDATE) {
            ((Runnable)pair2.getSecond()).run();
         }

         objectListIterator.remove();
      }

   }

   public void setTaskBatchSize(int taskBatchSize) {
      this.taskBatchSize = taskBatchSize;
   }

   static enum Stage {
      PRE_UPDATE,
      POST_UPDATE;
   }
}
