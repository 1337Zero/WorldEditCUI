package net.minecraft.item;

public class EnchantedGoldenAppleItem extends Item {
   public EnchantedGoldenAppleItem(Item.Settings settings) {
      super(settings);
   }

   public boolean hasEnchantmentGlint(ItemStack stack) {
      return true;
   }
}
