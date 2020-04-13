package net.minecraft.server.dedicated.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.command.arguments.MessageArgumentType;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;

public class BanCommand {
   private static final SimpleCommandExceptionType ALREADY_BANNED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.ban.failed", new Object[0]));

   public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher) {
      commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("ban").requires((serverCommandSource) -> {
         return serverCommandSource.getMinecraftServer().getPlayerManager().getUserBanList().isEnabled() && serverCommandSource.hasPermissionLevel(3);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("targets", GameProfileArgumentType.gameProfile()).executes((commandContext) -> {
         return ban((ServerCommandSource)commandContext.getSource(), GameProfileArgumentType.getProfileArgument(commandContext, "targets"), (Text)null);
      })).then(CommandManager.argument("reason", MessageArgumentType.message()).executes((commandContext) -> {
         return ban((ServerCommandSource)commandContext.getSource(), GameProfileArgumentType.getProfileArgument(commandContext, "targets"), MessageArgumentType.getMessage(commandContext, "reason"));
      }))));
   }

   private static int ban(ServerCommandSource serverCommandSource, Collection<GameProfile> collection, @Nullable Text text) throws CommandSyntaxException {
      BannedPlayerList bannedPlayerList = serverCommandSource.getMinecraftServer().getPlayerManager().getUserBanList();
      int i = 0;
      Iterator var5 = collection.iterator();

      while(var5.hasNext()) {
         GameProfile gameProfile = (GameProfile)var5.next();
         if (!bannedPlayerList.contains(gameProfile)) {
            BannedPlayerEntry bannedPlayerEntry = new BannedPlayerEntry(gameProfile, (Date)null, serverCommandSource.getName(), (Date)null, text == null ? null : text.getString());
            bannedPlayerList.add(bannedPlayerEntry);
            ++i;
            serverCommandSource.sendFeedback(new TranslatableText("commands.ban.success", new Object[]{Texts.toText(gameProfile), bannedPlayerEntry.getReason()}), true);
            ServerPlayerEntity serverPlayerEntity = serverCommandSource.getMinecraftServer().getPlayerManager().getPlayer(gameProfile.getId());
            if (serverPlayerEntity != null) {
               serverPlayerEntity.networkHandler.disconnect(new TranslatableText("multiplayer.disconnect.banned", new Object[0]));
            }
         }
      }

      if (i == 0) {
         throw ALREADY_BANNED_EXCEPTION.create();
      } else {
         return i;
      }
   }
}
