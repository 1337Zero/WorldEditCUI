package net.minecraft.world.gen.chunk;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.source.BiomeSource;

public class FloatingIslandsChunkGenerator extends SurfaceChunkGenerator<FloatingIslandsChunkGeneratorConfig> {
   private final BlockPos center;

   public FloatingIslandsChunkGenerator(IWorld iWorld, BiomeSource biomeSource, FloatingIslandsChunkGeneratorConfig floatingIslandsChunkGeneratorConfig) {
      super(iWorld, biomeSource, 8, 4, 128, floatingIslandsChunkGeneratorConfig, true);
      this.center = floatingIslandsChunkGeneratorConfig.getCenter();
   }

   protected void sampleNoiseColumn(double[] buffer, int x, int z) {
      double d = 1368.824D;
      double e = 684.412D;
      double f = 17.110300000000002D;
      double g = 4.277575000000001D;
      int i = true;
      int j = true;
      this.sampleNoiseColumn(buffer, x, z, 1368.824D, 684.412D, 17.110300000000002D, 4.277575000000001D, 64, -3000);
   }

   protected double[] computeNoiseRange(int x, int z) {
      return new double[]{(double)this.biomeSource.getNoiseRange(x, z), 0.0D};
   }

   protected double computeNoiseFalloff(double depth, double scale, int y) {
      return 8.0D - depth;
   }

   protected double method_16409() {
      return (double)((int)super.method_16409() / 2);
   }

   protected double method_16410() {
      return 8.0D;
   }

   public int getSpawnHeight() {
      return 50;
   }

   public int getSeaLevel() {
      return 0;
   }
}
