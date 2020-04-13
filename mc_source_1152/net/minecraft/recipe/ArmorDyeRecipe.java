package net.minecraft.recipe;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ArmorDyeRecipe extends SpecialCraftingRecipe {
   public ArmorDyeRecipe(Identifier identifier) {
      super(identifier);
   }

   public boolean matches(CraftingInventory craftingInventory, World world) {
      ItemStack itemStack = ItemStack.EMPTY;
      List<ItemStack> list = Lists.newArrayList();

      for(int i = 0; i < craftingInventory.getInvSize(); ++i) {
         ItemStack itemStack2 = craftingInventory.getInvStack(i);
         if (!itemStack2.isEmpty()) {
            if (itemStack2.getItem() instanceof DyeableItem) {
               if (!itemStack.isEmpty()) {
                  return false;
               }

               itemStack = itemStack2;
            } else {
               if (!(itemStack2.getItem() instanceof DyeItem)) {
                  return false;
               }

               list.add(itemStack2);
            }
         }
      }

      return !itemStack.isEmpty() && !list.isEmpty();
   }

   public ItemStack craft(CraftingInventory craftingInventory) {
      List<DyeItem> list = Lists.newArrayList();
      ItemStack itemStack = ItemStack.EMPTY;

      for(int i = 0; i < craftingInventory.getInvSize(); ++i) {
         ItemStack itemStack2 = craftingInventory.getInvStack(i);
         if (!itemStack2.isEmpty()) {
            Item item = itemStack2.getItem();
            if (item instanceof DyeableItem) {
               if (!itemStack.isEmpty()) {
                  return ItemStack.EMPTY;
               }

               itemStack = itemStack2.copy();
            } else {
               if (!(item instanceof DyeItem)) {
                  return ItemStack.EMPTY;
               }

               list.add((DyeItem)item);
            }
         }
      }

      if (!itemStack.isEmpty() && !list.isEmpty()) {
         return DyeableItem.blendAndSetColor(itemStack, list);
      } else {
         return ItemStack.EMPTY;
      }
   }

   @Environment(EnvType.CLIENT)
   public boolean fits(int width, int height) {
      return width * height >= 2;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.ARMOR_DYE;
   }
}
