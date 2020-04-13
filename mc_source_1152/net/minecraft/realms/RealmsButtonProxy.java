package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;

@Environment(EnvType.CLIENT)
public class RealmsButtonProxy extends ButtonWidget implements RealmsAbstractButtonProxy<RealmsButton> {
   private final RealmsButton button;

   public RealmsButtonProxy(RealmsButton button, int x, int y, String text, int width, int height, ButtonWidget.PressAction pressAction) {
      super(x, y, width, height, text, pressAction);
      this.button = button;
   }

   public boolean active() {
      return this.active;
   }

   public void active(boolean enabled) {
      this.active = enabled;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
   }

   public void setMessage(String value) {
      super.setMessage(value);
   }

   public int getWidth() {
      return super.getWidth();
   }

   public int y() {
      return this.y;
   }

   public void onClick(double mouseX, double mouseY) {
      this.button.onPress();
   }

   public void onRelease(double mouseX, double mouseY) {
      this.button.onRelease(mouseX, mouseY);
   }

   public void renderBg(MinecraftClient client, int mouseX, int mouseY) {
      this.button.renderBg(mouseX, mouseY);
   }

   public void renderButton(int mouseX, int mouseY, float delta) {
      this.button.renderButton(mouseX, mouseY, delta);
   }

   public void superRenderButton(int i, int j, float f) {
      super.renderButton(i, j, f);
   }

   public RealmsButton getButton() {
      return this.button;
   }

   public int getYImage(boolean isHovered) {
      return this.button.getYImage(isHovered);
   }

   public int getSuperYImage(boolean bl) {
      return super.getYImage(bl);
   }

   public int getHeight() {
      return this.height;
   }

   public boolean isHovered() {
      return super.isHovered();
   }
}
