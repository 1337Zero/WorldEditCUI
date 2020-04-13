package net.minecraft.world.chunk.light;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import org.apache.commons.lang3.mutable.MutableInt;

public final class ChunkSkyLightProvider extends ChunkLightProvider<SkyLightStorage.Data, SkyLightStorage> {
   private static final Direction[] DIRECTIONS = Direction.values();
   private static final Direction[] HORIZONTAL_DIRECTIONS;

   public ChunkSkyLightProvider(ChunkProvider chunkProvider) {
      super(chunkProvider, LightType.SKY, new SkyLightStorage(chunkProvider));
   }

   protected int getPropagatedLevel(long sourceId, long targetId, int level) {
      if (targetId == Long.MAX_VALUE) {
         return 15;
      } else {
         if (sourceId == Long.MAX_VALUE) {
            if (!((SkyLightStorage)this.lightStorage).method_15565(targetId)) {
               return 15;
            }

            level = 0;
         }

         if (level >= 15) {
            return level;
         } else {
            MutableInt mutableInt = new MutableInt();
            BlockState blockState = this.getStateForLighting(targetId, mutableInt);
            if (mutableInt.getValue() >= 15) {
               return 15;
            } else {
               int i = BlockPos.unpackLongX(sourceId);
               int j = BlockPos.unpackLongY(sourceId);
               int k = BlockPos.unpackLongZ(sourceId);
               int l = BlockPos.unpackLongX(targetId);
               int m = BlockPos.unpackLongY(targetId);
               int n = BlockPos.unpackLongZ(targetId);
               boolean bl = i == l && k == n;
               int o = Integer.signum(l - i);
               int p = Integer.signum(m - j);
               int q = Integer.signum(n - k);
               Direction direction2;
               if (sourceId == Long.MAX_VALUE) {
                  direction2 = Direction.DOWN;
               } else {
                  direction2 = Direction.fromVector(o, p, q);
               }

               BlockState blockState2 = this.getStateForLighting(sourceId, (MutableInt)null);
               VoxelShape voxelShape;
               if (direction2 != null) {
                  voxelShape = this.getOpaqueShape(blockState2, sourceId, direction2);
                  VoxelShape voxelShape2 = this.getOpaqueShape(blockState, targetId, direction2.getOpposite());
                  if (VoxelShapes.unionCoversFullCube(voxelShape, voxelShape2)) {
                     return 15;
                  }
               } else {
                  voxelShape = this.getOpaqueShape(blockState2, sourceId, Direction.DOWN);
                  if (VoxelShapes.unionCoversFullCube(voxelShape, VoxelShapes.empty())) {
                     return 15;
                  }

                  int r = bl ? -1 : 0;
                  Direction direction3 = Direction.fromVector(o, r, q);
                  if (direction3 == null) {
                     return 15;
                  }

                  VoxelShape voxelShape4 = this.getOpaqueShape(blockState, targetId, direction3.getOpposite());
                  if (VoxelShapes.unionCoversFullCube(VoxelShapes.empty(), voxelShape4)) {
                     return 15;
                  }
               }

               boolean bl2 = sourceId == Long.MAX_VALUE || bl && j > m;
               return bl2 && level == 0 && mutableInt.getValue() == 0 ? 0 : level + Math.max(1, mutableInt.getValue());
            }
         }
      }
   }

   protected void propagateLevel(long id, int level, boolean decrease) {
      long l = ChunkSectionPos.fromGlobalPos(id);
      int i = BlockPos.unpackLongY(id);
      int j = ChunkSectionPos.getLocalCoord(i);
      int k = ChunkSectionPos.getSectionCoord(i);
      int o;
      if (j != 0) {
         o = 0;
      } else {
         int n;
         for(n = 0; !((SkyLightStorage)this.lightStorage).hasLight(ChunkSectionPos.offset(l, 0, -n - 1, 0)) && ((SkyLightStorage)this.lightStorage).isAboveMinimumHeight(k - n - 1); ++n) {
         }

         o = n;
      }

      long p = BlockPos.add(id, 0, -1 - o * 16, 0);
      long q = ChunkSectionPos.fromGlobalPos(p);
      if (l == q || ((SkyLightStorage)this.lightStorage).hasLight(q)) {
         this.propagateLevel(id, p, level, decrease);
      }

      long r = BlockPos.offset(id, Direction.UP);
      long s = ChunkSectionPos.fromGlobalPos(r);
      if (l == s || ((SkyLightStorage)this.lightStorage).hasLight(s)) {
         this.propagateLevel(id, r, level, decrease);
      }

      Direction[] var19 = HORIZONTAL_DIRECTIONS;
      int var20 = var19.length;

      for(int var21 = 0; var21 < var20; ++var21) {
         Direction direction = var19[var21];
         int t = 0;

         while(true) {
            long u = BlockPos.add(id, direction.getOffsetX(), -t, direction.getOffsetZ());
            long v = ChunkSectionPos.fromGlobalPos(u);
            if (l == v) {
               this.propagateLevel(id, u, level, decrease);
               break;
            }

            if (((SkyLightStorage)this.lightStorage).hasLight(v)) {
               this.propagateLevel(id, u, level, decrease);
            }

            ++t;
            if (t > o * 16) {
               break;
            }
         }
      }

   }

