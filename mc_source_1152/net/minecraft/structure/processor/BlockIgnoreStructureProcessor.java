package net.minecraft.structure.processor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class BlockIgnoreStructureProcessor extends StructureProcessor {
   public static final BlockIgnoreStructureProcessor IGNORE_STRUCTURE_BLOCKS;
   public static final BlockIgnoreStructureProcessor IGNORE_AIR;
   public static final BlockIgnoreStructureProcessor IGNORE_AIR_AND_STRUCTURE_BLOCKS;
   private final ImmutableList<Block> blocks;

   public BlockIgnoreStructureProcessor(List<Block> list) {
      this.blocks = ImmutableList.copyOf(list);
   }

   public BlockIgnoreStructureProcessor(Dynamic<?> dynamic) {
      this(dynamic.get("blocks").asList((dynamicx) -> {
         return BlockState.deserialize(dynamicx).getBlock();
      }));
   }

   @Nullable
   public Structure.StructureBlockInfo process(WorldView worldView, BlockPos pos, Structure.StructureBlockInfo structureBlockInfo, Structure.StructureBlockInfo structureBlockInfo2, StructurePlacementData placementData) {
      return this.blocks.contains(structureBlockInfo2.state.getBlock()) ? null : structureBlockInfo2;
   }

   protected StructureProcessorType getType() {
      return StructureProcessorType.BLOCK_IGNORE;
   }

   protected <T> Dynamic<T> method_16666(DynamicOps<T> dynamicOps) {
      return new Dynamic(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("blocks"), dynamicOps.createList(this.blocks.stream().map((block) -> {
         return BlockState.serialize(dynamicOps, block.getDefaultState()).getValue();
      })))));
   }

   static {
      IGNORE_STRUCTURE_BLOCKS = new BlockIgnoreStructureProcessor(ImmutableList.of(Blocks.STRUCTURE_BLOCK));
      IGNORE_AIR = new BlockIgnoreStructureProcessor(ImmutableList.of(Blocks.AIR));
      IGNORE_AIR_AND_STRUCTURE_BLOCKS = new BlockIgnoreStructureProcessor(ImmutableList.of(Blocks.AIR, Blocks.STRUCTURE_BLOCK));
   }
}
