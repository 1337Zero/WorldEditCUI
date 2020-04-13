package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.client.network.ClientDummyContainerProvider;
import net.minecraft.container.AnvilContainer;
import net.minecraft.container.BlockContext;
import net.minecraft.container.NameableContainerProvider;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AnvilBlock extends FallingBlock {
   public static final DirectionProperty FACING;
   private static final VoxelShape BASE_SHAPE;
   private static final VoxelShape X_STEP_SHAPE;
   private static final VoxelShape X_STEM_SHAPE;
   private static final VoxelShape X_FACE_SHAPE;
   private static final VoxelShape Z_STEP_SHAPE;
   private static final VoxelShape Z_STEM_SHAPE;
   private static final VoxelShape Z_FACE_SHAPE;
   private static final VoxelShape X_AXIS_SHAPE;
   private static final VoxelShape Z_AXIS_SHAPE;
   private static final TranslatableText CONTAINER_NAME;

   public AnvilBlock(Block.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH));
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(FACING, ctx.getPlayerFacing().rotateYClockwise());
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         return ActionResult.SUCCESS;
      } else {
         player.openContainer(state.createContainerProvider(world, pos));
         player.incrementStat(Stats.INTERACT_WITH_ANVIL);
         return ActionResult.SUCCESS;
      }
   }

   @Nullable
   public NameableContainerProvider createContainerProvider(BlockState state, World world, BlockPos pos) {
      return new ClientDummyContainerProvider((i, playerInventory, playerEntity) -> {
         return new AnvilContainer(i, playerInventory, BlockContext.create(world, pos));
      }, CONTAINER_NAME);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      Direction direction = (Direction)state.get(FACING);
      return direction.getAxis() == Direction.Axis.X ? X_AXIS_SHAPE : Z_AXIS_SHAPE;
   }

   protected void configureFallingBlockEntity(FallingBlockEntity entity) {
      entity.setHurtEntities(true);
   }

   public void onLanding(World world, BlockPos pos, BlockState fallingBlockState, BlockState currentStateInPos) {
      world.playLevelEvent(1031, pos, 0);
   }

   public void onDestroyedOnLanding(World world, BlockPos pos) {
      world.playLevelEvent(1029, pos, 0);
   }

   @Nullable
   public static BlockState getLandingState(BlockState fallingState) {
      Block block = fallingState.getBlock();
      if (block == Blocks.ANVIL) {
         return (BlockState)Blocks.CHIPPED_ANVIL.getDefaultState().with(FACING, fallingState.get(FACING));
      } else {
         return block == Blocks.CHIPPED_ANVIL ? (BlockState)Blocks.DAMAGED_ANVIL.getDefaultState().with(FACING, fallingState.get(FACING)) : null;
      }
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }

   public boolean canPlaceAtSide(BlockState world, BlockView view, BlockPos pos, BlockPlacementEnvironment env) {
      return false;
   }

   static {
      FACING = HorizontalFacingBlock.FACING;
      BASE_SHAPE = Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D);
      X_STEP_SHAPE = Block.createCuboidShape(3.0D, 4.0D, 4.0D, 13.0D, 5.0D, 12.0D);
      X_STEM_SHAPE = Block.createCuboidShape(4.0D, 5.0D, 6.0D, 12.0D, 10.0D, 10.0D);
      X_FACE_SHAPE = Block.createCuboidShape(0.0D, 10.0D, 3.0D, 16.0D, 16.0D, 13.0D);
      Z_STEP_SHAPE = Block.createCuboidShape(4.0D, 4.0D, 3.0D, 12.0D, 5.0D, 13.0D);
      Z_STEM_SHAPE = Block.createCuboidShape(6.0D, 5.0D, 4.0D, 10.0D, 10.0D, 12.0D);
      Z_FACE_SHAPE = Block.createCuboidShape(3.0D, 10.0D, 0.0D, 13.0D, 16.0D, 16.0D);
      X_AXIS_SHAPE = VoxelShapes.union(BASE_SHAPE, X_STEP_SHAPE, X_STEM_SHAPE, X_FACE_SHAPE);
      Z_AXIS_SHAPE = VoxelShapes.union(BASE_SHAPE, Z_STEP_SHAPE, Z_STEM_SHAPE, Z_FACE_SHAPE);
      CONTAINER_NAME = new TranslatableText("container.repair", new Object[0]);
   }
}
