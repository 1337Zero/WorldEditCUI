package net.minecraft.structure;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluids;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class NetherFortressGenerator {
   private static final NetherFortressGenerator.class_3404[] field_14494 = new NetherFortressGenerator.class_3404[]{new NetherFortressGenerator.class_3404(NetherFortressGenerator.Bridge.class, 30, 0, true), new NetherFortressGenerator.class_3404(NetherFortressGenerator.BridgeCrossing.class, 10, 4), new NetherFortressGenerator.class_3404(NetherFortressGenerator.BridgeSmallCrossing.class, 10, 4), new NetherFortressGenerator.class_3404(NetherFortressGenerator.BridgeStairs.class, 10, 3), new NetherFortressGenerator.class_3404(NetherFortressGenerator.BridgePlatform.class, 5, 2), new NetherFortressGenerator.class_3404(NetherFortressGenerator.CorridorExit.class, 5, 1)};
   private static final NetherFortressGenerator.class_3404[] field_14493 = new NetherFortressGenerator.class_3404[]{new NetherFortressGenerator.class_3404(NetherFortressGenerator.SmallCorridor.class, 25, 0, true), new NetherFortressGenerator.class_3404(NetherFortressGenerator.CorridorCrossing.class, 15, 5), new NetherFortressGenerator.class_3404(NetherFortressGenerator.CorridorRightTurn.class, 5, 10), new NetherFortressGenerator.class_3404(NetherFortressGenerator.CorridorLeftTurn.class, 5, 10), new NetherFortressGenerator.class_3404(NetherFortressGenerator.CorridorStairs.class, 10, 3, true), new NetherFortressGenerator.class_3404(NetherFortressGenerator.CorridorBalcony.class, 7, 2), new NetherFortressGenerator.class_3404(NetherFortressGenerator.CorridorNetherWartsRoom.class, 5, 2)};

   private static NetherFortressGenerator.Piece generatePiece(NetherFortressGenerator.class_3404 arg, List<StructurePiece> list, Random random, int i, int j, int k, Direction direction, int l) {
      Class<? extends NetherFortressGenerator.Piece> var8 = arg.field_14501;
      NetherFortressGenerator.Piece piece = null;
      if (var8 == NetherFortressGenerator.Bridge.class) {
         piece = NetherFortressGenerator.Bridge.method_14798(list, random, i, j, k, direction, l);
      } else if (var8 == NetherFortressGenerator.BridgeCrossing.class) {
         piece = NetherFortressGenerator.BridgeCrossing.method_14796(list, i, j, k, direction, l);
      } else if (var8 == NetherFortressGenerator.BridgeSmallCrossing.class) {
         piece = NetherFortressGenerator.BridgeSmallCrossing.method_14817(list, i, j, k, direction, l);
      } else if (var8 == NetherFortressGenerator.BridgeStairs.class) {
         piece = NetherFortressGenerator.BridgeStairs.method_14818(list, i, j, k, l, direction);
      } else if (var8 == NetherFortressGenerator.BridgePlatform.class) {
         piece = NetherFortressGenerator.BridgePlatform.method_14807(list, i, j, k, l, direction);
      } else if (var8 == NetherFortressGenerator.CorridorExit.class) {
         piece = NetherFortressGenerator.CorridorExit.method_14801(list, random, i, j, k, direction, l);
      } else if (var8 == NetherFortressGenerator.SmallCorridor.class) {
         piece = NetherFortressGenerator.SmallCorridor.method_14804(list, i, j, k, direction, l);
      } else if (var8 == NetherFortressGenerator.CorridorRightTurn.class) {
         piece = NetherFortressGenerator.CorridorRightTurn.method_14805(list, random, i, j, k, direction, l);
      } else if (var8 == NetherFortressGenerator.CorridorLeftTurn.class) {
         piece = NetherFortressGenerator.CorridorLeftTurn.method_14803(list, random, i, j, k, direction, l);
      } else if (var8 == NetherFortressGenerator.CorridorStairs.class) {
         piece = NetherFortressGenerator.CorridorStairs.method_14799(list, i, j, k, direction, l);
      } else if (var8 == NetherFortressGenerator.CorridorBalcony.class) {
         piece = NetherFortressGenerator.CorridorBalcony.method_14800(list, i, j, k, direction, l);
      } else if (var8 == NetherFortressGenerator.CorridorCrossing.class) {
         piece = NetherFortressGenerator.CorridorCrossing.method_14802(list, i, j, k, direction, l);
      } else if (var8 == NetherFortressGenerator.CorridorNetherWartsRoom.class) {
         piece = NetherFortressGenerator.CorridorNetherWartsRoom.method_14806(list, i, j, k, direction, l);
      }

      return (NetherFortressGenerator.Piece)piece;
   }

   public static class CorridorBalcony extends NetherFortressGenerator.Piece {
      public CorridorBalcony(int i, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_BALCONY, i);
         this.setOrientation(direction);
         this.boundingBox = blockBox;
      }

      public CorridorBalcony(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_BALCONY, compoundTag);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         int i = 1;
         Direction direction = this.getFacing();
         if (direction == Direction.WEST || direction == Direction.NORTH) {
            i = 5;
         }

         this.method_14812((NetherFortressGenerator.Start)structurePiece, list, random, 0, i, random.nextInt(8) > 0);
         this.method_14808((NetherFortressGenerator.Start)structurePiece, list, random, 0, i, random.nextInt(8) > 0);
      }

      public static NetherFortressGenerator.CorridorBalcony method_14800(List<StructurePiece> list, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -3, 0, 0, 9, 7, 9, direction);
         return method_14809(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new NetherFortressGenerator.CorridorBalcony(l, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
         this.fillWithOutline(world, blockBox, 0, 0, 0, 8, 1, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 2, 0, 8, 5, 8, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 6, 0, 8, 6, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 2, 0, 2, 5, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 6, 2, 0, 8, 5, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 3, 0, 1, 4, 0, blockState2, blockState2, false);
         this.fillWithOutline(world, blockBox, 7, 3, 0, 7, 4, 0, blockState2, blockState2, false);
         this.fillWithOutline(world, blockBox, 0, 2, 4, 8, 2, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 1, 4, 2, 2, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 6, 1, 4, 7, 2, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 3, 8, 7, 3, 8, blockState2, blockState2, false);
         this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.EAST, true)).with(FenceBlock.SOUTH, true), 0, 3, 8, blockBox);
         this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.SOUTH, true), 8, 3, 8, blockBox);
         this.fillWithOutline(world, blockBox, 0, 3, 6, 0, 3, 7, blockState, blockState, false);
         this.fillWithOutline(world, blockBox, 8, 3, 6, 8, 3, 7, blockState, blockState, false);
         this.fillWithOutline(world, blockBox, 0, 3, 4, 0, 5, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 8, 3, 4, 8, 5, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 3, 5, 2, 5, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 6, 3, 5, 7, 5, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 4, 5, 1, 5, 5, blockState2, blockState2, false);
         this.fillWithOutline(world, blockBox, 7, 4, 5, 7, 5, 5, blockState2, blockState2, false);

         for(int i = 0; i <= 5; ++i) {
            for(int j = 0; j <= 8; ++j) {
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), j, -1, i, blockBox);
            }
         }

         return true;
      }
   }

   public static class CorridorStairs extends NetherFortressGenerator.Piece {
      public CorridorStairs(int i, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_STAIRS, i);
         this.setOrientation(direction);
         this.boundingBox = blockBox;
      }

      public CorridorStairs(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_STAIRS, compoundTag);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         this.method_14814((NetherFortressGenerator.Start)structurePiece, list, random, 1, 0, true);
      }

      public static NetherFortressGenerator.CorridorStairs method_14799(List<StructurePiece> list, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -1, -7, 0, 5, 14, 10, direction);
         return method_14809(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new NetherFortressGenerator.CorridorStairs(l, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         BlockState blockState = (BlockState)Blocks.NETHER_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);

         for(int i = 0; i <= 9; ++i) {
            int j = Math.max(1, 7 - i);
            int k = Math.min(Math.max(j + 5, 14 - i), 13);
            int l = i;
            this.fillWithOutline(world, blockBox, 0, 0, i, 4, j, i, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, blockBox, 1, j + 1, i, 3, k - 1, i, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            if (i <= 6) {
               this.addBlock(world, blockState, 1, j + 1, i, blockBox);
               this.addBlock(world, blockState, 2, j + 1, i, blockBox);
               this.addBlock(world, blockState, 3, j + 1, i, blockBox);
            }

            this.fillWithOutline(world, blockBox, 0, k, i, 4, k, i, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, blockBox, 0, j + 1, i, 0, k - 1, i, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            this.fillWithOutline(world, blockBox, 4, j + 1, i, 4, k - 1, i, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            if ((i & 1) == 0) {
               this.fillWithOutline(world, blockBox, 0, j + 2, i, 0, j + 3, i, blockState2, blockState2, false);
               this.fillWithOutline(world, blockBox, 4, j + 2, i, 4, j + 3, i, blockState2, blockState2, false);
            }

            for(int m = 0; m <= 4; ++m) {
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), m, -1, l, blockBox);
            }
         }

         return true;
      }
   }

   public static class CorridorLeftTurn extends NetherFortressGenerator.Piece {
      private boolean containsChest;

      public CorridorLeftTurn(int i, Random random, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_LEFT_TURN, i);
         this.setOrientation(direction);
         this.boundingBox = blockBox;
         this.containsChest = random.nextInt(3) == 0;
      }

      public CorridorLeftTurn(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_LEFT_TURN, compoundTag);
         this.containsChest = compoundTag.getBoolean("Chest");
      }

      protected void toNbt(CompoundTag tag) {
         super.toNbt(tag);
         tag.putBoolean("Chest", this.containsChest);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         this.method_14812((NetherFortressGenerator.Start)structurePiece, list, random, 0, 1, true);
      }

      public static NetherFortressGenerator.CorridorLeftTurn method_14803(List<StructurePiece> list, Random random, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -1, 0, 0, 5, 7, 5, direction);
         return method_14809(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new NetherFortressGenerator.CorridorLeftTurn(l, random, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         this.fillWithOutline(world, blockBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
         this.fillWithOutline(world, blockBox, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 4, 3, 1, 4, 4, 1, blockState2, blockState2, false);
         this.fillWithOutline(world, blockBox, 4, 3, 3, 4, 4, 3, blockState2, blockState2, false);
         this.fillWithOutline(world, blockBox, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 2, 4, 3, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 3, 4, 1, 4, 4, blockState, blockState, false);
         this.fillWithOutline(world, blockBox, 3, 3, 4, 3, 4, 4, blockState, blockState, false);
         if (this.containsChest && blockBox.contains(new BlockPos(this.applyXTransform(3, 3), this.applyYTransform(2), this.applyZTransform(3, 3)))) {
            this.containsChest = false;
            this.addChest(world, blockBox, random, 3, 2, 3, LootTables.NETHER_BRIDGE_CHEST);
         }

         this.fillWithOutline(world, blockBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 0; j <= 4; ++j) {
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, blockBox);
            }
         }

         return true;
      }
   }

   public static class CorridorRightTurn extends NetherFortressGenerator.Piece {
      private boolean containsChest;

      public CorridorRightTurn(int i, Random random, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_RIGHT_TURN, i);
         this.setOrientation(direction);
         this.boundingBox = blockBox;
         this.containsChest = random.nextInt(3) == 0;
      }

      public CorridorRightTurn(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_RIGHT_TURN, compoundTag);
         this.containsChest = compoundTag.getBoolean("Chest");
      }

      protected void toNbt(CompoundTag tag) {
         super.toNbt(tag);
         tag.putBoolean("Chest", this.containsChest);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         this.method_14808((NetherFortressGenerator.Start)structurePiece, list, random, 0, 1, true);
      }

      public static NetherFortressGenerator.CorridorRightTurn method_14805(List<StructurePiece> list, Random random, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -1, 0, 0, 5, 7, 5, direction);
         return method_14809(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new NetherFortressGenerator.CorridorRightTurn(l, random, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         this.fillWithOutline(world, blockBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
         this.fillWithOutline(world, blockBox, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 3, 1, 0, 4, 1, blockState2, blockState2, false);
         this.fillWithOutline(world, blockBox, 0, 3, 3, 0, 4, 3, blockState2, blockState2, false);
         this.fillWithOutline(world, blockBox, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 2, 4, 4, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 3, 4, 1, 4, 4, blockState, blockState, false);
         this.fillWithOutline(world, blockBox, 3, 3, 4, 3, 4, 4, blockState, blockState, false);
         if (this.containsChest && blockBox.contains(new BlockPos(this.applyXTransform(1, 3), this.applyYTransform(2), this.applyZTransform(1, 3)))) {
            this.containsChest = false;
            this.addChest(world, blockBox, random, 1, 2, 3, LootTables.NETHER_BRIDGE_CHEST);
         }

         this.fillWithOutline(world, blockBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 0; j <= 4; ++j) {
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, blockBox);
            }
         }

         return true;
      }
   }

   public static class CorridorCrossing extends NetherFortressGenerator.Piece {
      public CorridorCrossing(int i, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_CROSSING, i);
         this.setOrientation(direction);
         this.boundingBox = blockBox;
      }

      public CorridorCrossing(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_CROSSING, compoundTag);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         this.method_14814((NetherFortressGenerator.Start)structurePiece, list, random, 1, 0, true);
         this.method_14812((NetherFortressGenerator.Start)structurePiece, list, random, 0, 1, true);
         this.method_14808((NetherFortressGenerator.Start)structurePiece, list, random, 0, 1, true);
      }

      public static NetherFortressGenerator.CorridorCrossing method_14802(List<StructurePiece> list, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -1, 0, 0, 5, 7, 5, direction);
         return method_14809(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new NetherFortressGenerator.CorridorCrossing(l, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         this.fillWithOutline(world, blockBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 2, 4, 0, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 4, 2, 4, 4, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 0; j <= 4; ++j) {
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, blockBox);
            }
         }

         return true;
      }
   }

   public static class SmallCorridor extends NetherFortressGenerator.Piece {
      public SmallCorridor(int i, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_SMALL_CORRIDOR, i);
         this.setOrientation(direction);
         this.boundingBox = blockBox;
      }

      public SmallCorridor(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_SMALL_CORRIDOR, compoundTag);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         this.method_14814((NetherFortressGenerator.Start)structurePiece, list, random, 1, 0, true);
      }

      public static NetherFortressGenerator.SmallCorridor method_14804(List<StructurePiece> list, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -1, 0, 0, 5, 7, 5, direction);
         return method_14809(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new NetherFortressGenerator.SmallCorridor(l, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         this.fillWithOutline(world, blockBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
         this.fillWithOutline(world, blockBox, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 3, 1, 0, 4, 1, blockState, blockState, false);
         this.fillWithOutline(world, blockBox, 0, 3, 3, 0, 4, 3, blockState, blockState, false);
         this.fillWithOutline(world, blockBox, 4, 3, 1, 4, 4, 1, blockState, blockState, false);
         this.fillWithOutline(world, blockBox, 4, 3, 3, 4, 4, 3, blockState, blockState, false);
         this.fillWithOutline(world, blockBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 0; j <= 4; ++j) {
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, blockBox);
            }
         }

         return true;
      }
   }

   public static class CorridorNetherWartsRoom extends NetherFortressGenerator.Piece {
      public CorridorNetherWartsRoom(int i, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_NETHER_WARTS_ROOM, i);
         this.setOrientation(direction);
         this.boundingBox = blockBox;
      }

      public CorridorNetherWartsRoom(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_NETHER_WARTS_ROOM, compoundTag);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         this.method_14814((NetherFortressGenerator.Start)structurePiece, list, random, 5, 3, true);
         this.method_14814((NetherFortressGenerator.Start)structurePiece, list, random, 5, 11, true);
      }

      public static NetherFortressGenerator.CorridorNetherWartsRoom method_14806(List<StructurePiece> list, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -5, -3, 0, 13, 14, 13, direction);
         return method_14809(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new NetherFortressGenerator.CorridorNetherWartsRoom(l, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         this.fillWithOutline(world, blockBox, 0, 3, 0, 12, 4, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 5, 0, 12, 13, 12, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 5, 0, 1, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 11, 5, 0, 12, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 2, 5, 11, 4, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 8, 5, 11, 10, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 5, 9, 11, 7, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 2, 5, 0, 4, 12, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 8, 5, 0, 10, 12, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 5, 9, 0, 7, 12, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 2, 11, 2, 10, 12, 10, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
         BlockState blockState3 = (BlockState)blockState2.with(FenceBlock.WEST, true);
         BlockState blockState4 = (BlockState)blockState2.with(FenceBlock.EAST, true);

         int j;
         for(j = 1; j <= 11; j += 2) {
            this.fillWithOutline(world, blockBox, j, 10, 0, j, 11, 0, blockState, blockState, false);
            this.fillWithOutline(world, blockBox, j, 10, 12, j, 11, 12, blockState, blockState, false);
            this.fillWithOutline(world, blockBox, 0, 10, j, 0, 11, j, blockState2, blockState2, false);
            this.fillWithOutline(world, blockBox, 12, 10, j, 12, 11, j, blockState2, blockState2, false);
            this.addBlock(world, Blocks.NETHER_BRICKS.getDefaultState(), j, 13, 0, blockBox);
            this.addBlock(world, Blocks.NETHER_BRICKS.getDefaultState(), j, 13, 12, blockBox);
            this.addBlock(world, Blocks.NETHER_BRICKS.getDefaultState(), 0, 13, j, blockBox);
            this.addBlock(world, Blocks.NETHER_BRICKS.getDefaultState(), 12, 13, j, blockBox);
            if (j != 11) {
               this.addBlock(world, blockState, j + 1, 13, 0, blockBox);
               this.addBlock(world, blockState, j + 1, 13, 12, blockBox);
               this.addBlock(world, blockState2, 0, 13, j + 1, blockBox);
               this.addBlock(world, blockState2, 12, 13, j + 1, blockBox);
            }
         }

         this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.EAST, true), 0, 13, 0, blockBox);
         this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.SOUTH, true)).with(FenceBlock.EAST, true), 0, 13, 12, blockBox);
         this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.SOUTH, true)).with(FenceBlock.WEST, true), 12, 13, 12, blockBox);
         this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.WEST, true), 12, 13, 0, blockBox);

         for(j = 3; j <= 9; j += 2) {
            this.fillWithOutline(world, blockBox, 1, 7, j, 1, 8, j, blockState3, blockState3, false);
            this.fillWithOutline(world, blockBox, 11, 7, j, 11, 8, j, blockState4, blockState4, false);
         }

         BlockState blockState5 = (BlockState)Blocks.NETHER_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH);

         int k;
         int q;
         for(k = 0; k <= 6; ++k) {
            int l = k + 4;

            for(q = 5; q <= 7; ++q) {
               this.addBlock(world, blockState5, q, 5 + k, l, blockBox);
            }

            if (l >= 5 && l <= 8) {
               this.fillWithOutline(world, blockBox, 5, 5, l, 7, k + 4, l, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            } else if (l >= 9 && l <= 10) {
               this.fillWithOutline(world, blockBox, 5, 8, l, 7, k + 4, l, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            }

            if (k >= 1) {
               this.fillWithOutline(world, blockBox, 5, 6 + k, l, 7, 9 + k, l, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            }
         }

         for(k = 5; k <= 7; ++k) {
            this.addBlock(world, blockState5, k, 12, 11, blockBox);
         }

         this.fillWithOutline(world, blockBox, 5, 6, 7, 5, 7, 7, blockState4, blockState4, false);
         this.fillWithOutline(world, blockBox, 7, 6, 7, 7, 7, 7, blockState3, blockState3, false);
         this.fillWithOutline(world, blockBox, 5, 13, 12, 7, 13, 12, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 2, 5, 2, 3, 5, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 2, 5, 9, 3, 5, 10, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 2, 5, 4, 2, 5, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 9, 5, 2, 10, 5, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 9, 5, 9, 10, 5, 10, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 10, 5, 4, 10, 5, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         BlockState blockState6 = (BlockState)blockState5.with(StairsBlock.FACING, Direction.EAST);
         BlockState blockState7 = (BlockState)blockState5.with(StairsBlock.FACING, Direction.WEST);
         this.addBlock(world, blockState7, 4, 5, 2, blockBox);
         this.addBlock(world, blockState7, 4, 5, 3, blockBox);
         this.addBlock(world, blockState7, 4, 5, 9, blockBox);
         this.addBlock(world, blockState7, 4, 5, 10, blockBox);
         this.addBlock(world, blockState6, 8, 5, 2, blockBox);
         this.addBlock(world, blockState6, 8, 5, 3, blockBox);
         this.addBlock(world, blockState6, 8, 5, 9, blockBox);
         this.addBlock(world, blockState6, 8, 5, 10, blockBox);
         this.fillWithOutline(world, blockBox, 3, 4, 4, 4, 4, 8, Blocks.SOUL_SAND.getDefaultState(), Blocks.SOUL_SAND.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 8, 4, 4, 9, 4, 8, Blocks.SOUL_SAND.getDefaultState(), Blocks.SOUL_SAND.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 3, 5, 4, 4, 5, 8, Blocks.NETHER_WART.getDefaultState(), Blocks.NETHER_WART.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 8, 5, 4, 9, 5, 8, Blocks.NETHER_WART.getDefaultState(), Blocks.NETHER_WART.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 4, 2, 0, 8, 2, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 2, 4, 12, 2, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 4, 0, 0, 8, 1, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 4, 0, 9, 8, 1, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 0, 4, 3, 1, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 9, 0, 4, 12, 1, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);

         int r;
         for(q = 4; q <= 8; ++q) {
            for(r = 0; r <= 2; ++r) {
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), q, -1, r, blockBox);
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), q, -1, 12 - r, blockBox);
            }
         }

         for(q = 0; q <= 2; ++q) {
            for(r = 4; r <= 8; ++r) {
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), q, -1, r, blockBox);
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), 12 - q, -1, r, blockBox);
            }
         }

         return true;
      }
   }

   public static class CorridorExit extends NetherFortressGenerator.Piece {
      public CorridorExit(int i, Random random, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_EXIT, i);
         this.setOrientation(direction);
         this.boundingBox = blockBox;
      }

      public CorridorExit(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_CORRIDOR_EXIT, compoundTag);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         this.method_14814((NetherFortressGenerator.Start)structurePiece, list, random, 5, 3, true);
      }

      public static NetherFortressGenerator.CorridorExit method_14801(List<StructurePiece> list, Random random, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -5, -3, 0, 13, 14, 13, direction);
         return method_14809(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new NetherFortressGenerator.CorridorExit(l, random, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         this.fillWithOutline(world, blockBox, 0, 3, 0, 12, 4, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 5, 0, 12, 13, 12, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 5, 0, 1, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 11, 5, 0, 12, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 2, 5, 11, 4, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 8, 5, 11, 10, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 5, 9, 11, 7, 12, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 2, 5, 0, 4, 12, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 8, 5, 0, 10, 12, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 5, 9, 0, 7, 12, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 2, 11, 2, 10, 12, 10, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 5, 8, 0, 7, 8, 0, Blocks.NETHER_BRICK_FENCE.getDefaultState(), Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
         BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);

         int m;
         for(m = 1; m <= 11; m += 2) {
            this.fillWithOutline(world, blockBox, m, 10, 0, m, 11, 0, blockState, blockState, false);
            this.fillWithOutline(world, blockBox, m, 10, 12, m, 11, 12, blockState, blockState, false);
            this.fillWithOutline(world, blockBox, 0, 10, m, 0, 11, m, blockState2, blockState2, false);
            this.fillWithOutline(world, blockBox, 12, 10, m, 12, 11, m, blockState2, blockState2, false);
            this.addBlock(world, Blocks.NETHER_BRICKS.getDefaultState(), m, 13, 0, blockBox);
            this.addBlock(world, Blocks.NETHER_BRICKS.getDefaultState(), m, 13, 12, blockBox);
            this.addBlock(world, Blocks.NETHER_BRICKS.getDefaultState(), 0, 13, m, blockBox);
            this.addBlock(world, Blocks.NETHER_BRICKS.getDefaultState(), 12, 13, m, blockBox);
            if (m != 11) {
               this.addBlock(world, blockState, m + 1, 13, 0, blockBox);
               this.addBlock(world, blockState, m + 1, 13, 12, blockBox);
               this.addBlock(world, blockState2, 0, 13, m + 1, blockBox);
               this.addBlock(world, blockState2, 12, 13, m + 1, blockBox);
            }
         }

         this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.EAST, true), 0, 13, 0, blockBox);
         this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.SOUTH, true)).with(FenceBlock.EAST, true), 0, 13, 12, blockBox);
         this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.SOUTH, true)).with(FenceBlock.WEST, true), 12, 13, 12, blockBox);
         this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.WEST, true), 12, 13, 0, blockBox);

         for(m = 3; m <= 9; m += 2) {
            this.fillWithOutline(world, blockBox, 1, 7, m, 1, 8, m, (BlockState)blockState2.with(FenceBlock.WEST, true), (BlockState)blockState2.with(FenceBlock.WEST, true), false);
            this.fillWithOutline(world, blockBox, 11, 7, m, 11, 8, m, (BlockState)blockState2.with(FenceBlock.EAST, true), (BlockState)blockState2.with(FenceBlock.EAST, true), false);
         }

         this.fillWithOutline(world, blockBox, 4, 2, 0, 8, 2, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 2, 4, 12, 2, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 4, 0, 0, 8, 1, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 4, 0, 9, 8, 1, 12, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 0, 4, 3, 1, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 9, 0, 4, 12, 1, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);

         int n;
         for(m = 4; m <= 8; ++m) {
            for(n = 0; n <= 2; ++n) {
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), m, -1, n, blockBox);
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), m, -1, 12 - n, blockBox);
            }
         }

         for(m = 0; m <= 2; ++m) {
            for(n = 4; n <= 8; ++n) {
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), m, -1, n, blockBox);
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), 12 - m, -1, n, blockBox);
            }
         }

         this.fillWithOutline(world, blockBox, 5, 5, 5, 7, 5, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 6, 1, 6, 6, 4, 6, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.addBlock(world, Blocks.NETHER_BRICKS.getDefaultState(), 6, 0, 6, blockBox);
         this.addBlock(world, Blocks.LAVA.getDefaultState(), 6, 5, 6, blockBox);
         BlockPos blockPos = new BlockPos(this.applyXTransform(6, 6), this.applyYTransform(5), this.applyZTransform(6, 6));
         if (blockBox.contains(blockPos)) {
            world.getFluidTickScheduler().schedule(blockPos, Fluids.LAVA, 0);
         }

         return true;
      }
   }

   public static class BridgePlatform extends NetherFortressGenerator.Piece {
      private boolean hasBlazeSpawner;

      public BridgePlatform(int i, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_PLATFORM, i);
         this.setOrientation(direction);
         this.boundingBox = blockBox;
      }

      public BridgePlatform(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_PLATFORM, compoundTag);
         this.hasBlazeSpawner = compoundTag.getBoolean("Mob");
      }

      protected void toNbt(CompoundTag tag) {
         super.toNbt(tag);
         tag.putBoolean("Mob", this.hasBlazeSpawner);
      }

      public static NetherFortressGenerator.BridgePlatform method_14807(List<StructurePiece> list, int i, int j, int k, int l, Direction direction) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -2, 0, 0, 7, 8, 9, direction);
         return method_14809(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new NetherFortressGenerator.BridgePlatform(l, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         this.fillWithOutline(world, blockBox, 0, 2, 0, 6, 7, 7, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 0, 0, 5, 1, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 2, 1, 5, 2, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 3, 2, 5, 3, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 4, 3, 5, 4, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 2, 0, 1, 4, 2, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 5, 2, 0, 5, 4, 2, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 5, 2, 1, 5, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 5, 5, 2, 5, 5, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 5, 3, 0, 5, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 6, 5, 3, 6, 5, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 5, 8, 5, 5, 8, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
         this.addBlock(world, (BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true), 1, 6, 3, blockBox);
         this.addBlock(world, (BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.EAST, true), 5, 6, 3, blockBox);
         this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.EAST, true)).with(FenceBlock.NORTH, true), 0, 6, 3, blockBox);
         this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.NORTH, true), 6, 6, 3, blockBox);
         this.fillWithOutline(world, blockBox, 0, 6, 4, 0, 6, 7, blockState2, blockState2, false);
         this.fillWithOutline(world, blockBox, 6, 6, 4, 6, 6, 7, blockState2, blockState2, false);
         this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.EAST, true)).with(FenceBlock.SOUTH, true), 0, 6, 8, blockBox);
         this.addBlock(world, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.SOUTH, true), 6, 6, 8, blockBox);
         this.fillWithOutline(world, blockBox, 1, 6, 8, 5, 6, 8, blockState, blockState, false);
         this.addBlock(world, (BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.EAST, true), 1, 7, 8, blockBox);
         this.fillWithOutline(world, blockBox, 2, 7, 8, 4, 7, 8, blockState, blockState, false);
         this.addBlock(world, (BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true), 5, 7, 8, blockBox);
         this.addBlock(world, (BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.EAST, true), 2, 8, 8, blockBox);
         this.addBlock(world, blockState, 3, 8, 8, blockBox);
         this.addBlock(world, (BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true), 4, 8, 8, blockBox);
         if (!this.hasBlazeSpawner) {
            BlockPos blockPos = new BlockPos(this.applyXTransform(3, 5), this.applyYTransform(5), this.applyZTransform(3, 5));
            if (blockBox.contains(blockPos)) {
               this.hasBlazeSpawner = true;
               world.setBlockState(blockPos, Blocks.SPAWNER.getDefaultState(), 2);
               BlockEntity blockEntity = world.getBlockEntity(blockPos);
               if (blockEntity instanceof MobSpawnerBlockEntity) {
                  ((MobSpawnerBlockEntity)blockEntity).getLogic().setEntityId(EntityType.BLAZE);
               }
            }
         }

         for(int i = 0; i <= 6; ++i) {
            for(int j = 0; j <= 6; ++j) {
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, blockBox);
            }
         }

         return true;
      }
   }

   public static class BridgeStairs extends NetherFortressGenerator.Piece {
      public BridgeStairs(int i, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_STAIRS, i);
         this.setOrientation(direction);
         this.boundingBox = blockBox;
      }

      public BridgeStairs(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_STAIRS, compoundTag);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         this.method_14808((NetherFortressGenerator.Start)structurePiece, list, random, 6, 2, false);
      }

      public static NetherFortressGenerator.BridgeStairs method_14818(List<StructurePiece> list, int i, int j, int k, int l, Direction direction) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -2, 0, 0, 7, 11, 7, direction);
         return method_14809(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new NetherFortressGenerator.BridgeStairs(l, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         this.fillWithOutline(world, blockBox, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 2, 0, 6, 10, 6, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 2, 0, 1, 8, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 5, 2, 0, 6, 8, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 2, 1, 0, 8, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 6, 2, 1, 6, 8, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 2, 6, 5, 8, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
         this.fillWithOutline(world, blockBox, 0, 3, 2, 0, 5, 4, blockState2, blockState2, false);
         this.fillWithOutline(world, blockBox, 6, 3, 2, 6, 5, 2, blockState2, blockState2, false);
         this.fillWithOutline(world, blockBox, 6, 3, 4, 6, 5, 4, blockState2, blockState2, false);
         this.addBlock(world, Blocks.NETHER_BRICKS.getDefaultState(), 5, 2, 5, blockBox);
         this.fillWithOutline(world, blockBox, 4, 2, 5, 4, 3, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 3, 2, 5, 3, 4, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 2, 2, 5, 2, 5, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 2, 5, 1, 6, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 7, 1, 5, 7, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 6, 8, 2, 6, 8, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 2, 6, 0, 4, 8, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 2, 5, 0, 4, 5, 0, blockState, blockState, false);

         for(int i = 0; i <= 6; ++i) {
            for(int j = 0; j <= 6; ++j) {
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, blockBox);
            }
         }

         return true;
      }
   }

   public static class BridgeSmallCrossing extends NetherFortressGenerator.Piece {
      public BridgeSmallCrossing(int i, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_SMALL_CROSSING, i);
         this.setOrientation(direction);
         this.boundingBox = blockBox;
      }

      public BridgeSmallCrossing(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_SMALL_CROSSING, compoundTag);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         this.method_14814((NetherFortressGenerator.Start)structurePiece, list, random, 2, 0, false);
         this.method_14812((NetherFortressGenerator.Start)structurePiece, list, random, 0, 2, false);
         this.method_14808((NetherFortressGenerator.Start)structurePiece, list, random, 0, 2, false);
      }

      public static NetherFortressGenerator.BridgeSmallCrossing method_14817(List<StructurePiece> list, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -2, 0, 0, 7, 9, 7, direction);
         return method_14809(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new NetherFortressGenerator.BridgeSmallCrossing(l, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         this.fillWithOutline(world, blockBox, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 2, 0, 6, 7, 6, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 2, 0, 1, 6, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 2, 6, 1, 6, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 5, 2, 0, 6, 6, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 5, 2, 6, 6, 6, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 2, 0, 0, 6, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 2, 5, 0, 6, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 6, 2, 0, 6, 6, 1, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 6, 2, 5, 6, 6, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
         this.fillWithOutline(world, blockBox, 2, 6, 0, 4, 6, 0, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 2, 5, 0, 4, 5, 0, blockState, blockState, false);
         this.fillWithOutline(world, blockBox, 2, 6, 6, 4, 6, 6, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 2, 5, 6, 4, 5, 6, blockState, blockState, false);
         this.fillWithOutline(world, blockBox, 0, 6, 2, 0, 6, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 5, 2, 0, 5, 4, blockState2, blockState2, false);
         this.fillWithOutline(world, blockBox, 6, 6, 2, 6, 6, 4, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 6, 5, 2, 6, 5, 4, blockState2, blockState2, false);

         for(int i = 0; i <= 6; ++i) {
            for(int j = 0; j <= 6; ++j) {
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, blockBox);
            }
         }

         return true;
      }
   }

   public static class BridgeCrossing extends NetherFortressGenerator.Piece {
      public BridgeCrossing(int i, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, i);
         this.setOrientation(direction);
         this.boundingBox = blockBox;
      }

      protected BridgeCrossing(Random random, int x, int z) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, 0);
         this.setOrientation(Direction.Type.HORIZONTAL.random(random));
         if (this.getFacing().getAxis() == Direction.Axis.Z) {
            this.boundingBox = new BlockBox(x, 64, z, x + 19 - 1, 73, z + 19 - 1);
         } else {
            this.boundingBox = new BlockBox(x, 64, z, x + 19 - 1, 73, z + 19 - 1);
         }

      }

      protected BridgeCrossing(StructurePieceType structurePieceType, CompoundTag compoundTag) {
         super(structurePieceType, compoundTag);
      }

      public BridgeCrossing(StructureManager structureManager, CompoundTag compoundTag) {
         this(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, compoundTag);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         this.method_14814((NetherFortressGenerator.Start)structurePiece, list, random, 8, 3, false);
         this.method_14812((NetherFortressGenerator.Start)structurePiece, list, random, 3, 8, false);
         this.method_14808((NetherFortressGenerator.Start)structurePiece, list, random, 3, 8, false);
      }

      public static NetherFortressGenerator.BridgeCrossing method_14796(List<StructurePiece> list, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -8, -3, 0, 19, 10, 19, direction);
         return method_14809(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new NetherFortressGenerator.BridgeCrossing(l, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         this.fillWithOutline(world, blockBox, 7, 3, 0, 11, 4, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 3, 7, 18, 4, 11, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 8, 5, 0, 10, 7, 18, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 5, 8, 18, 7, 10, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 7, 5, 0, 7, 5, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 7, 5, 11, 7, 5, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 11, 5, 0, 11, 5, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 11, 5, 11, 11, 5, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 5, 7, 7, 5, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 11, 5, 7, 18, 5, 7, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 5, 11, 7, 5, 11, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 11, 5, 11, 18, 5, 11, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 7, 2, 0, 11, 2, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 7, 2, 13, 11, 2, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 7, 0, 0, 11, 1, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 7, 0, 15, 11, 1, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);

         int k;
         int l;
         for(k = 7; k <= 11; ++k) {
            for(l = 0; l <= 2; ++l) {
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), k, -1, l, blockBox);
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), k, -1, 18 - l, blockBox);
            }
         }

         this.fillWithOutline(world, blockBox, 0, 2, 7, 5, 2, 11, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 13, 2, 7, 18, 2, 11, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 0, 7, 3, 1, 11, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 15, 0, 7, 18, 1, 11, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);

         for(k = 0; k <= 2; ++k) {
            for(l = 7; l <= 11; ++l) {
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), k, -1, l, blockBox);
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), 18 - k, -1, l, blockBox);
            }
         }

         return true;
      }
   }

   public static class BridgeEnd extends NetherFortressGenerator.Piece {
      private final int seed;

      public BridgeEnd(int i, Random random, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_END, i);
         this.setOrientation(direction);
         this.boundingBox = blockBox;
         this.seed = random.nextInt();
      }

      public BridgeEnd(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_END, compoundTag);
         this.seed = compoundTag.getInt("Seed");
      }

      public static NetherFortressGenerator.BridgeEnd method_14797(List<StructurePiece> list, Random random, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -1, -3, 0, 5, 10, 8, direction);
         return method_14809(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new NetherFortressGenerator.BridgeEnd(l, random, blockBox, direction) : null;
      }

      protected void toNbt(CompoundTag tag) {
         super.toNbt(tag);
         tag.putInt("Seed", this.seed);
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         Random random2 = new Random((long)this.seed);

         int p;
         int q;
         int r;
         for(p = 0; p <= 4; ++p) {
            for(q = 3; q <= 4; ++q) {
               r = random2.nextInt(8);
               this.fillWithOutline(world, blockBox, p, q, 0, p, q, r, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            }
         }

         p = random2.nextInt(8);
         this.fillWithOutline(world, blockBox, 0, 5, 0, 0, 5, p, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         p = random2.nextInt(8);
         this.fillWithOutline(world, blockBox, 4, 5, 0, 4, 5, p, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);

         for(p = 0; p <= 4; ++p) {
            q = random2.nextInt(5);
            this.fillWithOutline(world, blockBox, p, 2, 0, p, 2, q, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         }

         for(p = 0; p <= 4; ++p) {
            for(q = 0; q <= 1; ++q) {
               r = random2.nextInt(3);
               this.fillWithOutline(world, blockBox, p, q, 0, p, q, r, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
            }
         }

         return true;
      }
   }

   public static class Bridge extends NetherFortressGenerator.Piece {
      public Bridge(int i, Random random, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE, i);
         this.setOrientation(direction);
         this.boundingBox = blockBox;
      }

      public Bridge(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE, compoundTag);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         this.method_14814((NetherFortressGenerator.Start)structurePiece, list, random, 1, 3, false);
      }

      public static NetherFortressGenerator.Bridge method_14798(List<StructurePiece> list, Random random, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -1, -3, 0, 5, 10, 19, direction);
         return method_14809(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new NetherFortressGenerator.Bridge(l, random, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         this.fillWithOutline(world, blockBox, 0, 3, 0, 4, 4, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 5, 0, 3, 7, 18, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 5, 0, 0, 5, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 4, 5, 0, 4, 5, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 2, 0, 4, 2, 5, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 2, 13, 4, 2, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 0, 0, 4, 1, 3, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 0, 0, 15, 4, 1, 18, Blocks.NETHER_BRICKS.getDefaultState(), Blocks.NETHER_BRICKS.getDefaultState(), false);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 0; j <= 2; ++j) {
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, j, blockBox);
               this.method_14936(world, Blocks.NETHER_BRICKS.getDefaultState(), i, -1, 18 - j, blockBox);
            }
         }

         BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
         BlockState blockState2 = (BlockState)blockState.with(FenceBlock.EAST, true);
         BlockState blockState3 = (BlockState)blockState.with(FenceBlock.WEST, true);
         this.fillWithOutline(world, blockBox, 0, 1, 1, 0, 4, 1, blockState2, blockState2, false);
         this.fillWithOutline(world, blockBox, 0, 3, 4, 0, 4, 4, blockState2, blockState2, false);
         this.fillWithOutline(world, blockBox, 0, 3, 14, 0, 4, 14, blockState2, blockState2, false);
         this.fillWithOutline(world, blockBox, 0, 1, 17, 0, 4, 17, blockState2, blockState2, false);
         this.fillWithOutline(world, blockBox, 4, 1, 1, 4, 4, 1, blockState3, blockState3, false);
         this.fillWithOutline(world, blockBox, 4, 3, 4, 4, 4, 4, blockState3, blockState3, false);
         this.fillWithOutline(world, blockBox, 4, 3, 14, 4, 4, 14, blockState3, blockState3, false);
         this.fillWithOutline(world, blockBox, 4, 1, 17, 4, 4, 17, blockState3, blockState3, false);
         return true;
      }
   }

   public static class Start extends NetherFortressGenerator.BridgeCrossing {
      public NetherFortressGenerator.class_3404 field_14506;
      public List<NetherFortressGenerator.class_3404> bridgePieces;
      public List<NetherFortressGenerator.class_3404> corridorPieces;
      public final List<StructurePiece> field_14505 = Lists.newArrayList();

      public Start(Random random, int i, int j) {
         super(random, i, j);
         this.bridgePieces = Lists.newArrayList();
         NetherFortressGenerator.class_3404[] var4 = NetherFortressGenerator.field_14494;
         int var5 = var4.length;

         int var6;
         NetherFortressGenerator.class_3404 lv2;
         for(var6 = 0; var6 < var5; ++var6) {
            lv2 = var4[var6];
            lv2.field_14502 = 0;
            this.bridgePieces.add(lv2);
         }

         this.corridorPieces = Lists.newArrayList();
         var4 = NetherFortressGenerator.field_14493;
         var5 = var4.length;

         for(var6 = 0; var6 < var5; ++var6) {
            lv2 = var4[var6];
            lv2.field_14502 = 0;
            this.corridorPieces.add(lv2);
         }

      }

      public Start(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.NETHER_FORTRESS_START, compoundTag);
      }
   }

   abstract static class Piece extends StructurePiece {
      protected Piece(StructurePieceType structurePieceType, int i) {
         super(structurePieceType, i);
      }

      public Piece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
         super(structurePieceType, compoundTag);
      }

      protected void toNbt(CompoundTag tag) {
      }

      private int method_14810(List<NetherFortressGenerator.class_3404> list) {
         boolean bl = false;
         int i = 0;

         NetherFortressGenerator.class_3404 lv;
         for(Iterator var4 = list.iterator(); var4.hasNext(); i += lv.field_14503) {
            lv = (NetherFortressGenerator.class_3404)var4.next();
            if (lv.field_14499 > 0 && lv.field_14502 < lv.field_14499) {
               bl = true;
            }
         }

         return bl ? i : -1;
      }

      private NetherFortressGenerator.Piece method_14811(NetherFortressGenerator.Start start, List<NetherFortressGenerator.class_3404> list, List<StructurePiece> list2, Random random, int i, int j, int k, Direction direction, int l) {
         int m = this.method_14810(list);
         boolean bl = m > 0 && l <= 30;
         int n = 0;

         while(n < 5 && bl) {
            ++n;
            int o = random.nextInt(m);
            Iterator var14 = list.iterator();

            while(var14.hasNext()) {
               NetherFortressGenerator.class_3404 lv = (NetherFortressGenerator.class_3404)var14.next();
               o -= lv.field_14503;
               if (o < 0) {
                  if (!lv.method_14816(l) || lv == start.field_14506 && !lv.field_14500) {
                     break;
                  }

                  NetherFortressGenerator.Piece piece = NetherFortressGenerator.generatePiece(lv, list2, random, i, j, k, direction, l);
                  if (piece != null) {
                     ++lv.field_14502;
                     start.field_14506 = lv;
                     if (!lv.method_14815()) {
                        list.remove(lv);
                     }

                     return piece;
                  }
               }
            }
         }

         return NetherFortressGenerator.BridgeEnd.method_14797(list2, random, i, j, k, direction, l);
      }

      private StructurePiece method_14813(NetherFortressGenerator.Start start, List<StructurePiece> list, Random random, int i, int j, int k, @Nullable Direction direction, int l, boolean bl) {
         if (Math.abs(i - start.getBoundingBox().minX) <= 112 && Math.abs(k - start.getBoundingBox().minZ) <= 112) {
            List<NetherFortressGenerator.class_3404> list2 = start.bridgePieces;
            if (bl) {
               list2 = start.corridorPieces;
            }

            StructurePiece structurePiece = this.method_14811(start, list2, list, random, i, j, k, direction, l + 1);
            if (structurePiece != null) {
               list.add(structurePiece);
               start.field_14505.add(structurePiece);
            }

            return structurePiece;
         } else {
            return NetherFortressGenerator.BridgeEnd.method_14797(list, random, i, j, k, direction, l);
         }
      }

      @Nullable
      protected StructurePiece method_14814(NetherFortressGenerator.Start start, List<StructurePiece> list, Random random, int i, int j, boolean bl) {
         Direction direction = this.getFacing();
         if (direction != null) {
            switch(direction) {
            case NORTH:
               return this.method_14813(start, list, random, this.boundingBox.minX + i, this.boundingBox.minY + j, this.boundingBox.minZ - 1, direction, this.method_14923(), bl);
            case SOUTH:
               return this.method_14813(start, list, random, this.boundingBox.minX + i, this.boundingBox.minY + j, this.boundingBox.maxZ + 1, direction, this.method_14923(), bl);
            case WEST:
               return this.method_14813(start, list, random, this.boundingBox.minX - 1, this.boundingBox.minY + j, this.boundingBox.minZ + i, direction, this.method_14923(), bl);
            case EAST:
               return this.method_14813(start, list, random, this.boundingBox.maxX + 1, this.boundingBox.minY + j, this.boundingBox.minZ + i, direction, this.method_14923(), bl);
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece method_14812(NetherFortressGenerator.Start start, List<StructurePiece> list, Random random, int i, int j, boolean bl) {
         Direction direction = this.getFacing();
         if (direction != null) {
            switch(direction) {
            case NORTH:
               return this.method_14813(start, list, random, this.boundingBox.minX - 1, this.boundingBox.minY + i, this.boundingBox.minZ + j, Direction.WEST, this.method_14923(), bl);
            case SOUTH:
               return this.method_14813(start, list, random, this.boundingBox.minX - 1, this.boundingBox.minY + i, this.boundingBox.minZ + j, Direction.WEST, this.method_14923(), bl);
            case WEST:
               return this.method_14813(start, list, random, this.boundingBox.minX + j, this.boundingBox.minY + i, this.boundingBox.minZ - 1, Direction.NORTH, this.method_14923(), bl);
            case EAST:
               return this.method_14813(start, list, random, this.boundingBox.minX + j, this.boundingBox.minY + i, this.boundingBox.minZ - 1, Direction.NORTH, this.method_14923(), bl);
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece method_14808(NetherFortressGenerator.Start start, List<StructurePiece> list, Random random, int i, int j, boolean bl) {
         Direction direction = this.getFacing();
         if (direction != null) {
            switch(direction) {
            case NORTH:
               return this.method_14813(start, list, random, this.boundingBox.maxX + 1, this.boundingBox.minY + i, this.boundingBox.minZ + j, Direction.EAST, this.method_14923(), bl);
            case SOUTH:
               return this.method_14813(start, list, random, this.boundingBox.maxX + 1, this.boundingBox.minY + i, this.boundingBox.minZ + j, Direction.EAST, this.method_14923(), bl);
            case WEST:
               return this.method_14813(start, list, random, this.boundingBox.minX + j, this.boundingBox.minY + i, this.boundingBox.maxZ + 1, Direction.SOUTH, this.method_14923(), bl);
            case EAST:
               return this.method_14813(start, list, random, this.boundingBox.minX + j, this.boundingBox.minY + i, this.boundingBox.maxZ + 1, Direction.SOUTH, this.method_14923(), bl);
            }
         }

         return null;
      }

      protected static boolean method_14809(BlockBox blockBox) {
         return blockBox != null && blockBox.minY > 10;
      }
   }

   static class class_3404 {
      public final Class<? extends NetherFortressGenerator.Piece> field_14501;
      public final int field_14503;
      public int field_14502;
      public final int field_14499;
      public final boolean field_14500;

      public class_3404(Class<? extends NetherFortressGenerator.Piece> var1, int i, int j, boolean bl) {
         this.field_14501 = var1;
         this.field_14503 = i;
         this.field_14499 = j;
         this.field_14500 = bl;
      }

      public class_3404(Class<? extends NetherFortressGenerator.Piece> var1, int i, int j) {
         this(var1, i, j, false);
      }

      public boolean method_14816(int i) {
         return this.field_14499 == 0 || this.field_14502 < this.field_14499;
      }

      public boolean method_14815() {
         return this.field_14499 == 0 || this.field_14502 < this.field_14499;
      }
   }
}
