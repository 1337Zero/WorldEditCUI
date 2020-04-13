package net.minecraft.container;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.util.Identifier;

public class PlayerContainer extends CraftingContainer<CraftingInventory> {
   public static final Identifier BLOCK_ATLAS_TEXTURE = new Identifier("textures/atlas/blocks.png");
   public static final Identifier EMPTY_HELMET_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_helmet");
   public static final Identifier EMPTY_CHESTPLATE_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_chestplate");
   public static final Identifier EMPTY_LEGGINGS_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_leggings");
   public static final Identifier EMPTY_BOOTS_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_boots");
   public static final Identifier EMPTY_OFFHAND_ARMOR_SLOT = new Identifier("item/empty_armor_slot_shield");
   private static final Identifier[] EMPTY_ARMOR_SLOT_TEXTURES;
   private static final EquipmentSlot[] EQUIPMENT_SLOT_ORDER;
   private final CraftingInventory invCrafting = new CraftingInventory(this, 2, 2);
   private final CraftingResultInventory invCraftingResult = new CraftingResultInventory();
   public final boolean local;
   private final PlayerEntity owner;

   public PlayerContainer(PlayerInventory inventory, boolean local, PlayerEntity playerEntity) {
      super((ContainerType)null, 0);
      this.local = local;
      this.owner = playerEntity;
      this.addSlot(new CraftingResultSlot(inventory.player, this.invCrafting, this.invCraftingResult, 0, 154, 28));

      int n;
      int m;
      for(n = 0; n < 2; ++n) {
         for(m = 0; m < 2; ++m) {
            this.addSlot(new Slot(this.invCrafting, m + n * 2, 98 + m * 18, 18 + n * 18));
         }
      }

      for(n = 0; n < 4; ++n) {
         final EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_ORDER[n];
         this.addSlot(new Slot(inventory, 39 - n, 8, 8 + n * 18) {
            public int getMaxStackAmount() {
               return 1;
            }

            public boolean canInsert(ItemStack stack) {
               return equipmentSlot == MobEntity.getPreferredEquipmentSlot(stack);
            }

            public boolean canTakeItems(PlayerEntity playerEntity) {
               ItemStack itemStack = this.getStack();
               return !itemStack.isEmpty() && !playerEntity.isCreative() && EnchantmentHelper.hasBindingCurse(itemStack) ? false : super.canTakeItems(playerEntity);
            }

            @Environment(EnvType.CLIENT)
            public Pair<Identifier, Identifier> getBackgroundSprite() {
               return Pair.of(PlayerContainer.BLOCK_ATLAS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_TEXTURES[equipmentSlot.getEntitySlotId()]);
            }
         });
      }

      for(n = 0; n < 3; ++n) {
         for(m = 0; m < 9; ++m) {
            this.addSlot(new Slot(inventory, m + (n + 1) * 9, 8 + m * 18, 84 + n * 18));
         }
      }

      for(n = 0; n < 9; ++n) {
         this.addSlot(new Slot(inventory, n, 8 + n * 18, 142));
      }

      this.addSlot(new Slot(inventory, 40, 77, 62) {
         @Environment(EnvType.CLIENT)
         public Pair<Identifier, Identifier> getBackgroundSprite() {
            return Pair.of(PlayerContainer.BLOCK_ATLAS_TEXTURE, PlayerContainer.EMPTY_OFFHAND_ARMOR_SLOT);
         }
      });
   }

   public void populateRecipeFinder(RecipeFinder recipeFinder) {
      this.invCrafting.provideRecipeInputs(recipeFinder);
   }

   public void clearCraftingSlots() {
      this.invCraftingResult.clear();
      this.invCrafting.clear();
   }

   public boolean matches(Recipe<? super CraftingInventory> recipe) {
      return recipe.matches(this.invCrafting, this.owner.world);
   }

   public void onContentChanged(Inventory inventory) {
      CraftingTableContainer.updateResult(this.syncId, this.owner.world, this.owner, this.invCrafting, this.invCraftingResult);
   }

   public void close(PlayerEntity player) {
      super.close(player);
      this.invCraftingResult.clear();
      if (!player.world.isClient) {
         this.dropInventory(player, player.world, this.invCrafting);
      }
   }

   public boolean canUse(PlayerEntity player) {
      return true;
   }

   public ItemStack transferSlot(PlayerEntity player, int invSlot) {
      ItemStack itemStack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slotList.get(invSlot);
      if (slot != null && slot.hasStack()) {
         ItemStack itemStack2 = slot.getStack();
         itemStack = itemStack2.copy();
         EquipmentSlot equipmentSlot = MobEntity.getPreferredEquipmentSlot(itemStack);
         if (invSlot == 0) {
            if (!this.insertItem(itemStack2, 9, 45, true)) {
               return ItemStack.EMPTY;
            }

            slot.onStackChanged(itemStack2, itemStack);
         } else if (invSlot >= 1 && invSlot < 5) {
            if (!this.insertItem(itemStack2, 9, 45, false)) {
               return ItemStack.EMPTY;
            }
         } else if (invSlot >= 5 && invSlot < 9) {
            if (!this.insertItem(itemStack2, 9, 45, false)) {
               return ItemStack.EMPTY;
            }
         } else if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR && !((Slot)this.slotList.get(8 - equipmentSlot.getEntitySlotId())).hasStack()) {
            int i = 8 - equipmentSlot.getEntitySlotId();
            if (!this.insertItem(itemStack2, i, i + 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (equipmentSlot == EquipmentSlot.OFFHAND && !((Slot)this.slotList.get(45)).hasStack()) {
            if (!this.insertItem(itemStack2, 45, 46, false)) {
               return ItemStack.EMPTY;
            }
         } else if (invSlot >= 9 && invSlot < 36) {
            if (!this.insertItem(itemStack2, 36, 45, false)) {
               return ItemStack.EMPTY;
            }
         } else if (invSlot >= 36 && invSlot < 45) {
            if (!this.insertItem(itemStack2, 9, 36, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.insertItem(itemStack2, 9, 45, false)) {
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

         ItemStack itemStack3 = slot.onTakeItem(player, itemStack2);
         if (invSlot == 0) {
            player.dropItem(itemStack3, false);
         }
      }

      return itemStack;
   }

   public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
      return slot.inventory != this.invCraftingResult && super.canInsertIntoSlot(stack, slot);
   }

   public int getCraftingResultSlotIndex() {
      return 0;
   }

   public int getCraftingWidth() {
      return this.invCrafting.getWidth();
   }

   public int getCraftingHeight() {
      return this.invCrafting.getHeight();
   }

   @Environment(EnvType.CLIENT)
   public int getCraftingSlotCount() {
      return 5;
   }

   static {
      EMPTY_ARMOR_SLOT_TEXTURES = new Identifier[]{EMPTY_BOOTS_SLOT_TEXTURE, EMPTY_LEGGINGS_SLOT_TEXTURE, EMPTY_CHESTPLATE_SLOT_TEXTURE, EMPTY_HELMET_SLOT_TEXTURE};
      EQUIPMENT_SLOT_ORDER = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
   }
}
