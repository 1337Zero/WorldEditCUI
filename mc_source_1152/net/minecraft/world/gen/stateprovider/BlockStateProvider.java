package net.minecraft.world.gen.stateprovider;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

public class BlockStateProvider extends StateProvider {
   private final Block block;

   public BlockStateProvider(Block block) {
      super(StateProviderType.SIMPLE_STATE_PROVIDER);
      this.block = block;
   }

   public <T> BlockStateProvider(Dynamic<T> configDeserializer) {
      this(BlockState.deserialize(configDeserializer.get("state").orElseEmptyMap()).getBlock());
   }

   public BlockState getBlockState(Random random, BlockPos pos) {
      Direction.Axis axis = Direction.Axis.pickRandomAxis(random);
      return (BlockState)this.block.getDefaultState().with(PillarBlock.AXIS, axis);
   }

   public <T> T serialize(DynamicOps<T> ops) {
      Builder<T, T> builder = ImmutableMap.builder();
      builder.put(ops.createString("type"), ops.createString(Registry.BLOCK_STATE_PROVIDER_TYPE.getId(this.stateProvider).toString())).put(ops.createString("state"), BlockState.serialize(ops, this.block.getDefaultState()).getValue());
      return (new Dynamic(ops, ops.createMap(builder.build()))).getValue();
   }
}
