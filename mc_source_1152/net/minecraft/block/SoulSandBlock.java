package net.minecraft.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class SoulSandBlock extends Block {
   protected static final VoxelShape COLLISION_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D);

   public SoulSandBlock(Block.Settings settings) {
      super(settings);
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return COLLISION_SHAPE;
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      BubbleColumnBlock.update(world, pos.up(), false);
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos neighborPos, boolean moved) {
      world.getBlockTickScheduler().schedule(pos, this, this.getTickRate(world));
   }

   public boolean isSimpleFullBlock(BlockState state, BlockView view, BlockPos pos) {
      return true;
   }

   public int getTickRate(WorldView worldView) {
      return 20;
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved) {
      world.getBlockTickScheduler().schedule(pos, this, this.getTickRate(world));
   }

   public boolean canPlaceAtSide(BlockState world, BlockView view, BlockPos pos, BlockPlacementEnvironment env) {
      return false;
   }

   public boolean allowsSpawning(BlockState state, BlockView view, BlockPos pos, EntityType<?> type) {
      return true;
   }

   @Environment(EnvType.CLIENT)
   public boolean hasInWallOverlay(BlockState state, BlockView view, BlockPos pos) {
      return true;
   }
}
