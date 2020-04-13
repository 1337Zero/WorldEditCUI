package net.minecraft.advancement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Formatting;

public enum AdvancementFrame {
   TASK("task", 0, Formatting.GREEN),
   CHALLENGE("challenge", 26, Formatting.DARK_PURPLE),
   GOAL("goal", 52, Formatting.GREEN);

   private final String id;
   private final int texV;
   private final Formatting titleFormat;

   private AdvancementFrame(String id, int texV, Formatting titleFormat) {
      this.id = id;
      this.texV = texV;
      this.titleFormat = titleFormat;
   }

   public String getId() {
      return this.id;
   }

   @Environment(EnvType.CLIENT)
   public int texV() {
      return this.texV;
   }

   public static AdvancementFrame forName(String name) {
      AdvancementFrame[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         AdvancementFrame advancementFrame = var1[var3];
         if (advancementFrame.id.equals(name)) {
            return advancementFrame;
         }
      }

      throw new IllegalArgumentException("Unknown frame type '" + name + "'");
   }

   public Formatting getTitleFormat() {
      return this.titleFormat;
   }
}
