package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class ConduitBlock extends BlockWithEntity implements Waterloggable {
   public static final BooleanProperty WATERLOGGED;
   protected static final VoxelShape SHAPE;

   public ConduitBlock(Block.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(WATERLOGGED, true));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(WATERLOGGED);
   }

   public BlockEntity createBlockEntity(BlockView view) {
      return new ConduitBlockEntity();
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.ENTITYBLOCK_ANIMATED;
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return SHAPE;
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
      if (itemStack.hasCustomName()) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof BeaconBlockEntity) {
            ((BeaconBlockEntity)blockEntity).setCustomName(itemStack.getName());
         }
      }

   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
      return (BlockState)this.getDefaultState().with(WATERLOGGED, fluidState.matches(FluidTags.WATER) && fluidState.getLevel() == 8);
   }

   public boolean canPlaceAtSide(BlockState world, BlockView view, BlockPos pos, BlockPlacementEnvironment env) {
      return false;
   }

   static {
      WATERLOGGED = Properties.WATERLOGGED;
      SHAPE = Block.createCuboidShape(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);
   }
}
