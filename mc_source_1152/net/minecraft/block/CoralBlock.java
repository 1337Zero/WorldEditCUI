package net.minecraft.block;

import java.util.Random;
import net.minecraft.entity.EntityContext;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class CoralBlock extends CoralParentBlock {
   private final Block deadCoralBlock;
   protected static final VoxelShape SHAPE = Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 15.0D, 14.0D);

   protected CoralBlock(Block deadCoralBlock, Block.Settings settings) {
      super(settings);
      this.deadCoralBlock = deadCoralBlock;
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved) {
      this.checkLivingConditions(state, world, pos);
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!isInWater(state, world, pos)) {
         world.setBlockState(pos, (BlockState)this.deadCoralBlock.getDefaultState().with(WATERLOGGED, false), 2);
      }

   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
      if (facing == Direction.DOWN && !state.canPlaceAt(world, pos)) {
         return Blocks.AIR.getDefaultState();
      } else {
         this.checkLivingConditions(state, world, pos);
         if ((Boolean)state.get(WATERLOGGED)) {
            world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
         }

         return super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
      }
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return SHAPE;
   }
}
