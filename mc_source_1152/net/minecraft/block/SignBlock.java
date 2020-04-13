package net.minecraft.block;

import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.SignType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraft.world.WorldView;

public class SignBlock extends AbstractSignBlock {
   public static final IntProperty ROTATION;

   public SignBlock(Block.Settings settings, SignType signType) {
      super(settings, signType);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(ROTATION, 0)).with(WATERLOGGED, false));
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return world.getBlockState(pos.down()).getMaterial().isSolid();
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
      return (BlockState)((BlockState)this.getDefaultState().with(ROTATION, MathHelper.floor((double)((180.0F + ctx.getPlayerYaw()) * 16.0F / 360.0F) + 0.5D) & 15)).with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
      return facing == Direction.DOWN && !this.canPlaceAt(state, world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(ROTATION, rotation.rotate((Integer)state.get(ROTATION), 16));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return (BlockState)state.with(ROTATION, mirror.mirror((Integer)state.get(ROTATION), 16));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(ROTATION, WATERLOGGED);
   }

   static {
      ROTATION = Properties.ROTATION;
   }
}
