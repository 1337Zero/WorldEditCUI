package net.minecraft.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class ShulkerBoxContainer extends Container {
   private final Inventory inventory;

   public ShulkerBoxContainer(int syncId, PlayerInventory playerInventory) {
      this(syncId, playerInventory, new BasicInventory(27));
   }

   public ShulkerBoxContainer(int syncId, PlayerInventory playerInventory, Inventory inventory) {
      super(ContainerType.SHULKER_BOX, syncId);
      checkContainerSize(inventory, 27);
      this.inventory = inventory;
      inventory.onInvOpen(playerInventory.player);
      int i = true;
      int j = true;

      int o;
      int n;
      for(o = 0; o < 3; ++o) {
         for(n = 0; n < 9; ++n) {
            this.addSlot(new ShulkerBoxSlot(inventory, n + o * 9, 8 + n * 18, 18 + o * 18));
         }
      }

      for(o = 0; o < 3; ++o) {
         for(n = 0; n < 9; ++n) {
            this.addSlot(new Slot(playerInventory, n + o * 9 + 9, 8 + n * 18, 84 + o * 18));
         }
      }

      for(o = 0; o < 9; ++o) {
         this.addSlot(new Slot(playerInventory, o, 8 + o * 18, 142));
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
