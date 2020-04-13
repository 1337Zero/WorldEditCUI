package net.minecraft.world.gen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ModifiableTestableWorld;

public class MegaJungleTreeFeature extends MegaTreeFeature<MegaTreeFeatureConfig> {
   public MegaJungleTreeFeature(Function<Dynamic<?>, ? extends MegaTreeFeatureConfig> function) {
      super(function);
   }

   public boolean generate(ModifiableTestableWorld modifiableTestableWorld, Random random, BlockPos blockPos, Set<BlockPos> set, Set<BlockPos> set2, BlockBox blockBox, MegaTreeFeatureConfig megaTreeFeatureConfig) {
      int i = this.getHeight(random, megaTreeFeatureConfig);
      if (!this.checkTreeFitsAndReplaceGround(modifiableTestableWorld, blockPos, i)) {
         return false;
      } else {
         this.makeLeaves(modifiableTestableWorld, random, blockPos.up(i), 2, set2, blockBox, megaTreeFeatureConfig);

         for(int j = blockPos.getY() + i - 2 - random.nextInt(4); j > blockPos.getY() + i / 2; j -= 2 + random.nextInt(4)) {
            float f = random.nextFloat() * 6.2831855F;
            int k = blockPos.getX() + (int)(0.5F + MathHelper.cos(f) * 4.0F);
            int l = blockPos.getZ() + (int)(0.5F + MathHelper.sin(f) * 4.0F);

            int n;
            for(n = 0; n < 5; ++n) {
               k = blockPos.getX() + (int)(1.5F + MathHelper.cos(f) * (float)n);
               l = blockPos.getZ() + (int)(1.5F + MathHelper.sin(f) * (float)n);
               BlockPos blockPos2 = new BlockPos(k, j - 3 + n / 2, l);
               this.setLogBlockState(modifiableTestableWorld, random, blockPos2, set, blockBox, megaTreeFeatureConfig);
            }

            n = 1 + random.nextInt(2);
            int o = j;

            for(int p = j - n; p <= o; ++p) {
               int q = p - o;
               this.makeRoundLeafLayer(modifiableTestableWorld, random, new BlockPos(k, p, l), 1 - q, set2, blockBox, megaTreeFeatureConfig);
            }
         }

         this.method_23400(modifiableTestableWorld, random, blockPos, i, set, blockBox, megaTreeFeatureConfig);
         return true;
      }
   }

   private void makeLeaves(ModifiableTestableWorld world, Random random, BlockPos blockPos, int i, Set<BlockPos> set, BlockBox blockBox, TreeFeatureConfig treeFeatureConfig) {
      int j = true;

      for(int k = -2; k <= 0; ++k) {
         this.makeSquaredLeafLayer(world, random, blockPos.up(k), i + 1 - k, set, blockBox, treeFeatureConfig);
      }

   }
}
