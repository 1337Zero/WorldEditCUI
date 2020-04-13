package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class SilkTouchEnchantment extends Enchantment {
   protected SilkTouchEnchantment(Enchantment.Weight weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.DIGGER, slotTypes);
   }

   public int getMinimumPower(int level) {
      return 15;
   }

   public int getMaximumPower(int level) {
      return super.getMinimumPower(level) + 50;
   }

   public int getMaximumLevel() {
      return 1;
   }

   public boolean differs(Enchantment other) {
      return super.differs(other) && other != Enchantments.FORTUNE;
   }
}
