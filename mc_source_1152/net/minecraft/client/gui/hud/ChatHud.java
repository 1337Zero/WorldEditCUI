package net.minecraft.client.gui.hud;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.options.ChatVisibility;
import net.minecraft.client.util.Texts;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ChatHud extends DrawableHelper {
   private static final Logger LOGGER = LogManager.getLogger();
   private final MinecraftClient client;
   private final List<String> messageHistory = Lists.newArrayList();
   private final List<ChatHudLine> messages = Lists.newArrayList();
   private final List<ChatHudLine> visibleMessages = Lists.newArrayList();
   private int scrolledLines;
   private boolean hasUnreadNewMessages;

   public ChatHud(MinecraftClient client) {
      this.client = client;
   }

   public void render(int ticks) {
      if (this.method_23677()) {
         int i = this.getVisibleLineCount();
         int j = this.visibleMessages.size();
         if (j > 0) {
            boolean bl = false;
            if (this.isChatFocused()) {
               bl = true;
            }

            double d = this.getChatScale();
            int k = MathHelper.ceil((double)this.getWidth() / d);
            RenderSystem.pushMatrix();
            RenderSystem.translatef(2.0F, 8.0F, 0.0F);
            RenderSystem.scaled(d, d, 1.0D);
            double e = this.client.options.chatOpacity * 0.8999999761581421D + 0.10000000149011612D;
            double f = this.client.options.textBackgroundOpacity;
            int l = 0;
            Matrix4f matrix4f = Matrix4f.translate(0.0F, 0.0F, -100.0F);

            int n;
            int o;
            int p;
            for(int m = 0; m + this.scrolledLines < this.visibleMessages.size() && m < i; ++m) {
               ChatHudLine chatHudLine = (ChatHudLine)this.visibleMessages.get(m + this.scrolledLines);
               if (chatHudLine != null) {
                  n = ticks - chatHudLine.getCreationTick();
                  if (n < 200 || bl) {
                     double g = bl ? 1.0D : getMessageOpacityMultiplier(n);
                     o = (int)(255.0D * g * e);
                     p = (int)(255.0D * g * f);
                     ++l;
                     if (o > 3) {
                        int q = false;
                        int r = -m * 9;
                        fill(matrix4f, -2, r - 9, 0 + k + 4, r, p << 24);
                        String string = chatHudLine.getText().asFormattedString();
                        RenderSystem.enableBlend();
                        this.client.textRenderer.drawWithShadow(string, 0.0F, (float)(r - 8), 16777215 + (o << 24));
                        RenderSystem.disableAlphaTest();
                        RenderSystem.disableBlend();
                     }
                  }
               }
            }

            if (bl) {
               this.client.textRenderer.getClass();
               int s = 9;
               RenderSystem.translatef(-3.0F, 0.0F, 0.0F);
               int t = j * s + j;
               n = l * s + l;
               int v = this.scrolledLines * n / j;
               int w = n * n / t;
               if (t != n) {
                  o = v > 0 ? 170 : 96;
                  p = this.hasUnreadNewMessages ? 13382451 : 3355562;
                  fill(0, -v, 2, -v - w, p + (o << 24));
                  fill(2, -v, 1, -v - w, 13421772 + (o << 24));
               }
            }

            RenderSystem.popMatrix();
         }
      }
   }

   private boolean method_23677() {
      return this.client.options.chatVisibility != ChatVisibility.HIDDEN;
   }

   private static double getMessageOpacityMultiplier(int age) {
      double d = (double)age / 200.0D;
      d = 1.0D - d;
      d *= 10.0D;
      d = MathHelper.clamp(d, 0.0D, 1.0D);
      d *= d;
      return d;
   }

   public void clear(boolean clearHistory) {
      this.visibleMessages.clear();
      this.messages.clear();
      if (clearHistory) {
         this.messageHistory.clear();
      }

   }

   public void addMessage(Text message) {
      this.addMessage(message, 0);
   }

   public void addMessage(Text message, int messageId) {
      this.addMessage(message, messageId, this.client.inGameHud.getTicks(), false);
      LOGGER.info("[CHAT] {}", message.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
   }

   private void addMessage(Text message, int messageId, int timestamp, boolean bl) {
      if (messageId != 0) {
         this.removeMessage(messageId);
      }

      int i = MathHelper.floor((double)this.getWidth() / this.getChatScale());
      List<Text> list = Texts.wrapLines(message, i, this.client.textRenderer, false, false);
      boolean bl2 = this.isChatFocused();

      Text text;
      for(Iterator var8 = list.iterator(); var8.hasNext(); this.visibleMessages.add(0, new ChatHudLine(timestamp, text, messageId))) {
         text = (Text)var8.next();
         if (bl2 && this.scrolledLines > 0) {
            this.hasUnreadNewMessages = true;
            this.scroll(1.0D);
         }
      }

      while(this.visibleMessages.size() > 100) {
         this.visibleMessages.remove(this.visibleMessages.size() - 1);
      }

      if (!bl) {
         this.messages.add(0, new ChatHudLine(timestamp, message, messageId));

         while(this.messages.size() > 100) {
            this.messages.remove(this.messages.size() - 1);
         }
      }

   }

   public void reset() {
      this.visibleMessages.clear();
      this.resetScroll();

      for(int i = this.messages.size() - 1; i >= 0; --i) {
         ChatHudLine chatHudLine = (ChatHudLine)this.messages.get(i);
         this.addMessage(chatHudLine.getText(), chatHudLine.getId(), chatHudLine.getCreationTick(), true);
      }

   }

   public List<String> getMessageHistory() {
      return this.messageHistory;
   }

   public void addToMessageHistory(String message) {
      if (this.messageHistory.isEmpty() || !((String)this.messageHistory.get(this.messageHistory.size() - 1)).equals(message)) {
         this.messageHistory.add(message);
      }

   }

   public void resetScroll() {
      this.scrolledLines = 0;
      this.hasUnreadNewMessages = false;
   }

   public void scroll(double amount) {
      this.scrolledLines = (int)((double)this.scrolledLines + amount);
      int i = this.visibleMessages.size();
      if (this.scrolledLines > i - this.getVisibleLineCount()) {
         this.scrolledLines = i - this.getVisibleLineCount();
      }

      if (this.scrolledLines <= 0) {
         this.scrolledLines = 0;
         this.hasUnreadNewMessages = false;
      }

   }

   @Nullable
   public Text getText(double x, double y) {
      if (this.isChatFocused() && !this.client.options.hudHidden && this.method_23677()) {
         double d = this.getChatScale();
         double e = x - 2.0D;
         double f = (double)this.client.getWindow().getScaledHeight() - y - 40.0D;
         e = (double)MathHelper.floor(e / d);
         f = (double)MathHelper.floor(f / d);
         if (e >= 0.0D && f >= 0.0D) {
            int i = Math.min(this.getVisibleLineCount(), this.visibleMessages.size());
            if (e <= (double)MathHelper.floor((double)this.getWidth() / this.getChatScale())) {
               this.client.textRenderer.getClass();
               if (f < (double)(9 * i + i)) {
                  this.client.textRenderer.getClass();
                  int j = (int)(f / 9.0D + (double)this.scrolledLines);
                  if (j >= 0 && j < this.visibleMessages.size()) {
                     ChatHudLine chatHudLine = (ChatHudLine)this.visibleMessages.get(j);
                     int k = 0;
                     Iterator var15 = chatHudLine.getText().iterator();

                     while(var15.hasNext()) {
                        Text text = (Text)var15.next();
                        if (text instanceof LiteralText) {
                           k += this.client.textRenderer.getStringWidth(Texts.getRenderChatMessage(((LiteralText)text).getRawString(), false));
                           if ((double)k > e) {
                              return text;
                           }
                        }
                     }
                  }

                  return null;
               }
            }

            return null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public boolean isChatFocused() {
      return this.client.currentScreen instanceof ChatScreen;
   }

   public void removeMessage(int messageId) {
      Iterator iterator = this.visibleMessages.iterator();

      ChatHudLine chatHudLine2;
      while(iterator.hasNext()) {
         chatHudLine2 = (ChatHudLine)iterator.next();
         if (chatHudLine2.getId() == messageId) {
            iterator.remove();
         }
      }

      iterator = this.messages.iterator();

      while(iterator.hasNext()) {
         chatHudLine2 = (ChatHudLine)iterator.next();
         if (chatHudLine2.getId() == messageId) {
            iterator.remove();
            break;
         }
      }

   }

   public int getWidth() {
      return getWidth(this.client.options.chatWidth);
   }

   public int getHeight() {
      return getHeight(this.isChatFocused() ? this.client.options.chatHeightFocused : this.client.options.chatHeightUnfocused);
   }

   public double getChatScale() {
      return this.client.options.chatScale;
   }

   public static int getWidth(double widthOption) {
      int i = true;
      int j = true;
      return MathHelper.floor(widthOption * 280.0D + 40.0D);
   }

   public static int getHeight(double heightOption) {
      int i = true;
      int j = true;
      return MathHelper.floor(heightOption * 160.0D + 20.0D);
   }

   public int getVisibleLineCount() {
      return this.getHeight() / 9;
   }
}
