package net.minecraft.world.gen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;

public class RandomFeature extends Feature<RandomFeatureConfig> {
   public RandomFeature(Function<Dynamic<?>, ? extends RandomFeatureConfig> configFactory) {
      super(configFactory);
   }

   public boolean generate(IWorld iWorld, ChunkGenerator<? extends ChunkGeneratorConfig> chunkGenerator, Random random, BlockPos blockPos, RandomFeatureConfig randomFeatureConfig) {
      Iterator var6 = randomFeatureConfig.features.iterator();

      RandomFeatureEntry randomFeatureEntry;
      do {
         if (!var6.hasNext()) {
            return randomFeatureConfig.defaultFeature.generate(iWorld, chunkGenerator, random, blockPos);
         }

         randomFeatureEntry = (RandomFeatureEntry)var6.next();
      } while(random.nextFloat() >= randomFeatureEntry.chance);

      return randomFeatureEntry.generate(iWorld, chunkGenerator, random, blockPos);
   }
}
