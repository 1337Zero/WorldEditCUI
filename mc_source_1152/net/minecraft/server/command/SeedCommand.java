package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class SeedCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("seed").requires((serverCommandSource) -> {
         return serverCommandSource.getMinecraftServer().isSinglePlayer() || serverCommandSource.hasPermissionLevel(2);
      })).executes((commandContext) -> {
         long l = ((ServerCommandSource)commandContext.getSource()).getWorld().getSeed();
         Text text = Texts.bracketed((new LiteralText(String.valueOf(l))).styled((style) -> {
            style.setColor(Formatting.GREEN).setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.valueOf(l))).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.copy.click", new Object[0]))).setInsertion(String.valueOf(l));
         }));
         ((ServerCommandSource)commandContext.getSource()).sendFeedback(new TranslatableText("commands.seed.success", new Object[]{text}), false);
         return (int)l;
      }));
   }
}
