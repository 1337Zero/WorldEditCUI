package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ScaffoldingBlock;
import net.minecraft.client.network.packet.ChatMessageS2CPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ScaffoldingItem extends BlockItem {
   public ScaffoldingItem(Block block, Item.Settings settings) {
      super(block, settings);
   }

   @Nullable
   public ItemPlacementContext getPlacementContext(ItemPlacementContext context) {
      BlockPos blockPos = context.getBlockPos();
      World world = context.getWorld();
      BlockState blockState = world.getBlockState(blockPos);
      Block block = this.getBlock();
      if (blockState.getBlock() != block) {
         return ScaffoldingBlock.calculateDistance(world, blockPos) == 7 ? null : context;
      } else {
         Direction direction2;
         if (context.shouldCancelInteraction()) {
            direction2 = context.hitsInsideBlock() ? context.getSide().getOpposite() : context.getSide();
         } else {
            direction2 = context.getSide() == Direction.UP ? context.getPlayerFacing() : Direction.UP;
         }

         int i = 0;
         BlockPos.Mutable mutable = (new BlockPos.Mutable(blockPos)).setOffset(direction2);

         while(i < 7) {
            if (!world.isClient && !World.isValid(mutable)) {
               PlayerEntity playerEntity = context.getPlayer();
               int j = world.getHeight();
               if (playerEntity instanceof ServerPlayerEntity && mutable.getY() >= j) {
                  ChatMessageS2CPacket chatMessageS2CPacket = new ChatMessageS2CPacket((new TranslatableText("build.tooHigh", new Object[]{j})).formatted(Formatting.RED), MessageType.GAME_INFO);
                  ((ServerPlayerEntity)playerEntity).networkHandler.sendPacket(chatMessageS2CPacket);
               }
               break;
            }

            blockState = world.getBlockState(mutable);
            if (blockState.getBlock() != this.getBlock()) {
               if (blockState.canReplace(context)) {
                  return ItemPlacementContext.offset(context, mutable, direction2);
               }
               break;
            }

            mutable.setOffset(direction2);
            if (direction2.getAxis().isHorizontal()) {
               ++i;
            }
         }

         return null;
      }
   }

   protected boolean checkStatePlacement() {
      return false;
   }
}
