package net.minecraft.entity.effect;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AbstractEntityAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

public class StatusEffect {
   private final Map<EntityAttribute, EntityAttributeModifier> attributeModifiers = Maps.newHashMap();
   private final StatusEffectType type;
   private final int color;
   @Nullable
   private String translationKey;

   @Nullable
   public static StatusEffect byRawId(int rawId) {
      return (StatusEffect)Registry.STATUS_EFFECT.get(rawId);
   }

   public static int getRawId(StatusEffect type) {
      return Registry.STATUS_EFFECT.getRawId(type);
   }

   protected StatusEffect(StatusEffectType type, int color) {
      this.type = type;
      this.color = color;
   }

   public void applyUpdateEffect(LivingEntity entity, int i) {
      if (this == StatusEffects.REGENERATION) {
         if (entity.getHealth() < entity.getMaximumHealth()) {
            entity.heal(1.0F);
         }
      } else if (this == StatusEffects.POISON) {
         if (entity.getHealth() > 1.0F) {
            entity.damage(DamageSource.MAGIC, 1.0F);
         }
      } else if (this == StatusEffects.WITHER) {
         entity.damage(DamageSource.WITHER, 1.0F);
      } else if (this == StatusEffects.HUNGER && entity instanceof PlayerEntity) {
         ((PlayerEntity)entity).addExhaustion(0.005F * (float)(i + 1));
      } else if (this == StatusEffects.SATURATION && entity instanceof PlayerEntity) {
         if (!entity.world.isClient) {
            ((PlayerEntity)entity).getHungerManager().add(i + 1, 1.0F);
         }
      } else if ((this != StatusEffects.INSTANT_HEALTH || entity.isUndead()) && (this != StatusEffects.INSTANT_DAMAGE || !entity.isUndead())) {
         if (this == StatusEffects.INSTANT_DAMAGE && !entity.isUndead() || this == StatusEffects.INSTANT_HEALTH && entity.isUndead()) {
            entity.damage(DamageSource.MAGIC, (float)(6 << i));
         }
      } else {
         entity.heal((float)Math.max(4 << i, 0));
      }

   }

   public void applyInstantEffect(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double d) {
      int j;
      if ((this != StatusEffects.INSTANT_HEALTH || target.isUndead()) && (this != StatusEffects.INSTANT_DAMAGE || !target.isUndead())) {
         if ((this != StatusEffects.INSTANT_DAMAGE || target.isUndead()) && (this != StatusEffects.INSTANT_HEALTH || !target.isUndead())) {
            this.applyUpdateEffect(target, amplifier);
         } else {
            j = (int)(d * (double)(6 << amplifier) + 0.5D);
            if (source == null) {
               target.damage(DamageSource.MAGIC, (float)j);
            } else {
               target.damage(DamageSource.magic(source, attacker), (float)j);
            }
         }
      } else {
         j = (int)(d * (double)(4 << amplifier) + 0.5D);
         target.heal((float)j);
      }

   }

   public boolean canApplyUpdateEffect(int duration, int i) {
      int l;
      if (this == StatusEffects.REGENERATION) {
         l = 50 >> i;
         if (l > 0) {
            return duration % l == 0;
         } else {
            return true;
         }
      } else if (this == StatusEffects.POISON) {
         l = 25 >> i;
         if (l > 0) {
            return duration % l == 0;
         } else {
            return true;
         }
      } else if (this == StatusEffects.WITHER) {
         l = 40 >> i;
         if (l > 0) {
            return duration % l == 0;
         } else {
            return true;
         }
      } else {
         return this == StatusEffects.HUNGER;
      }
   }

   public boolean isInstant() {
      return false;
   }

   protected String loadTranslationKey() {
      if (this.translationKey == null) {
         this.translationKey = Util.createTranslationKey("effect", Registry.STATUS_EFFECT.getId(this));
      }

      return this.translationKey;
   }

   public String getTranslationKey() {
      return this.loadTranslationKey();
   }

   public Text getName() {
      return new TranslatableText(this.getTranslationKey(), new Object[0]);
   }

   @Environment(EnvType.CLIENT)
   public StatusEffectType getType() {
      return this.type;
   }

   public int getColor() {
      return this.color;
   }

   public StatusEffect addAttributeModifier(EntityAttribute attribute, String uuid, double amount, EntityAttributeModifier.Operation operation) {
      EntityAttributeModifier entityAttributeModifier = new EntityAttributeModifier(UUID.fromString(uuid), this::getTranslationKey, amount, operation);
      this.attributeModifiers.put(attribute, entityAttributeModifier);
      return this;
   }

   @Environment(EnvType.CLIENT)
   public Map<EntityAttribute, EntityAttributeModifier> getAttributeModifiers() {
      return this.attributeModifiers;
   }

   public void onRemoved(LivingEntity entity, AbstractEntityAttributeContainer attributes, int amplifier) {
      Iterator var4 = this.attributeModifiers.entrySet().iterator();

      while(var4.hasNext()) {
         Entry<EntityAttribute, EntityAttributeModifier> entry = (Entry)var4.next();
         EntityAttributeInstance entityAttributeInstance = attributes.get((EntityAttribute)entry.getKey());
         if (entityAttributeInstance != null) {
            entityAttributeInstance.removeModifier((EntityAttributeModifier)entry.getValue());
         }
      }

   }

   public void onApplied(LivingEntity entity, AbstractEntityAttributeContainer attributes, int amplifier) {
      Iterator var4 = this.attributeModifiers.entrySet().iterator();

      while(var4.hasNext()) {
         Entry<EntityAttribute, EntityAttributeModifier> entry = (Entry)var4.next();
         EntityAttributeInstance entityAttributeInstance = attributes.get((EntityAttribute)entry.getKey());
         if (entityAttributeInstance != null) {
            EntityAttributeModifier entityAttributeModifier = (EntityAttributeModifier)entry.getValue();
            entityAttributeInstance.removeModifier(entityAttributeModifier);
            entityAttributeInstance.addModifier(new EntityAttributeModifier(entityAttributeModifier.getId(), this.getTranslationKey() + " " + amplifier, this.adjustModifierAmount(amplifier, entityAttributeModifier), entityAttributeModifier.getOperation()));
         }
      }

   }

   public double adjustModifierAmount(int amplifier, EntityAttributeModifier modifier) {
      return modifier.getAmount() * (double)(amplifier + 1);
   }

   @Environment(EnvType.CLIENT)
   public boolean isBeneficial() {
      return this.type == StatusEffectType.BENEFICIAL;
   }
}
