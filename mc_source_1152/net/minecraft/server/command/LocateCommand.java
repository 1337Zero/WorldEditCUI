package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class LocateCommand {
   private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.locate.failed", new Object[0]));

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("locate").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(CommandManager.literal("Pillager_Outpost").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), "Pillager_Outpost");
      }))).then(CommandManager.literal("Mineshaft").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), "Mineshaft");
      }))).then(CommandManager.literal("Mansion").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), "Mansion");
      }))).then(CommandManager.literal("Igloo").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), "Igloo");
      }))).then(CommandManager.literal("Desert_Pyramid").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), "Desert_Pyramid");
      }))).then(CommandManager.literal("Jungle_Pyramid").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), "Jungle_Pyramid");
      }))).then(CommandManager.literal("Swamp_Hut").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), "Swamp_Hut");
      }))).then(CommandManager.literal("Stronghold").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), "Stronghold");
      }))).then(CommandManager.literal("Monument").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), "Monument");
      }))).then(CommandManager.literal("Fortress").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), "Fortress");
      }))).then(CommandManager.literal("EndCity").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), "EndCity");
      }))).then(CommandManager.literal("Ocean_Ruin").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), "Ocean_Ruin");
      }))).then(CommandManager.literal("Buried_Treasure").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), "Buried_Treasure");
      }))).then(CommandManager.literal("Shipwreck").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), "Shipwreck");
      }))).then(CommandManager.literal("Village").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), "Village");
      })));
   }

   private static int execute(ServerCommandSource source, String structure) throws CommandSyntaxException {
      BlockPos blockPos = new BlockPos(source.getPosition());
      BlockPos blockPos2 = source.getWorld().locateStructure(structure, blockPos, 100, false);
      if (blockPos2 == null) {
         throw FAILED_EXCEPTION.create();
      } else {
         int i = MathHelper.floor(getDistance(blockPos.getX(), blockPos.getZ(), blockPos2.getX(), blockPos2.getZ()));
         Text text = Texts.bracketed(new TranslatableText("chat.coordinates", new Object[]{blockPos2.getX(), "~", blockPos2.getZ()})).styled((style) -> {
            style.setColor(Formatting.GREEN).setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + blockPos2.getX() + " ~ " + blockPos2.getZ())).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.coordinates.tooltip", new Object[0])));
         });
         source.sendFeedback(new TranslatableText("commands.locate.success", new Object[]{structure, text, i}), false);
         return i;
      }
   }

   private static float getDistance(int x1, int y1, int x2, int y2) {
      int i = x2 - x1;
      int j = y2 - y1;
      return MathHelper.sqrt((float)(i * i + j * j));
   }
}
