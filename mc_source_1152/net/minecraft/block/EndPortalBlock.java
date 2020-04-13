package net.minecraft.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class EndPortalBlock extends BlockWithEntity {
   protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

   protected EndPortalBlock(Block.Settings settings) {
      super(settings);
   }

   public BlockEntity createBlockEntity(BlockView view) {
      return new EndPortalBlockEntity();
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return SHAPE;
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if (!world.isClient && !entity.hasVehicle() && !entity.hasPassengers() && entity.canUsePortals() && VoxelShapes.matchesAnywhere(VoxelShapes.cuboid(entity.getBoundingBox().offset((double)(-pos.getX()), (double)(-pos.getY()), (double)(-pos.getZ()))), state.getOutlineShape(world, pos), BooleanBiFunction.AND)) {
         entity.changeDimension(world.dimension.getType() == DimensionType.THE_END ? DimensionType.OVERWORLD : DimensionType.THE_END);
      }

   }

   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      double d = (double)pos.getX() + (double)random.nextFloat();
      double e = (double)pos.getY() + 0.8D;
      double f = (double)pos.getZ() + (double)random.nextFloat();
      double g = 0.0D;
      double h = 0.0D;
      double i = 0.0D;
      world.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0D, 0.0D, 0.0D);
   }

   @Environment(EnvType.CLIENT)
   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      return ItemStack.EMPTY;
   }

   public boolean canBucketPlace(BlockState state, Fluid fluid) {
      return false;
   }
}
