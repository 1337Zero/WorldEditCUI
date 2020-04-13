package net.minecraft.advancement.criterion;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class KilledByCrossbowCriterion extends AbstractCriterion<KilledByCrossbowCriterion.Conditions> {
   private static final Identifier ID = new Identifier("killed_by_crossbow");

   public Identifier getId() {
      return ID;
   }

   public KilledByCrossbowCriterion.Conditions conditionsFromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
      EntityPredicate[] entityPredicates = EntityPredicate.fromJsonArray(jsonObject.get("victims"));
      NumberRange.IntRange intRange = NumberRange.IntRange.fromJson(jsonObject.get("unique_entity_types"));
      return new KilledByCrossbowCriterion.Conditions(entityPredicates, intRange);
   }

   public void trigger(ServerPlayerEntity player, Collection<Entity> victims, int amount) {
      this.test(player.getAdvancementTracker(), (conditions) -> {
         return conditions.matches(player, victims, amount);
      });
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final EntityPredicate[] victims;
      private final NumberRange.IntRange uniqueEntityTypes;

      public Conditions(EntityPredicate[] victims, NumberRange.IntRange intRange) {
         super(KilledByCrossbowCriterion.ID);
         this.victims = victims;
         this.uniqueEntityTypes = intRange;
      }

      public static KilledByCrossbowCriterion.Conditions create(EntityPredicate.Builder... builders) {
         EntityPredicate[] entityPredicates = new EntityPredicate[builders.length];

         for(int i = 0; i < builders.length; ++i) {
            EntityPredicate.Builder builder = builders[i];
            entityPredicates[i] = builder.build();
         }

         return new KilledByCrossbowCriterion.Conditions(entityPredicates, NumberRange.IntRange.ANY);
      }

      public static KilledByCrossbowCriterion.Conditions create(NumberRange.IntRange intRange) {
         EntityPredicate[] entityPredicates = new EntityPredicate[0];
         return new KilledByCrossbowCriterion.Conditions(entityPredicates, intRange);
      }

      public boolean matches(ServerPlayerEntity player, Collection<Entity> victims, int amount) {
         if (this.victims.length > 0) {
            List<Entity> list = Lists.newArrayList(victims);
            EntityPredicate[] var5 = this.victims;
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               EntityPredicate entityPredicate = var5[var7];
               boolean bl = false;
               Iterator iterator = list.iterator();

               while(iterator.hasNext()) {
                  Entity entity = (Entity)iterator.next();
                  if (entityPredicate.test(player, entity)) {
                     iterator.remove();
                     bl = true;
                     break;
                  }
               }

               if (!bl) {
                  return false;
               }
            }
         }

         if (this.uniqueEntityTypes == NumberRange.IntRange.ANY) {
            return true;
         } else {
            Set<EntityType<?>> set = Sets.newHashSet();
            Iterator var13 = victims.iterator();

            while(var13.hasNext()) {
               Entity entity2 = (Entity)var13.next();
               set.add(entity2.getType());
            }

            return this.uniqueEntityTypes.test(set.size()) && this.uniqueEntityTypes.test(amount);
         }
      }

      public JsonElement toJson() {
         JsonObject jsonObject = new JsonObject();
         jsonObject.add("victims", EntityPredicate.serializeAll(this.victims));
         jsonObject.add("unique_entity_types", this.uniqueEntityTypes.toJson());
         return jsonObject;
      }
   }
}
