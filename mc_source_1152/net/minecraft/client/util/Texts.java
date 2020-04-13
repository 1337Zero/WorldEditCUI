package net.minecraft.client.util;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class Texts {
   public static String getRenderChatMessage(String string, boolean forceColor) {
      return !forceColor && !MinecraftClient.getInstance().options.chatColors ? Formatting.strip(string) : string;
   }

   public static List<Text> wrapLines(Text text, int width, TextRenderer textRenderer, boolean bl, boolean forceColor) {
      int i = 0;
      Text text2 = new LiteralText("");
      List<Text> list = Lists.newArrayList();
      List<Text> list2 = Lists.newArrayList(text);

      for(int j = 0; j < list2.size(); ++j) {
         Text text3 = (Text)list2.get(j);
         String string = text3.asString();
         boolean bl2 = false;
         String string4;
         if (string.contains("\n")) {
            int k = string.indexOf(10);
            string4 = string.substring(k + 1);
            string = string.substring(0, k + 1);
            Text text4 = (new LiteralText(string4)).setStyle(text3.getStyle().deepCopy());
            list2.add(j + 1, text4);
            bl2 = true;
         }

         String string3 = getRenderChatMessage(text3.getStyle().asString() + string, forceColor);
         string4 = string3.endsWith("\n") ? string3.substring(0, string3.length() - 1) : string3;
         int l = textRenderer.getStringWidth(string4);
         Text text5 = (new LiteralText(string4)).setStyle(text3.getStyle().deepCopy());
         if (i + l > width) {
            String string5 = textRenderer.trimToWidth(string3, width - i, false);
            String string6 = string5.length() < string3.length() ? string3.substring(string5.length()) : null;
            if (string6 != null && !string6.isEmpty()) {
               int m = string6.charAt(0) != ' ' ? string5.lastIndexOf(32) : string5.length();
               if (m >= 0 && textRenderer.getStringWidth(string3.substring(0, m)) > 0) {
                  string5 = string3.substring(0, m);
                  if (bl) {
                     ++m;
                  }

                  string6 = string3.substring(m);
               } else if (i > 0 && !string3.contains(" ")) {
                  string5 = "";
                  string6 = string3;
               }

               Text text6 = (new LiteralText(string6)).setStyle(text3.getStyle().deepCopy());
               list2.add(j + 1, text6);
            }

            l = textRenderer.getStringWidth(string5);
            text5 = new LiteralText(string5);
            ((Text)text5).setStyle(text3.getStyle().deepCopy());
            bl2 = true;
         }

         if (i + l <= width) {
            i += l;
            text2.append((Text)text5);
         } else {
            bl2 = true;
         }

         if (bl2) {
            list.add(text2);
            i = 0;
            text2 = new LiteralText("");
         }
      }

      list.add(text2);
      return list;
   }
}
