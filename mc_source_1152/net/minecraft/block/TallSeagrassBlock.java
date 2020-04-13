package net.minecraft.block;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.EntityContext;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.WorldView;

public class TallSeagrassBlock extends ReplaceableTallPlantBlock implements FluidFillable {
   public static final EnumProperty<DoubleBlockHalf> HALF;
   protected static final VoxelShape SHAPE;

   public TallSeagrassBlock(Block.Settings settings) {
      super(settings);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return SHAPE;
   }

   protected boolean canPlantOnTop(BlockState floor, BlockView view, BlockPos pos) {
      return floor.isSideSolidFullSquare(view, pos, Direction.UP) && floor.getBlock() != Blocks.MAGMA_BLOCK;
   }

   @Environment(EnvType.CLIENT)
   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      return new ItemStack(Blocks.SEAGRASS);
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState blockState = super.getPlacementState(ctx);
      if (blockState != null) {
         FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos().up());
         if (fluidState.matches(FluidTags.WATER) && fluidState.getLevel() == 8) {
            return blockState;
         }
      }

      return null;
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      if (state.get(HALF) == DoubleBlockHalf.UPPER) {
         BlockState blockState = world.getBlockState(pos.down());
         return blockState.getBlock() == this && blockState.get(HALF) == DoubleBlockHalf.LOWER;
      } else {
         FluidState fluidState = world.getFluidState(pos);
         return super.canPlaceAt(state, world, pos) && fluidState.matches(FluidTags.WATER) && fluidState.getLevel() == 8;
      }
   }

   public FluidState getFluidState(BlockState state) {
      return Fluids.WATER.getStill(false);
   }

   public boolean canFillWithFluid(BlockView view, BlockPos pos, BlockState state, Fluid fluid) {
      return false;
   }

   public boolean tryFillWithFluid(IWorld world, BlockPos pos, BlockState state, FluidState fluidState) {
      return false;
   }

   static {
      HALF = ReplaceableTallPlantBlock.HALF;
      SHAPE = Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
   }
}
