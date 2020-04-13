package net.minecraft.inventory;

import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.util.DefaultedList;

public class CraftingResultInventory implements Inventory, RecipeUnlocker {
   private final DefaultedList<ItemStack> stack;
   private Recipe<?> lastRecipe;

   public CraftingResultInventory() {
      this.stack = DefaultedList.ofSize(1, ItemStack.EMPTY);
   }

   public int getInvSize() {
      return 1;
   }

   public boolean isInvEmpty() {
      Iterator var1 = this.stack.iterator();

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
      return (ItemStack)this.stack.get(0);
   }

   public ItemStack takeInvStack(int slot, int amount) {
      return Inventories.removeStack(this.stack, 0);
   }

   public ItemStack removeInvStack(int slot) {
      return Inventories.removeStack(this.stack, 0);
   }

   public void setInvStack(int slot, ItemStack stack) {
      this.stack.set(0, stack);
   }

   public void markDirty() {
   }

   public boolean canPlayerUseInv(PlayerEntity player) {
      return true;
   }

   public void clear() {
      this.stack.clear();
   }

   public void setLastRecipe(@Nullable Recipe<?> recipe) {
      this.lastRecipe = recipe;
   }

   @Nullable
   public Recipe<?> getLastRecipe() {
      return this.lastRecipe;
   }
}
