package net.minecraft.world.chunk.light;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;

public class LightingProvider implements LightingView {
   @Nullable
   private final ChunkLightProvider<?, ?> blockLightProvider;
   @Nullable
   private final ChunkLightProvider<?, ?> skyLightProvider;

   public LightingProvider(ChunkProvider chunkProvider, boolean hasBlockLight, boolean hasSkyLight) {
      this.blockLightProvider = hasBlockLight ? new ChunkBlockLightProvider(chunkProvider) : null;
      this.skyLightProvider = hasSkyLight ? new ChunkSkyLightProvider(chunkProvider) : null;
   }

   public void checkBlock(BlockPos pos) {
      if (this.blockLightProvider != null) {
         this.blockLightProvider.checkBlock(pos);
      }

      if (this.skyLightProvider != null) {
         this.skyLightProvider.checkBlock(pos);
      }

   }

   public void addLightSource(BlockPos pos, int level) {
      if (this.blockLightProvider != null) {
         this.blockLightProvider.addLightSource(pos, level);
      }

   }

   public boolean hasUpdates() {
      if (this.skyLightProvider != null && this.skyLightProvider.hasUpdates()) {
         return true;
      } else {
         return this.blockLightProvider != null && this.blockLightProvider.hasUpdates();
      }
   }

   public int doLightUpdates(int maxUpdateCount, boolean doSkylight, boolean skipEdgeLightPropagation) {
      if (this.blockLightProvider != null && this.skyLightProvider != null) {
         int i = maxUpdateCount / 2;
         int j = this.blockLightProvider.doLightUpdates(i, doSkylight, skipEdgeLightPropagation);
         int k = maxUpdateCount - i + j;
         int l = this.skyLightProvider.doLightUpdates(k, doSkylight, skipEdgeLightPropagation);
         return j == 0 && l > 0 ? this.blockLightProvider.doLightUpdates(l, doSkylight, skipEdgeLightPropagation) : l;
      } else if (this.blockLightProvider != null) {
         return this.blockLightProvider.doLightUpdates(maxUpdateCount, doSkylight, skipEdgeLightPropagation);
      } else {
         return this.skyLightProvider != null ? this.skyLightProvider.doLightUpdates(maxUpdateCount, doSkylight, skipEdgeLightPropagation) : maxUpdateCount;
      }
   }

   public void updateSectionStatus(ChunkSectionPos pos, boolean status) {
      if (this.blockLightProvider != null) {
         this.blockLightProvider.updateSectionStatus(pos, status);
      }

      if (this.skyLightProvider != null) {
         this.skyLightProvider.updateSectionStatus(pos, status);
      }

   }

   public void setLightEnabled(ChunkPos pos, boolean lightEnabled) {
      if (this.blockLightProvider != null) {
         this.blockLightProvider.setLightEnabled(pos, lightEnabled);
      }

      if (this.skyLightProvider != null) {
         this.skyLightProvider.setLightEnabled(pos, lightEnabled);
      }

   }

   public ChunkLightingView get(LightType lightType) {
      if (lightType == LightType.BLOCK) {
         return (ChunkLightingView)(this.blockLightProvider == null ? ChunkLightingView.Empty.INSTANCE : this.blockLightProvider);
      } else {
         return (ChunkLightingView)(this.skyLightProvider == null ? ChunkLightingView.Empty.INSTANCE : this.skyLightProvider);
      }
   }

   @Environment(EnvType.CLIENT)
   public String method_22876(LightType lightType, ChunkSectionPos chunkSectionPos) {
      if (lightType == LightType.BLOCK) {
         if (this.blockLightProvider != null) {
            return this.blockLightProvider.method_22875(chunkSectionPos.asLong());
         }
      } else if (this.skyLightProvider != null) {
         return this.skyLightProvider.method_22875(chunkSectionPos.asLong());
      }

      return "n/a";
   }

   public void queueData(LightType lightType, ChunkSectionPos chunkSectionPos, @Nullable ChunkNibbleArray chunkNibbleArray) {
      if (lightType == LightType.BLOCK) {
         if (this.blockLightProvider != null) {
            this.blockLightProvider.setLightArray(chunkSectionPos.asLong(), chunkNibbleArray);
         }
      } else if (this.skyLightProvider != null) {
         this.skyLightProvider.setLightArray(chunkSectionPos.asLong(), chunkNibbleArray);
      }

   }

   public void setRetainData(ChunkPos pos, boolean retainData) {
      if (this.blockLightProvider != null) {
         this.blockLightProvider.setRetainData(pos, retainData);
      }

      if (this.skyLightProvider != null) {
         this.skyLightProvider.setRetainData(pos, retainData);
      }

   }

   public int getLight(BlockPos pos, int ambientDarkness) {
      int i = this.skyLightProvider == null ? 0 : this.skyLightProvider.getLightLevel(pos) - ambientDarkness;
      int j = this.blockLightProvider == null ? 0 : this.blockLightProvider.getLightLevel(pos);
      return Math.max(j, i);
   }
}
