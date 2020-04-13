package com.mumfrey.worldeditcui.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;

public class RenderHelper {

	/**
	 * Prepares the Rendering Engine for drawing lines on the screen
	 * @param partialTicks
	 * @param player
	 */
	public static void setUpRenderer(float partialTicks,ClientPlayerEntity player){

		
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
	    
	    Camera c = MinecraftClient.getInstance().gameRenderer.getCamera();    	
	    Vec3d vec = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
	    GlStateManager.translated(-vec.x, -c.getPos().y, -vec.z);
	    
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
