package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.Attachment;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class BellBlock extends BlockWithEntity {
   public static final DirectionProperty FACING;
   private static final EnumProperty<Attachment> ATTACHMENT;
   public static final BooleanProperty field_20648;
   private static final VoxelShape NORTH_SOUTH_SHAPE;
   private static final VoxelShape EAST_WEST_SHAPE;
   private static final VoxelShape BELL_WAIST_SHAPE;
   private static final VoxelShape BELL_LIP_SHAPE;
   private static final VoxelShape BELL_SHAPE;
   private static final VoxelShape NORTH_SOUTH_WALLS_SHAPE;
   private static final VoxelShape EAST_WEST_WALLS_SHAPE;
   private static final VoxelShape WEST_WALL_SHAPE;
   private static final VoxelShape EAST_WALL_SHAPE;
   private static final VoxelShape NORTH_WALL_SHAPE;
   private static final VoxelShape SOUTH_WALL_SHAPE;
   private static final VoxelShape HANGING_SHAPE;

   public BellBlock(Block.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(ATTACHMENT, Attachment.FLOOR)).with(field_20648, false));
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos neighborPos, boolean moved) {
      boolean bl = world.isReceivingRedstonePower(pos);
      if (bl != (Boolean)state.get(field_20648)) {
         if (bl) {
            this.ring(world, pos, (Direction)null);
         }

         world.setBlockState(pos, (BlockState)state.with(field_20648, bl), 3);
      }

   }

   public void onProjectileHit(World world, BlockState state, BlockHitResult hitResult, Entity entity) {
      if (entity instanceof ProjectileEntity) {
         Entity entity2 = ((ProjectileEntity)entity).getOwner();
         PlayerEntity playerEntity = entity2 instanceof PlayerEntity ? (PlayerEntity)entity2 : null;
         this.ring(world, state, hitResult, playerEntity, true);
      }

   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      return this.ring(world, state, hit, player, true) ? ActionResult.SUCCESS : ActionResult.PASS;
   }

   public boolean ring(World world, BlockState state, BlockHitResult blockHitResult, @Nullable PlayerEntity playerEntity, boolean bl) {
      Direction direction = blockHitResult.getSide();
      BlockPos blockPos = blockHitResult.getBlockPos();
      boolean bl2 = !bl || this.isPointOnBell(state, direction, blockHitResult.getPos().y - (double)blockPos.getY());
      if (bl2) {
         boolean bl3 = this.ring(world, blockPos, direction);
         if (bl3 && playerEntity != null) {
            playerEntity.incrementStat(Stats.BELL_RING);
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean isPointOnBell(BlockState state, Direction side, double y) {
      if (side.getAxis() != Direction.Axis.Y && y <= 0.8123999834060669D) {
         Direction direction = (Direction)state.get(FACING);
         Attachment attachment = (Attachment)state.get(ATTACHMENT);
         switch(attachment) {
         case FLOOR:
            return direction.getAxis() == side.getAxis();
         case SINGLE_WALL:
         case DOUBLE_WALL:
            return direction.getAxis() != side.getAxis();
         case CEILING:
            return true;
         default:
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean ring(World world, BlockPos pos, @Nullable Direction direction) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (!world.isClient && blockEntity instanceof BellBlockEntity) {
         if (direction == null) {
            direction = (Direction)world.getBlockState(pos).get(FACING);
         }

         ((BellBlockEntity)blockEntity).activate(direction);
         world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS, 2.0F, 1.0F);
         return true;
      } else {
         return false;
      }
   }

   private VoxelShape getShape(BlockState state) {
      Direction direction = (Direction)state.get(FACING);
      Attachment attachment = (Attachment)state.get(ATTACHMENT);
      if (attachment == Attachment.FLOOR) {
         return direction != Direction.NORTH && direction != Direction.SOUTH ? EAST_WEST_SHAPE : NORTH_SOUTH_SHAPE;
      } else if (attachment == Attachment.CEILING) {
         return HANGING_SHAPE;
      } else if (attachment == Attachment.DOUBLE_WALL) {
         return direction != Direction.NORTH && direction != Direction.SOUTH ? EAST_WEST_WALLS_SHAPE : NORTH_SOUTH_WALLS_SHAPE;
      } else if (direction == Direction.NORTH) {
         return NORTH_WALL_SHAPE;
      } else if (direction == Direction.SOUTH) {
         return SOUTH_WALL_SHAPE;
      } else {
         return direction == Direction.EAST ? EAST_WALL_SHAPE : WEST_WALL_SHAPE;
      }
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return this.getShape(state);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return this.getShape(state);
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      Direction direction = ctx.getSide();
      BlockPos blockPos = ctx.getBlockPos();
      World world = ctx.getWorld();
      Direction.Axis axis = direction.getAxis();
      BlockState blockState2;
      if (axis == Direction.Axis.Y) {
         blockState2 = (BlockState)((BlockState)this.getDefaultState().with(ATTACHMENT, direction == Direction.DOWN ? Attachment.CEILING : Attachment.FLOOR)).with(FACING, ctx.getPlayerFacing());
         if (blockState2.canPlaceAt(ctx.getWorld(), blockPos)) {
            return blockState2;
         }
      } else {
         boolean bl = axis == Direction.Axis.X && world.getBlockState(blockPos.west()).isSideSolidFullSquare(world, blockPos.west(), Direction.EAST) && world.getBlockState(blockPos.east()).isSideSolidFullSquare(world, blockPos.east(), Direction.WEST) || axis == Direction.Axis.Z && world.getBlockState(blockPos.north()).isSideSolidFullSquare(world, blockPos.north(), Direction.SOUTH) && world.getBlockState(blockPos.south()).isSideSolidFullSquare(world, blockPos.south(), Direction.NORTH);
         blockState2 = (BlockState)((BlockState)this.getDefaultState().with(FACING, direction.getOpposite())).with(ATTACHMENT, bl ? Attachment.DOUBLE_WALL : Attachment.SINGLE_WALL);
         if (blockState2.canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) {
            return blockState2;
         }

         boolean bl2 = world.getBlockState(blockPos.down()).isSideSolidFullSquare(world, blockPos.down(), Direction.UP);
         blockState2 = (BlockState)blockState2.with(ATTACHMENT, bl2 ? Attachment.FLOOR : Attachment.CEILING);
         if (blockState2.canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) {
            return blockState2;
         }
      }

      return null;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
      Attachment attachment = (Attachment)state.get(ATTACHMENT);
      Direction direction = getPlacementSide(state).getOpposite();
      if (direction == facing && !state.canPlaceAt(world, pos) && attachment != Attachment.DOUBLE_WALL) {
         return Blocks.AIR.getDefaultState();
      } else {
         if (facing.getAxis() == ((Direction)state.get(FACING)).getAxis()) {
            if (attachment == Attachment.DOUBLE_WALL && !neighborState.isSideSolidFullSquare(world, neighborPos, facing)) {
               return (BlockState)((BlockState)state.with(ATTACHMENT, Attachment.SINGLE_WALL)).with(FACING, facing.getOpposite());
            }

            if (attachment == Attachment.SINGLE_WALL && direction.getOpposite() == facing && neighborState.isSideSolidFullSquare(world, neighborPos, (Direction)state.get(FACING))) {
               return (BlockState)state.with(ATTACHMENT, Attachment.DOUBLE_WALL);
            }
         }

         return super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
      }
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return WallMountedBlock.canPlaceAt(world, pos, getPlacementSide(state).getOpposite());
   }

   private static Direction getPlacementSide(BlockState state) {
      switch((Attachment)state.get(ATTACHMENT)) {
      case FLOOR:
         return Direction.UP;
      case CEILING:
         return Direction.DOWN;
      default:
         return ((Direction)state.get(FACING)).getOpposite();
      }
   }

   public PistonBehavior getPistonBehavior(BlockState state) {
      return PistonBehavior.DESTROY;
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING, ATTACHMENT, field_20648);
   }

   @Nullable
   public BlockEntity createBlockEntity(BlockView view) {
      return new BellBlockEntity();
   }

   public boolean canPlaceAtSide(BlockState world, BlockView view, BlockPos pos, BlockPlacementEnvironment env) {
      return false;
   }

   static {
      FACING = HorizontalFacingBlock.FACING;
      ATTACHMENT = Properties.ATTACHMENT;
      field_20648 = Properties.POWERED;
      NORTH_SOUTH_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 4.0D, 16.0D, 16.0D, 12.0D);
      EAST_WEST_SHAPE = Block.createCuboidShape(4.0D, 0.0D, 0.0D, 12.0D, 16.0D, 16.0D);
      BELL_WAIST_SHAPE = Block.createCuboidShape(5.0D, 6.0D, 5.0D, 11.0D, 13.0D, 11.0D);
      BELL_LIP_SHAPE = Block.createCuboidShape(4.0D, 4.0D, 4.0D, 12.0D, 6.0D, 12.0D);
      BELL_SHAPE = VoxelShapes.union(BELL_LIP_SHAPE, BELL_WAIST_SHAPE);
      NORTH_SOUTH_WALLS_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createCuboidShape(7.0D, 13.0D, 0.0D, 9.0D, 15.0D, 16.0D));
      EAST_WEST_WALLS_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createCuboidShape(0.0D, 13.0D, 7.0D, 16.0D, 15.0D, 9.0D));
      WEST_WALL_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createCuboidShape(0.0D, 13.0D, 7.0D, 13.0D, 15.0D, 9.0D));
      EAST_WALL_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createCuboidShape(3.0D, 13.0D, 7.0D, 16.0D, 15.0D, 9.0D));
      NORTH_WALL_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createCuboidShape(7.0D, 13.0D, 0.0D, 9.0D, 15.0D, 13.0D));
      SOUTH_WALL_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createCuboidShape(7.0D, 13.0D, 3.0D, 9.0D, 15.0D, 16.0D));
      HANGING_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createCuboidShape(7.0D, 13.0D, 7.0D, 9.0D, 16.0D, 9.0D));
   }
}
