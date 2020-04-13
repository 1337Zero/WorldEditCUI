package net.minecraft.world.gen.foliage;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ModifiableTestableWorld;
import net.minecraft.world.gen.feature.BranchedTreeFeatureConfig;

public class AcaciaFoliagePlacer extends FoliagePlacer {
   public AcaciaFoliagePlacer(int i, int j) {
      super(i, j, FoliagePlacerType.ACACIA_FOLIAGE_PLACER);
   }

   public <T> AcaciaFoliagePlacer(Dynamic<T> dynamic) {
      this(dynamic.get("radius").asInt(0), dynamic.get("radius_random").asInt(0));
   }

   public void generate(ModifiableTestableWorld world, Random random, BranchedTreeFeatureConfig config, int i, int j, int k, BlockPos pos, Set<BlockPos> positions) {
      config.foliagePlacer.generate(world, random, config, i, pos, 0, k, positions);
      config.foliagePlacer.generate(world, random, config, i, pos, 1, 1, positions);
      BlockPos blockPos = pos.up();

      int n;
      for(n = -1; n <= 1; ++n) {
         for(int m = -1; m <= 1; ++m) {
            this.method_23450(world, random, blockPos.add(n, 0, m), config, positions);
         }
      }

      for(n = 2; n <= k - 1; ++n) {
         this.method_23450(world, random, blockPos.east(n), config, positions);
         this.method_23450(world, random, blockPos.west(n), config, positions);
         this.method_23450(world, random, blockPos.south(n), config, positions);
         this.method_23450(world, random, blockPos.north(n), config, positions);
      }

   }

   public int getRadius(Random random, int i, int j, BranchedTreeFeatureConfig config) {
      return this.radius + random.nextInt(this.randomRadius + 1);
   }

   protected boolean method_23451(Random random, int i, int j, int k, int l, int m) {
      return Math.abs(j) == m && Math.abs(l) == m && m > 0;
   }

   public int method_23447(int i, int j, int k, int l) {
      return l == 0 ? 0 : 2;
   }
}
