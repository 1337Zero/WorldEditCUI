package net.minecraft.world.gen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.entity.EntityType;
import net.minecraft.structure.OceanMonumentGenerator;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class OceanMonumentFeature extends StructureFeature<DefaultFeatureConfig> {
   private static final List<Biome.SpawnEntry> MONSTER_SPAWNS;

   public OceanMonumentFeature(Function<Dynamic<?>, ? extends DefaultFeatureConfig> configFactory) {
      super(configFactory);
   }

   protected ChunkPos getStart(ChunkGenerator<?> chunkGenerator, Random random, int i, int j, int k, int l) {
      int m = chunkGenerator.getConfig().getOceanMonumentSpacing();
      int n = chunkGenerator.getConfig().getOceanMonumentSeparation();
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
         Set<Biome> set = chunkGenerator.getBiomeSource().getBiomesInArea(chunkZ * 16 + 9, chunkGenerator.getSeaLevel(), i * 16 + 9, 16);
         Iterator var9 = set.iterator();

         Biome biome2;
         do {
            if (!var9.hasNext()) {
               Set<Biome> set2 = chunkGenerator.getBiomeSource().getBiomesInArea(chunkZ * 16 + 9, chunkGenerator.getSeaLevel(), i * 16 + 9, 29);
               Iterator var13 = set2.iterator();

               Biome biome3;
               do {
                  if (!var13.hasNext()) {
                     return true;
                  }

                  biome3 = (Biome)var13.next();
               } while(biome3.getCategory() == Biome.Category.OCEAN || biome3.getCategory() == Biome.Category.RIVER);

               return false;
            }

            biome2 = (Biome)var9.next();
         } while(chunkGenerator.hasStructure(biome2, this));

         return false;
      } else {
         return false;
      }
   }

   public StructureFeature.StructureStartFactory getStructureStartFactory() {
      return OceanMonumentFeature.Start::new;
   }

   public String getName() {
      return "Monument";
   }

   public int getRadius() {
      return 8;
   }

   public List<Biome.SpawnEntry> getMonsterSpawns() {
      return MONSTER_SPAWNS;
   }

   static {
      MONSTER_SPAWNS = Lists.newArrayList(new Biome.SpawnEntry[]{new Biome.SpawnEntry(EntityType.GUARDIAN, 1, 2, 4)});
   }

   public static class Start extends StructureStart {
      private boolean field_13717;

      public Start(StructureFeature<?> structureFeature, int chunkX, int chunkZ, BlockBox blockBox, int i, long l) {
         super(structureFeature, chunkX, chunkZ, blockBox, i, l);
      }

      public void initialize(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int x, int z, Biome biome) {
         this.method_16588(x, z);
      }

      private void method_16588(int chunkX, int chunkZ) {
         int i = chunkX * 16 - 29;
         int j = chunkZ * 16 - 29;
         Direction direction = Direction.Type.HORIZONTAL.random(this.random);
         this.children.add(new OceanMonumentGenerator.Base(this.random, i, j, direction));
         this.setBoundingBoxFromChildren();
         this.field_13717 = true;
      }

      public void generateStructure(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         if (!this.field_13717) {
            this.children.clear();
            this.method_16588(this.getChunkX(), this.getChunkZ());
         }

         super.generateStructure(world, chunkGenerator, random, blockBox, chunkPos);
      }
   }
}
