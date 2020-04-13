package net.minecraft.container;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criterions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.server.network.ServerPlayerEntity;

public class BrewingStandContainer extends Container {
   private final Inventory inventory;
   private final PropertyDelegate propertyDelegate;
   private final Slot ingredientSlot;

   public BrewingStandContainer(int syncId, PlayerInventory playerInventory) {
      this(syncId, playerInventory, new BasicInventory(5), new ArrayPropertyDelegate(2));
   }

   public BrewingStandContainer(int i, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
      super(ContainerType.BREWING_STAND, i);
      checkContainerSize(inventory, 5);
      checkContainerDataCount(propertyDelegate, 2);
      this.inventory = inventory;
      this.propertyDelegate = propertyDelegate;
      this.addSlot(new BrewingStandContainer.SlotPotion(inventory, 0, 56, 51));
      this.addSlot(new BrewingStandContainer.SlotPotion(inventory, 1, 79, 58));
      this.addSlot(new BrewingStandContainer.SlotPotion(inventory, 2, 102, 51));
      this.ingredientSlot = this.addSlot(new BrewingStandContainer.SlotIngredient(inventory, 3, 79, 17));
      this.addSlot(new BrewingStandContainer.SlotFuel(inventory, 4, 17, 17));
      this.addProperties(propertyDelegate);

      int l;
      for(l = 0; l < 3; ++l) {
         for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k + l * 9 + 9, 8 + k * 18, 84 + l * 18));
         }
      }

      for(l = 0; l < 9; ++l) {
         this.addSlot(new Slot(playerInventory, l, 8 + l * 18, 142));
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
         if ((invSlot < 0 || invSlot > 2) && invSlot != 3 && invSlot != 4) {
            if (BrewingStandContainer.SlotFuel.matches(itemStack)) {
               if (this.insertItem(itemStack2, 4, 5, false) || this.ingredientSlot.canInsert(itemStack2) && !this.insertItem(itemStack2, 3, 4, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (this.ingredientSlot.canInsert(itemStack2)) {
               if (!this.insertItem(itemStack2, 3, 4, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (BrewingStandContainer.SlotPotion.matches(itemStack) && itemStack.getCount() == 1) {
               if (!this.insertItem(itemStack2, 0, 3, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (invSlot >= 5 && invSlot < 32) {
               if (!this.insertItem(itemStack2, 32, 41, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (invSlot >= 32 && invSlot < 41) {
               if (!this.insertItem(itemStack2, 5, 32, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.insertItem(itemStack2, 5, 41, false)) {
               return ItemStack.EMPTY;
            }
         } else {
            if (!this.insertItem(itemStack2, 5, 41, true)) {
               return ItemStack.EMPTY;
            }

            slot.onStackChanged(itemStack2, itemStack);
         }

         if (itemStack2.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
         } else {
            slot.markDirty();
         }

         if (itemStack2.getCount() == itemStack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTakeItem(player, itemStack2);
      }

      return itemStack;
   }

   @Environment(EnvType.CLIENT)
   public int getFuel() {
      return this.propertyDelegate.get(1);
   }

   @Environment(EnvType.CLIENT)
   public int getBrewTime() {
      return this.propertyDelegate.get(0);
   }

   static class SlotFuel extends Slot {
      public SlotFuel(Inventory invSlot, int xPosition, int yPosition, int i) {
         super(invSlot, xPosition, yPosition, i);
      }

      public boolean canInsert(ItemStack stack) {
         return matches(stack);
      }

      public static boolean matches(ItemStack itemStack) {
         return itemStack.getItem() == Items.BLAZE_POWDER;
      }

      public int getMaxStackAmount() {
         return 64;
      }
   }

   static class SlotIngredient extends Slot {
      public SlotIngredient(Inventory invSlot, int xPosition, int yPosition, int i) {
         super(invSlot, xPosition, yPosition, i);
      }

      public boolean canInsert(ItemStack stack) {
         return BrewingRecipeRegistry.isValidIngredient(stack);
      }

      public int getMaxStackAmount() {
         return 64;
      }
   }

   static class SlotPotion extends Slot {
      public SlotPotion(Inventory invSlot, int xPosition, int yPosition, int i) {
         super(invSlot, xPosition, yPosition, i);
      }

      public boolean canInsert(ItemStack stack) {
         return matches(stack);
      }

      public int getMaxStackAmount() {
         return 1;
      }

      public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
         Potion potion = PotionUtil.getPotion(stack);
         if (player instanceof ServerPlayerEntity) {
            Criterions.BREWED_POTION.trigger((ServerPlayerEntity)player, potion);
         }

         super.onTakeItem(player, stack);
         return stack;
      }

      public static boolean matches(ItemStack itemStack) {
         Item item = itemStack.getItem();
         return item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION || item == Items.GLASS_BOTTLE;
      }
   }
}
