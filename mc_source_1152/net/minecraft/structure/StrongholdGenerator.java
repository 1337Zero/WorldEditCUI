package net.minecraft.structure;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class StrongholdGenerator {
   private static final StrongholdGenerator.class_3427[] field_15265 = new StrongholdGenerator.class_3427[]{new StrongholdGenerator.class_3427(StrongholdGenerator.Corridor.class, 40, 0), new StrongholdGenerator.class_3427(StrongholdGenerator.PrisonHall.class, 5, 5), new StrongholdGenerator.class_3427(StrongholdGenerator.LeftTurn.class, 20, 0), new StrongholdGenerator.class_3427(StrongholdGenerator.RightTurn.class, 20, 0), new StrongholdGenerator.class_3427(StrongholdGenerator.SquareRoom.class, 10, 6), new StrongholdGenerator.class_3427(StrongholdGenerator.Stairs.class, 5, 5), new StrongholdGenerator.class_3427(StrongholdGenerator.SpiralStaircase.class, 5, 5), new StrongholdGenerator.class_3427(StrongholdGenerator.FiveWayCrossing.class, 5, 4), new StrongholdGenerator.class_3427(StrongholdGenerator.ChestCorridor.class, 5, 4), new StrongholdGenerator.class_3427(StrongholdGenerator.Library.class, 10, 2) {
      public boolean method_14862(int i) {
         return super.method_14862(i) && i > 4;
      }
   }, new StrongholdGenerator.class_3427(StrongholdGenerator.PortalRoom.class, 20, 1) {
      public boolean method_14862(int i) {
         return super.method_14862(i) && i > 5;
      }
   }};
   private static List<StrongholdGenerator.class_3427> field_15267;
   private static Class<? extends StrongholdGenerator.Piece> field_15266;
   private static int field_15264;
   private static final StrongholdGenerator.StoneBrickRandomizer field_15263 = new StrongholdGenerator.StoneBrickRandomizer();

   public static void method_14855() {
      field_15267 = Lists.newArrayList();
      StrongholdGenerator.class_3427[] var0 = field_15265;
      int var1 = var0.length;

      for(int var2 = 0; var2 < var1; ++var2) {
         StrongholdGenerator.class_3427 lv = var0[var2];
         lv.field_15277 = 0;
         field_15267.add(lv);
      }

      field_15266 = null;
   }

   private static boolean method_14852() {
      boolean bl = false;
      field_15264 = 0;

      StrongholdGenerator.class_3427 lv;
      for(Iterator var1 = field_15267.iterator(); var1.hasNext(); field_15264 += lv.field_15278) {
         lv = (StrongholdGenerator.class_3427)var1.next();
         if (lv.field_15275 > 0 && lv.field_15277 < lv.field_15275) {
            bl = true;
         }
      }

      return bl;
   }

   private static StrongholdGenerator.Piece method_14847(Class<? extends StrongholdGenerator.Piece> var0, List<StructurePiece> list, Random random, int i, int j, int k, @Nullable Direction direction, int l) {
      StrongholdGenerator.Piece piece = null;
      if (var0 == StrongholdGenerator.Corridor.class) {
         piece = StrongholdGenerator.Corridor.method_14867(list, random, i, j, k, direction, l);
      } else if (var0 == StrongholdGenerator.PrisonHall.class) {
         piece = StrongholdGenerator.PrisonHall.method_14864(list, random, i, j, k, direction, l);
      } else if (var0 == StrongholdGenerator.LeftTurn.class) {
         piece = StrongholdGenerator.LeftTurn.method_14859(list, random, i, j, k, direction, l);
      } else if (var0 == StrongholdGenerator.RightTurn.class) {
         piece = StrongholdGenerator.RightTurn.method_16652(list, random, i, j, k, direction, l);
      } else if (var0 == StrongholdGenerator.SquareRoom.class) {
         piece = StrongholdGenerator.SquareRoom.method_14865(list, random, i, j, k, direction, l);
      } else if (var0 == StrongholdGenerator.Stairs.class) {
         piece = StrongholdGenerator.Stairs.method_14868(list, random, i, j, k, direction, l);
      } else if (var0 == StrongholdGenerator.SpiralStaircase.class) {
         piece = StrongholdGenerator.SpiralStaircase.method_14866(list, random, i, j, k, direction, l);
      } else if (var0 == StrongholdGenerator.FiveWayCrossing.class) {
         piece = StrongholdGenerator.FiveWayCrossing.method_14858(list, random, i, j, k, direction, l);
      } else if (var0 == StrongholdGenerator.ChestCorridor.class) {
         piece = StrongholdGenerator.ChestCorridor.method_14856(list, random, i, j, k, direction, l);
      } else if (var0 == StrongholdGenerator.Library.class) {
         piece = StrongholdGenerator.Library.method_14860(list, random, i, j, k, direction, l);
      } else if (var0 == StrongholdGenerator.PortalRoom.class) {
         piece = StrongholdGenerator.PortalRoom.method_14863(list, i, j, k, direction, l);
      }

      return (StrongholdGenerator.Piece)piece;
   }

   private static StrongholdGenerator.Piece method_14851(StrongholdGenerator.Start start, List<StructurePiece> list, Random random, int i, int j, int k, Direction direction, int l) {
      if (!method_14852()) {
         return null;
      } else {
         if (field_15266 != null) {
            StrongholdGenerator.Piece piece = method_14847(field_15266, list, random, i, j, k, direction, l);
            field_15266 = null;
            if (piece != null) {
               return piece;
            }
         }

         int m = 0;

         while(m < 5) {
            ++m;
            int n = random.nextInt(field_15264);
            Iterator var10 = field_15267.iterator();

            while(var10.hasNext()) {
               StrongholdGenerator.class_3427 lv = (StrongholdGenerator.class_3427)var10.next();
               n -= lv.field_15278;
               if (n < 0) {
                  if (!lv.method_14862(l) || lv == start.field_15284) {
                     break;
                  }

                  StrongholdGenerator.Piece piece2 = method_14847(lv.field_15276, list, random, i, j, k, direction, l);
                  if (piece2 != null) {
                     ++lv.field_15277;
                     start.field_15284 = lv;
                     if (!lv.method_14861()) {
                        field_15267.remove(lv);
                     }

                     return piece2;
                  }
               }
            }
         }

         BlockBox blockBox = StrongholdGenerator.SmallCorridor.method_14857(list, random, i, j, k, direction);
         if (blockBox != null && blockBox.minY > 1) {
            return new StrongholdGenerator.SmallCorridor(l, blockBox, direction);
         } else {
            return null;
         }
      }
   }

   private static StructurePiece method_14854(StrongholdGenerator.Start start, List<StructurePiece> list, Random random, int i, int j, int k, @Nullable Direction direction, int l) {
      if (l > 50) {
         return null;
      } else if (Math.abs(i - start.getBoundingBox().minX) <= 112 && Math.abs(k - start.getBoundingBox().minZ) <= 112) {
         StructurePiece structurePiece = method_14851(start, list, random, i, j, k, direction, l + 1);
         if (structurePiece != null) {
            list.add(structurePiece);
            start.field_15282.add(structurePiece);
         }

         return structurePiece;
      } else {
         return null;
      }
   }

   static class StoneBrickRandomizer extends StructurePiece.BlockRandomizer {
      private StoneBrickRandomizer() {
      }

      public void setBlock(Random random, int x, int y, int z, boolean placeBlock) {
         if (placeBlock) {
            float f = random.nextFloat();
            if (f < 0.2F) {
               this.block = Blocks.CRACKED_STONE_BRICKS.getDefaultState();
            } else if (f < 0.5F) {
               this.block = Blocks.MOSSY_STONE_BRICKS.getDefaultState();
            } else if (f < 0.55F) {
               this.block = Blocks.INFESTED_STONE_BRICKS.getDefaultState();
            } else {
               this.block = Blocks.STONE_BRICKS.getDefaultState();
            }
         } else {
            this.block = Blocks.CAVE_AIR.getDefaultState();
         }

      }
   }

   public static class PortalRoom extends StrongholdGenerator.Piece {
      private boolean spawnerPlaced;

      public PortalRoom(int i, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_PORTAL_ROOM, i);
         this.setOrientation(direction);
         this.boundingBox = blockBox;
      }

      public PortalRoom(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.STRONGHOLD_PORTAL_ROOM, compoundTag);
         this.spawnerPlaced = compoundTag.getBoolean("Mob");
      }

      protected void toNbt(CompoundTag tag) {
         super.toNbt(tag);
         tag.putBoolean("Mob", this.spawnerPlaced);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         if (structurePiece != null) {
            ((StrongholdGenerator.Start)structurePiece).field_15283 = this;
         }

      }

      public static StrongholdGenerator.PortalRoom method_14863(List<StructurePiece> list, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -4, -1, 0, 11, 8, 16, direction);
         return method_14871(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new StrongholdGenerator.PortalRoom(l, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         this.fillWithOutline(world, blockBox, 0, 0, 0, 10, 7, 15, false, random, StrongholdGenerator.field_15263);
         this.generateEntrance(world, random, blockBox, StrongholdGenerator.Piece.EntranceType.GRATES, 4, 1, 0);
         int i = 6;
         this.fillWithOutline(world, blockBox, 1, i, 1, 1, i, 14, false, random, StrongholdGenerator.field_15263);
         this.fillWithOutline(world, blockBox, 9, i, 1, 9, i, 14, false, random, StrongholdGenerator.field_15263);
         this.fillWithOutline(world, blockBox, 2, i, 1, 8, i, 2, false, random, StrongholdGenerator.field_15263);
         this.fillWithOutline(world, blockBox, 2, i, 14, 8, i, 14, false, random, StrongholdGenerator.field_15263);
         this.fillWithOutline(world, blockBox, 1, 1, 1, 2, 1, 4, false, random, StrongholdGenerator.field_15263);
         this.fillWithOutline(world, blockBox, 8, 1, 1, 9, 1, 4, false, random, StrongholdGenerator.field_15263);
         this.fillWithOutline(world, blockBox, 1, 1, 1, 1, 1, 3, Blocks.LAVA.getDefaultState(), Blocks.LAVA.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 9, 1, 1, 9, 1, 3, Blocks.LAVA.getDefaultState(), Blocks.LAVA.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 3, 1, 8, 7, 1, 12, false, random, StrongholdGenerator.field_15263);
         this.fillWithOutline(world, blockBox, 4, 1, 9, 6, 1, 11, Blocks.LAVA.getDefaultState(), Blocks.LAVA.getDefaultState(), false);
         BlockState blockState = (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true)).with(PaneBlock.EAST, true);

         int k;
         for(k = 3; k < 14; k += 2) {
            this.fillWithOutline(world, blockBox, 0, 3, k, 0, 4, k, blockState, blockState, false);
            this.fillWithOutline(world, blockBox, 10, 3, k, 10, 4, k, blockState, blockState, false);
         }

         for(k = 2; k < 9; k += 2) {
            this.fillWithOutline(world, blockBox, k, 3, 15, k, 4, 15, blockState2, blockState2, false);
         }

         BlockState blockState3 = (BlockState)Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH);
         this.fillWithOutline(world, blockBox, 4, 1, 5, 6, 1, 7, false, random, StrongholdGenerator.field_15263);
         this.fillWithOutline(world, blockBox, 4, 2, 6, 6, 2, 7, false, random, StrongholdGenerator.field_15263);
         this.fillWithOutline(world, blockBox, 4, 3, 7, 6, 3, 7, false, random, StrongholdGenerator.field_15263);

         for(int l = 4; l <= 6; ++l) {
            this.addBlock(world, blockState3, l, 1, 4, blockBox);
            this.addBlock(world, blockState3, l, 2, 5, blockBox);
            this.addBlock(world, blockState3, l, 3, 6, blockBox);
         }

         BlockState blockState4 = (BlockState)Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.NORTH);
         BlockState blockState5 = (BlockState)Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.SOUTH);
         BlockState blockState6 = (BlockState)Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.EAST);
         BlockState blockState7 = (BlockState)Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.WEST);
         boolean bl = true;
         boolean[] bls = new boolean[12];

         for(int m = 0; m < bls.length; ++m) {
            bls[m] = random.nextFloat() > 0.9F;
            bl &= bls[m];
         }

         this.addBlock(world, (BlockState)blockState4.with(EndPortalFrameBlock.EYE, bls[0]), 4, 3, 8, blockBox);
         this.addBlock(world, (BlockState)blockState4.with(EndPortalFrameBlock.EYE, bls[1]), 5, 3, 8, blockBox);
         this.addBlock(world, (BlockState)blockState4.with(EndPortalFrameBlock.EYE, bls[2]), 6, 3, 8, blockBox);
         this.addBlock(world, (BlockState)blockState5.with(EndPortalFrameBlock.EYE, bls[3]), 4, 3, 12, blockBox);
         this.addBlock(world, (BlockState)blockState5.with(EndPortalFrameBlock.EYE, bls[4]), 5, 3, 12, blockBox);
         this.addBlock(world, (BlockState)blockState5.with(EndPortalFrameBlock.EYE, bls[5]), 6, 3, 12, blockBox);
         this.addBlock(world, (BlockState)blockState6.with(EndPortalFrameBlock.EYE, bls[6]), 3, 3, 9, blockBox);
         this.addBlock(world, (BlockState)blockState6.with(EndPortalFrameBlock.EYE, bls[7]), 3, 3, 10, blockBox);
         this.addBlock(world, (BlockState)blockState6.with(EndPortalFrameBlock.EYE, bls[8]), 3, 3, 11, blockBox);
         this.addBlock(world, (BlockState)blockState7.with(EndPortalFrameBlock.EYE, bls[9]), 7, 3, 9, blockBox);
         this.addBlock(world, (BlockState)blockState7.with(EndPortalFrameBlock.EYE, bls[10]), 7, 3, 10, blockBox);
         this.addBlock(world, (BlockState)blockState7.with(EndPortalFrameBlock.EYE, bls[11]), 7, 3, 11, blockBox);
         if (bl) {
            BlockState blockState8 = Blocks.END_PORTAL.getDefaultState();
            this.addBlock(world, blockState8, 4, 3, 9, blockBox);
            this.addBlock(world, blockState8, 5, 3, 9, blockBox);
            this.addBlock(world, blockState8, 6, 3, 9, blockBox);
            this.addBlock(world, blockState8, 4, 3, 10, blockBox);
            this.addBlock(world, blockState8, 5, 3, 10, blockBox);
            this.addBlock(world, blockState8, 6, 3, 10, blockBox);
            this.addBlock(world, blockState8, 4, 3, 11, blockBox);
            this.addBlock(world, blockState8, 5, 3, 11, blockBox);
            this.addBlock(world, blockState8, 6, 3, 11, blockBox);
         }

         if (!this.spawnerPlaced) {
            int i = this.applyYTransform(3);
            BlockPos blockPos = new BlockPos(this.applyXTransform(5, 6), i, this.applyZTransform(5, 6));
            if (blockBox.contains(blockPos)) {
               this.spawnerPlaced = true;
               world.setBlockState(blockPos, Blocks.SPAWNER.getDefaultState(), 2);
               BlockEntity blockEntity = world.getBlockEntity(blockPos);
               if (blockEntity instanceof MobSpawnerBlockEntity) {
                  ((MobSpawnerBlockEntity)blockEntity).getLogic().setEntityId(EntityType.SILVERFISH);
               }
            }
         }

         return true;
      }
   }

   public static class FiveWayCrossing extends StrongholdGenerator.Piece {
      private final boolean lowerLeftExists;
      private final boolean upperLeftExists;
      private final boolean lowerRightExists;
      private final boolean upperRightExists;

      public FiveWayCrossing(int i, Random random, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_FIVE_WAY_CROSSING, i);
         this.setOrientation(direction);
         this.entryDoor = this.getRandomEntrance(random);
         this.boundingBox = blockBox;
         this.lowerLeftExists = random.nextBoolean();
         this.upperLeftExists = random.nextBoolean();
         this.lowerRightExists = random.nextBoolean();
         this.upperRightExists = random.nextInt(3) > 0;
      }

      public FiveWayCrossing(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.STRONGHOLD_FIVE_WAY_CROSSING, compoundTag);
         this.lowerLeftExists = compoundTag.getBoolean("leftLow");
         this.upperLeftExists = compoundTag.getBoolean("leftHigh");
         this.lowerRightExists = compoundTag.getBoolean("rightLow");
         this.upperRightExists = compoundTag.getBoolean("rightHigh");
      }

      protected void toNbt(CompoundTag tag) {
         super.toNbt(tag);
         tag.putBoolean("leftLow", this.lowerLeftExists);
         tag.putBoolean("leftHigh", this.upperLeftExists);
         tag.putBoolean("rightLow", this.lowerRightExists);
         tag.putBoolean("rightHigh", this.upperRightExists);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         int i = 3;
         int j = 5;
         Direction direction = this.getFacing();
         if (direction == Direction.WEST || direction == Direction.NORTH) {
            i = 8 - i;
            j = 8 - j;
         }

         this.method_14874((StrongholdGenerator.Start)structurePiece, list, random, 5, 1);
         if (this.lowerLeftExists) {
            this.method_14870((StrongholdGenerator.Start)structurePiece, list, random, i, 1);
         }

         if (this.upperLeftExists) {
            this.method_14870((StrongholdGenerator.Start)structurePiece, list, random, j, 7);
         }

         if (this.lowerRightExists) {
            this.method_14873((StrongholdGenerator.Start)structurePiece, list, random, i, 1);
         }

         if (this.upperRightExists) {
            this.method_14873((StrongholdGenerator.Start)structurePiece, list, random, j, 7);
         }

      }

      public static StrongholdGenerator.FiveWayCrossing method_14858(List<StructurePiece> list, Random random, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -4, -3, 0, 10, 9, 11, direction);
         return method_14871(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new StrongholdGenerator.FiveWayCrossing(l, random, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         this.fillWithOutline(world, blockBox, 0, 0, 0, 9, 8, 10, true, random, StrongholdGenerator.field_15263);
         this.generateEntrance(world, random, blockBox, this.entryDoor, 4, 3, 0);
         if (this.lowerLeftExists) {
            this.fillWithOutline(world, blockBox, 0, 3, 1, 0, 5, 3, AIR, AIR, false);
         }

         if (this.lowerRightExists) {
            this.fillWithOutline(world, blockBox, 9, 3, 1, 9, 5, 3, AIR, AIR, false);
         }

         if (this.upperLeftExists) {
            this.fillWithOutline(world, blockBox, 0, 5, 7, 0, 7, 9, AIR, AIR, false);
         }

         if (this.upperRightExists) {
            this.fillWithOutline(world, blockBox, 9, 5, 7, 9, 7, 9, AIR, AIR, false);
         }

         this.fillWithOutline(world, blockBox, 5, 1, 10, 7, 3, 10, AIR, AIR, false);
         this.fillWithOutline(world, blockBox, 1, 2, 1, 8, 2, 6, false, random, StrongholdGenerator.field_15263);
         this.fillWithOutline(world, blockBox, 4, 1, 5, 4, 4, 9, false, random, StrongholdGenerator.field_15263);
         this.fillWithOutline(world, blockBox, 8, 1, 5, 8, 4, 9, false, random, StrongholdGenerator.field_15263);
         this.fillWithOutline(world, blockBox, 1, 4, 7, 3, 4, 9, false, random, StrongholdGenerator.field_15263);
         this.fillWithOutline(world, blockBox, 1, 3, 5, 3, 3, 6, false, random, StrongholdGenerator.field_15263);
         this.fillWithOutline(world, blockBox, 1, 3, 4, 3, 3, 4, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 1, 4, 6, 3, 4, 6, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 5, 1, 7, 7, 1, 8, false, random, StrongholdGenerator.field_15263);
         this.fillWithOutline(world, blockBox, 5, 1, 9, 7, 1, 9, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 5, 2, 7, 7, 2, 7, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 4, 5, 7, 4, 5, 9, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 8, 5, 7, 8, 5, 9, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
         this.fillWithOutline(world, blockBox, 5, 5, 7, 7, 5, 9, (BlockState)Blocks.SMOOTH_STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE), (BlockState)Blocks.SMOOTH_STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE), false);
         this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.SOUTH), 6, 5, 6, blockBox);
         return true;
      }
   }

   public static class Library extends StrongholdGenerator.Piece {
      private final boolean tall;

      public Library(int i, Random random, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_LIBRARY, i);
         this.setOrientation(direction);
         this.entryDoor = this.getRandomEntrance(random);
         this.boundingBox = blockBox;
         this.tall = blockBox.getBlockCountY() > 6;
      }

      public Library(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.STRONGHOLD_LIBRARY, compoundTag);
         this.tall = compoundTag.getBoolean("Tall");
      }

      protected void toNbt(CompoundTag tag) {
         super.toNbt(tag);
         tag.putBoolean("Tall", this.tall);
      }

      public static StrongholdGenerator.Library method_14860(List<StructurePiece> list, Random random, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -4, -1, 0, 14, 11, 15, direction);
         if (!method_14871(blockBox) || StructurePiece.method_14932(list, blockBox) != null) {
            blockBox = BlockBox.rotated(i, j, k, -4, -1, 0, 14, 6, 15, direction);
            if (!method_14871(blockBox) || StructurePiece.method_14932(list, blockBox) != null) {
               return null;
            }
         }

         return new StrongholdGenerator.Library(l, random, blockBox, direction);
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         int i = 11;
         if (!this.tall) {
            i = 6;
         }

         this.fillWithOutline(world, blockBox, 0, 0, 0, 13, i - 1, 14, true, random, StrongholdGenerator.field_15263);
         this.generateEntrance(world, random, blockBox, this.entryDoor, 4, 1, 0);
         this.fillWithOutlineUnderSealevel(world, blockBox, random, 0.07F, 2, 1, 1, 11, 4, 13, Blocks.COBWEB.getDefaultState(), Blocks.COBWEB.getDefaultState(), false, false);
         int j = true;
         int k = true;

         int l;
         for(l = 1; l <= 13; ++l) {
            if ((l - 1) % 4 == 0) {
               this.fillWithOutline(world, blockBox, 1, 1, l, 1, 4, l, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
               this.fillWithOutline(world, blockBox, 12, 1, l, 12, 4, l, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
               this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.EAST), 2, 3, l, blockBox);
               this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.WEST), 11, 3, l, blockBox);
               if (this.tall) {
                  this.fillWithOutline(world, blockBox, 1, 6, l, 1, 9, l, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
                  this.fillWithOutline(world, blockBox, 12, 6, l, 12, 9, l, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
               }
            } else {
               this.fillWithOutline(world, blockBox, 1, 1, l, 1, 4, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
               this.fillWithOutline(world, blockBox, 12, 1, l, 12, 4, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
               if (this.tall) {
                  this.fillWithOutline(world, blockBox, 1, 6, l, 1, 9, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
                  this.fillWithOutline(world, blockBox, 12, 6, l, 12, 9, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
               }
            }
         }

         for(l = 3; l < 12; l += 2) {
            this.fillWithOutline(world, blockBox, 3, 1, l, 4, 3, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
            this.fillWithOutline(world, blockBox, 6, 1, l, 7, 3, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
            this.fillWithOutline(world, blockBox, 9, 1, l, 10, 3, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
         }

         if (this.tall) {
            this.fillWithOutline(world, blockBox, 1, 5, 1, 3, 5, 13, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
            this.fillWithOutline(world, blockBox, 10, 5, 1, 12, 5, 13, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
            this.fillWithOutline(world, blockBox, 4, 5, 1, 9, 5, 2, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
            this.fillWithOutline(world, blockBox, 4, 5, 12, 9, 5, 13, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
            this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 9, 5, 11, blockBox);
            this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 8, 5, 11, blockBox);
            this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 9, 5, 10, blockBox);
            BlockState blockState = (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
            BlockState blockState2 = (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
            this.fillWithOutline(world, blockBox, 3, 6, 3, 3, 6, 11, blockState2, blockState2, false);
            this.fillWithOutline(world, blockBox, 10, 6, 3, 10, 6, 9, blockState2, blockState2, false);
            this.fillWithOutline(world, blockBox, 4, 6, 2, 9, 6, 2, blockState, blockState, false);
            this.fillWithOutline(world, blockBox, 4, 6, 12, 7, 6, 12, blockState, blockState, false);
            this.addBlock(world, (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.EAST, true), 3, 6, 2, blockBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.SOUTH, true)).with(FenceBlock.EAST, true), 3, 6, 12, blockBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.WEST, true), 10, 6, 2, blockBox);

            for(int n = 0; n <= 2; ++n) {
               this.addBlock(world, (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.SOUTH, true)).with(FenceBlock.WEST, true), 8 + n, 6, 12 - n, blockBox);
               if (n != 2) {
                  this.addBlock(world, (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.EAST, true), 8 + n, 6, 11 - n, blockBox);
               }
            }

            BlockState blockState3 = (BlockState)Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.SOUTH);
            this.addBlock(world, blockState3, 10, 1, 13, blockBox);
            this.addBlock(world, blockState3, 10, 2, 13, blockBox);
            this.addBlock(world, blockState3, 10, 3, 13, blockBox);
            this.addBlock(world, blockState3, 10, 4, 13, blockBox);
            this.addBlock(world, blockState3, 10, 5, 13, blockBox);
            this.addBlock(world, blockState3, 10, 6, 13, blockBox);
            this.addBlock(world, blockState3, 10, 7, 13, blockBox);
            int o = true;
            int p = true;
            BlockState blockState4 = (BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.EAST, true);
            this.addBlock(world, blockState4, 6, 9, 7, blockBox);
            BlockState blockState5 = (BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.WEST, true);
            this.addBlock(world, blockState5, 7, 9, 7, blockBox);
            this.addBlock(world, blockState4, 6, 8, 7, blockBox);
            this.addBlock(world, blockState5, 7, 8, 7, blockBox);
            BlockState blockState6 = (BlockState)((BlockState)blockState2.with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
            this.addBlock(world, blockState6, 6, 7, 7, blockBox);
            this.addBlock(world, blockState6, 7, 7, 7, blockBox);
            this.addBlock(world, blockState4, 5, 7, 7, blockBox);
            this.addBlock(world, blockState5, 8, 7, 7, blockBox);
            this.addBlock(world, (BlockState)blockState4.with(FenceBlock.NORTH, true), 6, 7, 6, blockBox);
            this.addBlock(world, (BlockState)blockState4.with(FenceBlock.SOUTH, true), 6, 7, 8, blockBox);
            this.addBlock(world, (BlockState)blockState5.with(FenceBlock.NORTH, true), 7, 7, 6, blockBox);
            this.addBlock(world, (BlockState)blockState5.with(FenceBlock.SOUTH, true), 7, 7, 8, blockBox);
            BlockState blockState7 = Blocks.TORCH.getDefaultState();
            this.addBlock(world, blockState7, 5, 8, 7, blockBox);
            this.addBlock(world, blockState7, 8, 8, 7, blockBox);
            this.addBlock(world, blockState7, 6, 8, 6, blockBox);
            this.addBlock(world, blockState7, 6, 8, 8, blockBox);
            this.addBlock(world, blockState7, 7, 8, 6, blockBox);
            this.addBlock(world, blockState7, 7, 8, 8, blockBox);
         }

         this.addChest(world, blockBox, random, 3, 3, 5, LootTables.STRONGHOLD_LIBRARY_CHEST);
         if (this.tall) {
            this.addBlock(world, AIR, 12, 9, 1, blockBox);
            this.addChest(world, blockBox, random, 12, 8, 1, LootTables.STRONGHOLD_LIBRARY_CHEST);
         }

         return true;
      }
   }

   public static class PrisonHall extends StrongholdGenerator.Piece {
      public PrisonHall(int i, Random random, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_PRISON_HALL, i);
         this.setOrientation(direction);
         this.entryDoor = this.getRandomEntrance(random);
         this.boundingBox = blockBox;
      }

      public PrisonHall(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.STRONGHOLD_PRISON_HALL, compoundTag);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         this.method_14874((StrongholdGenerator.Start)structurePiece, list, random, 1, 1);
      }

      public static StrongholdGenerator.PrisonHall method_14864(List<StructurePiece> list, Random random, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -1, -1, 0, 9, 5, 11, direction);
         return method_14871(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new StrongholdGenerator.PrisonHall(l, random, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         this.fillWithOutline(world, blockBox, 0, 0, 0, 8, 4, 10, true, random, StrongholdGenerator.field_15263);
         this.generateEntrance(world, random, blockBox, this.entryDoor, 1, 1, 0);
         this.fillWithOutline(world, blockBox, 1, 1, 10, 3, 3, 10, AIR, AIR, false);
         this.fillWithOutline(world, blockBox, 4, 1, 1, 4, 3, 1, false, random, StrongholdGenerator.field_15263);
         this.fillWithOutline(world, blockBox, 4, 1, 3, 4, 3, 3, false, random, StrongholdGenerator.field_15263);
         this.fillWithOutline(world, blockBox, 4, 1, 7, 4, 3, 7, false, random, StrongholdGenerator.field_15263);
         this.fillWithOutline(world, blockBox, 4, 1, 9, 4, 3, 9, false, random, StrongholdGenerator.field_15263);

         for(int i = 1; i <= 3; ++i) {
            this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true), 4, i, 4, blockBox);
            this.addBlock(world, (BlockState)((BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true)).with(PaneBlock.EAST, true), 4, i, 5, blockBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true), 4, i, 6, blockBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true)).with(PaneBlock.EAST, true), 5, i, 5, blockBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true)).with(PaneBlock.EAST, true), 6, i, 5, blockBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true)).with(PaneBlock.EAST, true), 7, i, 5, blockBox);
         }

         this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true), 4, 3, 2, blockBox);
         this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true), 4, 3, 8, blockBox);
         BlockState blockState = (BlockState)Blocks.IRON_DOOR.getDefaultState().with(DoorBlock.FACING, Direction.WEST);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.IRON_DOOR.getDefaultState().with(DoorBlock.FACING, Direction.WEST)).with(DoorBlock.HALF, DoubleBlockHalf.UPPER);
         this.addBlock(world, blockState, 4, 1, 2, blockBox);
         this.addBlock(world, blockState2, 4, 2, 2, blockBox);
         this.addBlock(world, blockState, 4, 1, 8, blockBox);
         this.addBlock(world, blockState2, 4, 2, 8, blockBox);
         return true;
      }
   }

   public static class SquareRoom extends StrongholdGenerator.Piece {
      protected final int roomType;

      public SquareRoom(int i, Random random, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_SQUARE_ROOM, i);
         this.setOrientation(direction);
         this.entryDoor = this.getRandomEntrance(random);
         this.boundingBox = blockBox;
         this.roomType = random.nextInt(5);
      }

      public SquareRoom(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.STRONGHOLD_SQUARE_ROOM, compoundTag);
         this.roomType = compoundTag.getInt("Type");
      }

      protected void toNbt(CompoundTag tag) {
         super.toNbt(tag);
         tag.putInt("Type", this.roomType);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         this.method_14874((StrongholdGenerator.Start)structurePiece, list, random, 4, 1);
         this.method_14870((StrongholdGenerator.Start)structurePiece, list, random, 1, 4);
         this.method_14873((StrongholdGenerator.Start)structurePiece, list, random, 1, 4);
      }

      public static StrongholdGenerator.SquareRoom method_14865(List<StructurePiece> list, Random random, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -4, -1, 0, 11, 7, 11, direction);
         return method_14871(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new StrongholdGenerator.SquareRoom(l, random, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         this.fillWithOutline(world, blockBox, 0, 0, 0, 10, 6, 10, true, random, StrongholdGenerator.field_15263);
         this.generateEntrance(world, random, blockBox, this.entryDoor, 4, 1, 0);
         this.fillWithOutline(world, blockBox, 4, 1, 10, 6, 3, 10, AIR, AIR, false);
         this.fillWithOutline(world, blockBox, 0, 1, 4, 0, 3, 6, AIR, AIR, false);
         this.fillWithOutline(world, blockBox, 10, 1, 4, 10, 3, 6, AIR, AIR, false);
         int m;
         switch(this.roomType) {
         case 0:
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 1, 5, blockBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 2, 5, blockBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 3, 5, blockBox);
            this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.WEST), 4, 3, 5, blockBox);
            this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.EAST), 6, 3, 5, blockBox);
            this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.SOUTH), 5, 3, 4, blockBox);
            this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.NORTH), 5, 3, 6, blockBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 4, 1, 4, blockBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 4, 1, 5, blockBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 4, 1, 6, blockBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 6, 1, 4, blockBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 6, 1, 5, blockBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 6, 1, 6, blockBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 5, 1, 4, blockBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 5, 1, 6, blockBox);
            break;
         case 1:
            for(m = 0; m < 5; ++m) {
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 1, 3 + m, blockBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 7, 1, 3 + m, blockBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3 + m, 1, 3, blockBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3 + m, 1, 7, blockBox);
            }

            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 1, 5, blockBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 2, 5, blockBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 3, 5, blockBox);
            this.addBlock(world, Blocks.WATER.getDefaultState(), 5, 4, 5, blockBox);
            break;
         case 2:
            for(m = 1; m <= 9; ++m) {
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 1, 3, m, blockBox);
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 9, 3, m, blockBox);
            }

            for(m = 1; m <= 9; ++m) {
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), m, 3, 1, blockBox);
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), m, 3, 9, blockBox);
            }

            this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 5, 1, 4, blockBox);
            this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 5, 1, 6, blockBox);
            this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 5, 3, 4, blockBox);
            this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 5, 3, 6, blockBox);
            this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 4, 1, 5, blockBox);
            this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 6, 1, 5, blockBox);
            this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 4, 3, 5, blockBox);
            this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 6, 3, 5, blockBox);

            for(m = 1; m <= 3; ++m) {
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 4, m, 4, blockBox);
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 6, m, 4, blockBox);
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 4, m, 6, blockBox);
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 6, m, 6, blockBox);
            }

            this.addBlock(world, Blocks.TORCH.getDefaultState(), 5, 3, 5, blockBox);

            for(m = 2; m <= 8; ++m) {
               this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 2, 3, m, blockBox);
               this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 3, 3, m, blockBox);
               if (m <= 3 || m >= 7) {
                  this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 4, 3, m, blockBox);
                  this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 5, 3, m, blockBox);
                  this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 6, 3, m, blockBox);
               }

               this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 7, 3, m, blockBox);
               this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 8, 3, m, blockBox);
            }

            BlockState blockState = (BlockState)Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.WEST);
            this.addBlock(world, blockState, 9, 1, 3, blockBox);
            this.addBlock(world, blockState, 9, 2, 3, blockBox);
            this.addBlock(world, blockState, 9, 3, 3, blockBox);
            this.addChest(world, blockBox, random, 3, 4, 8, LootTables.STRONGHOLD_CROSSING_CHEST);
         }

         return true;
      }
   }

   public static class RightTurn extends StrongholdGenerator.class_3466 {
      public RightTurn(int i, Random random, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_RIGHT_TURN, i);
         this.setOrientation(direction);
         this.entryDoor = this.getRandomEntrance(random);
         this.boundingBox = blockBox;
      }

      public RightTurn(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.STRONGHOLD_RIGHT_TURN, compoundTag);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         Direction direction = this.getFacing();
         if (direction != Direction.NORTH && direction != Direction.EAST) {
            this.method_14870((StrongholdGenerator.Start)structurePiece, list, random, 1, 1);
         } else {
            this.method_14873((StrongholdGenerator.Start)structurePiece, list, random, 1, 1);
         }

      }

      public static StrongholdGenerator.RightTurn method_16652(List<StructurePiece> list, Random random, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -1, -1, 0, 5, 5, 5, direction);
         return method_14871(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new StrongholdGenerator.RightTurn(l, random, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         this.fillWithOutline(world, blockBox, 0, 0, 0, 4, 4, 4, true, random, StrongholdGenerator.field_15263);
         this.generateEntrance(world, random, blockBox, this.entryDoor, 1, 1, 0);
         Direction direction = this.getFacing();
         if (direction != Direction.NORTH && direction != Direction.EAST) {
            this.fillWithOutline(world, blockBox, 0, 1, 1, 0, 3, 3, AIR, AIR, false);
         } else {
            this.fillWithOutline(world, blockBox, 4, 1, 1, 4, 3, 3, AIR, AIR, false);
         }

         return true;
      }
   }

   public static class LeftTurn extends StrongholdGenerator.class_3466 {
      public LeftTurn(int i, Random random, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_LEFT_TURN, i);
         this.setOrientation(direction);
         this.entryDoor = this.getRandomEntrance(random);
         this.boundingBox = blockBox;
      }

      public LeftTurn(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.STRONGHOLD_LEFT_TURN, compoundTag);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         Direction direction = this.getFacing();
         if (direction != Direction.NORTH && direction != Direction.EAST) {
            this.method_14873((StrongholdGenerator.Start)structurePiece, list, random, 1, 1);
         } else {
            this.method_14870((StrongholdGenerator.Start)structurePiece, list, random, 1, 1);
         }

      }

      public static StrongholdGenerator.LeftTurn method_14859(List<StructurePiece> list, Random random, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -1, -1, 0, 5, 5, 5, direction);
         return method_14871(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new StrongholdGenerator.LeftTurn(l, random, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         this.fillWithOutline(world, blockBox, 0, 0, 0, 4, 4, 4, true, random, StrongholdGenerator.field_15263);
         this.generateEntrance(world, random, blockBox, this.entryDoor, 1, 1, 0);
         Direction direction = this.getFacing();
         if (direction != Direction.NORTH && direction != Direction.EAST) {
            this.fillWithOutline(world, blockBox, 4, 1, 1, 4, 3, 3, AIR, AIR, false);
         } else {
            this.fillWithOutline(world, blockBox, 0, 1, 1, 0, 3, 3, AIR, AIR, false);
         }

         return true;
      }
   }

   public abstract static class class_3466 extends StrongholdGenerator.Piece {
      protected class_3466(StructurePieceType structurePieceType, int i) {
         super(structurePieceType, i);
      }

      public class_3466(StructurePieceType structurePieceType, CompoundTag compoundTag) {
         super(structurePieceType, compoundTag);
      }
   }

   public static class Stairs extends StrongholdGenerator.Piece {
      public Stairs(int i, Random random, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_STAIRS, i);
         this.setOrientation(direction);
         this.entryDoor = this.getRandomEntrance(random);
         this.boundingBox = blockBox;
      }

      public Stairs(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.STRONGHOLD_STAIRS, compoundTag);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         this.method_14874((StrongholdGenerator.Start)structurePiece, list, random, 1, 1);
      }

      public static StrongholdGenerator.Stairs method_14868(List<StructurePiece> list, Random random, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -1, -7, 0, 5, 11, 8, direction);
         return method_14871(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new StrongholdGenerator.Stairs(l, random, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         this.fillWithOutline(world, blockBox, 0, 0, 0, 4, 10, 7, true, random, StrongholdGenerator.field_15263);
         this.generateEntrance(world, random, blockBox, this.entryDoor, 1, 7, 0);
         this.generateEntrance(world, random, blockBox, StrongholdGenerator.Piece.EntranceType.OPENING, 1, 1, 7);
         BlockState blockState = (BlockState)Blocks.COBBLESTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH);

         for(int i = 0; i < 6; ++i) {
            this.addBlock(world, blockState, 1, 6 - i, 1 + i, blockBox);
            this.addBlock(world, blockState, 2, 6 - i, 1 + i, blockBox);
            this.addBlock(world, blockState, 3, 6 - i, 1 + i, blockBox);
            if (i < 5) {
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 5 - i, 1 + i, blockBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 5 - i, 1 + i, blockBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 5 - i, 1 + i, blockBox);
            }
         }

         return true;
      }
   }

   public static class ChestCorridor extends StrongholdGenerator.Piece {
      private boolean chestGenerated;

      public ChestCorridor(int i, Random random, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_CHEST_CORRIDOR, i);
         this.setOrientation(direction);
         this.entryDoor = this.getRandomEntrance(random);
         this.boundingBox = blockBox;
      }

      public ChestCorridor(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.STRONGHOLD_CHEST_CORRIDOR, compoundTag);
         this.chestGenerated = compoundTag.getBoolean("Chest");
      }

      protected void toNbt(CompoundTag tag) {
         super.toNbt(tag);
         tag.putBoolean("Chest", this.chestGenerated);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         this.method_14874((StrongholdGenerator.Start)structurePiece, list, random, 1, 1);
      }

      public static StrongholdGenerator.ChestCorridor method_14856(List<StructurePiece> list, Random random, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -1, -1, 0, 5, 5, 7, direction);
         return method_14871(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new StrongholdGenerator.ChestCorridor(l, random, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         this.fillWithOutline(world, blockBox, 0, 0, 0, 4, 4, 6, true, random, StrongholdGenerator.field_15263);
         this.generateEntrance(world, random, blockBox, this.entryDoor, 1, 1, 0);
         this.generateEntrance(world, random, blockBox, StrongholdGenerator.Piece.EntranceType.OPENING, 1, 1, 6);
         this.fillWithOutline(world, blockBox, 3, 1, 2, 3, 1, 4, Blocks.STONE_BRICKS.getDefaultState(), Blocks.STONE_BRICKS.getDefaultState(), false);
         this.addBlock(world, Blocks.STONE_BRICK_SLAB.getDefaultState(), 3, 1, 1, blockBox);
         this.addBlock(world, Blocks.STONE_BRICK_SLAB.getDefaultState(), 3, 1, 5, blockBox);
         this.addBlock(world, Blocks.STONE_BRICK_SLAB.getDefaultState(), 3, 2, 2, blockBox);
         this.addBlock(world, Blocks.STONE_BRICK_SLAB.getDefaultState(), 3, 2, 4, blockBox);

         for(int i = 2; i <= 4; ++i) {
            this.addBlock(world, Blocks.STONE_BRICK_SLAB.getDefaultState(), 2, 1, i, blockBox);
         }

         if (!this.chestGenerated && blockBox.contains(new BlockPos(this.applyXTransform(3, 3), this.applyYTransform(2), this.applyZTransform(3, 3)))) {
            this.chestGenerated = true;
            this.addChest(world, blockBox, random, 3, 2, 3, LootTables.STRONGHOLD_CORRIDOR_CHEST);
         }

         return true;
      }
   }

   public static class Corridor extends StrongholdGenerator.Piece {
      private final boolean leftExitExists;
      private final boolean rightExitExixts;

      public Corridor(int i, Random random, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_CORRIDOR, i);
         this.setOrientation(direction);
         this.entryDoor = this.getRandomEntrance(random);
         this.boundingBox = blockBox;
         this.leftExitExists = random.nextInt(2) == 0;
         this.rightExitExixts = random.nextInt(2) == 0;
      }

      public Corridor(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.STRONGHOLD_CORRIDOR, compoundTag);
         this.leftExitExists = compoundTag.getBoolean("Left");
         this.rightExitExixts = compoundTag.getBoolean("Right");
      }

      protected void toNbt(CompoundTag tag) {
         super.toNbt(tag);
         tag.putBoolean("Left", this.leftExitExists);
         tag.putBoolean("Right", this.rightExitExixts);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         this.method_14874((StrongholdGenerator.Start)structurePiece, list, random, 1, 1);
         if (this.leftExitExists) {
            this.method_14870((StrongholdGenerator.Start)structurePiece, list, random, 1, 2);
         }

         if (this.rightExitExixts) {
            this.method_14873((StrongholdGenerator.Start)structurePiece, list, random, 1, 2);
         }

      }

      public static StrongholdGenerator.Corridor method_14867(List<StructurePiece> list, Random random, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -1, -1, 0, 5, 5, 7, direction);
         return method_14871(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new StrongholdGenerator.Corridor(l, random, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         this.fillWithOutline(world, blockBox, 0, 0, 0, 4, 4, 6, true, random, StrongholdGenerator.field_15263);
         this.generateEntrance(world, random, blockBox, this.entryDoor, 1, 1, 0);
         this.generateEntrance(world, random, blockBox, StrongholdGenerator.Piece.EntranceType.OPENING, 1, 1, 6);
         BlockState blockState = (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.EAST);
         BlockState blockState2 = (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.WEST);
         this.addBlockWithRandomThreshold(world, blockBox, random, 0.1F, 1, 2, 1, blockState);
         this.addBlockWithRandomThreshold(world, blockBox, random, 0.1F, 3, 2, 1, blockState2);
         this.addBlockWithRandomThreshold(world, blockBox, random, 0.1F, 1, 2, 5, blockState);
         this.addBlockWithRandomThreshold(world, blockBox, random, 0.1F, 3, 2, 5, blockState2);
         if (this.leftExitExists) {
            this.fillWithOutline(world, blockBox, 0, 1, 2, 0, 3, 4, AIR, AIR, false);
         }

         if (this.rightExitExixts) {
            this.fillWithOutline(world, blockBox, 4, 1, 2, 4, 3, 4, AIR, AIR, false);
         }

         return true;
      }
   }

   public static class Start extends StrongholdGenerator.SpiralStaircase {
      public StrongholdGenerator.class_3427 field_15284;
      @Nullable
      public StrongholdGenerator.PortalRoom field_15283;
      public final List<StructurePiece> field_15282 = Lists.newArrayList();

      public Start(Random random, int i, int j) {
         super(StructurePieceType.STRONGHOLD_START, 0, random, i, j);
      }

      public Start(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.STRONGHOLD_START, compoundTag);
      }
   }

   public static class SpiralStaircase extends StrongholdGenerator.Piece {
      private final boolean isStructureStart;

      public SpiralStaircase(StructurePieceType structurePieceType, int i, Random random, int j, int k) {
         super(structurePieceType, i);
         this.isStructureStart = true;
         this.setOrientation(Direction.Type.HORIZONTAL.random(random));
         this.entryDoor = StrongholdGenerator.Piece.EntranceType.OPENING;
         if (this.getFacing().getAxis() == Direction.Axis.Z) {
            this.boundingBox = new BlockBox(j, 64, k, j + 5 - 1, 74, k + 5 - 1);
         } else {
            this.boundingBox = new BlockBox(j, 64, k, j + 5 - 1, 74, k + 5 - 1);
         }

      }

      public SpiralStaircase(int i, Random random, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_SPIRAL_STAIRCASE, i);
         this.isStructureStart = false;
         this.setOrientation(direction);
         this.entryDoor = this.getRandomEntrance(random);
         this.boundingBox = blockBox;
      }

      public SpiralStaircase(StructurePieceType structurePieceType, CompoundTag compoundTag) {
         super(structurePieceType, compoundTag);
         this.isStructureStart = compoundTag.getBoolean("Source");
      }

      public SpiralStaircase(StructureManager structureManager, CompoundTag compoundTag) {
         this(StructurePieceType.STRONGHOLD_SPIRAL_STAIRCASE, compoundTag);
      }

      protected void toNbt(CompoundTag tag) {
         super.toNbt(tag);
         tag.putBoolean("Source", this.isStructureStart);
      }

      public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
         if (this.isStructureStart) {
            StrongholdGenerator.field_15266 = StrongholdGenerator.FiveWayCrossing.class;
         }

         this.method_14874((StrongholdGenerator.Start)structurePiece, list, random, 1, 1);
      }

      public static StrongholdGenerator.SpiralStaircase method_14866(List<StructurePiece> list, Random random, int i, int j, int k, Direction direction, int l) {
         BlockBox blockBox = BlockBox.rotated(i, j, k, -1, -7, 0, 5, 11, 5, direction);
         return method_14871(blockBox) && StructurePiece.method_14932(list, blockBox) == null ? new StrongholdGenerator.SpiralStaircase(l, random, blockBox, direction) : null;
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         this.fillWithOutline(world, blockBox, 0, 0, 0, 4, 10, 4, true, random, StrongholdGenerator.field_15263);
         this.generateEntrance(world, random, blockBox, this.entryDoor, 1, 7, 0);
         this.generateEntrance(world, random, blockBox, StrongholdGenerator.Piece.EntranceType.OPENING, 1, 1, 4);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 6, 1, blockBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 5, 1, blockBox);
         this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 1, 6, 1, blockBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 5, 2, blockBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 4, 3, blockBox);
         this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 1, 5, 3, blockBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 4, 3, blockBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 3, 3, blockBox);
         this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 3, 4, 3, blockBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 3, 2, blockBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 2, 1, blockBox);
         this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 3, 3, 1, blockBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 2, 1, blockBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 1, 1, blockBox);
         this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 1, 2, 1, blockBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 1, 2, blockBox);
         this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 1, 1, 3, blockBox);
         return true;
      }
   }

   public static class SmallCorridor extends StrongholdGenerator.Piece {
      private final int length;

      public SmallCorridor(int i, BlockBox blockBox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_SMALL_CORRIDOR, i);
         this.setOrientation(direction);
         this.boundingBox = blockBox;
         this.length = direction != Direction.NORTH && direction != Direction.SOUTH ? blockBox.getBlockCountX() : blockBox.getBlockCountZ();
      }

      public SmallCorridor(StructureManager structureManager, CompoundTag compoundTag) {
         super(StructurePieceType.STRONGHOLD_SMALL_CORRIDOR, compoundTag);
         this.length = compoundTag.getInt("Steps");
      }

      protected void toNbt(CompoundTag tag) {
         super.toNbt(tag);
         tag.putInt("Steps", this.length);
      }

      public static BlockBox method_14857(List<StructurePiece> list, Random random, int i, int j, int k, Direction direction) {
         int l = true;
         BlockBox blockBox = BlockBox.rotated(i, j, k, -1, -1, 0, 5, 5, 4, direction);
         StructurePiece structurePiece = StructurePiece.method_14932(list, blockBox);
         if (structurePiece == null) {
            return null;
         } else {
            if (structurePiece.getBoundingBox().minY == blockBox.minY) {
               for(int m = 3; m >= 1; --m) {
                  blockBox = BlockBox.rotated(i, j, k, -1, -1, 0, 5, 5, m - 1, direction);
                  if (!structurePiece.getBoundingBox().intersects(blockBox)) {
                     return BlockBox.rotated(i, j, k, -1, -1, 0, 5, 5, m, direction);
                  }
               }
            }

            return null;
         }
      }

      public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
         for(int i = 0; i < this.length; ++i) {
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 0, 0, i, blockBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 0, i, blockBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 0, i, blockBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 0, i, blockBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 4, 0, i, blockBox);

            for(int j = 1; j <= 3; ++j) {
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 0, j, i, blockBox);
               this.addBlock(world, Blocks.CAVE_AIR.getDefaultState(), 1, j, i, blockBox);
               this.addBlock(world, Blocks.CAVE_AIR.getDefaultState(), 2, j, i, blockBox);
               this.addBlock(world, Blocks.CAVE_AIR.getDefaultState(), 3, j, i, blockBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 4, j, i, blockBox);
            }

            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 0, 4, i, blockBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 4, i, blockBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 4, i, blockBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 4, i, blockBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 4, 4, i, blockBox);
         }

         return true;
      }
   }

   abstract static class Piece extends StructurePiece {
      protected StrongholdGenerator.Piece.EntranceType entryDoor;

      protected Piece(StructurePieceType structurePieceType, int i) {
         super(structurePieceType, i);
         this.entryDoor = StrongholdGenerator.Piece.EntranceType.OPENING;
      }

      public Piece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
         super(structurePieceType, compoundTag);
         this.entryDoor = StrongholdGenerator.Piece.EntranceType.OPENING;
         this.entryDoor = StrongholdGenerator.Piece.EntranceType.valueOf(compoundTag.getString("EntryDoor"));
      }

      protected void toNbt(CompoundTag tag) {
         tag.putString("EntryDoor", this.entryDoor.name());
      }

      protected void generateEntrance(IWorld world, Random random, BlockBox boundingBox, StrongholdGenerator.Piece.EntranceType type, int x, int y, int z) {
         switch(type) {
         case OPENING:
            this.fillWithOutline(world, boundingBox, x, y, z, x + 3 - 1, y + 3 - 1, z, AIR, AIR, false);
            break;
         case WOOD_DOOR:
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x, y, z, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x, y + 1, z, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x, y + 2, z, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 1, y + 2, z, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y + 2, z, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y + 1, z, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y, z, boundingBox);
            this.addBlock(world, Blocks.OAK_DOOR.getDefaultState(), x + 1, y, z, boundingBox);
            this.addBlock(world, (BlockState)Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER), x + 1, y + 1, z, boundingBox);
            break;
         case GRATES:
            this.addBlock(world, Blocks.CAVE_AIR.getDefaultState(), x + 1, y, z, boundingBox);
            this.addBlock(world, Blocks.CAVE_AIR.getDefaultState(), x + 1, y + 1, z, boundingBox);
            this.addBlock(world, (BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true), x, y, z, boundingBox);
            this.addBlock(world, (BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true), x, y + 1, z, boundingBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.EAST, true)).with(PaneBlock.WEST, true), x, y + 2, z, boundingBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.EAST, true)).with(PaneBlock.WEST, true), x + 1, y + 2, z, boundingBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.EAST, true)).with(PaneBlock.WEST, true), x + 2, y + 2, z, boundingBox);
            this.addBlock(world, (BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.EAST, true), x + 2, y + 1, z, boundingBox);
            this.addBlock(world, (BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.EAST, true), x + 2, y, z, boundingBox);
            break;
         case IRON_DOOR:
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x, y, z, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x, y + 1, z, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x, y + 2, z, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 1, y + 2, z, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y + 2, z, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y + 1, z, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y, z, boundingBox);
            this.addBlock(world, Blocks.IRON_DOOR.getDefaultState(), x + 1, y, z, boundingBox);
            this.addBlock(world, (BlockState)Blocks.IRON_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER), x + 1, y + 1, z, boundingBox);
            this.addBlock(world, (BlockState)Blocks.STONE_BUTTON.getDefaultState().with(AbstractButtonBlock.FACING, Direction.NORTH), x + 2, y + 1, z + 1, boundingBox);
            this.addBlock(world, (BlockState)Blocks.STONE_BUTTON.getDefaultState().with(AbstractButtonBlock.FACING, Direction.SOUTH), x + 2, y + 1, z - 1, boundingBox);
         }

      }

      protected StrongholdGenerator.Piece.EntranceType getRandomEntrance(Random random) {
         int i = random.nextInt(5);
         switch(i) {
         case 0:
         case 1:
         default:
            return StrongholdGenerator.Piece.EntranceType.OPENING;
         case 2:
            return StrongholdGenerator.Piece.EntranceType.WOOD_DOOR;
         case 3:
            return StrongholdGenerator.Piece.EntranceType.GRATES;
         case 4:
            return StrongholdGenerator.Piece.EntranceType.IRON_DOOR;
         }
      }

      @Nullable
      protected StructurePiece method_14874(StrongholdGenerator.Start start, List<StructurePiece> list, Random random, int i, int j) {
         Direction direction = this.getFacing();
         if (direction != null) {
            switch(direction) {
            case NORTH:
               return StrongholdGenerator.method_14854(start, list, random, this.boundingBox.minX + i, this.boundingBox.minY + j, this.boundingBox.minZ - 1, direction, this.method_14923());
            case SOUTH:
               return StrongholdGenerator.method_14854(start, list, random, this.boundingBox.minX + i, this.boundingBox.minY + j, this.boundingBox.maxZ + 1, direction, this.method_14923());
            case WEST:
               return StrongholdGenerator.method_14854(start, list, random, this.boundingBox.minX - 1, this.boundingBox.minY + j, this.boundingBox.minZ + i, direction, this.method_14923());
            case EAST:
               return StrongholdGenerator.method_14854(start, list, random, this.boundingBox.maxX + 1, this.boundingBox.minY + j, this.boundingBox.minZ + i, direction, this.method_14923());
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece method_14870(StrongholdGenerator.Start start, List<StructurePiece> list, Random random, int i, int j) {
         Direction direction = this.getFacing();
         if (direction != null) {
            switch(direction) {
            case NORTH:
               return StrongholdGenerator.method_14854(start, list, random, this.boundingBox.minX - 1, this.boundingBox.minY + i, this.boundingBox.minZ + j, Direction.WEST, this.method_14923());
            case SOUTH:
               return StrongholdGenerator.method_14854(start, list, random, this.boundingBox.minX - 1, this.boundingBox.minY + i, this.boundingBox.minZ + j, Direction.WEST, this.method_14923());
            case WEST:
               return StrongholdGenerator.method_14854(start, list, random, this.boundingBox.minX + j, this.boundingBox.minY + i, this.boundingBox.minZ - 1, Direction.NORTH, this.method_14923());
            case EAST:
               return StrongholdGenerator.method_14854(start, list, random, this.boundingBox.minX + j, this.boundingBox.minY + i, this.boundingBox.minZ - 1, Direction.NORTH, this.method_14923());
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece method_14873(StrongholdGenerator.Start start, List<StructurePiece> list, Random random, int i, int j) {
         Direction direction = this.getFacing();
         if (direction != null) {
            switch(direction) {
            case NORTH:
               return StrongholdGenerator.method_14854(start, list, random, this.boundingBox.maxX + 1, this.boundingBox.minY + i, this.boundingBox.minZ + j, Direction.EAST, this.method_14923());
            case SOUTH:
               return StrongholdGenerator.method_14854(start, list, random, this.boundingBox.maxX + 1, this.boundingBox.minY + i, this.boundingBox.minZ + j, Direction.EAST, this.method_14923());
            case WEST:
               return StrongholdGenerator.method_14854(start, list, random, this.boundingBox.minX + j, this.boundingBox.minY + i, this.boundingBox.maxZ + 1, Direction.SOUTH, this.method_14923());
            case EAST:
               return StrongholdGenerator.method_14854(start, list, random, this.boundingBox.minX + j, this.boundingBox.minY + i, this.boundingBox.maxZ + 1, Direction.SOUTH, this.method_14923());
            }
         }

         return null;
      }

      protected static boolean method_14871(BlockBox blockBox) {
         return blockBox != null && blockBox.minY > 10;
      }

      public static enum EntranceType {
         OPENING,
         WOOD_DOOR,
         GRATES,
         IRON_DOOR;
      }
   }

   static class class_3427 {
      public final Class<? extends StrongholdGenerator.Piece> field_15276;
      public final int field_15278;
      public int field_15277;
      public final int field_15275;

      public class_3427(Class<? extends StrongholdGenerator.Piece> var1, int i, int j) {
         this.field_15276 = var1;
         this.field_15278 = i;
         this.field_15275 = j;
      }

      public boolean method_14862(int i) {
         return this.field_15275 == 0 || this.field_15277 < this.field_15275;
      }

      public boolean method_14861() {
         return this.field_15275 == 0 || this.field_15277 < this.field_15275;
      }
   }
}
