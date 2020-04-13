package net.minecraft.world.gen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.Dynamic;
import java.util.BitSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ProbabilityConfig;

public class NetherCaveCarver extends CaveCarver {
   public NetherCaveCarver(Function<Dynamic<?>, ? extends ProbabilityConfig> function) {
      super(function, 128);
      this.alwaysCarvableBlocks = ImmutableSet.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.DIRT, Blocks.COARSE_DIRT, new Block[]{Blocks.PODZOL, Blocks.GRASS_BLOCK, Blocks.NETHERRACK});
      this.carvableFluids = ImmutableSet.of(Fluids.LAVA, Fluids.WATER);
   }

   protected int getMaxCaveCount() {
      return 10;
   }

   protected float getTunnelSystemWidth(Random random) {
      return (random.nextFloat() * 2.0F + random.nextFloat()) * 2.0F;
   }

   protected double getTunnelSystemHeightWidthRatio() {
      return 5.0D;
   }

   protected int getCaveY(Random random) {
      return random.nextInt(this.heightLimit);
   }

   protected boolean carveAtPoint(Chunk chunk, Function<BlockPos, Biome> function, BitSet bitSet, Random random, BlockPos.Mutable mutable, BlockPos.Mutable mutable2, BlockPos.Mutable mutable3, int mainChunkX, int mainChunkZ, int i, int j, int k, int l, int m, int n, AtomicBoolean atomicBoolean) {
      int o = l | n << 4 | m << 8;
      if (bitSet.get(o)) {
         return false;
      } else {
         bitSet.set(o);
         mutable.set(j, m, k);
         if (this.canAlwaysCarveBlock(chunk.getBlockState(mutable))) {
            BlockState blockState2;
            if (m <= 31) {
               blockState2 = LAVA.getBlockState();
            } else {
               blockState2 = CAVE_AIR;
            }

            chunk.setBlockState(mutable, blockState2, false);
            return true;
         } else {
            return false;
         }
      }
   }
}
