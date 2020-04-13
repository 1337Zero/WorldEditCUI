package net.minecraft.world.biome.source;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.level.LevelProperties;

public class FixedBiomeSourceConfig implements BiomeSourceConfig {
   private Biome biome;

   public FixedBiomeSourceConfig(LevelProperties levelProperties) {
      this.biome = Biomes.PLAINS;
   }

   public FixedBiomeSourceConfig setBiome(Biome biome) {
      this.biome = biome;
      return this;
   }

   public Biome getBiome() {
      return this.biome;
   }
}
