package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityContext;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.WorldView;

public class CoralParentBlock extends Block implements Waterloggable {
   public static final BooleanProperty WATERLOGGED;
   private static final VoxelShape SHAPE;

   protected CoralParentBlock(Block.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(WATERLOGGED, true));
   }

   protected void checkLivingConditions(BlockState state, IWorld world, BlockPos pos) {
      if (!isInWater(state, world, pos)) {
         world.getBlockTickScheduler().schedule(pos, this, 60 + world.getRandom().nextInt(40));
      }

   }

   protected static boolean isInWater(BlockState state, BlockView world, BlockPos pos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         return true;
      } else {
         Direction[] var3 = Direction.values();
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            Direction direction = var3[var5];
            if (world.getFluidState(pos.offset(direction)).matches(FluidTags.WATER)) {
               return true;
            }
         }

         return false;
      }
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
      return (BlockState)this.getDefaultState().with(WATERLOGGED, fluidState.matches(FluidTags.WATER) && fluidState.getLevel() == 8);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return SHAPE;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return facing == Direction.DOWN && !this.canPlaceAt(state, world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockPos blockPos = pos.down();
      return world.getBlockState(blockPos).isSideSolidFullSquare(world, blockPos, Direction.UP);
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(WATERLOGGED);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   static {
      WATERLOGGED = Properties.WATERLOGGED;
      SHAPE = Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D);
   }
}
