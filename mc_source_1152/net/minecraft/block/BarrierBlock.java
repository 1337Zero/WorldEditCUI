package net.minecraft.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class BarrierBlock extends Block {
   protected BarrierBlock(Block.Settings settings) {
      super(settings);
   }

   public boolean isTranslucent(BlockState state, BlockView view, BlockPos pos) {
      return true;
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.INVISIBLE;
   }

   @Environment(EnvType.CLIENT)
   public float getAmbientOcclusionLightLevel(BlockState state, BlockView view, BlockPos pos) {
      return 1.0F;
   }

   public boolean allowsSpawning(BlockState state, BlockView view, BlockPos pos, EntityType<?> type) {
      return false;
   }
}
