package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;

public class AttachedStemBlock extends PlantBlock {
   public static final DirectionProperty FACING;
   private final GourdBlock gourdBlock;
   private static final Map<Direction, VoxelShape> FACING_TO_SHAPE;

   protected AttachedStemBlock(GourdBlock gourdBlock, Block.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH));
      this.gourdBlock = gourdBlock;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return (VoxelShape)FACING_TO_SHAPE.get(state.get(FACING));
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
      return neighborState.getBlock() != this.gourdBlock && facing == state.get(FACING) ? (BlockState)this.gourdBlock.getStem().getDefaultState().with(StemBlock.AGE, 7) : super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
   }

   protected boolean canPlantOnTop(BlockState floor, BlockView view, BlockPos pos) {
      return floor.getBlock() == Blocks.FARMLAND;
   }

   @Environment(EnvType.CLIENT)
   protected Item getSeeds() {
      if (this.gourdBlock == Blocks.PUMPKIN) {
         return Items.PUMPKIN_SEEDS;
      } else {
         return this.gourdBlock == Blocks.MELON ? Items.MELON_SEEDS : Items.AIR;
      }
   }

   @Environment(EnvType.CLIENT)
   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      return new ItemStack(this.getSeeds());
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }

   static {
      FACING = HorizontalFacingBlock.FACING;
      FACING_TO_SHAPE = Maps.newEnumMap(ImmutableMap.of(Direction.SOUTH, Block.createCuboidShape(6.0D, 0.0D, 6.0D, 10.0D, 10.0D, 16.0D), Direction.WEST, Block.createCuboidShape(0.0D, 0.0D, 6.0D, 10.0D, 10.0D, 10.0D), Direction.NORTH, Block.createCuboidShape(6.0D, 0.0D, 0.0D, 10.0D, 10.0D, 10.0D), Direction.EAST, Block.createCuboidShape(6.0D, 0.0D, 6.0D, 16.0D, 10.0D, 10.0D)));
   }
}
