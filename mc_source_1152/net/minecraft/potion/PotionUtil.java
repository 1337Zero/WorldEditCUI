package net.minecraft.potion;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

public class PotionUtil {
   public static List<StatusEffectInstance> getPotionEffects(ItemStack stack) {
      return getPotionEffects(stack.getTag());
   }

   public static List<StatusEffectInstance> getPotionEffects(Potion potion, Collection<StatusEffectInstance> custom) {
      List<StatusEffectInstance> list = Lists.newArrayList();
      list.addAll(potion.getEffects());
      list.addAll(custom);
      return list;
   }

   public static List<StatusEffectInstance> getPotionEffects(@Nullable CompoundTag tag) {
      List<StatusEffectInstance> list = Lists.newArrayList();
      list.addAll(getPotion(tag).getEffects());
      getCustomPotionEffects(tag, list);
      return list;
   }

   public static List<StatusEffectInstance> getCustomPotionEffects(ItemStack stack) {
      return getCustomPotionEffects(stack.getTag());
   }

   public static List<StatusEffectInstance> getCustomPotionEffects(@Nullable CompoundTag tag) {
      List<StatusEffectInstance> list = Lists.newArrayList();
      getCustomPotionEffects(tag, list);
      return list;
   }

   public static void getCustomPotionEffects(@Nullable CompoundTag tag, List<StatusEffectInstance> list) {
      if (tag != null && tag.contains("CustomPotionEffects", 9)) {
         ListTag listTag = tag.getList("CustomPotionEffects", 10);

         for(int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompound(i);
            StatusEffectInstance statusEffectInstance = StatusEffectInstance.deserialize(compoundTag);
            if (statusEffectInstance != null) {
               list.add(statusEffectInstance);
            }
         }
      }

   }

   public static int getColor(ItemStack stack) {
      CompoundTag compoundTag = stack.getTag();
      if (compoundTag != null && compoundTag.contains("CustomPotionColor", 99)) {
         return compoundTag.getInt("CustomPotionColor");
      } else {
         return getPotion(stack) == Potions.EMPTY ? 16253176 : getColor((Collection)getPotionEffects(stack));
      }
   }

   public static int getColor(Potion potion) {
      return potion == Potions.EMPTY ? 16253176 : getColor((Collection)potion.getEffects());
   }

   public static int getColor(Collection<StatusEffectInstance> effects) {
      int i = 3694022;
      if (effects.isEmpty()) {
         return 3694022;
      } else {
         float f = 0.0F;
         float g = 0.0F;
         float h = 0.0F;
         int j = 0;
         Iterator var6 = effects.iterator();

         while(var6.hasNext()) {
            StatusEffectInstance statusEffectInstance = (StatusEffectInstance)var6.next();
            if (statusEffectInstance.shouldShowParticles()) {
               int k = statusEffectInstance.getEffectType().getColor();
               int l = statusEffectInstance.getAmplifier() + 1;
               f += (float)(l * (k >> 16 & 255)) / 255.0F;
               g += (float)(l * (k >> 8 & 255)) / 255.0F;
               h += (float)(l * (k >> 0 & 255)) / 255.0F;
               j += l;
            }
         }

         if (j == 0) {
            return 0;
         } else {
            f = f / (float)j * 255.0F;
            g = g / (float)j * 255.0F;
            h = h / (float)j * 255.0F;
            return (int)f << 16 | (int)g << 8 | (int)h;
         }
      }
   }

   public static Potion getPotion(ItemStack stack) {
      return getPotion(stack.getTag());
   }

   public static Potion getPotion(@Nullable CompoundTag compound) {
      return compound == null ? Potions.EMPTY : Potion.byId(compound.getString("Potion"));
   }

   public static ItemStack setPotion(ItemStack stack, Potion potion) {
      Identifier identifier = Registry.POTION.getId(potion);
      if (potion == Potions.EMPTY) {
         stack.removeSubTag("Potion");
      } else {
         stack.getOrCreateTag().putString("Potion", identifier.toString());
      }

      return stack;
   }

