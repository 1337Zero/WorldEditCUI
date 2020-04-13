package net.minecraft.client.gui.screen.recipebook;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.Item;

@Environment(EnvType.CLIENT)
public class SmokerRecipeBookScreen extends AbstractFurnaceRecipeBookScreen {
   protected boolean isFilteringCraftable() {
      return this.recipeBook.isSmokerFilteringCraftable();
   }

   protected void setFilteringCraftable(boolean filteringCraftable) {
      this.recipeBook.setSmokerFilteringCraftable(filteringCraftable);
   }

   protected boolean isGuiOpen() {
      return this.recipeBook.isSmokerGuiOpen();
   }

   protected void setGuiOpen(boolean opened) {
      this.recipeBook.setSmokerGuiOpen(opened);
   }

   protected String getToggleCraftableButtonText() {
      return "gui.recipebook.toggleRecipes.smokable";
   }

   protected Set<Item> getAllowedFuels() {
      return AbstractFurnaceBlockEntity.createFuelTimeMap().keySet();
   }
}
