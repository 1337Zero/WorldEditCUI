package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.client.network.ClientDummyContainerProvider;
import net.minecraft.container.BlockContext;
import net.minecraft.container.CartographyTableContainer;
import net.minecraft.container.NameableContainerProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CartographyTableBlock extends Block {
   private static final TranslatableText CONTAINER_NAME = new TranslatableText("container.cartography_table", new Object[0]);

   protected CartographyTableBlock(Block.Settings settings) {
      super(settings);
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         return ActionResult.SUCCESS;
      } else {
         player.openContainer(state.createContainerProvider(world, pos));
         player.incrementStat(Stats.INTERACT_WITH_CARTOGRAPHY_TABLE);
         return ActionResult.SUCCESS;
      }
   }

   @Nullable
   public NameableContainerProvider createContainerProvider(BlockState state, World world, BlockPos pos) {
      return new ClientDummyContainerProvider((i, playerInventory, playerEntity) -> {
         return new CartographyTableContainer(i, playerInventory, BlockContext.create(world, pos));
      }, CONTAINER_NAME);
   }
}
