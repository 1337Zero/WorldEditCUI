package net.minecraft.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public abstract class AbstractGlassBlock extends TransparentBlock {
   protected AbstractGlassBlock(Block.Settings settings) {
      super(settings);
   }

   @Environment(EnvType.CLIENT)
   public float getAmbientOcclusionLightLevel(BlockState state, BlockView view, BlockPos pos) {
      return 1.0F;
   }

   public boolean isTranslucent(BlockState state, BlockView view, BlockPos pos) {
      return true;
   }

   public boolean canSuffocate(BlockState state, BlockView view, BlockPos pos) {
      return false;
   }

   public boolean isSimpleFullBlock(BlockState state, BlockView view, BlockPos pos) {
      return false;
   }

   public boolean allowsSpawning(BlockState state, BlockView view, BlockPos pos, EntityType<?> type) {
      return false;
   }
}
