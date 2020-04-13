package net.minecraft.world.biome.layer;

import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.layer.type.MergingLayer;
import net.minecraft.world.biome.layer.util.LayerRandomnessSource;
import net.minecraft.world.biome.layer.util.LayerSampler;
import net.minecraft.world.biome.layer.util.NorthWestCoordinateTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum AddHillsLayer implements MergingLayer, NorthWestCoordinateTransformer {
   INSTANCE;

   private static final Logger LOGGER = LogManager.getLogger();
   private static final int BIRCH_FOREST_ID = Registry.BIOME.getRawId(Biomes.BIRCH_FOREST);
   private static final int BIRCH_FOREST_HILLS_ID = Registry.BIOME.getRawId(Biomes.BIRCH_FOREST_HILLS);
   private static final int DESERT_ID = Registry.BIOME.getRawId(Biomes.DESERT);
   private static final int DESERT_HILLS_ID = Registry.BIOME.getRawId(Biomes.DESERT_HILLS);
   private static final int MOUNTAINS_ID = Registry.BIOME.getRawId(Biomes.MOUNTAINS);
   private static final int WOODED_MOUNTAINS_ID = Registry.BIOME.getRawId(Biomes.WOODED_MOUNTAINS);
   private static final int FOREST_ID = Registry.BIOME.getRawId(Biomes.FOREST);
   private static final int WOODED_HILLS_ID = Registry.BIOME.getRawId(Biomes.WOODED_HILLS);
   private static final int SNOWY_TUNDRA_ID = Registry.BIOME.getRawId(Biomes.SNOWY_TUNDRA);
   private static final int SNOWY_MOUNTAINS_ID = Registry.BIOME.getRawId(Biomes.SNOWY_MOUNTAINS);
   private static final int JUNGLE_ID = Registry.BIOME.getRawId(Biomes.JUNGLE);
   private static final int JUNGLE_HILLS_ID = Registry.BIOME.getRawId(Biomes.JUNGLE_HILLS);
   private static final int BAMBOO_JUNGLE_ID = Registry.BIOME.getRawId(Biomes.BAMBOO_JUNGLE);
   private static final int BAMBOO_JUNGLE_HILLS_ID = Registry.BIOME.getRawId(Biomes.BAMBOO_JUNGLE_HILLS);
   private static final int BADLANDS_ID = Registry.BIOME.getRawId(Biomes.BADLANDS);
   private static final int WOODED_BADLANDS_PLATEAU_ID = Registry.BIOME.getRawId(Biomes.WOODED_BADLANDS_PLATEAU);
   private static final int PLAINS_ID = Registry.BIOME.getRawId(Biomes.PLAINS);
   private static final int GIANT_TREE_TAIGA_ID = Registry.BIOME.getRawId(Biomes.GIANT_TREE_TAIGA);
   private static final int GIANT_TREE_TAIGA_HILLS_ID = Registry.BIOME.getRawId(Biomes.GIANT_TREE_TAIGA_HILLS);
   private static final int DARK_FOREST_ID = Registry.BIOME.getRawId(Biomes.DARK_FOREST);
   private static final int SAVANNA_ID = Registry.BIOME.getRawId(Biomes.SAVANNA);
   private static final int SAVANNA_PLATEAU_ID = Registry.BIOME.getRawId(Biomes.SAVANNA_PLATEAU);
   private static final int TAIGA_ID = Registry.BIOME.getRawId(Biomes.TAIGA);
   private static final int SNOWY_TAIGA_ID = Registry.BIOME.getRawId(Biomes.SNOWY_TAIGA);
   private static final int SNOWY_TAIGA_HILLS_ID = Registry.BIOME.getRawId(Biomes.SNOWY_TAIGA_HILLS);
   private static final int TAIGA_HILLS_ID = Registry.BIOME.getRawId(Biomes.TAIGA_HILLS);

   public int sample(LayerRandomnessSource context, LayerSampler sampler1, LayerSampler sampler2, int x, int z) {
      int i = sampler1.sample(this.transformX(x + 1), this.transformZ(z + 1));
      int j = sampler2.sample(this.transformX(x + 1), this.transformZ(z + 1));
      if (i > 255) {
         LOGGER.debug("old! {}", i);
      }

      int k = (j - 2) % 29;
      Biome biome3;
      if (!BiomeLayers.isShallowOcean(i) && j >= 2 && k == 1) {
         Biome biome = (Biome)Registry.BIOME.get(i);
         if (biome == null || !biome.hasParent()) {
            biome3 = Biome.getModifiedBiome(biome);
            return biome3 == null ? i : Registry.BIOME.getRawId(biome3);
         }
      }

      if (context.nextInt(3) == 0 || k == 0) {
         int l = i;
         if (i == DESERT_ID) {
            l = DESERT_HILLS_ID;
         } else if (i == FOREST_ID) {
            l = WOODED_HILLS_ID;
         } else if (i == BIRCH_FOREST_ID) {
            l = BIRCH_FOREST_HILLS_ID;
         } else if (i == DARK_FOREST_ID) {
            l = PLAINS_ID;
         } else if (i == TAIGA_ID) {
            l = TAIGA_HILLS_ID;
         } else if (i == GIANT_TREE_TAIGA_ID) {
            l = GIANT_TREE_TAIGA_HILLS_ID;
         } else if (i == SNOWY_TAIGA_ID) {
            l = SNOWY_TAIGA_HILLS_ID;
         } else if (i == PLAINS_ID) {
            l = context.nextInt(3) == 0 ? WOODED_HILLS_ID : FOREST_ID;
         } else if (i == SNOWY_TUNDRA_ID) {
            l = SNOWY_MOUNTAINS_ID;
         } else if (i == JUNGLE_ID) {
            l = JUNGLE_HILLS_ID;
         } else if (i == BAMBOO_JUNGLE_ID) {
            l = BAMBOO_JUNGLE_HILLS_ID;
         } else if (i == BiomeLayers.OCEAN_ID) {
            l = BiomeLayers.DEEP_OCEAN_ID;
         } else if (i == BiomeLayers.LUKEWARM_OCEAN_ID) {
            l = BiomeLayers.DEEP_LUKEWARM_OCEAN_ID;
         } else if (i == BiomeLayers.COLD_OCEAN_ID) {
            l = BiomeLayers.DEEP_COLD_OCEAN_ID;
         } else if (i == BiomeLayers.FROZEN_OCEAN_ID) {
            l = BiomeLayers.DEEP_FROZEN_OCEAN_ID;
         } else if (i == MOUNTAINS_ID) {
            l = WOODED_MOUNTAINS_ID;
         } else if (i == SAVANNA_ID) {
            l = SAVANNA_PLATEAU_ID;
         } else if (BiomeLayers.areSimilar(i, WOODED_BADLANDS_PLATEAU_ID)) {
            l = BADLANDS_ID;
         } else if ((i == BiomeLayers.DEEP_OCEAN_ID || i == BiomeLayers.DEEP_LUKEWARM_OCEAN_ID || i == BiomeLayers.DEEP_COLD_OCEAN_ID || i == BiomeLayers.DEEP_FROZEN_OCEAN_ID) && context.nextInt(3) == 0) {
            l = context.nextInt(2) == 0 ? PLAINS_ID : FOREST_ID;
         }

         if (k == 0 && l != i) {
            biome3 = Biome.getModifiedBiome((Biome)Registry.BIOME.get(l));
            l = biome3 == null ? i : Registry.BIOME.getRawId(biome3);
         }

         if (l != i) {
            int m = 0;
            if (BiomeLayers.areSimilar(sampler1.sample(this.transformX(x + 1), this.transformZ(z + 0)), i)) {
               ++m;
            }

            if (BiomeLayers.areSimilar(sampler1.sample(this.transformX(x + 2), this.transformZ(z + 1)), i)) {
               ++m;
            }

            if (BiomeLayers.areSimilar(sampler1.sample(this.transformX(x + 0), this.transformZ(z + 1)), i)) {
               ++m;
            }

            if (BiomeLayers.areSimilar(sampler1.sample(this.transformX(x + 1), this.transformZ(z + 2)), i)) {
               ++m;
            }

            if (m >= 3) {
               return l;
            }
         }
      }

      return i;
   }
}
