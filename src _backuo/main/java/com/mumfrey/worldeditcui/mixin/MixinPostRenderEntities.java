package com.mumfrey.worldeditcui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mumfrey.worldeditcui.LiteModWorldEditCUI;

import net.minecraft.client.render.GameRenderer;

import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class MixinPostRenderEntities {

	@Inject(method = "updateCameraAndRender(FJ)V", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", args = "ldc=litParticles"),cancellable = false)
	private void onPostRenderEntitiesWeCui(float partialTicks, long timeSlice, CallbackInfo ci) {
		//LiteModWorldEditCUI.instance.onPostRenderEntities(partialTicks);
	}
}
