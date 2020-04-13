package net.minecraft.item;

public class NetherStarItem extends Item {
   public NetherStarItem(Item.Settings settings) {
      super(settings);
   }

   public boolean hasEnchantmentGlint(ItemStack stack) {
      return true;
   }
}