   protected int recalculateLevel(long id, long excludedId, int maxLevel) {
      int i = maxLevel;
      if (Long.MAX_VALUE != excludedId) {
         int j = this.getPropagatedLevel(Long.MAX_VALUE, id, 0);
         if (maxLevel > j) {
            i = j;
         }

         if (i == 0) {
            return i;
         }
      }

      long l = ChunkSectionPos.fromGlobalPos(id);
      ChunkNibbleArray chunkNibbleArray = ((SkyLightStorage)this.lightStorage).getLightArray(l, true);
      Direction[] var10 = DIRECTIONS;
      int var11 = var10.length;

      for(int var12 = 0; var12 < var11; ++var12) {
         Direction direction = var10[var12];
         long m = BlockPos.offset(id, direction);
         long n = ChunkSectionPos.fromGlobalPos(m);
         ChunkNibbleArray chunkNibbleArray3;
         if (l == n) {
            chunkNibbleArray3 = chunkNibbleArray;
         } else {
            chunkNibbleArray3 = ((SkyLightStorage)this.lightStorage).getLightArray(n, true);
         }

         if (chunkNibbleArray3 != null) {
            if (m != excludedId) {
               int k = this.getPropagatedLevel(m, id, this.getCurrentLevelFromArray(chunkNibbleArray3, m));
               if (i > k) {
                  i = k;
               }

               if (i == 0) {
                  return i;
               }
            }
         } else if (direction != Direction.DOWN) {
            for(m = BlockPos.removeChunkSectionLocalY(m); !((SkyLightStorage)this.lightStorage).hasLight(n) && !((SkyLightStorage)this.lightStorage).isAboveTopmostLightArray(n); m = BlockPos.add(m, 0, 16, 0)) {
               n = ChunkSectionPos.offset(n, Direction.UP);
            }

            ChunkNibbleArray chunkNibbleArray4 = ((SkyLightStorage)this.lightStorage).getLightArray(n, true);
            if (m != excludedId) {
               int p;
               if (chunkNibbleArray4 != null) {
                  p = this.getPropagatedLevel(m, id, this.getCurrentLevelFromArray(chunkNibbleArray4, m));
               } else {
                  p = ((SkyLightStorage)this.lightStorage).isLightEnabled(n) ? 0 : 15;
               }

               if (i > p) {
                  i = p;
               }

               if (i == 0) {
                  return i;
               }
            }
         }
      }

      return i;
   }

   protected void resetLevel(long id) {
      ((SkyLightStorage)this.lightStorage).updateAll();
      long l = ChunkSectionPos.fromGlobalPos(id);
      if (((SkyLightStorage)this.lightStorage).hasLight(l)) {
         super.resetLevel(id);
      } else {
         for(id = BlockPos.removeChunkSectionLocalY(id); !((SkyLightStorage)this.lightStorage).hasLight(l) && !((SkyLightStorage)this.lightStorage).isAboveTopmostLightArray(l); id = BlockPos.add(id, 0, 16, 0)) {
            l = ChunkSectionPos.offset(l, Direction.UP);
         }

         if (((SkyLightStorage)this.lightStorage).hasLight(l)) {
            super.resetLevel(id);
         }
      }

   }

   @Environment(EnvType.CLIENT)
   public String method_22875(long l) {
      return super.method_22875(l) + (((SkyLightStorage)this.lightStorage).isAboveTopmostLightArray(l) ? "*" : "");
   }

   static {
      HORIZONTAL_DIRECTIONS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
   }
}
