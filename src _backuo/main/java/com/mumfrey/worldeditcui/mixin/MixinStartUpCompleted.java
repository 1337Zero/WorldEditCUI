package com.mumfrey.worldeditcui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mumfrey.worldeditcui.LiteModWorldEditCUI;

import net.minecraft.client.MinecraftClient;

import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftClient.class)
public class MixinStartUpCompleted {

    @Inject(method = "init()V", at = @At("RETURN") , cancellable = false)
    private void onStartupComplete(CallbackInfo ci)
    {
        LiteModWorldEditCUI.instance.onInitCompleted();
    }
	
}
