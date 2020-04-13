package net.minecraft.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceImpl;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ScreenshotUtils {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

   public static void saveScreenshot(File gameDirectory, int framebufferWidth, int framebufferHeight, Framebuffer framebuffer, Consumer<Text> messageReceiver) {
      saveScreenshot(gameDirectory, (String)null, framebufferWidth, framebufferHeight, framebuffer, messageReceiver);
   }

   public static void saveScreenshot(File gameDirectory, @Nullable String fileName, int framebufferWidth, int framebufferHeight, Framebuffer framebuffer, Consumer<Text> messageReceiver) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            saveScreenshotInner(gameDirectory, fileName, framebufferWidth, framebufferHeight, framebuffer, messageReceiver);
         });
      } else {
         saveScreenshotInner(gameDirectory, fileName, framebufferWidth, framebufferHeight, framebuffer, messageReceiver);
      }

   }

   private static void saveScreenshotInner(File gameDirectory, @Nullable String fileName, int framebufferWidth, int framebufferHeight, Framebuffer framebuffer, Consumer<Text> messageReceiver) {
      NativeImage nativeImage = takeScreenshot(framebufferWidth, framebufferHeight, framebuffer);
      File file = new File(gameDirectory, "screenshots");
      file.mkdir();
      File file3;
      if (fileName == null) {
         file3 = getScreenshotFilename(file);
      } else {
         file3 = new File(file, fileName);
      }

      ResourceImpl.RESOURCE_IO_EXECUTOR.execute(() -> {
         try {
            nativeImage.writeFile(file3);
            Text text = (new LiteralText(file3.getName())).formatted(Formatting.UNDERLINE).styled((style) -> {
               style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file3.getAbsolutePath()));
            });
            messageReceiver.accept(new TranslatableText("screenshot.success", new Object[]{text}));
         } catch (Exception var7) {
            LOGGER.warn("Couldn't save screenshot", var7);
            messageReceiver.accept(new TranslatableText("screenshot.failure", new Object[]{var7.getMessage()}));
         } finally {
            nativeImage.close();
         }

      });
   }

   public static NativeImage takeScreenshot(int width, int height, Framebuffer framebuffer) {
      width = framebuffer.textureWidth;
      height = framebuffer.textureHeight;
      NativeImage nativeImage = new NativeImage(width, height, false);
      RenderSystem.bindTexture(framebuffer.colorAttachment);
      nativeImage.loadFromTextureImage(0, true);
      nativeImage.mirrorVertically();
      return nativeImage;
   }

   private static File getScreenshotFilename(File directory) {
      String string = DATE_FORMAT.format(new Date());
      int i = 1;

      while(true) {
         File file = new File(directory, string + (i == 1 ? "" : "_" + i) + ".png");
         if (!file.exists()) {
            return file;
         }

         ++i;
      }
   }
}
