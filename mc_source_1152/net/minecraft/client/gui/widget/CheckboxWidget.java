package net.minecraft.client.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class CheckboxWidget extends AbstractPressableButtonWidget {
   private static final Identifier TEXTURE = new Identifier("textures/gui/checkbox.png");
   boolean checked;

   public CheckboxWidget(int x, int y, int width, int height, String message, boolean checked) {
      super(x, y, width, height, message);
      this.checked = checked;
   }

   public void onPress() {
      this.checked = !this.checked;
   }

   public boolean isChecked() {
      return this.checked;
   }

   public void renderButton(int mouseX, int mouseY, float delta) {
      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      minecraftClient.getTextureManager().bindTexture(TEXTURE);
      RenderSystem.enableDepthTest();
      TextRenderer textRenderer = minecraftClient.textRenderer;
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
      blit(this.x, this.y, 0.0F, this.checked ? 20.0F : 0.0F, 20, this.height, 32, 64);
      this.renderBg(minecraftClient, mouseX, mouseY);
      int i = 14737632;
      this.drawString(textRenderer, this.getMessage(), this.x + 24, this.y + (this.height - 8) / 2, 14737632 | MathHelper.ceil(this.alpha * 255.0F) << 24);
   }
}
