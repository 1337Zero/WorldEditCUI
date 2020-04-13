package net.minecraft.world.gen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.structure.DesertTempleGenerator;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class DesertPyramidFeature extends AbstractTempleFeature<DefaultFeatureConfig> {
   public DesertPyramidFeature(Function<Dynamic<?>, ? extends DefaultFeatureConfig> configFactory) {
      super(configFactory);
   }

   public String getName() {
      return "Desert_Pyramid";
   }

   public int getRadius() {
      return 3;
   }

   public StructureFeature.StructureStartFactory getStructureStartFactory() {
      return DesertPyramidFeature.Start::new;
   }

   protected int getSeedModifier() {
      return 14357617;
   }

   public static class Start extends StructureStart {
      public Start(StructureFeature<?> structureFeature, int chunkX, int chunkZ, BlockBox blockBox, int i, long l) {
         super(structureFeature, chunkX, chunkZ, blockBox, i, l);
      }

      public void initialize(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int x, int z, Biome biome) {
         DesertTempleGenerator desertTempleGenerator = new DesertTempleGenerator(this.random, x * 16, z * 16);
         this.children.add(desertTempleGenerator);
         this.setBoundingBoxFromChildren();
      }
   }
}
