package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class VanishingCurseEnchantment extends Enchantment {
   public VanishingCurseEnchantment(Enchantment.Weight weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.ALL, slotTypes);
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

   public boolean isTreasure() {
      return true;
   }

   public boolean isCursed() {
      return true;
   }
}
