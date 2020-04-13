package net.minecraft.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class TorchBlock extends Block {
   protected static final VoxelShape BOUNDING_SHAPE = Block.createCuboidShape(6.0D, 0.0D, 6.0D, 10.0D, 10.0D, 10.0D);

   protected TorchBlock(Block.Settings settings) {
      super(settings);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return BOUNDING_SHAPE;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
      return facing == Direction.DOWN && !this.canPlaceAt(state, world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return sideCoversSmallSquare(world, pos.down(), Direction.UP);
   }

   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      double d = (double)pos.getX() + 0.5D;
      double e = (double)pos.getY() + 0.7D;
      double f = (double)pos.getZ() + 0.5D;
      world.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0D, 0.0D, 0.0D);
      world.addParticle(ParticleTypes.FLAME, d, e, f, 0.0D, 0.0D, 0.0D);
   }
}
