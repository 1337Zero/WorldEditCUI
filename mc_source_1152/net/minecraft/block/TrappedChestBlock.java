package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;

public class TrappedChestBlock extends ChestBlock {
   public TrappedChestBlock(Block.Settings settings) {
      super(settings, () -> {
         return BlockEntityType.TRAPPED_CHEST;
      });
   }

   public BlockEntity createBlockEntity(BlockView view) {
      return new TrappedChestBlockEntity();
   }

   protected Stat<Identifier> getOpenStat() {
      return Stats.CUSTOM.getOrCreateStat(Stats.TRIGGER_TRAPPED_CHEST);
   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   public int getWeakRedstonePower(BlockState state, BlockView view, BlockPos pos, Direction facing) {
      return MathHelper.clamp(ChestBlockEntity.getPlayersLookingInChestCount(view, pos), 0, 15);
   }

   public int getStrongRedstonePower(BlockState state, BlockView view, BlockPos pos, Direction facing) {
      return facing == Direction.UP ? state.getWeakRedstonePower(view, pos, facing) : 0;
   }
}
