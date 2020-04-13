package net.minecraft.util;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class LowercaseMap<V> implements Map<String, V> {
   private final Map<String, V> delegate = Maps.newLinkedHashMap();

   public int size() {
      return this.delegate.size();
   }

   public boolean isEmpty() {
      return this.delegate.isEmpty();
   }

   public boolean containsKey(Object object) {
      return this.delegate.containsKey(object.toString().toLowerCase(Locale.ROOT));
   }

   public boolean containsValue(Object object) {
      return this.delegate.containsValue(object);
   }

   public V get(Object object) {
      return this.delegate.get(object.toString().toLowerCase(Locale.ROOT));
   }

   public V put(String string, V object) {
      return this.delegate.put(string.toLowerCase(Locale.ROOT), object);
   }

   public V remove(Object object) {
      return this.delegate.remove(object.toString().toLowerCase(Locale.ROOT));
   }

   public void putAll(Map<? extends String, ? extends V> map) {
      Iterator var2 = map.entrySet().iterator();

      while(var2.hasNext()) {
         Entry<? extends String, ? extends V> entry = (Entry)var2.next();
         this.put((String)entry.getKey(), entry.getValue());
      }

   }

   public void clear() {
      this.delegate.clear();
   }

   public Set<String> keySet() {
      return this.delegate.keySet();
   }

   public Collection<V> values() {
      return this.delegate.values();
   }

   public Set<Entry<String, V>> entrySet() {
      return this.delegate.entrySet();
   }
}
