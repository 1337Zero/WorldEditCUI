package net.minecraft.world.biome.source;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class FixedBiomeSource extends BiomeSource {
   private final Biome biome;

   public FixedBiomeSource(FixedBiomeSourceConfig config) {
      super(ImmutableSet.of(config.getBiome()));
      this.biome = config.getBiome();
   }

   public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
      return this.biome;
   }

   @Nullable
   public BlockPos locateBiome(int x, int y, int z, int radius, List<Biome> list, Random random) {
      return list.contains(this.biome) ? new BlockPos(x - radius + random.nextInt(radius * 2 + 1), y, z - radius + random.nextInt(radius * 2 + 1)) : null;
   }

   public Set<Biome> getBiomesInArea(int x, int y, int z, int radius) {
      return Sets.newHashSet(new Biome[]{this.biome});
   }
}
