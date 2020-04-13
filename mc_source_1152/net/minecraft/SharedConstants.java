package net.minecraft;

import com.mojang.bridge.game.GameVersion;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.types.constant.NamespacedStringType;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.command.TranslatableBuiltInExceptions;
import net.minecraft.datafixer.schema.SchemaIdentifierNormalize;

public class SharedConstants {
   public static final Level RESOURCE_LEAK_DETECTOR_DISABLED;
   public static boolean isDevelopment;
   public static final char[] INVALID_CHARS_LEVEL_NAME;
   private static GameVersion gameVersion;

   public static boolean isValidChar(char chr) {
      return chr != 167 && chr >= ' ' && chr != 127;
   }

   public static String stripInvalidChars(String s) {
      StringBuilder stringBuilder = new StringBuilder();
      char[] var2 = s.toCharArray();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         char c = var2[var4];
         if (isValidChar(c)) {
            stringBuilder.append(c);
         }
      }

      return stringBuilder.toString();
   }

   @Environment(EnvType.CLIENT)
   public static String stripSupplementaryChars(String s) {
      StringBuilder stringBuilder = new StringBuilder();

      for(int i = 0; i < s.length(); i = s.offsetByCodePoints(i, 1)) {
         int j = s.codePointAt(i);
         if (!Character.isSupplementaryCodePoint(j)) {
            stringBuilder.appendCodePoint(j);
         } else {
            stringBuilder.append('�');
         }
      }

      return stringBuilder.toString();
   }

   public static GameVersion getGameVersion() {
      if (gameVersion == null) {
         gameVersion = MinecraftVersion.create();
      }

      return gameVersion;
   }

   static {
      RESOURCE_LEAK_DETECTOR_DISABLED = Level.DISABLED;
      INVALID_CHARS_LEVEL_NAME = new char[]{'/', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '"', ':'};
      ResourceLeakDetector.setLevel(RESOURCE_LEAK_DETECTOR_DISABLED);
      CommandSyntaxException.ENABLE_COMMAND_STACK_TRACES = false;
      CommandSyntaxException.BUILT_IN_EXCEPTIONS = new TranslatableBuiltInExceptions();
      NamespacedStringType.ENSURE_NAMESPACE = SchemaIdentifierNormalize::normalize;
   }
}
