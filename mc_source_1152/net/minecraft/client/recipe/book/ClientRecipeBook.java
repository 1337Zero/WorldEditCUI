package net.minecraft.client.recipe.book;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.container.BlastFurnaceContainer;
import net.minecraft.container.CraftingContainer;
import net.minecraft.container.CraftingTableContainer;
import net.minecraft.container.FurnaceContainer;
import net.minecraft.container.PlayerContainer;
import net.minecraft.container.SmokerContainer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBook;

@Environment(EnvType.CLIENT)
public class ClientRecipeBook extends RecipeBook {
   private final RecipeManager manager;
   private final Map<RecipeBookGroup, List<RecipeResultCollection>> resultsByGroup = Maps.newHashMap();
   private final List<RecipeResultCollection> orderedResults = Lists.newArrayList();

   public ClientRecipeBook(RecipeManager manager) {
      this.manager = manager;
   }

   public void reload() {
      this.orderedResults.clear();
      this.resultsByGroup.clear();
      Table<RecipeBookGroup, String, RecipeResultCollection> table = HashBasedTable.create();
      Iterator var2 = this.manager.values().iterator();

      while(var2.hasNext()) {
         Recipe<?> recipe = (Recipe)var2.next();
         if (!recipe.isIgnoredInRecipeBook()) {
            RecipeBookGroup recipeBookGroup = getGroupForRecipe(recipe);
            String string = recipe.getGroup();
            RecipeResultCollection recipeResultCollection2;
            if (string.isEmpty()) {
               recipeResultCollection2 = this.addGroup(recipeBookGroup);
            } else {
               recipeResultCollection2 = (RecipeResultCollection)table.get(recipeBookGroup, string);
               if (recipeResultCollection2 == null) {
                  recipeResultCollection2 = this.addGroup(recipeBookGroup);
                  table.put(recipeBookGroup, string, recipeResultCollection2);
               }
            }

            recipeResultCollection2.addRecipe(recipe);
         }
      }

   }

   private RecipeResultCollection addGroup(RecipeBookGroup group) {
      RecipeResultCollection recipeResultCollection = new RecipeResultCollection();
      this.orderedResults.add(recipeResultCollection);
      ((List)this.resultsByGroup.computeIfAbsent(group, (recipeBookGroup) -> {
         return Lists.newArrayList();
      })).add(recipeResultCollection);
      if (group != RecipeBookGroup.FURNACE_BLOCKS && group != RecipeBookGroup.FURNACE_FOOD && group != RecipeBookGroup.FURNACE_MISC) {
         if (group != RecipeBookGroup.BLAST_FURNACE_BLOCKS && group != RecipeBookGroup.BLAST_FURNACE_MISC) {
            if (group == RecipeBookGroup.SMOKER_FOOD) {
               this.addGroupResults(RecipeBookGroup.SMOKER_SEARCH, recipeResultCollection);
            } else if (group == RecipeBookGroup.STONECUTTER) {
               this.addGroupResults(RecipeBookGroup.STONECUTTER, recipeResultCollection);
            } else if (group == RecipeBookGroup.CAMPFIRE) {
               this.addGroupResults(RecipeBookGroup.CAMPFIRE, recipeResultCollection);
            } else {
               this.addGroupResults(RecipeBookGroup.SEARCH, recipeResultCollection);
            }
         } else {
            this.addGroupResults(RecipeBookGroup.BLAST_FURNACE_SEARCH, recipeResultCollection);
         }
      } else {
         this.addGroupResults(RecipeBookGroup.FURNACE_SEARCH, recipeResultCollection);
      }

      return recipeResultCollection;
   }

   private void addGroupResults(RecipeBookGroup group, RecipeResultCollection results) {
      ((List)this.resultsByGroup.computeIfAbsent(group, (recipeBookGroup) -> {
         return Lists.newArrayList();
      })).add(results);
   }

   private static RecipeBookGroup getGroupForRecipe(Recipe<?> recipe) {
      RecipeType<?> recipeType = recipe.getType();
      if (recipeType == RecipeType.SMELTING) {
         if (recipe.getOutput().getItem().isFood()) {
            return RecipeBookGroup.FURNACE_FOOD;
         } else {
            return recipe.getOutput().getItem() instanceof BlockItem ? RecipeBookGroup.FURNACE_BLOCKS : RecipeBookGroup.FURNACE_MISC;
         }
      } else if (recipeType == RecipeType.BLASTING) {
         return recipe.getOutput().getItem() instanceof BlockItem ? RecipeBookGroup.BLAST_FURNACE_BLOCKS : RecipeBookGroup.BLAST_FURNACE_MISC;
      } else if (recipeType == RecipeType.SMOKING) {
         return RecipeBookGroup.SMOKER_FOOD;
      } else if (recipeType == RecipeType.STONECUTTING) {
         return RecipeBookGroup.STONECUTTER;
      } else if (recipeType == RecipeType.CAMPFIRE_COOKING) {
         return RecipeBookGroup.CAMPFIRE;
      } else {
         ItemStack itemStack = recipe.getOutput();
         ItemGroup itemGroup = itemStack.getItem().getGroup();
         if (itemGroup == ItemGroup.BUILDING_BLOCKS) {
            return RecipeBookGroup.BUILDING_BLOCKS;
         } else if (itemGroup != ItemGroup.TOOLS && itemGroup != ItemGroup.COMBAT) {
            return itemGroup == ItemGroup.REDSTONE ? RecipeBookGroup.REDSTONE : RecipeBookGroup.MISC;
         } else {
            return RecipeBookGroup.EQUIPMENT;
         }
      }
   }

   public static List<RecipeBookGroup> getGroupsForContainer(CraftingContainer<?> container) {
      if (!(container instanceof CraftingTableContainer) && !(container instanceof PlayerContainer)) {
         if (container instanceof FurnaceContainer) {
            return Lists.newArrayList(new RecipeBookGroup[]{RecipeBookGroup.FURNACE_SEARCH, RecipeBookGroup.FURNACE_FOOD, RecipeBookGroup.FURNACE_BLOCKS, RecipeBookGroup.FURNACE_MISC});
         } else if (container instanceof BlastFurnaceContainer) {
            return Lists.newArrayList(new RecipeBookGroup[]{RecipeBookGroup.BLAST_FURNACE_SEARCH, RecipeBookGroup.BLAST_FURNACE_BLOCKS, RecipeBookGroup.BLAST_FURNACE_MISC});
         } else {
            return container instanceof SmokerContainer ? Lists.newArrayList(new RecipeBookGroup[]{RecipeBookGroup.SMOKER_SEARCH, RecipeBookGroup.SMOKER_FOOD}) : Lists.newArrayList();
         }
      } else {
         return Lists.newArrayList(new RecipeBookGroup[]{RecipeBookGroup.SEARCH, RecipeBookGroup.EQUIPMENT, RecipeBookGroup.BUILDING_BLOCKS, RecipeBookGroup.MISC, RecipeBookGroup.REDSTONE});
      }
   }

   public List<RecipeResultCollection> getOrderedResults() {
      return this.orderedResults;
   }

   public List<RecipeResultCollection> getResultsForGroup(RecipeBookGroup category) {
      return (List)this.resultsByGroup.getOrDefault(category, Collections.emptyList());
   }
}
