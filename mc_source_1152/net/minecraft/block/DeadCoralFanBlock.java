package net.minecraft.block;

import net.minecraft.entity.EntityContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class DeadCoralFanBlock extends CoralParentBlock {
   private static final VoxelShape SHAPE = Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D);

   protected DeadCoralFanBlock(Block.Settings settings) {
      super(settings);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return SHAPE;
   }
}
