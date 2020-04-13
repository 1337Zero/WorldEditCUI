package net.minecraft.block;

import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class BedrockBlock extends Block {
   public BedrockBlock(Block.Settings settings) {
      super(settings);
   }

   public boolean allowsSpawning(BlockState state, BlockView view, BlockPos pos, EntityType<?> type) {
      return false;
   }
}
