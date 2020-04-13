package net.minecraft.advancement.criterion;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class FishingRodHookedCriterion extends AbstractCriterion<FishingRodHookedCriterion.Conditions> {
   private static final Identifier ID = new Identifier("fishing_rod_hooked");

   public Identifier getId() {
      return ID;
   }

   public FishingRodHookedCriterion.Conditions conditionsFromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
      ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("rod"));
      EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("entity"));
      ItemPredicate itemPredicate2 = ItemPredicate.fromJson(jsonObject.get("item"));
      return new FishingRodHookedCriterion.Conditions(itemPredicate, entityPredicate, itemPredicate2);
   }

   public void trigger(ServerPlayerEntity player, ItemStack rodStack, FishingBobberEntity bobber, Collection<ItemStack> fishingLoots) {
      this.test(player.getAdvancementTracker(), (conditions) -> {
         return conditions.matches(player, rodStack, bobber, fishingLoots);
      });
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final ItemPredicate rod;
      private final EntityPredicate hookedEntity;
      private final ItemPredicate caughtItem;

      public Conditions(ItemPredicate rod, EntityPredicate bobber, ItemPredicate item) {
         super(FishingRodHookedCriterion.ID);
         this.rod = rod;
         this.hookedEntity = bobber;
         this.caughtItem = item;
      }

      public static FishingRodHookedCriterion.Conditions create(ItemPredicate rod, EntityPredicate bobber, ItemPredicate item) {
         return new FishingRodHookedCriterion.Conditions(rod, bobber, item);
      }

      public boolean matches(ServerPlayerEntity player, ItemStack rodStack, FishingBobberEntity bobber, Collection<ItemStack> fishingLoots) {
         if (!this.rod.test(rodStack)) {
            return false;
         } else if (!this.hookedEntity.test(player, bobber.hookedEntity)) {
            return false;
         } else {
            if (this.caughtItem != ItemPredicate.ANY) {
               boolean bl = false;
               if (bobber.hookedEntity instanceof ItemEntity) {
                  ItemEntity itemEntity = (ItemEntity)bobber.hookedEntity;
                  if (this.caughtItem.test(itemEntity.getStack())) {
                     bl = true;
                  }
               }

               Iterator var8 = fishingLoots.iterator();

               while(var8.hasNext()) {
                  ItemStack itemStack = (ItemStack)var8.next();
                  if (this.caughtItem.test(itemStack)) {
                     bl = true;
                     break;
                  }
               }

               if (!bl) {
                  return false;
               }
            }

            return true;
         }
      }

      public JsonElement toJson() {
         JsonObject jsonObject = new JsonObject();
         jsonObject.add("rod", this.rod.toJson());
         jsonObject.add("entity", this.hookedEntity.serialize());
         jsonObject.add("item", this.caughtItem.toJson());
         return jsonObject;
      }
   }
}
