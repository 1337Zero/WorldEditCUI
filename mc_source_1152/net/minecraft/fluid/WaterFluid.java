package net.minecraft.fluid;

import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public abstract class WaterFluid extends BaseFluid {
   public Fluid getFlowing() {
      return Fluids.FLOWING_WATER;
   }

   public Fluid getStill() {
      return Fluids.WATER;
   }

   public Item getBucketItem() {
      return Items.WATER_BUCKET;
   }

   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(World world, BlockPos blockPos, FluidState fluidState, Random random) {
      if (!fluidState.isStill() && !(Boolean)fluidState.get(FALLING)) {
         if (random.nextInt(64) == 0) {
            world.playSound((double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D, SoundEvents.BLOCK_WATER_AMBIENT, SoundCategory.BLOCKS, random.nextFloat() * 0.25F + 0.75F, random.nextFloat() + 0.5F, false);
         }
      } else if (random.nextInt(10) == 0) {
         world.addParticle(ParticleTypes.UNDERWATER, (double)blockPos.getX() + (double)random.nextFloat(), (double)blockPos.getY() + (double)random.nextFloat(), (double)blockPos.getZ() + (double)random.nextFloat(), 0.0D, 0.0D, 0.0D);
      }

   }

   @Nullable
   @Environment(EnvType.CLIENT)
   public ParticleEffect getParticle() {
      return ParticleTypes.DRIPPING_WATER;
   }

   protected boolean isInfinite() {
      return true;
   }

   protected void beforeBreakingBlock(IWorld world, BlockPos pos, BlockState state) {
      BlockEntity blockEntity = state.getBlock().hasBlockEntity() ? world.getBlockEntity(pos) : null;
      Block.dropStacks(state, world.getWorld(), pos, blockEntity);
   }

   public int method_15733(WorldView worldView) {
      return 4;
   }

   public BlockState toBlockState(FluidState fluidState) {
      return (BlockState)Blocks.WATER.getDefaultState().with(FluidBlock.LEVEL, method_15741(fluidState));
   }

   public boolean matchesType(Fluid fluid) {
      return fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;
   }

   public int getLevelDecreasePerBlock(WorldView view) {
      return 1;
   }

   public int getTickRate(WorldView worldView) {
      return 5;
   }

   public boolean method_15777(FluidState fluidState, BlockView blockView, BlockPos blockPos, Fluid fluid, Direction direction) {
      return direction == Direction.DOWN && !fluid.matches(FluidTags.WATER);
   }

   protected float getBlastResistance() {
      return 100.0F;
   }

   public static class Flowing extends WaterFluid {
      protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
         super.appendProperties(builder);
         builder.add(LEVEL);
      }

      public int getLevel(FluidState fluidState) {
         return (Integer)fluidState.get(LEVEL);
      }

      public boolean isStill(FluidState fluidState) {
         return false;
      }
   }

   public static class Still extends WaterFluid {
      public int getLevel(FluidState fluidState) {
         return 8;
      }

      public boolean isStill(FluidState fluidState) {
         return true;
      }
   }
}
