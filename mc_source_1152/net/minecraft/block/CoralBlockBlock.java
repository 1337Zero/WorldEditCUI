package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;

public class CoralBlockBlock extends Block {
   private final Block deadCoralBlock;

   public CoralBlockBlock(Block deadCoralBlock, Block.Settings settings) {
      super(settings);
      this.deadCoralBlock = deadCoralBlock;
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!this.isInWater(world, pos)) {
         world.setBlockState(pos, this.deadCoralBlock.getDefaultState(), 2);
      }

   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
      if (!this.isInWater(world, pos)) {
         world.getBlockTickScheduler().schedule(pos, this, 60 + world.getRandom().nextInt(40));
      }

      return super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
   }

   protected boolean isInWater(BlockView world, BlockPos pos) {
      Direction[] var3 = Direction.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Direction direction = var3[var5];
         FluidState fluidState = world.getFluidState(pos.offset(direction));
         if (fluidState.matches(FluidTags.WATER)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      if (!this.isInWater(ctx.getWorld(), ctx.getBlockPos())) {
         ctx.getWorld().getBlockTickScheduler().schedule(ctx.getBlockPos(), this, 60 + ctx.getWorld().getRandom().nextInt(40));
      }

      return this.getDefaultState();
   }
}
