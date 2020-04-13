package net.minecraft.world.gen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.function.Function;
import net.minecraft.entity.EntityType;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.SwampHutGenerator;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class SwampHutFeature extends AbstractTempleFeature<DefaultFeatureConfig> {
   private static final List<Biome.SpawnEntry> MONSTER_SPAWNS;
   private static final List<Biome.SpawnEntry> CREATURE_SPAWNS;

   public SwampHutFeature(Function<Dynamic<?>, ? extends DefaultFeatureConfig> configFactory) {
      super(configFactory);
   }

   public String getName() {
      return "Swamp_Hut";
   }

   public int getRadius() {
      return 3;
   }

   public StructureFeature.StructureStartFactory getStructureStartFactory() {
      return SwampHutFeature.Start::new;
   }

   protected int getSeedModifier() {
      return 14357620;
   }

   public List<Biome.SpawnEntry> getMonsterSpawns() {
      return MONSTER_SPAWNS;
   }

   public List<Biome.SpawnEntry> getCreatureSpawns() {
      return CREATURE_SPAWNS;
   }

   public boolean method_14029(IWorld iWorld, BlockPos blockPos) {
      StructureStart structureStart = this.isInsideStructure(iWorld, blockPos, true);
      if (structureStart != StructureStart.DEFAULT && structureStart instanceof SwampHutFeature.Start && !structureStart.getChildren().isEmpty()) {
         StructurePiece structurePiece = (StructurePiece)structureStart.getChildren().get(0);
         return structurePiece instanceof SwampHutGenerator;
      } else {
         return false;
      }
   }

   static {
      MONSTER_SPAWNS = Lists.newArrayList(new Biome.SpawnEntry[]{new Biome.SpawnEntry(EntityType.WITCH, 1, 1, 1)});
      CREATURE_SPAWNS = Lists.newArrayList(new Biome.SpawnEntry[]{new Biome.SpawnEntry(EntityType.CAT, 1, 1, 1)});
   }

   public static class Start extends StructureStart {
      public Start(StructureFeature<?> structureFeature, int chunkX, int chunkZ, BlockBox blockBox, int i, long l) {
         super(structureFeature, chunkX, chunkZ, blockBox, i, l);
      }

      public void initialize(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int x, int z, Biome biome) {
         SwampHutGenerator swampHutGenerator = new SwampHutGenerator(this.random, x * 16, z * 16);
         this.children.add(swampHutGenerator);
         this.setBoundingBoxFromChildren();
      }
   }
}
