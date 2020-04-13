package net.minecraft.predicate.block;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;

public class BlockStatePredicate implements Predicate<BlockState> {
   public static final Predicate<BlockState> ANY = (blockState) -> {
      return true;
   };
   private final StateManager<Block, BlockState> factory;
   private final Map<Property<?>, Predicate<Object>> propertyTests = Maps.newHashMap();

   private BlockStatePredicate(StateManager<Block, BlockState> stateManager) {
      this.factory = stateManager;
   }

   public static BlockStatePredicate forBlock(Block block) {
      return new BlockStatePredicate(block.getStateManager());
   }

   public boolean test(@Nullable BlockState blockState) {
      if (blockState != null && blockState.getBlock().equals(this.factory.getOwner())) {
         if (this.propertyTests.isEmpty()) {
            return true;
         } else {
            Iterator var2 = this.propertyTests.entrySet().iterator();

            Entry entry;
            do {
               if (!var2.hasNext()) {
                  return true;
               }

               entry = (Entry)var2.next();
            } while(this.testProperty(blockState, (Property)entry.getKey(), (Predicate)entry.getValue()));

            return false;
         }
      } else {
         return false;
      }
   }

   protected <T extends Comparable<T>> boolean testProperty(BlockState blockState, Property<T> property, Predicate<Object> predicate) {
      T comparable = blockState.get(property);
      return predicate.test(comparable);
   }

   public <V extends Comparable<V>> BlockStatePredicate with(Property<V> property, Predicate<Object> predicate) {
      if (!this.factory.getProperties().contains(property)) {
         throw new IllegalArgumentException(this.factory + " cannot support property " + property);
      } else {
         this.propertyTests.put(property, predicate);
         return this;
      }
   }
}