   public static ItemStack setCustomPotionEffects(ItemStack stack, Collection<StatusEffectInstance> effects) {
      if (effects.isEmpty()) {
         return stack;
      } else {
         CompoundTag compoundTag = stack.getOrCreateTag();
         ListTag listTag = compoundTag.getList("CustomPotionEffects", 9);
         Iterator var4 = effects.iterator();

         while(var4.hasNext()) {
            StatusEffectInstance statusEffectInstance = (StatusEffectInstance)var4.next();
            listTag.add(statusEffectInstance.serialize(new CompoundTag()));
         }

         compoundTag.put("CustomPotionEffects", listTag);
         return stack;
      }
   }

   @Environment(EnvType.CLIENT)
   public static void buildTooltip(ItemStack stack, List<Text> list, float f) {
      List<StatusEffectInstance> list2 = getPotionEffects(stack);
      List<Pair<String, EntityAttributeModifier>> list3 = Lists.newArrayList();
      Iterator var5;
      TranslatableText text;
      StatusEffect statusEffect;
      if (list2.isEmpty()) {
         list.add((new TranslatableText("effect.none", new Object[0])).formatted(Formatting.GRAY));
      } else {
         for(var5 = list2.iterator(); var5.hasNext(); list.add(text.formatted(statusEffect.getType().getFormatting()))) {
            StatusEffectInstance statusEffectInstance = (StatusEffectInstance)var5.next();
            text = new TranslatableText(statusEffectInstance.getTranslationKey(), new Object[0]);
            statusEffect = statusEffectInstance.getEffectType();
            Map<EntityAttribute, EntityAttributeModifier> map = statusEffect.getAttributeModifiers();
            if (!map.isEmpty()) {
               Iterator var10 = map.entrySet().iterator();

               while(var10.hasNext()) {
                  Entry<EntityAttribute, EntityAttributeModifier> entry = (Entry)var10.next();
                  EntityAttributeModifier entityAttributeModifier = (EntityAttributeModifier)entry.getValue();
                  EntityAttributeModifier entityAttributeModifier2 = new EntityAttributeModifier(entityAttributeModifier.getName(), statusEffect.adjustModifierAmount(statusEffectInstance.getAmplifier(), entityAttributeModifier), entityAttributeModifier.getOperation());
                  list3.add(new Pair(((EntityAttribute)entry.getKey()).getId(), entityAttributeModifier2));
               }
            }

            if (statusEffectInstance.getAmplifier() > 0) {
               text.append(" ").append((Text)(new TranslatableText("potion.potency." + statusEffectInstance.getAmplifier(), new Object[0])));
            }

            if (statusEffectInstance.getDuration() > 20) {
               text.append(" (").append(StatusEffectUtil.durationToString(statusEffectInstance, f)).append(")");
            }
         }
      }

      if (!list3.isEmpty()) {
         list.add(new LiteralText(""));
         list.add((new TranslatableText("potion.whenDrank", new Object[0])).formatted(Formatting.DARK_PURPLE));
         var5 = list3.iterator();

         while(var5.hasNext()) {
            Pair<String, EntityAttributeModifier> pair = (Pair)var5.next();
            EntityAttributeModifier entityAttributeModifier3 = (EntityAttributeModifier)pair.getRight();
            double d = entityAttributeModifier3.getAmount();
            double g;
            if (entityAttributeModifier3.getOperation() != EntityAttributeModifier.Operation.MULTIPLY_BASE && entityAttributeModifier3.getOperation() != EntityAttributeModifier.Operation.MULTIPLY_TOTAL) {
               g = entityAttributeModifier3.getAmount();
            } else {
               g = entityAttributeModifier3.getAmount() * 100.0D;
            }

            if (d > 0.0D) {
               list.add((new TranslatableText("attribute.modifier.plus." + entityAttributeModifier3.getOperation().getId(), new Object[]{ItemStack.MODIFIER_FORMAT.format(g), new TranslatableText("attribute.name." + (String)pair.getLeft(), new Object[0])})).formatted(Formatting.BLUE));
            } else if (d < 0.0D) {
               g *= -1.0D;
               list.add((new TranslatableText("attribute.modifier.take." + entityAttributeModifier3.getOperation().getId(), new Object[]{ItemStack.MODIFIER_FORMAT.format(g), new TranslatableText("attribute.name." + (String)pair.getLeft(), new Object[0])})).formatted(Formatting.RED));
            }
         }
      }

   }
}
