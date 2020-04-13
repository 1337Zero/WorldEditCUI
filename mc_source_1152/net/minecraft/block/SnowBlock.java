package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.WorldView;

public class SnowBlock extends Block {
   public static final IntProperty LAYERS;
   protected static final VoxelShape[] LAYERS_TO_SHAPE;

   protected SnowBlock(Block.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(LAYERS, 1));
   }

   public boolean canPlaceAtSide(BlockState world, BlockView view, BlockPos pos, BlockPlacementEnvironment env) {
      switch(env) {
      case LAND:
         return (Integer)world.get(LAYERS) < 5;
      case WATER:
         return false;
      case AIR:
         return false;
      default:
         return false;
      }
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return LAYERS_TO_SHAPE[(Integer)state.get(LAYERS)];
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return LAYERS_TO_SHAPE[(Integer)state.get(LAYERS) - 1];
   }

   public boolean hasSidedTransparency(BlockState state) {
      return true;
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockState blockState = world.getBlockState(pos.down());
      Block block = blockState.getBlock();
      if (block != Blocks.ICE && block != Blocks.PACKED_ICE && block != Blocks.BARRIER) {
         if (block != Blocks.HONEY_BLOCK && block != Blocks.SOUL_SAND) {
            return Block.isFaceFullSquare(blockState.getCollisionShape(world, pos.down()), Direction.UP) || block == this && (Integer)blockState.get(LAYERS) == 8;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
      return !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (world.getLightLevel(LightType.BLOCK, pos) > 11) {
         dropStacks(state, world, pos);
         world.removeBlock(pos, false);
      }

   }

   public boolean canReplace(BlockState state, ItemPlacementContext ctx) {
      int i = (Integer)state.get(LAYERS);
      if (ctx.getStack().getItem() == this.asItem() && i < 8) {
         if (ctx.canReplaceExisting()) {
            return ctx.getSide() == Direction.UP;
         } else {
            return true;
         }
      } else {
         return i == 1;
      }
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos());
      if (blockState.getBlock() == this) {
         int i = (Integer)blockState.get(LAYERS);
         return (BlockState)blockState.with(LAYERS, Math.min(8, i + 1));
      } else {
         return super.getPlacementState(ctx);
      }
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(LAYERS);
   }

   static {
      LAYERS = Properties.LAYERS;
      LAYERS_TO_SHAPE = new VoxelShape[]{VoxelShapes.empty(), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)};
   }
}
