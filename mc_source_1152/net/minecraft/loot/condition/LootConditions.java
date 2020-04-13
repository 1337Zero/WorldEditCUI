package net.minecraft.loot.condition;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class LootConditions {
   private static final Map<Identifier, LootCondition.Factory<?>> byId = Maps.newHashMap();
   private static final Map<Class<? extends LootCondition>, LootCondition.Factory<?>> byClass = Maps.newHashMap();

   public static <T extends LootCondition> void register(LootCondition.Factory<? extends T> condition) {
      Identifier identifier = condition.getId();
      Class<T> var2 = condition.getConditionClass();
      if (byId.containsKey(identifier)) {
         throw new IllegalArgumentException("Can't re-register item condition name " + identifier);
      } else if (byClass.containsKey(var2)) {
         throw new IllegalArgumentException("Can't re-register item condition class " + var2.getName());
      } else {
         byId.put(identifier, condition);
         byClass.put(var2, condition);
      }
   }

   public static LootCondition.Factory<?> get(Identifier id) {
      LootCondition.Factory<?> factory = (LootCondition.Factory)byId.get(id);
      if (factory == null) {
         throw new IllegalArgumentException("Unknown loot item condition '" + id + "'");
      } else {
         return factory;
      }
   }

   public static <T extends LootCondition> LootCondition.Factory<T> getFactory(T condition) {
      LootCondition.Factory<T> factory = (LootCondition.Factory)byClass.get(condition.getClass());
      if (factory == null) {
         throw new IllegalArgumentException("Unknown loot item condition " + condition);
      } else {
         return factory;
      }
   }

   public static <T> Predicate<T> joinAnd(Predicate<T>[] predicates) {
      switch(predicates.length) {
      case 0:
         return (predicatesx) -> {
            return true;
         };
      case 1:
         return predicates[0];
      case 2:
         return predicates[0].and(predicates[1]);
      default:
         return (operand) -> {
            Predicate[] var2 = predicates;
            int var3 = predicates.length;

            for(int var4 = 0; var4 < var3; ++var4) {
               Predicate<T> predicate = var2[var4];
               if (!predicate.test(operand)) {
                  return false;
               }
            }

            return true;
         };
      }
   }

   public static <T> Predicate<T> joinOr(Predicate<T>[] predicates) {
      switch(predicates.length) {
      case 0:
         return (predicatesx) -> {
            return false;
         };
      case 1:
         return predicates[0];
      case 2:
         return predicates[0].or(predicates[1]);
      default:
         return (operand) -> {
            Predicate[] var2 = predicates;
            int var3 = predicates.length;

            for(int var4 = 0; var4 < var3; ++var4) {
               Predicate<T> predicate = var2[var4];
               if (predicate.test(operand)) {
                  return true;
               }
            }

            return false;
         };
      }
   }

   static {
      register(new InvertedLootCondition.Factory());
      register(new AlternativeLootCondition.Factory());
      register(new RandomChanceLootCondition.Factory());
      register(new RandomChanceWithLootingLootCondition.Factory());
      register(new EntityPropertiesLootCondition.Factory());
      register(new KilledByPlayerLootCondition.Factory());
      register(new EntityScoresLootCondition.Factory());
      register(new BlockStatePropertyLootCondition.Factory());
      register(new MatchToolLootCondition.Factory());
      register(new TableBonusLootCondition.Factory());
      register(new SurvivesExplosionLootCondition.Factory());
      register(new DamageSourcePropertiesLootCondition.Factory());
      register(new LocationCheckLootCondition.Factory());
      register(new WeatherCheckLootCondition.Factory());
      register(new ReferenceLootCondition.Factory());
      register(new TimeCheckLootCondition.Factory());
   }

   public static class Factory implements JsonDeserializer<LootCondition>, JsonSerializer<LootCondition> {
      public LootCondition deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         JsonObject jsonObject = JsonHelper.asObject(jsonElement, "condition");
         Identifier identifier = new Identifier(JsonHelper.getString(jsonObject, "condition"));

         LootCondition.Factory factory2;
         try {
            factory2 = LootConditions.get(identifier);
         } catch (IllegalArgumentException var8) {
            throw new JsonSyntaxException("Unknown condition '" + identifier + "'");
         }

         return factory2.fromJson(jsonObject, jsonDeserializationContext);
      }

      public JsonElement serialize(LootCondition lootCondition, Type type, JsonSerializationContext jsonSerializationContext) {
         LootCondition.Factory<LootCondition> factory = LootConditions.getFactory(lootCondition);
         JsonObject jsonObject = new JsonObject();
         jsonObject.addProperty("condition", factory.getId().toString());
         factory.toJson(jsonObject, lootCondition, jsonSerializationContext);
         return jsonObject;
      }
   }
}
