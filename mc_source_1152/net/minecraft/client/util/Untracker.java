package net.minecraft.client.util;

import com.mojang.blaze3d.platform.GLX;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.system.Pointer;

@Environment(EnvType.CLIENT)
public class Untracker {
   @Nullable
   private static final MethodHandle ALLOCATOR_UNTRACK = (MethodHandle)GLX.make(() -> {
      try {
         Lookup lookup = MethodHandles.lookup();
         Class<?> var1 = Class.forName("org.lwjgl.system.MemoryManage$DebugAllocator");
         Method method = var1.getDeclaredMethod("untrack", Long.TYPE);
         method.setAccessible(true);
         Field field = Class.forName("org.lwjgl.system.MemoryUtil$LazyInit").getDeclaredField("ALLOCATOR");
         field.setAccessible(true);
         Object object = field.get((Object)null);
         return var1.isInstance(object) ? lookup.unreflect(method) : null;
      } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | ClassNotFoundException var5) {
         throw new RuntimeException(var5);
      }
   });

   public static void untrack(long address) {
      if (ALLOCATOR_UNTRACK != null) {
         try {
            ALLOCATOR_UNTRACK.invoke(address);
         } catch (Throwable var3) {
            throw new RuntimeException(var3);
         }
      }
   }

   public static void untrack(Pointer pointer) {
      untrack(pointer.address());
   }
}
