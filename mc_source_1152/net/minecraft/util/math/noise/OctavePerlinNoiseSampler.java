package net.minecraft.util.math.noise;

import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.ChunkRandom;

public class OctavePerlinNoiseSampler implements NoiseSampler {
   private final PerlinNoiseSampler[] octaveSamplers;
   private final double field_20659;
   private final double field_20660;

   public OctavePerlinNoiseSampler(ChunkRandom chunkRandom, int i, int j) {
      this(chunkRandom, new IntRBTreeSet(IntStream.rangeClosed(-i, j).toArray()));
   }

   public OctavePerlinNoiseSampler(ChunkRandom chunkRandom, IntSortedSet intSortedSet) {
      if (intSortedSet.isEmpty()) {
         throw new IllegalArgumentException("Need some octaves!");
      } else {
         int i = -intSortedSet.firstInt();
         int j = intSortedSet.lastInt();
         int k = i + j + 1;
         if (k < 1) {
            throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
         } else {
            PerlinNoiseSampler perlinNoiseSampler = new PerlinNoiseSampler(chunkRandom);
            int l = j;
            this.octaveSamplers = new PerlinNoiseSampler[k];
            if (j >= 0 && j < k && intSortedSet.contains(0)) {
               this.octaveSamplers[j] = perlinNoiseSampler;
            }

            for(int m = j + 1; m < k; ++m) {
               if (m >= 0 && intSortedSet.contains(l - m)) {
                  this.octaveSamplers[m] = new PerlinNoiseSampler(chunkRandom);
               } else {
                  chunkRandom.consume(262);
               }
            }

            if (j > 0) {
               long n = (long)(perlinNoiseSampler.sample(0.0D, 0.0D, 0.0D, 0.0D, 0.0D) * 9.223372036854776E18D);
               ChunkRandom chunkRandom2 = new ChunkRandom(n);

               for(int o = l - 1; o >= 0; --o) {
                  if (o < k && intSortedSet.contains(l - o)) {
                     this.octaveSamplers[o] = new PerlinNoiseSampler(chunkRandom2);
                  } else {
                     chunkRandom2.consume(262);
                  }
               }
            }

            this.field_20660 = Math.pow(2.0D, (double)j);
            this.field_20659 = 1.0D / (Math.pow(2.0D, (double)k) - 1.0D);
         }
      }
   }

   public double sample(double x, double y, double z) {
      return this.sample(x, y, z, 0.0D, 0.0D, false);
   }

   public double sample(double x, double y, double z, double d, double e, boolean bl) {
      double f = 0.0D;
      double g = this.field_20660;
      double h = this.field_20659;
      PerlinNoiseSampler[] var18 = this.octaveSamplers;
      int var19 = var18.length;

      for(int var20 = 0; var20 < var19; ++var20) {
         PerlinNoiseSampler perlinNoiseSampler = var18[var20];
         if (perlinNoiseSampler != null) {
            f += perlinNoiseSampler.sample(maintainPrecision(x * g), bl ? -perlinNoiseSampler.originY : maintainPrecision(y * g), maintainPrecision(z * g), d * g, e * g) * h;
         }

         g /= 2.0D;
         h *= 2.0D;
      }

      return f;
   }

   @Nullable
   public PerlinNoiseSampler getOctave(int octave) {
      return this.octaveSamplers[octave];
   }

   public static double maintainPrecision(double d) {
      return d - (double)MathHelper.lfloor(d / 3.3554432E7D + 0.5D) * 3.3554432E7D;
   }

   public double sample(double x, double y, double d, double e) {
      return this.sample(x, y, 0.0D, d, e, false);
   }
}
