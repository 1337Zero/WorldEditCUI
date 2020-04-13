package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.network.packet.PlayerSpawnPositionS2CPacket;
import net.minecraft.command.arguments.BlockPosArgumentType;
import net.minecraft.network.Packet;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;

public class SetWorldSpawnCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("setworldspawn").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), new BlockPos(((ServerCommandSource)commandContext.getSource()).getPosition()));
      })).then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), BlockPosArgumentType.getBlockPos(commandContext, "pos"));
      })));
   }

   private static int execute(ServerCommandSource source, BlockPos pos) {
      source.getWorld().setSpawnPos(pos);
      source.getMinecraftServer().getPlayerManager().sendToAll((Packet)(new PlayerSpawnPositionS2CPacket(pos)));
      source.sendFeedback(new TranslatableText("commands.setworldspawn.success", new Object[]{pos.getX(), pos.getY(), pos.getZ()}), true);
      return 1;
   }
}
