package net.minecraft.server.dedicated.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

public class StopCommand {
   public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher) {
      commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("stop").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(4);
      })).executes((commandContext) -> {
         ((ServerCommandSource)commandContext.getSource()).sendFeedback(new TranslatableText("commands.stop.stopping", new Object[0]), true);
         ((ServerCommandSource)commandContext.getSource()).getMinecraftServer().stop(false);
         return 1;
      }));
   }
}
