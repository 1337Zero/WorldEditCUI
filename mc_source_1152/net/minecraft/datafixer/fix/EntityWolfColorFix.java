package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.TypeReferences;

public class EntityWolfColorFix extends ChoiceFix {
   public EntityWolfColorFix(Schema outputSchema, boolean changesType) {
      super(outputSchema, changesType, "EntityWolfColorFix", TypeReferences.ENTITY, "minecraft:wolf");
   }

   public Dynamic<?> fixCollarColor(Dynamic<?> tag) {
      return tag.update("CollarColor", (dynamic) -> {
         return dynamic.createByte((byte)(15 - dynamic.asInt(0)));
      });
   }

   protected Typed<?> transform(Typed<?> typed) {
      return typed.update(DSL.remainderFinder(), this::fixCollarColor);
   }
}
