package net.minecraft.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class HopperContainer extends Container {
   private final Inventory inventory;

   public HopperContainer(int syncId, PlayerInventory playerInventory) {
      this(syncId, playerInventory, new BasicInventory(5));
   }

   public HopperContainer(int syncId, PlayerInventory playerInventory, Inventory inventory) {
      super(ContainerType.HOPPER, syncId);
      this.inventory = inventory;
      checkContainerSize(inventory, 5);
      inventory.onInvOpen(playerInventory.player);
      int i = true;

      int m;
      for(m = 0; m < 5; ++m) {
         this.addSlot(new Slot(inventory, m, 44 + m * 18, 20));
      }

      for(m = 0; m < 3; ++m) {
         for(int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, m * 18 + 51));
         }
      }

      for(m = 0; m < 9; ++m) {
         this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 109));
      }

   }

   public boolean canUse(PlayerEntity player) {
      return this.inventory.canPlayerUseInv(player);
   }

   public ItemStack transferSlot(PlayerEntity player, int invSlot) {
      ItemStack itemStack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slotList.get(invSlot);
      if (slot != null && slot.hasStack()) {
         ItemStack itemStack2 = slot.getStack();
         itemStack = itemStack2.copy();
         if (invSlot < this.inventory.getInvSize()) {
            if (!this.insertItem(itemStack2, this.inventory.getInvSize(), this.slotList.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.insertItem(itemStack2, 0, this.inventory.getInvSize(), false)) {
            return ItemStack.EMPTY;
         }

         if (itemStack2.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
         } else {
            slot.markDirty();
         }
      }

      return itemStack;
   }

   public void close(PlayerEntity player) {
      super.close(player);
      this.inventory.onInvClose(player);
   }
}
