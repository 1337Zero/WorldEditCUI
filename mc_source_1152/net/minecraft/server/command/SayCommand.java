package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.arguments.MessageArgumentType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class SayCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("say").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(CommandManager.argument("message", MessageArgumentType.message()).executes((commandContext) -> {
         Text text = MessageArgumentType.getMessage(commandContext, "message");
         ((ServerCommandSource)commandContext.getSource()).getMinecraftServer().getPlayerManager().sendToAll((Text)(new TranslatableText("chat.type.announcement", new Object[]{((ServerCommandSource)commandContext.getSource()).getDisplayName(), text})));
         return 1;
      })));
   }
}
