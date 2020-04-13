package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class PowerEnchantment extends Enchantment {
   public PowerEnchantment(Enchantment.Weight weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.BOW, slotTypes);
   }

   public int getMinimumPower(int level) {
      return 1 + (level - 1) * 10;
   }

   public int getMaximumPower(int level) {
      return this.getMinimumPower(level) + 15;
   }

   public int getMaximumLevel() {
      return 5;
   }
}
