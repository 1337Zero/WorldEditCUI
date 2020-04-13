package net.minecraft.advancement.criterion;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.advancement.PlayerAdvancementTracker;

public abstract class AbstractCriterion<T extends CriterionConditions> implements Criterion<T> {
   private final Map<PlayerAdvancementTracker, Set<Criterion.ConditionsContainer<T>>> progressions = Maps.newIdentityHashMap();

   public final void beginTrackingCondition(PlayerAdvancementTracker manager, Criterion.ConditionsContainer<T> conditionsContainer) {
      ((Set)this.progressions.computeIfAbsent(manager, (playerAdvancementTracker) -> {
         return Sets.newHashSet();
      })).add(conditionsContainer);
   }

   public final void endTrackingCondition(PlayerAdvancementTracker manager, Criterion.ConditionsContainer<T> conditionsContainer) {
      Set<Criterion.ConditionsContainer<T>> set = (Set)this.progressions.get(manager);
      if (set != null) {
         set.remove(conditionsContainer);
         if (set.isEmpty()) {
            this.progressions.remove(manager);
         }
      }

   }

   public final void endTracking(PlayerAdvancementTracker tracker) {
      this.progressions.remove(tracker);
   }

   protected void test(PlayerAdvancementTracker tracker, Predicate<T> tester) {
      Set<Criterion.ConditionsContainer<T>> set = (Set)this.progressions.get(tracker);
      if (set != null) {
         List<Criterion.ConditionsContainer<T>> list = null;
         Iterator var5 = set.iterator();

         Criterion.ConditionsContainer conditionsContainer2;
         while(var5.hasNext()) {
            conditionsContainer2 = (Criterion.ConditionsContainer)var5.next();
            if (tester.test(conditionsContainer2.getConditions())) {
               if (list == null) {
                  list = Lists.newArrayList();
               }

               list.add(conditionsContainer2);
            }
         }

         if (list != null) {
            var5 = list.iterator();

            while(var5.hasNext()) {
               conditionsContainer2 = (Criterion.ConditionsContainer)var5.next();
               conditionsContainer2.grant(tracker);
            }
         }

      }
   }

   protected void grant(PlayerAdvancementTracker tracker) {
      Set<Criterion.ConditionsContainer<T>> set = (Set)this.progressions.get(tracker);
      if (set != null && !set.isEmpty()) {
         UnmodifiableIterator var3 = ImmutableSet.copyOf(set).iterator();

         while(var3.hasNext()) {
            Criterion.ConditionsContainer<T> conditionsContainer = (Criterion.ConditionsContainer)var3.next();
            conditionsContainer.grant(tracker);
         }
      }

   }
}
