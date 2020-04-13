package net.minecraft.advancement.criterion;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.DistancePredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class LevitationCriterion extends AbstractCriterion<LevitationCriterion.Conditions> {
   private static final Identifier ID = new Identifier("levitation");

   public Identifier getId() {
      return ID;
   }

   public LevitationCriterion.Conditions conditionsFromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
      DistancePredicate distancePredicate = DistancePredicate.deserialize(jsonObject.get("distance"));
      NumberRange.IntRange intRange = NumberRange.IntRange.fromJson(jsonObject.get("duration"));
      return new LevitationCriterion.Conditions(distancePredicate, intRange);
   }

   public void trigger(ServerPlayerEntity player, Vec3d startPos, int duration) {
      this.test(player.getAdvancementTracker(), (conditions) -> {
         return conditions.matches(player, startPos, duration);
      });
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final DistancePredicate distance;
      private final NumberRange.IntRange duration;

      public Conditions(DistancePredicate distance, NumberRange.IntRange intRange) {
         super(LevitationCriterion.ID);
         this.distance = distance;
         this.duration = intRange;
      }

      public static LevitationCriterion.Conditions create(DistancePredicate distance) {
         return new LevitationCriterion.Conditions(distance, NumberRange.IntRange.ANY);
      }

      public boolean matches(ServerPlayerEntity player, Vec3d startPos, int duration) {
         if (!this.distance.test(startPos.x, startPos.y, startPos.z, player.getX(), player.getY(), player.getZ())) {
            return false;
         } else {
            return this.duration.test(duration);
         }
      }

      public JsonElement toJson() {
         JsonObject jsonObject = new JsonObject();
         jsonObject.add("distance", this.distance.serialize());
         jsonObject.add("duration", this.duration.toJson());
         return jsonObject;
      }
   }
}
