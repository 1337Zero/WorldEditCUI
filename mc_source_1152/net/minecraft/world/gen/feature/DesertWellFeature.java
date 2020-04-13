package net.minecraft.world.gen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;

public class DesertWellFeature extends Feature<DefaultFeatureConfig> {
   private static final BlockStatePredicate CAN_GENERATE;
   private final BlockState slab;
   private final BlockState wall;
   private final BlockState fluidInside;

   public DesertWellFeature(Function<Dynamic<?>, ? extends DefaultFeatureConfig> configFactory) {
      super(configFactory);
      this.slab = Blocks.SANDSTONE_SLAB.getDefaultState();
      this.wall = Blocks.SANDSTONE.getDefaultState();
      this.fluidInside = Blocks.WATER.getDefaultState();
   }

   public boolean generate(IWorld iWorld, ChunkGenerator<? extends ChunkGeneratorConfig> chunkGenerator, Random random, BlockPos blockPos, DefaultFeatureConfig defaultFeatureConfig) {
      for(blockPos = blockPos.up(); iWorld.isAir(blockPos) && blockPos.getY() > 2; blockPos = blockPos.down()) {
      }

      if (!CAN_GENERATE.test(iWorld.getBlockState(blockPos))) {
         return false;
      } else {
         int p;
         int q;
         for(p = -2; p <= 2; ++p) {
            for(q = -2; q <= 2; ++q) {
               if (iWorld.isAir(blockPos.add(p, -1, q)) && iWorld.isAir(blockPos.add(p, -2, q))) {
                  return false;
               }
            }
         }

         for(p = -1; p <= 0; ++p) {
            for(q = -2; q <= 2; ++q) {
               for(int m = -2; m <= 2; ++m) {
                  iWorld.setBlockState(blockPos.add(q, p, m), this.wall, 2);
               }
            }
         }

         iWorld.setBlockState(blockPos, this.fluidInside, 2);
         Iterator var9 = Direction.Type.HORIZONTAL.iterator();

         while(var9.hasNext()) {
            Direction direction = (Direction)var9.next();
            iWorld.setBlockState(blockPos.offset(direction), this.fluidInside, 2);
         }

         for(p = -2; p <= 2; ++p) {
            for(q = -2; q <= 2; ++q) {
               if (p == -2 || p == 2 || q == -2 || q == 2) {
                  iWorld.setBlockState(blockPos.add(p, 1, q), this.wall, 2);
               }
            }
         }

         iWorld.setBlockState(blockPos.add(2, 1, 0), this.slab, 2);
         iWorld.setBlockState(blockPos.add(-2, 1, 0), this.slab, 2);
         iWorld.setBlockState(blockPos.add(0, 1, 2), this.slab, 2);
         iWorld.setBlockState(blockPos.add(0, 1, -2), this.slab, 2);

         for(p = -1; p <= 1; ++p) {
            for(q = -1; q <= 1; ++q) {
               if (p == 0 && q == 0) {
                  iWorld.setBlockState(blockPos.add(p, 4, q), this.wall, 2);
               } else {
                  iWorld.setBlockState(blockPos.add(p, 4, q), this.slab, 2);
               }
            }
         }

         for(p = 1; p <= 3; ++p) {
            iWorld.setBlockState(blockPos.add(-1, p, -1), this.wall, 2);
            iWorld.setBlockState(blockPos.add(-1, p, 1), this.wall, 2);
            iWorld.setBlockState(blockPos.add(1, p, -1), this.wall, 2);
            iWorld.setBlockState(blockPos.add(1, p, 1), this.wall, 2);
         }

         return true;
      }
   }

   static {
      CAN_GENERATE = BlockStatePredicate.forBlock(Blocks.SAND);
   }
}
