package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.entity.EntityContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SaplingBlock extends PlantBlock implements Fertilizable {
   public static final IntProperty STAGE;
   protected static final VoxelShape SHAPE;
   private final SaplingGenerator generator;

   protected SaplingBlock(SaplingGenerator generator, Block.Settings settings) {
      super(settings);
      this.generator = generator;
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(STAGE, 0));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return SHAPE;
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      super.scheduledTick(state, world, pos, random);
      if (world.getLightLevel(pos.up()) >= 9 && random.nextInt(7) == 0) {
         this.generate(world, pos, state, random);
      }

   }

   public void generate(ServerWorld serverWorld, BlockPos blockPos, BlockState blockState, Random random) {
      if ((Integer)blockState.get(STAGE) == 0) {
         serverWorld.setBlockState(blockPos, (BlockState)blockState.cycle(STAGE), 4);
      } else {
         this.generator.generate(serverWorld, serverWorld.getChunkManager().getChunkGenerator(), blockPos, blockState, random);
      }

   }

   public boolean isFertilizable(BlockView world, BlockPos pos, BlockState state, boolean isClient) {
      return true;
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return (double)world.random.nextFloat() < 0.45D;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      this.generate(world, pos, state, random);
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(STAGE);
   }

   static {
      STAGE = Properties.STAGE;
      SHAPE = Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);
   }
}
