package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import net.minecraft.command.arguments.BlockPosArgumentType;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;

public class SpawnPointCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("spawnpoint").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), Collections.singleton(((ServerCommandSource)commandContext.getSource()).getPlayer()), new BlockPos(((ServerCommandSource)commandContext.getSource()).getPosition()));
      })).then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), new BlockPos(((ServerCommandSource)commandContext.getSource()).getPosition()));
      })).then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), BlockPosArgumentType.getBlockPos(commandContext, "pos"));
      }))));
   }

   private static int execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, BlockPos pos) {
      Iterator var3 = targets.iterator();

      while(var3.hasNext()) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var3.next();
         serverPlayerEntity.setPlayerSpawn(pos, true, false);
      }

      if (targets.size() == 1) {
         source.sendFeedback(new TranslatableText("commands.spawnpoint.success.single", new Object[]{pos.getX(), pos.getY(), pos.getZ(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()}), true);
      } else {
         source.sendFeedback(new TranslatableText("commands.spawnpoint.success.multiple", new Object[]{pos.getX(), pos.getY(), pos.getZ(), targets.size()}), true);
      }

      return targets.size();
   }
}
