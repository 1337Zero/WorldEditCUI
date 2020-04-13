package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.SliderWidget;

@Environment(EnvType.CLIENT)
public class RealmsSliderButtonProxy extends SliderWidget implements RealmsAbstractButtonProxy<RealmsSliderButton> {
   private final RealmsSliderButton button;

   public RealmsSliderButtonProxy(RealmsSliderButton realmsSliderButton, int i, int j, int k, int l, double d) {
      super(i, j, k, l, d);
      this.button = realmsSliderButton;
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
      this.button.onClick(mouseX, mouseY);
   }

   public void onRelease(double mouseX, double mouseY) {
      this.button.onRelease(mouseX, mouseY);
   }

   public void updateMessage() {
      this.button.updateMessage();
   }

   public void applyValue() {
      this.button.applyValue();
   }

   public double getValue() {
      return this.value;
   }

   public void setValue(double d) {
      this.value = d;
   }

   public void renderBg(MinecraftClient client, int mouseX, int mouseY) {
      super.renderBg(client, mouseX, mouseY);
   }

   public RealmsSliderButton getButton() {
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
}
