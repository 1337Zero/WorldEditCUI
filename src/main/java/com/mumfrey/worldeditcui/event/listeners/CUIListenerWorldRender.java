package com.mumfrey.worldeditcui.event.listeners;

import com.mumfrey.worldeditcui.WorldEditCUI;
import com.mumfrey.worldeditcui.render.RenderHelper;
import com.mumfrey.worldeditcui.util.Vector3;

import net.minecraft.client.MinecraftClient;

/**
 * Listener for WorldRenderEvent
 * 
 * @author lahwran
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class CUIListenerWorldRender {
	private WorldEditCUI controller;


	public CUIListenerWorldRender(WorldEditCUI controller, MinecraftClient minecraft) {
		this.controller = controller;
	}

	public void onRender(float partialTicks, double cameraX, double cameraY, double cameraZ) {
		try {
			RenderHelper.setUpRenderer(partialTicks, MinecraftClient.getInstance().player);
			try {				
				this.controller.renderSelections(new Vector3(0,0,0), partialTicks);				
			} catch (Exception e) {
				e.printStackTrace();
			}
			RenderHelper.normalizeRenderer();
		} catch (Exception ex) {
		}
	}
}
