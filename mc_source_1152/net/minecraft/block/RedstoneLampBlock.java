package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class RedstoneLampBlock extends Block {
   public static final BooleanProperty LIT;

   public RedstoneLampBlock(Block.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)this.getDefaultState().with(LIT, false));
   }

   public int getLuminance(BlockState state) {
      return (Boolean)state.get(LIT) ? super.getLuminance(state) : 0;
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved) {
      super.onBlockAdded(state, world, pos, oldState, moved);
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(LIT, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos neighborPos, boolean moved) {
      if (!world.isClient) {
         boolean bl = (Boolean)state.get(LIT);
         if (bl != world.isReceivingRedstonePower(pos)) {
            if (bl) {
               world.getBlockTickScheduler().schedule(pos, this, 4);
            } else {
               world.setBlockState(pos, (BlockState)state.cycle(LIT), 2);
            }
         }

      }
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if ((Boolean)state.get(LIT) && !world.isReceivingRedstonePower(pos)) {
         world.setBlockState(pos, (BlockState)state.cycle(LIT), 2);
      }

   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(LIT);
   }

   public boolean allowsSpawning(BlockState state, BlockView view, BlockPos pos, EntityType<?> type) {
      return true;
   }

   static {
      LIT = RedstoneTorchBlock.LIT;
   }
}
