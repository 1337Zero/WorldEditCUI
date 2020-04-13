package net.minecraft.client.options;

import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Monitor;
import net.minecraft.client.util.VideoMode;
import net.minecraft.client.util.Window;

@Environment(EnvType.CLIENT)
public class FullScreenOption extends DoubleOption {
   public FullScreenOption(Window window) {
      this(window, window.getMonitor());
   }

   private FullScreenOption(Window window, @Nullable Monitor monitor) {
      super("options.fullscreen.resolution", -1.0D, monitor != null ? (double)(monitor.getVideoModeCount() - 1) : -1.0D, 1.0F, (gameOptions) -> {
         if (monitor == null) {
            return -1.0D;
         } else {
            Optional<VideoMode> optional = window.getVideoMode();
            return (Double)optional.map((videoMode) -> {
               return (double)monitor.findClosestVideoModeIndex(videoMode);
            }).orElse(-1.0D);
         }
      }, (gameOptions, var3) -> {
         if (monitor != null) {
            if (var3 == -1.0D) {
               window.setVideoMode(Optional.empty());
            } else {
               window.setVideoMode(Optional.of(monitor.getVideoMode(var3.intValue())));
            }

         }
      }, (gameOptions, doubleOption) -> {
         if (monitor == null) {
            return I18n.translate("options.fullscreen.unavailable");
         } else {
            double d = doubleOption.get(gameOptions);
            String string = doubleOption.getDisplayPrefix();
            return d == -1.0D ? string + I18n.translate("options.fullscreen.current") : monitor.getVideoMode((int)d).toString();
         }
      });
   }
}
