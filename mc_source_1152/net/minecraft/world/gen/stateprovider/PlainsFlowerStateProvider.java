package net.minecraft.world.gen.stateprovider;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

public class PlainsFlowerStateProvider extends StateProvider {
   private static final BlockState[] tulips;
   private static final BlockState[] flowers;

   public PlainsFlowerStateProvider() {
      super(StateProviderType.PLAIN_FLOWER_PROVIDER);
   }

   public <T> PlainsFlowerStateProvider(Dynamic<T> configDeserializer) {
      this();
   }

   public BlockState getBlockState(Random random, BlockPos pos) {
      double d = Biome.FOLIAGE_NOISE.sample((double)pos.getX() / 200.0D, (double)pos.getZ() / 200.0D, false);
      if (d < -0.8D) {
         return tulips[random.nextInt(tulips.length)];
      } else {
         return random.nextInt(3) > 0 ? flowers[random.nextInt(flowers.length)] : Blocks.DANDELION.getDefaultState();
      }
   }

   public <T> T serialize(DynamicOps<T> ops) {
      Builder<T, T> builder = ImmutableMap.builder();
      builder.put(ops.createString("type"), ops.createString(Registry.BLOCK_STATE_PROVIDER_TYPE.getId(this.stateProvider).toString()));
      return (new Dynamic(ops, ops.createMap(builder.build()))).getValue();
   }

   static {
      tulips = new BlockState[]{Blocks.ORANGE_TULIP.getDefaultState(), Blocks.RED_TULIP.getDefaultState(), Blocks.PINK_TULIP.getDefaultState(), Blocks.WHITE_TULIP.getDefaultState()};
      flowers = new BlockState[]{Blocks.POPPY.getDefaultState(), Blocks.AZURE_BLUET.getDefaultState(), Blocks.OXEYE_DAISY.getDefaultState(), Blocks.CORNFLOWER.getDefaultState()};
   }
}
