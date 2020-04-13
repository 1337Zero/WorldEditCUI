package net.minecraft.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class NetherWartBlock extends PlantBlock {
   public static final IntProperty AGE;
   private static final VoxelShape[] AGE_TO_SHAPE;

   protected NetherWartBlock(Block.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return AGE_TO_SHAPE[(Integer)state.get(AGE)];
   }

   protected boolean canPlantOnTop(BlockState floor, BlockView view, BlockPos pos) {
      return floor.getBlock() == Blocks.SOUL_SAND;
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      int i = (Integer)state.get(AGE);
      if (i < 3 && random.nextInt(10) == 0) {
         state = (BlockState)state.with(AGE, i + 1);
         world.setBlockState(pos, state, 2);
      }

      super.scheduledTick(state, world, pos, random);
   }

   @Environment(EnvType.CLIENT)
   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      return new ItemStack(Items.NETHER_WART);
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(AGE);
   }

   static {
      AGE = Properties.AGE_3;
      AGE_TO_SHAPE = new VoxelShape[]{Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 5.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 11.0D, 16.0D), Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D)};
   }
}
