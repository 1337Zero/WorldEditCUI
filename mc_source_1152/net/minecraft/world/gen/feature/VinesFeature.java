package net.minecraft.world.gen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.VineBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;

public class VinesFeature extends Feature<DefaultFeatureConfig> {
   private static final Direction[] DIRECTIONS = Direction.values();

   public VinesFeature(Function<Dynamic<?>, ? extends DefaultFeatureConfig> configFactory) {
      super(configFactory);
   }

   public boolean generate(IWorld iWorld, ChunkGenerator<? extends ChunkGeneratorConfig> chunkGenerator, Random random, BlockPos blockPos, DefaultFeatureConfig defaultFeatureConfig) {
      BlockPos.Mutable mutable = new BlockPos.Mutable(blockPos);

      for(int i = blockPos.getY(); i < 256; ++i) {
         mutable.set((Vec3i)blockPos);
         mutable.setOffset(random.nextInt(4) - random.nextInt(4), 0, random.nextInt(4) - random.nextInt(4));
         mutable.setY(i);
         if (iWorld.isAir(mutable)) {
            Direction[] var8 = DIRECTIONS;
            int var9 = var8.length;

            for(int var10 = 0; var10 < var9; ++var10) {
               Direction direction = var8[var10];
               if (direction != Direction.DOWN && VineBlock.shouldConnectTo(iWorld, mutable, direction)) {
                  iWorld.setBlockState(mutable, (BlockState)Blocks.VINE.getDefaultState().with(VineBlock.getFacingProperty(direction), true), 2);
                  break;
               }
            }
         }
      }

      return true;
   }
}
