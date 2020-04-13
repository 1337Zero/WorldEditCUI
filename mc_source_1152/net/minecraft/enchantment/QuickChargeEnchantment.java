package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class QuickChargeEnchantment extends Enchantment {
   public QuickChargeEnchantment(Enchantment.Weight weight, EquipmentSlot... slot) {
      super(weight, EnchantmentTarget.CROSSBOW, slot);
   }

   public int getMinimumPower(int level) {
      return 12 + (level - 1) * 20;
   }

   public int getMaximumPower(int level) {
      return 50;
   }

   public int getMaximumLevel() {
      return 3;
   }
}
