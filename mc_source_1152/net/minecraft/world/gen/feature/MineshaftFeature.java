package net.minecraft.world.gen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.structure.MineshaftGenerator;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class MineshaftFeature extends StructureFeature<MineshaftFeatureConfig> {
   public MineshaftFeature(Function<Dynamic<?>, ? extends MineshaftFeatureConfig> configFactory) {
      super(configFactory);
   }

   public boolean shouldStartAt(BiomeAccess biomeAccess, ChunkGenerator<?> chunkGenerator, Random random, int chunkZ, int i, Biome biome) {
      ((ChunkRandom)random).setStructureSeed(chunkGenerator.getSeed(), chunkZ, i);
      if (chunkGenerator.hasStructure(biome, this)) {
         MineshaftFeatureConfig mineshaftFeatureConfig = (MineshaftFeatureConfig)chunkGenerator.getStructureConfig(biome, this);
         double d = mineshaftFeatureConfig.probability;
         return random.nextDouble() < d;
      } else {
         return false;
      }
   }

   public StructureFeature.StructureStartFactory getStructureStartFactory() {
      return MineshaftFeature.Start::new;
   }

   public String getName() {
      return "Mineshaft";
   }

   public int getRadius() {
      return 8;
   }

   public static class Start extends StructureStart {
      public Start(StructureFeature<?> structureFeature, int chunkX, int chunkZ, BlockBox blockBox, int i, long l) {
         super(structureFeature, chunkX, chunkZ, blockBox, i, l);
      }

      public void initialize(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int x, int z, Biome biome) {
         MineshaftFeatureConfig mineshaftFeatureConfig = (MineshaftFeatureConfig)chunkGenerator.getStructureConfig(biome, Feature.MINESHAFT);
         MineshaftGenerator.MineshaftRoom mineshaftRoom = new MineshaftGenerator.MineshaftRoom(0, this.random, (x << 4) + 2, (z << 4) + 2, mineshaftFeatureConfig.type);
         this.children.add(mineshaftRoom);
         mineshaftRoom.method_14918(mineshaftRoom, this.children, this.random);
         this.setBoundingBoxFromChildren();
         if (mineshaftFeatureConfig.type == MineshaftFeature.Type.MESA) {
            int i = true;
            int j = chunkGenerator.getSeaLevel() - this.boundingBox.maxY + this.boundingBox.getBlockCountY() / 2 - -5;
            this.boundingBox.offset(0, j, 0);
            Iterator var10 = this.children.iterator();

            while(var10.hasNext()) {
               StructurePiece structurePiece = (StructurePiece)var10.next();
               structurePiece.translate(0, j, 0);
            }
         } else {
            this.method_14978(chunkGenerator.getSeaLevel(), this.random, 10);
         }

      }
   }

   public static enum Type {
      NORMAL("normal"),
      MESA("mesa");

      private static final Map<String, MineshaftFeature.Type> nameMap = (Map)Arrays.stream(values()).collect(Collectors.toMap(MineshaftFeature.Type::getName, (type) -> {
         return type;
      }));
      private final String name;

      private Type(String string2) {
         this.name = string2;
      }

      public String getName() {
         return this.name;
      }

      public static MineshaftFeature.Type byName(String nam) {
         return (MineshaftFeature.Type)nameMap.get(nam);
      }

      public static MineshaftFeature.Type byIndex(int index) {
         return index >= 0 && index < values().length ? values()[index] : NORMAL;
      }
   }
}
