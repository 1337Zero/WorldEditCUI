package net.minecraft.world.biome;

import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityType;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.MineshaftFeature;
import net.minecraft.world.gen.feature.MineshaftFeatureConfig;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;

public final class SnowyMountainsBiome extends Biome {
   public SnowyMountainsBiome() {
      super((new Biome.Settings()).configureSurfaceBuilder(SurfaceBuilder.DEFAULT, SurfaceBuilder.GRASS_CONFIG).precipitation(Biome.Precipitation.SNOW).category(Biome.Category.ICY).depth(0.45F).scale(0.3F).temperature(0.0F).downfall(0.5F).waterColor(4159204).waterFogColor(329011).parent((String)null));
      this.addStructureFeature(Feature.MINESHAFT.configure(new MineshaftFeatureConfig(0.004D, MineshaftFeature.Type.NORMAL)));
      this.addStructureFeature(Feature.STRONGHOLD.configure(FeatureConfig.DEFAULT));
      DefaultBiomeFeatures.addLandCarvers(this);
      DefaultBiomeFeatures.addDefaultStructures(this);
      DefaultBiomeFeatures.addDefaultLakes(this);
      DefaultBiomeFeatures.addDungeons(this);
      DefaultBiomeFeatures.addMineables(this);
      DefaultBiomeFeatures.addDefaultOres(this);
      DefaultBiomeFeatures.addDefaultDisks(this);
      DefaultBiomeFeatures.addSnowySpruceTrees(this);
      DefaultBiomeFeatures.addDefaultFlowers(this);
      DefaultBiomeFeatures.addDefaultGrass(this);
      DefaultBiomeFeatures.addDefaultMushrooms(this);
      DefaultBiomeFeatures.addDefaultVegetation(this);
      DefaultBiomeFeatures.addSprings(this);
      DefaultBiomeFeatures.addFrozenTopLayer(this);
      this.addSpawn(EntityCategory.CREATURE, new Biome.SpawnEntry(EntityType.RABBIT, 10, 2, 3));
      this.addSpawn(EntityCategory.CREATURE, new Biome.SpawnEntry(EntityType.POLAR_BEAR, 1, 1, 2));
      this.addSpawn(EntityCategory.AMBIENT, new Biome.SpawnEntry(EntityType.BAT, 10, 8, 8));
      this.addSpawn(EntityCategory.MONSTER, new Biome.SpawnEntry(EntityType.SPIDER, 100, 4, 4));
      this.addSpawn(EntityCategory.MONSTER, new Biome.SpawnEntry(EntityType.ZOMBIE, 95, 4, 4));
      this.addSpawn(EntityCategory.MONSTER, new Biome.SpawnEntry(EntityType.ZOMBIE_VILLAGER, 5, 1, 1));
      this.addSpawn(EntityCategory.MONSTER, new Biome.SpawnEntry(EntityType.CREEPER, 100, 4, 4));
      this.addSpawn(EntityCategory.MONSTER, new Biome.SpawnEntry(EntityType.SLIME, 100, 4, 4));
      this.addSpawn(EntityCategory.MONSTER, new Biome.SpawnEntry(EntityType.ENDERMAN, 10, 1, 4));
      this.addSpawn(EntityCategory.MONSTER, new Biome.SpawnEntry(EntityType.WITCH, 5, 1, 1));
      this.addSpawn(EntityCategory.MONSTER, new Biome.SpawnEntry(EntityType.SKELETON, 20, 4, 4));
      this.addSpawn(EntityCategory.MONSTER, new Biome.SpawnEntry(EntityType.STRAY, 80, 4, 4));
   }

   public float getMaxSpawnLimit() {
      return 0.07F;
   }
}
