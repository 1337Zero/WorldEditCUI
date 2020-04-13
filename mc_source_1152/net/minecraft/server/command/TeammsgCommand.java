package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.command.arguments.MessageArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class TeammsgCommand {
   private static final SimpleCommandExceptionType NO_TEAM_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.teammsg.failed.noteam", new Object[0]));

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("teammsg").then(CommandManager.argument("message", MessageArgumentType.message()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), MessageArgumentType.getMessage(commandContext, "message"));
      })));
      dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("tm").redirect(literalCommandNode));
   }

   private static int execute(ServerCommandSource source, Text message) throws CommandSyntaxException {
      Entity entity = source.getEntityOrThrow();
      Team team = (Team)entity.getScoreboardTeam();
      if (team == null) {
         throw NO_TEAM_EXCEPTION.create();
      } else {
         Consumer<Style> consumer = (style) -> {
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.type.team.hover", new Object[0]))).setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/teammsg "));
         };
         Text text = team.getFormattedName().styled(consumer);
         Iterator var6 = text.getSiblings().iterator();

         while(var6.hasNext()) {
            Text text2 = (Text)var6.next();
            text2.styled(consumer);
         }

         List<ServerPlayerEntity> list = source.getMinecraftServer().getPlayerManager().getPlayerList();
         Iterator var10 = list.iterator();

         while(var10.hasNext()) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var10.next();
            if (serverPlayerEntity == entity) {
               serverPlayerEntity.sendMessage(new TranslatableText("chat.type.team.sent", new Object[]{text, source.getDisplayName(), message.deepCopy()}));
            } else if (serverPlayerEntity.getScoreboardTeam() == team) {
               serverPlayerEntity.sendMessage(new TranslatableText("chat.type.team.text", new Object[]{text, source.getDisplayName(), message.deepCopy()}));
            }
         }

         return list.size();
      }
   }
}
