package net.minecraft.realms;

import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class DisconnectedRealmsScreen extends RealmsScreen {
   private final String title;
   private final Text reason;
   private List<String> lines;
   private final RealmsScreen parent;
   private int textHeight;

   public DisconnectedRealmsScreen(RealmsScreen realmsScreen, String string, Text text) {
      this.parent = realmsScreen;
      this.title = getLocalizedString(string);
      this.reason = text;
   }

   public void init() {
      Realms.setConnectedToRealms(false);
      Realms.clearResourcePack();
      Realms.narrateNow(this.title + ": " + this.reason.getString());
      this.lines = this.fontSplit(this.reason.asFormattedString(), this.width() - 50);
      this.textHeight = this.lines.size() * this.fontLineHeight();
      this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 100, this.height() / 2 + this.textHeight / 2 + this.fontLineHeight(), getLocalizedString("gui.back")) {
         public void onPress() {
            Realms.setScreen(DisconnectedRealmsScreen.this.parent);
         }
      });
   }

   public boolean keyPressed(int i, int j, int k) {
      if (i == 256) {
         Realms.setScreen(this.parent);
         return true;
      } else {
         return super.keyPressed(i, j, k);
      }
   }

   public void render(int i, int j, float f) {
      this.renderBackground();
      this.drawCenteredString(this.title, this.width() / 2, this.height() / 2 - this.textHeight / 2 - this.fontLineHeight() * 2, 11184810);
      int k = this.height() / 2 - this.textHeight / 2;
      if (this.lines != null) {
         for(Iterator var5 = this.lines.iterator(); var5.hasNext(); k += this.fontLineHeight()) {
            String string = (String)var5.next();
            this.drawCenteredString(string, this.width() / 2, k, 16777215);
         }
      }

      super.render(i, j, f);
   }
}
