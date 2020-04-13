package net.minecraft.container;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class LecternContainer extends Container {
   private final Inventory inventory;
   private final PropertyDelegate propertyDelegate;

   public LecternContainer(int syncId) {
      this(syncId, new BasicInventory(1), new ArrayPropertyDelegate(1));
   }

   public LecternContainer(int syncId, Inventory inventory, PropertyDelegate propertyDelegate) {
      super(ContainerType.LECTERN, syncId);
      checkContainerSize(inventory, 1);
      checkContainerDataCount(propertyDelegate, 1);
      this.inventory = inventory;
      this.propertyDelegate = propertyDelegate;
      this.addSlot(new Slot(inventory, 0, 0, 0) {
         public void markDirty() {
            super.markDirty();
            LecternContainer.this.onContentChanged(this.inventory);
         }
      });
      this.addProperties(propertyDelegate);
   }

   public boolean onButtonClick(PlayerEntity player, int id) {
      int j;
      if (id >= 100) {
         j = id - 100;
         this.setProperties(0, j);
         return true;
      } else {
         switch(id) {
         case 1:
            j = this.propertyDelegate.get(0);
            this.setProperties(0, j - 1);
            return true;
         case 2:
            j = this.propertyDelegate.get(0);
            this.setProperties(0, j + 1);
            return true;
         case 3:
            if (!player.canModifyWorld()) {
               return false;
            }

            ItemStack itemStack = this.inventory.removeInvStack(0);
            this.inventory.markDirty();
            if (!player.inventory.insertStack(itemStack)) {
               player.dropItem(itemStack, false);
            }

            return true;
         default:
            return false;
         }
      }
   }

   public void setProperties(int pos, int propertyId) {
      super.setProperties(pos, propertyId);
      this.sendContentUpdates();
   }

   public boolean canUse(PlayerEntity player) {
      return this.inventory.canPlayerUseInv(player);
   }

   @Environment(EnvType.CLIENT)
   public ItemStack getBookItem() {
      return this.inventory.getInvStack(0);
   }

   @Environment(EnvType.CLIENT)
   public int getPage() {
      return this.propertyDelegate.get(0);
   }
}
