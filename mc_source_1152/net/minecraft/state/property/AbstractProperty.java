package net.minecraft.state.property;

import com.google.common.base.MoreObjects;

public abstract class AbstractProperty<T extends Comparable<T>> implements Property<T> {
   private final Class<T> type;
   private final String name;
   private Integer computedHashCode;

   protected AbstractProperty(String name, Class<T> type) {
      this.type = type;
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public Class<T> getType() {
      return this.type;
   }

   public String toString() {
      return MoreObjects.toStringHelper(this).add("name", this.name).add("clazz", this.type).add("values", this.getValues()).toString();
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof AbstractProperty)) {
         return false;
      } else {
         AbstractProperty<?> abstractProperty = (AbstractProperty)obj;
         return this.type.equals(abstractProperty.type) && this.name.equals(abstractProperty.name);
      }
   }

   public final int hashCode() {
      if (this.computedHashCode == null) {
         this.computedHashCode = this.computeHashCode();
      }

      return this.computedHashCode;
   }

   public int computeHashCode() {
      return 31 * this.type.hashCode() + this.name.hashCode();
   }
}
