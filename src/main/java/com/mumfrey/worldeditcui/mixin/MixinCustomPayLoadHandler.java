package com.mumfrey.worldeditcui.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.base.Charsets;
import com.mumfrey.worldeditcui.LiteModWorldEditCUI;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.util.PacketByteBuf;

import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinCustomPayLoadHandler {

	@Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = false)
	private void handleModCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
		PacketByteBuf data = packet.getData();		
		String channel = packet.getChannel().getNamespace();	
		System.out.println("got message from server channel = " + channel);
		System.out.println("data = " + data);
		System.out.println("channel = " + channel);
		onCustomPayload(channel, data);
	}

	public void onCustomPayload(String channel, PacketByteBuf data) {
		try {
			int readableBytes = data.readableBytes();
			if (readableBytes > 0) {
				byte[] payload = new byte[readableBytes];
				data.readBytes(payload);
				System.out.println("instance found ?" + LiteModWorldEditCUI.instance);
				LiteModWorldEditCUI.instance.receiveMessage(new String(payload, Charsets.UTF_8), channel);
			} else {
				System.out.println("Warning, invalid (zero length) payload received from server");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
}
