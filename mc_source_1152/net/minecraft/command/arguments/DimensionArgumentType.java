package net.minecraft.command.arguments;

import com.google.common.collect.Streams;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;

public class DimensionArgumentType implements ArgumentType<DimensionType> {
   private static final Collection<String> EXAMPLES;
   public static final DynamicCommandExceptionType INVALID_DIMENSION_EXCEPTION;

   public DimensionType parse(StringReader stringReader) throws CommandSyntaxException {
      Identifier identifier = Identifier.fromCommandInput(stringReader);
      return (DimensionType)Registry.DIMENSION_TYPE.getOrEmpty(identifier).orElseThrow(() -> {
         return INVALID_DIMENSION_EXCEPTION.create(identifier);
      });
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return CommandSource.suggestIdentifiers(Streams.stream(DimensionType.getAll()).map(DimensionType::getId), builder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static DimensionArgumentType dimension() {
      return new DimensionArgumentType();
   }

   public static DimensionType getDimensionArgument(CommandContext<ServerCommandSource> context, String name) {
      return (DimensionType)context.getArgument(name, DimensionType.class);
   }

   static {
      EXAMPLES = (Collection)Stream.of(DimensionType.OVERWORLD, DimensionType.THE_NETHER).map((dimensionType) -> {
         return DimensionType.getId(dimensionType).toString();
      }).collect(Collectors.toList());
      INVALID_DIMENSION_EXCEPTION = new DynamicCommandExceptionType((object) -> {
         return new TranslatableText("argument.dimension.invalid", new Object[]{object});
      });
   }
}
