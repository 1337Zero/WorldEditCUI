package net.minecraft.state;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.state.property.Property;
import net.minecraft.util.MapUtil;

public class StateManager<O, S extends State<S>> {
   private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
   private final O owner;
   private final ImmutableSortedMap<String, Property<?>> properties;
   private final ImmutableList<S> states;

   protected <A extends AbstractState<O, S>> StateManager(O owner, StateManager.Factory<O, S, A> factory, Map<String, Property<?>> namedProperties) {
      this.owner = owner;
      this.properties = ImmutableSortedMap.copyOf(namedProperties);
      Map<Map<Property<?>, Comparable<?>>, A> map = Maps.newLinkedHashMap();
      List<A> list = Lists.newArrayList();
      Stream<List<Comparable<?>>> stream = Stream.of(Collections.emptyList());

      Property property;
      for(UnmodifiableIterator var7 = this.properties.values().iterator(); var7.hasNext(); stream = stream.flatMap((listx) -> {
         return property.getValues().stream().map((comparable) -> {
            List<Comparable<?>> list2 = Lists.newArrayList(listx);
            list2.add(comparable);
            return list2;
         });
      })) {
         property = (Property)var7.next();
      }

      stream.forEach((list2) -> {
         Map<Property<?>, Comparable<?>> map2 = MapUtil.createMap(this.properties.values(), list2);
         A abstractState = factory.create(owner, ImmutableMap.copyOf(map2));
         map.put(map2, abstractState);
         list.add(abstractState);
      });
      Iterator var9 = list.iterator();

      while(var9.hasNext()) {
         A abstractState = (AbstractState)var9.next();
         abstractState.createWithTable(map);
      }

      this.states = ImmutableList.copyOf(list);
   }

   public ImmutableList<S> getStates() {
      return this.states;
   }

   public S getDefaultState() {
      return (State)this.states.get(0);
   }

   public O getOwner() {
      return this.owner;
   }

   public Collection<Property<?>> getProperties() {
      return this.properties.values();
   }

   public String toString() {
      return MoreObjects.toStringHelper(this).add("block", this.owner).add("properties", this.properties.values().stream().map(Property::getName).collect(Collectors.toList())).toString();
   }

   @Nullable
   public Property<?> getProperty(String name) {
      return (Property)this.properties.get(name);
   }

   public static class Builder<O, S extends State<S>> {
      private final O owner;
      private final Map<String, Property<?>> namedProperties = Maps.newHashMap();

      public Builder(O owner) {
         this.owner = owner;
      }

      public StateManager.Builder<O, S> add(Property<?>... properties) {
         Property[] var2 = properties;
         int var3 = properties.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            Property<?> property = var2[var4];
            this.validate(property);
            this.namedProperties.put(property.getName(), property);
         }

         return this;
      }

      private <T extends Comparable<T>> void validate(Property<T> property) {
         String string = property.getName();
         if (!StateManager.VALID_NAME_PATTERN.matcher(string).matches()) {
            throw new IllegalArgumentException(this.owner + " has invalidly named property: " + string);
         } else {
            Collection<T> collection = property.getValues();
            if (collection.size() <= 1) {
               throw new IllegalArgumentException(this.owner + " attempted use property " + string + " with <= 1 possible values");
            } else {
               Iterator var4 = collection.iterator();

               String string2;
               do {
                  if (!var4.hasNext()) {
                     if (this.namedProperties.containsKey(string)) {
                        throw new IllegalArgumentException(this.owner + " has duplicate property: " + string);
                     }

                     return;
                  }

                  T comparable = (Comparable)var4.next();
                  string2 = property.name(comparable);
               } while(StateManager.VALID_NAME_PATTERN.matcher(string2).matches());

               throw new IllegalArgumentException(this.owner + " has property: " + string + " with invalidly named value: " + string2);
            }
         }
      }

      public <A extends AbstractState<O, S>> StateManager<O, S> build(StateManager.Factory<O, S, A> factory) {
         return new StateManager(this.owner, factory, this.namedProperties);
      }
   }

   public interface Factory<O, S extends State<S>, A extends AbstractState<O, S>> {
      A create(O owner, ImmutableMap<Property<?>, Comparable<?>> entries);
   }
}
