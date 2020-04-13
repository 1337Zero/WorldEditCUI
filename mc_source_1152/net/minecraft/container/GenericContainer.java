package net.minecraft.container;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class GenericContainer extends Container {
   private final Inventory inventory;
   private final int rows;

   private GenericContainer(ContainerType<?> containerType, int i, PlayerInventory playerInventory, int j) {
      this(containerType, i, playerInventory, new BasicInventory(9 * j), j);
   }

   public static GenericContainer createGeneric9x1(int syncId, PlayerInventory playerInventory) {
      return new GenericContainer(ContainerType.GENERIC_9X1, syncId, playerInventory, 1);
   }

   public static GenericContainer createGeneric9x2(int syncId, PlayerInventory playerInventory) {
      return new GenericContainer(ContainerType.GENERIC_9X2, syncId, playerInventory, 2);
   }

   public static GenericContainer createGeneric9x3(int syncId, PlayerInventory playerInventory) {
      return new GenericContainer(ContainerType.GENERIC_9X3, syncId, playerInventory, 3);
   }

   public static GenericContainer createGeneric9x4(int syncId, PlayerInventory playerInventory) {
      return new GenericContainer(ContainerType.GENERIC_9X4, syncId, playerInventory, 4);
   }

   public static GenericContainer createGeneric9x5(int syncId, PlayerInventory playerInventory) {
      return new GenericContainer(ContainerType.GENERIC_9X5, syncId, playerInventory, 5);
   }

   public static GenericContainer createGeneric9x6(int syncId, PlayerInventory playerInventory) {
      return new GenericContainer(ContainerType.GENERIC_9X6, syncId, playerInventory, 6);
   }

   public static GenericContainer createGeneric9x3(int syncId, PlayerInventory playerInventory, Inventory inventory) {
      return new GenericContainer(ContainerType.GENERIC_9X3, syncId, playerInventory, inventory, 3);
   }

   public static GenericContainer createGeneric9x6(int syncId, PlayerInventory playerInventory, Inventory inventory) {
      return new GenericContainer(ContainerType.GENERIC_9X6, syncId, playerInventory, inventory, 6);
   }

   public GenericContainer(ContainerType<?> containerType, int syncId, PlayerInventory playerInventory, Inventory inventory, int rows) {
      super(containerType, syncId);
      checkContainerSize(inventory, rows * 9);
      this.inventory = inventory;
      this.rows = rows;
      inventory.onInvOpen(playerInventory.player);
      int i = (this.rows - 4) * 18;

      int n;
      int m;
      for(n = 0; n < this.rows; ++n) {
         for(m = 0; m < 9; ++m) {
            this.addSlot(new Slot(inventory, m + n * 9, 8 + m * 18, 18 + n * 18));
         }
      }

      for(n = 0; n < 3; ++n) {
         for(m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m + n * 9 + 9, 8 + m * 18, 103 + n * 18 + i));
         }
      }

      for(n = 0; n < 9; ++n) {
         this.addSlot(new Slot(playerInventory, n, 8 + n * 18, 161 + i));
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
         if (invSlot < this.rows * 9) {
            if (!this.insertItem(itemStack2, this.rows * 9, this.slotList.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.insertItem(itemStack2, 0, this.rows * 9, false)) {
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

   public Inventory getInventory() {
      return this.inventory;
   }

   @Environment(EnvType.CLIENT)
   public int getRows() {
      return this.rows;
   }
}
