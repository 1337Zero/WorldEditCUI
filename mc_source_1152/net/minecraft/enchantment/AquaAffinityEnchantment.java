package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class AquaAffinityEnchantment extends Enchantment {
   public AquaAffinityEnchantment(Enchantment.Weight weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.ARMOR_HEAD, slotTypes);
   }

   public int getMinimumPower(int level) {
      return 1;
   }

   public int getMaximumPower(int level) {
      return this.getMinimumPower(level) + 40;
   }

   public int getMaximumLevel() {
      return 1;
   }
}
