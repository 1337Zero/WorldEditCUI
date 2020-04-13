package net.minecraft.world.gen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;

public class StrongholdFeature extends StructureFeature<DefaultFeatureConfig> {
   private boolean stateStillValid;
   private ChunkPos[] startPositions;
   private final List<StructureStart> starts = Lists.newArrayList();
   private long lastSeed;

   public StrongholdFeature(Function<Dynamic<?>, ? extends DefaultFeatureConfig> configFactory) {
      super(configFactory);
   }

   public boolean shouldStartAt(BiomeAccess biomeAccess, ChunkGenerator<?> chunkGenerator, Random random, int chunkZ, int i, Biome biome) {
      if (this.lastSeed != chunkGenerator.getSeed()) {
         this.invalidateState();
      }

      if (!this.stateStillValid) {
         this.initialize(chunkGenerator);
         this.stateStillValid = true;
      }

      ChunkPos[] var7 = this.startPositions;
      int var8 = var7.length;

      for(int var9 = 0; var9 < var8; ++var9) {
         ChunkPos chunkPos = var7[var9];
         if (chunkZ == chunkPos.x && i == chunkPos.z) {
            return true;
         }
      }

      return false;
   }

   private void invalidateState() {
      this.stateStillValid = false;
      this.startPositions = null;
      this.starts.clear();
   }

   public StructureFeature.StructureStartFactory getStructureStartFactory() {
      return StrongholdFeature.Start::new;
   }

   public String getName() {
      return "Stronghold";
   }

   public int getRadius() {
      return 8;
   }

   @Nullable
   public BlockPos locateStructure(World world, ChunkGenerator<? extends ChunkGeneratorConfig> chunkGenerator, BlockPos blockPos, int i, boolean skipExistingChunks) {
      if (!chunkGenerator.getBiomeSource().hasStructureFeature(this)) {
         return null;
      } else {
         if (this.lastSeed != world.getSeed()) {
            this.invalidateState();
         }

         if (!this.stateStillValid) {
            this.initialize(chunkGenerator);
            this.stateStillValid = true;
         }

         BlockPos blockPos2 = null;
         BlockPos.Mutable mutable = new BlockPos.Mutable();
         double d = Double.MAX_VALUE;
         ChunkPos[] var10 = this.startPositions;
         int var11 = var10.length;

         for(int var12 = 0; var12 < var11; ++var12) {
            ChunkPos chunkPos = var10[var12];
            mutable.set((chunkPos.x << 4) + 8, 32, (chunkPos.z << 4) + 8);
            double e = mutable.getSquaredDistance(blockPos);
            if (blockPos2 == null) {
               blockPos2 = new BlockPos(mutable);
               d = e;
            } else if (e < d) {
               blockPos2 = new BlockPos(mutable);
               d = e;
            }
         }

         return blockPos2;
      }
   }

   private void initialize(ChunkGenerator<?> chunkGenerator) {
      this.lastSeed = chunkGenerator.getSeed();
      List<Biome> list = Lists.newArrayList();
      Iterator var3 = Registry.BIOME.iterator();

      while(var3.hasNext()) {
         Biome biome = (Biome)var3.next();
         if (biome != null && chunkGenerator.hasStructure(biome, this)) {
            list.add(biome);
         }
      }

      int i = chunkGenerator.getConfig().getStrongholdDistance();
      int j = chunkGenerator.getConfig().getStrongholdCount();
      int k = chunkGenerator.getConfig().getStrongholdSpread();
      this.startPositions = new ChunkPos[j];
      int l = 0;
      Iterator var7 = this.starts.iterator();

      while(var7.hasNext()) {
         StructureStart structureStart = (StructureStart)var7.next();
         if (l < this.startPositions.length) {
            this.startPositions[l++] = new ChunkPos(structureStart.getChunkX(), structureStart.getChunkZ());
         }
      }

      Random random = new Random();
      random.setSeed(chunkGenerator.getSeed());
      double d = random.nextDouble() * 3.141592653589793D * 2.0D;
      int m = l;
      if (l < this.startPositions.length) {
         int n = 0;
         int o = 0;

         for(int p = 0; p < this.startPositions.length; ++p) {
            double e = (double)(4 * i + i * o * 6) + (random.nextDouble() - 0.5D) * (double)i * 2.5D;
            int q = (int)Math.round(Math.cos(d) * e);
            int r = (int)Math.round(Math.sin(d) * e);
            BlockPos blockPos = chunkGenerator.getBiomeSource().locateBiome((q << 4) + 8, chunkGenerator.getSeaLevel(), (r << 4) + 8, 112, list, random);
            if (blockPos != null) {
               q = blockPos.getX() >> 4;
               r = blockPos.getZ() >> 4;
            }

            if (p >= m) {
               this.startPositions[p] = new ChunkPos(q, r);
            }

            d += 6.283185307179586D / (double)k;
            ++n;
            if (n == k) {
               ++o;
               n = 0;
               k += 2 * k / (o + 1);
               k = Math.min(k, this.startPositions.length - p);
               d += random.nextDouble() * 3.141592653589793D * 2.0D;
            }
         }
      }

   }

   public static class Start extends StructureStart {
      public Start(StructureFeature<?> structureFeature, int chunkX, int chunkZ, BlockBox blockBox, int i, long l) {
         super(structureFeature, chunkX, chunkZ, blockBox, i, l);
      }

      public void initialize(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int x, int z, Biome biome) {
         int i = 0;
         long l = chunkGenerator.getSeed();

         StrongholdGenerator.Start start;
         do {
            this.children.clear();
            this.boundingBox = BlockBox.empty();
            this.random.setStructureSeed(l + (long)(i++), x, z);
            StrongholdGenerator.method_14855();
            start = new StrongholdGenerator.Start(this.random, (x << 4) + 2, (z << 4) + 2);
            this.children.add(start);
            start.method_14918(start, this.children, this.random);
            List list = start.field_15282;

            while(!list.isEmpty()) {
               int j = this.random.nextInt(list.size());
               StructurePiece structurePiece = (StructurePiece)list.remove(j);
               structurePiece.method_14918(start, this.children, this.random);
            }

            this.setBoundingBoxFromChildren();
            this.method_14978(chunkGenerator.getSeaLevel(), this.random, 10);
         } while(this.children.isEmpty() || start.field_15283 == null);

         ((StrongholdFeature)this.getFeature()).starts.add(this);
      }
   }
}
