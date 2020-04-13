package net.minecraft.container;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

public class GrindstoneContainer extends Container {
   private final Inventory resultInventory;
   private final Inventory craftingInventory;
   private final BlockContext context;

   public GrindstoneContainer(int syncId, PlayerInventory playerInventory) {
      this(syncId, playerInventory, BlockContext.EMPTY);
   }

   public GrindstoneContainer(int syncId, PlayerInventory playerInventory, final BlockContext blockContext) {
      super(ContainerType.GRINDSTONE, syncId);
      this.resultInventory = new CraftingResultInventory();
      this.craftingInventory = new BasicInventory(2) {
         public void markDirty() {
            super.markDirty();
            GrindstoneContainer.this.onContentChanged(this);
         }
      };
      this.context = blockContext;
      this.addSlot(new Slot(this.craftingInventory, 0, 49, 19) {
         public boolean canInsert(ItemStack stack) {
            return stack.isDamageable() || stack.getItem() == Items.ENCHANTED_BOOK || stack.hasEnchantments();
         }
      });
      this.addSlot(new Slot(this.craftingInventory, 1, 49, 40) {
         public boolean canInsert(ItemStack stack) {
            return stack.isDamageable() || stack.getItem() == Items.ENCHANTED_BOOK || stack.hasEnchantments();
         }
      });
      this.addSlot(new Slot(this.resultInventory, 2, 129, 34) {
         public boolean canInsert(ItemStack stack) {
            return false;
         }

         public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
            blockContext.run((world, blockPos) -> {
               int i = this.getExperience(world);

               while(i > 0) {
                  int j = ExperienceOrbEntity.roundToOrbSize(i);
                  i -= j;
                  world.spawnEntity(new ExperienceOrbEntity(world, (double)blockPos.getX(), (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D, j));
               }

               world.playLevelEvent(1042, blockPos, 0);
            });
            GrindstoneContainer.this.craftingInventory.setInvStack(0, ItemStack.EMPTY);
            GrindstoneContainer.this.craftingInventory.setInvStack(1, ItemStack.EMPTY);
            return stack;
         }

         private int getExperience(World world) {
            int ix = 0;
            int i = ix + this.getExperience(GrindstoneContainer.this.craftingInventory.getInvStack(0));
            i += this.getExperience(GrindstoneContainer.this.craftingInventory.getInvStack(1));
            if (i > 0) {
               int j = (int)Math.ceil((double)i / 2.0D);
               return j + world.random.nextInt(j);
            } else {
               return 0;
            }
         }

         private int getExperience(ItemStack stack) {
            int i = 0;
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
            Iterator var4 = map.entrySet().iterator();

            while(var4.hasNext()) {
               Entry<Enchantment, Integer> entry = (Entry)var4.next();
               Enchantment enchantment = (Enchantment)entry.getKey();
               Integer integer = (Integer)entry.getValue();
               if (!enchantment.isCursed()) {
                  i += enchantment.getMinimumPower(integer);
               }
            }

            return i;
         }
      });

      int k;
      for(k = 0; k < 3; ++k) {
         for(int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j + k * 9 + 9, 8 + j * 18, 84 + k * 18));
         }
      }

      for(k = 0; k < 9; ++k) {
         this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
      }

   }

   public void onContentChanged(Inventory inventory) {
      super.onContentChanged(inventory);
      if (inventory == this.craftingInventory) {
         this.updateResult();
      }

   }

   private void updateResult() {
      ItemStack itemStack = this.craftingInventory.getInvStack(0);
      ItemStack itemStack2 = this.craftingInventory.getInvStack(1);
      boolean bl = !itemStack.isEmpty() || !itemStack2.isEmpty();
      boolean bl2 = !itemStack.isEmpty() && !itemStack2.isEmpty();
      if (!bl) {
         this.resultInventory.setInvStack(0, ItemStack.EMPTY);
      } else {
         boolean bl3 = !itemStack.isEmpty() && itemStack.getItem() != Items.ENCHANTED_BOOK && !itemStack.hasEnchantments() || !itemStack2.isEmpty() && itemStack2.getItem() != Items.ENCHANTED_BOOK && !itemStack2.hasEnchantments();
         if (itemStack.getCount() > 1 || itemStack2.getCount() > 1 || !bl2 && bl3) {
            this.resultInventory.setInvStack(0, ItemStack.EMPTY);
            this.sendContentUpdates();
            return;
         }

         int i = 1;
         int m;
         ItemStack itemStack3;
         if (bl2) {
            if (itemStack.getItem() != itemStack2.getItem()) {
               this.resultInventory.setInvStack(0, ItemStack.EMPTY);
               this.sendContentUpdates();
               return;
            }

            Item item = itemStack.getItem();
            int j = item.getMaxDamage() - itemStack.getDamage();
            int k = item.getMaxDamage() - itemStack2.getDamage();
            int l = j + k + item.getMaxDamage() * 5 / 100;
            m = Math.max(item.getMaxDamage() - l, 0);
            itemStack3 = this.transferEnchantments(itemStack, itemStack2);
            if (!itemStack3.isDamageable()) {
               if (!ItemStack.areEqualIgnoreDamage(itemStack, itemStack2)) {
                  this.resultInventory.setInvStack(0, ItemStack.EMPTY);
                  this.sendContentUpdates();
                  return;
               }

               i = 2;
            }
         } else {
            boolean bl4 = !itemStack.isEmpty();
            m = bl4 ? itemStack.getDamage() : itemStack2.getDamage();
            itemStack3 = bl4 ? itemStack : itemStack2;
         }

         this.resultInventory.setInvStack(0, this.grind(itemStack3, m, i));
      }

      this.sendContentUpdates();
   }

   private ItemStack transferEnchantments(ItemStack target, ItemStack source) {
      ItemStack itemStack = target.copy();
      Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(source);
      Iterator var5 = map.entrySet().iterator();

      while(true) {
         Entry entry;
         Enchantment enchantment;
         do {
            if (!var5.hasNext()) {
               return itemStack;
            }

            entry = (Entry)var5.next();
            enchantment = (Enchantment)entry.getKey();
         } while(enchantment.isCursed() && EnchantmentHelper.getLevel(enchantment, itemStack) != 0);

         itemStack.addEnchantment(enchantment, (Integer)entry.getValue());
      }
   }

   private ItemStack grind(ItemStack item, int damage, int amount) {
      ItemStack itemStack = item.copy();
      itemStack.removeSubTag("Enchantments");
      itemStack.removeSubTag("StoredEnchantments");
      if (damage > 0) {
         itemStack.setDamage(damage);
      } else {
         itemStack.removeSubTag("Damage");
      }

      itemStack.setCount(amount);
      Map<Enchantment, Integer> map = (Map)EnchantmentHelper.getEnchantments(item).entrySet().stream().filter((entry) -> {
         return ((Enchantment)entry.getKey()).isCursed();
      }).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
      EnchantmentHelper.set(map, itemStack);
      itemStack.setRepairCost(0);
      if (itemStack.getItem() == Items.ENCHANTED_BOOK && map.size() == 0) {
         itemStack = new ItemStack(Items.BOOK);
         if (item.hasCustomName()) {
            itemStack.setCustomName(item.getName());
         }
      }

      for(int i = 0; i < map.size(); ++i) {
         itemStack.setRepairCost(AnvilContainer.getNextCost(itemStack.getRepairCost()));
      }

      return itemStack;
   }

   public void close(PlayerEntity player) {
      super.close(player);
      this.context.run((world, blockPos) -> {
         this.dropInventory(player, world, this.craftingInventory);
      });
   }

   public boolean canUse(PlayerEntity player) {
      return canUse(this.context, player, Blocks.GRINDSTONE);
   }

   public ItemStack transferSlot(PlayerEntity player, int invSlot) {
      ItemStack itemStack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slotList.get(invSlot);
      if (slot != null && slot.hasStack()) {
         ItemStack itemStack2 = slot.getStack();
         itemStack = itemStack2.copy();
         ItemStack itemStack3 = this.craftingInventory.getInvStack(0);
         ItemStack itemStack4 = this.craftingInventory.getInvStack(1);
         if (invSlot == 2) {
            if (!this.insertItem(itemStack2, 3, 39, true)) {
               return ItemStack.EMPTY;
            }

            slot.onStackChanged(itemStack2, itemStack);
         } else if (invSlot != 0 && invSlot != 1) {
            if (!itemStack3.isEmpty() && !itemStack4.isEmpty()) {
               if (invSlot >= 3 && invSlot < 30) {
                  if (!this.insertItem(itemStack2, 30, 39, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if (invSlot >= 30 && invSlot < 39 && !this.insertItem(itemStack2, 3, 30, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.insertItem(itemStack2, 0, 2, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.insertItem(itemStack2, 3, 39, false)) {
            return ItemStack.EMPTY;
         }

         if (itemStack2.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
         } else {
            slot.markDirty();
         }

         if (itemStack2.getCount() == itemStack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTakeItem(player, itemStack2);
      }

      return itemStack;
   }
}
