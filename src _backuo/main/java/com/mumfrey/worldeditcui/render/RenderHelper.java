package com.mumfrey.worldeditcui.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.network.ClientPlayerEntity;

public class RenderHelper {

	/**
	 * Prepares the Rendering Engine for drawing lines on the screen
	 * @param partialTicks
	 * @param player
	 */
	public static void setUpRenderer(float partialTicks,ClientPlayerEntity player){
		double x = player.prevX + (player.getX() - player.prevX) * partialTicks;
		double y = player.prevY + (player.getY() - player.prevY) * partialTicks;
		double z = player.prevZ + (player.getZ() - player.prevZ) * partialTicks;
		
		GlStateManager.pushMatrix();
		GlStateManager.multiTexCoords2f(GL13.GL_TEXTURE1,240F, 240F);
	    
	    GlStateManager.enableBlend();
		GL11.glEnable(2848);
	    GlStateManager.blendFunc(770, 771);
	    GlStateManager.disableTexture();
		GlStateManager.disableLighting();
	    GlStateManager.disableDepthTest();
	    
	    GlStateManager.depthFunc(515);
	    GL11.glLineWidth(1.0F);
	    GlStateManager.translated(-x, -y, -z);
	    RenderSystem.shadeModel(7425);
	    RenderSystem.lineWidth(1.0F);
	}	
	/**
	 * Normalizes the Render Engine, call this if you are done with drawin stuff
	 */
	public static void normalizeRenderer(){
	    GlStateManager.enableTexture();
	    GlStateManager.disableBlend();
	    GlStateManager.popMatrix();
	    GlStateManager.enableDepthTest();
	    RenderSystem.shadeModel(7424);
	}
	
}
