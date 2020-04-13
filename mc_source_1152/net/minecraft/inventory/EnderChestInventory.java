package net.minecraft.inventory;

import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class EnderChestInventory extends BasicInventory {
   private EnderChestBlockEntity currentBlockEntity;

   public EnderChestInventory() {
      super(27);
   }

   public void setCurrentBlockEntity(EnderChestBlockEntity enderChestBlockEntity) {
      this.currentBlockEntity = enderChestBlockEntity;
   }

   public void readTags(ListTag listTag) {
      int j;
      for(j = 0; j < this.getInvSize(); ++j) {
         this.setInvStack(j, ItemStack.EMPTY);
      }

      for(j = 0; j < listTag.size(); ++j) {
         CompoundTag compoundTag = listTag.getCompound(j);
         int k = compoundTag.getByte("Slot") & 255;
         if (k >= 0 && k < this.getInvSize()) {
            this.setInvStack(k, ItemStack.fromTag(compoundTag));
         }
      }

   }

   public ListTag getTags() {
      ListTag listTag = new ListTag();

      for(int i = 0; i < this.getInvSize(); ++i) {
         ItemStack itemStack = this.getInvStack(i);
         if (!itemStack.isEmpty()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putByte("Slot", (byte)i);
            itemStack.toTag(compoundTag);
            listTag.add(compoundTag);
         }
      }

      return listTag;
   }

   public boolean canPlayerUseInv(PlayerEntity player) {
      return this.currentBlockEntity != null && !this.currentBlockEntity.canPlayerUse(player) ? false : super.canPlayerUseInv(player);
   }

   public void onInvOpen(PlayerEntity player) {
      if (this.currentBlockEntity != null) {
         this.currentBlockEntity.onOpen();
      }

      super.onInvOpen(player);
   }

   public void onInvClose(PlayerEntity player) {
      if (this.currentBlockEntity != null) {
         this.currentBlockEntity.onClose();
      }

      super.onInvClose(player);
      this.currentBlockEntity = null;
   }
}
