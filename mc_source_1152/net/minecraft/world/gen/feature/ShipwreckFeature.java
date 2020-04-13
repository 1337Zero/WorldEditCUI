package net.minecraft.world.gen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.structure.ShipwreckGenerator;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class ShipwreckFeature extends AbstractTempleFeature<ShipwreckFeatureConfig> {
   public ShipwreckFeature(Function<Dynamic<?>, ? extends ShipwreckFeatureConfig> configFactory) {
      super(configFactory);
   }

   public String getName() {
      return "Shipwreck";
   }

   public int getRadius() {
      return 3;
   }

   public StructureFeature.StructureStartFactory getStructureStartFactory() {
      return ShipwreckFeature.Start::new;
   }

   protected int getSeedModifier() {
      return 165745295;
   }

   protected int getSpacing(ChunkGenerator<?> chunkGenerator) {
      return chunkGenerator.getConfig().getShipwreckSpacing();
   }

   protected int getSeparation(ChunkGenerator<?> chunkGenerator) {
      return chunkGenerator.getConfig().getShipwreckSeparation();
   }

   public static class Start extends StructureStart {
      public Start(StructureFeature<?> structureFeature, int chunkX, int chunkZ, BlockBox blockBox, int i, long l) {
         super(structureFeature, chunkX, chunkZ, blockBox, i, l);
      }

      public void initialize(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int x, int z, Biome biome) {
         ShipwreckFeatureConfig shipwreckFeatureConfig = (ShipwreckFeatureConfig)chunkGenerator.getStructureConfig(biome, Feature.SHIPWRECK);
         BlockRotation blockRotation = BlockRotation.values()[this.random.nextInt(BlockRotation.values().length)];
         BlockPos blockPos = new BlockPos(x * 16, 90, z * 16);
         ShipwreckGenerator.addParts(structureManager, blockPos, blockRotation, this.children, this.random, shipwreckFeatureConfig);
         this.setBoundingBoxFromChildren();
      }
   }
}
