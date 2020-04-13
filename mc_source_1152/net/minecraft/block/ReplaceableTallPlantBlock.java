package net.minecraft.block;

import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.EnumProperty;

public class ReplaceableTallPlantBlock extends TallPlantBlock {
   public static final EnumProperty<DoubleBlockHalf> HALF;

   public ReplaceableTallPlantBlock(Block.Settings settings) {
      super(settings);
   }

   public boolean canReplace(BlockState state, ItemPlacementContext ctx) {
      boolean bl = super.canReplace(state, ctx);
      return bl && ctx.getStack().getItem() == this.asItem() ? false : bl;
   }

   static {
      HALF = TallPlantBlock.HALF;
   }
}
