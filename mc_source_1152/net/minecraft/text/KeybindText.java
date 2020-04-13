package net.minecraft.text;

import java.util.function.Function;
import java.util.function.Supplier;

public class KeybindText extends BaseText {
   public static Function<String, Supplier<String>> i18n = (key) -> {
      return () -> {
         return key;
      };
   };
   private final String key;
   private Supplier<String> name;

   public KeybindText(String key) {
      this.key = key;
   }

   public String asString() {
      if (this.name == null) {
         this.name = (Supplier)i18n.apply(this.key);
      }

      return (String)this.name.get();
   }

   public KeybindText copy() {
      return new KeybindText(this.key);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof KeybindText)) {
         return false;
      } else {
         KeybindText keybindText = (KeybindText)o;
         return this.key.equals(keybindText.key) && super.equals(o);
      }
   }

   public String toString() {
      return "KeybindComponent{keybind='" + this.key + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
   }

   public String getKey() {
      return this.key;
   }
}
