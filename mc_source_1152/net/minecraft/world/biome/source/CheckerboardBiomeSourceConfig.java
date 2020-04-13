package net.minecraft.world.biome.source;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.level.LevelProperties;

public class CheckerboardBiomeSourceConfig implements BiomeSourceConfig {
   private Biome[] biomes;
   private int size;

   public CheckerboardBiomeSourceConfig(LevelProperties levelProperties) {
      this.biomes = new Biome[]{Biomes.PLAINS};
      this.size = 1;
   }

   public CheckerboardBiomeSourceConfig setBiomes(Biome[] biomes) {
      this.biomes = biomes;
      return this;
   }

   public CheckerboardBiomeSourceConfig setSize(int size) {
      this.size = size;
      return this;
   }

   public Biome[] getBiomes() {
      return this.biomes;
   }

   public int getSize() {
      return this.size;
   }
}
