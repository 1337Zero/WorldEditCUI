package net.minecraft.enchantment;

import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;

public enum EnchantmentTarget {
   ALL {
      public boolean isAcceptableItem(Item item) {
         EnchantmentTarget[] var2 = EnchantmentTarget.values();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            EnchantmentTarget enchantmentTarget = var2[var4];
            if (enchantmentTarget != EnchantmentTarget.ALL && enchantmentTarget.isAcceptableItem(item)) {
               return true;
            }
         }

         return false;
      }
   },
   ARMOR {
      public boolean isAcceptableItem(Item item) {
         return item instanceof ArmorItem;
      }
   },
   ARMOR_FEET {
      public boolean isAcceptableItem(Item item) {
         return item instanceof ArmorItem && ((ArmorItem)item).getSlotType() == EquipmentSlot.FEET;
      }
   },
   ARMOR_LEGS {
      public boolean isAcceptableItem(Item item) {
         return item instanceof ArmorItem && ((ArmorItem)item).getSlotType() == EquipmentSlot.LEGS;
      }
   },
   ARMOR_CHEST {
      public boolean isAcceptableItem(Item item) {
         return item instanceof ArmorItem && ((ArmorItem)item).getSlotType() == EquipmentSlot.CHEST;
      }
   },
   ARMOR_HEAD {
      public boolean isAcceptableItem(Item item) {
         return item instanceof ArmorItem && ((ArmorItem)item).getSlotType() == EquipmentSlot.HEAD;
      }
   },
   WEAPON {
      public boolean isAcceptableItem(Item item) {
         return item instanceof SwordItem;
      }
   },
   DIGGER {
      public boolean isAcceptableItem(Item item) {
         return item instanceof MiningToolItem;
      }
   },
   FISHING_ROD {
      public boolean isAcceptableItem(Item item) {
         return item instanceof FishingRodItem;
      }
   },
   TRIDENT {
      public boolean isAcceptableItem(Item item) {
         return item instanceof TridentItem;
      }
   },
   BREAKABLE {
      public boolean isAcceptableItem(Item item) {
         return item.isDamageable();
      }
   },
   BOW {
      public boolean isAcceptableItem(Item item) {
         return item instanceof BowItem;
      }
   },
   WEARABLE {
      public boolean isAcceptableItem(Item item) {
         Block block = Block.getBlockFromItem(item);
         return item instanceof ArmorItem || item instanceof ElytraItem || block instanceof AbstractSkullBlock || block instanceof CarvedPumpkinBlock;
      }
   },
   CROSSBOW {
      public boolean isAcceptableItem(Item item) {
         return item instanceof CrossbowItem;
      }
   };

   private EnchantmentTarget() {
   }

   public abstract boolean isAcceptableItem(Item item);
}
