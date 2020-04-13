package net.minecraft.world.chunk.light;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Arrays;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkToNibbleArrayMap;
import net.minecraft.world.chunk.ColumnChunkNibbleArray;

public class SkyLightStorage extends LightStorage<SkyLightStorage.Data> {
   private static final Direction[] LIGHT_REDUCTION_DIRECTIONS;
   private final LongSet field_15820 = new LongOpenHashSet();
   private final LongSet pendingSkylightUpdates = new LongOpenHashSet();
   private final LongSet field_15816 = new LongOpenHashSet();
   private final LongSet lightEnabled = new LongOpenHashSet();
   private volatile boolean hasSkyLightUpdates;

   protected SkyLightStorage(ChunkProvider chunkProvider) {
      super(LightType.SKY, chunkProvider, new SkyLightStorage.Data(new Long2ObjectOpenHashMap(), new Long2IntOpenHashMap(), Integer.MAX_VALUE));
   }

   protected int getLight(long blockPos) {
      long l = ChunkSectionPos.fromGlobalPos(blockPos);
      int i = ChunkSectionPos.getY(l);
      SkyLightStorage.Data data = (SkyLightStorage.Data)this.uncachedLightArrays;
      int j = data.topArraySectionY.get(ChunkSectionPos.withZeroZ(l));
      if (j != data.defaultTopArraySectionY && i < j) {
         ChunkNibbleArray chunkNibbleArray = this.getLightArray(data, l);
         if (chunkNibbleArray == null) {
            for(blockPos = BlockPos.removeChunkSectionLocalY(blockPos); chunkNibbleArray == null; chunkNibbleArray = this.getLightArray(data, l)) {
               l = ChunkSectionPos.offset(l, Direction.UP);
               ++i;
               if (i >= j) {
                  return 15;
               }

               blockPos = BlockPos.add(blockPos, 0, 16, 0);
            }
         }

         return chunkNibbleArray.get(ChunkSectionPos.getLocalCoord(BlockPos.unpackLongX(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongY(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongZ(blockPos)));
      } else {
         return 15;
      }
   }

   protected void onLightArrayCreated(long blockPos) {
      int i = ChunkSectionPos.getY(blockPos);
      if (((SkyLightStorage.Data)this.lightArrays).defaultTopArraySectionY > i) {
         ((SkyLightStorage.Data)this.lightArrays).defaultTopArraySectionY = i;
         ((SkyLightStorage.Data)this.lightArrays).topArraySectionY.defaultReturnValue(((SkyLightStorage.Data)this.lightArrays).defaultTopArraySectionY);
      }

      long l = ChunkSectionPos.withZeroZ(blockPos);
      int j = ((SkyLightStorage.Data)this.lightArrays).topArraySectionY.get(l);
      if (j < i + 1) {
         ((SkyLightStorage.Data)this.lightArrays).topArraySectionY.put(l, i + 1);
         if (this.lightEnabled.contains(l)) {
            this.method_20810(blockPos);
            if (j > ((SkyLightStorage.Data)this.lightArrays).defaultTopArraySectionY) {
               long m = ChunkSectionPos.asLong(ChunkSectionPos.getX(blockPos), j - 1, ChunkSectionPos.getZ(blockPos));
               this.method_20809(m);
            }

            this.checkForUpdates();
         }
      }

   }

   private void method_20809(long l) {
      this.field_15816.add(l);
      this.pendingSkylightUpdates.remove(l);
   }

   private void method_20810(long l) {
      this.pendingSkylightUpdates.add(l);
      this.field_15816.remove(l);
   }

   private void checkForUpdates() {
      this.hasSkyLightUpdates = !this.pendingSkylightUpdates.isEmpty() || !this.field_15816.isEmpty();
   }

   protected void onChunkRemoved(long l) {
      long m = ChunkSectionPos.withZeroZ(l);
      boolean bl = this.lightEnabled.contains(m);
      if (bl) {
         this.method_20809(l);
      }

      int i = ChunkSectionPos.getY(l);
      if (((SkyLightStorage.Data)this.lightArrays).topArraySectionY.get(m) == i + 1) {
         long n;
         for(n = l; !this.hasLight(n) && this.isAboveMinimumHeight(i); n = ChunkSectionPos.offset(n, Direction.DOWN)) {
            --i;
         }

         if (this.hasLight(n)) {
            ((SkyLightStorage.Data)this.lightArrays).topArraySectionY.put(m, i + 1);
            if (bl) {
               this.method_20810(n);
            }
         } else {
            ((SkyLightStorage.Data)this.lightArrays).topArraySectionY.remove(m);
         }
      }

      if (bl) {
         this.checkForUpdates();
      }

   }

   protected void setLightEnabled(long l, boolean bl) {
      this.updateAll();
      if (bl && this.lightEnabled.add(l)) {
         int i = ((SkyLightStorage.Data)this.lightArrays).topArraySectionY.get(l);
         if (i != ((SkyLightStorage.Data)this.lightArrays).defaultTopArraySectionY) {
            long m = ChunkSectionPos.asLong(ChunkSectionPos.getX(l), i - 1, ChunkSectionPos.getZ(l));
            this.method_20810(m);
            this.checkForUpdates();
         }
      } else if (!bl) {
         this.lightEnabled.remove(l);
      }

   }

   protected boolean hasLightUpdates() {
      return super.hasLightUpdates() || this.hasSkyLightUpdates;
   }

   protected ChunkNibbleArray createLightArray(long pos) {
      ChunkNibbleArray chunkNibbleArray = (ChunkNibbleArray)this.lightArraysToAdd.get(pos);
      if (chunkNibbleArray != null) {
         return chunkNibbleArray;
      } else {
         long l = ChunkSectionPos.offset(pos, Direction.UP);
         int i = ((SkyLightStorage.Data)this.lightArrays).topArraySectionY.get(ChunkSectionPos.withZeroZ(pos));
         if (i != ((SkyLightStorage.Data)this.lightArrays).defaultTopArraySectionY && ChunkSectionPos.getY(l) < i) {
            ChunkNibbleArray chunkNibbleArray2;
            while((chunkNibbleArray2 = this.getLightArray(l, true)) == null) {
               l = ChunkSectionPos.offset(l, Direction.UP);
            }

            return new ChunkNibbleArray((new ColumnChunkNibbleArray(chunkNibbleArray2, 0)).asByteArray());
         } else {
            return new ChunkNibbleArray();
         }
      }
   }

   protected void updateLightArrays(ChunkLightProvider<SkyLightStorage.Data, ?> lightProvider, boolean doSkylight, boolean skipEdgeLightPropagation) {
      super.updateLightArrays(lightProvider, doSkylight, skipEdgeLightPropagation);
      if (doSkylight) {
         LongIterator var4;
         long l;
         int ag;
         int j;
         if (!this.pendingSkylightUpdates.isEmpty()) {
            var4 = this.pendingSkylightUpdates.iterator();

            label160:
            while(true) {
               while(true) {
                  do {
                     do {
                        do {
                           if (!var4.hasNext()) {
                              break label160;
                           }

                           l = (Long)var4.next();
                           ag = this.getLevel(l);
                        } while(ag == 2);
                     } while(this.field_15816.contains(l));
                  } while(!this.field_15820.add(l));

                  int k;
                  if (ag == 1) {
                     this.removeChunkData(lightProvider, l);
                     if (this.field_15802.add(l)) {
                        ((SkyLightStorage.Data)this.lightArrays).replaceWithCopy(l);
                     }

                     Arrays.fill(this.getLightArray(l, true).asByteArray(), (byte)-1);
                     j = ChunkSectionPos.getWorldCoord(ChunkSectionPos.getX(l));
                     k = ChunkSectionPos.getWorldCoord(ChunkSectionPos.getY(l));
                     int m = ChunkSectionPos.getWorldCoord(ChunkSectionPos.getZ(l));
                     Direction[] var11 = LIGHT_REDUCTION_DIRECTIONS;
                     int z = var11.length;

                     long ab;
                     for(int var13 = 0; var13 < z; ++var13) {
                        Direction direction = var11[var13];
                        ab = ChunkSectionPos.offset(l, direction);
                        if ((this.field_15816.contains(ab) || !this.field_15820.contains(ab) && !this.pendingSkylightUpdates.contains(ab)) && this.hasLight(ab)) {
                           for(int o = 0; o < 16; ++o) {
                              for(int p = 0; p < 16; ++p) {
                                 long w;
                                 long x;
                                 switch(direction) {
                                 case NORTH:
                                    w = BlockPos.asLong(j + o, k + p, m);
                                    x = BlockPos.asLong(j + o, k + p, m - 1);
                                    break;
                                 case SOUTH:
                                    w = BlockPos.asLong(j + o, k + p, m + 16 - 1);
                                    x = BlockPos.asLong(j + o, k + p, m + 16);
                                    break;
                                 case WEST:
                                    w = BlockPos.asLong(j, k + o, m + p);
                                    x = BlockPos.asLong(j - 1, k + o, m + p);
                                    break;
                                 default:
                                    w = BlockPos.asLong(j + 16 - 1, k + o, m + p);
                                    x = BlockPos.asLong(j + 16, k + o, m + p);
                                 }

                                 lightProvider.updateLevel(w, x, lightProvider.getPropagatedLevel(w, x, 0), true);
                              }
                           }
                        }
                     }

                     for(int y = 0; y < 16; ++y) {
                        for(z = 0; z < 16; ++z) {
                           long aa = BlockPos.asLong(ChunkSectionPos.getWorldCoord(ChunkSectionPos.getX(l)) + y, ChunkSectionPos.getWorldCoord(ChunkSectionPos.getY(l)), ChunkSectionPos.getWorldCoord(ChunkSectionPos.getZ(l)) + z);
                           ab = BlockPos.asLong(ChunkSectionPos.getWorldCoord(ChunkSectionPos.getX(l)) + y, ChunkSectionPos.getWorldCoord(ChunkSectionPos.getY(l)) - 1, ChunkSectionPos.getWorldCoord(ChunkSectionPos.getZ(l)) + z);
                           lightProvider.updateLevel(aa, ab, lightProvider.getPropagatedLevel(aa, ab, 0), true);
                        }
                     }
                  } else {
                     for(j = 0; j < 16; ++j) {
                        for(k = 0; k < 16; ++k) {
                           long ae = BlockPos.asLong(ChunkSectionPos.getWorldCoord(ChunkSectionPos.getX(l)) + j, ChunkSectionPos.getWorldCoord(ChunkSectionPos.getY(l)) + 16 - 1, ChunkSectionPos.getWorldCoord(ChunkSectionPos.getZ(l)) + k);
                           lightProvider.updateLevel(Long.MAX_VALUE, ae, 0, true);
                        }
                     }
                  }
               }
            }
         }

         this.pendingSkylightUpdates.clear();
         if (!this.field_15816.isEmpty()) {
            var4 = this.field_15816.iterator();

            label90:
            while(true) {
               do {
                  do {
                     if (!var4.hasNext()) {
                        break label90;
                     }

                     l = (Long)var4.next();
                  } while(!this.field_15820.remove(l));
               } while(!this.hasLight(l));

               for(ag = 0; ag < 16; ++ag) {
                  for(j = 0; j < 16; ++j) {
                     long ai = BlockPos.asLong(ChunkSectionPos.getWorldCoord(ChunkSectionPos.getX(l)) + ag, ChunkSectionPos.getWorldCoord(ChunkSectionPos.getY(l)) + 16 - 1, ChunkSectionPos.getWorldCoord(ChunkSectionPos.getZ(l)) + j);
                     lightProvider.updateLevel(Long.MAX_VALUE, ai, 15, false);
                  }
               }
            }
         }

         this.field_15816.clear();
         this.hasSkyLightUpdates = false;
      }
   }

   protected boolean isAboveMinimumHeight(int blockY) {
      return blockY >= ((SkyLightStorage.Data)this.lightArrays).defaultTopArraySectionY;
   }

   protected boolean method_15565(long l) {
      int i = BlockPos.unpackLongY(l);
      if ((i & 15) != 15) {
         return false;
      } else {
         long m = ChunkSectionPos.fromGlobalPos(l);
         long n = ChunkSectionPos.withZeroZ(m);
         if (!this.lightEnabled.contains(n)) {
            return false;
         } else {
            int j = ((SkyLightStorage.Data)this.lightArrays).topArraySectionY.get(n);
            return ChunkSectionPos.getWorldCoord(j) == i + 16;
         }
      }
   }

   protected boolean isAboveTopmostLightArray(long pos) {
      long l = ChunkSectionPos.withZeroZ(pos);
      int i = ((SkyLightStorage.Data)this.lightArrays).topArraySectionY.get(l);
      return i == ((SkyLightStorage.Data)this.lightArrays).defaultTopArraySectionY || ChunkSectionPos.getY(pos) >= i;
   }

   protected boolean isLightEnabled(long sectionPos) {
      long l = ChunkSectionPos.withZeroZ(sectionPos);
      return this.lightEnabled.contains(l);
   }

   static {
      LIGHT_REDUCTION_DIRECTIONS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
   }

   public static final class Data extends ChunkToNibbleArrayMap<SkyLightStorage.Data> {
      private int defaultTopArraySectionY;
      private final Long2IntOpenHashMap topArraySectionY;

      public Data(Long2ObjectOpenHashMap<ChunkNibbleArray> long2ObjectOpenHashMap, Long2IntOpenHashMap long2IntOpenHashMap, int i) {
         super(long2ObjectOpenHashMap);
         this.topArraySectionY = long2IntOpenHashMap;
         long2IntOpenHashMap.defaultReturnValue(i);
         this.defaultTopArraySectionY = i;
      }

      public SkyLightStorage.Data copy() {
         return new SkyLightStorage.Data(this.arrays.clone(), this.topArraySectionY.clone(), this.defaultTopArraySectionY);
      }
   }
}
