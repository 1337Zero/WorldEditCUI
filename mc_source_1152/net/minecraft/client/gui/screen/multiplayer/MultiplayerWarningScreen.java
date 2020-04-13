package net.minecraft.client.gui.screen.multiplayer;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class MultiplayerWarningScreen extends Screen {
   private final Screen parent;
   private final Text header;
   private final Text message;
   private final Text checkMessage;
   private final Text proceedText;
   private final Text backText;
   private CheckboxWidget checkbox;
   private final List<String> lines;

   public MultiplayerWarningScreen(Screen parent) {
      super(NarratorManager.EMPTY);
      this.header = (new TranslatableText("multiplayerWarning.header", new Object[0])).formatted(Formatting.BOLD);
      this.message = new TranslatableText("multiplayerWarning.message", new Object[0]);
      this.checkMessage = new TranslatableText("multiplayerWarning.check", new Object[0]);
      this.proceedText = new TranslatableText("gui.proceed", new Object[0]);
      this.backText = new TranslatableText("gui.back", new Object[0]);
      this.lines = Lists.newArrayList();
      this.parent = parent;
   }

   protected void init() {
      super.init();
      this.lines.clear();
      this.lines.addAll(this.font.wrapStringToWidthAsList(this.message.asFormattedString(), this.width - 50));
      int var10000 = this.lines.size() + 1;
      this.font.getClass();
      int i = var10000 * 9;
      this.addButton(new ButtonWidget(this.width / 2 - 155, 100 + i, 150, 20, this.proceedText.asFormattedString(), (buttonWidget) -> {
         if (this.checkbox.isChecked()) {
            this.minecraft.options.skipMultiplayerWarning = true;
            this.minecraft.options.write();
         }

         this.minecraft.openScreen(new MultiplayerScreen(this.parent));
      }));
      this.addButton(new ButtonWidget(this.width / 2 - 155 + 160, 100 + i, 150, 20, this.backText.asFormattedString(), (buttonWidget) -> {
         this.minecraft.openScreen(this.parent);
      }));
      this.checkbox = new CheckboxWidget(this.width / 2 - 155 + 80, 76 + i, 150, 20, this.checkMessage.asFormattedString(), false);
      this.addButton(this.checkbox);
   }

   public String getNarrationMessage() {
      return this.header.getString() + "\n" + this.message.getString();
   }

   public void render(int i, int j, float f) {
      this.renderDirtBackground(0);
      this.drawCenteredString(this.font, this.header.asFormattedString(), this.width / 2, 30, 16777215);
      int k = 70;

      for(Iterator var5 = this.lines.iterator(); var5.hasNext(); k += 9) {
         String string = (String)var5.next();
         this.drawCenteredString(this.font, string, this.width / 2, k, 16777215);
         this.font.getClass();
      }

      super.render(i, j, f);
   }
}
