package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class ItemLoreToComponentFix extends DataFix {
   public ItemLoreToComponentFix(Schema outputSchema, boolean changesType) {
      super(outputSchema, changesType);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
      OpticFinder<?> opticFinder = type.findField("tag");
      return this.fixTypeEverywhereTyped("Item Lore componentize", type, (typed) -> {
         return typed.updateTyped(opticFinder, (typedx) -> {
            return typedx.update(DSL.remainderFinder(), (dynamic) -> {
               return dynamic.update("display", (dynamicx) -> {
                  return dynamicx.update("Lore", (dynamic) -> {
                     Optional var10000 = dynamic.asStreamOpt().map(ItemLoreToComponentFix::fixLoreTags);
                     dynamic.getClass();
                     return (Dynamic)DataFixUtils.orElse(var10000.map(dynamic::createList), dynamic);
                  });
               });
            });
         });
      });
   }

   private static <T> Stream<Dynamic<T>> fixLoreTags(Stream<Dynamic<T>> tags) {
      return tags.map((dynamic) -> {
         Optional var10000 = dynamic.asString().map(ItemLoreToComponentFix::componentize);
         dynamic.getClass();
         return (Dynamic)DataFixUtils.orElse(var10000.map(dynamic::createString), dynamic);
      });
   }

   private static String componentize(String string) {
      return Text.Serializer.toJson(new LiteralText(string));
   }
}
