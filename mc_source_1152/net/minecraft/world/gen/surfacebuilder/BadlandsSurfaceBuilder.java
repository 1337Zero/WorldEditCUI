package net.minecraft.world.gen.surfacebuilder;

import com.mojang.datafixers.Dynamic;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.OctaveSimplexNoiseSampler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkRandom;

public class BadlandsSurfaceBuilder extends SurfaceBuilder<TernarySurfaceConfig> {
   private static final BlockState WHITE_TERACOTTA;
   private static final BlockState ORANGE_TERRACOTTA;
   private static final BlockState TERACOTTA;
   private static final BlockState YELLOW_TERACOTTA;
   private static final BlockState BROWN_TERACOTTA;
   private static final BlockState RED_TERACOTTA;
   private static final BlockState LIGHT_GRAY_TERACOTTA;
   protected BlockState[] layerBlocks;
   protected long seed;
   protected OctaveSimplexNoiseSampler heightCutoffNoise;
   protected OctaveSimplexNoiseSampler heightNoise;
   protected OctaveSimplexNoiseSampler layerNoise;

   public BadlandsSurfaceBuilder(Function<Dynamic<?>, ? extends TernarySurfaceConfig> function) {
      super(function);
   }

   public void generate(Random random, Chunk chunk, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, long m, TernarySurfaceConfig ternarySurfaceConfig) {
      int n = i & 15;
      int o = j & 15;
      BlockState blockState3 = WHITE_TERACOTTA;
      BlockState blockState4 = biome.getSurfaceConfig().getUnderMaterial();
      int p = (int)(d / 3.0D + 3.0D + random.nextDouble() * 0.25D);
      boolean bl = Math.cos(d / 3.0D * 3.141592653589793D) > 0.0D;
      int q = -1;
      boolean bl2 = false;
      int r = 0;
      BlockPos.Mutable mutable = new BlockPos.Mutable();

      for(int s = k; s >= 0; --s) {
         if (r < 15) {
            mutable.set(n, s, o);
            BlockState blockState5 = chunk.getBlockState(mutable);
            if (blockState5.isAir()) {
               q = -1;
            } else if (blockState5.getBlock() == blockState.getBlock()) {
               if (q == -1) {
                  bl2 = false;
                  if (p <= 0) {
                     blockState3 = Blocks.AIR.getDefaultState();
                     blockState4 = blockState;
                  } else if (s >= l - 4 && s <= l + 1) {
                     blockState3 = WHITE_TERACOTTA;
                     blockState4 = biome.getSurfaceConfig().getUnderMaterial();
                  }

                  if (s < l && (blockState3 == null || blockState3.isAir())) {
                     blockState3 = blockState2;
                  }

                  q = p + Math.max(0, s - l);
                  if (s >= l - 1) {
                     if (s > l + 3 + p) {
                        BlockState blockState8;
                        if (s >= 64 && s <= 127) {
                           if (bl) {
                              blockState8 = TERACOTTA;
                           } else {
                              blockState8 = this.calculateLayerBlockState(i, s, j);
                           }
                        } else {
                           blockState8 = ORANGE_TERRACOTTA;
                        }

                        chunk.setBlockState(mutable, blockState8, false);
                     } else {
                        chunk.setBlockState(mutable, biome.getSurfaceConfig().getTopMaterial(), false);
                        bl2 = true;
                     }
                  } else {
                     chunk.setBlockState(mutable, blockState4, false);
                     Block block = blockState4.getBlock();
                     if (block == Blocks.WHITE_TERRACOTTA || block == Blocks.ORANGE_TERRACOTTA || block == Blocks.MAGENTA_TERRACOTTA || block == Blocks.LIGHT_BLUE_TERRACOTTA || block == Blocks.YELLOW_TERRACOTTA || block == Blocks.LIME_TERRACOTTA || block == Blocks.PINK_TERRACOTTA || block == Blocks.GRAY_TERRACOTTA || block == Blocks.LIGHT_GRAY_TERRACOTTA || block == Blocks.CYAN_TERRACOTTA || block == Blocks.PURPLE_TERRACOTTA || block == Blocks.BLUE_TERRACOTTA || block == Blocks.BROWN_TERRACOTTA || block == Blocks.GREEN_TERRACOTTA || block == Blocks.RED_TERRACOTTA || block == Blocks.BLACK_TERRACOTTA) {
                        chunk.setBlockState(mutable, ORANGE_TERRACOTTA, false);
                     }
                  }
               } else if (q > 0) {
                  --q;
                  if (bl2) {
                     chunk.setBlockState(mutable, ORANGE_TERRACOTTA, false);
                  } else {
                     chunk.setBlockState(mutable, this.calculateLayerBlockState(i, s, j), false);
                  }
               }

               ++r;
            }
         }
      }

   }

