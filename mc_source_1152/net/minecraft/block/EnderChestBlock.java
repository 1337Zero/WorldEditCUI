package net.minecraft.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.client.network.ClientDummyContainerProvider;
import net.minecraft.container.GenericContainer;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class EnderChestBlock extends AbstractChestBlock<EnderChestBlockEntity> implements Waterloggable {
   public static final DirectionProperty FACING;
   public static final BooleanProperty WATERLOGGED;
   protected static final VoxelShape SHAPE;
   public static final TranslatableText CONTAINER_NAME;

   protected EnderChestBlock(Block.Settings settings) {
      super(settings, () -> {
         return BlockEntityType.ENDER_CHEST;
      });
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(WATERLOGGED, false));
   }

   @Environment(EnvType.CLIENT)
   public DoubleBlockProperties.PropertySource<? extends ChestBlockEntity> getBlockEntitySource(BlockState state, World world, BlockPos pos, boolean ignoreBlocked) {
      return DoubleBlockProperties.PropertyRetriever::getFallback;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return SHAPE;
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.ENTITYBLOCK_ANIMATED;
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
      return (BlockState)((BlockState)this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite())).with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      EnderChestInventory enderChestInventory = player.getEnderChestInventory();
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (enderChestInventory != null && blockEntity instanceof EnderChestBlockEntity) {
         BlockPos blockPos = pos.up();
         if (world.getBlockState(blockPos).isSimpleFullBlock(world, blockPos)) {
            return ActionResult.SUCCESS;
         } else if (world.isClient) {
            return ActionResult.SUCCESS;
         } else {
            EnderChestBlockEntity enderChestBlockEntity = (EnderChestBlockEntity)blockEntity;
            enderChestInventory.setCurrentBlockEntity(enderChestBlockEntity);
            player.openContainer(new ClientDummyContainerProvider((i, playerInventory, playerEntity) -> {
               return GenericContainer.createGeneric9x3(i, playerInventory, enderChestInventory);
            }, CONTAINER_NAME));
            player.incrementStat(Stats.OPEN_ENDERCHEST);
            return ActionResult.SUCCESS;
         }
      } else {
         return ActionResult.SUCCESS;
      }
   }

   public BlockEntity createBlockEntity(BlockView view) {
      return new EnderChestBlockEntity();
   }

   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      for(int i = 0; i < 3; ++i) {
         int j = random.nextInt(2) * 2 - 1;
         int k = random.nextInt(2) * 2 - 1;
         double d = (double)pos.getX() + 0.5D + 0.25D * (double)j;
         double e = (double)((float)pos.getY() + random.nextFloat());
         double f = (double)pos.getZ() + 0.5D + 0.25D * (double)k;
         double g = (double)(random.nextFloat() * (float)j);
         double h = ((double)random.nextFloat() - 0.5D) * 0.125D;
         double l = (double)(random.nextFloat() * (float)k);
         world.addParticle(ParticleTypes.PORTAL, d, e, f, g, h, l);
      }

   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING, WATERLOGGED);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
   }

   public boolean canPlaceAtSide(BlockState world, BlockView view, BlockPos pos, BlockPlacementEnvironment env) {
      return false;
   }

   static {
      FACING = HorizontalFacingBlock.FACING;
      WATERLOGGED = Properties.WATERLOGGED;
      SHAPE = Block.createCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
      CONTAINER_NAME = new TranslatableText("container.enderchest", new Object[0]);
   }
}
