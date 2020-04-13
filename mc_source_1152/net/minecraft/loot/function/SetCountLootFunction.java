package net.minecraft.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTableRange;
import net.minecraft.loot.LootTableRanges;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.Identifier;

public class SetCountLootFunction extends ConditionalLootFunction {
   private final LootTableRange countRange;

   private SetCountLootFunction(LootCondition[] conditions, LootTableRange countRange) {
      super(conditions);
      this.countRange = countRange;
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      stack.setCount(this.countRange.next(context.getRandom()));
      return stack;
   }

   public static ConditionalLootFunction.Builder<?> builder(LootTableRange countRange) {
      return builder((conditions) -> {
         return new SetCountLootFunction(conditions, countRange);
      });
   }

   public static class Factory extends ConditionalLootFunction.Factory<SetCountLootFunction> {
      protected Factory() {
         super(new Identifier("set_count"), SetCountLootFunction.class);
      }

      public void toJson(JsonObject jsonObject, SetCountLootFunction setCountLootFunction, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)setCountLootFunction, jsonSerializationContext);
         jsonObject.add("count", LootTableRanges.toJson(setCountLootFunction.countRange, jsonSerializationContext));
      }

      public SetCountLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {
         LootTableRange lootTableRange = LootTableRanges.fromJson(jsonObject.get("count"), jsonDeserializationContext);
         return new SetCountLootFunction(lootConditions, lootTableRange);
      }
   }
}