   public void initSeed(long seed) {
      if (this.seed != seed || this.layerBlocks == null) {
         this.initLayerBlocks(seed);
      }

      if (this.seed != seed || this.heightCutoffNoise == null || this.heightNoise == null) {
         ChunkRandom chunkRandom = new ChunkRandom(seed);
         this.heightCutoffNoise = new OctaveSimplexNoiseSampler(chunkRandom, 3, 0);
         this.heightNoise = new OctaveSimplexNoiseSampler(chunkRandom, 0, 0);
      }

      this.seed = seed;
   }

   protected void initLayerBlocks(long seed) {
      this.layerBlocks = new BlockState[64];
      Arrays.fill(this.layerBlocks, TERACOTTA);
      ChunkRandom chunkRandom = new ChunkRandom(seed);
      this.layerNoise = new OctaveSimplexNoiseSampler(chunkRandom, 0, 0);

      int j;
      for(j = 0; j < 64; ++j) {
         j += chunkRandom.nextInt(5) + 1;
         if (j < 64) {
            this.layerBlocks[j] = ORANGE_TERRACOTTA;
         }
      }

      j = chunkRandom.nextInt(4) + 2;

      int o;
      int t;
      int y;
      int z;
      for(o = 0; o < j; ++o) {
         t = chunkRandom.nextInt(3) + 1;
         y = chunkRandom.nextInt(64);

         for(z = 0; y + z < 64 && z < t; ++z) {
            this.layerBlocks[y + z] = YELLOW_TERACOTTA;
         }
      }

      o = chunkRandom.nextInt(4) + 2;

      int w;
      for(t = 0; t < o; ++t) {
         y = chunkRandom.nextInt(3) + 2;
         z = chunkRandom.nextInt(64);

         for(w = 0; z + w < 64 && w < y; ++w) {
            this.layerBlocks[z + w] = BROWN_TERACOTTA;
         }
      }

      t = chunkRandom.nextInt(4) + 2;

      for(y = 0; y < t; ++y) {
         z = chunkRandom.nextInt(3) + 1;
         w = chunkRandom.nextInt(64);

         for(int x = 0; w + x < 64 && x < z; ++x) {
            this.layerBlocks[w + x] = RED_TERACOTTA;
         }
      }

      y = chunkRandom.nextInt(3) + 3;
      z = 0;

      for(w = 0; w < y; ++w) {
         int ab = true;
         z += chunkRandom.nextInt(16) + 4;

         for(int ac = 0; z + ac < 64 && ac < 1; ++ac) {
            this.layerBlocks[z + ac] = WHITE_TERACOTTA;
            if (z + ac > 1 && chunkRandom.nextBoolean()) {
               this.layerBlocks[z + ac - 1] = LIGHT_GRAY_TERACOTTA;
            }

            if (z + ac < 63 && chunkRandom.nextBoolean()) {
               this.layerBlocks[z + ac + 1] = LIGHT_GRAY_TERACOTTA;
            }
         }
      }

   }

   protected BlockState calculateLayerBlockState(int x, int y, int z) {
      int i = (int)Math.round(this.layerNoise.sample((double)x / 512.0D, (double)z / 512.0D, false) * 2.0D);
      return this.layerBlocks[(y + i + 64) % 64];
   }

   static {
      WHITE_TERACOTTA = Blocks.WHITE_TERRACOTTA.getDefaultState();
      ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.getDefaultState();
      TERACOTTA = Blocks.TERRACOTTA.getDefaultState();
      YELLOW_TERACOTTA = Blocks.YELLOW_TERRACOTTA.getDefaultState();
      BROWN_TERACOTTA = Blocks.BROWN_TERRACOTTA.getDefaultState();
      RED_TERACOTTA = Blocks.RED_TERRACOTTA.getDefaultState();
      LIGHT_GRAY_TERACOTTA = Blocks.LIGHT_GRAY_TERRACOTTA.getDefaultState();
   }
}
