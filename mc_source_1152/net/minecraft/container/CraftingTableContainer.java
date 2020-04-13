package net.minecraft.container;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.packet.GuiSlotUpdateS2CPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class CraftingTableContainer extends CraftingContainer<CraftingInventory> {
   private final CraftingInventory craftingInv;
   private final CraftingResultInventory resultInv;
   private final BlockContext context;
   private final PlayerEntity player;

   public CraftingTableContainer(int syncId, PlayerInventory playerInventory) {
      this(syncId, playerInventory, BlockContext.EMPTY);
   }

   public CraftingTableContainer(int syncId, PlayerInventory playerInventory, BlockContext blockContext) {
      super(ContainerType.CRAFTING, syncId);
      this.craftingInv = new CraftingInventory(this, 3, 3);
      this.resultInv = new CraftingResultInventory();
      this.context = blockContext;
      this.player = playerInventory.player;
      this.addSlot(new CraftingResultSlot(playerInventory.player, this.craftingInv, this.resultInv, 0, 124, 35));

      int m;
      int l;
      for(m = 0; m < 3; ++m) {
         for(l = 0; l < 3; ++l) {
            this.addSlot(new Slot(this.craftingInv, l + m * 3, 30 + l * 18, 17 + m * 18));
         }
      }

      for(m = 0; m < 3; ++m) {
         for(l = 0; l < 9; ++l) {
            this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
         }
      }

      for(m = 0; m < 9; ++m) {
         this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
      }

   }

   protected static void updateResult(int syncId, World world, PlayerEntity player, CraftingInventory craftingInventory, CraftingResultInventory resultInventory) {
      if (!world.isClient) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)player;
         ItemStack itemStack = ItemStack.EMPTY;
         Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, craftingInventory, world);
         if (optional.isPresent()) {
            CraftingRecipe craftingRecipe = (CraftingRecipe)optional.get();
            if (resultInventory.shouldCraftRecipe(world, serverPlayerEntity, craftingRecipe)) {
               itemStack = craftingRecipe.craft(craftingInventory);
            }
         }

         resultInventory.setInvStack(0, itemStack);
         serverPlayerEntity.networkHandler.sendPacket(new GuiSlotUpdateS2CPacket(syncId, 0, itemStack));
      }
   }

   public void onContentChanged(Inventory inventory) {
      this.context.run((world, blockPos) -> {
         updateResult(this.syncId, world, this.player, this.craftingInv, this.resultInv);
      });
   }

   public void populateRecipeFinder(RecipeFinder recipeFinder) {
      this.craftingInv.provideRecipeInputs(recipeFinder);
   }

   public void clearCraftingSlots() {
      this.craftingInv.clear();
      this.resultInv.clear();
   }

   public boolean matches(Recipe<? super CraftingInventory> recipe) {
      return recipe.matches(this.craftingInv, this.player.world);
   }

   public void close(PlayerEntity player) {
      super.close(player);
      this.context.run((world, blockPos) -> {
         this.dropInventory(player, world, this.craftingInv);
      });
   }

   public boolean canUse(PlayerEntity player) {
      return canUse(this.context, player, Blocks.CRAFTING_TABLE);
   }

   public ItemStack transferSlot(PlayerEntity player, int invSlot) {
      ItemStack itemStack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slotList.get(invSlot);
      if (slot != null && slot.hasStack()) {
         ItemStack itemStack2 = slot.getStack();
         itemStack = itemStack2.copy();
         if (invSlot == 0) {
            this.context.run((world, blockPos) -> {
               itemStack2.getItem().onCraft(itemStack2, world, player);
            });
            if (!this.insertItem(itemStack2, 10, 46, true)) {
               return ItemStack.EMPTY;
            }

            slot.onStackChanged(itemStack2, itemStack);
         } else if (invSlot >= 10 && invSlot < 46) {
            if (!this.insertItem(itemStack2, 1, 10, false)) {
               if (invSlot < 37) {
                  if (!this.insertItem(itemStack2, 37, 46, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if (!this.insertItem(itemStack2, 10, 37, false)) {
                  return ItemStack.EMPTY;
               }
            }
         } else if (!this.insertItem(itemStack2, 10, 46, false)) {
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
      return slot.inventory != this.resultInv && super.canInsertIntoSlot(stack, slot);
   }

   public int getCraftingResultSlotIndex() {
      return 0;
   }

   public int getCraftingWidth() {
      return this.craftingInv.getWidth();
   }

   public int getCraftingHeight() {
      return this.craftingInv.getHeight();
   }

   @Environment(EnvType.CLIENT)
   public int getCraftingSlotCount() {
      return 10;
   }
}
