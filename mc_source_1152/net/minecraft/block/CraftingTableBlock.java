package net.minecraft.block;

import net.minecraft.client.network.ClientDummyContainerProvider;
import net.minecraft.container.BlockContext;
import net.minecraft.container.CraftingTableContainer;
import net.minecraft.container.NameableContainerProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CraftingTableBlock extends Block {
   private static final Text CONTAINER_NAME = new TranslatableText("container.crafting", new Object[0]);

   protected CraftingTableBlock(Block.Settings settings) {
      super(settings);
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         return ActionResult.SUCCESS;
      } else {
         player.openContainer(state.createContainerProvider(world, pos));
         player.incrementStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
         return ActionResult.SUCCESS;
      }
   }

   public NameableContainerProvider createContainerProvider(BlockState state, World world, BlockPos pos) {
      return new ClientDummyContainerProvider((i, playerInventory, playerEntity) -> {
         return new CraftingTableContainer(i, playerInventory, BlockContext.create(world, pos));
      }, CONTAINER_NAME);
   }
}
