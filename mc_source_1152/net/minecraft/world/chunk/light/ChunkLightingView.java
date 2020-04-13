package net.minecraft.world.chunk.light;

import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.ChunkNibbleArray;

public interface ChunkLightingView extends LightingView {
   @Nullable
   ChunkNibbleArray getLightArray(ChunkSectionPos pos);

   int getLightLevel(BlockPos blockPos);

   public static enum Empty implements ChunkLightingView {
      INSTANCE;

      @Nullable
      public ChunkNibbleArray getLightArray(ChunkSectionPos pos) {
         return null;
      }

      public int getLightLevel(BlockPos blockPos) {
         return 0;
      }

      public void updateSectionStatus(ChunkSectionPos pos, boolean status) {
      }
   }
}
