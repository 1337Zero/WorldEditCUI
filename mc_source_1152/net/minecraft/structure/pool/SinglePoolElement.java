package net.minecraft.structure.pool;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.structure.processor.JigsawReplacementStructureProcessor;
import net.minecraft.structure.processor.NopStructureProcessor;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.DynamicDeserializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class SinglePoolElement extends StructurePoolElement {
   protected final Identifier location;
   protected final ImmutableList<StructureProcessor> processors;

   @Deprecated
   public SinglePoolElement(String location, List<StructureProcessor> processors) {
      this(location, processors, StructurePool.Projection.RIGID);
   }

   public SinglePoolElement(String string, List<StructureProcessor> list, StructurePool.Projection projection) {
      super(projection);
      this.location = new Identifier(string);
      this.processors = ImmutableList.copyOf(list);
   }

   @Deprecated
   public SinglePoolElement(String string) {
      this(string, ImmutableList.of());
   }

   public SinglePoolElement(Dynamic<?> dynamic) {
      super(dynamic);
      this.location = new Identifier(dynamic.get("location").asString(""));
      this.processors = ImmutableList.copyOf(dynamic.get("processors").asList((dynamicx) -> {
         return (StructureProcessor)DynamicDeserializer.deserialize(dynamicx, Registry.STRUCTURE_PROCESSOR, "processor_type", NopStructureProcessor.INSTANCE);
      }));
   }

   public List<Structure.StructureBlockInfo> method_16614(StructureManager structureManager, BlockPos blockPos, BlockRotation blockRotation, boolean bl) {
      Structure structure = structureManager.getStructureOrBlank(this.location);
      List<Structure.StructureBlockInfo> list = structure.method_15165(blockPos, (new StructurePlacementData()).setRotation(blockRotation), Blocks.STRUCTURE_BLOCK, bl);
      List<Structure.StructureBlockInfo> list2 = Lists.newArrayList();
      Iterator var8 = list.iterator();

      while(var8.hasNext()) {
         Structure.StructureBlockInfo structureBlockInfo = (Structure.StructureBlockInfo)var8.next();
         if (structureBlockInfo.tag != null) {
            StructureBlockMode structureBlockMode = StructureBlockMode.valueOf(structureBlockInfo.tag.getString("mode"));
            if (structureBlockMode == StructureBlockMode.DATA) {
               list2.add(structureBlockInfo);
            }
         }
      }

      return list2;
   }

   public List<Structure.StructureBlockInfo> getStructureBlockInfos(StructureManager structureManager, BlockPos pos, BlockRotation rotation, Random random) {
      Structure structure = structureManager.getStructureOrBlank(this.location);
      List<Structure.StructureBlockInfo> list = structure.method_15165(pos, (new StructurePlacementData()).setRotation(rotation), Blocks.JIGSAW, true);
      Collections.shuffle(list, random);
      return list;
   }

   public BlockBox getBoundingBox(StructureManager structureManager, BlockPos pos, BlockRotation rotation) {
      Structure structure = structureManager.getStructureOrBlank(this.location);
      return structure.calculateBoundingBox((new StructurePlacementData()).setRotation(rotation), pos);
   }

   public boolean generate(StructureManager structureManager, IWorld world, ChunkGenerator<?> chunkGenerator, BlockPos blockPos, BlockRotation blockRotation, BlockBox blockBox, Random random) {
      Structure structure = structureManager.getStructureOrBlank(this.location);
      StructurePlacementData structurePlacementData = this.method_16616(blockRotation, blockBox);
      if (!structure.method_15172(world, blockPos, structurePlacementData, 18)) {
         return false;
      } else {
         List<Structure.StructureBlockInfo> list = Structure.process(world, blockPos, structurePlacementData, this.method_16614(structureManager, blockPos, blockRotation, false));
         Iterator var11 = list.iterator();

         while(var11.hasNext()) {
            Structure.StructureBlockInfo structureBlockInfo = (Structure.StructureBlockInfo)var11.next();
            this.method_16756(world, structureBlockInfo, blockPos, blockRotation, random, blockBox);
         }

         return true;
      }
   }

   protected StructurePlacementData method_16616(BlockRotation blockRotation, BlockBox blockBox) {
      StructurePlacementData structurePlacementData = new StructurePlacementData();
      structurePlacementData.setBoundingBox(blockBox);
      structurePlacementData.setRotation(blockRotation);
      structurePlacementData.method_15131(true);
      structurePlacementData.setIgnoreEntities(false);
      structurePlacementData.addProcessor(BlockIgnoreStructureProcessor.IGNORE_AIR_AND_STRUCTURE_BLOCKS);
      structurePlacementData.addProcessor(JigsawReplacementStructureProcessor.INSTANCE);
      this.processors.forEach(structurePlacementData::addProcessor);
      this.getProjection().getProcessors().forEach(structurePlacementData::addProcessor);
      return structurePlacementData;
   }

   public StructurePoolElementType getType() {
      return StructurePoolElementType.SINGLE_POOL_ELEMENT;
   }

   public <T> Dynamic<T> method_16625(DynamicOps<T> dynamicOps) {
      return new Dynamic(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("location"), dynamicOps.createString(this.location.toString()), dynamicOps.createString("processors"), dynamicOps.createList(this.processors.stream().map((structureProcessor) -> {
         return structureProcessor.method_16771(dynamicOps).getValue();
      })))));
   }

   public String toString() {
      return "Single[" + this.location + "]";
   }
}
