package net.minecraft.enchantment;

import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;

public class ImpalingEnchantment extends Enchantment {
   public ImpalingEnchantment(Enchantment.Weight weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.TRIDENT, slotTypes);
   }

   public int getMinimumPower(int level) {
      return 1 + (level - 1) * 8;
   }

   public int getMaximumPower(int level) {
      return this.getMinimumPower(level) + 20;
   }

   public int getMaximumLevel() {
      return 5;
   }

   public float getAttackDamage(int level, EntityGroup group) {
      return group == EntityGroup.AQUATIC ? (float)level * 2.5F : 0.0F;
   }
}
