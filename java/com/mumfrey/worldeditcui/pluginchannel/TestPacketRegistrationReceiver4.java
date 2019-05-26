    
package com.mumfrey.worldeditcui.pluginchannel;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class TestPacketRegistrationReceiver4 implements Packet<INetHandlerPlayServer> {
    private int data;

    public TestPacketRegistrationReceiver4() {}

    public TestPacketRegistrationReceiver4(int data) {
        this.data = data;
    }

    @Override
    public void readPacketData(PacketBuffer buf) {
        data = buf.readVarInt();
        System.out.println("reading data");
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        buf.writeVarInt(data);
        System.out.println("write data");
    }

    @Override
    public void processPacket(INetHandlerPlayServer handler) {
    	myLog("Server received test packet: " + data);
    }
    private void myLog(String msg) {
    	System.out.println("[TestPacketRegistrationReceiver4] " + msg);
    }
}