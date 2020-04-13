package net.minecraft.block;

import net.minecraft.client.network.ClientDummyContainerProvider;
import net.minecraft.container.BlockContext;
import net.minecraft.container.LoomContainer;
import net.minecraft.container.NameableContainerProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LoomBlock extends HorizontalFacingBlock {
   private static final TranslatableText CONTAINER_NAME = new TranslatableText("container.loom", new Object[0]);

   protected LoomBlock(Block.Settings settings) {
      super(settings);
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         return ActionResult.SUCCESS;
      } else {
         player.openContainer(state.createContainerProvider(world, pos));
         player.incrementStat(Stats.INTERACT_WITH_LOOM);
         return ActionResult.SUCCESS;
      }
   }

   public NameableContainerProvider createContainerProvider(BlockState state, World world, BlockPos pos) {
      return new ClientDummyContainerProvider((i, playerInventory, playerEntity) -> {
         return new LoomContainer(i, playerInventory, BlockContext.create(world, pos));
      }, CONTAINER_NAME);
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }
}
