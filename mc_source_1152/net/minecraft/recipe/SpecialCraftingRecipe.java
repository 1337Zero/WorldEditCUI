package net.minecraft.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public abstract class SpecialCraftingRecipe implements CraftingRecipe {
   private final Identifier id;

   public SpecialCraftingRecipe(Identifier id) {
      this.id = id;
   }

   public Identifier getId() {
      return this.id;
   }

   public boolean isIgnoredInRecipeBook() {
      return true;
   }

   public ItemStack getOutput() {
      return ItemStack.EMPTY;
   }
}
