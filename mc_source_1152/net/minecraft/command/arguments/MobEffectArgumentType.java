package net.minecraft.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class MobEffectArgumentType implements ArgumentType<StatusEffect> {
   private static final Collection<String> EXAMPLES = Arrays.asList("spooky", "effect");
   public static final DynamicCommandExceptionType INVALID_EFFECT_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("effect.effectNotFound", new Object[]{object});
   });

   public static MobEffectArgumentType mobEffect() {
      return new MobEffectArgumentType();
   }

   public static StatusEffect getMobEffect(CommandContext<ServerCommandSource> commandContext, String string) throws CommandSyntaxException {
      return (StatusEffect)commandContext.getArgument(string, StatusEffect.class);
   }

   public StatusEffect parse(StringReader stringReader) throws CommandSyntaxException {
      Identifier identifier = Identifier.fromCommandInput(stringReader);
      return (StatusEffect)Registry.STATUS_EFFECT.getOrEmpty(identifier).orElseThrow(() -> {
         return INVALID_EFFECT_EXCEPTION.create(identifier);
      });
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return CommandSource.suggestIdentifiers((Iterable)Registry.STATUS_EFFECT.getIds(), builder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
