package net.minecraft.world.gen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class BoulderFeatureConfig implements FeatureConfig {
   public final BlockState state;
   public final int startRadius;

   public BoulderFeatureConfig(BlockState state, int startRadius) {
      this.state = state;
      this.startRadius = startRadius;
   }

   public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
      return new Dynamic(ops, ops.createMap(ImmutableMap.of(ops.createString("state"), BlockState.serialize(ops, this.state).getValue(), ops.createString("start_radius"), ops.createInt(this.startRadius))));
   }

   public static <T> BoulderFeatureConfig deserialize(Dynamic<T> dynamic) {
      BlockState blockState = (BlockState)dynamic.get("state").map(BlockState::deserialize).orElse(Blocks.AIR.getDefaultState());
      int i = dynamic.get("start_radius").asInt(0);
      return new BoulderFeatureConfig(blockState, i);
   }
}
