package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;

public class BlockEntityBannerColorFix extends ChoiceFix {
   public BlockEntityBannerColorFix(Schema outputSchema, boolean changesType) {
      super(outputSchema, changesType, "BlockEntityBannerColorFix", TypeReferences.BLOCK_ENTITY, "minecraft:banner");
   }

   public Dynamic<?> fixBannerColor(Dynamic<?> tag) {
      tag = tag.update("Base", (tagx) -> {
         return tagx.createInt(15 - tagx.asInt(0));
      });
      tag = tag.update("Patterns", (dynamic) -> {
         Optional var10000 = dynamic.asStreamOpt().map((stream) -> {
            return stream.map((dynamic) -> {
               return dynamic.update("Color", (tag) -> {
                  return tag.createInt(15 - tag.asInt(0));
               });
            });
         });
         dynamic.getClass();
         return (Dynamic)DataFixUtils.orElse(var10000.map(dynamic::createList), dynamic);
      });
      return tag;
   }

   protected Typed<?> transform(Typed<?> typed) {
      return typed.update(DSL.remainderFinder(), this::fixBannerColor);
   }
}
