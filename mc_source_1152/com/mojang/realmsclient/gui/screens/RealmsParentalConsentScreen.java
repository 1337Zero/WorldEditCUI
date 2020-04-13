package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsParentalConsentScreen extends RealmsScreen {
   private final RealmsScreen nextScreen;

   public RealmsParentalConsentScreen(RealmsScreen nextScreen) {
      this.nextScreen = nextScreen;
   }

   public void init() {
      Realms.narrateNow(getLocalizedString("mco.account.privacyinfo"));
      String string = getLocalizedString("mco.account.update");
      String string2 = getLocalizedString("gui.back");
      int i = Math.max(this.fontWidth(string), this.fontWidth(string2)) + 30;
      String string3 = getLocalizedString("mco.account.privacy.info");
      int j = (int)((double)this.fontWidth(string3) * 1.2D);
      this.buttonsAdd(new RealmsButton(1, this.width() / 2 - j / 2, RealmsConstants.row(11), j, 20, string3) {
         public void onPress() {
            RealmsUtil.browseTo("https://minecraft.net/privacy/gdpr/");
         }
      });
      this.buttonsAdd(new RealmsButton(1, this.width() / 2 - (i + 5), RealmsConstants.row(13), i, 20, string) {
         public void onPress() {
            RealmsUtil.browseTo("https://minecraft.net/update-account");
         }
      });
      this.buttonsAdd(new RealmsButton(0, this.width() / 2 + 5, RealmsConstants.row(13), i, 20, string2) {
         public void onPress() {
            Realms.setScreen(RealmsParentalConsentScreen.this.nextScreen);
         }
      });
   }

   public void tick() {
      super.tick();
   }

   public boolean mouseClicked(double x, double y, int buttonNum) {
      return super.mouseClicked(x, y, buttonNum);
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      List<String> list = this.getLocalizedStringWithLineWidth("mco.account.privacyinfo", (int)Math.round((double)this.width() * 0.9D));
      int i = 15;

      for(Iterator var6 = list.iterator(); var6.hasNext(); i += 15) {
         String string = (String)var6.next();
         this.drawCenteredString(string, this.width() / 2, i, 16777215);
      }

      super.render(xm, ym, a);
   }
}
