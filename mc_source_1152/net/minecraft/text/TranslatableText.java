package net.minecraft.text;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Language;

public class TranslatableText extends BaseText implements ParsableText {
   private static final Language EMPTY_LANGUAGE = new Language();
   private static final Language LANGUAGE = Language.getInstance();
   private final String key;
   private final Object[] args;
   private final Object lock = new Object();
   private long languageReloadTimestamp = -1L;
   protected final List<Text> translations = Lists.newArrayList();
   public static final Pattern ARG_FORMAT = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

   public TranslatableText(String key, Object... args) {
      this.key = key;
      this.args = args;

      for(int i = 0; i < args.length; ++i) {
         Object object = args[i];
         if (object instanceof Text) {
            Text text = ((Text)object).deepCopy();
            this.args[i] = text;
            text.getStyle().setParent(this.getStyle());
         } else if (object == null) {
            this.args[i] = "null";
         }
      }

   }

   @VisibleForTesting
   synchronized void updateTranslations() {
      synchronized(this.lock) {
         long l = LANGUAGE.getTimeLoaded();
         if (l == this.languageReloadTimestamp) {
            return;
         }

         this.languageReloadTimestamp = l;
         this.translations.clear();
      }

      String string = LANGUAGE.translate(this.key);

      try {
         this.setTranslation(string);
      } catch (TranslationException var5) {
         this.translations.clear();
         this.translations.add(new LiteralText(string));
      }

   }

   protected void setTranslation(String translation) {
      Matcher matcher = ARG_FORMAT.matcher(translation);

      try {
         int i = 0;

         int j;
         int l;
         for(j = 0; matcher.find(j); j = l) {
            int k = matcher.start();
            l = matcher.end();
            if (k > j) {
               Text text = new LiteralText(String.format(translation.substring(j, k)));
               text.getStyle().setParent(this.getStyle());
               this.translations.add(text);
            }

            String string = matcher.group(2);
            String string2 = translation.substring(k, l);
            if ("%".equals(string) && "%%".equals(string2)) {
               Text text2 = new LiteralText("%");
               text2.getStyle().setParent(this.getStyle());
               this.translations.add(text2);
            } else {
               if (!"s".equals(string)) {
                  throw new TranslationException(this, "Unsupported format: '" + string2 + "'");
               }

               String string3 = matcher.group(1);
               int m = string3 != null ? Integer.parseInt(string3) - 1 : i++;
               if (m < this.args.length) {
                  this.translations.add(this.getArg(m));
               }
            }
         }

         if (j < translation.length()) {
            Text text3 = new LiteralText(String.format(translation.substring(j)));
            text3.getStyle().setParent(this.getStyle());
            this.translations.add(text3);
         }

      } catch (IllegalFormatException var11) {
         throw new TranslationException(this, var11);
      }
   }

   private Text getArg(int index) {
      if (index >= this.args.length) {
         throw new TranslationException(this, index);
      } else {
         Object object = this.args[index];
         Object text2;
         if (object instanceof Text) {
            text2 = (Text)object;
         } else {
            text2 = new LiteralText(object == null ? "null" : object.toString());
            ((Text)text2).getStyle().setParent(this.getStyle());
         }

         return (Text)text2;
      }
   }

   public Text setStyle(Style style) {
      super.setStyle(style);
      Object[] var2 = this.args;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Object object = var2[var4];
         if (object instanceof Text) {
            ((Text)object).getStyle().setParent(this.getStyle());
         }
      }

      if (this.languageReloadTimestamp > -1L) {
         Iterator var6 = this.translations.iterator();

         while(var6.hasNext()) {
            Text text = (Text)var6.next();
            text.getStyle().setParent(style);
         }
      }

      return this;
   }

   public Stream<Text> stream() {
      this.updateTranslations();
      return Streams.concat(new Stream[]{this.translations.stream(), this.siblings.stream()}).flatMap(Text::stream);
   }

   public String asString() {
      this.updateTranslations();
      StringBuilder stringBuilder = new StringBuilder();
      Iterator var2 = this.translations.iterator();

      while(var2.hasNext()) {
         Text text = (Text)var2.next();
         stringBuilder.append(text.asString());
      }

      return stringBuilder.toString();
   }

   public TranslatableText copy() {
      Object[] objects = new Object[this.args.length];

      for(int i = 0; i < this.args.length; ++i) {
         if (this.args[i] instanceof Text) {
            objects[i] = ((Text)this.args[i]).deepCopy();
         } else {
            objects[i] = this.args[i];
         }
      }

      return new TranslatableText(this.key, objects);
   }

   public Text parse(@Nullable ServerCommandSource source, @Nullable Entity sender, int depth) throws CommandSyntaxException {
      Object[] objects = new Object[this.args.length];

      for(int i = 0; i < objects.length; ++i) {
         Object object = this.args[i];
         if (object instanceof Text) {
            objects[i] = Texts.parse(source, (Text)object, sender, depth);
         } else {
            objects[i] = object;
         }
      }

      return new TranslatableText(this.key, objects);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof TranslatableText)) {
         return false;
      } else {
         TranslatableText translatableText = (TranslatableText)o;
         return Arrays.equals(this.args, translatableText.args) && this.key.equals(translatableText.key) && super.equals(o);
      }
   }

   public int hashCode() {
      int i = super.hashCode();
      i = 31 * i + this.key.hashCode();
      i = 31 * i + Arrays.hashCode(this.args);
      return i;
   }

   public String toString() {
      return "TranslatableComponent{key='" + this.key + '\'' + ", args=" + Arrays.toString(this.args) + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
   }

   public String getKey() {
      return this.key;
   }

   public Object[] getArgs() {
      return this.args;
   }
}
