package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class InfinityEnchantment extends Enchantment {
   public InfinityEnchantment(Enchantment.Weight weight, EquipmentSlot... slotTypes) {
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

   public boolean differs(Enchantment other) {
      return other instanceof MendingEnchantment ? false : super.differs(other);
   }
}
