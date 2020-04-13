package net.minecraft.util;

import java.util.List;
import java.util.Random;

public class WeightedPicker {
   public static int getWeightSum(List<? extends WeightedPicker.Entry> list) {
      int i = 0;
      int j = 0;

      for(int k = list.size(); j < k; ++j) {
         WeightedPicker.Entry entry = (WeightedPicker.Entry)list.get(j);
         i += entry.weight;
      }

      return i;
   }

   public static <T extends WeightedPicker.Entry> T getRandom(Random random, List<T> list, int weightSum) {
      if (weightSum <= 0) {
         throw (IllegalArgumentException)Util.throwOrPause(new IllegalArgumentException());
      } else {
         int i = random.nextInt(weightSum);
         return getAt(list, i);
      }
   }

   public static <T extends WeightedPicker.Entry> T getAt(List<T> list, int pos) {
      int i = 0;

      for(int j = list.size(); i < j; ++i) {
         T entry = (WeightedPicker.Entry)list.get(i);
         pos -= entry.weight;
         if (pos < 0) {
            return entry;
         }
      }

      return null;
   }

   public static <T extends WeightedPicker.Entry> T getRandom(Random random, List<T> list) {
      return getRandom(random, list, getWeightSum(list));
   }

   public static class Entry {
      protected final int weight;

      public Entry(int i) {
         this.weight = i;
      }
   }
}
