package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class FlameEnchantment extends Enchantment {
   public FlameEnchantment(Enchantment.Weight weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.BOW, slotTypes);
   }

   public int getMinimumPower(int level) {
      return 20;
   }

   public int getMaximumPower(int level) {
      return 50;
   }

   public int getMaximumLevel() {
      return 1;
   }
}
