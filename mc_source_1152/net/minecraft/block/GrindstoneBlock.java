package net.minecraft.block;

import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.client.network.ClientDummyContainerProvider;
import net.minecraft.container.BlockContext;
import net.minecraft.container.GrindstoneContainer;
import net.minecraft.container.NameableContainerProvider;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class GrindstoneBlock extends WallMountedBlock {
   public static final VoxelShape field_16379 = Block.createCuboidShape(2.0D, 0.0D, 6.0D, 4.0D, 7.0D, 10.0D);
   public static final VoxelShape field_16392 = Block.createCuboidShape(12.0D, 0.0D, 6.0D, 14.0D, 7.0D, 10.0D);
   public static final VoxelShape field_16366 = Block.createCuboidShape(2.0D, 7.0D, 5.0D, 4.0D, 13.0D, 11.0D);
   public static final VoxelShape field_16339 = Block.createCuboidShape(12.0D, 7.0D, 5.0D, 14.0D, 13.0D, 11.0D);
   public static final VoxelShape field_16348;
   public static final VoxelShape field_16365;
   public static final VoxelShape field_16385;
   public static final VoxelShape NORTH_SOUTH_SHAPE;
   public static final VoxelShape field_16373;
   public static final VoxelShape field_16346;
   public static final VoxelShape field_16343;
   public static final VoxelShape field_16374;
   public static final VoxelShape field_16386;
   public static final VoxelShape field_16378;
   public static final VoxelShape field_16362;
   public static final VoxelShape EAST_WEST_SHAPE;
   public static final VoxelShape field_16352;
   public static final VoxelShape field_16377;
   public static final VoxelShape field_16393;
   public static final VoxelShape field_16371;
   public static final VoxelShape field_16340;
   public static final VoxelShape field_16354;
   public static final VoxelShape field_16369;
   public static final VoxelShape SOUTH_WALL_SHAPE;
   public static final VoxelShape field_16363;
   public static final VoxelShape field_16347;
   public static final VoxelShape field_16401;
   public static final VoxelShape field_16367;
   public static final VoxelShape field_16388;
   public static final VoxelShape field_16396;
   public static final VoxelShape field_16368;
   public static final VoxelShape NORTH_WALL_SHAPE;
   public static final VoxelShape field_16342;
   public static final VoxelShape field_16358;
   public static final VoxelShape field_16390;
   public static final VoxelShape field_16382;
   public static final VoxelShape field_16359;
   public static final VoxelShape field_16351;
   public static final VoxelShape field_16344;
   public static final VoxelShape WEST_WALL_SHAPE;
   public static final VoxelShape field_16394;
   public static final VoxelShape field_16375;
   public static final VoxelShape field_16345;
   public static final VoxelShape field_16350;
   public static final VoxelShape field_16372;
   public static final VoxelShape field_16381;
   public static final VoxelShape field_16391;
   public static final VoxelShape EAST_WALL_SHAPE;
   public static final VoxelShape field_16341;
   public static final VoxelShape field_16355;
   public static final VoxelShape field_16384;
   public static final VoxelShape field_16400;
   public static final VoxelShape field_16364;
   public static final VoxelShape field_16349;
   public static final VoxelShape field_16397;
   public static final VoxelShape NORTH_SOUTH_HANGING_SHAPE;
   public static final VoxelShape field_16387;
   public static final VoxelShape field_16398;
   public static final VoxelShape field_16357;
   public static final VoxelShape field_16353;
   public static final VoxelShape field_16395;
   public static final VoxelShape field_16360;
   public static final VoxelShape field_16389;
   public static final VoxelShape EAST_WEST_HANGING_SHAPE;
   private static final TranslatableText CONTAINER_NAME;

   protected GrindstoneBlock(Block.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(FACE, WallMountLocation.WALL));
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   private VoxelShape getShape(BlockState state) {
      Direction direction = (Direction)state.get(FACING);
      switch((WallMountLocation)state.get(FACE)) {
      case FLOOR:
         if (direction != Direction.NORTH && direction != Direction.SOUTH) {
            return EAST_WEST_SHAPE;
         }

         return NORTH_SOUTH_SHAPE;
      case WALL:
         if (direction == Direction.NORTH) {
            return NORTH_WALL_SHAPE;
         } else if (direction == Direction.SOUTH) {
            return SOUTH_WALL_SHAPE;
         } else {
            if (direction == Direction.EAST) {
               return EAST_WALL_SHAPE;
            }

            return WEST_WALL_SHAPE;
         }
      case CEILING:
         if (direction != Direction.NORTH && direction != Direction.SOUTH) {
            return EAST_WEST_HANGING_SHAPE;
         }

         return NORTH_SOUTH_HANGING_SHAPE;
      default:
         return EAST_WEST_SHAPE;
      }
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return this.getShape(state);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return this.getShape(state);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return true;
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         return ActionResult.SUCCESS;
      } else {
         player.openContainer(state.createContainerProvider(world, pos));
         player.incrementStat(Stats.INTERACT_WITH_GRINDSTONE);
         return ActionResult.SUCCESS;
      }
   }

   public NameableContainerProvider createContainerProvider(BlockState state, World world, BlockPos pos) {
      return new ClientDummyContainerProvider((i, playerInventory, playerEntity) -> {
         return new GrindstoneContainer(i, playerInventory, BlockContext.create(world, pos));
      }, CONTAINER_NAME);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING, FACE);
   }

   public boolean canPlaceAtSide(BlockState world, BlockView view, BlockPos pos, BlockPlacementEnvironment env) {
      return false;
   }

   static {
      field_16348 = VoxelShapes.union(field_16379, field_16366);
      field_16365 = VoxelShapes.union(field_16392, field_16339);
      field_16385 = VoxelShapes.union(field_16348, field_16365);
      NORTH_SOUTH_SHAPE = VoxelShapes.union(field_16385, Block.createCuboidShape(4.0D, 4.0D, 2.0D, 12.0D, 16.0D, 14.0D));
      field_16373 = Block.createCuboidShape(6.0D, 0.0D, 2.0D, 10.0D, 7.0D, 4.0D);
      field_16346 = Block.createCuboidShape(6.0D, 0.0D, 12.0D, 10.0D, 7.0D, 14.0D);
      field_16343 = Block.createCuboidShape(5.0D, 7.0D, 2.0D, 11.0D, 13.0D, 4.0D);
      field_16374 = Block.createCuboidShape(5.0D, 7.0D, 12.0D, 11.0D, 13.0D, 14.0D);
      field_16386 = VoxelShapes.union(field_16373, field_16343);
      field_16378 = VoxelShapes.union(field_16346, field_16374);
      field_16362 = VoxelShapes.union(field_16386, field_16378);
      EAST_WEST_SHAPE = VoxelShapes.union(field_16362, Block.createCuboidShape(2.0D, 4.0D, 4.0D, 14.0D, 16.0D, 12.0D));
      field_16352 = Block.createCuboidShape(2.0D, 6.0D, 0.0D, 4.0D, 10.0D, 7.0D);
      field_16377 = Block.createCuboidShape(12.0D, 6.0D, 0.0D, 14.0D, 10.0D, 7.0D);
      field_16393 = Block.createCuboidShape(2.0D, 5.0D, 7.0D, 4.0D, 11.0D, 13.0D);
      field_16371 = Block.createCuboidShape(12.0D, 5.0D, 7.0D, 14.0D, 11.0D, 13.0D);
      field_16340 = VoxelShapes.union(field_16352, field_16393);
      field_16354 = VoxelShapes.union(field_16377, field_16371);
      field_16369 = VoxelShapes.union(field_16340, field_16354);
      SOUTH_WALL_SHAPE = VoxelShapes.union(field_16369, Block.createCuboidShape(4.0D, 2.0D, 4.0D, 12.0D, 14.0D, 16.0D));
      field_16363 = Block.createCuboidShape(2.0D, 6.0D, 7.0D, 4.0D, 10.0D, 16.0D);
      field_16347 = Block.createCuboidShape(12.0D, 6.0D, 7.0D, 14.0D, 10.0D, 16.0D);
      field_16401 = Block.createCuboidShape(2.0D, 5.0D, 3.0D, 4.0D, 11.0D, 9.0D);
      field_16367 = Block.createCuboidShape(12.0D, 5.0D, 3.0D, 14.0D, 11.0D, 9.0D);
      field_16388 = VoxelShapes.union(field_16363, field_16401);
      field_16396 = VoxelShapes.union(field_16347, field_16367);
      field_16368 = VoxelShapes.union(field_16388, field_16396);
      NORTH_WALL_SHAPE = VoxelShapes.union(field_16368, Block.createCuboidShape(4.0D, 2.0D, 0.0D, 12.0D, 14.0D, 12.0D));
      field_16342 = Block.createCuboidShape(7.0D, 6.0D, 2.0D, 16.0D, 10.0D, 4.0D);
      field_16358 = Block.createCuboidShape(7.0D, 6.0D, 12.0D, 16.0D, 10.0D, 14.0D);
      field_16390 = Block.createCuboidShape(3.0D, 5.0D, 2.0D, 9.0D, 11.0D, 4.0D);
      field_16382 = Block.createCuboidShape(3.0D, 5.0D, 12.0D, 9.0D, 11.0D, 14.0D);
      field_16359 = VoxelShapes.union(field_16342, field_16390);
      field_16351 = VoxelShapes.union(field_16358, field_16382);
      field_16344 = VoxelShapes.union(field_16359, field_16351);
      WEST_WALL_SHAPE = VoxelShapes.union(field_16344, Block.createCuboidShape(0.0D, 2.0D, 4.0D, 12.0D, 14.0D, 12.0D));
      field_16394 = Block.createCuboidShape(0.0D, 6.0D, 2.0D, 9.0D, 10.0D, 4.0D);
      field_16375 = Block.createCuboidShape(0.0D, 6.0D, 12.0D, 9.0D, 10.0D, 14.0D);
      field_16345 = Block.createCuboidShape(7.0D, 5.0D, 2.0D, 13.0D, 11.0D, 4.0D);
      field_16350 = Block.createCuboidShape(7.0D, 5.0D, 12.0D, 13.0D, 11.0D, 14.0D);
      field_16372 = VoxelShapes.union(field_16394, field_16345);
      field_16381 = VoxelShapes.union(field_16375, field_16350);
      field_16391 = VoxelShapes.union(field_16372, field_16381);
      EAST_WALL_SHAPE = VoxelShapes.union(field_16391, Block.createCuboidShape(4.0D, 2.0D, 4.0D, 16.0D, 14.0D, 12.0D));
      field_16341 = Block.createCuboidShape(2.0D, 9.0D, 6.0D, 4.0D, 16.0D, 10.0D);
      field_16355 = Block.createCuboidShape(12.0D, 9.0D, 6.0D, 14.0D, 16.0D, 10.0D);
      field_16384 = Block.createCuboidShape(2.0D, 3.0D, 5.0D, 4.0D, 9.0D, 11.0D);
      field_16400 = Block.createCuboidShape(12.0D, 3.0D, 5.0D, 14.0D, 9.0D, 11.0D);
      field_16364 = VoxelShapes.union(field_16341, field_16384);
      field_16349 = VoxelShapes.union(field_16355, field_16400);
      field_16397 = VoxelShapes.union(field_16364, field_16349);
      NORTH_SOUTH_HANGING_SHAPE = VoxelShapes.union(field_16397, Block.createCuboidShape(4.0D, 0.0D, 2.0D, 12.0D, 12.0D, 14.0D));
      field_16387 = Block.createCuboidShape(6.0D, 9.0D, 2.0D, 10.0D, 16.0D, 4.0D);
      field_16398 = Block.createCuboidShape(6.0D, 9.0D, 12.0D, 10.0D, 16.0D, 14.0D);
      field_16357 = Block.createCuboidShape(5.0D, 3.0D, 2.0D, 11.0D, 9.0D, 4.0D);
      field_16353 = Block.createCuboidShape(5.0D, 3.0D, 12.0D, 11.0D, 9.0D, 14.0D);
      field_16395 = VoxelShapes.union(field_16387, field_16357);
      field_16360 = VoxelShapes.union(field_16398, field_16353);
      field_16389 = VoxelShapes.union(field_16395, field_16360);
      EAST_WEST_HANGING_SHAPE = VoxelShapes.union(field_16389, Block.createCuboidShape(2.0D, 0.0D, 4.0D, 14.0D, 12.0D, 12.0D));
      CONTAINER_NAME = new TranslatableText("container.grindstone_title", new Object[0]);
   }
}
