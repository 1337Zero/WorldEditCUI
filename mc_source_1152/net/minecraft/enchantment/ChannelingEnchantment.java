package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class ChannelingEnchantment extends Enchantment {
   public ChannelingEnchantment(Enchantment.Weight weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.TRIDENT, slotTypes);
   }

   public int getMinimumPower(int level) {
      return 25;
   }

   public int getMaximumPower(int level) {
      return 50;
   }

   public int getMaximumLevel() {
      return 1;
   }

   public boolean differs(Enchantment other) {
      return super.differs(other);
   }
}
