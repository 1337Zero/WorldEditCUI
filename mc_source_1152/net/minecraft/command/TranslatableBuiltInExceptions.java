package net.minecraft.command;

import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.text.TranslatableText;

public class TranslatableBuiltInExceptions implements BuiltInExceptionProvider {
   private static final Dynamic2CommandExceptionType DOUBLE_TOO_LOW = new Dynamic2CommandExceptionType((found, min) -> {
      return new TranslatableText("argument.double.low", new Object[]{min, found});
   });
   private static final Dynamic2CommandExceptionType DOUBLE_TOO_HIGH = new Dynamic2CommandExceptionType((found, max) -> {
      return new TranslatableText("argument.double.big", new Object[]{max, found});
   });
   private static final Dynamic2CommandExceptionType FLOAT_TOO_LOW = new Dynamic2CommandExceptionType((found, min) -> {
      return new TranslatableText("argument.float.low", new Object[]{min, found});
   });
   private static final Dynamic2CommandExceptionType FLOAT_TOO_HIGH = new Dynamic2CommandExceptionType((found, max) -> {
      return new TranslatableText("argument.float.big", new Object[]{max, found});
   });
   private static final Dynamic2CommandExceptionType INTEGER_TOO_LOW = new Dynamic2CommandExceptionType((found, min) -> {
      return new TranslatableText("argument.integer.low", new Object[]{min, found});
   });
   private static final Dynamic2CommandExceptionType INTEGER_TOO_HIGH = new Dynamic2CommandExceptionType((found, max) -> {
      return new TranslatableText("argument.integer.big", new Object[]{max, found});
   });
   private static final Dynamic2CommandExceptionType LONG_TOO_LOW = new Dynamic2CommandExceptionType((found, min) -> {
      return new TranslatableText("argument.long.low", new Object[]{min, found});
   });
   private static final Dynamic2CommandExceptionType LONG_TOO_HIGH = new Dynamic2CommandExceptionType((found, max) -> {
      return new TranslatableText("argument.long.big", new Object[]{max, found});
   });
   private static final DynamicCommandExceptionType LITERAL_INCORRECT = new DynamicCommandExceptionType((expected) -> {
      return new TranslatableText("argument.literal.incorrect", new Object[]{expected});
   });
   private static final SimpleCommandExceptionType READER_EXPECTED_START_QUOTE = new SimpleCommandExceptionType(new TranslatableText("parsing.quote.expected.start", new Object[0]));
   private static final SimpleCommandExceptionType READER_EXPECTED_END_QUOTE = new SimpleCommandExceptionType(new TranslatableText("parsing.quote.expected.end", new Object[0]));
   private static final DynamicCommandExceptionType READER_INVALID_ESCAPE = new DynamicCommandExceptionType((character) -> {
      return new TranslatableText("parsing.quote.escape", new Object[]{character});
   });
   private static final DynamicCommandExceptionType READER_INVALID_BOOL = new DynamicCommandExceptionType((value) -> {
      return new TranslatableText("parsing.bool.invalid", new Object[]{value});
   });
   private static final DynamicCommandExceptionType READER_INVALID_INT = new DynamicCommandExceptionType((value) -> {
      return new TranslatableText("parsing.int.invalid", new Object[]{value});
   });
   private static final SimpleCommandExceptionType READER_EXPECTED_INT = new SimpleCommandExceptionType(new TranslatableText("parsing.int.expected", new Object[0]));
   private static final DynamicCommandExceptionType READER_INVALID_LONG = new DynamicCommandExceptionType((value) -> {
      return new TranslatableText("parsing.long.invalid", new Object[]{value});
   });
   private static final SimpleCommandExceptionType READER_EXPECTED_LONG = new SimpleCommandExceptionType(new TranslatableText("parsing.long.expected", new Object[0]));
   private static final DynamicCommandExceptionType READER_INVALID_DOUBLE = new DynamicCommandExceptionType((value) -> {
      return new TranslatableText("parsing.double.invalid", new Object[]{value});
   });
   private static final SimpleCommandExceptionType READER_EXPECTED_DOUBLE = new SimpleCommandExceptionType(new TranslatableText("parsing.double.expected", new Object[0]));
   private static final DynamicCommandExceptionType READER_INVALID_FLOAT = new DynamicCommandExceptionType((value) -> {
      return new TranslatableText("parsing.float.invalid", new Object[]{value});
   });
   private static final SimpleCommandExceptionType READER_EXPECTED_FLOAT = new SimpleCommandExceptionType(new TranslatableText("parsing.float.expected", new Object[0]));
   private static final SimpleCommandExceptionType READER_EXPECTED_BOOL = new SimpleCommandExceptionType(new TranslatableText("parsing.bool.expected", new Object[0]));
   private static final DynamicCommandExceptionType READER_EXPECTED_SYMBOL = new DynamicCommandExceptionType((symbol) -> {
      return new TranslatableText("parsing.expected", new Object[]{symbol});
   });
   private static final SimpleCommandExceptionType DISPATCHER_UNKNOWN_COMMAND = new SimpleCommandExceptionType(new TranslatableText("command.unknown.command", new Object[0]));
   private static final SimpleCommandExceptionType DISPATCHER_UNKNOWN_ARGUMENT = new SimpleCommandExceptionType(new TranslatableText("command.unknown.argument", new Object[0]));
   private static final SimpleCommandExceptionType DISPATCHER_EXPECTED_ARGUMENT_SEPARATOR = new SimpleCommandExceptionType(new TranslatableText("command.expected.separator", new Object[0]));
   private static final DynamicCommandExceptionType DISPATCHER_PARSE_EXCEPTION = new DynamicCommandExceptionType((message) -> {
      return new TranslatableText("command.exception", new Object[]{message});
   });

