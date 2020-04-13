package net.minecraft.loot.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class InvertedLootCondition implements LootCondition {
   private final LootCondition term;

   private InvertedLootCondition(LootCondition term) {
      this.term = term;
   }

   public final boolean test(LootContext lootContext) {
      return !this.term.test(lootContext);
   }

   public Set<LootContextParameter<?>> getRequiredParameters() {
      return this.term.getRequiredParameters();
   }

   public void check(LootTableReporter reporter) {
      LootCondition.super.check(reporter);
      this.term.check(reporter);
   }

   public static LootCondition.Builder builder(LootCondition.Builder term) {
      InvertedLootCondition invertedLootCondition = new InvertedLootCondition(term.build());
      return () -> {
         return invertedLootCondition;
      };
   }

   public static class Factory extends LootCondition.Factory<InvertedLootCondition> {
      public Factory() {
         super(new Identifier("inverted"), InvertedLootCondition.class);
      }

      public void toJson(JsonObject jsonObject, InvertedLootCondition invertedLootCondition, JsonSerializationContext jsonSerializationContext) {
         jsonObject.add("term", jsonSerializationContext.serialize(invertedLootCondition.term));
      }

      public InvertedLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         LootCondition lootCondition = (LootCondition)JsonHelper.deserialize(jsonObject, "term", jsonDeserializationContext, LootCondition.class);
         return new InvertedLootCondition(lootCondition);
      }
   }
}
