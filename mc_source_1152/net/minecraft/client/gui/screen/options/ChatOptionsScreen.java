package net.minecraft.client.gui.screen.options;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.Option;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class ChatOptionsScreen extends GameOptionsScreen {
   private static final Option[] OPTIONS;
   private AbstractButtonWidget narratorOptionButton;

   public ChatOptionsScreen(Screen parent, GameOptions options) {
      super(parent, options, new TranslatableText("options.chat.title", new Object[0]));
   }

   protected void init() {
      int i = 0;
      Option[] var2 = OPTIONS;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Option option = var2[var4];
         int j = this.width / 2 - 155 + i % 2 * 160;
         int k = this.height / 6 + 24 * (i >> 1);
         AbstractButtonWidget abstractButtonWidget = this.addButton(option.createButton(this.minecraft.options, j, k, 150));
         if (option == Option.NARRATOR) {
            this.narratorOptionButton = abstractButtonWidget;
            abstractButtonWidget.active = NarratorManager.INSTANCE.isActive();
         }

         ++i;
      }

      this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 6 + 24 * (i + 1) / 2, 200, 20, I18n.translate("gui.done"), (buttonWidget) -> {
         this.minecraft.openScreen(this.parent);
      }));
   }

   public void render(int mouseX, int mouseY, float delta) {
      this.renderBackground();
      this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, 20, 16777215);
      super.render(mouseX, mouseY, delta);
   }

   public void setNarratorMessage() {
      this.narratorOptionButton.setMessage(Option.NARRATOR.getMessage(this.gameOptions));
   }

   static {
      OPTIONS = new Option[]{Option.VISIBILITY, Option.CHAT_COLOR, Option.CHAT_LINKS, Option.CHAT_LINKS_PROMPT, Option.CHAT_OPACITY, Option.TEXT_BACKGROUND_OPACITY, Option.CHAT_SCALE, Option.CHAT_WIDTH, Option.CHAT_HEIGHT_FOCUSED, Option.SATURATION, Option.REDUCED_DEBUG_INFO, Option.AUTO_SUGGESTIONS, Option.NARRATOR};
   }
}