   public Dynamic2CommandExceptionType doubleTooLow() {
      return DOUBLE_TOO_LOW;
   }

   public Dynamic2CommandExceptionType doubleTooHigh() {
      return DOUBLE_TOO_HIGH;
   }

   public Dynamic2CommandExceptionType floatTooLow() {
      return FLOAT_TOO_LOW;
   }

   public Dynamic2CommandExceptionType floatTooHigh() {
      return FLOAT_TOO_HIGH;
   }

   public Dynamic2CommandExceptionType integerTooLow() {
      return INTEGER_TOO_LOW;
   }

   public Dynamic2CommandExceptionType integerTooHigh() {
      return INTEGER_TOO_HIGH;
   }

   public Dynamic2CommandExceptionType longTooLow() {
      return LONG_TOO_LOW;
   }

   public Dynamic2CommandExceptionType longTooHigh() {
      return LONG_TOO_HIGH;
   }

   public DynamicCommandExceptionType literalIncorrect() {
      return LITERAL_INCORRECT;
   }

   public SimpleCommandExceptionType readerExpectedStartOfQuote() {
      return READER_EXPECTED_START_QUOTE;
   }

   public SimpleCommandExceptionType readerExpectedEndOfQuote() {
      return READER_EXPECTED_END_QUOTE;
   }

   public DynamicCommandExceptionType readerInvalidEscape() {
      return READER_INVALID_ESCAPE;
   }

   public DynamicCommandExceptionType readerInvalidBool() {
      return READER_INVALID_BOOL;
   }

   public DynamicCommandExceptionType readerInvalidInt() {
      return READER_INVALID_INT;
   }

   public SimpleCommandExceptionType readerExpectedInt() {
      return READER_EXPECTED_INT;
   }

   public DynamicCommandExceptionType readerInvalidLong() {
      return READER_INVALID_LONG;
   }

   public SimpleCommandExceptionType readerExpectedLong() {
      return READER_EXPECTED_LONG;
   }

   public DynamicCommandExceptionType readerInvalidDouble() {
      return READER_INVALID_DOUBLE;
   }

   public SimpleCommandExceptionType readerExpectedDouble() {
      return READER_EXPECTED_DOUBLE;
   }

   public DynamicCommandExceptionType readerInvalidFloat() {
      return READER_INVALID_FLOAT;
   }

   public SimpleCommandExceptionType readerExpectedFloat() {
      return READER_EXPECTED_FLOAT;
   }

   public SimpleCommandExceptionType readerExpectedBool() {
      return READER_EXPECTED_BOOL;
   }

   public DynamicCommandExceptionType readerExpectedSymbol() {
      return READER_EXPECTED_SYMBOL;
   }

   public SimpleCommandExceptionType dispatcherUnknownCommand() {
      return DISPATCHER_UNKNOWN_COMMAND;
   }

   public SimpleCommandExceptionType dispatcherUnknownArgument() {
      return DISPATCHER_UNKNOWN_ARGUMENT;
   }

   public SimpleCommandExceptionType dispatcherExpectedArgumentSeparator() {
      return DISPATCHER_EXPECTED_ARGUMENT_SEPARATOR;
   }

   public DynamicCommandExceptionType dispatcherParseException() {
      return DISPATCHER_PARSE_EXCEPTION;
   }
}
