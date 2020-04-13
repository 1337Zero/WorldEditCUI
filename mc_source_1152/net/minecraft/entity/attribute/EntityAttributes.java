package net.minecraft.entity.attribute;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityAttributes {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final EntityAttribute MAX_HEALTH = (new ClampedEntityAttribute((EntityAttribute)null, "generic.maxHealth", 20.0D, 0.0D, 1024.0D)).setName("Max Health").setTracked(true);
   public static final EntityAttribute FOLLOW_RANGE = (new ClampedEntityAttribute((EntityAttribute)null, "generic.followRange", 32.0D, 0.0D, 2048.0D)).setName("Follow Range");
   public static final EntityAttribute KNOCKBACK_RESISTANCE = (new ClampedEntityAttribute((EntityAttribute)null, "generic.knockbackResistance", 0.0D, 0.0D, 1.0D)).setName("Knockback Resistance");
   public static final EntityAttribute MOVEMENT_SPEED = (new ClampedEntityAttribute((EntityAttribute)null, "generic.movementSpeed", 0.699999988079071D, 0.0D, 1024.0D)).setName("Movement Speed").setTracked(true);
   public static final EntityAttribute FLYING_SPEED = (new ClampedEntityAttribute((EntityAttribute)null, "generic.flyingSpeed", 0.4000000059604645D, 0.0D, 1024.0D)).setName("Flying Speed").setTracked(true);
   public static final EntityAttribute ATTACK_DAMAGE = new ClampedEntityAttribute((EntityAttribute)null, "generic.attackDamage", 2.0D, 0.0D, 2048.0D);
   public static final EntityAttribute ATTACK_KNOCKBACK = new ClampedEntityAttribute((EntityAttribute)null, "generic.attackKnockback", 0.0D, 0.0D, 5.0D);
   public static final EntityAttribute ATTACK_SPEED = (new ClampedEntityAttribute((EntityAttribute)null, "generic.attackSpeed", 4.0D, 0.0D, 1024.0D)).setTracked(true);
   public static final EntityAttribute ARMOR = (new ClampedEntityAttribute((EntityAttribute)null, "generic.armor", 0.0D, 0.0D, 30.0D)).setTracked(true);
   public static final EntityAttribute ARMOR_TOUGHNESS = (new ClampedEntityAttribute((EntityAttribute)null, "generic.armorToughness", 0.0D, 0.0D, 20.0D)).setTracked(true);
   public static final EntityAttribute LUCK = (new ClampedEntityAttribute((EntityAttribute)null, "generic.luck", 0.0D, -1024.0D, 1024.0D)).setTracked(true);

   public static ListTag toTag(AbstractEntityAttributeContainer container) {
      ListTag listTag = new ListTag();
      Iterator var2 = container.values().iterator();

      while(var2.hasNext()) {
         EntityAttributeInstance entityAttributeInstance = (EntityAttributeInstance)var2.next();
         listTag.add(toTag(entityAttributeInstance));
      }

      return listTag;
   }

   private static CompoundTag toTag(EntityAttributeInstance instance) {
      CompoundTag compoundTag = new CompoundTag();
      EntityAttribute entityAttribute = instance.getAttribute();
      compoundTag.putString("Name", entityAttribute.getId());
      compoundTag.putDouble("Base", instance.getBaseValue());
      Collection<EntityAttributeModifier> collection = instance.getModifiers();
      if (collection != null && !collection.isEmpty()) {
         ListTag listTag = new ListTag();
         Iterator var5 = collection.iterator();

         while(var5.hasNext()) {
            EntityAttributeModifier entityAttributeModifier = (EntityAttributeModifier)var5.next();
            if (entityAttributeModifier.shouldSerialize()) {
               listTag.add(toTag(entityAttributeModifier));
            }
         }

         compoundTag.put("Modifiers", listTag);
      }

      return compoundTag;
   }

   public static CompoundTag toTag(EntityAttributeModifier modifier) {
      CompoundTag compoundTag = new CompoundTag();
      compoundTag.putString("Name", modifier.getName());
      compoundTag.putDouble("Amount", modifier.getAmount());
      compoundTag.putInt("Operation", modifier.getOperation().getId());
      compoundTag.putUuid("UUID", modifier.getId());
      return compoundTag;
   }

   public static void fromTag(AbstractEntityAttributeContainer container, ListTag tag) {
      for(int i = 0; i < tag.size(); ++i) {
         CompoundTag compoundTag = tag.getCompound(i);
         EntityAttributeInstance entityAttributeInstance = container.get(compoundTag.getString("Name"));
         if (entityAttributeInstance == null) {
            LOGGER.warn("Ignoring unknown attribute '{}'", compoundTag.getString("Name"));
         } else {
            fromTag(entityAttributeInstance, compoundTag);
         }
      }

   }

   private static void fromTag(EntityAttributeInstance instance, CompoundTag tag) {
      instance.setBaseValue(tag.getDouble("Base"));
      if (tag.contains("Modifiers", 9)) {
         ListTag listTag = tag.getList("Modifiers", 10);

         for(int i = 0; i < listTag.size(); ++i) {
            EntityAttributeModifier entityAttributeModifier = createFromTag(listTag.getCompound(i));
            if (entityAttributeModifier != null) {
               EntityAttributeModifier entityAttributeModifier2 = instance.getModifier(entityAttributeModifier.getId());
               if (entityAttributeModifier2 != null) {
                  instance.removeModifier(entityAttributeModifier2);
               }

               instance.addModifier(entityAttributeModifier);
            }
         }
      }

   }

   @Nullable
   public static EntityAttributeModifier createFromTag(CompoundTag tag) {
      UUID uUID = tag.getUuid("UUID");

      try {
         EntityAttributeModifier.Operation operation = EntityAttributeModifier.Operation.fromId(tag.getInt("Operation"));
         return new EntityAttributeModifier(uUID, tag.getString("Name"), tag.getDouble("Amount"), operation);
      } catch (Exception var3) {
         LOGGER.warn("Unable to create attribute: {}", var3.getMessage());
         return null;
      }
   }
}
