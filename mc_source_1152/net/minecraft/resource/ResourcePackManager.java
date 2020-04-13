package net.minecraft.resource;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class ResourcePackManager<T extends ResourcePackProfile> implements AutoCloseable {
   private final Set<ResourcePackProvider> providers = Sets.newHashSet();
   private final Map<String, T> profiles = Maps.newLinkedHashMap();
   private final List<T> enabled = Lists.newLinkedList();
   private final ResourcePackProfile.Factory<T> profileFactory;

   public ResourcePackManager(ResourcePackProfile.Factory<T> factory) {
      this.profileFactory = factory;
   }

   public void scanPacks() {
      this.close();
      Set<String> set = (Set)this.enabled.stream().map(ResourcePackProfile::getName).collect(Collectors.toCollection(LinkedHashSet::new));
      this.profiles.clear();
      this.enabled.clear();
      Iterator var2 = this.providers.iterator();

      while(var2.hasNext()) {
         ResourcePackProvider resourcePackProvider = (ResourcePackProvider)var2.next();
         resourcePackProvider.register(this.profiles, this.profileFactory);
      }

      this.sort();
      List var10000 = this.enabled;
      Stream var10001 = set.stream();
      Map var10002 = this.profiles;
      var10002.getClass();
      var10000.addAll((Collection)var10001.map(var10002::get).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new)));
      var2 = this.profiles.values().iterator();

      while(var2.hasNext()) {
         T resourcePackProfile = (ResourcePackProfile)var2.next();
         if (resourcePackProfile.isAlwaysEnabled() && !this.enabled.contains(resourcePackProfile)) {
            resourcePackProfile.getInitialPosition().insert(this.enabled, resourcePackProfile, Functions.identity(), false);
         }
      }

   }

   private void sort() {
      List<Entry<String, T>> list = Lists.newArrayList(this.profiles.entrySet());
      this.profiles.clear();
      list.stream().sorted(Entry.comparingByKey()).forEachOrdered((entry) -> {
         ResourcePackProfile var10000 = (ResourcePackProfile)this.profiles.put(entry.getKey(), entry.getValue());
      });
   }

   public void setEnabledProfiles(Collection<T> enabled) {
      this.enabled.clear();
      this.enabled.addAll(enabled);
      Iterator var2 = this.profiles.values().iterator();

      while(var2.hasNext()) {
         T resourcePackProfile = (ResourcePackProfile)var2.next();
         if (resourcePackProfile.isAlwaysEnabled() && !this.enabled.contains(resourcePackProfile)) {
            resourcePackProfile.getInitialPosition().insert(this.enabled, resourcePackProfile, Functions.identity(), false);
         }
      }

   }

   public Collection<T> getProfiles() {
      return this.profiles.values();
   }

   public Collection<T> getDisabledProfiles() {
      Collection<T> collection = Lists.newArrayList(this.profiles.values());
      collection.removeAll(this.enabled);
      return collection;
   }

   public Collection<T> getEnabledProfiles() {
      return this.enabled;
   }

   @Nullable
   public T getProfile(String name) {
      return (ResourcePackProfile)this.profiles.get(name);
   }

   public void registerProvider(ResourcePackProvider provider) {
      this.providers.add(provider);
   }

   public void close() {
      this.profiles.values().forEach(ResourcePackProfile::close);
   }
}
