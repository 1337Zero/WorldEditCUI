package net.minecraft.advancement.criterion;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.passive.AbstractTraderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class VillagerTradeCriterion extends AbstractCriterion<VillagerTradeCriterion.Conditions> {
   private static final Identifier ID = new Identifier("villager_trade");

   public Identifier getId() {
      return ID;
   }

   public VillagerTradeCriterion.Conditions conditionsFromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
      EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("villager"));
      ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
      return new VillagerTradeCriterion.Conditions(entityPredicate, itemPredicate);
   }

   public void handle(ServerPlayerEntity player, AbstractTraderEntity trader, ItemStack stack) {
      this.test(player.getAdvancementTracker(), (conditions) -> {
         return conditions.matches(player, trader, stack);
      });
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final EntityPredicate villager;
      private final ItemPredicate item;

      public Conditions(EntityPredicate entity, ItemPredicate item) {
         super(VillagerTradeCriterion.ID);
         this.villager = entity;
         this.item = item;
      }

      public static VillagerTradeCriterion.Conditions any() {
         return new VillagerTradeCriterion.Conditions(EntityPredicate.ANY, ItemPredicate.ANY);
      }

      public boolean matches(ServerPlayerEntity player, AbstractTraderEntity trader, ItemStack stack) {
         if (!this.villager.test(player, trader)) {
            return false;
         } else {
            return this.item.test(stack);
         }
      }

      public JsonElement toJson() {
         JsonObject jsonObject = new JsonObject();
         jsonObject.add("item", this.item.toJson());
         jsonObject.add("villager", this.villager.serialize());
         return jsonObject;
      }
   }
}
