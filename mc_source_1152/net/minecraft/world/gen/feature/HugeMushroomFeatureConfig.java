package net.minecraft.world.gen.feature;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.stateprovider.StateProvider;
import net.minecraft.world.gen.stateprovider.StateProviderType;

public class HugeMushroomFeatureConfig implements FeatureConfig {
   public final StateProvider capProvider;
   public final StateProvider stemProvider;
   public final int capSize;

   public HugeMushroomFeatureConfig(StateProvider capProvider, StateProvider stemProvider, int capSize) {
      this.capProvider = capProvider;
      this.stemProvider = stemProvider;
      this.capSize = capSize;
   }

   public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
      Builder<T, T> builder = ImmutableMap.builder();
      builder.put(ops.createString("cap_provider"), this.capProvider.serialize(ops)).put(ops.createString("stem_provider"), this.stemProvider.serialize(ops)).put(ops.createString("foliage_radius"), ops.createInt(this.capSize));
      return new Dynamic(ops, ops.createMap(builder.build()));
   }

   public static <T> HugeMushroomFeatureConfig deserialize(Dynamic<T> dynamic) {
      StateProviderType<?> stateProviderType = (StateProviderType)Registry.BLOCK_STATE_PROVIDER_TYPE.get(new Identifier((String)dynamic.get("cap_provider").get("type").asString().orElseThrow(RuntimeException::new)));
      StateProviderType<?> stateProviderType2 = (StateProviderType)Registry.BLOCK_STATE_PROVIDER_TYPE.get(new Identifier((String)dynamic.get("stem_provider").get("type").asString().orElseThrow(RuntimeException::new)));
      return new HugeMushroomFeatureConfig(stateProviderType.deserialize(dynamic.get("cap_provider").orElseEmptyMap()), stateProviderType2.deserialize(dynamic.get("stem_provider").orElseEmptyMap()), dynamic.get("foliage_radius").asInt(2));
   }
}
