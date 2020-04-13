package net.minecraft.world.gen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ModifiableTestableWorld;
import net.minecraft.world.TestableWorld;

public class DarkOakTreeFeature extends AbstractTreeFeature<MegaTreeFeatureConfig> {
   public DarkOakTreeFeature(Function<Dynamic<?>, ? extends MegaTreeFeatureConfig> configFactory) {
      super(configFactory);
   }

   public boolean generate(ModifiableTestableWorld modifiableTestableWorld, Random random, BlockPos blockPos, Set<BlockPos> set, Set<BlockPos> set2, BlockBox blockBox, MegaTreeFeatureConfig megaTreeFeatureConfig) {
      int i = random.nextInt(3) + random.nextInt(2) + megaTreeFeatureConfig.baseHeight;
      int j = blockPos.getX();
      int k = blockPos.getY();
      int l = blockPos.getZ();
      if (k >= 1 && k + i + 1 < 256) {
         BlockPos blockPos2 = blockPos.down();
         if (!isNaturalDirtOrGrass(modifiableTestableWorld, blockPos2)) {
            return false;
         } else if (!this.doesTreeFit(modifiableTestableWorld, blockPos, i)) {
            return false;
         } else {
            this.setToDirt(modifiableTestableWorld, blockPos2);
            this.setToDirt(modifiableTestableWorld, blockPos2.east());
            this.setToDirt(modifiableTestableWorld, blockPos2.south());
            this.setToDirt(modifiableTestableWorld, blockPos2.south().east());
            Direction direction = Direction.Type.HORIZONTAL.random(random);
            int m = i - random.nextInt(4);
            int n = 2 - random.nextInt(3);
            int o = j;
            int p = l;
            int q = k + i - 1;

            int y;
            int z;
            for(y = 0; y < i; ++y) {
               if (y >= m && n > 0) {
                  o += direction.getOffsetX();
                  p += direction.getOffsetZ();
                  --n;
               }

               z = k + y;
               BlockPos blockPos3 = new BlockPos(o, z, p);
               if (isAirOrLeaves(modifiableTestableWorld, blockPos3)) {
                  this.setLogBlockState(modifiableTestableWorld, random, blockPos3, set, blockBox, megaTreeFeatureConfig);
                  this.setLogBlockState(modifiableTestableWorld, random, blockPos3.east(), set, blockBox, megaTreeFeatureConfig);
                  this.setLogBlockState(modifiableTestableWorld, random, blockPos3.south(), set, blockBox, megaTreeFeatureConfig);
                  this.setLogBlockState(modifiableTestableWorld, random, blockPos3.east().south(), set, blockBox, megaTreeFeatureConfig);
               }
            }

            for(y = -2; y <= 0; ++y) {
               for(z = -2; z <= 0; ++z) {
                  int v = -1;
                  this.setLeavesBlockState(modifiableTestableWorld, random, new BlockPos(o + y, q + v, p + z), set2, blockBox, megaTreeFeatureConfig);
                  this.setLeavesBlockState(modifiableTestableWorld, random, new BlockPos(1 + o - y, q + v, p + z), set2, blockBox, megaTreeFeatureConfig);
                  this.setLeavesBlockState(modifiableTestableWorld, random, new BlockPos(o + y, q + v, 1 + p - z), set2, blockBox, megaTreeFeatureConfig);
                  this.setLeavesBlockState(modifiableTestableWorld, random, new BlockPos(1 + o - y, q + v, 1 + p - z), set2, blockBox, megaTreeFeatureConfig);
                  if ((y > -2 || z > -1) && (y != -1 || z != -2)) {
                     int v = 1;
                     this.setLeavesBlockState(modifiableTestableWorld, random, new BlockPos(o + y, q + v, p + z), set2, blockBox, megaTreeFeatureConfig);
                     this.setLeavesBlockState(modifiableTestableWorld, random, new BlockPos(1 + o - y, q + v, p + z), set2, blockBox, megaTreeFeatureConfig);
                     this.setLeavesBlockState(modifiableTestableWorld, random, new BlockPos(o + y, q + v, 1 + p - z), set2, blockBox, megaTreeFeatureConfig);
                     this.setLeavesBlockState(modifiableTestableWorld, random, new BlockPos(1 + o - y, q + v, 1 + p - z), set2, blockBox, megaTreeFeatureConfig);
                  }
               }
            }

            if (random.nextBoolean()) {
               this.setLeavesBlockState(modifiableTestableWorld, random, new BlockPos(o, q + 2, p), set2, blockBox, megaTreeFeatureConfig);
               this.setLeavesBlockState(modifiableTestableWorld, random, new BlockPos(o + 1, q + 2, p), set2, blockBox, megaTreeFeatureConfig);
               this.setLeavesBlockState(modifiableTestableWorld, random, new BlockPos(o + 1, q + 2, p + 1), set2, blockBox, megaTreeFeatureConfig);
               this.setLeavesBlockState(modifiableTestableWorld, random, new BlockPos(o, q + 2, p + 1), set2, blockBox, megaTreeFeatureConfig);
            }

            for(y = -3; y <= 4; ++y) {
               for(z = -3; z <= 4; ++z) {
                  if ((y != -3 || z != -3) && (y != -3 || z != 4) && (y != 4 || z != -3) && (y != 4 || z != 4) && (Math.abs(y) < 3 || Math.abs(z) < 3)) {
                     this.setLeavesBlockState(modifiableTestableWorld, random, new BlockPos(o + y, q, p + z), set2, blockBox, megaTreeFeatureConfig);
                  }
               }
            }

            for(y = -1; y <= 2; ++y) {
               for(z = -1; z <= 2; ++z) {
                  if ((y < 0 || y > 1 || z < 0 || z > 1) && random.nextInt(3) <= 0) {
                     int aa = random.nextInt(3) + 2;

                     int ae;
                     for(ae = 0; ae < aa; ++ae) {
                        this.setLogBlockState(modifiableTestableWorld, random, new BlockPos(j + y, q - ae - 1, l + z), set, blockBox, megaTreeFeatureConfig);
                     }

                     int af;
                     for(ae = -1; ae <= 1; ++ae) {
                        for(af = -1; af <= 1; ++af) {
                           this.setLeavesBlockState(modifiableTestableWorld, random, new BlockPos(o + y + ae, q, p + z + af), set2, blockBox, megaTreeFeatureConfig);
                        }
                     }

                     for(ae = -2; ae <= 2; ++ae) {
                        for(af = -2; af <= 2; ++af) {
                           if (Math.abs(ae) != 2 || Math.abs(af) != 2) {
                              this.setLeavesBlockState(modifiableTestableWorld, random, new BlockPos(o + y + ae, q - 1, p + z + af), set2, blockBox, megaTreeFeatureConfig);
                           }
                        }
                     }
                  }
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   private boolean doesTreeFit(TestableWorld world, BlockPos pos, int treeHeight) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      BlockPos.Mutable mutable = new BlockPos.Mutable();

      for(int l = 0; l <= treeHeight + 1; ++l) {
         int m = 1;
         if (l == 0) {
            m = 0;
         }

         if (l >= treeHeight - 1) {
            m = 2;
         }

         for(int n = -m; n <= m; ++n) {
            for(int o = -m; o <= m; ++o) {
               if (!canTreeReplace(world, mutable.set(i + n, j + l, k + o))) {
                  return false;
               }
            }
         }
      }

      return true;
   }
}
