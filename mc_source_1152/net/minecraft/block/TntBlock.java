package net.minecraft.block;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public class TntBlock extends Block {
   public static final BooleanProperty UNSTABLE;

   public TntBlock(Block.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)this.getDefaultState().with(UNSTABLE, false));
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved) {
      if (oldState.getBlock() != state.getBlock()) {
         if (world.isReceivingRedstonePower(pos)) {
            primeTnt(world, pos);
            world.removeBlock(pos, false);
         }

      }
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos neighborPos, boolean moved) {
      if (world.isReceivingRedstonePower(pos)) {
         primeTnt(world, pos);
         world.removeBlock(pos, false);
      }

   }

   public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      if (!world.isClient() && !player.isCreative() && (Boolean)state.get(UNSTABLE)) {
         primeTnt(world, pos);
      }

      super.onBreak(world, pos, state, player);
   }

   public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
      if (!world.isClient) {
         TntEntity tntEntity = new TntEntity(world, (double)((float)pos.getX() + 0.5F), (double)pos.getY(), (double)((float)pos.getZ() + 0.5F), explosion.getCausingEntity());
         tntEntity.setFuse((short)(world.random.nextInt(tntEntity.getFuseTimer() / 4) + tntEntity.getFuseTimer() / 8));
         world.spawnEntity(tntEntity);
      }
   }

   public static void primeTnt(World world, BlockPos pos) {
      primeTnt(world, pos, (LivingEntity)null);
   }

   private static void primeTnt(World world, BlockPos pos, @Nullable LivingEntity igniter) {
      if (!world.isClient) {
         TntEntity tntEntity = new TntEntity(world, (double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D, igniter);
         world.spawnEntity(tntEntity);
         world.playSound((PlayerEntity)null, tntEntity.getX(), tntEntity.getY(), tntEntity.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
      }
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      ItemStack itemStack = player.getStackInHand(hand);
      Item item = itemStack.getItem();
      if (item != Items.FLINT_AND_STEEL && item != Items.FIRE_CHARGE) {
         return super.onUse(state, world, pos, player, hand, hit);
      } else {
         primeTnt(world, pos, player);
         world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
         if (!player.isCreative()) {
            if (item == Items.FLINT_AND_STEEL) {
               itemStack.damage(1, (LivingEntity)player, (Consumer)((playerEntity) -> {
                  playerEntity.sendToolBreakStatus(hand);
               }));
            } else {
               itemStack.decrement(1);
            }
         }

         return ActionResult.SUCCESS;
      }
   }

   public void onProjectileHit(World world, BlockState state, BlockHitResult hitResult, Entity entity) {
      if (!world.isClient && entity instanceof ProjectileEntity) {
         ProjectileEntity projectileEntity = (ProjectileEntity)entity;
         Entity entity2 = projectileEntity.getOwner();
         if (projectileEntity.isOnFire()) {
            BlockPos blockPos = hitResult.getBlockPos();
            primeTnt(world, blockPos, entity2 instanceof LivingEntity ? (LivingEntity)entity2 : null);
            world.removeBlock(blockPos, false);
         }
      }

   }

   public boolean shouldDropItemsOnExplosion(Explosion explosion) {
      return false;
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(UNSTABLE);
   }

   static {
      UNSTABLE = Properties.UNSTABLE;
   }
}
