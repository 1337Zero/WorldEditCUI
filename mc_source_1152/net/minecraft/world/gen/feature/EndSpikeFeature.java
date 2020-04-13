package net.minecraft.world.gen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PaneBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EnderCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;

public class EndSpikeFeature extends Feature<EndSpikeFeatureConfig> {
   private static final LoadingCache<Long, List<EndSpikeFeature.Spike>> CACHE;

   public EndSpikeFeature(Function<Dynamic<?>, ? extends EndSpikeFeatureConfig> configFactory) {
      super(configFactory);
   }

   public static List<EndSpikeFeature.Spike> getSpikes(IWorld iWorld) {
      Random random = new Random(iWorld.getSeed());
      long l = random.nextLong() & 65535L;
      return (List)CACHE.getUnchecked(l);
   }

   public boolean generate(IWorld iWorld, ChunkGenerator<? extends ChunkGeneratorConfig> chunkGenerator, Random random, BlockPos blockPos, EndSpikeFeatureConfig endSpikeFeatureConfig) {
      List<EndSpikeFeature.Spike> list = endSpikeFeatureConfig.getSpikes();
      if (list.isEmpty()) {
         list = getSpikes(iWorld);
      }

      Iterator var7 = list.iterator();

      while(var7.hasNext()) {
         EndSpikeFeature.Spike spike = (EndSpikeFeature.Spike)var7.next();
         if (spike.isInChunk(blockPos)) {
            this.generateSpike(iWorld, random, endSpikeFeatureConfig, spike);
         }
      }

      return true;
   }

