package net.minecraft.world.gen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.structure.JungleTempleGenerator;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class JungleTempleFeature extends AbstractTempleFeature<DefaultFeatureConfig> {
   public JungleTempleFeature(Function<Dynamic<?>, ? extends DefaultFeatureConfig> configFactory) {
      super(configFactory);
   }

   public String getName() {
      return "Jungle_Pyramid";
   }

   public int getRadius() {
      return 3;
   }

   public StructureFeature.StructureStartFactory getStructureStartFactory() {
      return JungleTempleFeature.Start::new;
   }

   protected int getSeedModifier() {
      return 14357619;
   }

   public static class Start extends StructureStart {
      public Start(StructureFeature<?> structureFeature, int chunkX, int chunkZ, BlockBox blockBox, int i, long l) {
         super(structureFeature, chunkX, chunkZ, blockBox, i, l);
      }

      public void initialize(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int x, int z, Biome biome) {
         JungleTempleGenerator jungleTempleGenerator = new JungleTempleGenerator(this.random, x * 16, z * 16);
         this.children.add(jungleTempleGenerator);
         this.setBoundingBoxFromChildren();
      }
   }
}
