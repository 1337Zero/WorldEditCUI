package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class DepthStriderEnchantment extends Enchantment {
   public DepthStriderEnchantment(Enchantment.Weight weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.ARMOR_FEET, slotTypes);
   }

   public int getMinimumPower(int level) {
      return level * 10;
   }

   public int getMaximumPower(int level) {
      return this.getMinimumPower(level) + 15;
   }

   public int getMaximumLevel() {
      return 3;
   }

   public boolean differs(Enchantment other) {
      return super.differs(other) && other != Enchantments.FROST_WALKER;
   }
}
