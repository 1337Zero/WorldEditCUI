package net.minecraft.client.render.chunk;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class ChunkOcclusionDataBuilder {
   private static final int STEP_X = (int)Math.pow(16.0D, 0.0D);
   private static final int STEP_Z = (int)Math.pow(16.0D, 1.0D);
   private static final int STEP_Y = (int)Math.pow(16.0D, 2.0D);
   private static final Direction[] DIRECTIONS = Direction.values();
   private final BitSet closed = new BitSet(4096);
   private static final int[] EDGE_POINTS = (int[])Util.make(new int[1352], (is) -> {
      int i = false;
      int j = true;
      int k = 0;

      for(int l = 0; l < 16; ++l) {
         for(int m = 0; m < 16; ++m) {
            for(int n = 0; n < 16; ++n) {
               if (l == 0 || l == 15 || m == 0 || m == 15 || n == 0 || n == 15) {
                  is[k++] = pack(l, m, n);
               }
            }
         }
      }

   });
   private int openCount = 4096;

   public void markClosed(BlockPos pos) {
      this.closed.set(pack(pos), true);
      --this.openCount;
   }

   private static int pack(BlockPos pos) {
      return pack(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
   }

   private static int pack(int x, int y, int z) {
      return x << 0 | y << 8 | z << 4;
   }

   public ChunkOcclusionData build() {
      ChunkOcclusionData chunkOcclusionData = new ChunkOcclusionData();
      if (4096 - this.openCount < 256) {
         chunkOcclusionData.fill(true);
      } else if (this.openCount == 0) {
         chunkOcclusionData.fill(false);
      } else {
         int[] var2 = EDGE_POINTS;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            int i = var2[var4];
            if (!this.closed.get(i)) {
               chunkOcclusionData.addOpenEdgeFaces(this.getOpenFaces(i));
            }
         }
      }

      return chunkOcclusionData;
   }

   public Set<Direction> getOpenFaces(BlockPos pos) {
      return this.getOpenFaces(pack(pos));
   }

   private Set<Direction> getOpenFaces(int pos) {
      Set<Direction> set = EnumSet.noneOf(Direction.class);
      IntPriorityQueue intPriorityQueue = new IntArrayFIFOQueue();
      intPriorityQueue.enqueue(pos);
      this.closed.set(pos, true);

      while(!intPriorityQueue.isEmpty()) {
         int i = intPriorityQueue.dequeueInt();
         this.addEdgeFaces(i, set);
         Direction[] var5 = DIRECTIONS;
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            Direction direction = var5[var7];
            int j = this.offset(i, direction);
            if (j >= 0 && !this.closed.get(j)) {
               this.closed.set(j, true);
               intPriorityQueue.enqueue(j);
            }
         }
      }

      return set;
   }

   private void addEdgeFaces(int pos, Set<Direction> openFaces) {
      int i = pos >> 0 & 15;
      if (i == 0) {
         openFaces.add(Direction.WEST);
      } else if (i == 15) {
         openFaces.add(Direction.EAST);
      }

      int j = pos >> 8 & 15;
      if (j == 0) {
         openFaces.add(Direction.DOWN);
      } else if (j == 15) {
         openFaces.add(Direction.UP);
      }

      int k = pos >> 4 & 15;
      if (k == 0) {
         openFaces.add(Direction.NORTH);
      } else if (k == 15) {
         openFaces.add(Direction.SOUTH);
      }

   }

   private int offset(int pos, Direction direction) {
      switch(direction) {
      case DOWN:
         if ((pos >> 8 & 15) == 0) {
            return -1;
         }

         return pos - STEP_Y;
      case UP:
         if ((pos >> 8 & 15) == 15) {
            return -1;
         }

         return pos + STEP_Y;
      case NORTH:
         if ((pos >> 4 & 15) == 0) {
            return -1;
         }

         return pos - STEP_Z;
      case SOUTH:
         if ((pos >> 4 & 15) == 15) {
            return -1;
         }

         return pos + STEP_Z;
      case WEST:
         if ((pos >> 0 & 15) == 0) {
            return -1;
         }

         return pos - STEP_X;
      case EAST:
         if ((pos >> 0 & 15) == 15) {
            return -1;
         }

         return pos + STEP_X;
      default:
         return -1;
      }
   }
}