   private void generateSpike(IWorld world, Random random, EndSpikeFeatureConfig config, EndSpikeFeature.Spike spike) {
      int i = spike.getRadius();
      Iterator var6 = BlockPos.iterate(new BlockPos(spike.getCenterX() - i, 0, spike.getCenterZ() - i), new BlockPos(spike.getCenterX() + i, spike.getHeight() + 10, spike.getCenterZ() + i)).iterator();

      while(true) {
         while(var6.hasNext()) {
            BlockPos blockPos = (BlockPos)var6.next();
            if (blockPos.getSquaredDistance((double)spike.getCenterX(), (double)blockPos.getY(), (double)spike.getCenterZ(), false) <= (double)(i * i + 1) && blockPos.getY() < spike.getHeight()) {
               this.setBlockState(world, blockPos, Blocks.OBSIDIAN.getDefaultState());
            } else if (blockPos.getY() > 65) {
               this.setBlockState(world, blockPos, Blocks.AIR.getDefaultState());
            }
         }

         if (spike.isGuarded()) {
            int j = true;
            int k = true;
            int l = true;
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            for(int m = -2; m <= 2; ++m) {
               for(int n = -2; n <= 2; ++n) {
                  for(int o = 0; o <= 3; ++o) {
                     boolean bl = MathHelper.abs(m) == 2;
                     boolean bl2 = MathHelper.abs(n) == 2;
                     boolean bl3 = o == 3;
                     if (bl || bl2 || bl3) {
                        boolean bl4 = m == -2 || m == 2 || bl3;
                        boolean bl5 = n == -2 || n == 2 || bl3;
                        BlockState blockState = (BlockState)((BlockState)((BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, bl4 && n != -2)).with(PaneBlock.SOUTH, bl4 && n != 2)).with(PaneBlock.WEST, bl5 && m != -2)).with(PaneBlock.EAST, bl5 && m != 2);
                        this.setBlockState(world, mutable.set(spike.getCenterX() + m, spike.getHeight() + o, spike.getCenterZ() + n), blockState);
                     }
                  }
               }
            }
         }

         EnderCrystalEntity enderCrystalEntity = (EnderCrystalEntity)EntityType.END_CRYSTAL.create(world.getWorld());
         enderCrystalEntity.setBeamTarget(config.getPos());
         enderCrystalEntity.setInvulnerable(config.isCrystalInvulerable());
         enderCrystalEntity.refreshPositionAndAngles((double)((float)spike.getCenterX() + 0.5F), (double)(spike.getHeight() + 1), (double)((float)spike.getCenterZ() + 0.5F), random.nextFloat() * 360.0F, 0.0F);
         world.spawnEntity(enderCrystalEntity);
         this.setBlockState(world, new BlockPos(spike.getCenterX(), spike.getHeight(), spike.getCenterZ()), Blocks.BEDROCK.getDefaultState());
         return;
      }
   }

   static {
      CACHE = CacheBuilder.newBuilder().expireAfterWrite(5L, TimeUnit.MINUTES).build(new EndSpikeFeature.SpikeCache());
   }

   static class SpikeCache extends CacheLoader<Long, List<EndSpikeFeature.Spike>> {
      private SpikeCache() {
      }

      public List<EndSpikeFeature.Spike> load(Long var1) {
         List<Integer> list = (List)IntStream.range(0, 10).boxed().collect(Collectors.toList());
         Collections.shuffle(list, new Random(var1));
         List<EndSpikeFeature.Spike> list2 = Lists.newArrayList();

         for(int i = 0; i < 10; ++i) {
            int j = MathHelper.floor(42.0D * Math.cos(2.0D * (-3.141592653589793D + 0.3141592653589793D * (double)i)));
            int k = MathHelper.floor(42.0D * Math.sin(2.0D * (-3.141592653589793D + 0.3141592653589793D * (double)i)));
            int l = (Integer)list.get(i);
            int m = 2 + l / 3;
            int n = 76 + l * 3;
            boolean bl = l == 1 || l == 2;
            list2.add(new EndSpikeFeature.Spike(j, k, m, n, bl));
         }

         return list2;
      }
   }

   public static class Spike {
      private final int centerX;
      private final int centerZ;
      private final int radius;
      private final int height;
      private final boolean guarded;
      private final Box boundingBox;

      public Spike(int centerX, int centerZ, int radius, int height, boolean bl) {
         this.centerX = centerX;
         this.centerZ = centerZ;
         this.radius = radius;
         this.height = height;
         this.guarded = bl;
         this.boundingBox = new Box((double)(centerX - radius), 0.0D, (double)(centerZ - radius), (double)(centerX + radius), 256.0D, (double)(centerZ + radius));
      }

      public boolean isInChunk(BlockPos pos) {
         return pos.getX() >> 4 == this.centerX >> 4 && pos.getZ() >> 4 == this.centerZ >> 4;
      }

      public int getCenterX() {
         return this.centerX;
      }

      public int getCenterZ() {
         return this.centerZ;
      }

      public int getRadius() {
         return this.radius;
      }

      public int getHeight() {
         return this.height;
      }

      public boolean isGuarded() {
         return this.guarded;
      }

      public Box getBoundingBox() {
         return this.boundingBox;
      }

      public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
         Builder<T, T> builder = ImmutableMap.builder();
         builder.put(dynamicOps.createString("centerX"), dynamicOps.createInt(this.centerX));
         builder.put(dynamicOps.createString("centerZ"), dynamicOps.createInt(this.centerZ));
         builder.put(dynamicOps.createString("radius"), dynamicOps.createInt(this.radius));
         builder.put(dynamicOps.createString("height"), dynamicOps.createInt(this.height));
         builder.put(dynamicOps.createString("guarded"), dynamicOps.createBoolean(this.guarded));
         return new Dynamic(dynamicOps, dynamicOps.createMap(builder.build()));
      }

      public static <T> EndSpikeFeature.Spike deserialize(Dynamic<T> dynamic) {
         return new EndSpikeFeature.Spike(dynamic.get("centerX").asInt(0), dynamic.get("centerZ").asInt(0), dynamic.get("radius").asInt(0), dynamic.get("height").asInt(0), dynamic.get("guarded").asBoolean(false));
      }
   }
}
