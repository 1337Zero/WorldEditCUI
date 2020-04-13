package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.command.arguments.MessageArgumentType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class MessageCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("msg").then(CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.argument("message", MessageArgumentType.message()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), MessageArgumentType.getMessage(commandContext, "message"));
      }))));
      dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("tell").redirect(literalCommandNode));
      dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("w").redirect(literalCommandNode));
   }

   private static int execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Text message) {
      Iterator var3 = targets.iterator();

      while(var3.hasNext()) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var3.next();
         serverPlayerEntity.sendMessage((new TranslatableText("commands.message.display.incoming", new Object[]{source.getDisplayName(), message.deepCopy()})).formatted(new Formatting[]{Formatting.GRAY, Formatting.ITALIC}));
         source.sendFeedback((new TranslatableText("commands.message.display.outgoing", new Object[]{serverPlayerEntity.getDisplayName(), message.deepCopy()})).formatted(new Formatting[]{Formatting.GRAY, Formatting.ITALIC}), false);
      }

      return targets.size();
   }
}
