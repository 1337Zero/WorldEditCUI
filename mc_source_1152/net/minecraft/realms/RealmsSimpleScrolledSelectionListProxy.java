package net.minecraft.realms;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class RealmsSimpleScrolledSelectionListProxy extends ListWidget {
   private final RealmsSimpleScrolledSelectionList realmsSimpleScrolledSelectionList;

   public RealmsSimpleScrolledSelectionListProxy(RealmsSimpleScrolledSelectionList realmsSimpleScrolledSelectionList, int i, int j, int k, int l, int m) {
      super(MinecraftClient.getInstance(), i, j, k, l, m);
      this.realmsSimpleScrolledSelectionList = realmsSimpleScrolledSelectionList;
   }

   public int getItemCount() {
      return this.realmsSimpleScrolledSelectionList.getItemCount();
   }

   public boolean selectItem(int index, int button, double mouseX, double mouseY) {
      return this.realmsSimpleScrolledSelectionList.selectItem(index, button, mouseX, mouseY);
   }

   public boolean isSelectedItem(int index) {
      return this.realmsSimpleScrolledSelectionList.isSelectedItem(index);
   }

   public void renderBackground() {
      this.realmsSimpleScrolledSelectionList.renderBackground();
   }

   public void renderItem(int index, int y, int i, int j, int k, int l, float f) {
      this.realmsSimpleScrolledSelectionList.renderItem(index, y, i, j, k, l);
   }

   public int getWidth() {
      return this.width;
   }

   public int getMaxPosition() {
      return this.realmsSimpleScrolledSelectionList.getMaxPosition();
   }

   public int getScrollbarPosition() {
      return this.realmsSimpleScrolledSelectionList.getScrollbarPosition();
   }

   public void render(int mouseX, int mouseY, float delta) {
      if (this.visible) {
         this.renderBackground();
         int i = this.getScrollbarPosition();
         int j = i + 6;
         this.capYPosition();
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder bufferBuilder = tessellator.getBuffer();
         int k = this.left + this.width / 2 - this.getRowWidth() / 2 + 2;
         int l = this.top + 4 - (int)this.scroll;
         if (this.renderHeader) {
            this.renderHeader(k, l, tessellator);
         }

         this.renderList(k, l, mouseX, mouseY, delta);
         RenderSystem.disableDepthTest();
         this.renderHoleBackground(0, this.top, 255, 255);
         this.renderHoleBackground(this.bottom, this.height, 255, 255);
         RenderSystem.enableBlend();
         RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
         RenderSystem.disableAlphaTest();
         RenderSystem.shadeModel(7425);
         RenderSystem.disableTexture();
         int m = this.getMaxScroll();
         if (m > 0) {
            int n = (this.bottom - this.top) * (this.bottom - this.top) / this.getMaxPosition();
            n = MathHelper.clamp(n, 32, this.bottom - this.top - 8);
            int o = (int)this.scroll * (this.bottom - this.top - n) / m + this.top;
            if (o < this.top) {
               o = this.top;
            }

            bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex((double)i, (double)this.bottom, 0.0D).texture(0.0F, 1.0F).color(0, 0, 0, 255).next();
            bufferBuilder.vertex((double)j, (double)this.bottom, 0.0D).texture(1.0F, 1.0F).color(0, 0, 0, 255).next();
            bufferBuilder.vertex((double)j, (double)this.top, 0.0D).texture(1.0F, 0.0F).color(0, 0, 0, 255).next();
            bufferBuilder.vertex((double)i, (double)this.top, 0.0D).texture(0.0F, 0.0F).color(0, 0, 0, 255).next();
            tessellator.draw();
            bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex((double)i, (double)(o + n), 0.0D).texture(0.0F, 1.0F).color(128, 128, 128, 255).next();
            bufferBuilder.vertex((double)j, (double)(o + n), 0.0D).texture(1.0F, 1.0F).color(128, 128, 128, 255).next();
            bufferBuilder.vertex((double)j, (double)o, 0.0D).texture(1.0F, 0.0F).color(128, 128, 128, 255).next();
            bufferBuilder.vertex((double)i, (double)o, 0.0D).texture(0.0F, 0.0F).color(128, 128, 128, 255).next();
            tessellator.draw();
            bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex((double)i, (double)(o + n - 1), 0.0D).texture(0.0F, 1.0F).color(192, 192, 192, 255).next();
            bufferBuilder.vertex((double)(j - 1), (double)(o + n - 1), 0.0D).texture(1.0F, 1.0F).color(192, 192, 192, 255).next();
            bufferBuilder.vertex((double)(j - 1), (double)o, 0.0D).texture(1.0F, 0.0F).color(192, 192, 192, 255).next();
            bufferBuilder.vertex((double)i, (double)o, 0.0D).texture(0.0F, 0.0F).color(192, 192, 192, 255).next();
            tessellator.draw();
         }

         this.renderDecorations(mouseX, mouseY);
         RenderSystem.enableTexture();
         RenderSystem.shadeModel(7424);
         RenderSystem.enableAlphaTest();
         RenderSystem.disableBlend();
      }
   }

   public boolean mouseScrolled(double d, double e, double amount) {
      return this.realmsSimpleScrolledSelectionList.mouseScrolled(d, e, amount) ? true : super.mouseScrolled(d, e, amount);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return this.realmsSimpleScrolledSelectionList.mouseClicked(mouseX, mouseY, button) ? true : super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      return this.realmsSimpleScrolledSelectionList.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      return this.realmsSimpleScrolledSelectionList.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
   }
}
