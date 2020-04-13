package net.minecraft.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.container.Container;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BrewingStandBlock extends BlockWithEntity {
   public static final BooleanProperty[] BOTTLE_PROPERTIES;
   protected static final VoxelShape SHAPE;

   public BrewingStandBlock(Block.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(BOTTLE_PROPERTIES[0], false)).with(BOTTLE_PROPERTIES[1], false)).with(BOTTLE_PROPERTIES[2], false));
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public BlockEntity createBlockEntity(BlockView view) {
      return new BrewingStandBlockEntity();
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return SHAPE;
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         return ActionResult.SUCCESS;
      } else {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof BrewingStandBlockEntity) {
            player.openContainer((BrewingStandBlockEntity)blockEntity);
            player.incrementStat(Stats.INTERACT_WITH_BREWINGSTAND);
         }

         return ActionResult.SUCCESS;
      }
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      if (itemStack.hasCustomName()) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof BrewingStandBlockEntity) {
            ((BrewingStandBlockEntity)blockEntity).setCustomName(itemStack.getName());
         }
      }

   }

   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      double d = (double)pos.getX() + 0.4D + (double)random.nextFloat() * 0.2D;
      double e = (double)pos.getY() + 0.7D + (double)random.nextFloat() * 0.3D;
      double f = (double)pos.getZ() + 0.4D + (double)random.nextFloat() * 0.2D;
      world.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0D, 0.0D, 0.0D);
   }

   public void onBlockRemoved(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (state.getBlock() != newState.getBlock()) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof BrewingStandBlockEntity) {
            ItemScatterer.spawn(world, (BlockPos)pos, (Inventory)((BrewingStandBlockEntity)blockEntity));
         }

         super.onBlockRemoved(state, world, pos, newState, moved);
      }
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      return Container.calculateComparatorOutput(world.getBlockEntity(pos));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(BOTTLE_PROPERTIES[0], BOTTLE_PROPERTIES[1], BOTTLE_PROPERTIES[2]);
   }

   public boolean canPlaceAtSide(BlockState world, BlockView view, BlockPos pos, BlockPlacementEnvironment env) {
      return false;
   }

   static {
      BOTTLE_PROPERTIES = new BooleanProperty[]{Properties.HAS_BOTTLE_0, Properties.HAS_BOTTLE_1, Properties.HAS_BOTTLE_2};
      SHAPE = VoxelShapes.union(Block.createCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 2.0D, 15.0D), Block.createCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 14.0D, 9.0D));
   }
}
