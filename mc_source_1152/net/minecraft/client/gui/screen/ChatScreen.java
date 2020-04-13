package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class ChatScreen extends Screen {
   private String field_2389 = "";
   private int messageHistorySize = -1;
   protected TextFieldWidget chatField;
   private String originalChatText = "";
   private CommandSuggestor commandSuggestor;

   public ChatScreen(String originalChatText) {
      super(NarratorManager.EMPTY);
      this.originalChatText = originalChatText;
   }

   protected void init() {
      this.minecraft.keyboard.enableRepeatEvents(true);
      this.messageHistorySize = this.minecraft.inGameHud.getChatHud().getMessageHistory().size();
      this.chatField = new TextFieldWidget(this.font, 4, this.height - 12, this.width - 4, 12, I18n.translate("chat.editBox")) {
         protected String getNarrationMessage() {
            return super.getNarrationMessage() + ChatScreen.this.commandSuggestor.method_23958();
         }
      };
      this.chatField.setMaxLength(256);
      this.chatField.setHasBorder(false);
      this.chatField.setText(this.originalChatText);
      this.chatField.setChangedListener(this::onChatFieldUpdate);
      this.children.add(this.chatField);
      this.commandSuggestor = new CommandSuggestor(this.minecraft, this, this.chatField, this.font, false, false, 1, 10, true, -805306368);
      this.commandSuggestor.refresh();
      this.setInitialFocus(this.chatField);
   }

   public void resize(MinecraftClient client, int width, int height) {
      String string = this.chatField.getText();
      this.init(client, width, height);
      this.setText(string);
      this.commandSuggestor.refresh();
   }

   public void removed() {
      this.minecraft.keyboard.enableRepeatEvents(false);
      this.minecraft.inGameHud.getChatHud().resetScroll();
   }

   public void tick() {
      this.chatField.tick();
   }

   private void onChatFieldUpdate(String chatText) {
      String string = this.chatField.getText();
      this.commandSuggestor.setWindowActive(!string.equals(this.originalChatText));
      this.commandSuggestor.refresh();
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.commandSuggestor.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else if (super.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else if (keyCode == 256) {
         this.minecraft.openScreen((Screen)null);
         return true;
      } else if (keyCode != 257 && keyCode != 335) {
         if (keyCode == 265) {
            this.setChatFromHistory(-1);
            return true;
         } else if (keyCode == 264) {
            this.setChatFromHistory(1);
            return true;
         } else if (keyCode == 266) {
            this.minecraft.inGameHud.getChatHud().scroll((double)(this.minecraft.inGameHud.getChatHud().getVisibleLineCount() - 1));
            return true;
         } else if (keyCode == 267) {
            this.minecraft.inGameHud.getChatHud().scroll((double)(-this.minecraft.inGameHud.getChatHud().getVisibleLineCount() + 1));
            return true;
         } else {
            return false;
         }
      } else {
         String string = this.chatField.getText().trim();
         if (!string.isEmpty()) {
            this.sendMessage(string);
         }

         this.minecraft.openScreen((Screen)null);
         return true;
      }
   }

   public boolean mouseScrolled(double d, double e, double amount) {
      if (amount > 1.0D) {
         amount = 1.0D;
      }

      if (amount < -1.0D) {
         amount = -1.0D;
      }

      if (this.commandSuggestor.mouseScrolled(amount)) {
         return true;
      } else {
         if (!hasShiftDown()) {
            amount *= 7.0D;
         }

         this.minecraft.inGameHud.getChatHud().scroll(amount);
         return true;
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.commandSuggestor.mouseClicked((double)((int)mouseX), (double)((int)mouseY), button)) {
         return true;
      } else {
         if (button == 0) {
            Text text = this.minecraft.inGameHud.getChatHud().getText(mouseX, mouseY);
            if (text != null && this.handleComponentClicked(text)) {
               return true;
            }
         }

         return this.chatField.mouseClicked(mouseX, mouseY, button) ? true : super.mouseClicked(mouseX, mouseY, button);
      }
   }

   protected void insertText(String string, boolean bl) {
      if (bl) {
         this.chatField.setText(string);
      } else {
         this.chatField.write(string);
      }

   }

   public void setChatFromHistory(int i) {
      int j = this.messageHistorySize + i;
      int k = this.minecraft.inGameHud.getChatHud().getMessageHistory().size();
      j = MathHelper.clamp(j, 0, k);
      if (j != this.messageHistorySize) {
         if (j == k) {
            this.messageHistorySize = k;
            this.chatField.setText(this.field_2389);
         } else {
            if (this.messageHistorySize == k) {
               this.field_2389 = this.chatField.getText();
            }

            this.chatField.setText((String)this.minecraft.inGameHud.getChatHud().getMessageHistory().get(j));
            this.commandSuggestor.setWindowActive(false);
            this.messageHistorySize = j;
         }
      }
   }

   public void render(int mouseX, int mouseY, float delta) {
      this.setFocused(this.chatField);
      this.chatField.setSelected(true);
      fill(2, this.height - 14, this.width - 2, this.height - 2, this.minecraft.options.getTextBackgroundColor(Integer.MIN_VALUE));
      this.chatField.render(mouseX, mouseY, delta);
      this.commandSuggestor.render(mouseX, mouseY);
      Text text = this.minecraft.inGameHud.getChatHud().getText((double)mouseX, (double)mouseY);
      if (text != null && text.getStyle().getHoverEvent() != null) {
         this.renderComponentHoverEffect(text, mouseX, mouseY);
      }

      super.render(mouseX, mouseY, delta);
   }

   public boolean isPauseScreen() {
      return false;
   }

   private void setText(String text) {
      this.chatField.setText(text);
   }
}
