package net.minecraft.block;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.container.Container;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public abstract class AbstractFurnaceBlock extends BlockWithEntity {
   public static final DirectionProperty FACING;
   public static final BooleanProperty LIT;

   protected AbstractFurnaceBlock(Block.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(LIT, false));
   }

   public int getLuminance(BlockState state) {
      return (Boolean)state.get(LIT) ? super.getLuminance(state) : 0;
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         return ActionResult.SUCCESS;
      } else {
         this.openContainer(world, pos, player);
         return ActionResult.SUCCESS;
      }
   }

   protected abstract void openContainer(World world, BlockPos pos, PlayerEntity player);

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      if (itemStack.hasCustomName()) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof AbstractFurnaceBlockEntity) {
            ((AbstractFurnaceBlockEntity)blockEntity).setCustomName(itemStack.getName());
         }
      }

   }

   public void onBlockRemoved(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (state.getBlock() != newState.getBlock()) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof AbstractFurnaceBlockEntity) {
            ItemScatterer.spawn(world, (BlockPos)pos, (Inventory)((AbstractFurnaceBlockEntity)blockEntity));
            world.updateHorizontalAdjacent(pos, this);
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

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING, LIT);
   }

   static {
      FACING = HorizontalFacingBlock.FACING;
      LIT = RedstoneTorchBlock.LIT;
   }
}
