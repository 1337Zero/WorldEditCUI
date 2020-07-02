package com.mumfrey.worldeditcui.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Vec3d;

public class RenderHelper {

	/**
	 * Prepares the Rendering Engine for drawing lines on the screen
	 * 
	 * @param partialTicks
	 * @param player
	 */
	public static void setUpRenderer(float partialTicks, ClientPlayerEntity player) {

		GlStateManager.pushMatrix();
		GlStateManager.multiTexCoords2f(GL13.GL_TEXTURE1, 240F, 240F);

		GlStateManager.enableBlend();
		GL11.glEnable(2848);
		GlStateManager.blendFunc(770, 771);
		GlStateManager.disableTexture();
		GlStateManager.disableLighting();
		GlStateManager.disableDepthTest();

		GlStateManager.depthFunc(515);
		GL11.glLineWidth(1.0F);

		Camera c = MinecraftClient.getInstance().gameRenderer.getCamera();
		Vec3d vec = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
		GlStateManager.translated(-vec.x, -c.getPos().y, -vec.z);

		RenderSystem.shadeModel(7425);
		RenderSystem.lineWidth(1.0F);
	}

	/**
	 * Normalizes the Render Engine, call this if you are done with drawin stuff
	 */
	public static void normalizeRenderer() {
		GlStateManager.enableTexture();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
		GlStateManager.enableDepthTest();
		RenderSystem.shadeModel(7424);
	}

	/**
	 * Draws a solid color rectangle with the specified coordinates and color.
	 */
	public static void drawRect(int left, int top, int right, int bottom, int color) {
		if (left < right) {
			int i = left;
			left = right;
			right = i;
		}

		if (top < bottom) {
			int j = top;
			top = bottom;
			bottom = j;
		}

		float f3 = (float) (color >> 24 & 255) / 255.0F;
		float f = (float) (color >> 16 & 255) / 255.0F;
		float f1 = (float) (color >> 8 & 255) / 255.0F;
		float f2 = (float) (color & 255) / 255.0F;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture();
		//GlStateManager.SrcFactor.SRC_ALPHA.
		GlStateManager.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA.field_22545,
				GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA.field_22528, GlStateManager.SrcFactor.ONE.field_22545,
				GlStateManager.DstFactor.ZERO.field_22528);
		GlStateManager.color4f(f, f1, f2, f3);
		bufferbuilder.begin(7, VertexFormats.POSITION);
		bufferbuilder.vertex((double) left, (double) bottom, 0.0D);
		bufferbuilder.next();
		bufferbuilder.vertex((double) right, (double) bottom, 0.0D);
		bufferbuilder.next();
		bufferbuilder.vertex((double) right, (double) top, 0.0D);
		bufferbuilder.next();
		bufferbuilder.vertex((double) left, (double) top, 0.0D);
		bufferbuilder.next();
		tessellator.draw();
		GlStateManager.enableTexture();
		GlStateManager.disableBlend();
	}

	/**
	 * Draws a rectangle with a vertical gradient between the specified colors (ARGB
	 * format). Args : x1, y1, x2, y2, topColor, bottomColor
	 */
	public static void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
		float f = (float) (startColor >> 24 & 255) / 255.0F;
		float f1 = (float) (startColor >> 16 & 255) / 255.0F;
		float f2 = (float) (startColor >> 8 & 255) / 255.0F;
		float f3 = (float) (startColor & 255) / 255.0F;
		float f4 = (float) (endColor >> 24 & 255) / 255.0F;
		float f5 = (float) (endColor >> 16 & 255) / 255.0F;
		float f6 = (float) (endColor >> 8 & 255) / 255.0F;
		float f7 = (float) (endColor & 255) / 255.0F;
		float zLevel = 1.0f;
		GlStateManager.disableTexture();
		GlStateManager.enableBlend();
		GlStateManager.disableAlphaTest();
		GlStateManager.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA.field_22545,
				GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA.field_22528, GlStateManager.SrcFactor.ONE.field_22545,
				GlStateManager.DstFactor.ZERO.field_22528);
		GlStateManager.shadeModel(7425);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, VertexFormats.POSITION_COLOR);
		bufferbuilder.vertex((double) right, (double) top, (double) zLevel).color(f1, f2, f3, f).next();
		bufferbuilder.vertex((double) left, (double) top, (double) zLevel).color(f1, f2, f3, f).next();
		bufferbuilder.vertex((double) left, (double) bottom, (double) zLevel).color(f5, f6, f7, f4).next();
		bufferbuilder.vertex((double) right, (double) bottom, (double) zLevel).color(f5, f6, f7, f4).next();
		tessellator.draw();
		GlStateManager.shadeModel(7424);
		GlStateManager.disableBlend();
		GlStateManager.enableAlphaTest();
		GlStateManager.enableTexture();
	}

}
