package net.minecraft.container;

import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criterions;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.InfoEnchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.registry.Registry;

public class EnchantingTableContainer extends Container {
   private final Inventory inventory;
   private final BlockContext context;
   private final Random random;
   private final Property seed;
   public final int[] enchantmentPower;
   public final int[] enchantmentId;
   public final int[] enchantmentLevel;

   public EnchantingTableContainer(int syncId, PlayerInventory playerInventory) {
      this(syncId, playerInventory, BlockContext.EMPTY);
   }

   public EnchantingTableContainer(int syncId, PlayerInventory playerInventory, BlockContext blockContext) {
      super(ContainerType.ENCHANTMENT, syncId);
      this.inventory = new BasicInventory(2) {
         public void markDirty() {
            super.markDirty();
            EnchantingTableContainer.this.onContentChanged(this);
         }
      };
      this.random = new Random();
      this.seed = Property.create();
      this.enchantmentPower = new int[3];
      this.enchantmentId = new int[]{-1, -1, -1};
      this.enchantmentLevel = new int[]{-1, -1, -1};
      this.context = blockContext;
      this.addSlot(new Slot(this.inventory, 0, 15, 47) {
         public boolean canInsert(ItemStack stack) {
            return true;
         }

         public int getMaxStackAmount() {
            return 1;
         }
      });
      this.addSlot(new Slot(this.inventory, 1, 35, 47) {
         public boolean canInsert(ItemStack stack) {
            return stack.getItem() == Items.LAPIS_LAZULI;
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

      this.addProperty(Property.create((int[])this.enchantmentPower, 0));
      this.addProperty(Property.create((int[])this.enchantmentPower, 1));
      this.addProperty(Property.create((int[])this.enchantmentPower, 2));
      this.addProperty(this.seed).set(playerInventory.player.getEnchantmentTableSeed());
      this.addProperty(Property.create((int[])this.enchantmentId, 0));
      this.addProperty(Property.create((int[])this.enchantmentId, 1));
      this.addProperty(Property.create((int[])this.enchantmentId, 2));
      this.addProperty(Property.create((int[])this.enchantmentLevel, 0));
      this.addProperty(Property.create((int[])this.enchantmentLevel, 1));
      this.addProperty(Property.create((int[])this.enchantmentLevel, 2));
   }

   public void onContentChanged(Inventory inventory) {
      if (inventory == this.inventory) {
         ItemStack itemStack = inventory.getInvStack(0);
         if (!itemStack.isEmpty() && itemStack.isEnchantable()) {
            this.context.run((world, blockPos) -> {
               int i = 0;

               int j;
               for(j = -1; j <= 1; ++j) {
                  for(int k = -1; k <= 1; ++k) {
                     if ((j != 0 || k != 0) && world.isAir(blockPos.add(k, 0, j)) && world.isAir(blockPos.add(k, 1, j))) {
                        if (world.getBlockState(blockPos.add(k * 2, 0, j * 2)).getBlock() == Blocks.BOOKSHELF) {
                           ++i;
                        }

                        if (world.getBlockState(blockPos.add(k * 2, 1, j * 2)).getBlock() == Blocks.BOOKSHELF) {
                           ++i;
                        }

                        if (k != 0 && j != 0) {
                           if (world.getBlockState(blockPos.add(k * 2, 0, j)).getBlock() == Blocks.BOOKSHELF) {
                              ++i;
                           }

                           if (world.getBlockState(blockPos.add(k * 2, 1, j)).getBlock() == Blocks.BOOKSHELF) {
                              ++i;
                           }

                           if (world.getBlockState(blockPos.add(k, 0, j * 2)).getBlock() == Blocks.BOOKSHELF) {
                              ++i;
                           }

                           if (world.getBlockState(blockPos.add(k, 1, j * 2)).getBlock() == Blocks.BOOKSHELF) {
                              ++i;
                           }
                        }
                     }
                  }
               }

               this.random.setSeed((long)this.seed.get());

               for(j = 0; j < 3; ++j) {
                  this.enchantmentPower[j] = EnchantmentHelper.calculateEnchantmentPower(this.random, j, i, itemStack);
                  this.enchantmentId[j] = -1;
                  this.enchantmentLevel[j] = -1;
                  if (this.enchantmentPower[j] < j + 1) {
                     this.enchantmentPower[j] = 0;
                  }
               }

               for(j = 0; j < 3; ++j) {
                  if (this.enchantmentPower[j] > 0) {
                     List<InfoEnchantment> list = this.getRandomEnchantments(itemStack, j, this.enchantmentPower[j]);
                     if (list != null && !list.isEmpty()) {
                        InfoEnchantment infoEnchantment = (InfoEnchantment)list.get(this.random.nextInt(list.size()));
                        this.enchantmentId[j] = Registry.ENCHANTMENT.getRawId(infoEnchantment.enchantment);
                        this.enchantmentLevel[j] = infoEnchantment.level;
                     }
                  }
               }

               this.sendContentUpdates();
            });
         } else {
            for(int i = 0; i < 3; ++i) {
               this.enchantmentPower[i] = 0;
               this.enchantmentId[i] = -1;
               this.enchantmentLevel[i] = -1;
            }
         }
      }

   }

   public boolean onButtonClick(PlayerEntity player, int id) {
      ItemStack itemStack = this.inventory.getInvStack(0);
      ItemStack itemStack2 = this.inventory.getInvStack(1);
      int i = id + 1;
      if ((itemStack2.isEmpty() || itemStack2.getCount() < i) && !player.abilities.creativeMode) {
         return false;
      } else if (this.enchantmentPower[id] <= 0 || itemStack.isEmpty() || (player.experienceLevel < i || player.experienceLevel < this.enchantmentPower[id]) && !player.abilities.creativeMode) {
         return false;
      } else {
         this.context.run((world, blockPos) -> {
            ItemStack itemStack3 = itemStack;
            List<InfoEnchantment> list = this.getRandomEnchantments(itemStack, id, this.enchantmentPower[id]);
            if (!list.isEmpty()) {
               player.applyEnchantmentCosts(itemStack, i);
               boolean bl = itemStack.getItem() == Items.BOOK;
               if (bl) {
                  itemStack3 = new ItemStack(Items.ENCHANTED_BOOK);
                  this.inventory.setInvStack(0, itemStack3);
               }

               for(int k = 0; k < list.size(); ++k) {
                  InfoEnchantment infoEnchantment = (InfoEnchantment)list.get(k);
                  if (bl) {
                     EnchantedBookItem.addEnchantment(itemStack3, infoEnchantment);
                  } else {
                     itemStack3.addEnchantment(infoEnchantment.enchantment, infoEnchantment.level);
                  }
               }

               if (!player.abilities.creativeMode) {
                  itemStack2.decrement(i);
                  if (itemStack2.isEmpty()) {
                     this.inventory.setInvStack(1, ItemStack.EMPTY);
                  }
               }

               player.incrementStat(Stats.ENCHANT_ITEM);
               if (player instanceof ServerPlayerEntity) {
                  Criterions.ENCHANTED_ITEM.trigger((ServerPlayerEntity)player, itemStack3, i);
               }

               this.inventory.markDirty();
               this.seed.set(player.getEnchantmentTableSeed());
               this.onContentChanged(this.inventory);
               world.playSound((PlayerEntity)null, blockPos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
            }

         });
         return true;
      }
   }

   private List<InfoEnchantment> getRandomEnchantments(ItemStack stack, int i, int j) {
      this.random.setSeed((long)(this.seed.get() + i));
      List<InfoEnchantment> list = EnchantmentHelper.getEnchantments(this.random, stack, j, false);
      if (stack.getItem() == Items.BOOK && list.size() > 1) {
         list.remove(this.random.nextInt(list.size()));
      }

      return list;
   }

   @Environment(EnvType.CLIENT)
   public int getLapisCount() {
      ItemStack itemStack = this.inventory.getInvStack(1);
      return itemStack.isEmpty() ? 0 : itemStack.getCount();
   }

   @Environment(EnvType.CLIENT)
   public int getSeed() {
      return this.seed.get();
   }

   public void close(PlayerEntity player) {
      super.close(player);
      this.context.run((world, blockPos) -> {
         this.dropInventory(player, player.world, this.inventory);
      });
   }

   public boolean canUse(PlayerEntity player) {
      return canUse(this.context, player, Blocks.ENCHANTING_TABLE);
   }

   public ItemStack transferSlot(PlayerEntity player, int invSlot) {
      ItemStack itemStack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slotList.get(invSlot);
      if (slot != null && slot.hasStack()) {
         ItemStack itemStack2 = slot.getStack();
         itemStack = itemStack2.copy();
         if (invSlot == 0) {
            if (!this.insertItem(itemStack2, 2, 38, true)) {
               return ItemStack.EMPTY;
            }
         } else if (invSlot == 1) {
            if (!this.insertItem(itemStack2, 2, 38, true)) {
               return ItemStack.EMPTY;
            }
         } else if (itemStack2.getItem() == Items.LAPIS_LAZULI) {
            if (!this.insertItem(itemStack2, 1, 2, true)) {
               return ItemStack.EMPTY;
            }
         } else {
            if (((Slot)this.slotList.get(0)).hasStack() || !((Slot)this.slotList.get(0)).canInsert(itemStack2)) {
               return ItemStack.EMPTY;
            }

            if (itemStack2.hasTag() && itemStack2.getCount() == 1) {
               ((Slot)this.slotList.get(0)).setStack(itemStack2.copy());
               itemStack2.setCount(0);
            } else if (!itemStack2.isEmpty()) {
               ((Slot)this.slotList.get(0)).setStack(new ItemStack(itemStack2.getItem()));
               itemStack2.decrement(1);
            }
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
