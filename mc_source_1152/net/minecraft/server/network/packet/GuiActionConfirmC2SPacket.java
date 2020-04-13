package net.minecraft.server.network.packet;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.util.PacketByteBuf;

public class GuiActionConfirmC2SPacket implements Packet<ServerPlayPacketListener> {
   private int windowId;
   private short actionId;
   private boolean accepted;

   public GuiActionConfirmC2SPacket() {
   }

   @Environment(EnvType.CLIENT)
   public GuiActionConfirmC2SPacket(int windowId, short actionId, boolean accepted) {
      this.windowId = windowId;
      this.actionId = actionId;
      this.accepted = accepted;
   }

   public void apply(ServerPlayPacketListener serverPlayPacketListener) {
      serverPlayPacketListener.onConfirmTransaction(this);
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.windowId = buf.readByte();
      this.actionId = buf.readShort();
      this.accepted = buf.readByte() != 0;
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeByte(this.windowId);
      buf.writeShort(this.actionId);
      buf.writeByte(this.accepted ? 1 : 0);
   }

   public int getWindowId() {
      return this.windowId;
   }

   public short getSyncId() {
      return this.actionId;
   }
}
