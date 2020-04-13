package net.minecraft.structure;

import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TripwireBlock;
import net.minecraft.block.TripwireHookBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class JungleTempleGenerator extends StructurePieceWithDimensions {
   private boolean placedMainChest;
   private boolean placedHiddenChest;
   private boolean placedTrap1;
   private boolean placedTrap2;
   private static final JungleTempleGenerator.CobblestoneRandomizer COBBLESTONE_RANDOMIZER = new JungleTempleGenerator.CobblestoneRandomizer();

   public JungleTempleGenerator(Random random, int x, int z) {
      super(StructurePieceType.JUNGLE_TEMPLE, random, x, 64, z, 12, 10, 15);
   }

   public JungleTempleGenerator(StructureManager manager, CompoundTag tag) {
      super(StructurePieceType.JUNGLE_TEMPLE, tag);
      this.placedMainChest = tag.getBoolean("placedMainChest");
      this.placedHiddenChest = tag.getBoolean("placedHiddenChest");
      this.placedTrap1 = tag.getBoolean("placedTrap1");
      this.placedTrap2 = tag.getBoolean("placedTrap2");
   }

   protected void toNbt(CompoundTag tag) {
      super.toNbt(tag);
      tag.putBoolean("placedMainChest", this.placedMainChest);
      tag.putBoolean("placedHiddenChest", this.placedHiddenChest);
      tag.putBoolean("placedTrap1", this.placedTrap1);
      tag.putBoolean("placedTrap2", this.placedTrap2);
   }

   public boolean generate(IWorld world, ChunkGenerator<?> chunkGenerator, Random random, BlockBox blockBox, ChunkPos chunkPos) {
      if (!this.method_14839(world, blockBox, 0)) {
         return false;
      } else {
         this.fillWithOutline(world, blockBox, 0, -4, 0, this.width - 1, 0, this.depth - 1, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 2, 1, 2, 9, 2, 2, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 2, 1, 12, 9, 2, 12, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 2, 1, 3, 2, 2, 11, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 9, 1, 3, 9, 2, 11, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 1, 3, 1, 10, 6, 1, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 1, 3, 13, 10, 6, 13, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 1, 3, 2, 1, 6, 12, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 10, 3, 2, 10, 6, 12, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 2, 3, 2, 9, 3, 12, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 2, 6, 2, 9, 6, 12, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 3, 7, 3, 8, 7, 11, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 4, 8, 4, 7, 8, 10, false, random, COBBLESTONE_RANDOMIZER);
         this.fill(world, blockBox, 3, 1, 3, 8, 2, 11);
         this.fill(world, blockBox, 4, 3, 6, 7, 3, 9);
         this.fill(world, blockBox, 2, 4, 2, 9, 5, 12);
         this.fill(world, blockBox, 4, 6, 5, 7, 6, 9);
         this.fill(world, blockBox, 5, 7, 6, 6, 7, 8);
         this.fill(world, blockBox, 5, 1, 2, 6, 2, 2);
         this.fill(world, blockBox, 5, 2, 12, 6, 2, 12);
         this.fill(world, blockBox, 5, 5, 1, 6, 5, 1);
         this.fill(world, blockBox, 5, 5, 13, 6, 5, 13);
         this.addBlock(world, Blocks.AIR.getDefaultState(), 1, 5, 5, blockBox);
         this.addBlock(world, Blocks.AIR.getDefaultState(), 10, 5, 5, blockBox);
         this.addBlock(world, Blocks.AIR.getDefaultState(), 1, 5, 9, blockBox);
         this.addBlock(world, Blocks.AIR.getDefaultState(), 10, 5, 9, blockBox);

         int j;
         for(j = 0; j <= 14; j += 14) {
            this.fillWithOutline(world, blockBox, 2, 4, j, 2, 5, j, false, random, COBBLESTONE_RANDOMIZER);
            this.fillWithOutline(world, blockBox, 4, 4, j, 4, 5, j, false, random, COBBLESTONE_RANDOMIZER);
            this.fillWithOutline(world, blockBox, 7, 4, j, 7, 5, j, false, random, COBBLESTONE_RANDOMIZER);
            this.fillWithOutline(world, blockBox, 9, 4, j, 9, 5, j, false, random, COBBLESTONE_RANDOMIZER);
         }

         this.fillWithOutline(world, blockBox, 5, 6, 0, 6, 6, 0, false, random, COBBLESTONE_RANDOMIZER);

         for(j = 0; j <= 11; j += 11) {
            for(int k = 2; k <= 12; k += 2) {
               this.fillWithOutline(world, blockBox, j, 4, k, j, 5, k, false, random, COBBLESTONE_RANDOMIZER);
            }

            this.fillWithOutline(world, blockBox, j, 6, 5, j, 6, 5, false, random, COBBLESTONE_RANDOMIZER);
            this.fillWithOutline(world, blockBox, j, 6, 9, j, 6, 9, false, random, COBBLESTONE_RANDOMIZER);
         }

         this.fillWithOutline(world, blockBox, 2, 7, 2, 2, 9, 2, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 9, 7, 2, 9, 9, 2, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 2, 7, 12, 2, 9, 12, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 9, 7, 12, 9, 9, 12, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 4, 9, 4, 4, 9, 4, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 7, 9, 4, 7, 9, 4, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 4, 9, 10, 4, 9, 10, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 7, 9, 10, 7, 9, 10, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 5, 9, 7, 6, 9, 7, false, random, COBBLESTONE_RANDOMIZER);
         BlockState blockState = (BlockState)Blocks.COBBLESTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST);
         BlockState blockState2 = (BlockState)Blocks.COBBLESTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST);
         BlockState blockState3 = (BlockState)Blocks.COBBLESTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH);
         BlockState blockState4 = (BlockState)Blocks.COBBLESTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH);
         this.addBlock(world, blockState4, 5, 9, 6, blockBox);
         this.addBlock(world, blockState4, 6, 9, 6, blockBox);
         this.addBlock(world, blockState3, 5, 9, 8, blockBox);
         this.addBlock(world, blockState3, 6, 9, 8, blockBox);
         this.addBlock(world, blockState4, 4, 0, 0, blockBox);
         this.addBlock(world, blockState4, 5, 0, 0, blockBox);
         this.addBlock(world, blockState4, 6, 0, 0, blockBox);
         this.addBlock(world, blockState4, 7, 0, 0, blockBox);
         this.addBlock(world, blockState4, 4, 1, 8, blockBox);
         this.addBlock(world, blockState4, 4, 2, 9, blockBox);
         this.addBlock(world, blockState4, 4, 3, 10, blockBox);
         this.addBlock(world, blockState4, 7, 1, 8, blockBox);
         this.addBlock(world, blockState4, 7, 2, 9, blockBox);
         this.addBlock(world, blockState4, 7, 3, 10, blockBox);
         this.fillWithOutline(world, blockBox, 4, 1, 9, 4, 1, 9, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 7, 1, 9, 7, 1, 9, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 4, 1, 10, 7, 2, 10, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 5, 4, 5, 6, 4, 5, false, random, COBBLESTONE_RANDOMIZER);
         this.addBlock(world, blockState, 4, 4, 5, blockBox);
         this.addBlock(world, blockState2, 7, 4, 5, blockBox);

         int n;
         for(n = 0; n < 4; ++n) {
            this.addBlock(world, blockState3, 5, 0 - n, 6 + n, blockBox);
            this.addBlock(world, blockState3, 6, 0 - n, 6 + n, blockBox);
            this.fill(world, blockBox, 5, 0 - n, 7 + n, 6, 0 - n, 9 + n);
         }

         this.fill(world, blockBox, 1, -3, 12, 10, -1, 13);
         this.fill(world, blockBox, 1, -3, 1, 3, -1, 13);
         this.fill(world, blockBox, 1, -3, 1, 9, -1, 5);

         for(n = 1; n <= 13; n += 2) {
            this.fillWithOutline(world, blockBox, 1, -3, n, 1, -2, n, false, random, COBBLESTONE_RANDOMIZER);
         }

         for(n = 2; n <= 12; n += 2) {
            this.fillWithOutline(world, blockBox, 1, -1, n, 3, -1, n, false, random, COBBLESTONE_RANDOMIZER);
         }

         this.fillWithOutline(world, blockBox, 2, -2, 1, 5, -2, 1, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 7, -2, 1, 9, -2, 1, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 6, -3, 1, 6, -3, 1, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 6, -1, 1, 6, -1, 1, false, random, COBBLESTONE_RANDOMIZER);
         this.addBlock(world, (BlockState)((BlockState)Blocks.TRIPWIRE_HOOK.getDefaultState().with(TripwireHookBlock.FACING, Direction.EAST)).with(TripwireHookBlock.ATTACHED, true), 1, -3, 8, blockBox);
         this.addBlock(world, (BlockState)((BlockState)Blocks.TRIPWIRE_HOOK.getDefaultState().with(TripwireHookBlock.FACING, Direction.WEST)).with(TripwireHookBlock.ATTACHED, true), 4, -3, 8, blockBox);
         this.addBlock(world, (BlockState)((BlockState)((BlockState)Blocks.TRIPWIRE.getDefaultState().with(TripwireBlock.EAST, true)).with(TripwireBlock.WEST, true)).with(TripwireBlock.ATTACHED, true), 2, -3, 8, blockBox);
         this.addBlock(world, (BlockState)((BlockState)((BlockState)Blocks.TRIPWIRE.getDefaultState().with(TripwireBlock.EAST, true)).with(TripwireBlock.WEST, true)).with(TripwireBlock.ATTACHED, true), 3, -3, 8, blockBox);
         BlockState blockState5 = (BlockState)((BlockState)Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_NORTH, WireConnection.SIDE)).with(RedstoneWireBlock.WIRE_CONNECTION_SOUTH, WireConnection.SIDE);
         this.addBlock(world, (BlockState)Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_SOUTH, WireConnection.SIDE), 5, -3, 7, blockBox);
         this.addBlock(world, blockState5, 5, -3, 6, blockBox);
         this.addBlock(world, blockState5, 5, -3, 5, blockBox);
         this.addBlock(world, blockState5, 5, -3, 4, blockBox);
         this.addBlock(world, blockState5, 5, -3, 3, blockBox);
         this.addBlock(world, blockState5, 5, -3, 2, blockBox);
         this.addBlock(world, (BlockState)((BlockState)Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_NORTH, WireConnection.SIDE)).with(RedstoneWireBlock.WIRE_CONNECTION_WEST, WireConnection.SIDE), 5, -3, 1, blockBox);
         this.addBlock(world, (BlockState)Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_EAST, WireConnection.SIDE), 4, -3, 1, blockBox);
         this.addBlock(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 3, -3, 1, blockBox);
         if (!this.placedTrap1) {
            this.placedTrap1 = this.addDispenser(world, blockBox, random, 3, -2, 1, Direction.NORTH, LootTables.JUNGLE_TEMPLE_DISPENSER_CHEST);
         }

         this.addBlock(world, (BlockState)Blocks.VINE.getDefaultState().with(VineBlock.SOUTH, true), 3, -2, 2, blockBox);
         this.addBlock(world, (BlockState)((BlockState)Blocks.TRIPWIRE_HOOK.getDefaultState().with(TripwireHookBlock.FACING, Direction.NORTH)).with(TripwireHookBlock.ATTACHED, true), 7, -3, 1, blockBox);
         this.addBlock(world, (BlockState)((BlockState)Blocks.TRIPWIRE_HOOK.getDefaultState().with(TripwireHookBlock.FACING, Direction.SOUTH)).with(TripwireHookBlock.ATTACHED, true), 7, -3, 5, blockBox);
         this.addBlock(world, (BlockState)((BlockState)((BlockState)Blocks.TRIPWIRE.getDefaultState().with(TripwireBlock.NORTH, true)).with(TripwireBlock.SOUTH, true)).with(TripwireBlock.ATTACHED, true), 7, -3, 2, blockBox);
         this.addBlock(world, (BlockState)((BlockState)((BlockState)Blocks.TRIPWIRE.getDefaultState().with(TripwireBlock.NORTH, true)).with(TripwireBlock.SOUTH, true)).with(TripwireBlock.ATTACHED, true), 7, -3, 3, blockBox);
         this.addBlock(world, (BlockState)((BlockState)((BlockState)Blocks.TRIPWIRE.getDefaultState().with(TripwireBlock.NORTH, true)).with(TripwireBlock.SOUTH, true)).with(TripwireBlock.ATTACHED, true), 7, -3, 4, blockBox);
         this.addBlock(world, (BlockState)Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_EAST, WireConnection.SIDE), 8, -3, 6, blockBox);
         this.addBlock(world, (BlockState)((BlockState)Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_WEST, WireConnection.SIDE)).with(RedstoneWireBlock.WIRE_CONNECTION_SOUTH, WireConnection.SIDE), 9, -3, 6, blockBox);
         this.addBlock(world, (BlockState)((BlockState)Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_NORTH, WireConnection.SIDE)).with(RedstoneWireBlock.WIRE_CONNECTION_SOUTH, WireConnection.UP), 9, -3, 5, blockBox);
         this.addBlock(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 9, -3, 4, blockBox);
         this.addBlock(world, (BlockState)Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_NORTH, WireConnection.SIDE), 9, -2, 4, blockBox);
         if (!this.placedTrap2) {
            this.placedTrap2 = this.addDispenser(world, blockBox, random, 9, -2, 3, Direction.WEST, LootTables.JUNGLE_TEMPLE_DISPENSER_CHEST);
         }

         this.addBlock(world, (BlockState)Blocks.VINE.getDefaultState().with(VineBlock.EAST, true), 8, -1, 3, blockBox);
         this.addBlock(world, (BlockState)Blocks.VINE.getDefaultState().with(VineBlock.EAST, true), 8, -2, 3, blockBox);
         if (!this.placedMainChest) {
            this.placedMainChest = this.addChest(world, blockBox, random, 8, -3, 3, LootTables.JUNGLE_TEMPLE_CHEST);
         }

         this.addBlock(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 9, -3, 2, blockBox);
         this.addBlock(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 8, -3, 1, blockBox);
         this.addBlock(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 4, -3, 5, blockBox);
         this.addBlock(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 5, -2, 5, blockBox);
         this.addBlock(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 5, -1, 5, blockBox);
         this.addBlock(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 6, -3, 5, blockBox);
         this.addBlock(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 7, -2, 5, blockBox);
         this.addBlock(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 7, -1, 5, blockBox);
         this.addBlock(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 8, -3, 5, blockBox);
         this.fillWithOutline(world, blockBox, 9, -1, 1, 9, -1, 5, false, random, COBBLESTONE_RANDOMIZER);
         this.fill(world, blockBox, 8, -3, 8, 10, -1, 10);
         this.addBlock(world, Blocks.CHISELED_STONE_BRICKS.getDefaultState(), 8, -2, 11, blockBox);
         this.addBlock(world, Blocks.CHISELED_STONE_BRICKS.getDefaultState(), 9, -2, 11, blockBox);
         this.addBlock(world, Blocks.CHISELED_STONE_BRICKS.getDefaultState(), 10, -2, 11, blockBox);
         BlockState blockState6 = (BlockState)((BlockState)Blocks.LEVER.getDefaultState().with(LeverBlock.FACING, Direction.NORTH)).with(LeverBlock.FACE, WallMountLocation.WALL);
         this.addBlock(world, blockState6, 8, -2, 12, blockBox);
         this.addBlock(world, blockState6, 9, -2, 12, blockBox);
         this.addBlock(world, blockState6, 10, -2, 12, blockBox);
         this.fillWithOutline(world, blockBox, 8, -3, 8, 8, -3, 10, false, random, COBBLESTONE_RANDOMIZER);
         this.fillWithOutline(world, blockBox, 10, -3, 8, 10, -3, 10, false, random, COBBLESTONE_RANDOMIZER);
         this.addBlock(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 10, -2, 9, blockBox);
         this.addBlock(world, (BlockState)Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_NORTH, WireConnection.SIDE), 8, -2, 9, blockBox);
         this.addBlock(world, (BlockState)Blocks.REDSTONE_WIRE.getDefaultState().with(RedstoneWireBlock.WIRE_CONNECTION_SOUTH, WireConnection.SIDE), 8, -2, 10, blockBox);
         this.addBlock(world, Blocks.REDSTONE_WIRE.getDefaultState(), 10, -1, 9, blockBox);
         this.addBlock(world, (BlockState)Blocks.STICKY_PISTON.getDefaultState().with(PistonBlock.FACING, Direction.UP), 9, -2, 8, blockBox);
         this.addBlock(world, (BlockState)Blocks.STICKY_PISTON.getDefaultState().with(PistonBlock.FACING, Direction.WEST), 10, -2, 8, blockBox);
         this.addBlock(world, (BlockState)Blocks.STICKY_PISTON.getDefaultState().with(PistonBlock.FACING, Direction.WEST), 10, -1, 8, blockBox);
         this.addBlock(world, (BlockState)Blocks.REPEATER.getDefaultState().with(RepeaterBlock.FACING, Direction.NORTH), 10, -2, 10, blockBox);
         if (!this.placedHiddenChest) {
            this.placedHiddenChest = this.addChest(world, blockBox, random, 9, -3, 10, LootTables.JUNGLE_TEMPLE_CHEST);
         }

         return true;
      }
   }

   static class CobblestoneRandomizer extends StructurePiece.BlockRandomizer {
      private CobblestoneRandomizer() {
      }

      public void setBlock(Random random, int x, int y, int z, boolean placeBlock) {
         if (random.nextFloat() < 0.4F) {
            this.block = Blocks.COBBLESTONE.getDefaultState();
         } else {
            this.block = Blocks.MOSSY_COBBLESTONE.getDefaultState();
         }

      }
   }
}
