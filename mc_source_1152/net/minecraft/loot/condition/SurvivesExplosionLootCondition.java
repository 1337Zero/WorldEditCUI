package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.Identifier;

public class SurvivesExplosionLootCondition implements LootCondition {
   private static final SurvivesExplosionLootCondition INSTANCE = new SurvivesExplosionLootCondition();

   private SurvivesExplosionLootCondition() {
   }

   public Set<LootContextParameter<?>> getRequiredParameters() {
      return ImmutableSet.of(LootContextParameters.EXPLOSION_RADIUS);
   }

   public boolean test(LootContext lootContext) {
      Float var2 = (Float)lootContext.get(LootContextParameters.EXPLOSION_RADIUS);
      if (var2 != null) {
         Random random = lootContext.getRandom();
         float f = 1.0F / var2;
         return random.nextFloat() <= f;
      } else {
         return true;
      }
   }

   public static LootCondition.Builder builder() {
      return () -> {
         return INSTANCE;
      };
   }

   public static class Factory extends LootCondition.Factory<SurvivesExplosionLootCondition> {
      protected Factory() {
         super(new Identifier("survives_explosion"), SurvivesExplosionLootCondition.class);
      }

      public void toJson(JsonObject jsonObject, SurvivesExplosionLootCondition survivesExplosionLootCondition, JsonSerializationContext jsonSerializationContext) {
      }

      public SurvivesExplosionLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         return SurvivesExplosionLootCondition.INSTANCE;
      }
   }
}
