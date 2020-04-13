package net.minecraft.container;

public class ArrayPropertyDelegate implements PropertyDelegate {
   private final int[] data;

   public ArrayPropertyDelegate(int size) {
      this.data = new int[size];
   }

   public int get(int key) {
      return this.data[key];
   }

   public void set(int key, int value) {
      this.data[key] = value;
   }

   public int size() {
      return this.data.length;
   }
}
