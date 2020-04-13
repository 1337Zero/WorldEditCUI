package net.minecraft.block;

import net.minecraft.client.block.ColoredBlock;
import net.minecraft.util.DyeColor;

public class StainedGlassPaneBlock extends PaneBlock implements ColoredBlock {
   private final DyeColor color;

   public StainedGlassPaneBlock(DyeColor color, Block.Settings settings) {
      super(settings);
      this.color = color;
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(NORTH, false)).with(EAST, false)).with(SOUTH, false)).with(WEST, false)).with(WATERLOGGED, false));
   }

   public DyeColor getColor() {
      return this.color;
   }
}
