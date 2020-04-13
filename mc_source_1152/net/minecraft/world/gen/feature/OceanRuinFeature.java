package net.minecraft.world.gen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.structure.OceanRuinGenerator;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class OceanRuinFeature extends AbstractTempleFeature<OceanRuinFeatureConfig> {
   public OceanRuinFeature(Function<Dynamic<?>, ? extends OceanRuinFeatureConfig> configFactory) {
      super(configFactory);
   }

   public String getName() {
      return "Ocean_Ruin";
   }

   public int getRadius() {
      return 3;
   }

   protected int getSpacing(ChunkGenerator<?> chunkGenerator) {
      return chunkGenerator.getConfig().getOceanRuinSpacing();
   }

   protected int getSeparation(ChunkGenerator<?> chunkGenerator) {
      return chunkGenerator.getConfig().getOceanRuinSeparation();
   }

   public StructureFeature.StructureStartFactory getStructureStartFactory() {
      return OceanRuinFeature.Start::new;
   }

   protected int getSeedModifier() {
      return 14357621;
   }

   public static enum BiomeType {
      WARM("warm"),
      COLD("cold");

      private static final Map<String, OceanRuinFeature.BiomeType> nameMap = (Map)Arrays.stream(values()).collect(Collectors.toMap(OceanRuinFeature.BiomeType::getName, (biomeType) -> {
         return biomeType;
      }));
      private final String name;

      private BiomeType(String string2) {
         this.name = string2;
      }

      public String getName() {
         return this.name;
      }

      public static OceanRuinFeature.BiomeType byName(String name) {
         return (OceanRuinFeature.BiomeType)nameMap.get(name);
      }
   }

   public static class Start extends StructureStart {
      public Start(StructureFeature<?> structureFeature, int chunkX, int chunkZ, BlockBox blockBox, int i, long l) {
         super(structureFeature, chunkX, chunkZ, blockBox, i, l);
      }

      public void initialize(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int x, int z, Biome biome) {
         OceanRuinFeatureConfig oceanRuinFeatureConfig = (OceanRuinFeatureConfig)chunkGenerator.getStructureConfig(biome, Feature.OCEAN_RUIN);
         int i = x * 16;
         int j = z * 16;
         BlockPos blockPos = new BlockPos(i, 90, j);
         BlockRotation blockRotation = BlockRotation.values()[this.random.nextInt(BlockRotation.values().length)];
         OceanRuinGenerator.addPieces(structureManager, blockPos, blockRotation, this.children, this.random, oceanRuinFeatureConfig);
         this.setBoundingBoxFromChildren();
      }
   }
}
