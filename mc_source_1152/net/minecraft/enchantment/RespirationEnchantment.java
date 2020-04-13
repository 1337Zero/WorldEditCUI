package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class RespirationEnchantment extends Enchantment {
   public RespirationEnchantment(Enchantment.Weight weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.ARMOR_HEAD, slotTypes);
   }

   public int getMinimumPower(int level) {
      return 10 * level;
   }

   public int getMaximumPower(int level) {
      return this.getMinimumPower(level) + 30;
   }

   public int getMaximumLevel() {
      return 3;
   }
}
