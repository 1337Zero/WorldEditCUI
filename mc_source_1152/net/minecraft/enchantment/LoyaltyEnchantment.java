package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class LoyaltyEnchantment extends Enchantment {
   public LoyaltyEnchantment(Enchantment.Weight weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.TRIDENT, slotTypes);
   }

   public int getMinimumPower(int level) {
      return 5 + level * 7;
   }

   public int getMaximumPower(int level) {
      return 50;
   }

   public int getMaximumLevel() {
      return 3;
   }

   public boolean differs(Enchantment other) {
      return super.differs(other);
   }
}
