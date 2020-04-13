package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class MultishotEnchantment extends Enchantment {
   public MultishotEnchantment(Enchantment.Weight weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.CROSSBOW, slotTypes);
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
      return super.differs(other) && other != Enchantments.PIERCING;
   }
}
