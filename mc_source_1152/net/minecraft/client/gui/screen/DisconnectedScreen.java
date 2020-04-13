package net.minecraft.client.gui.screen;

import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class DisconnectedScreen extends Screen {
   private final Text reason;
   private List<String> reasonFormatted;
   private final Screen parent;
   private int reasonHeight;

   public DisconnectedScreen(Screen parent, String title, Text reason) {
      super(new TranslatableText(title, new Object[0]));
      this.parent = parent;
      this.reason = reason;
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   protected void init() {
      this.reasonFormatted = this.font.wrapStringToWidthAsList(this.reason.asFormattedString(), this.width - 50);
      int var10001 = this.reasonFormatted.size();
      this.font.getClass();
      this.reasonHeight = var10001 * 9;
      int var10003 = this.width / 2 - 100;
      int var10004 = this.height / 2 + this.reasonHeight / 2;
      this.font.getClass();
      this.addButton(new ButtonWidget(var10003, Math.min(var10004 + 9, this.height - 30), 200, 20, I18n.translate("gui.toMenu"), (buttonWidget) -> {
         this.minecraft.openScreen(this.parent);
      }));
   }

   public void render(int mouseX, int mouseY, float delta) {
      this.renderBackground();
      TextRenderer var10001 = this.font;
      String var10002 = this.title.asFormattedString();
      int var10003 = this.width / 2;
      int var10004 = this.height / 2 - this.reasonHeight / 2;
      this.font.getClass();
      this.drawCenteredString(var10001, var10002, var10003, var10004 - 9 * 2, 11184810);
      int i = this.height / 2 - this.reasonHeight / 2;
      if (this.reasonFormatted != null) {
         for(Iterator var5 = this.reasonFormatted.iterator(); var5.hasNext(); i += 9) {
            String string = (String)var5.next();
            this.drawCenteredString(this.font, string, this.width / 2, i, 16777215);
            this.font.getClass();
         }
      }

      super.render(mouseX, mouseY, delta);
   }
}
