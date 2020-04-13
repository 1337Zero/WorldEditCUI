package net.minecraft.client.gui.screen;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ConfirmScreen extends Screen {
   private final Text message;
   private final List<String> messageSplit;
   protected String yesTranslated;
   protected String noTranslated;
   private int buttonEnableTimer;
   protected final BooleanConsumer callback;

   public ConfirmScreen(BooleanConsumer callback, Text title, Text message) {
      this(callback, title, message, I18n.translate("gui.yes"), I18n.translate("gui.no"));
   }

   public ConfirmScreen(BooleanConsumer callback, Text title, Text message, String yesTranslated, String noTranslated) {
      super(title);
      this.messageSplit = Lists.newArrayList();
      this.callback = callback;
      this.message = message;
      this.yesTranslated = yesTranslated;
      this.noTranslated = noTranslated;
   }

   public String getNarrationMessage() {
      return super.getNarrationMessage() + ". " + this.message.getString();
   }

   protected void init() {
      super.init();
      this.addButton(new ButtonWidget(this.width / 2 - 155, this.height / 6 + 96, 150, 20, this.yesTranslated, (buttonWidget) -> {
         this.callback.accept(true);
      }));
      this.addButton(new ButtonWidget(this.width / 2 - 155 + 160, this.height / 6 + 96, 150, 20, this.noTranslated, (buttonWidget) -> {
         this.callback.accept(false);
      }));
      this.messageSplit.clear();
      this.messageSplit.addAll(this.font.wrapStringToWidthAsList(this.message.asFormattedString(), this.width - 50));
   }

   public void render(int mouseX, int mouseY, float delta) {
      this.renderBackground();
      this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, 70, 16777215);
      int i = 90;

      for(Iterator var5 = this.messageSplit.iterator(); var5.hasNext(); i += 9) {
         String string = (String)var5.next();
         this.drawCenteredString(this.font, string, this.width / 2, i, 16777215);
         this.font.getClass();
      }

      super.render(mouseX, mouseY, delta);
   }

   public void disableButtons(int i) {
      this.buttonEnableTimer = i;

      AbstractButtonWidget abstractButtonWidget;
      for(Iterator var2 = this.buttons.iterator(); var2.hasNext(); abstractButtonWidget.active = false) {
         abstractButtonWidget = (AbstractButtonWidget)var2.next();
      }

   }

   public void tick() {
      super.tick();
      AbstractButtonWidget abstractButtonWidget;
      if (--this.buttonEnableTimer == 0) {
         for(Iterator var1 = this.buttons.iterator(); var1.hasNext(); abstractButtonWidget.active = true) {
            abstractButtonWidget = (AbstractButtonWidget)var1.next();
         }
      }

   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.callback.accept(false);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }
}
