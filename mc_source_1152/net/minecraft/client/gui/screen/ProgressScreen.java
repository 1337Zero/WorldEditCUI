package net.minecraft.client.gui.screen;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ProgressListener;

@Environment(EnvType.CLIENT)
public class ProgressScreen extends Screen implements ProgressListener {
   private String title = "";
   private String task = "";
   private int progress;
   private boolean done;

   public ProgressScreen() {
      super(NarratorManager.EMPTY);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public void method_15412(Text text) {
      this.method_15413(text);
   }

   public void method_15413(Text text) {
      this.title = text.asFormattedString();
      this.method_15414(new TranslatableText("progress.working", new Object[0]));
   }

   public void method_15414(Text text) {
      this.task = text.asFormattedString();
      this.progressStagePercentage(0);
   }

   public void progressStagePercentage(int i) {
      this.progress = i;
   }

   public void setDone() {
      this.done = true;
   }

   public void render(int mouseX, int mouseY, float delta) {
      if (this.done) {
         if (!this.minecraft.isConnectedToRealms()) {
            this.minecraft.openScreen((Screen)null);
         }

      } else {
         this.renderBackground();
         this.drawCenteredString(this.font, this.title, this.width / 2, 70, 16777215);
         if (!Objects.equals(this.task, "") && this.progress != 0) {
            this.drawCenteredString(this.font, this.task + " " + this.progress + "%", this.width / 2, 90, 16777215);
         }

         super.render(mouseX, mouseY, delta);
      }
   }
}
