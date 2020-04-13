package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Texts;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class DeathScreen extends Screen {
   private int ticksSinceDeath;
   private final Text message;
   private final boolean isHardcore;

   public DeathScreen(@Nullable Text message, boolean isHardcore) {
      super(new TranslatableText(isHardcore ? "deathScreen.title.hardcore" : "deathScreen.title", new Object[0]));
      this.message = message;
      this.isHardcore = isHardcore;
   }

   protected void init() {
      this.ticksSinceDeath = 0;
      this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 72, 200, 20, this.isHardcore ? I18n.translate("deathScreen.spectate") : I18n.translate("deathScreen.respawn"), (buttonWidgetx) -> {
         this.minecraft.player.requestRespawn();
         this.minecraft.openScreen((Screen)null);
      }));
      ButtonWidget buttonWidget = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 96, 200, 20, I18n.translate("deathScreen.titleScreen"), (buttonWidgetx) -> {
         if (this.isHardcore) {
            this.quitLevel();
         } else {
            ConfirmScreen confirmScreen = new ConfirmScreen(this::onConfirmQuit, new TranslatableText("deathScreen.quit.confirm", new Object[0]), new LiteralText(""), I18n.translate("deathScreen.titleScreen"), I18n.translate("deathScreen.respawn"));
            this.minecraft.openScreen(confirmScreen);
            confirmScreen.disableButtons(20);
         }
      }));
      if (!this.isHardcore && this.minecraft.getSession() == null) {
         buttonWidget.active = false;
      }

      AbstractButtonWidget abstractButtonWidget;
      for(Iterator var2 = this.buttons.iterator(); var2.hasNext(); abstractButtonWidget.active = false) {
         abstractButtonWidget = (AbstractButtonWidget)var2.next();
      }

   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   private void onConfirmQuit(boolean quit) {
      if (quit) {
         this.quitLevel();
      } else {
         this.minecraft.player.requestRespawn();
         this.minecraft.openScreen((Screen)null);
      }

   }

   private void quitLevel() {
      if (this.minecraft.world != null) {
         this.minecraft.world.disconnect();
      }

      this.minecraft.disconnect(new SaveLevelScreen(new TranslatableText("menu.savingLevel", new Object[0])));
      this.minecraft.openScreen(new TitleScreen());
   }

   public void render(int mouseX, int mouseY, float delta) {
      this.fillGradient(0, 0, this.width, this.height, 1615855616, -1602211792);
      RenderSystem.pushMatrix();
      RenderSystem.scalef(2.0F, 2.0F, 2.0F);
      this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2 / 2, 30, 16777215);
      RenderSystem.popMatrix();
      if (this.message != null) {
         this.drawCenteredString(this.font, this.message.asFormattedString(), this.width / 2, 85, 16777215);
      }

      this.drawCenteredString(this.font, I18n.translate("deathScreen.score") + ": " + Formatting.YELLOW + this.minecraft.player.getScore(), this.width / 2, 100, 16777215);
      if (this.message != null && mouseY > 85) {
         this.font.getClass();
         if (mouseY < 85 + 9) {
            Text text = this.getTextComponentUnderMouse(mouseX);
            if (text != null && text.getStyle().getHoverEvent() != null) {
               this.renderComponentHoverEffect(text, mouseX, mouseY);
            }
         }
      }

      super.render(mouseX, mouseY, delta);
   }

   @Nullable
   public Text getTextComponentUnderMouse(int mouseX) {
      if (this.message == null) {
         return null;
      } else {
         int i = this.minecraft.textRenderer.getStringWidth(this.message.asFormattedString());
         int j = this.width / 2 - i / 2;
         int k = this.width / 2 + i / 2;
         int l = j;
         if (mouseX >= j && mouseX <= k) {
            Iterator var6 = this.message.iterator();

            Text text;
            do {
               if (!var6.hasNext()) {
                  return null;
               }

               text = (Text)var6.next();
               l += this.minecraft.textRenderer.getStringWidth(Texts.getRenderChatMessage(text.asString(), false));
            } while(l <= mouseX);

            return text;
         } else {
            return null;
         }
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.message != null && mouseY > 85.0D) {
         this.font.getClass();
         if (mouseY < (double)(85 + 9)) {
            Text text = this.getTextComponentUnderMouse((int)mouseX);
            if (text != null && text.getStyle().getClickEvent() != null && text.getStyle().getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
               this.handleComponentClicked(text);
               return false;
            }
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void tick() {
      super.tick();
      ++this.ticksSinceDeath;
      AbstractButtonWidget abstractButtonWidget;
      if (this.ticksSinceDeath == 20) {
         for(Iterator var1 = this.buttons.iterator(); var1.hasNext(); abstractButtonWidget.active = true) {
            abstractButtonWidget = (AbstractButtonWidget)var1.next();
         }
      }

   }
}
