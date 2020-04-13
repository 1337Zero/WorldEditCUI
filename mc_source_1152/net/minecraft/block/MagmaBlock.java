package net.minecraft.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class MagmaBlock extends Block {
   public MagmaBlock(Block.Settings settings) {
      super(settings);
   }

   public void onSteppedOn(World world, BlockPos pos, Entity entity) {
      if (!entity.isFireImmune() && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entity)) {
         entity.damage(DamageSource.HOT_FLOOR, 1.0F);
      }

      super.onSteppedOn(world, pos, entity);
   }

   @Environment(EnvType.CLIENT)
   public boolean hasEmissiveLighting(BlockState state) {
      return true;
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      BubbleColumnBlock.update(world, pos.up(), true);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
      if (facing == Direction.UP && neighborState.getBlock() == Blocks.WATER) {
         world.getBlockTickScheduler().schedule(pos, this, this.getTickRate(world));
      }

      return super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      BlockPos blockPos = pos.up();
      if (world.getFluidState(pos).matches(FluidTags.WATER)) {
         world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
         world.spawnParticles(ParticleTypes.LARGE_SMOKE, (double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.25D, (double)blockPos.getZ() + 0.5D, 8, 0.5D, 0.25D, 0.5D, 0.0D);
      }

   }

   public int getTickRate(WorldView worldView) {
      return 20;
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved) {
      world.getBlockTickScheduler().schedule(pos, this, this.getTickRate(world));
   }

   public boolean allowsSpawning(BlockState state, BlockView view, BlockPos pos, EntityType<?> type) {
      return type.isFireImmune();
   }

   public boolean shouldPostProcess(BlockState state, BlockView view, BlockPos pos) {
      return true;
   }
}
