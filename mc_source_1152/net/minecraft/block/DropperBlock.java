package net.minecraft.block;

import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointerImpl;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class DropperBlock extends DispenserBlock {
   private static final DispenserBehavior BEHAVIOR = new ItemDispenserBehavior();

   public DropperBlock(Block.Settings settings) {
      super(settings);
   }

   protected DispenserBehavior getBehaviorForItem(ItemStack stack) {
      return BEHAVIOR;
   }

   public BlockEntity createBlockEntity(BlockView view) {
      return new DropperBlockEntity();
   }

   protected void dispense(World world, BlockPos pos) {
      BlockPointerImpl blockPointerImpl = new BlockPointerImpl(world, pos);
      DispenserBlockEntity dispenserBlockEntity = (DispenserBlockEntity)blockPointerImpl.getBlockEntity();
      int i = dispenserBlockEntity.chooseNonEmptySlot();
      if (i < 0) {
         world.playLevelEvent(1001, pos, 0);
      } else {
         ItemStack itemStack = dispenserBlockEntity.getInvStack(i);
         if (!itemStack.isEmpty()) {
            Direction direction = (Direction)world.getBlockState(pos).get(FACING);
            Inventory inventory = HopperBlockEntity.getInventoryAt(world, pos.offset(direction));
            ItemStack itemStack3;
            if (inventory == null) {
               itemStack3 = BEHAVIOR.dispense(blockPointerImpl, itemStack);
            } else {
               itemStack3 = HopperBlockEntity.transfer(dispenserBlockEntity, inventory, itemStack.copy().split(1), direction.getOpposite());
               if (itemStack3.isEmpty()) {
                  itemStack3 = itemStack.copy();
                  itemStack3.decrement(1);
               } else {
                  itemStack3 = itemStack.copy();
               }
            }

            dispenserBlockEntity.setInvStack(i, itemStack3);
         }
      }
   }
}
