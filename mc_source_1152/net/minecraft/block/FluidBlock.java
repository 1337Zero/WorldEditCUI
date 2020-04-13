package net.minecraft.block;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.fluid.BaseFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class FluidBlock extends Block implements FluidDrainable {
   public static final IntProperty LEVEL;
   protected final BaseFluid fluid;
   private final List<FluidState> statesByLevel;

   protected FluidBlock(BaseFluid fluid, Block.Settings settings) {
      super(settings);
      this.fluid = fluid;
      this.statesByLevel = Lists.newArrayList();
      this.statesByLevel.add(fluid.getStill(false));

      for(int i = 1; i < 8; ++i) {
         this.statesByLevel.add(fluid.getFlowing(8 - i, false));
      }

      this.statesByLevel.add(fluid.getFlowing(8, true));
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(LEVEL, 0));
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      world.getFluidState(pos).onRandomTick(world, pos, random);
   }

   public boolean isTranslucent(BlockState state, BlockView view, BlockPos pos) {
      return false;
   }

   public boolean canPlaceAtSide(BlockState world, BlockView view, BlockPos pos, BlockPlacementEnvironment env) {
      return !this.fluid.matches(FluidTags.LAVA);
   }

   public FluidState getFluidState(BlockState state) {
      int i = (Integer)state.get(LEVEL);
      return (FluidState)this.statesByLevel.get(Math.min(i, 8));
   }

   @Environment(EnvType.CLIENT)
   public boolean isSideInvisible(BlockState state, BlockState neighbor, Direction facing) {
      return neighbor.getFluidState().getFluid().matchesType(this.fluid);
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.INVISIBLE;
   }

   public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
      return Collections.emptyList();
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return VoxelShapes.empty();
   }

   public int getTickRate(WorldView worldView) {
      return this.fluid.getTickRate(worldView);
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved) {
      if (this.receiveNeighborFluids(world, pos, state)) {
         world.getFluidTickScheduler().schedule(pos, state.getFluidState().getFluid(), this.getTickRate(world));
      }

   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
      if (state.getFluidState().isStill() || neighborState.getFluidState().isStill()) {
         world.getFluidTickScheduler().schedule(pos, state.getFluidState().getFluid(), this.getTickRate(world));
      }

      return super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos neighborPos, boolean moved) {
      if (this.receiveNeighborFluids(world, pos, state)) {
         world.getFluidTickScheduler().schedule(pos, state.getFluidState().getFluid(), this.getTickRate(world));
      }

   }

   public boolean receiveNeighborFluids(World world, BlockPos pos, BlockState state) {
      if (this.fluid.matches(FluidTags.LAVA)) {
         boolean bl = false;
         Direction[] var5 = Direction.values();
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            Direction direction = var5[var7];
            if (direction != Direction.DOWN && world.getFluidState(pos.offset(direction)).matches(FluidTags.WATER)) {
               bl = true;
               break;
            }
         }

         if (bl) {
            FluidState fluidState = world.getFluidState(pos);
            if (fluidState.isStill()) {
               world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
               this.playExtinguishSound(world, pos);
               return false;
            }

            if (fluidState.getHeight(world, pos) >= 0.44444445F) {
               world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
               this.playExtinguishSound(world, pos);
               return false;
            }
         }
      }

      return true;
   }

   private void playExtinguishSound(IWorld world, BlockPos pos) {
      world.playLevelEvent(1501, pos, 0);
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(LEVEL);
   }

   public Fluid tryDrainFluid(IWorld world, BlockPos pos, BlockState state) {
      if ((Integer)state.get(LEVEL) == 0) {
         world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
         return this.fluid;
      } else {
         return Fluids.EMPTY;
      }
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if (this.fluid.matches(FluidTags.LAVA)) {
         entity.setInLava();
      }

   }

   static {
      LEVEL = Properties.LEVEL_15;
   }
}
