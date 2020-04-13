package net.minecraft.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class DoubleInventory implements Inventory {
   private final Inventory first;
   private final Inventory second;

   public DoubleInventory(Inventory first, Inventory second) {
      if (first == null) {
         first = second;
      }

      if (second == null) {
         second = first;
      }

      this.first = first;
      this.second = second;
   }

   public int getInvSize() {
      return this.first.getInvSize() + this.second.getInvSize();
   }

   public boolean isInvEmpty() {
      return this.first.isInvEmpty() && this.second.isInvEmpty();
   }

   public boolean isPart(Inventory inventory) {
      return this.first == inventory || this.second == inventory;
   }

   public ItemStack getInvStack(int slot) {
      return slot >= this.first.getInvSize() ? this.second.getInvStack(slot - this.first.getInvSize()) : this.first.getInvStack(slot);
   }

   public ItemStack takeInvStack(int slot, int amount) {
      return slot >= this.first.getInvSize() ? this.second.takeInvStack(slot - this.first.getInvSize(), amount) : this.first.takeInvStack(slot, amount);
   }

   public ItemStack removeInvStack(int slot) {
      return slot >= this.first.getInvSize() ? this.second.removeInvStack(slot - this.first.getInvSize()) : this.first.removeInvStack(slot);
   }

   public void setInvStack(int slot, ItemStack stack) {
      if (slot >= this.first.getInvSize()) {
         this.second.setInvStack(slot - this.first.getInvSize(), stack);
      } else {
         this.first.setInvStack(slot, stack);
      }

   }

   public int getInvMaxStackAmount() {
      return this.first.getInvMaxStackAmount();
   }

   public void markDirty() {
      this.first.markDirty();
      this.second.markDirty();
   }

   public boolean canPlayerUseInv(PlayerEntity player) {
      return this.first.canPlayerUseInv(player) && this.second.canPlayerUseInv(player);
   }

   public void onInvOpen(PlayerEntity player) {
      this.first.onInvOpen(player);
      this.second.onInvOpen(player);
   }

   public void onInvClose(PlayerEntity player) {
      this.first.onInvClose(player);
      this.second.onInvClose(player);
   }

   public boolean isValidInvStack(int slot, ItemStack stack) {
      return slot >= this.first.getInvSize() ? this.second.isValidInvStack(slot - this.first.getInvSize(), stack) : this.first.isValidInvStack(slot, stack);
   }

   public void clear() {
      this.first.clear();
      this.second.clear();
   }
}
