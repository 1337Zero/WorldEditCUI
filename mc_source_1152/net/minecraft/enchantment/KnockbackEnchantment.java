package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class KnockbackEnchantment extends Enchantment {
   protected KnockbackEnchantment(Enchantment.Weight weight, EquipmentSlot... slot) {
      super(weight, EnchantmentTarget.WEAPON, slot);
   }

   public int getMinimumPower(int level) {
      return 5 + 20 * (level - 1);
   }

   public int getMaximumPower(int level) {
      return super.getMinimumPower(level) + 50;
   }

   public int getMaximumLevel() {
      return 2;
   }
}
