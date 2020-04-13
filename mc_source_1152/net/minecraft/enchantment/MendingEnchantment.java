package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class MendingEnchantment extends Enchantment {
   public MendingEnchantment(Enchantment.Weight weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.BREAKABLE, slotTypes);
   }

   public int getMinimumPower(int level) {
      return level * 25;
   }

   public int getMaximumPower(int level) {
      return this.getMinimumPower(level) + 50;
   }

   public boolean isTreasure() {
      return true;
   }

   public int getMaximumLevel() {
      return 1;
   }
}
