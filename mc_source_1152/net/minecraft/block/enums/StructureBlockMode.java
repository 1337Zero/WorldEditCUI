package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public enum StructureBlockMode implements StringIdentifiable {
   SAVE("save"),
   LOAD("load"),
   CORNER("corner"),
   DATA("data");

   private final String name;

   private StructureBlockMode(String name) {
      this.name = name;
   }

   public String asString() {
      return this.name;
   }
}
