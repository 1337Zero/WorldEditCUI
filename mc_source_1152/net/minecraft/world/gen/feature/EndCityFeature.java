package net.minecraft.world.gen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.structure.EndCityGenerator;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class EndCityFeature extends StructureFeature<DefaultFeatureConfig> {
   public EndCityFeature(Function<Dynamic<?>, ? extends DefaultFeatureConfig> configFactory) {
      super(configFactory);
   }

   protected ChunkPos getStart(ChunkGenerator<?> chunkGenerator, Random random, int i, int j, int k, int l) {
      int m = chunkGenerator.getConfig().getEndCityDistance();
      int n = chunkGenerator.getConfig().getEndCitySeparation();
      int o = i + m * k;
      int p = j + m * l;
      int q = o < 0 ? o - m + 1 : o;
      int r = p < 0 ? p - m + 1 : p;
      int s = q / m;
      int t = r / m;
      ((ChunkRandom)random).setStructureSeed(chunkGenerator.getSeed(), s, t, 10387313);
      s *= m;
      t *= m;
      s += (random.nextInt(m - n) + random.nextInt(m - n)) / 2;
      t += (random.nextInt(m - n) + random.nextInt(m - n)) / 2;
      return new ChunkPos(s, t);
   }

   public boolean shouldStartAt(BiomeAccess biomeAccess, ChunkGenerator<?> chunkGenerator, Random random, int chunkZ, int i, Biome biome) {
      ChunkPos chunkPos = this.getStart(chunkGenerator, random, chunkZ, i, 0, 0);
      if (chunkZ == chunkPos.x && i == chunkPos.z) {
         if (!chunkGenerator.hasStructure(biome, this)) {
            return false;
         } else {
            int j = getGenerationHeight(chunkZ, i, chunkGenerator);
            return j >= 60;
         }
      } else {
         return false;
      }
   }

   public StructureFeature.StructureStartFactory getStructureStartFactory() {
      return EndCityFeature.Start::new;
   }

   public String getName() {
      return "EndCity";
   }

   public int getRadius() {
      return 8;
   }

   private static int getGenerationHeight(int chunkX, int chunkZ, ChunkGenerator<?> chunkGenerator) {
      Random random = new Random((long)(chunkX + chunkZ * 10387313));
      BlockRotation blockRotation = BlockRotation.values()[random.nextInt(BlockRotation.values().length)];
      int i = 5;
      int j = 5;
      if (blockRotation == BlockRotation.CLOCKWISE_90) {
         i = -5;
      } else if (blockRotation == BlockRotation.CLOCKWISE_180) {
         i = -5;
         j = -5;
      } else if (blockRotation == BlockRotation.COUNTERCLOCKWISE_90) {
         j = -5;
      }

      int k = (chunkX << 4) + 7;
      int l = (chunkZ << 4) + 7;
      int m = chunkGenerator.getHeightInGround(k, l, Heightmap.Type.WORLD_SURFACE_WG);
      int n = chunkGenerator.getHeightInGround(k, l + j, Heightmap.Type.WORLD_SURFACE_WG);
      int o = chunkGenerator.getHeightInGround(k + i, l, Heightmap.Type.WORLD_SURFACE_WG);
      int p = chunkGenerator.getHeightInGround(k + i, l + j, Heightmap.Type.WORLD_SURFACE_WG);
      return Math.min(Math.min(m, n), Math.min(o, p));
   }

   public static class Start extends StructureStart {
      public Start(StructureFeature<?> structureFeature, int chunkX, int chunkZ, BlockBox blockBox, int i, long l) {
         super(structureFeature, chunkX, chunkZ, blockBox, i, l);
      }

      public void initialize(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int x, int z, Biome biome) {
         BlockRotation blockRotation = BlockRotation.values()[this.random.nextInt(BlockRotation.values().length)];
         int i = EndCityFeature.getGenerationHeight(x, z, chunkGenerator);
         if (i >= 60) {
            BlockPos blockPos = new BlockPos(x * 16 + 8, i, z * 16 + 8);
            EndCityGenerator.addPieces(structureManager, blockPos, blockRotation, this.children, this.random);
            this.setBoundingBoxFromChildren();
         }
      }
   }
}
