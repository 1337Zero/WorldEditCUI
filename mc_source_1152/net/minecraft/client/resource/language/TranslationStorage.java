package net.minecraft.client.resource.language;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class TranslationStorage {
   private static final Gson GSON = new Gson();
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Pattern PARAM_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");
   protected final Map<String, String> translations = Maps.newHashMap();

   public synchronized void load(ResourceManager container, List<String> list) {
      this.translations.clear();
      Iterator var3 = list.iterator();

      while(var3.hasNext()) {
         String string = (String)var3.next();
         String string2 = String.format("lang/%s.json", string);
         Iterator var6 = container.getAllNamespaces().iterator();

         while(var6.hasNext()) {
            String string3 = (String)var6.next();

            try {
               Identifier identifier = new Identifier(string3, string2);
               this.load(container.getAllResources(identifier));
            } catch (FileNotFoundException var9) {
            } catch (Exception var10) {
               LOGGER.warn("Skipped language file: {}:{} ({})", string3, string2, var10.toString());
            }
         }
      }

   }

   private void load(List<Resource> list) {
      Iterator var2 = list.iterator();

      while(var2.hasNext()) {
         Resource resource = (Resource)var2.next();
         InputStream inputStream = resource.getInputStream();

         try {
            this.load(inputStream);
         } finally {
            IOUtils.closeQuietly(inputStream);
         }
      }

   }

   private void load(InputStream inputStream) {
      JsonElement jsonElement = (JsonElement)GSON.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonElement.class);
      JsonObject jsonObject = JsonHelper.asObject(jsonElement, "strings");
      Iterator var4 = jsonObject.entrySet().iterator();

      while(var4.hasNext()) {
         Entry<String, JsonElement> entry = (Entry)var4.next();
         String string = PARAM_PATTERN.matcher(JsonHelper.asString((JsonElement)entry.getValue(), (String)entry.getKey())).replaceAll("%$1s");
         this.translations.put(entry.getKey(), string);
      }

   }

   private String get(String string) {
      String string2 = (String)this.translations.get(string);
      return string2 == null ? string : string2;
   }

   public String translate(String key, Object[] objects) {
      String string = this.get(key);

      try {
         return String.format(string, objects);
      } catch (IllegalFormatException var5) {
         return "Format error: " + string;
      }
   }

   public boolean containsKey(String string) {
      return this.translations.containsKey(string);
   }
}
