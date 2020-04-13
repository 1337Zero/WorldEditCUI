package net.minecraft.world.gen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;

public class ForestRockFeature extends Feature<BoulderFeatureConfig> {
   public ForestRockFeature(Function<Dynamic<?>, ? extends BoulderFeatureConfig> configFactory) {
      super(configFactory);
   }

   public boolean generate(IWorld iWorld, ChunkGenerator<? extends ChunkGeneratorConfig> chunkGenerator, Random random, BlockPos blockPos, BoulderFeatureConfig boulderFeatureConfig) {
      while(true) {
         label48: {
            if (blockPos.getY() > 3) {
               if (iWorld.isAir(blockPos.down())) {
                  break label48;
               }

               Block block = iWorld.getBlockState(blockPos.down()).getBlock();
               if (!isDirt(block) && !isStone(block)) {
                  break label48;
               }
            }

            if (blockPos.getY() <= 3) {
               return false;
            }

            int i = boulderFeatureConfig.startRadius;

            for(int j = 0; i >= 0 && j < 3; ++j) {
               int k = i + random.nextInt(2);
               int l = i + random.nextInt(2);
               int m = i + random.nextInt(2);
               float f = (float)(k + l + m) * 0.333F + 0.5F;
               Iterator var12 = BlockPos.iterate(blockPos.add(-k, -l, -m), blockPos.add(k, l, m)).iterator();

               while(var12.hasNext()) {
                  BlockPos blockPos2 = (BlockPos)var12.next();
                  if (blockPos2.getSquaredDistance(blockPos) <= (double)(f * f)) {
                     iWorld.setBlockState(blockPos2, boulderFeatureConfig.state, 4);
                  }
               }

               blockPos = blockPos.add(-(i + 1) + random.nextInt(2 + i * 2), 0 - random.nextInt(2), -(i + 1) + random.nextInt(2 + i * 2));
            }

            return true;
         }

         blockPos = blockPos.down();
      }
   }
}
