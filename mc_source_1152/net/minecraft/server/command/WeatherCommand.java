package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.text.TranslatableText;

public class WeatherCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("weather").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(((LiteralArgumentBuilder)CommandManager.literal("clear").executes((commandContext) -> {
         return executeClear((ServerCommandSource)commandContext.getSource(), 6000);
      })).then(CommandManager.argument("duration", IntegerArgumentType.integer(0, 1000000)).executes((commandContext) -> {
         return executeClear((ServerCommandSource)commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "duration") * 20);
      })))).then(((LiteralArgumentBuilder)CommandManager.literal("rain").executes((commandContext) -> {
         return executeRain((ServerCommandSource)commandContext.getSource(), 6000);
      })).then(CommandManager.argument("duration", IntegerArgumentType.integer(0, 1000000)).executes((commandContext) -> {
         return executeRain((ServerCommandSource)commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "duration") * 20);
      })))).then(((LiteralArgumentBuilder)CommandManager.literal("thunder").executes((commandContext) -> {
         return executeThunder((ServerCommandSource)commandContext.getSource(), 6000);
      })).then(CommandManager.argument("duration", IntegerArgumentType.integer(0, 1000000)).executes((commandContext) -> {
         return executeThunder((ServerCommandSource)commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "duration") * 20);
      }))));
   }

   private static int executeClear(ServerCommandSource source, int duration) {
      source.getWorld().getLevelProperties().setClearWeatherTime(duration);
      source.getWorld().getLevelProperties().setRainTime(0);
      source.getWorld().getLevelProperties().setThunderTime(0);
      source.getWorld().getLevelProperties().setRaining(false);
      source.getWorld().getLevelProperties().setThundering(false);
      source.sendFeedback(new TranslatableText("commands.weather.set.clear", new Object[0]), true);
      return duration;
   }

   private static int executeRain(ServerCommandSource source, int duration) {
      source.getWorld().getLevelProperties().setClearWeatherTime(0);
      source.getWorld().getLevelProperties().setRainTime(duration);
      source.getWorld().getLevelProperties().setThunderTime(duration);
      source.getWorld().getLevelProperties().setRaining(true);
      source.getWorld().getLevelProperties().setThundering(false);
      source.sendFeedback(new TranslatableText("commands.weather.set.rain", new Object[0]), true);
      return duration;
   }

   private static int executeThunder(ServerCommandSource source, int duration) {
      source.getWorld().getLevelProperties().setClearWeatherTime(0);
      source.getWorld().getLevelProperties().setRainTime(duration);
      source.getWorld().getLevelProperties().setThunderTime(duration);
      source.getWorld().getLevelProperties().setRaining(true);
      source.getWorld().getLevelProperties().setThundering(true);
      source.sendFeedback(new TranslatableText("commands.weather.set.thunder", new Object[0]), true);
      return duration;
   }
}
