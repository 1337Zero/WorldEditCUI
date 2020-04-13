package net.minecraft.block;

import java.util.Map;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;

public class MushroomBlock extends Block {
   public static final BooleanProperty NORTH;
   public static final BooleanProperty EAST;
   public static final BooleanProperty SOUTH;
   public static final BooleanProperty WEST;
   public static final BooleanProperty UP;
   public static final BooleanProperty DOWN;
   private static final Map<Direction, BooleanProperty> FACING_PROPERTIES;

   public MushroomBlock(Block.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(NORTH, true)).with(EAST, true)).with(SOUTH, true)).with(WEST, true)).with(UP, true)).with(DOWN, true));
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockView blockView = ctx.getWorld();
      BlockPos blockPos = ctx.getBlockPos();
      return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(DOWN, this != blockView.getBlockState(blockPos.down()).getBlock())).with(UP, this != blockView.getBlockState(blockPos.up()).getBlock())).with(NORTH, this != blockView.getBlockState(blockPos.north()).getBlock())).with(EAST, this != blockView.getBlockState(blockPos.east()).getBlock())).with(SOUTH, this != blockView.getBlockState(blockPos.south()).getBlock())).with(WEST, this != blockView.getBlockState(blockPos.west()).getBlock());
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
      return neighborState.getBlock() == this ? (BlockState)state.with((Property)FACING_PROPERTIES.get(facing), false) : super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)state.with((Property)FACING_PROPERTIES.get(rotation.rotate(Direction.NORTH)), state.get(NORTH))).with((Property)FACING_PROPERTIES.get(rotation.rotate(Direction.SOUTH)), state.get(SOUTH))).with((Property)FACING_PROPERTIES.get(rotation.rotate(Direction.EAST)), state.get(EAST))).with((Property)FACING_PROPERTIES.get(rotation.rotate(Direction.WEST)), state.get(WEST))).with((Property)FACING_PROPERTIES.get(rotation.rotate(Direction.UP)), state.get(UP))).with((Property)FACING_PROPERTIES.get(rotation.rotate(Direction.DOWN)), state.get(DOWN));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)state.with((Property)FACING_PROPERTIES.get(mirror.apply(Direction.NORTH)), state.get(NORTH))).with((Property)FACING_PROPERTIES.get(mirror.apply(Direction.SOUTH)), state.get(SOUTH))).with((Property)FACING_PROPERTIES.get(mirror.apply(Direction.EAST)), state.get(EAST))).with((Property)FACING_PROPERTIES.get(mirror.apply(Direction.WEST)), state.get(WEST))).with((Property)FACING_PROPERTIES.get(mirror.apply(Direction.UP)), state.get(UP))).with((Property)FACING_PROPERTIES.get(mirror.apply(Direction.DOWN)), state.get(DOWN));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(UP, DOWN, NORTH, EAST, SOUTH, WEST);
   }

   static {
      NORTH = ConnectedPlantBlock.NORTH;
      EAST = ConnectedPlantBlock.EAST;
      SOUTH = ConnectedPlantBlock.SOUTH;
      WEST = ConnectedPlantBlock.WEST;
      UP = ConnectedPlantBlock.UP;
      DOWN = ConnectedPlantBlock.DOWN;
      FACING_PROPERTIES = ConnectedPlantBlock.FACING_PROPERTIES;
   }
}
