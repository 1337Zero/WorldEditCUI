package net.minecraft.tag;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TagContainer<T> {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = new Gson();
   private static final int JSON_EXTENSION_LENGTH = ".json".length();
   private Map<Identifier, Tag<T>> entries = ImmutableMap.of();
   private final Function<Identifier, Optional<T>> getter;
   private final String dataType;
   private final boolean ordered;
   private final String entryType;

   public TagContainer(Function<Identifier, Optional<T>> getter, String dataType, boolean ordered, String entryType) {
      this.getter = getter;
      this.dataType = dataType;
      this.ordered = ordered;
      this.entryType = entryType;
   }

   @Nullable
   public Tag<T> get(Identifier id) {
      return (Tag)this.entries.get(id);
   }

   public Tag<T> getOrCreate(Identifier id) {
      Tag<T> tag = (Tag)this.entries.get(id);
      return tag == null ? new Tag(id) : tag;
   }

   public Collection<Identifier> getKeys() {
      return this.entries.keySet();
   }

   @Environment(EnvType.CLIENT)
   public Collection<Identifier> getTagsFor(T object) {
      List<Identifier> list = Lists.newArrayList();
      Iterator var3 = this.entries.entrySet().iterator();

      while(var3.hasNext()) {
         Entry<Identifier, Tag<T>> entry = (Entry)var3.next();
         if (((Tag)entry.getValue()).contains(object)) {
            list.add(entry.getKey());
         }
      }

      return list;
   }

   public CompletableFuture<Map<Identifier, Tag.Builder<T>>> prepareReload(ResourceManager manager, Executor executor) {
      return CompletableFuture.supplyAsync(() -> {
         Map<Identifier, Tag.Builder<T>> map = Maps.newHashMap();
         Iterator var3 = manager.findResources(this.dataType, (stringx) -> {
            return stringx.endsWith(".json");
         }).iterator();

         while(var3.hasNext()) {
            Identifier identifier = (Identifier)var3.next();
            String string = identifier.getPath();
            Identifier identifier2 = new Identifier(identifier.getNamespace(), string.substring(this.dataType.length() + 1, string.length() - JSON_EXTENSION_LENGTH));

            try {
               Iterator var7 = manager.getAllResources(identifier).iterator();

               while(var7.hasNext()) {
                  Resource resource = (Resource)var7.next();

                  try {
                     InputStream inputStream = resource.getInputStream();
                     Throwable var10 = null;

                     try {
                        Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                        Throwable var12 = null;

                        try {
                           JsonObject jsonObject = (JsonObject)JsonHelper.deserialize(GSON, (Reader)reader, (Class)JsonObject.class);
                           if (jsonObject == null) {
                              LOGGER.error("Couldn't load {} tag list {} from {} in data pack {} as it's empty or null", this.entryType, identifier2, identifier, resource.getResourcePackName());
                           } else {
                              ((Tag.Builder)map.computeIfAbsent(identifier2, (identifierx) -> {
                                 return (Tag.Builder)Util.make(Tag.Builder.create(), (builder) -> {
                                    builder.ordered(this.ordered);
                                 });
                              })).fromJson(this.getter, jsonObject);
                           }
                        } catch (Throwable var53) {
                           var12 = var53;
                           throw var53;
                        } finally {
                           if (reader != null) {
                              if (var12 != null) {
                                 try {
                                    reader.close();
                                 } catch (Throwable var52) {
                                    var12.addSuppressed(var52);
                                 }
                              } else {
                                 reader.close();
                              }
                           }

                        }
                     } catch (Throwable var55) {
                        var10 = var55;
                        throw var55;
                     } finally {
                        if (inputStream != null) {
                           if (var10 != null) {
                              try {
                                 inputStream.close();
                              } catch (Throwable var51) {
                                 var10.addSuppressed(var51);
                              }
                           } else {
                              inputStream.close();
                           }
                        }

                     }
                  } catch (RuntimeException | IOException var57) {
                     LOGGER.error("Couldn't read {} tag list {} from {} in data pack {}", this.entryType, identifier2, identifier, resource.getResourcePackName(), var57);
                  } finally {
                     IOUtils.closeQuietly(resource);
                  }
               }
            } catch (IOException var59) {
               LOGGER.error("Couldn't read {} tag list {} from {}", this.entryType, identifier2, identifier, var59);
            }
         }

         return map;
      }, executor);
   }

   public void applyReload(Map<Identifier, Tag.Builder<T>> preparedBuilders) {
      HashMap map = Maps.newHashMap();

      while(!preparedBuilders.isEmpty()) {
         boolean bl = false;
         Iterator iterator = preparedBuilders.entrySet().iterator();

         while(iterator.hasNext()) {
            Entry<Identifier, Tag.Builder<T>> entry = (Entry)iterator.next();
            Tag.Builder<T> builder = (Tag.Builder)entry.getValue();
            map.getClass();
            if (builder.applyTagGetter(map::get)) {
               bl = true;
               Identifier identifier = (Identifier)entry.getKey();
               map.put(identifier, builder.build(identifier));
               iterator.remove();
            }
         }

         if (!bl) {
            preparedBuilders.forEach((identifierx, builderx) -> {
               LOGGER.error("Couldn't load {} tag {} as it either references another tag that doesn't exist, or ultimately references itself", this.entryType, identifierx);
            });
            break;
         }
      }

      preparedBuilders.forEach((identifierx, builderx) -> {
         Tag var10000 = (Tag)map.put(identifierx, builderx.build(identifierx));
      });
      this.setEntries(map);
   }

   protected void setEntries(Map<Identifier, Tag<T>> entries) {
      this.entries = ImmutableMap.copyOf(entries);
   }

   public Map<Identifier, Tag<T>> getEntries() {
      return this.entries;
   }
}
