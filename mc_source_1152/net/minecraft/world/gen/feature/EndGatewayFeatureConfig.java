package net.minecraft.world.gen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Optional;
import net.minecraft.util.math.BlockPos;

public class EndGatewayFeatureConfig implements FeatureConfig {
   private final Optional<BlockPos> exitPos;
   private final boolean exact;

   private EndGatewayFeatureConfig(Optional<BlockPos> exitPos, boolean exact) {
      this.exitPos = exitPos;
      this.exact = exact;
   }

   public static EndGatewayFeatureConfig createConfig(BlockPos exitPortalPosition, boolean exitsAtSpawn) {
      return new EndGatewayFeatureConfig(Optional.of(exitPortalPosition), exitsAtSpawn);
   }

   public static EndGatewayFeatureConfig createConfig() {
      return new EndGatewayFeatureConfig(Optional.empty(), false);
   }

   public Optional<BlockPos> getExitPos() {
      return this.exitPos;
   }

   public boolean isExact() {
      return this.exact;
   }

   public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
      return new Dynamic(ops, this.exitPos.map((blockPos) -> {
         return ops.createMap(ImmutableMap.of(ops.createString("exit_x"), ops.createInt(blockPos.getX()), ops.createString("exit_y"), ops.createInt(blockPos.getY()), ops.createString("exit_z"), ops.createInt(blockPos.getZ()), ops.createString("exact"), ops.createBoolean(this.exact)));
      }).orElse(ops.emptyMap()));
   }

   public static <T> EndGatewayFeatureConfig deserialize(Dynamic<T> dynamic) {
      Optional<BlockPos> optional = dynamic.get("exit_x").asNumber().flatMap((number) -> {
         return dynamic.get("exit_y").asNumber().flatMap((number2) -> {
            return dynamic.get("exit_z").asNumber().map((number3) -> {
               return new BlockPos(number.intValue(), number2.intValue(), number3.intValue());
            });
         });
      });
      boolean bl = dynamic.get("exact").asBoolean(false);
      return new EndGatewayFeatureConfig(optional, bl);
   }
}
