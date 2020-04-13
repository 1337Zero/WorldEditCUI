package net.minecraft.client.gui.screen;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class NoticeScreen extends Screen {
   private final Runnable actionHandler;
   protected final Text notice;
   private final List<String> noticeLines;
   protected final String buttonString;
   private int field_2347;

   public NoticeScreen(Runnable actionHandler, Text title, Text notice) {
      this(actionHandler, title, notice, "gui.back");
   }

   public NoticeScreen(Runnable actionHandler, Text title, Text notice, String buttonString) {
      super(title);
      this.noticeLines = Lists.newArrayList();
      this.actionHandler = actionHandler;
      this.notice = notice;
      this.buttonString = I18n.translate(buttonString);
   }

   protected void init() {
      super.init();
      this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 6 + 168, 200, 20, this.buttonString, (buttonWidget) -> {
         this.actionHandler.run();
      }));
      this.noticeLines.clear();
      this.noticeLines.addAll(this.font.wrapStringToWidthAsList(this.notice.asFormattedString(), this.width - 50));
   }

   public void render(int mouseX, int mouseY, float delta) {
      this.renderBackground();
      this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, 70, 16777215);
      int i = 90;

      for(Iterator var5 = this.noticeLines.iterator(); var5.hasNext(); i += 9) {
         String string = (String)var5.next();
         this.drawCenteredString(this.font, string, this.width / 2, i, 16777215);
         this.font.getClass();
      }

      super.render(mouseX, mouseY, delta);
   }

   public void tick() {
      super.tick();
      AbstractButtonWidget abstractButtonWidget;
      if (--this.field_2347 == 0) {
         for(Iterator var1 = this.buttons.iterator(); var1.hasNext(); abstractButtonWidget.active = true) {
            abstractButtonWidget = (AbstractButtonWidget)var1.next();
         }
      }

   }
}
