package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class LuckEnchantment extends Enchantment {
   protected LuckEnchantment(Enchantment.Weight weight, EnchantmentTarget type, EquipmentSlot... slotTypes) {
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

   public boolean differs(Enchantment other) {
      return super.differs(other) && other != Enchantments.SILK_TOUCH;
   }
}
