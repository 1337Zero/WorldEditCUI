package net.minecraft.container;

public abstract class Property {
   private int oldValue;

   public static Property create(final PropertyDelegate propertyDelegate, final int key) {
      return new Property() {
         public int get() {
            return propertyDelegate.get(key);
         }

         public void set(int value) {
            propertyDelegate.set(key, value);
         }
      };
   }

   public static Property create(final int[] is, final int key) {
      return new Property() {
         public int get() {
            return is[key];
         }

         public void set(int value) {
            is[key] = value;
         }
      };
   }

   public static Property create() {
      return new Property() {
         private int value;

         public int get() {
            return this.value;
         }

         public void set(int value) {
            this.value = value;
         }
      };
   }

   public abstract int get();

   public abstract void set(int value);

   public boolean detectChanges() {
      int i = this.get();
      boolean bl = i != this.oldValue;
      this.oldValue = i;
      return bl;
   }
}
