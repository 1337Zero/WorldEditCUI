package net.minecraft.datafixer.fix;

import com.google.common.collect.Sets;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.Optional;
import java.util.Set;
import net.minecraft.datafixer.TypeReferences;

public class EntityHealthFix extends DataFix {
   private static final Set<String> ENTITIES = Sets.newHashSet(new String[]{"ArmorStand", "Bat", "Blaze", "CaveSpider", "Chicken", "Cow", "Creeper", "EnderDragon", "Enderman", "Endermite", "EntityHorse", "Ghast", "Giant", "Guardian", "LavaSlime", "MushroomCow", "Ozelot", "Pig", "PigZombie", "Rabbit", "Sheep", "Shulker", "Silverfish", "Skeleton", "Slime", "SnowMan", "Spider", "Squid", "Villager", "VillagerGolem", "Witch", "WitherBoss", "Wolf", "Zombie"});

   public EntityHealthFix(Schema outputSchema, boolean changesType) {
      super(outputSchema, changesType);
   }

   public Dynamic<?> fixHealth(Dynamic<?> tag) {
      Optional<Number> optional = tag.get("HealF").asNumber();
      Optional<Number> optional2 = tag.get("Health").asNumber();
      float h;
      if (optional.isPresent()) {
         h = ((Number)optional.get()).floatValue();
         tag = tag.remove("HealF");
      } else {
         if (!optional2.isPresent()) {
            return tag;
         }

         h = ((Number)optional2.get()).floatValue();
      }

      return tag.set("Health", tag.createFloat(h));
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("EntityHealthFix", this.getInputSchema().getType(TypeReferences.ENTITY), (typed) -> {
         return typed.update(DSL.remainderFinder(), this::fixHealth);
      });
   }
}
