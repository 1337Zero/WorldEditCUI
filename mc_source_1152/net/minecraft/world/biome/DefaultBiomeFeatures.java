package net.minecraft.world.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MushroomBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.ProbabilityConfig;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.decorator.AlterGroundTreeDecorator;
import net.minecraft.world.gen.decorator.BeehiveTreeDecorator;
import net.minecraft.world.gen.decorator.CarvingMaskDecoratorConfig;
import net.minecraft.world.gen.decorator.ChanceDecoratorConfig;
import net.minecraft.world.gen.decorator.CocoaBeansTreeDecorator;
import net.minecraft.world.gen.decorator.CountChanceDecoratorConfig;
import net.minecraft.world.gen.decorator.CountDecoratorConfig;
import net.minecraft.world.gen.decorator.CountDepthDecoratorConfig;
import net.minecraft.world.gen.decorator.CountExtraChanceDecoratorConfig;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.DecoratorConfig;
import net.minecraft.world.gen.decorator.LeaveVineTreeDecorator;
import net.minecraft.world.gen.decorator.NoiseHeightmapDecoratorConfig;
import net.minecraft.world.gen.decorator.RangeDecoratorConfig;
import net.minecraft.world.gen.decorator.TopSolidHeightmapNoiseBiasedDecoratorConfig;
import net.minecraft.world.gen.decorator.TrunkVineTreeDecorator;
import net.minecraft.world.gen.feature.BlockPileFeatureConfig;
import net.minecraft.world.gen.feature.BoulderFeatureConfig;
import net.minecraft.world.gen.feature.BranchedTreeFeatureConfig;
import net.minecraft.world.gen.feature.BuriedTreasureFeatureConfig;
import net.minecraft.world.gen.feature.DiskFeatureConfig;
import net.minecraft.world.gen.feature.EmeraldOreFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.HugeMushroomFeatureConfig;
import net.minecraft.world.gen.feature.MegaTreeFeatureConfig;
import net.minecraft.world.gen.feature.MineshaftFeature;
import net.minecraft.world.gen.feature.MineshaftFeatureConfig;
import net.minecraft.world.gen.feature.OceanRuinFeature;
import net.minecraft.world.gen.feature.OceanRuinFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.RandomBooleanFeatureConfig;
import net.minecraft.world.gen.feature.RandomFeatureConfig;
import net.minecraft.world.gen.feature.RandomPatchFeatureConfig;
import net.minecraft.world.gen.feature.RandomRandomFeatureConfig;
import net.minecraft.world.gen.feature.SeagrassFeatureConfig;
import net.minecraft.world.gen.feature.ShipwreckFeatureConfig;
import net.minecraft.world.gen.feature.SimpleBlockFeatureConfig;
import net.minecraft.world.gen.feature.SingleStateFeatureConfig;
import net.minecraft.world.gen.feature.SpringFeatureConfig;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.VillageFeatureConfig;
import net.minecraft.world.gen.foliage.AcaciaFoliagePlacer;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.foliage.PineFoliagePlacer;
import net.minecraft.world.gen.foliage.SpruceFoliagePlacer;
import net.minecraft.world.gen.placer.ColumnPlacer;
import net.minecraft.world.gen.placer.DoublePlantPlacer;
import net.minecraft.world.gen.placer.SimpleBlockPlacer;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.ForestFlowerStateProvider;
import net.minecraft.world.gen.stateprovider.PlainsFlowerStateProvider;
import net.minecraft.world.gen.stateprovider.SimpleStateProvider;
import net.minecraft.world.gen.stateprovider.WeightedStateProvider;

public class DefaultBiomeFeatures {
   private static final BlockState GRASS;
   private static final BlockState FERN;
   private static final BlockState PODZOL;
   private static final BlockState OAK_LOG;
   private static final BlockState OAK_LEAVES;
   private static final BlockState JUNGLE_LOG;
   private static final BlockState JUNGLE_LEAVES;
   private static final BlockState SPRUCE_LOG;
   private static final BlockState SPRUCE_LEAVES;
   private static final BlockState ACACIA_LOG;
   private static final BlockState ACACIA_LEAVES;
   private static final BlockState BIRCH_LOG;
   private static final BlockState BIRCH_LEAVES;
   private static final BlockState DARK_OAK_LOG;
   private static final BlockState DARK_OAK_LEAVES;
   private static final BlockState WATER;
   private static final BlockState LAVA;
   private static final BlockState DIRT;
   private static final BlockState GRAVEL;
   private static final BlockState GRANITE;
   private static final BlockState DIORITE;
   private static final BlockState ANDESITE;
   private static final BlockState COAL_ORE;
   private static final BlockState IRON_ORE;
   private static final BlockState GOLD_ORE;
   private static final BlockState REDSTONE_ORE;
   private static final BlockState DIAMOND_ORE;
   private static final BlockState LAPIS_ORE;
   private static final BlockState STONE;
   private static final BlockState EMERALD_ORE;
   private static final BlockState INFESTED_STONE;
   private static final BlockState SAND;
   private static final BlockState CLAY;
   private static final BlockState GRASS_BLOCK;
   private static final BlockState MOSSY_COBBLESTONE;
   private static final BlockState LARGE_FERN;
   private static final BlockState TALL_GRASS;
   private static final BlockState LILAC;
   private static final BlockState ROSE_BUSH;
   private static final BlockState PEONY;
   private static final BlockState BROWN_MUSHROOM;
   private static final BlockState RED_MUSHROOM;
   private static final BlockState SEAGRASS;
   private static final BlockState PACKED_ICE;
   private static final BlockState BLUE_ICE;
   private static final BlockState LILY_OF_THE_VALLEY;
   private static final BlockState BLUE_ORCHID;
   private static final BlockState POPPY;
   private static final BlockState DANDELION;
   private static final BlockState DEAD_BUSH;
   private static final BlockState MELON;
   private static final BlockState PUMPKIN;
   private static final BlockState SWEET_BERRY_BUSH;
   private static final BlockState FIRE;
   private static final BlockState NETHERRACK;
   private static final BlockState LILY_PAD;
   private static final BlockState SNOW;
   private static final BlockState JACK_O_LANTERN;
   private static final BlockState SUNFLOWER;
   private static final BlockState CACTUS;
   private static final BlockState SUGAR_CANE;
   private static final BlockState RED_MUSHROOM_BLOCK;
   private static final BlockState BROWN_MUSHROOM_BLOCK;
   private static final BlockState MUSHROOM_BLOCK;
   public static final BranchedTreeFeatureConfig OAK_TREE_CONFIG;
   public static final BranchedTreeFeatureConfig JUNGLE_TREE_CONFIG;
   public static final BranchedTreeFeatureConfig JUNGLE_SAPLING_TREE_CONFIG;
   public static final BranchedTreeFeatureConfig PINE_TREE_CONFIG;
   public static final BranchedTreeFeatureConfig SPRUCE_TREE_CONFIG;
   public static final BranchedTreeFeatureConfig ACACIA_TREE_CONFIG;
   public static final BranchedTreeFeatureConfig BIRCH_TREE_CONFIG;
   public static final BranchedTreeFeatureConfig field_21833;
   public static final BranchedTreeFeatureConfig LARGE_BIRCH_TREE_CONFIG;
   public static final BranchedTreeFeatureConfig SWAMP_TREE_CONFIG;
   public static final BranchedTreeFeatureConfig FANCY_TREE_CONFIG;
   public static final BranchedTreeFeatureConfig OAK_TREE_WITH_MORE_BEEHIVES_CONFIG;
   public static final BranchedTreeFeatureConfig field_21834;
   public static final BranchedTreeFeatureConfig FANCY_TREE_WITH_MORE_BEEHIVES_CONFIG;
   public static final BranchedTreeFeatureConfig field_21835;
   public static final BranchedTreeFeatureConfig OAK_TREE_WITH_BEEHIVES_CONFIG;
   public static final BranchedTreeFeatureConfig FANCY_TREE_WITH_BEEHIVES_CONFIG;
   public static final BranchedTreeFeatureConfig BIRCH_TREE_WITH_BEEHIVES_CONFIG;
   public static final BranchedTreeFeatureConfig field_21836;
   public static final TreeFeatureConfig JUNGLE_GROUND_BUSH_CONFIG;
   public static final MegaTreeFeatureConfig DARK_OAK_TREE_CONFIG;
   public static final MegaTreeFeatureConfig MEGA_SPRUCE_TREE_CONFIG;
   public static final MegaTreeFeatureConfig MEGA_PINE_TREE_CONFIG;
   public static final MegaTreeFeatureConfig MEGA_JUNGLE_TREE_CONFIG;
   public static final RandomPatchFeatureConfig GRASS_CONFIG;
   public static final RandomPatchFeatureConfig TAIGA_GRASS_CONFIG;
   public static final RandomPatchFeatureConfig LUSH_GRASS_CONFIG;
   public static final RandomPatchFeatureConfig LILY_OF_THE_VALLEY_CONFIG;
   public static final RandomPatchFeatureConfig BLUE_ORCHID_CONFIG;
   public static final RandomPatchFeatureConfig DEFAULT_FLOWER_CONFIG;
   public static final RandomPatchFeatureConfig PLAINS_FLOWER_CONFIG;
   public static final RandomPatchFeatureConfig FOREST_FLOWER_CONFIG;
   public static final RandomPatchFeatureConfig DEAD_BUSH_CONFIG;
   public static final RandomPatchFeatureConfig MELON_PATCH_CONFIG;
   public static final RandomPatchFeatureConfig PUMPKIN_PATCH_CONFIG;
   public static final RandomPatchFeatureConfig SWEET_BERRY_BUSH_CONFIG;
   public static final RandomPatchFeatureConfig NETHER_FIRE_CONFIG;
   public static final RandomPatchFeatureConfig LILY_PAD_CONFIG;
   public static final RandomPatchFeatureConfig RED_MUSHROOM_CONFIG;
   public static final RandomPatchFeatureConfig BROWN_MUSHROOM_CONFIG;
   public static final RandomPatchFeatureConfig LILAC_CONFIG;
   public static final RandomPatchFeatureConfig ROSE_BUSH_CONFIG;
   public static final RandomPatchFeatureConfig PEONY_CONFIG;
   public static final RandomPatchFeatureConfig SUNFLOWER_CONFIG;
   public static final RandomPatchFeatureConfig TALL_GRASS_CONFIG;
   public static final RandomPatchFeatureConfig LARGE_FERN_CONFIG;
   public static final RandomPatchFeatureConfig CACTUS_CONFIG;
   public static final RandomPatchFeatureConfig SUGAR_CANE_CONFIG;
   public static final BlockPileFeatureConfig HAY_PILE_CONFIG;
   public static final BlockPileFeatureConfig SNOW_PILE_CONFIG;
   public static final BlockPileFeatureConfig MELON_PILE_CONFIG;
   public static final BlockPileFeatureConfig PUMPKIN_PILE_CONFIG;
   public static final BlockPileFeatureConfig BLUE_ICE_PILE_CONFIG;
   public static final SpringFeatureConfig WATER_SPRING_CONFIG;
   public static final SpringFeatureConfig LAVA_SPRING_CONFIG;
   public static final SpringFeatureConfig NETHER_SPRING_CONFIG;
   public static final SpringFeatureConfig ENCLOSED_NETHER_SPRING_CONFIG;
   public static final HugeMushroomFeatureConfig HUGE_RED_MUSHROOM_CONFIG;
   public static final HugeMushroomFeatureConfig HUGE_BROWN_MUSHROOM_CONFIG;

