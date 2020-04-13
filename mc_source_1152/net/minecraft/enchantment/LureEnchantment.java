package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class LureEnchantment extends Enchantment {
   protected LureEnchantment(Enchantment.Weight weight, EnchantmentTarget type, EquipmentSlot... slotTypes) {
      super(weight, type, slotTypes);
   }

   public int getMinimumPower(int level) {
      return 15 + (level - 1) * 9;
   }

   public int getMaximumPower(int level) {
      return super.getMinimumPower(level) + 50;
   }

   public int getMaximumLevel() {
      return 3;
   }
}
