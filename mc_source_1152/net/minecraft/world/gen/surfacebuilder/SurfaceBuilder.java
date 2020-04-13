package net.minecraft.world.gen.surfacebuilder;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

public abstract class SurfaceBuilder<C extends SurfaceConfig> {
   public static final BlockState AIR;
   public static final BlockState DIRT;
   public static final BlockState GRASS_BLOCK;
   public static final BlockState PODZOL;
   public static final BlockState GRAVEL;
   public static final BlockState STONE;
   public static final BlockState COARSE_DIRT;
   public static final BlockState SAND;
   public static final BlockState RED_SAND;
   public static final BlockState WHITE_TERRACOTTA;
   public static final BlockState MYCELIUM;
   public static final BlockState NETHERRACK;
   public static final BlockState END_STONE;
   public static final TernarySurfaceConfig AIR_CONFIG;
   public static final TernarySurfaceConfig PODZOL_CONFIG;
   public static final TernarySurfaceConfig GRAVEL_CONFIG;
   public static final TernarySurfaceConfig GRASS_CONFIG;
   public static final TernarySurfaceConfig DIRT_CONFIG;
   public static final TernarySurfaceConfig STONE_CONFIG;
   public static final TernarySurfaceConfig COARSE_DIRT_CONFIG;
   public static final TernarySurfaceConfig SAND_CONFIG;
   public static final TernarySurfaceConfig GRASS_SAND_UNDERWATER_CONFIG;
   public static final TernarySurfaceConfig SAND_SAND_UNDERWATER_CONFIG;
   public static final TernarySurfaceConfig BADLANDS_CONFIG;
   public static final TernarySurfaceConfig MYCELIUM_CONFIG;
   public static final TernarySurfaceConfig NETHER_CONFIG;
   public static final TernarySurfaceConfig END_CONFIG;
   public static final SurfaceBuilder<TernarySurfaceConfig> DEFAULT;
   public static final SurfaceBuilder<TernarySurfaceConfig> MOUNTAIN;
   public static final SurfaceBuilder<TernarySurfaceConfig> SHATTERED_SAVANNA;
   public static final SurfaceBuilder<TernarySurfaceConfig> GRAVELLY_MOUNTAIN;
   public static final SurfaceBuilder<TernarySurfaceConfig> GIANT_TREE_TAIGA;
   public static final SurfaceBuilder<TernarySurfaceConfig> SWAMP;
   public static final SurfaceBuilder<TernarySurfaceConfig> BADLANDS;
   public static final SurfaceBuilder<TernarySurfaceConfig> WOODED_BADLANDS;
   public static final SurfaceBuilder<TernarySurfaceConfig> ERODED_BADLANDS;
   public static final SurfaceBuilder<TernarySurfaceConfig> FROZEN_OCEAN;
   public static final SurfaceBuilder<TernarySurfaceConfig> NETHER;
   public static final SurfaceBuilder<TernarySurfaceConfig> NOPE;
   private final Function<Dynamic<?>, ? extends C> factory;

   private static <C extends SurfaceConfig, F extends SurfaceBuilder<C>> F register(String string, F surfaceBuilder) {
      return (SurfaceBuilder)Registry.register(Registry.SURFACE_BUILDER, (String)string, surfaceBuilder);
   }

   public SurfaceBuilder(Function<Dynamic<?>, ? extends C> function) {
      this.factory = function;
   }

   public abstract void generate(Random random, Chunk chunk, Biome biome, int x, int z, int height, double noise, BlockState defaultBlock, BlockState defaultFluid, int seaLevel, long seed, C surfaceBlocks);

   public void initSeed(long seed) {
   }

