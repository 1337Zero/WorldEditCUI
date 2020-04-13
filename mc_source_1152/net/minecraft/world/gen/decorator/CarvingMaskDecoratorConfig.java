package net.minecraft.world.gen.decorator;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.gen.GenerationStep;

public class CarvingMaskDecoratorConfig implements DecoratorConfig {
   protected final GenerationStep.Carver step;
   protected final float probability;

   public CarvingMaskDecoratorConfig(GenerationStep.Carver step, float probability) {
      this.step = step;
      this.probability = probability;
   }

   public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
      return new Dynamic(ops, ops.createMap(ImmutableMap.of(ops.createString("step"), ops.createString(this.step.toString()), ops.createString("probability"), ops.createFloat(this.probability))));
   }

   public static CarvingMaskDecoratorConfig deserialize(Dynamic<?> dynamic) {
      GenerationStep.Carver carver = GenerationStep.Carver.valueOf(dynamic.get("step").asString(""));
      float f = dynamic.get("probability").asFloat(0.0F);
      return new CarvingMaskDecoratorConfig(carver, f);
   }
}
