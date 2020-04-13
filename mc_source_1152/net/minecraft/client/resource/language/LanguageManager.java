package net.minecraft.client.resource.language;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.metadata.LanguageResourceMetadata;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.SynchronousResourceReloadListener;
import net.minecraft.util.Language;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class LanguageManager implements SynchronousResourceReloadListener {
   private static final Logger LOGGER = LogManager.getLogger();
   protected static final TranslationStorage STORAGE = new TranslationStorage();
   private String currentLanguageCode;
   private final Map<String, LanguageDefinition> languageDefs = Maps.newHashMap();

   public LanguageManager(String string) {
      this.currentLanguageCode = string;
      I18n.setLanguage(STORAGE);
   }

   public void reloadResources(List<ResourcePack> list) {
      this.languageDefs.clear();
      Iterator var2 = list.iterator();

      while(var2.hasNext()) {
         ResourcePack resourcePack = (ResourcePack)var2.next();

         try {
            LanguageResourceMetadata languageResourceMetadata = (LanguageResourceMetadata)resourcePack.parseMetadata(LanguageResourceMetadata.READER);
            if (languageResourceMetadata != null) {
               Iterator var5 = languageResourceMetadata.getLanguageDefinitions().iterator();

               while(var5.hasNext()) {
                  LanguageDefinition languageDefinition = (LanguageDefinition)var5.next();
                  if (!this.languageDefs.containsKey(languageDefinition.getCode())) {
                     this.languageDefs.put(languageDefinition.getCode(), languageDefinition);
                  }
               }
            }
         } catch (IOException | RuntimeException var7) {
            LOGGER.warn("Unable to parse language metadata section of resourcepack: {}", resourcePack.getName(), var7);
         }
      }

   }

   public void apply(ResourceManager manager) {
      List<String> list = Lists.newArrayList(new String[]{"en_us"});
      if (!"en_us".equals(this.currentLanguageCode)) {
         list.add(this.currentLanguageCode);
      }

      STORAGE.load(manager, list);
      Language.load(STORAGE.translations);
   }

   public boolean isRightToLeft() {
      return this.getLanguage() != null && this.getLanguage().isRightToLeft();
   }

   public void setLanguage(LanguageDefinition languageDefinition) {
      this.currentLanguageCode = languageDefinition.getCode();
   }

   public LanguageDefinition getLanguage() {
      String string = this.languageDefs.containsKey(this.currentLanguageCode) ? this.currentLanguageCode : "en_us";
      return (LanguageDefinition)this.languageDefs.get(string);
   }

   public SortedSet<LanguageDefinition> getAllLanguages() {
      return Sets.newTreeSet(this.languageDefs.values());
   }

   public LanguageDefinition getLanguage(String code) {
      return (LanguageDefinition)this.languageDefs.get(code);
   }
}
