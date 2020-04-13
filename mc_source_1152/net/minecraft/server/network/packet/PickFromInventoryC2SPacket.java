package net.minecraft.server.network.packet;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.util.PacketByteBuf;

public class PickFromInventoryC2SPacket implements Packet<ServerPlayPacketListener> {
   private int slot;

   public PickFromInventoryC2SPacket() {
   }

   @Environment(EnvType.CLIENT)
   public PickFromInventoryC2SPacket(int slot) {
      this.slot = slot;
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.slot = buf.readVarInt();
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeVarInt(this.slot);
   }

   public void apply(ServerPlayPacketListener serverPlayPacketListener) {
      serverPlayPacketListener.onPickFromInventory(this);
   }

   public int getSlot() {
      return this.slot;
   }
}
