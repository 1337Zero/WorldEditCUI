package net.minecraft.block;

import java.util.Random;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class ObserverBlock extends FacingBlock {
   public static final BooleanProperty POWERED;

   public ObserverBlock(Block.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.SOUTH)).with(POWERED, false));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING, POWERED);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if ((Boolean)state.get(POWERED)) {
         world.setBlockState(pos, (BlockState)state.with(POWERED, false), 2);
      } else {
         world.setBlockState(pos, (BlockState)state.with(POWERED, true), 2);
         world.getBlockTickScheduler().schedule(pos, this, 2);
      }

      this.updateNeighbors(world, pos, state);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
      if (state.get(FACING) == facing && !(Boolean)state.get(POWERED)) {
         this.scheduleTick(world, pos);
      }

      return super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
   }

   private void scheduleTick(IWorld world, BlockPos pos) {
      if (!world.isClient() && !world.getBlockTickScheduler().isScheduled(pos, this)) {
         world.getBlockTickScheduler().schedule(pos, this, 2);
      }

   }

   protected void updateNeighbors(World world, BlockPos pos, BlockState state) {
      Direction direction = (Direction)state.get(FACING);
      BlockPos blockPos = pos.offset(direction.getOpposite());
      world.updateNeighbor(blockPos, this, pos);
      world.updateNeighborsExcept(blockPos, this, direction);
   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   public int getStrongRedstonePower(BlockState state, BlockView view, BlockPos pos, Direction facing) {
      return state.getWeakRedstonePower(view, pos, facing);
   }

   public int getWeakRedstonePower(BlockState state, BlockView view, BlockPos pos, Direction facing) {
      return (Boolean)state.get(POWERED) && state.get(FACING) == facing ? 15 : 0;
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved) {
      if (state.getBlock() != oldState.getBlock()) {
         if (!world.isClient() && (Boolean)state.get(POWERED) && !world.getBlockTickScheduler().isScheduled(pos, this)) {
            BlockState blockState = (BlockState)state.with(POWERED, false);
            world.setBlockState(pos, blockState, 18);
            this.updateNeighbors(world, pos, blockState);
         }

      }
   }

   public void onBlockRemoved(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (state.getBlock() != newState.getBlock()) {
         if (!world.isClient && (Boolean)state.get(POWERED) && world.getBlockTickScheduler().isScheduled(pos, this)) {
            this.updateNeighbors(world, pos, (BlockState)state.with(POWERED, false));
         }

      }
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite().getOpposite());
   }

   static {
      POWERED = Properties.POWERED;
   }
}
