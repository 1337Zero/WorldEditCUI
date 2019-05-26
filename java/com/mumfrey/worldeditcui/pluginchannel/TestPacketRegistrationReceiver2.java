    
package com.mumfrey.worldeditcui.pluginchannel;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class TestPacketRegistrationReceiver2 implements Packet<INetHandlerPlayServer> {
    private int data;

    public TestPacketRegistrationReceiver2() {}

    public TestPacketRegistrationReceiver2(int data) {
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
    	System.out.println("[TestPacketRegistrationReceiver2] " + msg);
    }
}