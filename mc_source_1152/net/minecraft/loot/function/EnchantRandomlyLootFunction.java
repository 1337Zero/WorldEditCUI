package net.minecraft.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.InfoEnchantment;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EnchantRandomlyLootFunction extends ConditionalLootFunction {
   private static final Logger LOGGER = LogManager.getLogger();
   private final List<Enchantment> enchantments;

   private EnchantRandomlyLootFunction(LootCondition[] conditions, Collection<Enchantment> enchantments) {
      super(conditions);
      this.enchantments = ImmutableList.copyOf(enchantments);
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      Random random = context.getRandom();
      Enchantment enchantment3;
      if (this.enchantments.isEmpty()) {
         List<Enchantment> list = Lists.newArrayList();
         Iterator var6 = Registry.ENCHANTMENT.iterator();

         label32:
         while(true) {
            Enchantment enchantment;
            do {
               if (!var6.hasNext()) {
                  if (list.isEmpty()) {
                     LOGGER.warn("Couldn't find a compatible enchantment for {}", stack);
                     return stack;
                  }

                  enchantment3 = (Enchantment)list.get(random.nextInt(list.size()));
                  break label32;
               }

               enchantment = (Enchantment)var6.next();
            } while(stack.getItem() != Items.BOOK && !enchantment.isAcceptableItem(stack));

            list.add(enchantment);
         }
      } else {
         enchantment3 = (Enchantment)this.enchantments.get(random.nextInt(this.enchantments.size()));
      }

      int i = MathHelper.nextInt(random, enchantment3.getMinimumLevel(), enchantment3.getMaximumLevel());
      if (stack.getItem() == Items.BOOK) {
         stack = new ItemStack(Items.ENCHANTED_BOOK);
         EnchantedBookItem.addEnchantment(stack, new InfoEnchantment(enchantment3, i));
      } else {
         stack.addEnchantment(enchantment3, i);
      }

      return stack;
   }

   public static ConditionalLootFunction.Builder<?> builder() {
      return builder((conditions) -> {
         return new EnchantRandomlyLootFunction(conditions, ImmutableList.of());
      });
   }

   public static class Factory extends ConditionalLootFunction.Factory<EnchantRandomlyLootFunction> {
      public Factory() {
         super(new Identifier("enchant_randomly"), EnchantRandomlyLootFunction.class);
      }

      public void toJson(JsonObject jsonObject, EnchantRandomlyLootFunction enchantRandomlyLootFunction, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)enchantRandomlyLootFunction, jsonSerializationContext);
         if (!enchantRandomlyLootFunction.enchantments.isEmpty()) {
            JsonArray jsonArray = new JsonArray();
            Iterator var5 = enchantRandomlyLootFunction.enchantments.iterator();

            while(var5.hasNext()) {
               Enchantment enchantment = (Enchantment)var5.next();
               Identifier identifier = Registry.ENCHANTMENT.getId(enchantment);
               if (identifier == null) {
                  throw new IllegalArgumentException("Don't know how to serialize enchantment " + enchantment);
               }

               jsonArray.add(new JsonPrimitive(identifier.toString()));
            }

            jsonObject.add("enchantments", jsonArray);
         }

      }

      public EnchantRandomlyLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {
         List<Enchantment> list = Lists.newArrayList();
         if (jsonObject.has("enchantments")) {
            JsonArray jsonArray = JsonHelper.getArray(jsonObject, "enchantments");
            Iterator var6 = jsonArray.iterator();

            while(var6.hasNext()) {
               JsonElement jsonElement = (JsonElement)var6.next();
               String string = JsonHelper.asString(jsonElement, "enchantment");
               Enchantment enchantment = (Enchantment)Registry.ENCHANTMENT.getOrEmpty(new Identifier(string)).orElseThrow(() -> {
                  return new JsonSyntaxException("Unknown enchantment '" + string + "'");
               });
               list.add(enchantment);
            }
         }

         return new EnchantRandomlyLootFunction(lootConditions, list);
      }
   }
}
