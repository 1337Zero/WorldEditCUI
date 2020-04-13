package net.minecraft.world.gen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.entity.EntityType;
import net.minecraft.structure.PillagerOutpostGenerator;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.VillageStructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class PillagerOutpostFeature extends AbstractTempleFeature<DefaultFeatureConfig> {
   private static final List<Biome.SpawnEntry> MONSTER_SPAWNS;

   public PillagerOutpostFeature(Function<Dynamic<?>, ? extends DefaultFeatureConfig> configFactory) {
      super(configFactory);
   }

   public String getName() {
      return "Pillager_Outpost";
   }

   public int getRadius() {
      return 3;
   }

   public List<Biome.SpawnEntry> getMonsterSpawns() {
      return MONSTER_SPAWNS;
   }

   public boolean shouldStartAt(BiomeAccess biomeAccess, ChunkGenerator<?> chunkGenerator, Random random, int chunkZ, int i, Biome biome) {
      ChunkPos chunkPos = this.getStart(chunkGenerator, random, chunkZ, i, 0, 0);
      if (chunkZ == chunkPos.x && i == chunkPos.z) {
         int j = chunkZ >> 4;
         int k = i >> 4;
         random.setSeed((long)(j ^ k << 4) ^ chunkGenerator.getSeed());
         random.nextInt();
         if (random.nextInt(5) != 0) {
            return false;
         }

         if (chunkGenerator.hasStructure(biome, this)) {
            for(int l = chunkZ - 10; l <= chunkZ + 10; ++l) {
               for(int m = i - 10; m <= i + 10; ++m) {
                  if (Feature.VILLAGE.shouldStartAt(biomeAccess, chunkGenerator, random, l, m, biomeAccess.getBiome(new BlockPos((l << 4) + 9, 0, (m << 4) + 9)))) {
                     return false;
                  }
               }
            }

            return true;
         }
      }

      return false;
   }

   public StructureFeature.StructureStartFactory getStructureStartFactory() {
      return PillagerOutpostFeature.Start::new;
   }

   protected int getSeedModifier() {
      return 165745296;
   }

   static {
      MONSTER_SPAWNS = Lists.newArrayList(new Biome.SpawnEntry[]{new Biome.SpawnEntry(EntityType.PILLAGER, 1, 1, 1)});
   }

   public static class Start extends VillageStructureStart {
      public Start(StructureFeature<?> structureFeature, int chunkX, int chunkZ, BlockBox blockBox, int i, long l) {
         super(structureFeature, chunkX, chunkZ, blockBox, i, l);
      }

      public void initialize(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int x, int z, Biome biome) {
         BlockPos blockPos = new BlockPos(x * 16, 90, z * 16);
         PillagerOutpostGenerator.addPieces(chunkGenerator, structureManager, blockPos, this.children, this.random);
         this.setBoundingBoxFromChildren();
      }
   }
}
