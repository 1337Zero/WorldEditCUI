package net.minecraft.block.sapling;

import java.util.Iterator;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.BranchedTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public abstract class SaplingGenerator {
   @Nullable
   protected abstract ConfiguredFeature<BranchedTreeFeatureConfig, ?> createTreeFeature(Random random, boolean bl);

   public boolean generate(IWorld iWorld, ChunkGenerator<?> chunkGenerator, BlockPos blockPos, BlockState blockState, Random random) {
      ConfiguredFeature<BranchedTreeFeatureConfig, ?> configuredFeature = this.createTreeFeature(random, this.method_24282(iWorld, blockPos));
      if (configuredFeature == null) {
         return false;
      } else {
         iWorld.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 4);
         ((BranchedTreeFeatureConfig)configuredFeature.config).method_23916();
         if (configuredFeature.generate(iWorld, chunkGenerator, random, blockPos)) {
            return true;
         } else {
            iWorld.setBlockState(blockPos, blockState, 4);
            return false;
         }
      }
   }

   private boolean method_24282(IWorld iWorld, BlockPos blockPos) {
      Iterator var3 = BlockPos.Mutable.iterate(blockPos.down().north(2).west(2), blockPos.up().south(2).east(2)).iterator();

      BlockPos blockPos2;
      do {
         if (!var3.hasNext()) {
            return false;
         }

         blockPos2 = (BlockPos)var3.next();
      } while(!iWorld.getBlockState(blockPos2).matches(BlockTags.FLOWERS));

      return true;
   }
}
