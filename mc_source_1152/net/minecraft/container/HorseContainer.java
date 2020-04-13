package net.minecraft.container;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class HorseContainer extends Container {
   private final Inventory playerInv;
   private final HorseBaseEntity entity;

   public HorseContainer(int syncId, PlayerInventory playerInventory, Inventory inventory, final HorseBaseEntity horseBaseEntity) {
      super((ContainerType)null, syncId);
      this.playerInv = inventory;
      this.entity = horseBaseEntity;
      int i = true;
      inventory.onInvOpen(playerInventory.player);
      int j = true;
      this.addSlot(new Slot(inventory, 0, 8, 18) {
         public boolean canInsert(ItemStack stack) {
            return stack.getItem() == Items.SADDLE && !this.hasStack() && horseBaseEntity.canBeSaddled();
         }

         @Environment(EnvType.CLIENT)
         public boolean doDrawHoveringEffect() {
            return horseBaseEntity.canBeSaddled();
         }
      });
      this.addSlot(new Slot(inventory, 1, 8, 36) {
         public boolean canInsert(ItemStack stack) {
            return horseBaseEntity.canEquip(stack);
         }

         @Environment(EnvType.CLIENT)
         public boolean doDrawHoveringEffect() {
            return horseBaseEntity.canEquip();
         }

         public int getMaxStackAmount() {
            return 1;
         }
      });
      int o;
      int n;
      if (horseBaseEntity instanceof AbstractDonkeyEntity && ((AbstractDonkeyEntity)horseBaseEntity).hasChest()) {
         for(o = 0; o < 3; ++o) {
            for(n = 0; n < ((AbstractDonkeyEntity)horseBaseEntity).method_6702(); ++n) {
               this.addSlot(new Slot(inventory, 2 + n + o * ((AbstractDonkeyEntity)horseBaseEntity).method_6702(), 80 + n * 18, 18 + o * 18));
            }
         }
      }

      for(o = 0; o < 3; ++o) {
         for(n = 0; n < 9; ++n) {
            this.addSlot(new Slot(playerInventory, n + o * 9 + 9, 8 + n * 18, 102 + o * 18 + -18));
         }
      }

      for(o = 0; o < 9; ++o) {
         this.addSlot(new Slot(playerInventory, o, 8 + o * 18, 142));
      }

   }

   public boolean canUse(PlayerEntity player) {
      return this.playerInv.canPlayerUseInv(player) && this.entity.isAlive() && this.entity.distanceTo(player) < 8.0F;
   }

   public ItemStack transferSlot(PlayerEntity player, int invSlot) {
      ItemStack itemStack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slotList.get(invSlot);
      if (slot != null && slot.hasStack()) {
         ItemStack itemStack2 = slot.getStack();
         itemStack = itemStack2.copy();
         int i = this.playerInv.getInvSize();
         if (invSlot < i) {
            if (!this.insertItem(itemStack2, i, this.slotList.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (this.getSlot(1).canInsert(itemStack2) && !this.getSlot(1).hasStack()) {
            if (!this.insertItem(itemStack2, 1, 2, false)) {
               return ItemStack.EMPTY;
            }
         } else if (this.getSlot(0).canInsert(itemStack2)) {
            if (!this.insertItem(itemStack2, 0, 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (i <= 2 || !this.insertItem(itemStack2, 2, i, false)) {
            int k = i + 27;
            int m = k + 9;
            if (invSlot >= k && invSlot < m) {
               if (!this.insertItem(itemStack2, i, k, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (invSlot >= i && invSlot < k) {
               if (!this.insertItem(itemStack2, k, m, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.insertItem(itemStack2, k, k, false)) {
               return ItemStack.EMPTY;
            }

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
      this.playerInv.onInvClose(player);
   }
}
