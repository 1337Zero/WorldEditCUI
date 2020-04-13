package net.minecraft.world.dimension;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.source.BiomeSourceType;
import net.minecraft.world.biome.source.FixedBiomeSourceConfig;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.chunk.CavesChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorType;

public class TheNetherDimension extends Dimension {
   private static final Vec3d field_21216 = new Vec3d(0.20000000298023224D, 0.029999999329447746D, 0.029999999329447746D);

   public TheNetherDimension(World world, DimensionType type) {
      super(world, type, 0.1F);
      this.waterVaporizes = true;
      this.isNether = true;
   }

   @Environment(EnvType.CLIENT)
   public Vec3d getFogColor(float skyAngle, float tickDelta) {
      return field_21216;
   }

   public ChunkGenerator<?> createChunkGenerator() {
      CavesChunkGeneratorConfig cavesChunkGeneratorConfig = (CavesChunkGeneratorConfig)ChunkGeneratorType.CAVES.createSettings();
      cavesChunkGeneratorConfig.setDefaultBlock(Blocks.NETHERRACK.getDefaultState());
      cavesChunkGeneratorConfig.setDefaultFluid(Blocks.LAVA.getDefaultState());
      return ChunkGeneratorType.CAVES.create(this.world, BiomeSourceType.FIXED.applyConfig(((FixedBiomeSourceConfig)BiomeSourceType.FIXED.getConfig(this.world.getLevelProperties())).setBiome(Biomes.NETHER)), cavesChunkGeneratorConfig);
   }

   public boolean hasVisibleSky() {
      return false;
   }

   @Nullable
   public BlockPos getSpawningBlockInChunk(ChunkPos chunkPos, boolean checkMobSpawnValidity) {
      return null;
   }

   @Nullable
   public BlockPos getTopSpawningBlockPosition(int x, int z, boolean checkMobSpawnValidity) {
      return null;
   }

   public float getSkyAngle(long timeOfDay, float tickDelta) {
      return 0.5F;
   }

   public boolean canPlayersSleep() {
      return false;
   }

   @Environment(EnvType.CLIENT)
   public boolean isFogThick(int x, int z) {
      return true;
   }

   public WorldBorder createWorldBorder() {
      return new WorldBorder() {
         public double getCenterX() {
            return super.getCenterX() / 8.0D;
         }

         public double getCenterZ() {
            return super.getCenterZ() / 8.0D;
         }
      };
   }

   public DimensionType getType() {
      return DimensionType.THE_NETHER;
   }
}
