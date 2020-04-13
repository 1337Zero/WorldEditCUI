package net.minecraft.util.profiler;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class DummyProfiler implements ReadableProfiler {
   public static final DummyProfiler INSTANCE = new DummyProfiler();

   private DummyProfiler() {
   }

   public void startTick() {
   }

   public void endTick() {
   }

   public void push(String string) {
   }

   public void push(Supplier<String> supplier) {
   }

   public void pop() {
   }

   public void swap(String string) {
   }

   @Environment(EnvType.CLIENT)
   public void swap(Supplier<String> supplier) {
   }

   public void method_24270(String string) {
   }

   public void method_24271(Supplier<String> supplier) {
   }

   public ProfileResult getResults() {
      return EmptyProfileResult.INSTANCE;
   }
}
