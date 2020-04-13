package net.minecraft.block.entity;

import net.minecraft.container.Container;
import net.minecraft.container.SmokerContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class SmokerBlockEntity extends AbstractFurnaceBlockEntity {
   public SmokerBlockEntity() {
      super(BlockEntityType.SMOKER, RecipeType.SMOKING);
   }

   protected Text getContainerName() {
      return new TranslatableText("container.smoker", new Object[0]);
   }

   protected int getFuelTime(ItemStack fuel) {
      return super.getFuelTime(fuel) / 2;
   }

   protected Container createContainer(int i, PlayerInventory playerInventory) {
      return new SmokerContainer(i, playerInventory, this, this.propertyDelegate);
   }
}
