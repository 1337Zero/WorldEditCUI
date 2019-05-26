package com.mumfrey.worldeditcui.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.base.Charsets;
import com.mumfrey.worldeditcui.LiteModWorldEditCUI;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.injection.At;

@Mixin(NetHandlerPlayClient.class)
public class MixinCustomPayLoadHandler {

	@Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
	private void handleModCustomPayload(SPacketCustomPayload packet, CallbackInfo ci) {
		ResourceLocation channelName = packet.getChannelName();
		PacketBuffer data = packet.getBufferData();
		onCustomPayload(channelName.toString(), data);

	}

	public void onCustomPayload(String channel, PacketBuffer data) {
		try {
			int readableBytes = data.readableBytes();
			if (readableBytes > 0) {
				byte[] payload = new byte[readableBytes];
				data.readBytes(payload);
				LiteModWorldEditCUI.instance.receiveMessage(new String(payload, Charsets.UTF_8), channel);
			} else {
				System.out.println("Warning, invalid (zero length) payload received from server");
			}
		} catch (Exception ex) {
		}
	}
}
