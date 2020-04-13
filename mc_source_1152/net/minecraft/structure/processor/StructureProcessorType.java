package net.minecraft.structure.processor;

import net.minecraft.util.DynamicDeserializer;
import net.minecraft.util.registry.Registry;

public interface StructureProcessorType extends DynamicDeserializer<StructureProcessor> {
   StructureProcessorType BLOCK_IGNORE = register("block_ignore", BlockIgnoreStructureProcessor::new);
   StructureProcessorType BLOCK_ROT = register("block_rot", BlockRotStructureProcessor::new);
   StructureProcessorType GRAVITY = register("gravity", GravityStructureProcessor::new);
   StructureProcessorType JIGSAW_REPLACEMENT = register("jigsaw_replacement", (dynamic) -> {
      return JigsawReplacementStructureProcessor.INSTANCE;
   });
   StructureProcessorType RULE = register("rule", RuleStructureProcessor::new);
   StructureProcessorType NOP = register("nop", (dynamic) -> {
      return NopStructureProcessor.INSTANCE;
   });

   static StructureProcessorType register(String id, StructureProcessorType processor) {
      return (StructureProcessorType)Registry.register(Registry.STRUCTURE_PROCESSOR, (String)id, processor);
   }
}
