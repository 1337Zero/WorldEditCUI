package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityContext;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.WorldView;

public class KelpBlock extends Block implements FluidFillable {
   public static final IntProperty AGE;
   protected static final VoxelShape SHAPE;

   protected KelpBlock(Block.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return SHAPE;
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
      return fluidState.matches(FluidTags.WATER) && fluidState.getLevel() == 8 ? this.getPlacementState((IWorld)ctx.getWorld()) : null;
   }

   public BlockState getPlacementState(IWorld world) {
      return (BlockState)this.getDefaultState().with(AGE, world.getRandom().nextInt(25));
   }

   public FluidState getFluidState(BlockState state) {
      return Fluids.WATER.getStill(false);
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!state.canPlaceAt(world, pos)) {
         world.breakBlock(pos, true);
      } else {
         BlockPos blockPos = pos.up();
         BlockState blockState = world.getBlockState(blockPos);
         if (blockState.getBlock() == Blocks.WATER && (Integer)state.get(AGE) < 25 && random.nextDouble() < 0.14D) {
            world.setBlockState(blockPos, (BlockState)state.cycle(AGE));
         }

      }
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockPos blockPos = pos.down();
      BlockState blockState = world.getBlockState(blockPos);
      Block block = blockState.getBlock();
      if (block == Blocks.MAGMA_BLOCK) {
         return false;
      } else {
         return block == this || block == Blocks.KELP_PLANT || blockState.isSideSolidFullSquare(world, blockPos, Direction.UP);
      }
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
      if (!state.canPlaceAt(world, pos)) {
         if (facing == Direction.DOWN) {
            return Blocks.AIR.getDefaultState();
         }

         world.getBlockTickScheduler().schedule(pos, this, 1);
      }

      if (facing == Direction.UP && neighborState.getBlock() == this) {
         return Blocks.KELP_PLANT.getDefaultState();
      } else {
         world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
         return super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
      }
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(AGE);
   }

   public boolean canFillWithFluid(BlockView view, BlockPos pos, BlockState state, Fluid fluid) {
      return false;
   }

   public boolean tryFillWithFluid(IWorld world, BlockPos pos, BlockState state, FluidState fluidState) {
      return false;
   }

   static {
      AGE = Properties.AGE_25;
      SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 9.0D, 16.0D);
   }
}
