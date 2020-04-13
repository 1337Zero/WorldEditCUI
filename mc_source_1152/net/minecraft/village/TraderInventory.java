package net.minecraft.village;

import java.util.Iterator;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;

public class TraderInventory implements Inventory {
   private final Trader trader;
   private final DefaultedList<ItemStack> inventory;
   @Nullable
   private TradeOffer traderRecipe;
   private int recipeIndex;
   private int traderRewardedExperience;

   public TraderInventory(Trader trader) {
      this.inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
      this.trader = trader;
   }

   public int getInvSize() {
      return this.inventory.size();
   }

   public boolean isInvEmpty() {
      Iterator var1 = this.inventory.iterator();

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
      return (ItemStack)this.inventory.get(slot);
   }

   public ItemStack takeInvStack(int slot, int amount) {
      ItemStack itemStack = (ItemStack)this.inventory.get(slot);
      if (slot == 2 && !itemStack.isEmpty()) {
         return Inventories.splitStack(this.inventory, slot, itemStack.getCount());
      } else {
         ItemStack itemStack2 = Inventories.splitStack(this.inventory, slot, amount);
         if (!itemStack2.isEmpty() && this.needRecipeUpdate(slot)) {
            this.updateRecipes();
         }

         return itemStack2;
      }
   }

   private boolean needRecipeUpdate(int slot) {
      return slot == 0 || slot == 1;
   }

   public ItemStack removeInvStack(int slot) {
      return Inventories.removeStack(this.inventory, slot);
   }

   public void setInvStack(int slot, ItemStack stack) {
      this.inventory.set(slot, stack);
      if (!stack.isEmpty() && stack.getCount() > this.getInvMaxStackAmount()) {
         stack.setCount(this.getInvMaxStackAmount());
      }

      if (this.needRecipeUpdate(slot)) {
         this.updateRecipes();
      }

   }

   public boolean canPlayerUseInv(PlayerEntity player) {
      return this.trader.getCurrentCustomer() == player;
   }

   public void markDirty() {
      this.updateRecipes();
   }

   public void updateRecipes() {
      this.traderRecipe = null;
      ItemStack itemStack3;
      ItemStack itemStack4;
      if (((ItemStack)this.inventory.get(0)).isEmpty()) {
         itemStack3 = (ItemStack)this.inventory.get(1);
         itemStack4 = ItemStack.EMPTY;
      } else {
         itemStack3 = (ItemStack)this.inventory.get(0);
         itemStack4 = (ItemStack)this.inventory.get(1);
      }

      if (itemStack3.isEmpty()) {
         this.setInvStack(2, ItemStack.EMPTY);
         this.traderRewardedExperience = 0;
      } else {
         TraderOfferList traderOfferList = this.trader.getOffers();
         if (!traderOfferList.isEmpty()) {
            TradeOffer tradeOffer = traderOfferList.getValidRecipe(itemStack3, itemStack4, this.recipeIndex);
            if (tradeOffer == null || tradeOffer.isDisabled()) {
               this.traderRecipe = tradeOffer;
               tradeOffer = traderOfferList.getValidRecipe(itemStack4, itemStack3, this.recipeIndex);
            }

            if (tradeOffer != null && !tradeOffer.isDisabled()) {
               this.traderRecipe = tradeOffer;
               this.setInvStack(2, tradeOffer.getSellItem());
               this.traderRewardedExperience = tradeOffer.getTraderExperience();
            } else {
               this.setInvStack(2, ItemStack.EMPTY);
               this.traderRewardedExperience = 0;
            }
         }

         this.trader.onSellingItem(this.getInvStack(2));
      }
   }

   @Nullable
   public TradeOffer getTradeOffer() {
      return this.traderRecipe;
   }

   public void setRecipeIndex(int index) {
      this.recipeIndex = index;
      this.updateRecipes();
   }

   public void clear() {
      this.inventory.clear();
   }

   @Environment(EnvType.CLIENT)
   public int getTraderRewardedExperience() {
      return this.traderRewardedExperience;
   }
}
