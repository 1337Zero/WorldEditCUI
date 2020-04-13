package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.loot.BinomialLootTableRange;
import net.minecraft.loot.ConstantLootTableRange;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.UniformLootTableRange;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LootConditionManager extends JsonDataLoader {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(UniformLootTableRange.class, new UniformLootTableRange.Serializer()).registerTypeAdapter(BinomialLootTableRange.class, new BinomialLootTableRange.Serializer()).registerTypeAdapter(ConstantLootTableRange.class, new ConstantLootTableRange.Serializer()).registerTypeHierarchyAdapter(LootCondition.class, new LootConditions.Factory()).registerTypeHierarchyAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer()).create();
   private Map<Identifier, LootCondition> conditions = ImmutableMap.of();

   public LootConditionManager() {
      super(GSON, "predicates");
   }

   @Nullable
   public LootCondition get(Identifier id) {
      return (LootCondition)this.conditions.get(id);
   }

   protected void apply(Map<Identifier, JsonObject> map, ResourceManager resourceManager, Profiler profiler) {
      Builder<Identifier, LootCondition> builder = ImmutableMap.builder();
      map.forEach((identifier, jsonObject) -> {
         try {
            LootCondition lootCondition = (LootCondition)GSON.fromJson(jsonObject, LootCondition.class);
            builder.put(identifier, lootCondition);
         } catch (Exception var4) {
            LOGGER.error("Couldn't parse loot table {}", identifier, var4);
         }

      });
      Map<Identifier, LootCondition> map2 = builder.build();
      LootTableReporter lootTableReporter = new LootTableReporter(LootContextTypes.GENERIC, map2::get, (identifier) -> {
         return null;
      });
      map2.forEach((identifier, lootCondition) -> {
         lootCondition.check(lootTableReporter.withCondition("{" + identifier + "}", identifier));
      });
      lootTableReporter.getMessages().forEach((string, string2) -> {
         LOGGER.warn("Found validation problem in " + string + ": " + string2);
      });
      this.conditions = map2;
   }

   public Set<Identifier> getIds() {
      return Collections.unmodifiableSet(this.conditions.keySet());
   }
}
