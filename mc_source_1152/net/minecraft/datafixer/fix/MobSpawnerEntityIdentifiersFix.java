package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;

public class MobSpawnerEntityIdentifiersFix extends DataFix {
   public MobSpawnerEntityIdentifiersFix(Schema outputSchema, boolean changesType) {
      super(outputSchema, changesType);
   }

   private Dynamic<?> fixSpawner(Dynamic<?> tag) {
      if (!"MobSpawner".equals(tag.get("id").asString(""))) {
         return tag;
      } else {
         Optional<String> optional = tag.get("EntityId").asString();
         if (optional.isPresent()) {
            Dynamic<?> dynamic = (Dynamic)DataFixUtils.orElse(tag.get("SpawnData").get(), tag.emptyMap());
            dynamic = dynamic.set("id", dynamic.createString(((String)optional.get()).isEmpty() ? "Pig" : (String)optional.get()));
            tag = tag.set("SpawnData", dynamic);
            tag = tag.remove("EntityId");
         }

         Optional<? extends Stream<? extends Dynamic<?>>> optional2 = tag.get("SpawnPotentials").asStreamOpt();
         if (optional2.isPresent()) {
            tag = tag.set("SpawnPotentials", tag.createList(((Stream)optional2.get()).map((dynamicx) -> {
               Optional<String> optional = dynamicx.get("Type").asString();
               if (optional.isPresent()) {
                  Dynamic<?> dynamic2 = ((Dynamic)DataFixUtils.orElse(dynamicx.get("Properties").get(), dynamicx.emptyMap())).set("id", dynamicx.createString((String)optional.get()));
                  return dynamicx.set("Entity", dynamic2).remove("Type").remove("Properties");
               } else {
                  return dynamicx;
               }
            })));
         }

         return tag;
      }
   }

   public TypeRewriteRule makeRule() {
      Type<?> type = this.getOutputSchema().getType(TypeReferences.UNTAGGED_SPAWNER);
      return this.fixTypeEverywhereTyped("MobSpawnerEntityIdentifiersFix", this.getInputSchema().getType(TypeReferences.UNTAGGED_SPAWNER), type, (typed) -> {
         Dynamic<?> dynamic = (Dynamic)typed.get(DSL.remainderFinder());
         dynamic = dynamic.set("id", dynamic.createString("MobSpawner"));
         Pair<?, ? extends Optional<? extends Typed<?>>> pair = type.readTyped(this.fixSpawner(dynamic));
         return !((Optional)pair.getSecond()).isPresent() ? typed : (Typed)((Optional)pair.getSecond()).get();
      });
   }
}
