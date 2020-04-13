package net.minecraft.block;

import java.util.Iterator;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class FarmlandBlock extends Block {
   public static final IntProperty MOISTURE;
   protected static final VoxelShape SHAPE;

   protected FarmlandBlock(Block.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(MOISTURE, 0));
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
      if (facing == Direction.UP && !state.canPlaceAt(world, pos)) {
         world.getBlockTickScheduler().schedule(pos, this, 1);
      }

      return super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockState blockState = world.getBlockState(pos.up());
      return !blockState.getMaterial().isSolid() || blockState.getBlock() instanceof FenceGateBlock || blockState.getBlock() instanceof PistonExtensionBlock;
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return !this.getDefaultState().canPlaceAt(ctx.getWorld(), ctx.getBlockPos()) ? Blocks.DIRT.getDefaultState() : super.getPlacementState(ctx);
   }

   public boolean hasSidedTransparency(BlockState state) {
      return true;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return SHAPE;
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!state.canPlaceAt(world, pos)) {
         setToDirt(state, world, pos);
      } else {
         int i = (Integer)state.get(MOISTURE);
         if (!isWaterNearby(world, pos) && !world.hasRain(pos.up())) {
            if (i > 0) {
               world.setBlockState(pos, (BlockState)state.with(MOISTURE, i - 1), 2);
            } else if (!hasCrop(world, pos)) {
               setToDirt(state, world, pos);
            }
         } else if (i < 7) {
            world.setBlockState(pos, (BlockState)state.with(MOISTURE, 7), 2);
         }

      }
   }

   public void onLandedUpon(World world, BlockPos pos, Entity entity, float distance) {
      if (!world.isClient && world.random.nextFloat() < distance - 0.5F && entity instanceof LivingEntity && (entity instanceof PlayerEntity || world.getGameRules().getBoolean(GameRules.MOB_GRIEFING)) && entity.getWidth() * entity.getWidth() * entity.getHeight() > 0.512F) {
         setToDirt(world.getBlockState(pos), world, pos);
      }

      super.onLandedUpon(world, pos, entity, distance);
   }

   public static void setToDirt(BlockState state, World world, BlockPos pos) {
      world.setBlockState(pos, pushEntitiesUpBeforeBlockChange(state, Blocks.DIRT.getDefaultState(), world, pos));
   }

   private static boolean hasCrop(BlockView world, BlockPos pos) {
      Block block = world.getBlockState(pos.up()).getBlock();
      return block instanceof CropBlock || block instanceof StemBlock || block instanceof AttachedStemBlock;
   }

   private static boolean isWaterNearby(WorldView worldView, BlockPos pos) {
      Iterator var2 = BlockPos.iterate(pos.add(-4, 0, -4), pos.add(4, 1, 4)).iterator();

      BlockPos blockPos;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         blockPos = (BlockPos)var2.next();
      } while(!worldView.getFluidState(blockPos).matches(FluidTags.WATER));

      return true;
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(MOISTURE);
   }

   public boolean canPlaceAtSide(BlockState world, BlockView view, BlockPos pos, BlockPlacementEnvironment env) {
      return false;
   }

   @Environment(EnvType.CLIENT)
   public boolean hasInWallOverlay(BlockState state, BlockView view, BlockPos pos) {
      return true;
   }

   static {
      MOISTURE = Properties.MOISTURE;
      SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);
   }
}
