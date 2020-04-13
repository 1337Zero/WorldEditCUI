package net.minecraft.client.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public class TextureUtil {
   private static final Logger LOGGER = LogManager.getLogger();

   public static int generateTextureId() {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      return GlStateManager.getTexLevelParameter();
   }

   public static void releaseTextureId(int i) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GlStateManager.deleteTexture(i);
   }

   public static void prepareImage(int i, int j, int k) {
      prepareImage(NativeImage.GLFormat.RGBA, i, 0, j, k);
   }

   public static void prepareImage(NativeImage.GLFormat gLFormat, int i, int j, int k) {
      prepareImage(gLFormat, i, 0, j, k);
   }

   public static void prepareImage(int i, int j, int k, int l) {
      prepareImage(NativeImage.GLFormat.RGBA, i, j, k, l);
   }

   public static void prepareImage(NativeImage.GLFormat gLFormat, int i, int j, int k, int l) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      bind(i);
      if (j >= 0) {
         GlStateManager.texParameter(3553, 33085, j);
         GlStateManager.texParameter(3553, 33082, 0);
         GlStateManager.texParameter(3553, 33083, j);
         GlStateManager.texParameter(3553, 34049, 0.0F);
      }

      for(int m = 0; m <= j; ++m) {
         GlStateManager.texImage2D(3553, m, gLFormat.getGlConstant(), k >> m, l >> m, 0, 6408, 5121, (IntBuffer)null);
      }

   }

   private static void bind(int i) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      GlStateManager.bindTexture(i);
   }

   public static ByteBuffer readResource(InputStream inputStream) throws IOException {
      ByteBuffer byteBuffer2;
      if (inputStream instanceof FileInputStream) {
         FileInputStream fileInputStream = (FileInputStream)inputStream;
         FileChannel fileChannel = fileInputStream.getChannel();
         byteBuffer2 = MemoryUtil.memAlloc((int)fileChannel.size() + 1);

         while(true) {
            if (fileChannel.read(byteBuffer2) != -1) {
               continue;
            }
         }
      } else {
         byteBuffer2 = MemoryUtil.memAlloc(8192);
         ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);

         while(readableByteChannel.read(byteBuffer2) != -1) {
            if (byteBuffer2.remaining() == 0) {
               byteBuffer2 = MemoryUtil.memRealloc(byteBuffer2, byteBuffer2.capacity() * 2);
            }
         }
      }

      return byteBuffer2;
   }

   public static String readResourceAsString(InputStream inputStream) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      ByteBuffer byteBuffer = null;

      try {
         byteBuffer = readResource(inputStream);
         int i = byteBuffer.position();
         byteBuffer.rewind();
         String var3 = MemoryUtil.memASCII(byteBuffer, i);
         return var3;
      } catch (IOException var7) {
      } finally {
         if (byteBuffer != null) {
            MemoryUtil.memFree(byteBuffer);
         }

      }

      return null;
   }

   public static void initTexture(IntBuffer intBuffer, int i, int j) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      GL11.glPixelStorei(3312, 0);
      GL11.glPixelStorei(3313, 0);
      GL11.glPixelStorei(3314, 0);
      GL11.glPixelStorei(3315, 0);
      GL11.glPixelStorei(3316, 0);
      GL11.glPixelStorei(3317, 4);
      GL11.glTexImage2D(3553, 0, 6408, i, j, 0, 32993, 33639, intBuffer);
      GL11.glTexParameteri(3553, 10242, 10497);
      GL11.glTexParameteri(3553, 10243, 10497);
      GL11.glTexParameteri(3553, 10240, 9728);
      GL11.glTexParameteri(3553, 10241, 9729);
   }
}
