package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.WorldView;

public class LanternBlock extends Block {
   public static final BooleanProperty HANGING;
   protected static final VoxelShape STANDING_SHAPE;
   protected static final VoxelShape HANGING_SHAPE;

   public LanternBlock(Block.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(HANGING, false));
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      Direction[] var2 = ctx.getPlacementDirections();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Direction direction = var2[var4];
         if (direction.getAxis() == Direction.Axis.Y) {
            BlockState blockState = (BlockState)this.getDefaultState().with(HANGING, direction == Direction.UP);
            if (blockState.canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) {
               return blockState;
            }
         }
      }

      return null;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return (Boolean)state.get(HANGING) ? HANGING_SHAPE : STANDING_SHAPE;
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(HANGING);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      Direction direction = attachedDirection(state).getOpposite();
      return Block.sideCoversSmallSquare(world, pos.offset(direction), direction.getOpposite());
   }

   protected static Direction attachedDirection(BlockState state) {
      return (Boolean)state.get(HANGING) ? Direction.DOWN : Direction.UP;
   }

   public PistonBehavior getPistonBehavior(BlockState state) {
      return PistonBehavior.DESTROY;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
      return attachedDirection(state).getOpposite() == facing && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
   }

   public boolean canPlaceAtSide(BlockState world, BlockView view, BlockPos pos, BlockPlacementEnvironment env) {
      return false;
   }

   static {
      HANGING = Properties.HANGING;
      STANDING_SHAPE = VoxelShapes.union(Block.createCuboidShape(5.0D, 0.0D, 5.0D, 11.0D, 7.0D, 11.0D), Block.createCuboidShape(6.0D, 7.0D, 6.0D, 10.0D, 9.0D, 10.0D));
      HANGING_SHAPE = VoxelShapes.union(Block.createCuboidShape(5.0D, 1.0D, 5.0D, 11.0D, 8.0D, 11.0D), Block.createCuboidShape(6.0D, 8.0D, 6.0D, 10.0D, 10.0D, 10.0D));
   }
}
