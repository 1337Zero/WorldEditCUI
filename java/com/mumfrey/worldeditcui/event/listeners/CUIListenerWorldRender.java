package com.mumfrey.worldeditcui.event.listeners;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import com.mumfrey.worldeditcui.WorldEditCUI;
import com.mumfrey.worldeditcui.util.Vector3;

/**
 * Listener for WorldRenderEvent
 * 
 * @author lahwran
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class CUIListenerWorldRender
{
	private WorldEditCUI controller;
	
	private Minecraft minecraft;
	
	public CUIListenerWorldRender(WorldEditCUI controller, Minecraft minecraft)
	{
		this.controller = controller;
		this.minecraft = minecraft;
	}
	
	public void onRender(float partialTicks)
	{
		try
		{
			//TODO: For what is this used?
			//OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
			
			//glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.blendFunc(0x302, 0x303);
			//glEnableBlend();
			GlStateManager.enableBlend();
			//glEnableAlphaTest();
			GlStateManager.enableAlphaTest();
			//glAlphaFunc(0x204, 0.0F);
			GlStateManager.alphaFunc(0x204, 0.0F);
			//glDisableTexture2D();
			GlStateManager.disableTexture2D();
			//glEnableDepthTest();
			GlStateManager.enableDepthTest();
			//glDepthMask(false);
			GlStateManager.depthMask(false);
			//glPushMatrix();
			GlStateManager.pushMatrix();
			//glDisableFog();
			GlStateManager.disableFog();
			try
			{
				Vector3 cameraPos = new Vector3(this.minecraft.getRenderViewEntity(), partialTicks); 
				GlStateManager.color4f(1.0F, 1.0F, 1.0F, 0.5F);
				//glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
				this.controller.renderSelections(cameraPos, partialTicks);
			}
			catch (Exception e)
			{
			}
			GlStateManager.depthFunc(0x203);
			//glDepthFunc(GL_LEQUAL);
		    GlStateManager.popMatrix();
			//glPopMatrix();
			
		    GlStateManager.depthMask(true);
			//glDepthMask(true);
			GlStateManager.enableTexture2D();
		    //glEnableTexture2D();
			GlStateManager.disableBlend();
			//glDisableBlend();
			GlStateManager.alphaFunc(0x204, 0.1F);
			//glAlphaFunc(GL_GREATER, 0.1F);
		}
		catch (Exception ex) {}
	}
}
