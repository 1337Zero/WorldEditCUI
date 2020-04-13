package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;

public class IglooMetadataRemovalFix extends DataFix {
   public IglooMetadataRemovalFix(Schema outputSchema, boolean changesType) {
      super(outputSchema, changesType);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(TypeReferences.STRUCTURE_FEATURE);
      Type<?> type2 = this.getOutputSchema().getType(TypeReferences.STRUCTURE_FEATURE);
      return this.writeFixAndRead("IglooMetadataRemovalFix", type, type2, IglooMetadataRemovalFix::removeMetadata);
   }

   private static <T> Dynamic<T> removeMetadata(Dynamic<T> tag) {
      boolean bl = (Boolean)tag.get("Children").asStreamOpt().map((stream) -> {
         return stream.allMatch(IglooMetadataRemovalFix::isIgloo);
      }).orElse(false);
      return bl ? tag.set("id", tag.createString("Igloo")).remove("Children") : tag.update("Children", IglooMetadataRemovalFix::removeIgloos);
   }

   private static <T> Dynamic<T> removeIgloos(Dynamic<T> tag) {
      Optional var10000 = tag.asStreamOpt().map((stream) -> {
         return stream.filter((dynamic) -> {
            return !isIgloo(dynamic);
         });
      });
      tag.getClass();
      return (Dynamic)var10000.map(tag::createList).orElse(tag);
   }

   private static boolean isIgloo(Dynamic<?> tag) {
      return tag.get("id").asString("").equals("Iglu");
   }
}
