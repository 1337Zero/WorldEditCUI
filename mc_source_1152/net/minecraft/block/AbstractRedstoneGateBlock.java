package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public abstract class AbstractRedstoneGateBlock extends HorizontalFacingBlock {
   protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
   public static final BooleanProperty POWERED;

   protected AbstractRedstoneGateBlock(Block.Settings settings) {
      super(settings);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return SHAPE;
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return topCoversMediumSquare(world, pos.down());
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!this.isLocked(world, pos, state)) {
         boolean bl = (Boolean)state.get(POWERED);
         boolean bl2 = this.hasPower(world, pos, state);
         if (bl && !bl2) {
            world.setBlockState(pos, (BlockState)state.with(POWERED, false), 2);
         } else if (!bl) {
            world.setBlockState(pos, (BlockState)state.with(POWERED, true), 2);
            if (!bl2) {
               world.getBlockTickScheduler().schedule(pos, this, this.getUpdateDelayInternal(state), TickPriority.VERY_HIGH);
            }
         }

      }
   }

   public int getStrongRedstonePower(BlockState state, BlockView view, BlockPos pos, Direction facing) {
      return state.getWeakRedstonePower(view, pos, facing);
   }

   public int getWeakRedstonePower(BlockState state, BlockView view, BlockPos pos, Direction facing) {
      if (!(Boolean)state.get(POWERED)) {
         return 0;
      } else {
         return state.get(FACING) == facing ? this.getOutputLevel(view, pos, state) : 0;
      }
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos neighborPos, boolean moved) {
      if (state.canPlaceAt(world, pos)) {
         this.updatePowered(world, pos, state);
      } else {
         BlockEntity blockEntity = this.hasBlockEntity() ? world.getBlockEntity(pos) : null;
         dropStacks(state, world, pos, blockEntity);
         world.removeBlock(pos, false);
         Direction[] var8 = Direction.values();
         int var9 = var8.length;

         for(int var10 = 0; var10 < var9; ++var10) {
            Direction direction = var8[var10];
            world.updateNeighborsAlways(pos.offset(direction), this);
         }

      }
   }

   protected void updatePowered(World world, BlockPos pos, BlockState state) {
      if (!this.isLocked(world, pos, state)) {
         boolean bl = (Boolean)state.get(POWERED);
         boolean bl2 = this.hasPower(world, pos, state);
         if (bl != bl2 && !world.getBlockTickScheduler().isTicking(pos, this)) {
            TickPriority tickPriority = TickPriority.HIGH;
            if (this.isTargetNotAligned(world, pos, state)) {
               tickPriority = TickPriority.EXTREMELY_HIGH;
            } else if (bl) {
               tickPriority = TickPriority.VERY_HIGH;
            }

            world.getBlockTickScheduler().schedule(pos, this, this.getUpdateDelayInternal(state), tickPriority);
         }

      }
   }

   public boolean isLocked(WorldView worldView, BlockPos pos, BlockState state) {
      return false;
   }

   protected boolean hasPower(World world, BlockPos pos, BlockState state) {
      return this.getPower(world, pos, state) > 0;
   }

   protected int getPower(World world, BlockPos pos, BlockState state) {
      Direction direction = (Direction)state.get(FACING);
      BlockPos blockPos = pos.offset(direction);
      int i = world.getEmittedRedstonePower(blockPos, direction);
      if (i >= 15) {
         return i;
      } else {
         BlockState blockState = world.getBlockState(blockPos);
         return Math.max(i, blockState.getBlock() == Blocks.REDSTONE_WIRE ? (Integer)blockState.get(RedstoneWireBlock.POWER) : 0);
      }
   }

   protected int getMaxInputLevelSides(WorldView worldView, BlockPos pos, BlockState state) {
      Direction direction = (Direction)state.get(FACING);
      Direction direction2 = direction.rotateYClockwise();
      Direction direction3 = direction.rotateYCounterclockwise();
      return Math.max(this.getInputLevel(worldView, pos.offset(direction2), direction2), this.getInputLevel(worldView, pos.offset(direction3), direction3));
   }

   protected int getInputLevel(WorldView worldView, BlockPos pos, Direction dir) {
      BlockState blockState = worldView.getBlockState(pos);
      Block block = blockState.getBlock();
      if (this.isValidInput(blockState)) {
         if (block == Blocks.REDSTONE_BLOCK) {
            return 15;
         } else {
            return block == Blocks.REDSTONE_WIRE ? (Integer)blockState.get(RedstoneWireBlock.POWER) : worldView.getStrongRedstonePower(pos, dir);
         }
      } else {
         return 0;
      }
   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      if (this.hasPower(world, pos, state)) {
         world.getBlockTickScheduler().schedule(pos, this, 1);
      }

   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved) {
      this.updateTarget(world, pos, state);
   }

   public void onBlockRemoved(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!moved && state.getBlock() != newState.getBlock()) {
         super.onBlockRemoved(state, world, pos, newState, moved);
         this.updateTarget(world, pos, state);
      }
   }

   protected void updateTarget(World world, BlockPos pos, BlockState state) {
      Direction direction = (Direction)state.get(FACING);
      BlockPos blockPos = pos.offset(direction.getOpposite());
      world.updateNeighbor(blockPos, this, pos);
      world.updateNeighborsExcept(blockPos, this, direction);
   }

   protected boolean isValidInput(BlockState state) {
      return state.emitsRedstonePower();
   }

   protected int getOutputLevel(BlockView view, BlockPos pos, BlockState state) {
      return 15;
   }

   public static boolean isRedstoneGate(BlockState state) {
      return state.getBlock() instanceof AbstractRedstoneGateBlock;
   }

   public boolean isTargetNotAligned(BlockView world, BlockPos pos, BlockState state) {
      Direction direction = ((Direction)state.get(FACING)).getOpposite();
      BlockState blockState = world.getBlockState(pos.offset(direction));
      return isRedstoneGate(blockState) && blockState.get(FACING) != direction;
   }

   protected abstract int getUpdateDelayInternal(BlockState state);

   static {
      POWERED = Properties.POWERED;
   }
}
