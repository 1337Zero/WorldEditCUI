package com.mumfrey.worldeditcui.mixin;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import org.dimdev.rift.injectedmethods.RiftCPacketCustomPayload;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.base.Charsets;

@Mixin(NetHandlerPlayServer.class)
public class MixinCustomNetHandlerPlayServer {
    @Shadow @Final private MinecraftServer server;
    @Shadow public EntityPlayerMP player;
    @Shadow @Final public NetworkManager netManager;

    @Inject(method = "processCustomPayload", at = @At("HEAD"), cancellable = true)
    private void handleModCustomPayload(CPacketCustomPayload packet, CallbackInfo ci) {
        ResourceLocation channelName = ((RiftCPacketCustomPayload) packet).getChannelName();
        PacketBuffer data = ((RiftCPacketCustomPayload) packet).getData();

        String msg = "";
        
        int readableBytes = data.readableBytes();
		if (readableBytes > 0)
		{
			byte[] payload = new byte[readableBytes];
			data.readBytes(payload);
			msg = new String(payload, Charsets.UTF_8);
		}
		
        System.out.println("[Server] Got data " + msg + " on Channel " + channelName);
        
       /* for (CustomPayloadHandler customPayloadHandler : RiftLoader.instance.getListeners(CustomPayloadHandler.class)) {
            if (customPayloadHandler.serverHandlesChannel(channelName)) {
                customPayloadHandler.serverHandleCustomPayload(channelName, data);
            }
        }
        
        

        Class<? extends Message> messageClass = Message.REGISTRY.get(channelName);
        if (messageClass != null) {
            try {
                Message message = RiftLoader.instance.newInstance(messageClass);
                message.read(data);
                message.process(new ServerMessageContext(server, player, netManager));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Error creating " + messageClass, e);
            }
            ci.cancel();
        }*/
    }
}
