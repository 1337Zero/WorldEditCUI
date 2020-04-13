package net.minecraft.inventory;

import java.util.Iterator;
import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.util.DefaultedList;

public class CraftingInventory implements Inventory, RecipeInputProvider {
   private final DefaultedList<ItemStack> stacks;
   private final int width;
   private final int height;
   private final Container container;

   public CraftingInventory(Container container, int width, int height) {
      this.stacks = DefaultedList.ofSize(width * height, ItemStack.EMPTY);
      this.container = container;
      this.width = width;
      this.height = height;
   }

   public int getInvSize() {
      return this.stacks.size();
   }

   public boolean isInvEmpty() {
      Iterator var1 = this.stacks.iterator();

      ItemStack itemStack;
      do {
         if (!var1.hasNext()) {
            return true;
         }

         itemStack = (ItemStack)var1.next();
      } while(itemStack.isEmpty());

      return false;
   }

   public ItemStack getInvStack(int slot) {
      return slot >= this.getInvSize() ? ItemStack.EMPTY : (ItemStack)this.stacks.get(slot);
   }

   public ItemStack removeInvStack(int slot) {
      return Inventories.removeStack(this.stacks, slot);
   }

   public ItemStack takeInvStack(int slot, int amount) {
      ItemStack itemStack = Inventories.splitStack(this.stacks, slot, amount);
      if (!itemStack.isEmpty()) {
         this.container.onContentChanged(this);
      }

      return itemStack;
   }

   public void setInvStack(int slot, ItemStack stack) {
      this.stacks.set(slot, stack);
      this.container.onContentChanged(this);
   }

   public void markDirty() {
   }

   public boolean canPlayerUseInv(PlayerEntity player) {
      return true;
   }

   public void clear() {
      this.stacks.clear();
   }

   public int getHeight() {
      return this.height;
   }

   public int getWidth() {
      return this.width;
   }

   public void provideRecipeInputs(RecipeFinder recipeFinder) {
      Iterator var2 = this.stacks.iterator();

      while(var2.hasNext()) {
         ItemStack itemStack = (ItemStack)var2.next();
         recipeFinder.addNormalItem(itemStack);
      }

   }
}
