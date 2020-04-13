package net.minecraft.text;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class BaseText implements Text {
   protected final List<Text> siblings = Lists.newArrayList();
   private Style style;

   public Text append(Text text) {
      text.getStyle().setParent(this.getStyle());
      this.siblings.add(text);
      return this;
   }

   public List<Text> getSiblings() {
      return this.siblings;
   }

   public Text setStyle(Style style) {
      this.style = style;
      Iterator var2 = this.siblings.iterator();

      while(var2.hasNext()) {
         Text text = (Text)var2.next();
         text.getStyle().setParent(this.getStyle());
      }

      return this;
   }

   public Style getStyle() {
      if (this.style == null) {
         this.style = new Style();
         Iterator var1 = this.siblings.iterator();

         while(var1.hasNext()) {
            Text text = (Text)var1.next();
            text.getStyle().setParent(this.style);
         }
      }

      return this.style;
   }

   public Stream<Text> stream() {
      return Streams.concat(new Stream[]{Stream.of(this), this.siblings.stream().flatMap(Text::stream)});
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof BaseText)) {
         return false;
      } else {
         BaseText baseText = (BaseText)obj;
         return this.siblings.equals(baseText.siblings) && this.getStyle().equals(baseText.getStyle());
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.getStyle(), this.siblings});
   }

   public String toString() {
      return "BaseComponent{style=" + this.style + ", siblings=" + this.siblings + '}';
   }
}
