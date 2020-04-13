package net.minecraft.util;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class MapUtil {
   public static <K, V> Map<K, V> createMap(Iterable<K> keys, Iterable<V> values) {
      return createMap(keys, values, Maps.newLinkedHashMap());
   }

   public static <K, V> Map<K, V> createMap(Iterable<K> keys, Iterable<V> values, Map<K, V> result) {
      Iterator<V> iterator = values.iterator();
      Iterator var4 = keys.iterator();

      while(var4.hasNext()) {
         K object = var4.next();
         result.put(object, iterator.next());
      }

      if (iterator.hasNext()) {
         throw new NoSuchElementException();
      } else {
         return result;
      }
   }
}