   public static void addLandCarvers(Biome biome) {
      biome.addCarver(GenerationStep.Carver.AIR, Biome.configureCarver(Carver.CAVE, new ProbabilityConfig(0.14285715F)));
      biome.addCarver(GenerationStep.Carver.AIR, Biome.configureCarver(Carver.CANYON, new ProbabilityConfig(0.02F)));
   }

   public static void addOceanCarvers(Biome biome) {
      biome.addCarver(GenerationStep.Carver.AIR, Biome.configureCarver(Carver.CAVE, new ProbabilityConfig(0.06666667F)));
      biome.addCarver(GenerationStep.Carver.AIR, Biome.configureCarver(Carver.CANYON, new ProbabilityConfig(0.02F)));
      biome.addCarver(GenerationStep.Carver.LIQUID, Biome.configureCarver(Carver.UNDERWATER_CANYON, new ProbabilityConfig(0.02F)));
      biome.addCarver(GenerationStep.Carver.LIQUID, Biome.configureCarver(Carver.UNDERWATER_CAVE, new ProbabilityConfig(0.06666667F)));
   }

   public static void addDefaultStructures(Biome biome) {
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_STRUCTURES, Feature.MINESHAFT.configure(new MineshaftFeatureConfig(0.004000000189989805D, MineshaftFeature.Type.NORMAL)).createDecoratedFeature(Decorator.NOPE.configure(DecoratorConfig.DEFAULT)));
      biome.addFeature(GenerationStep.Feature.SURFACE_STRUCTURES, Feature.PILLAGER_OUTPOST.configure(FeatureConfig.DEFAULT).createDecoratedFeature(Decorator.NOPE.configure(DecoratorConfig.DEFAULT)));
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_STRUCTURES, Feature.STRONGHOLD.configure(FeatureConfig.DEFAULT).createDecoratedFeature(Decorator.NOPE.configure(DecoratorConfig.DEFAULT)));
      biome.addFeature(GenerationStep.Feature.SURFACE_STRUCTURES, Feature.SWAMP_HUT.configure(FeatureConfig.DEFAULT).createDecoratedFeature(Decorator.NOPE.configure(DecoratorConfig.DEFAULT)));
      biome.addFeature(GenerationStep.Feature.SURFACE_STRUCTURES, Feature.DESERT_PYRAMID.configure(FeatureConfig.DEFAULT).createDecoratedFeature(Decorator.NOPE.configure(DecoratorConfig.DEFAULT)));
      biome.addFeature(GenerationStep.Feature.SURFACE_STRUCTURES, Feature.JUNGLE_TEMPLE.configure(FeatureConfig.DEFAULT).createDecoratedFeature(Decorator.NOPE.configure(DecoratorConfig.DEFAULT)));
      biome.addFeature(GenerationStep.Feature.SURFACE_STRUCTURES, Feature.IGLOO.configure(FeatureConfig.DEFAULT).createDecoratedFeature(Decorator.NOPE.configure(DecoratorConfig.DEFAULT)));
      biome.addFeature(GenerationStep.Feature.SURFACE_STRUCTURES, Feature.SHIPWRECK.configure(new ShipwreckFeatureConfig(false)).createDecoratedFeature(Decorator.NOPE.configure(DecoratorConfig.DEFAULT)));
      biome.addFeature(GenerationStep.Feature.SURFACE_STRUCTURES, Feature.OCEAN_MONUMENT.configure(FeatureConfig.DEFAULT).createDecoratedFeature(Decorator.NOPE.configure(DecoratorConfig.DEFAULT)));
      biome.addFeature(GenerationStep.Feature.SURFACE_STRUCTURES, Feature.WOODLAND_MANSION.configure(FeatureConfig.DEFAULT).createDecoratedFeature(Decorator.NOPE.configure(DecoratorConfig.DEFAULT)));
      biome.addFeature(GenerationStep.Feature.SURFACE_STRUCTURES, Feature.OCEAN_RUIN.configure(new OceanRuinFeatureConfig(OceanRuinFeature.BiomeType.COLD, 0.3F, 0.9F)).createDecoratedFeature(Decorator.NOPE.configure(DecoratorConfig.DEFAULT)));
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_STRUCTURES, Feature.BURIED_TREASURE.configure(new BuriedTreasureFeatureConfig(0.01F)).createDecoratedFeature(Decorator.NOPE.configure(DecoratorConfig.DEFAULT)));
      biome.addFeature(GenerationStep.Feature.SURFACE_STRUCTURES, Feature.VILLAGE.configure(new VillageFeatureConfig("village/plains/town_centers", 6)).createDecoratedFeature(Decorator.NOPE.configure(DecoratorConfig.DEFAULT)));
   }

   public static void addDefaultLakes(Biome biome) {
      biome.addFeature(GenerationStep.Feature.LOCAL_MODIFICATIONS, Feature.LAKE.configure(new SingleStateFeatureConfig(WATER)).createDecoratedFeature(Decorator.WATER_LAKE.configure(new ChanceDecoratorConfig(4))));
      biome.addFeature(GenerationStep.Feature.LOCAL_MODIFICATIONS, Feature.LAKE.configure(new SingleStateFeatureConfig(LAVA)).createDecoratedFeature(Decorator.LAVA_LAKE.configure(new ChanceDecoratorConfig(80))));
   }

   public static void addDesertLakes(Biome biome) {
      biome.addFeature(GenerationStep.Feature.LOCAL_MODIFICATIONS, Feature.LAKE.configure(new SingleStateFeatureConfig(LAVA)).createDecoratedFeature(Decorator.LAVA_LAKE.configure(new ChanceDecoratorConfig(80))));
   }

   public static void addDungeons(Biome biome) {
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_STRUCTURES, Feature.MONSTER_ROOM.configure(FeatureConfig.DEFAULT).createDecoratedFeature(Decorator.DUNGEONS.configure(new ChanceDecoratorConfig(8))));
   }

   public static void addMineables(Biome biome) {
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, Feature.ORE.configure(new OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, DIRT, 33)).createDecoratedFeature(Decorator.COUNT_RANGE.configure(new RangeDecoratorConfig(10, 0, 0, 256))));
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, Feature.ORE.configure(new OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, GRAVEL, 33)).createDecoratedFeature(Decorator.COUNT_RANGE.configure(new RangeDecoratorConfig(8, 0, 0, 256))));
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, Feature.ORE.configure(new OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, GRANITE, 33)).createDecoratedFeature(Decorator.COUNT_RANGE.configure(new RangeDecoratorConfig(10, 0, 0, 80))));
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, Feature.ORE.configure(new OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, DIORITE, 33)).createDecoratedFeature(Decorator.COUNT_RANGE.configure(new RangeDecoratorConfig(10, 0, 0, 80))));
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, Feature.ORE.configure(new OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, ANDESITE, 33)).createDecoratedFeature(Decorator.COUNT_RANGE.configure(new RangeDecoratorConfig(10, 0, 0, 80))));
   }

   public static void addDefaultOres(Biome biome) {
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, Feature.ORE.configure(new OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, COAL_ORE, 17)).createDecoratedFeature(Decorator.COUNT_RANGE.configure(new RangeDecoratorConfig(20, 0, 0, 128))));
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, Feature.ORE.configure(new OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, IRON_ORE, 9)).createDecoratedFeature(Decorator.COUNT_RANGE.configure(new RangeDecoratorConfig(20, 0, 0, 64))));
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, Feature.ORE.configure(new OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, GOLD_ORE, 9)).createDecoratedFeature(Decorator.COUNT_RANGE.configure(new RangeDecoratorConfig(2, 0, 0, 32))));
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, Feature.ORE.configure(new OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, REDSTONE_ORE, 8)).createDecoratedFeature(Decorator.COUNT_RANGE.configure(new RangeDecoratorConfig(8, 0, 0, 16))));
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, Feature.ORE.configure(new OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, DIAMOND_ORE, 8)).createDecoratedFeature(Decorator.COUNT_RANGE.configure(new RangeDecoratorConfig(1, 0, 0, 16))));
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, Feature.ORE.configure(new OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, LAPIS_ORE, 7)).createDecoratedFeature(Decorator.COUNT_DEPTH_AVERAGE.configure(new CountDepthDecoratorConfig(1, 16, 16))));
   }

   public static void addExtraGoldOre(Biome biome) {
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, Feature.ORE.configure(new OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, GOLD_ORE, 9)).createDecoratedFeature(Decorator.COUNT_RANGE.configure(new RangeDecoratorConfig(20, 32, 32, 80))));
   }

   public static void addEmeraldOre(Biome biome) {
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, Feature.EMERALD_ORE.configure(new EmeraldOreFeatureConfig(STONE, EMERALD_ORE)).createDecoratedFeature(Decorator.EMERALD_ORE.configure(DecoratorConfig.DEFAULT)));
   }

   public static void addInfestedStone(Biome biome) {
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_DECORATION, Feature.ORE.configure(new OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, INFESTED_STONE, 9)).createDecoratedFeature(Decorator.COUNT_RANGE.configure(new RangeDecoratorConfig(7, 0, 0, 64))));
   }

   public static void addDefaultDisks(Biome biome) {
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, Feature.DISK.configure(new DiskFeatureConfig(SAND, 7, 2, Lists.newArrayList(new BlockState[]{DIRT, GRASS_BLOCK}))).createDecoratedFeature(Decorator.COUNT_TOP_SOLID.configure(new CountDecoratorConfig(3))));
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, Feature.DISK.configure(new DiskFeatureConfig(CLAY, 4, 1, Lists.newArrayList(new BlockState[]{DIRT, CLAY}))).createDecoratedFeature(Decorator.COUNT_TOP_SOLID.configure(new CountDecoratorConfig(1))));
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, Feature.DISK.configure(new DiskFeatureConfig(GRAVEL, 6, 2, Lists.newArrayList(new BlockState[]{DIRT, GRASS_BLOCK}))).createDecoratedFeature(Decorator.COUNT_TOP_SOLID.configure(new CountDecoratorConfig(1))));
   }

   public static void addClay(Biome biome) {
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, Feature.DISK.configure(new DiskFeatureConfig(CLAY, 4, 1, Lists.newArrayList(new BlockState[]{DIRT, CLAY}))).createDecoratedFeature(Decorator.COUNT_TOP_SOLID.configure(new CountDecoratorConfig(1))));
   }

   public static void addMossyRocks(Biome biome) {
      biome.addFeature(GenerationStep.Feature.LOCAL_MODIFICATIONS, Feature.FOREST_ROCK.configure(new BoulderFeatureConfig(MOSSY_COBBLESTONE, 0)).createDecoratedFeature(Decorator.FOREST_ROCK.configure(new CountDecoratorConfig(3))));
   }

   public static void addLargeFerns(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(LARGE_FERN_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_32.configure(new CountDecoratorConfig(7))));
   }

   public static void addSweetBerryBushesSnowy(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(SWEET_BERRY_BUSH_CONFIG).createDecoratedFeature(Decorator.CHANCE_HEIGHTMAP_DOUBLE.configure(new ChanceDecoratorConfig(12))));
   }

   public static void addSweetBerryBushes(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(SWEET_BERRY_BUSH_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(1))));
   }

   public static void addBamboo(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.BAMBOO.configure(new ProbabilityConfig(0.0F)).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(16))));
   }

   public static void addBambooJungleTrees(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.BAMBOO.configure(new ProbabilityConfig(0.2F)).createDecoratedFeature(Decorator.TOP_SOLID_HEIGHTMAP_NOISE_BIASED.configure(new TopSolidHeightmapNoiseBiasedDecoratorConfig(160, 80.0D, 0.3D, Heightmap.Type.WORLD_SURFACE_WG))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_SELECTOR.configure(new RandomFeatureConfig(ImmutableList.of(Feature.FANCY_TREE.configure(FANCY_TREE_CONFIG).withChance(0.05F), Feature.JUNGLE_GROUND_BUSH.configure(JUNGLE_GROUND_BUSH_CONFIG).withChance(0.15F), Feature.MEGA_JUNGLE_TREE.configure(MEGA_JUNGLE_TREE_CONFIG).withChance(0.7F)), Feature.RANDOM_PATCH.configure(LUSH_GRASS_CONFIG))).createDecoratedFeature(Decorator.COUNT_EXTRA_HEIGHTMAP.configure(new CountExtraChanceDecoratorConfig(30, 0.1F, 1))));
   }

   public static void addTaigaTrees(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_SELECTOR.configure(new RandomFeatureConfig(ImmutableList.of(Feature.NORMAL_TREE.configure(PINE_TREE_CONFIG).withChance(0.33333334F)), Feature.NORMAL_TREE.configure(SPRUCE_TREE_CONFIG))).createDecoratedFeature(Decorator.COUNT_EXTRA_HEIGHTMAP.configure(new CountExtraChanceDecoratorConfig(10, 0.1F, 1))));
   }

   public static void addWaterBiomeOakTrees(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_SELECTOR.configure(new RandomFeatureConfig(ImmutableList.of(Feature.FANCY_TREE.configure(FANCY_TREE_CONFIG).withChance(0.1F)), Feature.NORMAL_TREE.configure(OAK_TREE_CONFIG))).createDecoratedFeature(Decorator.COUNT_EXTRA_HEIGHTMAP.configure(new CountExtraChanceDecoratorConfig(0, 0.1F, 1))));
   }

   public static void addBirchTrees(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.NORMAL_TREE.configure(field_21833).createDecoratedFeature(Decorator.COUNT_EXTRA_HEIGHTMAP.configure(new CountExtraChanceDecoratorConfig(10, 0.1F, 1))));
   }

   public static void addForestTrees(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_SELECTOR.configure(new RandomFeatureConfig(ImmutableList.of(Feature.NORMAL_TREE.configure(field_21833).withChance(0.2F), Feature.FANCY_TREE.configure(field_21834).withChance(0.1F)), Feature.NORMAL_TREE.configure(field_21835))).createDecoratedFeature(Decorator.COUNT_EXTRA_HEIGHTMAP.configure(new CountExtraChanceDecoratorConfig(10, 0.1F, 1))));
   }

   public static void addTallBirchTrees(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_SELECTOR.configure(new RandomFeatureConfig(ImmutableList.of(Feature.NORMAL_TREE.configure(LARGE_BIRCH_TREE_CONFIG).withChance(0.5F)), Feature.NORMAL_TREE.configure(field_21833))).createDecoratedFeature(Decorator.COUNT_EXTRA_HEIGHTMAP.configure(new CountExtraChanceDecoratorConfig(10, 0.1F, 1))));
   }

   public static void addSavannaTrees(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_SELECTOR.configure(new RandomFeatureConfig(ImmutableList.of(Feature.ACACIA_TREE.configure(ACACIA_TREE_CONFIG).withChance(0.8F)), Feature.NORMAL_TREE.configure(OAK_TREE_CONFIG))).createDecoratedFeature(Decorator.COUNT_EXTRA_HEIGHTMAP.configure(new CountExtraChanceDecoratorConfig(1, 0.1F, 1))));
   }

   public static void addExtraSavannaTrees(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_SELECTOR.configure(new RandomFeatureConfig(ImmutableList.of(Feature.ACACIA_TREE.configure(ACACIA_TREE_CONFIG).withChance(0.8F)), Feature.NORMAL_TREE.configure(OAK_TREE_CONFIG))).createDecoratedFeature(Decorator.COUNT_EXTRA_HEIGHTMAP.configure(new CountExtraChanceDecoratorConfig(2, 0.1F, 1))));
   }

   public static void addMountainTrees(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_SELECTOR.configure(new RandomFeatureConfig(ImmutableList.of(Feature.NORMAL_TREE.configure(SPRUCE_TREE_CONFIG).withChance(0.666F), Feature.FANCY_TREE.configure(FANCY_TREE_CONFIG).withChance(0.1F)), Feature.NORMAL_TREE.configure(OAK_TREE_CONFIG))).createDecoratedFeature(Decorator.COUNT_EXTRA_HEIGHTMAP.configure(new CountExtraChanceDecoratorConfig(0, 0.1F, 1))));
   }

   public static void addExtraMountainTrees(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_SELECTOR.configure(new RandomFeatureConfig(ImmutableList.of(Feature.NORMAL_TREE.configure(SPRUCE_TREE_CONFIG).withChance(0.666F), Feature.FANCY_TREE.configure(FANCY_TREE_CONFIG).withChance(0.1F)), Feature.NORMAL_TREE.configure(OAK_TREE_CONFIG))).createDecoratedFeature(Decorator.COUNT_EXTRA_HEIGHTMAP.configure(new CountExtraChanceDecoratorConfig(3, 0.1F, 1))));
   }

   public static void addJungleTrees(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_SELECTOR.configure(new RandomFeatureConfig(ImmutableList.of(Feature.FANCY_TREE.configure(FANCY_TREE_CONFIG).withChance(0.1F), Feature.JUNGLE_GROUND_BUSH.configure(JUNGLE_GROUND_BUSH_CONFIG).withChance(0.5F), Feature.MEGA_JUNGLE_TREE.configure(MEGA_JUNGLE_TREE_CONFIG).withChance(0.33333334F)), Feature.NORMAL_TREE.configure(JUNGLE_TREE_CONFIG))).createDecoratedFeature(Decorator.COUNT_EXTRA_HEIGHTMAP.configure(new CountExtraChanceDecoratorConfig(50, 0.1F, 1))));
   }

   public static void addJungleEdgeTrees(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_SELECTOR.configure(new RandomFeatureConfig(ImmutableList.of(Feature.FANCY_TREE.configure(FANCY_TREE_CONFIG).withChance(0.1F), Feature.JUNGLE_GROUND_BUSH.configure(JUNGLE_GROUND_BUSH_CONFIG).withChance(0.5F)), Feature.NORMAL_TREE.configure(JUNGLE_TREE_CONFIG))).createDecoratedFeature(Decorator.COUNT_EXTRA_HEIGHTMAP.configure(new CountExtraChanceDecoratorConfig(2, 0.1F, 1))));
   }

   public static void addBadlandsPlateauTrees(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.NORMAL_TREE.configure(OAK_TREE_CONFIG).createDecoratedFeature(Decorator.COUNT_EXTRA_HEIGHTMAP.configure(new CountExtraChanceDecoratorConfig(5, 0.1F, 1))));
   }

   public static void addSnowySpruceTrees(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.NORMAL_TREE.configure(SPRUCE_TREE_CONFIG).createDecoratedFeature(Decorator.COUNT_EXTRA_HEIGHTMAP.configure(new CountExtraChanceDecoratorConfig(0, 0.1F, 1))));
   }

   public static void addGiantSpruceTaigaTrees(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_SELECTOR.configure(new RandomFeatureConfig(ImmutableList.of(Feature.MEGA_SPRUCE_TREE.configure(MEGA_SPRUCE_TREE_CONFIG).withChance(0.33333334F), Feature.NORMAL_TREE.configure(PINE_TREE_CONFIG).withChance(0.33333334F)), Feature.NORMAL_TREE.configure(SPRUCE_TREE_CONFIG))).createDecoratedFeature(Decorator.COUNT_EXTRA_HEIGHTMAP.configure(new CountExtraChanceDecoratorConfig(10, 0.1F, 1))));
   }

   public static void addGiantTreeTaigaTrees(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_SELECTOR.configure(new RandomFeatureConfig(ImmutableList.of(Feature.MEGA_SPRUCE_TREE.configure(MEGA_SPRUCE_TREE_CONFIG).withChance(0.025641026F), Feature.MEGA_SPRUCE_TREE.configure(MEGA_PINE_TREE_CONFIG).withChance(0.30769232F), Feature.NORMAL_TREE.configure(PINE_TREE_CONFIG).withChance(0.33333334F)), Feature.NORMAL_TREE.configure(SPRUCE_TREE_CONFIG))).createDecoratedFeature(Decorator.COUNT_EXTRA_HEIGHTMAP.configure(new CountExtraChanceDecoratorConfig(10, 0.1F, 1))));
   }

   public static void addJungleGrass(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(LUSH_GRASS_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(25))));
   }

   public static void addSavannaTallGrass(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(TALL_GRASS_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_32.configure(new CountDecoratorConfig(7))));
   }

   public static void addShatteredSavannaGrass(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(GRASS_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(5))));
   }

   public static void addSavannaGrass(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(GRASS_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(20))));
   }

   public static void addBadlandsGrass(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(GRASS_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(1))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(DEAD_BUSH_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(20))));
   }

   public static void addForestFlowers(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_RANDOM_SELECTOR.configure(new RandomRandomFeatureConfig(ImmutableList.of(Feature.RANDOM_PATCH.configure(LILAC_CONFIG), Feature.RANDOM_PATCH.configure(ROSE_BUSH_CONFIG), Feature.RANDOM_PATCH.configure(PEONY_CONFIG), Feature.FLOWER.configure(LILY_OF_THE_VALLEY_CONFIG)), 0)).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_32.configure(new CountDecoratorConfig(5))));
   }

   public static void addForestGrass(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(GRASS_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(2))));
   }

   public static void addSwampFeatures(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.NORMAL_TREE.configure(SWAMP_TREE_CONFIG).createDecoratedFeature(Decorator.COUNT_EXTRA_HEIGHTMAP.configure(new CountExtraChanceDecoratorConfig(2, 0.1F, 1))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.FLOWER.configure(BLUE_ORCHID_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_32.configure(new CountDecoratorConfig(1))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(GRASS_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(5))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(DEAD_BUSH_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(1))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(LILY_PAD_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(4))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(BROWN_MUSHROOM_CONFIG).createDecoratedFeature(Decorator.COUNT_CHANCE_HEIGHTMAP.configure(new CountChanceDecoratorConfig(8, 0.25F))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(RED_MUSHROOM_CONFIG).createDecoratedFeature(Decorator.COUNT_CHANCE_HEIGHTMAP_DOUBLE.configure(new CountChanceDecoratorConfig(8, 0.125F))));
   }

   public static void addMushroomFieldsFeatures(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_BOOLEAN_SELECTOR.configure(new RandomBooleanFeatureConfig(Feature.HUGE_RED_MUSHROOM.configure(HUGE_RED_MUSHROOM_CONFIG), Feature.HUGE_BROWN_MUSHROOM.configure(HUGE_BROWN_MUSHROOM_CONFIG))).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP.configure(new CountDecoratorConfig(1))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(BROWN_MUSHROOM_CONFIG).createDecoratedFeature(Decorator.COUNT_CHANCE_HEIGHTMAP.configure(new CountChanceDecoratorConfig(1, 0.25F))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(RED_MUSHROOM_CONFIG).createDecoratedFeature(Decorator.COUNT_CHANCE_HEIGHTMAP_DOUBLE.configure(new CountChanceDecoratorConfig(1, 0.125F))));
   }

   public static void addPlainsFeatures(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_SELECTOR.configure(new RandomFeatureConfig(ImmutableList.of(Feature.FANCY_TREE.configure(FANCY_TREE_WITH_MORE_BEEHIVES_CONFIG).withChance(0.33333334F)), Feature.NORMAL_TREE.configure(OAK_TREE_WITH_MORE_BEEHIVES_CONFIG))).createDecoratedFeature(Decorator.COUNT_EXTRA_HEIGHTMAP.configure(new CountExtraChanceDecoratorConfig(0, 0.05F, 1))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.FLOWER.configure(PLAINS_FLOWER_CONFIG).createDecoratedFeature(Decorator.NOISE_HEIGHTMAP_32.configure(new NoiseHeightmapDecoratorConfig(-0.8D, 15, 4))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(GRASS_CONFIG).createDecoratedFeature(Decorator.NOISE_HEIGHTMAP_DOUBLE.configure(new NoiseHeightmapDecoratorConfig(-0.8D, 5, 10))));
   }

   public static void addDesertDeadBushes(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(DEAD_BUSH_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(2))));
   }

   public static void addGiantTaigaGrass(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(TAIGA_GRASS_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(7))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(DEAD_BUSH_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(1))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(BROWN_MUSHROOM_CONFIG).createDecoratedFeature(Decorator.COUNT_CHANCE_HEIGHTMAP.configure(new CountChanceDecoratorConfig(3, 0.25F))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(RED_MUSHROOM_CONFIG).createDecoratedFeature(Decorator.COUNT_CHANCE_HEIGHTMAP_DOUBLE.configure(new CountChanceDecoratorConfig(3, 0.125F))));
   }

   public static void addDefaultFlowers(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.FLOWER.configure(DEFAULT_FLOWER_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_32.configure(new CountDecoratorConfig(2))));
   }

   public static void addExtraDefaultFlowers(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.FLOWER.configure(DEFAULT_FLOWER_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_32.configure(new CountDecoratorConfig(4))));
   }

   public static void addDefaultGrass(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(GRASS_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(1))));
   }

   public static void addTaigaGrass(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(TAIGA_GRASS_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(1))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(BROWN_MUSHROOM_CONFIG).createDecoratedFeature(Decorator.COUNT_CHANCE_HEIGHTMAP.configure(new CountChanceDecoratorConfig(1, 0.25F))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(RED_MUSHROOM_CONFIG).createDecoratedFeature(Decorator.COUNT_CHANCE_HEIGHTMAP_DOUBLE.configure(new CountChanceDecoratorConfig(1, 0.125F))));
   }

   public static void addPlainsTallGrass(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(TALL_GRASS_CONFIG).createDecoratedFeature(Decorator.NOISE_HEIGHTMAP_32.configure(new NoiseHeightmapDecoratorConfig(-0.8D, 0, 7))));
   }

   public static void addDefaultMushrooms(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(BROWN_MUSHROOM_CONFIG).createDecoratedFeature(Decorator.CHANCE_HEIGHTMAP_DOUBLE.configure(new ChanceDecoratorConfig(4))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(RED_MUSHROOM_CONFIG).createDecoratedFeature(Decorator.CHANCE_HEIGHTMAP_DOUBLE.configure(new ChanceDecoratorConfig(8))));
   }

   public static void addDefaultVegetation(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(SUGAR_CANE_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(10))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(PUMPKIN_PATCH_CONFIG).createDecoratedFeature(Decorator.CHANCE_HEIGHTMAP_DOUBLE.configure(new ChanceDecoratorConfig(32))));
   }

   public static void addBadlandsVegetation(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(SUGAR_CANE_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(13))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(PUMPKIN_PATCH_CONFIG).createDecoratedFeature(Decorator.CHANCE_HEIGHTMAP_DOUBLE.configure(new ChanceDecoratorConfig(32))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(CACTUS_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(5))));
   }

   public static void addJungleVegetation(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(MELON_PATCH_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(1))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.VINES.configure(FeatureConfig.DEFAULT).createDecoratedFeature(Decorator.COUNT_HEIGHT_64.configure(new CountDecoratorConfig(50))));
   }

   public static void addDesertVegetation(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(SUGAR_CANE_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(60))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(PUMPKIN_PATCH_CONFIG).createDecoratedFeature(Decorator.CHANCE_HEIGHTMAP_DOUBLE.configure(new ChanceDecoratorConfig(32))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(CACTUS_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(10))));
   }

   public static void addSwampVegetation(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(SUGAR_CANE_CONFIG).createDecoratedFeature(Decorator.COUNT_HEIGHTMAP_DOUBLE.configure(new CountDecoratorConfig(20))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.RANDOM_PATCH.configure(PUMPKIN_PATCH_CONFIG).createDecoratedFeature(Decorator.CHANCE_HEIGHTMAP_DOUBLE.configure(new ChanceDecoratorConfig(32))));
   }

   public static void addDesertFeatures(Biome biome) {
      biome.addFeature(GenerationStep.Feature.SURFACE_STRUCTURES, Feature.DESERT_WELL.configure(FeatureConfig.DEFAULT).createDecoratedFeature(Decorator.CHANCE_HEIGHTMAP.configure(new ChanceDecoratorConfig(1000))));
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_DECORATION, Feature.FOSSIL.configure(FeatureConfig.DEFAULT).createDecoratedFeature(Decorator.CHANCE_PASSTHROUGH.configure(new ChanceDecoratorConfig(64))));
   }

   public static void addFossils(Biome biome) {
      biome.addFeature(GenerationStep.Feature.UNDERGROUND_DECORATION, Feature.FOSSIL.configure(FeatureConfig.DEFAULT).createDecoratedFeature(Decorator.CHANCE_PASSTHROUGH.configure(new ChanceDecoratorConfig(64))));
   }

   public static void addKelp(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.KELP.configure(FeatureConfig.DEFAULT).createDecoratedFeature(Decorator.TOP_SOLID_HEIGHTMAP_NOISE_BIASED.configure(new TopSolidHeightmapNoiseBiasedDecoratorConfig(120, 80.0D, 0.0D, Heightmap.Type.OCEAN_FLOOR_WG))));
   }

   public static void addSeagrassOnStone(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.SIMPLE_BLOCK.configure(new SimpleBlockFeatureConfig(SEAGRASS, new BlockState[]{STONE}, new BlockState[]{WATER}, new BlockState[]{WATER})).createDecoratedFeature(Decorator.CARVING_MASK.configure(new CarvingMaskDecoratorConfig(GenerationStep.Carver.LIQUID, 0.1F))));
   }

   public static void addSeagrass(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.SEAGRASS.configure(new SeagrassFeatureConfig(80, 0.3D)).createDecoratedFeature(Decorator.TOP_SOLID_HEIGHTMAP.configure(DecoratorConfig.DEFAULT)));
   }

   public static void addMoreSeagrass(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.SEAGRASS.configure(new SeagrassFeatureConfig(80, 0.8D)).createDecoratedFeature(Decorator.TOP_SOLID_HEIGHTMAP.configure(DecoratorConfig.DEFAULT)));
   }

   public static void addLessKelp(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.KELP.configure(FeatureConfig.DEFAULT).createDecoratedFeature(Decorator.TOP_SOLID_HEIGHTMAP_NOISE_BIASED.configure(new TopSolidHeightmapNoiseBiasedDecoratorConfig(80, 80.0D, 0.0D, Heightmap.Type.OCEAN_FLOOR_WG))));
   }

   public static void addSprings(Biome biome) {
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.SPRING_FEATURE.configure(WATER_SPRING_CONFIG).createDecoratedFeature(Decorator.COUNT_BIASED_RANGE.configure(new RangeDecoratorConfig(50, 8, 8, 256))));
      biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, Feature.SPRING_FEATURE.configure(LAVA_SPRING_CONFIG).createDecoratedFeature(Decorator.COUNT_VERY_BIASED_RANGE.configure(new RangeDecoratorConfig(20, 8, 16, 256))));
   }

   public static void addIcebergs(Biome biome) {
      biome.addFeature(GenerationStep.Feature.LOCAL_MODIFICATIONS, Feature.ICEBERG.configure(new SingleStateFeatureConfig(PACKED_ICE)).createDecoratedFeature(Decorator.ICEBERG.configure(new ChanceDecoratorConfig(16))));
      biome.addFeature(GenerationStep.Feature.LOCAL_MODIFICATIONS, Feature.ICEBERG.configure(new SingleStateFeatureConfig(BLUE_ICE)).createDecoratedFeature(Decorator.ICEBERG.configure(new ChanceDecoratorConfig(200))));
   }

   public static void addBlueIce(Biome biome) {
      biome.addFeature(GenerationStep.Feature.SURFACE_STRUCTURES, Feature.BLUE_ICE.configure(FeatureConfig.DEFAULT).createDecoratedFeature(Decorator.RANDOM_COUNT_RANGE.configure(new RangeDecoratorConfig(20, 30, 32, 64))));
   }

   public static void addFrozenTopLayer(Biome biome) {
      biome.addFeature(GenerationStep.Feature.TOP_LAYER_MODIFICATION, Feature.FREEZE_TOP_LAYER.configure(FeatureConfig.DEFAULT).createDecoratedFeature(Decorator.NOPE.configure(DecoratorConfig.DEFAULT)));
   }

   public static void addEndCities(Biome biome) {
      biome.addFeature(GenerationStep.Feature.SURFACE_STRUCTURES, Feature.END_CITY.configure(FeatureConfig.DEFAULT).createDecoratedFeature(Decorator.NOPE.configure(DecoratorConfig.DEFAULT)));
   }

   static {
      GRASS = Blocks.GRASS.getDefaultState();
      FERN = Blocks.FERN.getDefaultState();
      PODZOL = Blocks.PODZOL.getDefaultState();
      OAK_LOG = Blocks.OAK_LOG.getDefaultState();
      OAK_LEAVES = Blocks.OAK_LEAVES.getDefaultState();
      JUNGLE_LOG = Blocks.JUNGLE_LOG.getDefaultState();
      JUNGLE_LEAVES = Blocks.JUNGLE_LEAVES.getDefaultState();
      SPRUCE_LOG = Blocks.SPRUCE_LOG.getDefaultState();
      SPRUCE_LEAVES = Blocks.SPRUCE_LEAVES.getDefaultState();
      ACACIA_LOG = Blocks.ACACIA_LOG.getDefaultState();
      ACACIA_LEAVES = Blocks.ACACIA_LEAVES.getDefaultState();
      BIRCH_LOG = Blocks.BIRCH_LOG.getDefaultState();
      BIRCH_LEAVES = Blocks.BIRCH_LEAVES.getDefaultState();
      DARK_OAK_LOG = Blocks.DARK_OAK_LOG.getDefaultState();
      DARK_OAK_LEAVES = Blocks.DARK_OAK_LEAVES.getDefaultState();
      WATER = Blocks.WATER.getDefaultState();
      LAVA = Blocks.LAVA.getDefaultState();
      DIRT = Blocks.DIRT.getDefaultState();
      GRAVEL = Blocks.GRAVEL.getDefaultState();
      GRANITE = Blocks.GRANITE.getDefaultState();
      DIORITE = Blocks.DIORITE.getDefaultState();
      ANDESITE = Blocks.ANDESITE.getDefaultState();
      COAL_ORE = Blocks.COAL_ORE.getDefaultState();
      IRON_ORE = Blocks.IRON_ORE.getDefaultState();
      GOLD_ORE = Blocks.GOLD_ORE.getDefaultState();
      REDSTONE_ORE = Blocks.REDSTONE_ORE.getDefaultState();
      DIAMOND_ORE = Blocks.DIAMOND_ORE.getDefaultState();
      LAPIS_ORE = Blocks.LAPIS_ORE.getDefaultState();
      STONE = Blocks.STONE.getDefaultState();
      EMERALD_ORE = Blocks.EMERALD_ORE.getDefaultState();
      INFESTED_STONE = Blocks.INFESTED_STONE.getDefaultState();
      SAND = Blocks.SAND.getDefaultState();
      CLAY = Blocks.CLAY.getDefaultState();
      GRASS_BLOCK = Blocks.GRASS_BLOCK.getDefaultState();
      MOSSY_COBBLESTONE = Blocks.MOSSY_COBBLESTONE.getDefaultState();
      LARGE_FERN = Blocks.LARGE_FERN.getDefaultState();
      TALL_GRASS = Blocks.TALL_GRASS.getDefaultState();
      LILAC = Blocks.LILAC.getDefaultState();
      ROSE_BUSH = Blocks.ROSE_BUSH.getDefaultState();
      PEONY = Blocks.PEONY.getDefaultState();
      BROWN_MUSHROOM = Blocks.BROWN_MUSHROOM.getDefaultState();
      RED_MUSHROOM = Blocks.RED_MUSHROOM.getDefaultState();
      SEAGRASS = Blocks.SEAGRASS.getDefaultState();
      PACKED_ICE = Blocks.PACKED_ICE.getDefaultState();
      BLUE_ICE = Blocks.BLUE_ICE.getDefaultState();
      LILY_OF_THE_VALLEY = Blocks.LILY_OF_THE_VALLEY.getDefaultState();
      BLUE_ORCHID = Blocks.BLUE_ORCHID.getDefaultState();
      POPPY = Blocks.POPPY.getDefaultState();
      DANDELION = Blocks.DANDELION.getDefaultState();
      DEAD_BUSH = Blocks.DEAD_BUSH.getDefaultState();
      MELON = Blocks.MELON.getDefaultState();
      PUMPKIN = Blocks.PUMPKIN.getDefaultState();
      SWEET_BERRY_BUSH = (BlockState)Blocks.SWEET_BERRY_BUSH.getDefaultState().with(SweetBerryBushBlock.AGE, 3);
      FIRE = Blocks.FIRE.getDefaultState();
      NETHERRACK = Blocks.NETHERRACK.getDefaultState();
      LILY_PAD = Blocks.LILY_PAD.getDefaultState();
      SNOW = Blocks.SNOW.getDefaultState();
      JACK_O_LANTERN = Blocks.JACK_O_LANTERN.getDefaultState();
      SUNFLOWER = Blocks.SUNFLOWER.getDefaultState();
      CACTUS = Blocks.CACTUS.getDefaultState();
      SUGAR_CANE = Blocks.SUGAR_CANE.getDefaultState();
      RED_MUSHROOM_BLOCK = (BlockState)Blocks.RED_MUSHROOM_BLOCK.getDefaultState().with(MushroomBlock.DOWN, false);
      BROWN_MUSHROOM_BLOCK = (BlockState)((BlockState)Blocks.BROWN_MUSHROOM_BLOCK.getDefaultState().with(MushroomBlock.UP, true)).with(MushroomBlock.DOWN, false);
      MUSHROOM_BLOCK = (BlockState)((BlockState)Blocks.MUSHROOM_STEM.getDefaultState().with(MushroomBlock.UP, false)).with(MushroomBlock.DOWN, false);
      OAK_TREE_CONFIG = (new BranchedTreeFeatureConfig.Builder(new SimpleStateProvider(OAK_LOG), new SimpleStateProvider(OAK_LEAVES), new BlobFoliagePlacer(2, 0))).baseHeight(4).heightRandA(2).foliageHeight(3).noVines().build();
      JUNGLE_TREE_CONFIG = (new BranchedTreeFeatureConfig.Builder(new SimpleStateProvider(JUNGLE_LOG), new SimpleStateProvider(JUNGLE_LEAVES), new BlobFoliagePlacer(2, 0))).baseHeight(4).heightRandA(8).foliageHeight(3).treeDecorators(ImmutableList.of(new CocoaBeansTreeDecorator(0.2F), new TrunkVineTreeDecorator(), new LeaveVineTreeDecorator())).noVines().build();
      JUNGLE_SAPLING_TREE_CONFIG = (new BranchedTreeFeatureConfig.Builder(new SimpleStateProvider(JUNGLE_LOG), new SimpleStateProvider(JUNGLE_LEAVES), new BlobFoliagePlacer(2, 0))).baseHeight(4).heightRandA(8).foliageHeight(3).noVines().build();
      PINE_TREE_CONFIG = (new BranchedTreeFeatureConfig.Builder(new SimpleStateProvider(SPRUCE_LOG), new SimpleStateProvider(SPRUCE_LEAVES), new PineFoliagePlacer(1, 0))).baseHeight(7).heightRandA(4).trunkTopOffset(1).foliageHeight(3).foliageHeightRandom(1).noVines().build();
      SPRUCE_TREE_CONFIG = (new BranchedTreeFeatureConfig.Builder(new SimpleStateProvider(SPRUCE_LOG), new SimpleStateProvider(SPRUCE_LEAVES), new SpruceFoliagePlacer(2, 1))).baseHeight(6).heightRandA(3).trunkHeight(1).trunkHeightRandom(1).trunkTopOffsetRandom(2).noVines().build();
      ACACIA_TREE_CONFIG = (new BranchedTreeFeatureConfig.Builder(new SimpleStateProvider(ACACIA_LOG), new SimpleStateProvider(ACACIA_LEAVES), new AcaciaFoliagePlacer(2, 0))).baseHeight(5).heightRandA(2).heightRandB(2).trunkHeight(0).noVines().build();
      BIRCH_TREE_CONFIG = (new BranchedTreeFeatureConfig.Builder(new SimpleStateProvider(BIRCH_LOG), new SimpleStateProvider(BIRCH_LEAVES), new BlobFoliagePlacer(2, 0))).baseHeight(5).heightRandA(2).foliageHeight(3).noVines().build();
      field_21833 = (new BranchedTreeFeatureConfig.Builder(new SimpleStateProvider(BIRCH_LOG), new SimpleStateProvider(BIRCH_LEAVES), new BlobFoliagePlacer(2, 0))).baseHeight(5).heightRandA(2).foliageHeight(3).noVines().treeDecorators(ImmutableList.of(new BeehiveTreeDecorator(0.002F))).build();
      LARGE_BIRCH_TREE_CONFIG = (new BranchedTreeFeatureConfig.Builder(new SimpleStateProvider(BIRCH_LOG), new SimpleStateProvider(BIRCH_LEAVES), new BlobFoliagePlacer(2, 0))).baseHeight(5).heightRandA(2).heightRandB(6).foliageHeight(3).noVines().treeDecorators(ImmutableList.of(new BeehiveTreeDecorator(0.002F))).build();
      SWAMP_TREE_CONFIG = (new BranchedTreeFeatureConfig.Builder(new SimpleStateProvider(OAK_LOG), new SimpleStateProvider(OAK_LEAVES), new BlobFoliagePlacer(3, 0))).baseHeight(5).heightRandA(3).foliageHeight(3).maxWaterDepth(1).treeDecorators(ImmutableList.of(new LeaveVineTreeDecorator())).build();
      FANCY_TREE_CONFIG = (new BranchedTreeFeatureConfig.Builder(new SimpleStateProvider(OAK_LOG), new SimpleStateProvider(OAK_LEAVES), new BlobFoliagePlacer(0, 0))).build();
      OAK_TREE_WITH_MORE_BEEHIVES_CONFIG = (new BranchedTreeFeatureConfig.Builder(new SimpleStateProvider(OAK_LOG), new SimpleStateProvider(OAK_LEAVES), new BlobFoliagePlacer(2, 0))).baseHeight(4).heightRandA(2).foliageHeight(3).noVines().treeDecorators(ImmutableList.of(new BeehiveTreeDecorator(0.05F))).build();
      field_21834 = (new BranchedTreeFeatureConfig.Builder(new SimpleStateProvider(OAK_LOG), new SimpleStateProvider(OAK_LEAVES), new BlobFoliagePlacer(0, 0))).treeDecorators(ImmutableList.of(new BeehiveTreeDecorator(0.002F))).build();
      FANCY_TREE_WITH_MORE_BEEHIVES_CONFIG = (new BranchedTreeFeatureConfig.Builder(new SimpleStateProvider(OAK_LOG), new SimpleStateProvider(OAK_LEAVES), new BlobFoliagePlacer(0, 0))).treeDecorators(ImmutableList.of(new BeehiveTreeDecorator(0.05F))).build();
      field_21835 = (new BranchedTreeFeatureConfig.Builder(new SimpleStateProvider(OAK_LOG), new SimpleStateProvider(OAK_LEAVES), new BlobFoliagePlacer(2, 0))).baseHeight(4).heightRandA(2).foliageHeight(3).noVines().treeDecorators(ImmutableList.of(new BeehiveTreeDecorator(0.002F))).build();
      OAK_TREE_WITH_BEEHIVES_CONFIG = (new BranchedTreeFeatureConfig.Builder(new SimpleStateProvider(OAK_LOG), new SimpleStateProvider(OAK_LEAVES), new BlobFoliagePlacer(2, 0))).baseHeight(4).heightRandA(2).foliageHeight(3).noVines().treeDecorators(ImmutableList.of(new BeehiveTreeDecorator(0.02F))).build();
      FANCY_TREE_WITH_BEEHIVES_CONFIG = (new BranchedTreeFeatureConfig.Builder(new SimpleStateProvider(OAK_LOG), new SimpleStateProvider(OAK_LEAVES), new BlobFoliagePlacer(0, 0))).treeDecorators(ImmutableList.of(new BeehiveTreeDecorator(0.02F))).build();
      BIRCH_TREE_WITH_BEEHIVES_CONFIG = (new BranchedTreeFeatureConfig.Builder(new SimpleStateProvider(BIRCH_LOG), new SimpleStateProvider(BIRCH_LEAVES), new BlobFoliagePlacer(2, 0))).baseHeight(5).heightRandA(2).foliageHeight(3).noVines().treeDecorators(ImmutableList.of(new BeehiveTreeDecorator(0.02F))).build();
      field_21836 = (new BranchedTreeFeatureConfig.Builder(new SimpleStateProvider(BIRCH_LOG), new SimpleStateProvider(BIRCH_LEAVES), new BlobFoliagePlacer(2, 0))).baseHeight(5).heightRandA(2).foliageHeight(3).noVines().treeDecorators(ImmutableList.of(new BeehiveTreeDecorator(0.05F))).build();
      JUNGLE_GROUND_BUSH_CONFIG = (new TreeFeatureConfig.Builder(new SimpleStateProvider(JUNGLE_LOG), new SimpleStateProvider(OAK_LEAVES))).baseHeight(4).build();
      DARK_OAK_TREE_CONFIG = (new MegaTreeFeatureConfig.Builder(new SimpleStateProvider(DARK_OAK_LOG), new SimpleStateProvider(DARK_OAK_LEAVES))).baseHeight(6).build();
      MEGA_SPRUCE_TREE_CONFIG = (new MegaTreeFeatureConfig.Builder(new SimpleStateProvider(SPRUCE_LOG), new SimpleStateProvider(SPRUCE_LEAVES))).baseHeight(13).heightInterval(15).crownHeight(13).treeDecorators(ImmutableList.of(new AlterGroundTreeDecorator(new SimpleStateProvider(PODZOL)))).build();
      MEGA_PINE_TREE_CONFIG = (new MegaTreeFeatureConfig.Builder(new SimpleStateProvider(SPRUCE_LOG), new SimpleStateProvider(SPRUCE_LEAVES))).baseHeight(13).heightInterval(15).crownHeight(3).treeDecorators(ImmutableList.of(new AlterGroundTreeDecorator(new SimpleStateProvider(PODZOL)))).build();
      MEGA_JUNGLE_TREE_CONFIG = (new MegaTreeFeatureConfig.Builder(new SimpleStateProvider(JUNGLE_LOG), new SimpleStateProvider(JUNGLE_LEAVES))).baseHeight(10).heightInterval(20).treeDecorators(ImmutableList.of(new TrunkVineTreeDecorator(), new LeaveVineTreeDecorator())).build();
      GRASS_CONFIG = (new RandomPatchFeatureConfig.Builder(new SimpleStateProvider(GRASS), new SimpleBlockPlacer())).tries(32).build();
      TAIGA_GRASS_CONFIG = (new RandomPatchFeatureConfig.Builder((new WeightedStateProvider()).addState(GRASS, 1).addState(FERN, 4), new SimpleBlockPlacer())).tries(32).build();
      LUSH_GRASS_CONFIG = (new RandomPatchFeatureConfig.Builder((new WeightedStateProvider()).addState(GRASS, 3).addState(FERN, 1), new SimpleBlockPlacer())).blacklist(ImmutableSet.of(PODZOL)).tries(32).build();
      LILY_OF_THE_VALLEY_CONFIG = (new RandomPatchFeatureConfig.Builder(new SimpleStateProvider(LILY_OF_THE_VALLEY), new SimpleBlockPlacer())).tries(64).build();
      BLUE_ORCHID_CONFIG = (new RandomPatchFeatureConfig.Builder(new SimpleStateProvider(BLUE_ORCHID), new SimpleBlockPlacer())).tries(64).build();
      DEFAULT_FLOWER_CONFIG = (new RandomPatchFeatureConfig.Builder((new WeightedStateProvider()).addState(POPPY, 2).addState(DANDELION, 1), new SimpleBlockPlacer())).tries(64).build();
      PLAINS_FLOWER_CONFIG = (new RandomPatchFeatureConfig.Builder(new PlainsFlowerStateProvider(), new SimpleBlockPlacer())).tries(64).build();
      FOREST_FLOWER_CONFIG = (new RandomPatchFeatureConfig.Builder(new ForestFlowerStateProvider(), new SimpleBlockPlacer())).tries(64).build();
      DEAD_BUSH_CONFIG = (new RandomPatchFeatureConfig.Builder(new SimpleStateProvider(DEAD_BUSH), new SimpleBlockPlacer())).tries(4).build();
      MELON_PATCH_CONFIG = (new RandomPatchFeatureConfig.Builder(new SimpleStateProvider(MELON), new SimpleBlockPlacer())).tries(64).whitelist(ImmutableSet.of(GRASS_BLOCK.getBlock())).canReplace().cannotProject().build();
      PUMPKIN_PATCH_CONFIG = (new RandomPatchFeatureConfig.Builder(new SimpleStateProvider(PUMPKIN), new SimpleBlockPlacer())).tries(64).whitelist(ImmutableSet.of(GRASS_BLOCK.getBlock())).cannotProject().build();
      SWEET_BERRY_BUSH_CONFIG = (new RandomPatchFeatureConfig.Builder(new SimpleStateProvider(SWEET_BERRY_BUSH), new SimpleBlockPlacer())).tries(64).whitelist(ImmutableSet.of(GRASS_BLOCK.getBlock())).cannotProject().build();
      NETHER_FIRE_CONFIG = (new RandomPatchFeatureConfig.Builder(new SimpleStateProvider(FIRE), new SimpleBlockPlacer())).tries(64).whitelist(ImmutableSet.of(NETHERRACK.getBlock())).cannotProject().build();
      LILY_PAD_CONFIG = (new RandomPatchFeatureConfig.Builder(new SimpleStateProvider(LILY_PAD), new SimpleBlockPlacer())).tries(10).build();
      RED_MUSHROOM_CONFIG = (new RandomPatchFeatureConfig.Builder(new SimpleStateProvider(RED_MUSHROOM), new SimpleBlockPlacer())).tries(64).cannotProject().build();
      BROWN_MUSHROOM_CONFIG = (new RandomPatchFeatureConfig.Builder(new SimpleStateProvider(BROWN_MUSHROOM), new SimpleBlockPlacer())).tries(64).cannotProject().build();
      LILAC_CONFIG = (new RandomPatchFeatureConfig.Builder(new SimpleStateProvider(LILAC), new DoublePlantPlacer())).tries(64).cannotProject().build();
      ROSE_BUSH_CONFIG = (new RandomPatchFeatureConfig.Builder(new SimpleStateProvider(ROSE_BUSH), new DoublePlantPlacer())).tries(64).cannotProject().build();
      PEONY_CONFIG = (new RandomPatchFeatureConfig.Builder(new SimpleStateProvider(PEONY), new DoublePlantPlacer())).tries(64).cannotProject().build();
      SUNFLOWER_CONFIG = (new RandomPatchFeatureConfig.Builder(new SimpleStateProvider(SUNFLOWER), new DoublePlantPlacer())).tries(64).cannotProject().build();
      TALL_GRASS_CONFIG = (new RandomPatchFeatureConfig.Builder(new SimpleStateProvider(TALL_GRASS), new DoublePlantPlacer())).tries(64).cannotProject().build();
      LARGE_FERN_CONFIG = (new RandomPatchFeatureConfig.Builder(new SimpleStateProvider(LARGE_FERN), new DoublePlantPlacer())).tries(64).cannotProject().build();
      CACTUS_CONFIG = (new RandomPatchFeatureConfig.Builder(new SimpleStateProvider(CACTUS), new ColumnPlacer(1, 2))).tries(10).cannotProject().build();
      SUGAR_CANE_CONFIG = (new RandomPatchFeatureConfig.Builder(new SimpleStateProvider(SUGAR_CANE), new ColumnPlacer(2, 2))).tries(20).spreadX(4).spreadY(0).spreadZ(4).cannotProject().needsWater().build();
      HAY_PILE_CONFIG = new BlockPileFeatureConfig(new BlockStateProvider(Blocks.HAY_BLOCK));
      SNOW_PILE_CONFIG = new BlockPileFeatureConfig(new SimpleStateProvider(SNOW));
      MELON_PILE_CONFIG = new BlockPileFeatureConfig(new SimpleStateProvider(MELON));
      PUMPKIN_PILE_CONFIG = new BlockPileFeatureConfig((new WeightedStateProvider()).addState(PUMPKIN, 19).addState(JACK_O_LANTERN, 1));
      BLUE_ICE_PILE_CONFIG = new BlockPileFeatureConfig((new WeightedStateProvider()).addState(BLUE_ICE, 1).addState(PACKED_ICE, 5));
      WATER_SPRING_CONFIG = new SpringFeatureConfig(Fluids.WATER.getDefaultState(), true, 4, 1, ImmutableSet.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE));
      LAVA_SPRING_CONFIG = new SpringFeatureConfig(Fluids.LAVA.getDefaultState(), true, 4, 1, ImmutableSet.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE));
      NETHER_SPRING_CONFIG = new SpringFeatureConfig(Fluids.LAVA.getDefaultState(), false, 4, 1, ImmutableSet.of(Blocks.NETHERRACK));
      ENCLOSED_NETHER_SPRING_CONFIG = new SpringFeatureConfig(Fluids.LAVA.getDefaultState(), false, 5, 0, ImmutableSet.of(Blocks.NETHERRACK));
      HUGE_RED_MUSHROOM_CONFIG = new HugeMushroomFeatureConfig(new SimpleStateProvider(RED_MUSHROOM_BLOCK), new SimpleStateProvider(MUSHROOM_BLOCK), 2);
      HUGE_BROWN_MUSHROOM_CONFIG = new HugeMushroomFeatureConfig(new SimpleStateProvider(BROWN_MUSHROOM_BLOCK), new SimpleStateProvider(MUSHROOM_BLOCK), 3);
   }
}
