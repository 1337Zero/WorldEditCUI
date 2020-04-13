package net.minecraft.container;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class CartographyTableContainer extends Container {
   private final BlockContext context;
   private boolean currentlyTakingItem;
   private long lastTakeResultTime;
   public final Inventory inventory;
   private final CraftingResultInventory resultSlot;

   public CartographyTableContainer(int syncId, PlayerInventory inventory) {
      this(syncId, inventory, BlockContext.EMPTY);
   }

   public CartographyTableContainer(int syncId, PlayerInventory inventory, final BlockContext context) {
      super(ContainerType.CARTOGRAPHY_TABLE, syncId);
      this.inventory = new BasicInventory(2) {
         public void markDirty() {
            CartographyTableContainer.this.onContentChanged(this);
            super.markDirty();
         }
      };
      this.resultSlot = new CraftingResultInventory() {
         public void markDirty() {
            CartographyTableContainer.this.onContentChanged(this);
            super.markDirty();
         }
      };
      this.context = context;
      this.addSlot(new Slot(this.inventory, 0, 15, 15) {
         public boolean canInsert(ItemStack stack) {
            return stack.getItem() == Items.FILLED_MAP;
         }
      });
      this.addSlot(new Slot(this.inventory, 1, 15, 52) {
         public boolean canInsert(ItemStack stack) {
            Item item = stack.getItem();
            return item == Items.PAPER || item == Items.MAP || item == Items.GLASS_PANE;
         }
      });
      this.addSlot(new Slot(this.resultSlot, 2, 145, 39) {
         public boolean canInsert(ItemStack stack) {
            return false;
         }

         public ItemStack takeStack(int amount) {
            ItemStack itemStack = super.takeStack(amount);
            ItemStack itemStack2 = (ItemStack)context.run((world, blockPos) -> {
               if (!CartographyTableContainer.this.currentlyTakingItem && CartographyTableContainer.this.inventory.getInvStack(1).getItem() == Items.GLASS_PANE) {
                  ItemStack itemStack2 = FilledMapItem.copyMap(world, CartographyTableContainer.this.inventory.getInvStack(0));
                  if (itemStack2 != null) {
                     itemStack2.setCount(1);
                     return itemStack2;
                  }
               }

               return itemStack;
            }).orElse(itemStack);
            CartographyTableContainer.this.inventory.takeInvStack(0, 1);
            CartographyTableContainer.this.inventory.takeInvStack(1, 1);
            return itemStack2;
         }

         protected void onCrafted(ItemStack stack, int amount) {
            this.takeStack(amount);
            super.onCrafted(stack, amount);
         }

         public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
            stack.getItem().onCraft(stack, player.world, player);
            context.run((world, blockPos) -> {
               long l = world.getTime();
               if (CartographyTableContainer.this.lastTakeResultTime != l) {
                  world.playSound((PlayerEntity)null, blockPos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                  CartographyTableContainer.this.lastTakeResultTime = l;
               }

            });
            return super.onTakeItem(player, stack);
         }
      });

      int k;
      for(k = 0; k < 3; ++k) {
         for(int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(inventory, j + k * 9 + 9, 8 + j * 18, 84 + k * 18));
         }
      }

      for(k = 0; k < 9; ++k) {
         this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
      }

   }

   public boolean canUse(PlayerEntity player) {
      return canUse(this.context, player, Blocks.CARTOGRAPHY_TABLE);
   }

   public void onContentChanged(Inventory inventory) {
      ItemStack itemStack = this.inventory.getInvStack(0);
      ItemStack itemStack2 = this.inventory.getInvStack(1);
      ItemStack itemStack3 = this.resultSlot.getInvStack(2);
      if (itemStack3.isEmpty() || !itemStack.isEmpty() && !itemStack2.isEmpty()) {
         if (!itemStack.isEmpty() && !itemStack2.isEmpty()) {
            this.updateResult(itemStack, itemStack2, itemStack3);
         }
      } else {
         this.resultSlot.removeInvStack(2);
      }

   }

   private void updateResult(ItemStack map, ItemStack item, ItemStack oldResult) {
      this.context.run((world, blockPos) -> {
         Item itemx = item.getItem();
         MapState mapState = FilledMapItem.getMapState(map, world);
         if (mapState != null) {
            ItemStack itemStack6;
            if (itemx == Items.PAPER && !mapState.locked && mapState.scale < 4) {
               itemStack6 = map.copy();
               itemStack6.setCount(1);
               itemStack6.getOrCreateTag().putInt("map_scale_direction", 1);
               this.sendContentUpdates();
            } else if (itemx == Items.GLASS_PANE && !mapState.locked) {
               itemStack6 = map.copy();
               itemStack6.setCount(1);
               this.sendContentUpdates();
            } else {
               if (itemx != Items.MAP) {
                  this.resultSlot.removeInvStack(2);
                  this.sendContentUpdates();
                  return;
               }

               itemStack6 = map.copy();
               itemStack6.setCount(2);
               this.sendContentUpdates();
            }

            if (!ItemStack.areEqualIgnoreDamage(itemStack6, oldResult)) {
               this.resultSlot.setInvStack(2, itemStack6);
               this.sendContentUpdates();
            }

         }
      });
   }

   public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
      return slot.inventory != this.resultSlot && super.canInsertIntoSlot(stack, slot);
   }

   public ItemStack transferSlot(PlayerEntity player, int invSlot) {
      ItemStack itemStack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slotList.get(invSlot);
      if (slot != null && slot.hasStack()) {
         ItemStack itemStack2 = slot.getStack();
         ItemStack itemStack3 = itemStack2;
         Item item = itemStack2.getItem();
         itemStack = itemStack2.copy();
         if (invSlot == 2) {
            if (this.inventory.getInvStack(1).getItem() == Items.GLASS_PANE) {
               itemStack3 = (ItemStack)this.context.run((world, blockPos) -> {
                  ItemStack itemStack2x = FilledMapItem.copyMap(world, this.inventory.getInvStack(0));
                  if (itemStack2x != null) {
                     itemStack2x.setCount(1);
                     return itemStack2x;
                  } else {
                     return itemStack2;
                  }
               }).orElse(itemStack2);
            }

            item.onCraft(itemStack3, player.world, player);
            if (!this.insertItem(itemStack3, 3, 39, true)) {
               return ItemStack.EMPTY;
            }

            slot.onStackChanged(itemStack3, itemStack);
         } else if (invSlot != 1 && invSlot != 0) {
            if (item == Items.FILLED_MAP) {
               if (!this.insertItem(itemStack2, 0, 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (item != Items.PAPER && item != Items.MAP && item != Items.GLASS_PANE) {
               if (invSlot >= 3 && invSlot < 30) {
                  if (!this.insertItem(itemStack2, 30, 39, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if (invSlot >= 30 && invSlot < 39 && !this.insertItem(itemStack2, 3, 30, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.insertItem(itemStack2, 1, 2, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.insertItem(itemStack2, 3, 39, false)) {
            return ItemStack.EMPTY;
         }

         if (itemStack3.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
         }

         slot.markDirty();
         if (itemStack3.getCount() == itemStack.getCount()) {
            return ItemStack.EMPTY;
         }

         this.currentlyTakingItem = true;
         slot.onTakeItem(player, itemStack3);
         this.currentlyTakingItem = false;
         this.sendContentUpdates();
      }

      return itemStack;
   }

   public void close(PlayerEntity player) {
      super.close(player);
      this.resultSlot.removeInvStack(2);
      this.context.run((world, blockPos) -> {
         this.dropInventory(player, player.world, this.inventory);
      });
   }
}
