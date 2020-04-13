package net.minecraft.village;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class SimpleTrader implements Trader {
   private final TraderInventory traderInventory;
   private final PlayerEntity player;
   private TraderOfferList recipeList = new TraderOfferList();
   private int experience;

   public SimpleTrader(PlayerEntity playerEntity) {
      this.player = playerEntity;
      this.traderInventory = new TraderInventory(this);
   }

   @Nullable
   public PlayerEntity getCurrentCustomer() {
      return this.player;
   }

   public void setCurrentCustomer(@Nullable PlayerEntity customer) {
   }

   public TraderOfferList getOffers() {
      return this.recipeList;
   }

   @Environment(EnvType.CLIENT)
   public void setOffersFromServer(@Nullable TraderOfferList traderOfferList) {
      this.recipeList = traderOfferList;
   }

   public void trade(TradeOffer tradeOffer) {
      tradeOffer.use();
   }

   public void onSellingItem(ItemStack itemStack) {
   }

   public World getTraderWorld() {
      return this.player.world;
   }

   public int getExperience() {
      return this.experience;
   }

   public void setExperienceFromServer(int experience) {
      this.experience = experience;
   }

   public boolean isLevelledTrader() {
      return true;
   }

   public SoundEvent getYesSound() {
      return SoundEvents.ENTITY_VILLAGER_YES;
   }
}
