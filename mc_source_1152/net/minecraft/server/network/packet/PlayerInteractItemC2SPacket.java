package net.minecraft.server.network.packet;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.util.Hand;
import net.minecraft.util.PacketByteBuf;

public class PlayerInteractItemC2SPacket implements Packet<ServerPlayPacketListener> {
   private Hand hand;

   public PlayerInteractItemC2SPacket() {
   }

   public PlayerInteractItemC2SPacket(Hand hand) {
      this.hand = hand;
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.hand = (Hand)buf.readEnumConstant(Hand.class);
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeEnumConstant(this.hand);
   }

   public void apply(ServerPlayPacketListener serverPlayPacketListener) {
      serverPlayPacketListener.onPlayerInteractItem(this);
   }

   public Hand getHand() {
      return this.hand;
   }
}