   static {
      AIR = Blocks.AIR.getDefaultState();
      DIRT = Blocks.DIRT.getDefaultState();
      GRASS_BLOCK = Blocks.GRASS_BLOCK.getDefaultState();
      PODZOL = Blocks.PODZOL.getDefaultState();
      GRAVEL = Blocks.GRAVEL.getDefaultState();
      STONE = Blocks.STONE.getDefaultState();
      COARSE_DIRT = Blocks.COARSE_DIRT.getDefaultState();
      SAND = Blocks.SAND.getDefaultState();
      RED_SAND = Blocks.RED_SAND.getDefaultState();
      WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.getDefaultState();
      MYCELIUM = Blocks.MYCELIUM.getDefaultState();
      NETHERRACK = Blocks.NETHERRACK.getDefaultState();
      END_STONE = Blocks.END_STONE.getDefaultState();
      AIR_CONFIG = new TernarySurfaceConfig(AIR, AIR, AIR);
      PODZOL_CONFIG = new TernarySurfaceConfig(PODZOL, DIRT, GRAVEL);
      GRAVEL_CONFIG = new TernarySurfaceConfig(GRAVEL, GRAVEL, GRAVEL);
      GRASS_CONFIG = new TernarySurfaceConfig(GRASS_BLOCK, DIRT, GRAVEL);
      DIRT_CONFIG = new TernarySurfaceConfig(DIRT, DIRT, GRAVEL);
      STONE_CONFIG = new TernarySurfaceConfig(STONE, STONE, GRAVEL);
      COARSE_DIRT_CONFIG = new TernarySurfaceConfig(COARSE_DIRT, DIRT, GRAVEL);
      SAND_CONFIG = new TernarySurfaceConfig(SAND, SAND, GRAVEL);
      GRASS_SAND_UNDERWATER_CONFIG = new TernarySurfaceConfig(GRASS_BLOCK, DIRT, SAND);
      SAND_SAND_UNDERWATER_CONFIG = new TernarySurfaceConfig(SAND, SAND, SAND);
      BADLANDS_CONFIG = new TernarySurfaceConfig(RED_SAND, WHITE_TERRACOTTA, GRAVEL);
      MYCELIUM_CONFIG = new TernarySurfaceConfig(MYCELIUM, DIRT, GRAVEL);
      NETHER_CONFIG = new TernarySurfaceConfig(NETHERRACK, NETHERRACK, NETHERRACK);
      END_CONFIG = new TernarySurfaceConfig(END_STONE, END_STONE, END_STONE);
      DEFAULT = register("default", new DefaultSurfaceBuilder(TernarySurfaceConfig::deserialize));
      MOUNTAIN = register("mountain", new MountainSurfaceBuilder(TernarySurfaceConfig::deserialize));
      SHATTERED_SAVANNA = register("shattered_savanna", new ShatteredSavannaSurfaceBuilder(TernarySurfaceConfig::deserialize));
      GRAVELLY_MOUNTAIN = register("gravelly_mountain", new GravellyMountainSurfaceBuilder(TernarySurfaceConfig::deserialize));
      GIANT_TREE_TAIGA = register("giant_tree_taiga", new GiantTreeTaigaSurfaceBuilder(TernarySurfaceConfig::deserialize));
      SWAMP = register("swamp", new SwampSurfaceBuilder(TernarySurfaceConfig::deserialize));
      BADLANDS = register("badlands", new BadlandsSurfaceBuilder(TernarySurfaceConfig::deserialize));
      WOODED_BADLANDS = register("wooded_badlands", new WoodedBadlandsSurfaceBuilder(TernarySurfaceConfig::deserialize));
      ERODED_BADLANDS = register("eroded_badlands", new ErodedBadlandsSurfaceBuilder(TernarySurfaceConfig::deserialize));
      FROZEN_OCEAN = register("frozen_ocean", new FrozenOceanSurfaceBuilder(TernarySurfaceConfig::deserialize));
      NETHER = register("nether", new NetherSurfaceBuilder(TernarySurfaceConfig::deserialize));
      NOPE = register("nope", new NopeSurfaceBuilder(TernarySurfaceConfig::deserialize));
   }
}
