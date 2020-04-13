package net.minecraft.world;

import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;

public class ChunkTickScheduler<T> implements TickScheduler<T> {
   protected final Predicate<T> shouldExclude;
   private final ChunkPos pos;
   private final ShortList[] scheduledPositions;

   public ChunkTickScheduler(Predicate<T> shouldExclude, ChunkPos chunkPos) {
      this(shouldExclude, chunkPos, new ListTag());
   }

   public ChunkTickScheduler(Predicate<T> shouldExclude, ChunkPos chunkPos, ListTag listTag) {
      this.scheduledPositions = new ShortList[16];
      this.shouldExclude = shouldExclude;
      this.pos = chunkPos;

      for(int i = 0; i < listTag.size(); ++i) {
         ListTag listTag2 = listTag.getList(i);

         for(int j = 0; j < listTag2.size(); ++j) {
            Chunk.getList(this.scheduledPositions, i).add(listTag2.getShort(j));
         }
      }

   }

   public ListTag toNbt() {
      return ChunkSerializer.toNbt(this.scheduledPositions);
   }

   public void tick(TickScheduler<T> scheduler, Function<BlockPos, T> dataMapper) {
      for(int i = 0; i < this.scheduledPositions.length; ++i) {
         if (this.scheduledPositions[i] != null) {
            ShortListIterator var4 = this.scheduledPositions[i].iterator();

            while(var4.hasNext()) {
               Short var5 = (Short)var4.next();
               BlockPos blockPos = ProtoChunk.joinBlockPos(var5, i, this.pos);
               scheduler.schedule(blockPos, dataMapper.apply(blockPos), 0);
            }

            this.scheduledPositions[i].clear();
         }
      }

   }

   public boolean isScheduled(BlockPos pos, T object) {
      return false;
   }

   public void schedule(BlockPos pos, T object, int delay, TickPriority priority) {
      Chunk.getList(this.scheduledPositions, pos.getY() >> 4).add(ProtoChunk.getPackedSectionRelative(pos));
   }

   public boolean isTicking(BlockPos pos, T object) {
      return false;
   }

   public void scheduleAll(Stream<ScheduledTick<T>> stream) {
      stream.forEach((scheduledTick) -> {
         this.schedule(scheduledTick.pos, scheduledTick.getObject(), 0, scheduledTick.priority);
      });
   }
}
