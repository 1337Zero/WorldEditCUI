package net.minecraft.advancement.criterion;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ChanneledLightningCriterion extends AbstractCriterion<ChanneledLightningCriterion.Conditions> {
   private static final Identifier ID = new Identifier("channeled_lightning");

   public Identifier getId() {
      return ID;
   }

   public ChanneledLightningCriterion.Conditions conditionsFromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
      EntityPredicate[] entityPredicates = EntityPredicate.fromJsonArray(jsonObject.get("victims"));
      return new ChanneledLightningCriterion.Conditions(entityPredicates);
   }

   public void trigger(ServerPlayerEntity player, Collection<? extends Entity> victims) {
      this.test(player.getAdvancementTracker(), (conditions) -> {
         return conditions.matches(player, victims);
      });
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final EntityPredicate[] victims;

      public Conditions(EntityPredicate[] victims) {
         super(ChanneledLightningCriterion.ID);
         this.victims = victims;
      }

      public static ChanneledLightningCriterion.Conditions create(EntityPredicate... victims) {
         return new ChanneledLightningCriterion.Conditions(victims);
      }

      public boolean matches(ServerPlayerEntity player, Collection<? extends Entity> victims) {
         EntityPredicate[] var3 = this.victims;
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            EntityPredicate entityPredicate = var3[var5];
            boolean bl = false;
            Iterator var8 = victims.iterator();

            while(var8.hasNext()) {
               Entity entity = (Entity)var8.next();
               if (entityPredicate.test(player, entity)) {
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

      public JsonElement toJson() {
         JsonObject jsonObject = new JsonObject();
         jsonObject.add("victims", EntityPredicate.serializeAll(this.victims));
         return jsonObject;
      }
   }
}
