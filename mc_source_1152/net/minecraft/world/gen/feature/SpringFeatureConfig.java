package net.minecraft.world.gen.feature;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;

public class SpringFeatureConfig implements FeatureConfig {
   public final FluidState state;
   public final boolean requiresBlockBelow;
   public final int rockCount;
   public final int holeCount;
   public final Set<Block> validBlocks;

   public SpringFeatureConfig(FluidState state, boolean requiresBlockBelow, int rockCount, int holeCount, Set<Block> validBlocks) {
      this.state = state;
      this.requiresBlockBelow = requiresBlockBelow;
      this.rockCount = rockCount;
      this.holeCount = holeCount;
      this.validBlocks = validBlocks;
   }

   public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
      Object var10004 = ops.createString("state");
      Object var10005 = FluidState.serialize(ops, this.state).getValue();
      Object var10006 = ops.createString("requires_block_below");
      Object var10007 = ops.createBoolean(this.requiresBlockBelow);
      Object var10008 = ops.createString("rock_count");
      Object var10009 = ops.createInt(this.rockCount);
      Object var10010 = ops.createString("hole_count");
      Object var10011 = ops.createInt(this.holeCount);
      Object var10012 = ops.createString("valid_blocks");
      Stream var10014 = this.validBlocks.stream();
      DefaultedRegistry var10015 = Registry.BLOCK;
      var10015.getClass();
      var10014 = var10014.map(var10015::getId).map(Identifier::toString);
      ops.getClass();
      return new Dynamic(ops, ops.createMap(ImmutableMap.of(var10004, var10005, var10006, var10007, var10008, var10009, var10010, var10011, var10012, ops.createList(var10014.map(ops::createString)))));
   }

   public static <T> SpringFeatureConfig deserialize(Dynamic<T> dynamic) {
      return new SpringFeatureConfig((FluidState)dynamic.get("state").map(FluidState::deserialize).orElse(Fluids.EMPTY.getDefaultState()), dynamic.get("requires_block_below").asBoolean(true), dynamic.get("rock_count").asInt(4), dynamic.get("hole_count").asInt(1), ImmutableSet.copyOf(dynamic.get("valid_blocks").asList((dynamicx) -> {
         return (Block)Registry.BLOCK.get(new Identifier(dynamicx.asString("minecraft:air")));
      })));
   }
}
