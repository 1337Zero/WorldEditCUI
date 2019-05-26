package com.mumfrey.worldeditcui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mumfrey.worldeditcui.LiteModWorldEditCUI;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public class MixinStartUpCompleted {

    @Inject(method = "init()V", at = @At("RETURN"))
    private void onStartupComplete(CallbackInfo ci)
    {
        LiteModWorldEditCUI.instance.onInitCompleted();
    }
	
}
