package net.minecraft.world.gen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ModifiableTestableWorld;

public abstract class BranchedTreeFeature<T extends BranchedTreeFeatureConfig> extends AbstractTreeFeature<T> {
   public BranchedTreeFeature(Function<Dynamic<?>, ? extends T> function) {
      super(function);
   }

   protected void generate(ModifiableTestableWorld world, Random random, int height, BlockPos pos, int trunkTopOffset, Set<BlockPos> logPositions, BlockBox blockBox, BranchedTreeFeatureConfig config) {
      for(int i = 0; i < height - trunkTopOffset; ++i) {
         this.setLogBlockState(world, random, pos.up(i), logPositions, blockBox, config);
      }

   }

   public Optional<BlockPos> findPositionToGenerate(ModifiableTestableWorld world, int height, int i, int j, BlockPos pos, BranchedTreeFeatureConfig config) {
      BlockPos blockPos2;
      int m;
      int n;
      if (!config.field_21593) {
         m = world.getTopPosition(Heightmap.Type.OCEAN_FLOOR, pos).getY();
         n = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, pos).getY();
         blockPos2 = new BlockPos(pos.getX(), m, pos.getZ());
         if (n - m > config.maxWaterDepth) {
            return Optional.empty();
         }
      } else {
         blockPos2 = pos;
      }

      if (blockPos2.getY() >= 1 && blockPos2.getY() + height + 1 <= 256) {
         for(m = 0; m <= height + 1; ++m) {
            n = config.foliagePlacer.method_23447(i, height, j, m);
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            for(int o = -n; o <= n; ++o) {
               int p = -n;

               while(p <= n) {
                  if (m + blockPos2.getY() >= 0 && m + blockPos2.getY() < 256) {
                     mutable.set(o + blockPos2.getX(), m + blockPos2.getY(), p + blockPos2.getZ());
                     if (canTreeReplace(world, mutable) && (config.noVines || !isLeaves(world, mutable))) {
                        ++p;
                        continue;
                     }

                     return Optional.empty();
                  }

                  return Optional.empty();
               }
            }
         }

         if (isDirtOrGrass(world, blockPos2.down()) && blockPos2.getY() < 256 - height - 1) {
            return Optional.of(blockPos2);
         } else {
            return Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }
}
