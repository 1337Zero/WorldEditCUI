package net.minecraft.container;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class Container {
   private final DefaultedList<ItemStack> stackList = DefaultedList.of();
   public final List<Slot> slotList = Lists.newArrayList();
   private final List<Property> properties = Lists.newArrayList();
   @Nullable
   private final ContainerType<?> type;
   public final int syncId;
   @Environment(EnvType.CLIENT)
   private short actionId;
   private int quickCraftStage = -1;
   private int quickCraftButton;
   private final Set<Slot> quickCraftSlots = Sets.newHashSet();
   private final List<ContainerListener> listeners = Lists.newArrayList();
   private final Set<PlayerEntity> restrictedPlayers = Sets.newHashSet();

   protected Container(@Nullable ContainerType<?> type, int syncId) {
      this.type = type;
      this.syncId = syncId;
   }

   protected static boolean canUse(BlockContext blockContext, PlayerEntity playerEntity, Block block) {
      return (Boolean)blockContext.run((world, blockPos) -> {
         return world.getBlockState(blockPos).getBlock() != block ? false : playerEntity.squaredDistanceTo((double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D) <= 64.0D;
      }, true);
   }

   public ContainerType<?> getType() {
      if (this.type == null) {
         throw new UnsupportedOperationException("Unable to construct this menu by type");
      } else {
         return this.type;
      }
   }

   protected static void checkContainerSize(Inventory inventory, int expectedSize) {
      int i = inventory.getInvSize();
      if (i < expectedSize) {
         throw new IllegalArgumentException("Container size " + i + " is smaller than expected " + expectedSize);
      }
   }

   protected static void checkContainerDataCount(PropertyDelegate data, int expectedCount) {
      int i = data.size();
      if (i < expectedCount) {
         throw new IllegalArgumentException("Container data count " + i + " is smaller than expected " + expectedCount);
      }
   }

   protected Slot addSlot(Slot slot) {
      slot.id = this.slotList.size();
      this.slotList.add(slot);
      this.stackList.add(ItemStack.EMPTY);
      return slot;
   }

   protected Property addProperty(Property property) {
      this.properties.add(property);
      return property;
   }

   protected void addProperties(PropertyDelegate propertyDelegate) {
      for(int i = 0; i < propertyDelegate.size(); ++i) {
         this.addProperty(Property.create(propertyDelegate, i));
      }

   }

   public void addListener(ContainerListener listener) {
      if (!this.listeners.contains(listener)) {
         this.listeners.add(listener);
         listener.onContainerRegistered(this, this.getStacks());
         this.sendContentUpdates();
      }
   }

   @Environment(EnvType.CLIENT)
   public void removeListener(ContainerListener listener) {
      this.listeners.remove(listener);
   }

   public DefaultedList<ItemStack> getStacks() {
      DefaultedList<ItemStack> defaultedList = DefaultedList.of();

      for(int i = 0; i < this.slotList.size(); ++i) {
         defaultedList.add(((Slot)this.slotList.get(i)).getStack());
      }

      return defaultedList;
   }

   public void sendContentUpdates() {
      int j;
      for(j = 0; j < this.slotList.size(); ++j) {
         ItemStack itemStack = ((Slot)this.slotList.get(j)).getStack();
         ItemStack itemStack2 = (ItemStack)this.stackList.get(j);
         if (!ItemStack.areEqualIgnoreDamage(itemStack2, itemStack)) {
            itemStack2 = itemStack.copy();
            this.stackList.set(j, itemStack2);
            Iterator var4 = this.listeners.iterator();

            while(var4.hasNext()) {
               ContainerListener containerListener = (ContainerListener)var4.next();
               containerListener.onContainerSlotUpdate(this, j, itemStack2);
            }
         }
      }

      for(j = 0; j < this.properties.size(); ++j) {
         Property property = (Property)this.properties.get(j);
         if (property.detectChanges()) {
            Iterator var7 = this.listeners.iterator();

            while(var7.hasNext()) {
               ContainerListener containerListener2 = (ContainerListener)var7.next();
               containerListener2.onContainerPropertyUpdate(this, j, property.get());
            }
         }
      }

   }

   public boolean onButtonClick(PlayerEntity player, int id) {
      return false;
   }

   public Slot getSlot(int i) {
      return (Slot)this.slotList.get(i);
   }

   public ItemStack transferSlot(PlayerEntity player, int invSlot) {
      Slot slot = (Slot)this.slotList.get(invSlot);
      return slot != null ? slot.getStack() : ItemStack.EMPTY;
   }

   public ItemStack onSlotClick(int slotId, int clickData, SlotActionType actionType, PlayerEntity playerEntity) {
      ItemStack itemStack = ItemStack.EMPTY;
      PlayerInventory playerInventory = playerEntity.inventory;
      ItemStack itemStack7;
      ItemStack itemStack8;
      int l;
      int j;
      if (actionType == SlotActionType.QUICK_CRAFT) {
         int i = this.quickCraftButton;
         this.quickCraftButton = unpackButtonId(clickData);
         if ((i != 1 || this.quickCraftButton != 2) && i != this.quickCraftButton) {
            this.endQuickCraft();
         } else if (playerInventory.getCursorStack().isEmpty()) {
            this.endQuickCraft();
         } else if (this.quickCraftButton == 0) {
            this.quickCraftStage = unpackQuickCraftStage(clickData);
            if (shouldQuickCraftContinue(this.quickCraftStage, playerEntity)) {
               this.quickCraftButton = 1;
               this.quickCraftSlots.clear();
            } else {
               this.endQuickCraft();
            }
         } else if (this.quickCraftButton == 1) {
            Slot slot = (Slot)this.slotList.get(slotId);
            itemStack8 = playerInventory.getCursorStack();
            if (slot != null && canInsertItemIntoSlot(slot, itemStack8, true) && slot.canInsert(itemStack8) && (this.quickCraftStage == 2 || itemStack8.getCount() > this.quickCraftSlots.size()) && this.canInsertIntoSlot(slot)) {
               this.quickCraftSlots.add(slot);
            }
         } else if (this.quickCraftButton == 2) {
            if (!this.quickCraftSlots.isEmpty()) {
               itemStack7 = playerInventory.getCursorStack().copy();
               j = playerInventory.getCursorStack().getCount();
               Iterator var23 = this.quickCraftSlots.iterator();

               label342:
               while(true) {
                  Slot slot2;
                  ItemStack itemStack4;
                  do {
                     do {
                        do {
                           do {
                              if (!var23.hasNext()) {
                                 itemStack7.setCount(j);
                                 playerInventory.setCursorStack(itemStack7);
                                 break label342;
                              }

                              slot2 = (Slot)var23.next();
                              itemStack4 = playerInventory.getCursorStack();
                           } while(slot2 == null);
                        } while(!canInsertItemIntoSlot(slot2, itemStack4, true));
                     } while(!slot2.canInsert(itemStack4));
                  } while(this.quickCraftStage != 2 && itemStack4.getCount() < this.quickCraftSlots.size());

                  if (this.canInsertIntoSlot(slot2)) {
                     ItemStack itemStack5 = itemStack7.copy();
                     int k = slot2.hasStack() ? slot2.getStack().getCount() : 0;
                     calculateStackSize(this.quickCraftSlots, this.quickCraftStage, itemStack5, k);
                     l = Math.min(itemStack5.getMaxCount(), slot2.getMaxStackAmount(itemStack5));
                     if (itemStack5.getCount() > l) {
                        itemStack5.setCount(l);
                     }

                     j -= itemStack5.getCount() - k;
                     slot2.setStack(itemStack5);
                  }
               }
            }

            this.endQuickCraft();
         } else {
            this.endQuickCraft();
         }
      } else if (this.quickCraftButton != 0) {
         this.endQuickCraft();
      } else {
         Slot slot4;
         int o;
         if (actionType != SlotActionType.PICKUP && actionType != SlotActionType.QUICK_MOVE || clickData != 0 && clickData != 1) {
            if (actionType == SlotActionType.SWAP && clickData >= 0 && clickData < 9) {
               slot4 = (Slot)this.slotList.get(slotId);
               itemStack7 = playerInventory.getInvStack(clickData);
               itemStack8 = slot4.getStack();
               if (!itemStack7.isEmpty() || !itemStack8.isEmpty()) {
                  if (itemStack7.isEmpty()) {
                     if (slot4.canTakeItems(playerEntity)) {
                        playerInventory.setInvStack(clickData, itemStack8);
                        slot4.onTake(itemStack8.getCount());
                        slot4.setStack(ItemStack.EMPTY);
                        slot4.onTakeItem(playerEntity, itemStack8);
                     }
                  } else if (itemStack8.isEmpty()) {
                     if (slot4.canInsert(itemStack7)) {
                        o = slot4.getMaxStackAmount(itemStack7);
                        if (itemStack7.getCount() > o) {
                           slot4.setStack(itemStack7.split(o));
                        } else {
                           slot4.setStack(itemStack7);
                           playerInventory.setInvStack(clickData, ItemStack.EMPTY);
                        }
                     }
                  } else if (slot4.canTakeItems(playerEntity) && slot4.canInsert(itemStack7)) {
                     o = slot4.getMaxStackAmount(itemStack7);
                     if (itemStack7.getCount() > o) {
                        slot4.setStack(itemStack7.split(o));
                        slot4.onTakeItem(playerEntity, itemStack8);
                        if (!playerInventory.insertStack(itemStack8)) {
                           playerEntity.dropItem(itemStack8, true);
                        }
                     } else {
                        slot4.setStack(itemStack7);
                        playerInventory.setInvStack(clickData, itemStack8);
                        slot4.onTakeItem(playerEntity, itemStack8);
                     }
                  }
               }
            } else if (actionType == SlotActionType.CLONE && playerEntity.abilities.creativeMode && playerInventory.getCursorStack().isEmpty() && slotId >= 0) {
               slot4 = (Slot)this.slotList.get(slotId);
               if (slot4 != null && slot4.hasStack()) {
                  itemStack7 = slot4.getStack().copy();
                  itemStack7.setCount(itemStack7.getMaxCount());
                  playerInventory.setCursorStack(itemStack7);
               }
            } else if (actionType == SlotActionType.THROW && playerInventory.getCursorStack().isEmpty() && slotId >= 0) {
               slot4 = (Slot)this.slotList.get(slotId);
               if (slot4 != null && slot4.hasStack() && slot4.canTakeItems(playerEntity)) {
                  itemStack7 = slot4.takeStack(clickData == 0 ? 1 : slot4.getStack().getCount());
                  slot4.onTakeItem(playerEntity, itemStack7);
                  playerEntity.dropItem(itemStack7, true);
               }
            } else if (actionType == SlotActionType.PICKUP_ALL && slotId >= 0) {
               slot4 = (Slot)this.slotList.get(slotId);
               itemStack7 = playerInventory.getCursorStack();
               if (!itemStack7.isEmpty() && (slot4 == null || !slot4.hasStack() || !slot4.canTakeItems(playerEntity))) {
                  j = clickData == 0 ? 0 : this.slotList.size() - 1;
                  o = clickData == 0 ? 1 : -1;

                  for(int u = 0; u < 2; ++u) {
                     for(int v = j; v >= 0 && v < this.slotList.size() && itemStack7.getCount() < itemStack7.getMaxCount(); v += o) {
                        Slot slot9 = (Slot)this.slotList.get(v);
                        if (slot9.hasStack() && canInsertItemIntoSlot(slot9, itemStack7, true) && slot9.canTakeItems(playerEntity) && this.canInsertIntoSlot(itemStack7, slot9)) {
                           ItemStack itemStack14 = slot9.getStack();
                           if (u != 0 || itemStack14.getCount() != itemStack14.getMaxCount()) {
                              l = Math.min(itemStack7.getMaxCount() - itemStack7.getCount(), itemStack14.getCount());
                              ItemStack itemStack15 = slot9.takeStack(l);
                              itemStack7.increment(l);
                              if (itemStack15.isEmpty()) {
                                 slot9.setStack(ItemStack.EMPTY);
                              }

                              slot9.onTakeItem(playerEntity, itemStack15);
                           }
                        }
                     }
                  }
               }

               this.sendContentUpdates();
            }
         } else if (slotId == -999) {
            if (!playerInventory.getCursorStack().isEmpty()) {
               if (clickData == 0) {
                  playerEntity.dropItem(playerInventory.getCursorStack(), true);
                  playerInventory.setCursorStack(ItemStack.EMPTY);
               }

               if (clickData == 1) {
                  playerEntity.dropItem(playerInventory.getCursorStack().split(1), true);
               }
            }
         } else if (actionType == SlotActionType.QUICK_MOVE) {
            if (slotId < 0) {
               return ItemStack.EMPTY;
            }

            slot4 = (Slot)this.slotList.get(slotId);
            if (slot4 == null || !slot4.canTakeItems(playerEntity)) {
               return ItemStack.EMPTY;
            }

            for(itemStack7 = this.transferSlot(playerEntity, slotId); !itemStack7.isEmpty() && ItemStack.areItemsEqualIgnoreDamage(slot4.getStack(), itemStack7); itemStack7 = this.transferSlot(playerEntity, slotId)) {
               itemStack = itemStack7.copy();
            }
         } else {
            if (slotId < 0) {
               return ItemStack.EMPTY;
            }

            slot4 = (Slot)this.slotList.get(slotId);
            if (slot4 != null) {
               itemStack7 = slot4.getStack();
               itemStack8 = playerInventory.getCursorStack();
               if (!itemStack7.isEmpty()) {
                  itemStack = itemStack7.copy();
               }

               if (itemStack7.isEmpty()) {
                  if (!itemStack8.isEmpty() && slot4.canInsert(itemStack8)) {
                     o = clickData == 0 ? itemStack8.getCount() : 1;
                     if (o > slot4.getMaxStackAmount(itemStack8)) {
                        o = slot4.getMaxStackAmount(itemStack8);
                     }

                     slot4.setStack(itemStack8.split(o));
                  }
               } else if (slot4.canTakeItems(playerEntity)) {
                  if (itemStack8.isEmpty()) {
                     if (itemStack7.isEmpty()) {
                        slot4.setStack(ItemStack.EMPTY);
                        playerInventory.setCursorStack(ItemStack.EMPTY);
                     } else {
                        o = clickData == 0 ? itemStack7.getCount() : (itemStack7.getCount() + 1) / 2;
                        playerInventory.setCursorStack(slot4.takeStack(o));
                        if (itemStack7.isEmpty()) {
                           slot4.setStack(ItemStack.EMPTY);
                        }

                        slot4.onTakeItem(playerEntity, playerInventory.getCursorStack());
                     }
                  } else if (slot4.canInsert(itemStack8)) {
                     if (canStacksCombine(itemStack7, itemStack8)) {
                        o = clickData == 0 ? itemStack8.getCount() : 1;
                        if (o > slot4.getMaxStackAmount(itemStack8) - itemStack7.getCount()) {
                           o = slot4.getMaxStackAmount(itemStack8) - itemStack7.getCount();
                        }

                        if (o > itemStack8.getMaxCount() - itemStack7.getCount()) {
                           o = itemStack8.getMaxCount() - itemStack7.getCount();
                        }

                        itemStack8.decrement(o);
                        itemStack7.increment(o);
                     } else if (itemStack8.getCount() <= slot4.getMaxStackAmount(itemStack8)) {
                        slot4.setStack(itemStack8);
                        playerInventory.setCursorStack(itemStack7);
                     }
                  } else if (itemStack8.getMaxCount() > 1 && canStacksCombine(itemStack7, itemStack8) && !itemStack7.isEmpty()) {
                     o = itemStack7.getCount();
                     if (o + itemStack8.getCount() <= itemStack8.getMaxCount()) {
                        itemStack8.increment(o);
                        itemStack7 = slot4.takeStack(o);
                        if (itemStack7.isEmpty()) {
                           slot4.setStack(ItemStack.EMPTY);
                        }

                        slot4.onTakeItem(playerEntity, playerInventory.getCursorStack());
                     }
                  }
               }

               slot4.markDirty();
            }
         }
      }

      return itemStack;
   }

   public static boolean canStacksCombine(ItemStack itemStack, ItemStack itemStack2) {
      return itemStack.getItem() == itemStack2.getItem() && ItemStack.areTagsEqual(itemStack, itemStack2);
   }

   public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
      return true;
   }

   public void close(PlayerEntity player) {
      PlayerInventory playerInventory = player.inventory;
      if (!playerInventory.getCursorStack().isEmpty()) {
         player.dropItem(playerInventory.getCursorStack(), false);
         playerInventory.setCursorStack(ItemStack.EMPTY);
      }

   }

   protected void dropInventory(PlayerEntity playerEntity, World world, Inventory inventory) {
      int j;
      if (!playerEntity.isAlive() || playerEntity instanceof ServerPlayerEntity && ((ServerPlayerEntity)playerEntity).method_14239()) {
         for(j = 0; j < inventory.getInvSize(); ++j) {
            playerEntity.dropItem(inventory.removeInvStack(j), false);
         }

      } else {
         for(j = 0; j < inventory.getInvSize(); ++j) {
            playerEntity.inventory.offerOrDrop(world, inventory.removeInvStack(j));
         }

      }
   }

   public void onContentChanged(Inventory inventory) {
      this.sendContentUpdates();
   }

   public void setStackInSlot(int slot, ItemStack itemStack) {
      this.getSlot(slot).setStack(itemStack);
   }

   @Environment(EnvType.CLIENT)
   public void updateSlotStacks(List<ItemStack> stacks) {
      for(int i = 0; i < stacks.size(); ++i) {
         this.getSlot(i).setStack((ItemStack)stacks.get(i));
      }

   }

   public void setProperties(int pos, int propertyId) {
      ((Property)this.properties.get(pos)).set(propertyId);
   }

   @Environment(EnvType.CLIENT)
   public short getNextActionId(PlayerInventory playerInventory) {
      ++this.actionId;
      return this.actionId;
   }

   public boolean isRestricted(PlayerEntity playerEntity) {
      return !this.restrictedPlayers.contains(playerEntity);
   }

   public void setPlayerRestriction(PlayerEntity playerEntity, boolean unrestricted) {
      if (unrestricted) {
         this.restrictedPlayers.remove(playerEntity);
      } else {
         this.restrictedPlayers.add(playerEntity);
      }

   }

   public abstract boolean canUse(PlayerEntity player);

   protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
      boolean bl = false;
      int i = startIndex;
      if (fromLast) {
         i = endIndex - 1;
      }

      Slot slot2;
      ItemStack itemStack;
      if (stack.isStackable()) {
         while(!stack.isEmpty()) {
            if (fromLast) {
               if (i < startIndex) {
                  break;
               }
            } else if (i >= endIndex) {
               break;
            }

            slot2 = (Slot)this.slotList.get(i);
            itemStack = slot2.getStack();
            if (!itemStack.isEmpty() && canStacksCombine(stack, itemStack)) {
               int j = itemStack.getCount() + stack.getCount();
               if (j <= stack.getMaxCount()) {
                  stack.setCount(0);
                  itemStack.setCount(j);
                  slot2.markDirty();
                  bl = true;
               } else if (itemStack.getCount() < stack.getMaxCount()) {
                  stack.decrement(stack.getMaxCount() - itemStack.getCount());
                  itemStack.setCount(stack.getMaxCount());
                  slot2.markDirty();
                  bl = true;
               }
            }

            if (fromLast) {
               --i;
            } else {
               ++i;
            }
         }
      }

      if (!stack.isEmpty()) {
         if (fromLast) {
            i = endIndex - 1;
         } else {
            i = startIndex;
         }

         while(true) {
            if (fromLast) {
               if (i < startIndex) {
                  break;
               }
            } else if (i >= endIndex) {
               break;
            }

            slot2 = (Slot)this.slotList.get(i);
            itemStack = slot2.getStack();
            if (itemStack.isEmpty() && slot2.canInsert(stack)) {
               if (stack.getCount() > slot2.getMaxStackAmount()) {
                  slot2.setStack(stack.split(slot2.getMaxStackAmount()));
               } else {
                  slot2.setStack(stack.split(stack.getCount()));
               }

               slot2.markDirty();
               bl = true;
               break;
            }

            if (fromLast) {
               --i;
            } else {
               ++i;
            }
         }
      }

      return bl;
   }

   public static int unpackQuickCraftStage(int clickData) {
      return clickData >> 2 & 3;
   }

   public static int unpackButtonId(int clickData) {
      return clickData & 3;
   }

   @Environment(EnvType.CLIENT)
   public static int packClickData(int buttonId, int quickCraftStage) {
      return buttonId & 3 | (quickCraftStage & 3) << 2;
   }

   public static boolean shouldQuickCraftContinue(int i, PlayerEntity playerEntity) {
      if (i == 0) {
         return true;
      } else if (i == 1) {
         return true;
      } else {
         return i == 2 && playerEntity.abilities.creativeMode;
      }
   }

   protected void endQuickCraft() {
      this.quickCraftButton = 0;
      this.quickCraftSlots.clear();
   }

   public static boolean canInsertItemIntoSlot(@Nullable Slot slot, ItemStack stack, boolean bl) {
      boolean bl2 = slot == null || !slot.hasStack();
      if (!bl2 && stack.isItemEqualIgnoreDamage(slot.getStack()) && ItemStack.areTagsEqual(slot.getStack(), stack)) {
         return slot.getStack().getCount() + (bl ? 0 : stack.getCount()) <= stack.getMaxCount();
      } else {
         return bl2;
      }
   }

   public static void calculateStackSize(Set<Slot> slots, int rmode, ItemStack stack, int stackSize) {
      switch(rmode) {
      case 0:
         stack.setCount(MathHelper.floor((float)stack.getCount() / (float)slots.size()));
         break;
      case 1:
         stack.setCount(1);
         break;
      case 2:
         stack.setCount(stack.getItem().getMaxCount());
      }

      stack.increment(stackSize);
   }

   public boolean canInsertIntoSlot(Slot slot) {
      return true;
   }

   public static int calculateComparatorOutput(@Nullable BlockEntity entity) {
      return entity instanceof Inventory ? calculateComparatorOutput((Inventory)entity) : 0;
   }

   public static int calculateComparatorOutput(@Nullable Inventory inventory) {
      if (inventory == null) {
         return 0;
      } else {
         int i = 0;
         float f = 0.0F;

         for(int j = 0; j < inventory.getInvSize(); ++j) {
            ItemStack itemStack = inventory.getInvStack(j);
            if (!itemStack.isEmpty()) {
               f += (float)itemStack.getCount() / (float)Math.min(inventory.getInvMaxStackAmount(), itemStack.getMaxCount());
               ++i;
            }
         }

         f /= (float)inventory.getInvSize();
         return MathHelper.floor(f * 14.0F) + (i > 0 ? 1 : 0);
      }
   }
}
