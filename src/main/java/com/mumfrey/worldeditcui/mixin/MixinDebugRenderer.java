package com.mumfrey.worldeditcui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mumfrey.worldeditcui.LiteModWorldEditCUI;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;


@Mixin(DebugRenderer.class)
public class MixinDebugRenderer {	
	@Inject(method = "render", at = @At("HEAD"), cancellable = false)
	private void handleModCustomPayload(MatrixStack metrics, VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		LiteModWorldEditCUI.instance.onHudRender(MinecraftClient.getInstance().getTickDelta());
	}
	
}
