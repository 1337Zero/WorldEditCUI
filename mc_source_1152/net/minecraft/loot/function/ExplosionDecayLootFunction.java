package net.minecraft.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Random;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.Identifier;

public class ExplosionDecayLootFunction extends ConditionalLootFunction {
   private ExplosionDecayLootFunction(LootCondition[] conditions) {
      super(conditions);
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      Float var3 = (Float)context.get(LootContextParameters.EXPLOSION_RADIUS);
      if (var3 != null) {
         Random random = context.getRandom();
         float f = 1.0F / var3;
         int i = stack.getCount();
         int j = 0;

         for(int k = 0; k < i; ++k) {
            if (random.nextFloat() <= f) {
               ++j;
            }
         }

         stack.setCount(j);
      }

      return stack;
   }

   public static ConditionalLootFunction.Builder<?> builder() {
      return builder(ExplosionDecayLootFunction::new);
   }

   public static class Factory extends ConditionalLootFunction.Factory<ExplosionDecayLootFunction> {
      protected Factory() {
         super(new Identifier("explosion_decay"), ExplosionDecayLootFunction.class);
      }

      public ExplosionDecayLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {
         return new ExplosionDecayLootFunction(lootConditions);
      }
   }
}
