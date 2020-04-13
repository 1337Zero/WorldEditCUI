package net.minecraft.world.gen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;

public class EndSpikeFeatureConfig implements FeatureConfig {
   private final boolean crystalInvulnerable;
   private final List<EndSpikeFeature.Spike> spikes;
   @Nullable
   private final BlockPos crystalBeamTarget;

   public EndSpikeFeatureConfig(boolean crystalInvulnerable, List<EndSpikeFeature.Spike> spikes, @Nullable BlockPos crystalBeamTarget) {
      this.crystalInvulnerable = crystalInvulnerable;
      this.spikes = spikes;
      this.crystalBeamTarget = crystalBeamTarget;
   }

   public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
      Dynamic var10000 = new Dynamic;
      Object var10004 = ops.createString("crystalInvulnerable");
      Object var10005 = ops.createBoolean(this.crystalInvulnerable);
      Object var10006 = ops.createString("spikes");
      Object var10007 = ops.createList(this.spikes.stream().map((spike) -> {
         return spike.serialize(ops).getValue();
      }));
      Object var10008 = ops.createString("crystalBeamTarget");
      Object var10009;
      if (this.crystalBeamTarget == null) {
         var10009 = ops.createList(Stream.empty());
      } else {
         IntStream var10010 = IntStream.of(new int[]{this.crystalBeamTarget.getX(), this.crystalBeamTarget.getY(), this.crystalBeamTarget.getZ()});
         ops.getClass();
         var10009 = ops.createList(var10010.mapToObj(ops::createInt));
      }

      var10000.<init>(ops, ops.createMap(ImmutableMap.of(var10004, var10005, var10006, var10007, var10008, var10009)));
      return var10000;
   }

   public static <T> EndSpikeFeatureConfig deserialize(Dynamic<T> dynamic) {
      List<EndSpikeFeature.Spike> list = dynamic.get("spikes").asList(EndSpikeFeature.Spike::deserialize);
      List<Integer> list2 = dynamic.get("crystalBeamTarget").asList((dynamicx) -> {
         return dynamicx.asInt(0);
      });
      BlockPos blockPos2;
      if (list2.size() == 3) {
         blockPos2 = new BlockPos((Integer)list2.get(0), (Integer)list2.get(1), (Integer)list2.get(2));
      } else {
         blockPos2 = null;
      }

      return new EndSpikeFeatureConfig(dynamic.get("crystalInvulnerable").asBoolean(false), list, blockPos2);
   }

   public boolean isCrystalInvulerable() {
      return this.crystalInvulnerable;
   }

   public List<EndSpikeFeature.Spike> getSpikes() {
      return this.spikes;
   }

   @Nullable
   public BlockPos getPos() {
      return this.crystalBeamTarget;
   }
}
