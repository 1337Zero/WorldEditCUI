package net.minecraft.world.gen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.block.BlockState;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;

public class RandomPatchFeature extends Feature<RandomPatchFeatureConfig> {
   public RandomPatchFeature(Function<Dynamic<?>, ? extends RandomPatchFeatureConfig> function) {
      super(function);
   }

   public boolean generate(IWorld iWorld, ChunkGenerator<? extends ChunkGeneratorConfig> chunkGenerator, Random random, BlockPos blockPos, RandomPatchFeatureConfig randomPatchFeatureConfig) {
      BlockState blockState = randomPatchFeatureConfig.stateProvider.getBlockState(random, blockPos);
      BlockPos blockPos3;
      if (randomPatchFeatureConfig.project) {
         blockPos3 = iWorld.getTopPosition(Heightmap.Type.WORLD_SURFACE_WG, blockPos);
      } else {
         blockPos3 = blockPos;
      }

      int i = 0;
      BlockPos.Mutable mutable = new BlockPos.Mutable();

      for(int j = 0; j < randomPatchFeatureConfig.tries; ++j) {
         mutable.set((Vec3i)blockPos3).setOffset(random.nextInt(randomPatchFeatureConfig.spreadX + 1) - random.nextInt(randomPatchFeatureConfig.spreadX + 1), random.nextInt(randomPatchFeatureConfig.spreadY + 1) - random.nextInt(randomPatchFeatureConfig.spreadY + 1), random.nextInt(randomPatchFeatureConfig.spreadZ + 1) - random.nextInt(randomPatchFeatureConfig.spreadZ + 1));
         BlockPos blockPos4 = mutable.down();
         BlockState blockState2 = iWorld.getBlockState(blockPos4);
         if ((iWorld.isAir(mutable) || randomPatchFeatureConfig.canReplace && iWorld.getBlockState(mutable).getMaterial().isReplaceable()) && blockState.canPlaceAt(iWorld, mutable) && (randomPatchFeatureConfig.whitelist.isEmpty() || randomPatchFeatureConfig.whitelist.contains(blockState2.getBlock())) && !randomPatchFeatureConfig.blacklist.contains(blockState2) && (!randomPatchFeatureConfig.needsWater || iWorld.getFluidState(blockPos4.west()).matches(FluidTags.WATER) || iWorld.getFluidState(blockPos4.east()).matches(FluidTags.WATER) || iWorld.getFluidState(blockPos4.north()).matches(FluidTags.WATER) || iWorld.getFluidState(blockPos4.south()).matches(FluidTags.WATER))) {
            randomPatchFeatureConfig.blockPlacer.method_23403(iWorld, mutable, blockState, random);
            ++i;
         }
      }

      return i > 0;
   }
}
