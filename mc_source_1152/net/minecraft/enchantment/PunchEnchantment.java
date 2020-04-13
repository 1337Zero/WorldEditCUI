package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class PunchEnchantment extends Enchantment {
   public PunchEnchantment(Enchantment.Weight weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.BOW, slotTypes);
   }

   public int getMinimumPower(int level) {
      return 12 + (level - 1) * 20;
   }

   public int getMaximumPower(int level) {
      return this.getMinimumPower(level) + 25;
   }

   public int getMaximumLevel() {
      return 2;
   }
}
