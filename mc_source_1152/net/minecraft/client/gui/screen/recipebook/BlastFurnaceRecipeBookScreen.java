package net.minecraft.client.gui.screen.recipebook;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.Item;

@Environment(EnvType.CLIENT)
public class BlastFurnaceRecipeBookScreen extends AbstractFurnaceRecipeBookScreen {
   protected boolean isFilteringCraftable() {
      return this.recipeBook.isBlastFurnaceFilteringCraftable();
   }

   protected void setFilteringCraftable(boolean filteringCraftable) {
      this.recipeBook.setBlastFurnaceFilteringCraftable(filteringCraftable);
   }

   protected boolean isGuiOpen() {
      return this.recipeBook.isBlastFurnaceGuiOpen();
   }

   protected void setGuiOpen(boolean opened) {
      this.recipeBook.setBlastFurnaceGuiOpen(opened);
   }

   protected String getToggleCraftableButtonText() {
      return "gui.recipebook.toggleRecipes.blastable";
   }

   protected Set<Item> getAllowedFuels() {
      return AbstractFurnaceBlockEntity.createFuelTimeMap().keySet();
   }
}
