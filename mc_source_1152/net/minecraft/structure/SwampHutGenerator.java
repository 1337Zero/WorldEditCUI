package net.minecraft.structure;

import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.StairShape;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class SwampHutGenerator extends StructurePieceWithDimensions {
   private boolean hasWitch;
   private boolean hasCat;

   public SwampHutGenerator(Random random, int i, int j) {
      super(StructurePieceType.SWAMP_HUT, random, i, 64, j, 7, 7, 9);
   }

   public SwampHutGenerator(StructureManager structureManager, CompoundTag compoundTag) {
      super(StructurePieceType.SWAMP_HUT, compoundTag);
      this.hasWitch = compoundTag.getBoolean("Witch");
      this.hasCat = compoundTag.getBoolean("Cat");
   }

   protected void toNbt(CompoundTag tag) {
      super.toNbt(tag);
      tag.putBoolean("Witch", this.hasWitch);
      tag.putBoolean("Cat", this.hasCat);
   }

   public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
      if (!this.method_14839(world, blockBox, 0)) {
         return false;
      } else {
         this.fillWithOutline(world, blockBox, 1, 1, 1, 5, 1, 7, Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 4, 2, 5, 4, 7, Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 2, 1, 0, 4, 1, 0, Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 2, 2, 2, 3, 3, 2, Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 2, 3, 1, 3, 6, Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 5, 2, 3, 5, 3, 6, Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 2, 2, 7, 4, 3, 7, Blocks.SPRUCE_PLANKS.getDefaultState(), Blocks.SPRUCE_PLANKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 0, 2, 1, 3, 2, Blocks.OAK_LOG.getDefaultState(), Blocks.OAK_LOG.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 5, 0, 2, 5, 3, 2, Blocks.OAK_LOG.getDefaultState(), Blocks.OAK_LOG.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 0, 7, 1, 3, 7, Blocks.OAK_LOG.getDefaultState(), Blocks.OAK_LOG.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 5, 0, 7, 5, 3, 7, Blocks.OAK_LOG.getDefaultState(), Blocks.OAK_LOG.getDefaultState(), false);
         this.addBlock(world, Blocks.OAK_FENCE.getDefaultState(), 2, 3, 2, blockBox);
         this.addBlock(world, Blocks.OAK_FENCE.getDefaultState(), 3, 3, 7, blockBox);
         this.addBlock(world, Blocks.AIR.getDefaultState(), 1, 3, 4, blockBox);
         this.addBlock(world, Blocks.AIR.getDefaultState(), 5, 3, 4, blockBox);
         this.addBlock(world, Blocks.AIR.getDefaultState(), 5, 3, 5, blockBox);
         this.addBlock(world, Blocks.POTTED_RED_MUSHROOM.getDefaultState(), 1, 3, 5, blockBox);
         this.addBlock(world, Blocks.CRAFTING_TABLE.getDefaultState(), 3, 2, 6, blockBox);
         this.addBlock(world, Blocks.CAULDRON.getDefaultState(), 4, 2, 6, blockBox);
         this.addBlock(world, Blocks.OAK_FENCE.getDefaultState(), 1, 2, 1, blockBox);
         this.addBlock(world, Blocks.OAK_FENCE.getDefaultState(), 5, 2, 1, blockBox);
         BlockState blockState = (BlockState)Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH);
         BlockState blockState2 = (BlockState)Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST);
         BlockState blockState3 = (BlockState)Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST);
         BlockState blockState4 = (BlockState)Blocks.SPRUCE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH);
         this.fillWithOutline(world, blockBox, 0, 4, 1, 6, 4, 1, blockState, blockState, false);
         this.fillWithOutline(world, blockBox, 0, 4, 2, 0, 4, 7, blockState2, blockState2, false);
         this.fillWithOutline(world, blockBox, 6, 4, 2, 6, 4, 7, blockState3, blockState3, false);
         this.fillWithOutline(world, blockBox, 0, 4, 8, 6, 4, 8, blockState4, blockState4, false);
         this.addBlock(world, (BlockState)blockState.with(StairsBlock.SHAPE, StairShape.OUTER_RIGHT), 0, 4, 1, blockBox);
         this.addBlock(world, (BlockState)blockState.with(StairsBlock.SHAPE, StairShape.OUTER_LEFT), 6, 4, 1, blockBox);
         this.addBlock(world, (BlockState)blockState4.with(StairsBlock.SHAPE, StairShape.OUTER_LEFT), 0, 4, 8, blockBox);
         this.addBlock(world, (BlockState)blockState4.with(StairsBlock.SHAPE, StairShape.OUTER_RIGHT), 6, 4, 8, blockBox);

         int k;
         int l;
         for(k = 2; k <= 7; k += 5) {
            for(l = 1; l <= 5; l += 4) {
               this.method_14936(world, Blocks.OAK_LOG.getDefaultState(), l, -1, k, blockBox);
            }
         }

         if (!this.hasWitch) {
            k = this.applyXTransform(2, 5);
            l = this.applyYTransform(2);
            int m = this.applyZTransform(2, 5);
            if (blockBox.contains(new BlockPos(k, l, m))) {
               this.hasWitch = true;
               WitchEntity witchEntity = (WitchEntity)EntityType.WITCH.create(world.getWorld());
               witchEntity.setPersistent();
               witchEntity.refreshPositionAndAngles((double)k + 0.5D, (double)l, (double)m + 0.5D, 0.0F, 0.0F);
               witchEntity.initialize(world, world.getLocalDifficulty(new BlockPos(k, l, m)), SpawnType.STRUCTURE, (EntityData)null, (CompoundTag)null);
               world.spawnEntity(witchEntity);
            }
         }

         this.method_16181(world, blockBox);
         return true;
      }
   }

   private void method_16181(IWorld iWorld, BlockBox blockBox) {
      if (!this.hasCat) {
         int i = this.applyXTransform(2, 5);
         int j = this.applyYTransform(2);
         int k = this.applyZTransform(2, 5);
         if (blockBox.contains(new BlockPos(i, j, k))) {
            this.hasCat = true;
            CatEntity catEntity = (CatEntity)EntityType.CAT.create(iWorld.getWorld());
            catEntity.setPersistent();
            catEntity.refreshPositionAndAngles((double)i + 0.5D, (double)j, (double)k + 0.5D, 0.0F, 0.0F);
            catEntity.initialize(iWorld, iWorld.getLocalDifficulty(new BlockPos(i, j, k)), SpawnType.STRUCTURE, (EntityData)null, (CompoundTag)null);
            iWorld.spawnEntity(catEntity);
         }
      }

   }
}
