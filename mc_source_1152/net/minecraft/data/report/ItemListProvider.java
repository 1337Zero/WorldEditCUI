package net.minecraft.data.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import net.minecraft.data.DataCache;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;

public class ItemListProvider implements DataProvider {
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private final DataGenerator root;

   public ItemListProvider(DataGenerator dataGenerator) {
      this.root = dataGenerator;
   }

   public void run(DataCache dataCache) throws IOException {
      JsonObject jsonObject = new JsonObject();
      Registry.REGISTRIES.getIds().forEach((identifier) -> {
         jsonObject.add(identifier.toString(), toJson((MutableRegistry)Registry.REGISTRIES.get(identifier)));
      });
      Path path = this.root.getOutput().resolve("reports/registries.json");
      DataProvider.writeToPath(GSON, dataCache, jsonObject, path);
   }

   private static <T> JsonElement toJson(MutableRegistry<T> mutableRegistry) {
      JsonObject jsonObject = new JsonObject();
      if (mutableRegistry instanceof DefaultedRegistry) {
         Identifier identifier = ((DefaultedRegistry)mutableRegistry).getDefaultId();
         jsonObject.addProperty("default", identifier.toString());
      }

      int i = Registry.REGISTRIES.getRawId(mutableRegistry);
      jsonObject.addProperty("protocol_id", i);
      JsonObject jsonObject2 = new JsonObject();
      Iterator var4 = mutableRegistry.getIds().iterator();

      while(var4.hasNext()) {
         Identifier identifier2 = (Identifier)var4.next();
         T object = mutableRegistry.get(identifier2);
         int j = mutableRegistry.getRawId(object);
         JsonObject jsonObject3 = new JsonObject();
         jsonObject3.addProperty("protocol_id", j);
         jsonObject2.add(identifier2.toString(), jsonObject3);
      }

      jsonObject.add("entries", jsonObject2);
      return jsonObject;
   }

   public String getName() {
      return "Registry Dump";
   }
}
