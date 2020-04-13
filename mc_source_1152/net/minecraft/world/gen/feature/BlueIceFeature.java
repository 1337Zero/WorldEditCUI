package net.minecraft.world.gen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;

public class BlueIceFeature extends Feature<DefaultFeatureConfig> {
   public BlueIceFeature(Function<Dynamic<?>, ? extends DefaultFeatureConfig> configFactory) {
      super(configFactory);
   }

   public boolean generate(IWorld iWorld, ChunkGenerator<? extends ChunkGeneratorConfig> chunkGenerator, Random random, BlockPos blockPos, DefaultFeatureConfig defaultFeatureConfig) {
      if (blockPos.getY() > iWorld.getSeaLevel() - 1) {
         return false;
      } else if (iWorld.getBlockState(blockPos).getBlock() != Blocks.WATER && iWorld.getBlockState(blockPos.down()).getBlock() != Blocks.WATER) {
         return false;
      } else {
         boolean bl = false;
         Direction[] var7 = Direction.values();
         int j = var7.length;

         int k;
         for(k = 0; k < j; ++k) {
            Direction direction = var7[k];
            if (direction != Direction.DOWN && iWorld.getBlockState(blockPos.offset(direction)).getBlock() == Blocks.PACKED_ICE) {
               bl = true;
               break;
            }
         }

         if (!bl) {
            return false;
         } else {
            iWorld.setBlockState(blockPos, Blocks.BLUE_ICE.getDefaultState(), 2);

            for(int i = 0; i < 200; ++i) {
               j = random.nextInt(5) - random.nextInt(6);
               k = 3;
               if (j < 2) {
                  k += j / 2;
               }

               if (k >= 1) {
                  BlockPos blockPos2 = blockPos.add(random.nextInt(k) - random.nextInt(k), j, random.nextInt(k) - random.nextInt(k));
                  BlockState blockState = iWorld.getBlockState(blockPos2);
                  Block block = blockState.getBlock();
                  if (blockState.getMaterial() == Material.AIR || block == Blocks.WATER || block == Blocks.PACKED_ICE || block == Blocks.ICE) {
                     Direction[] var13 = Direction.values();
                     int var14 = var13.length;

                     for(int var15 = 0; var15 < var14; ++var15) {
                        Direction direction2 = var13[var15];
                        Block block2 = iWorld.getBlockState(blockPos2.offset(direction2)).getBlock();
                        if (block2 == Blocks.BLUE_ICE) {
                           iWorld.setBlockState(blockPos2, Blocks.BLUE_ICE.getDefaultState(), 2);
                           break;
                        }
                     }
                  }
               }
            }

            return true;
         }
      }
   }
}
