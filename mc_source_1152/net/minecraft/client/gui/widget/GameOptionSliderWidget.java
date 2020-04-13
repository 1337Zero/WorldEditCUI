package net.minecraft.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.options.DoubleOption;
import net.minecraft.client.options.GameOptions;

@Environment(EnvType.CLIENT)
public class GameOptionSliderWidget extends SliderWidget {
   private final DoubleOption option;

   public GameOptionSliderWidget(GameOptions gameOptions, int x, int y, int width, int height, DoubleOption option) {
      super(gameOptions, x, y, width, height, (double)((float)option.getRatio(option.get(gameOptions))));
      this.option = option;
      this.updateMessage();
   }

   protected void applyValue() {
      this.option.set(this.options, this.option.getValue(this.value));
      this.options.write();
   }

   protected void updateMessage() {
      this.setMessage(this.option.getDisplayString(this.options));
   }
}
