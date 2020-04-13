package net.minecraft.world.gen.chunk;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.CatSpawner;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.PhantomSpawner;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.DecoratorConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DecoratedFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.FillLayerFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.surfacebuilder.ConfiguredSurfaceBuilder;

public class FlatChunkGenerator extends ChunkGenerator<FlatChunkGeneratorConfig> {
   private final Biome biome = this.getBiome();
   private final PhantomSpawner phantomSpawner = new PhantomSpawner();
   private final CatSpawner catSpawner = new CatSpawner();

   public FlatChunkGenerator(IWorld world, BiomeSource biomeSource, FlatChunkGeneratorConfig config) {
      super(world, biomeSource, config);
   }

   private Biome getBiome() {
      Biome biome = ((FlatChunkGeneratorConfig)this.config).getBiome();
      FlatChunkGenerator.FlatChunkGeneratorBiome flatChunkGeneratorBiome = new FlatChunkGenerator.FlatChunkGeneratorBiome(biome.getSurfaceBuilder(), biome.getPrecipitation(), biome.getCategory(), biome.getDepth(), biome.getScale(), biome.getTemperature(), biome.getRainfall(), biome.getWaterColor(), biome.getWaterFogColor(), biome.getParent());
      Map<String, Map<String, String>> map = ((FlatChunkGeneratorConfig)this.config).getStructures();
      Iterator var4 = map.keySet().iterator();

      while(true) {
         ConfiguredFeature[] configuredFeatures;
         int var8;
         ConfiguredFeature configuredFeature3;
         do {
            if (!var4.hasNext()) {
               boolean bl = (!((FlatChunkGeneratorConfig)this.config).hasNoTerrain() || biome == Biomes.THE_VOID) && map.containsKey("decoration");
               if (bl) {
                  List<GenerationStep.Feature> list = Lists.newArrayList();
                  list.add(GenerationStep.Feature.UNDERGROUND_STRUCTURES);
                  list.add(GenerationStep.Feature.SURFACE_STRUCTURES);
                  GenerationStep.Feature[] var18 = GenerationStep.Feature.values();
                  int var20 = var18.length;

                  for(var8 = 0; var8 < var20; ++var8) {
                     GenerationStep.Feature feature = var18[var8];
                     if (!list.contains(feature)) {
                        Iterator var23 = biome.getFeaturesForStep(feature).iterator();

                        while(var23.hasNext()) {
                           configuredFeature3 = (ConfiguredFeature)var23.next();
                           flatChunkGeneratorBiome.addFeature(feature, configuredFeature3);
                        }
                     }
                  }
               }

               BlockState[] blockStates = ((FlatChunkGeneratorConfig)this.config).getLayerBlocks();

               for(int i = 0; i < blockStates.length; ++i) {
                  BlockState blockState = blockStates[i];
                  if (blockState != null && !Heightmap.Type.MOTION_BLOCKING.getBlockPredicate().test(blockState)) {
                     ((FlatChunkGeneratorConfig)this.config).removeLayerBlock(i);
                     flatChunkGeneratorBiome.addFeature(GenerationStep.Feature.TOP_LAYER_MODIFICATION, Feature.FILL_LAYER.configure(new FillLayerFeatureConfig(i, blockState)).createDecoratedFeature(Decorator.NOPE.configure(DecoratorConfig.DEFAULT)));
                  }
               }

               return flatChunkGeneratorBiome;
            }

            String string = (String)var4.next();
            configuredFeatures = (ConfiguredFeature[])FlatChunkGeneratorConfig.STRUCTURE_TO_FEATURES.get(string);
         } while(configuredFeatures == null);

         ConfiguredFeature[] var7 = configuredFeatures;
         var8 = configuredFeatures.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            ConfiguredFeature<?, ?> configuredFeature = var7[var9];
            flatChunkGeneratorBiome.addFeature((GenerationStep.Feature)FlatChunkGeneratorConfig.FEATURE_TO_GENERATION_STEP.get(configuredFeature), configuredFeature);
            configuredFeature3 = ((DecoratedFeatureConfig)configuredFeature.config).feature;
            if (configuredFeature3.feature instanceof StructureFeature) {
               StructureFeature<FeatureConfig> structureFeature = (StructureFeature)configuredFeature3.feature;
               FeatureConfig featureConfig = biome.getStructureFeatureConfig(structureFeature);
               FeatureConfig featureConfig2 = featureConfig != null ? featureConfig : (FeatureConfig)FlatChunkGeneratorConfig.FEATURE_TO_FEATURE_CONFIG.get(configuredFeature);
               flatChunkGeneratorBiome.addStructureFeature(structureFeature.configure(featureConfig2));
            }
         }
      }
   }

   public void buildSurface(ChunkRegion chunkRegion, Chunk chunk) {
   }

   public int getSpawnHeight() {
      Chunk chunk = this.world.getChunk(0, 0);
      return chunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, 8, 8);
   }

   protected Biome getDecorationBiome(BiomeAccess biomeAccess, BlockPos pos) {
      return this.biome;
   }

   public void populateNoise(IWorld world, Chunk chunk) {
      BlockState[] blockStates = ((FlatChunkGeneratorConfig)this.config).getLayerBlocks();
      BlockPos.Mutable mutable = new BlockPos.Mutable();
      Heightmap heightmap = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
      Heightmap heightmap2 = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);

      for(int i = 0; i < blockStates.length; ++i) {
         BlockState blockState = blockStates[i];
         if (blockState != null) {
            for(int j = 0; j < 16; ++j) {
               for(int k = 0; k < 16; ++k) {
                  chunk.setBlockState(mutable.set(j, i, k), blockState, false);
                  heightmap.trackUpdate(j, i, k, blockState);
                  heightmap2.trackUpdate(j, i, k, blockState);
               }
            }
         }
      }

   }

   public int getHeightOnGround(int x, int z, Heightmap.Type heightmapType) {
      BlockState[] blockStates = ((FlatChunkGeneratorConfig)this.config).getLayerBlocks();

      for(int i = blockStates.length - 1; i >= 0; --i) {
         BlockState blockState = blockStates[i];
         if (blockState != null && heightmapType.getBlockPredicate().test(blockState)) {
            return i + 1;
         }
      }

      return 0;
   }

   public void spawnEntities(ServerWorld serverWorld, boolean spawnMonsters, boolean spawnAnimals) {
      this.phantomSpawner.spawn(serverWorld, spawnMonsters, spawnAnimals);
      this.catSpawner.spawn(serverWorld, spawnMonsters, spawnAnimals);
   }

   public boolean hasStructure(Biome biome, StructureFeature<? extends FeatureConfig> structureFeature) {
      return this.biome.hasStructureFeature(structureFeature);
   }

   @Nullable
   public <C extends FeatureConfig> C getStructureConfig(Biome biome, StructureFeature<C> structureFeature) {
      return this.biome.getStructureFeatureConfig(structureFeature);
   }

   @Nullable
   public BlockPos locateStructure(World world, String id, BlockPos center, int radius, boolean skipExistingChunks) {
      return !((FlatChunkGeneratorConfig)this.config).getStructures().keySet().contains(id.toLowerCase(Locale.ROOT)) ? null : super.locateStructure(world, id, center, radius, skipExistingChunks);
   }

   class FlatChunkGeneratorBiome extends Biome {
      protected FlatChunkGeneratorBiome(ConfiguredSurfaceBuilder<?> configuredSurfaceBuilder, Biome.Precipitation precipitation, Biome.Category category, float f, float g, float h, float i, int j, int k, @Nullable String string) {
         super((new Biome.Settings()).surfaceBuilder(configuredSurfaceBuilder).precipitation(precipitation).category(category).depth(f).scale(g).temperature(h).downfall(i).waterColor(j).waterFogColor(k).parent(string));
      }
   }
}
