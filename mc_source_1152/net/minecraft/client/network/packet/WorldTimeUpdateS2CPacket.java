package net.minecraft.client.network.packet;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.util.PacketByteBuf;

public class WorldTimeUpdateS2CPacket implements Packet<ClientPlayPacketListener> {
   private long time;
   private long timeOfDay;

   public WorldTimeUpdateS2CPacket() {
   }

   public WorldTimeUpdateS2CPacket(long time, long timeOfDay, boolean bl) {
      this.time = time;
      this.timeOfDay = timeOfDay;
      if (!bl) {
         this.timeOfDay = -this.timeOfDay;
         if (this.timeOfDay == 0L) {
            this.timeOfDay = -1L;
         }
      }

   }

   public void read(PacketByteBuf buf) throws IOException {
      this.time = buf.readLong();
      this.timeOfDay = buf.readLong();
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeLong(this.time);
      buf.writeLong(this.timeOfDay);
   }

   public void apply(ClientPlayPacketListener clientPlayPacketListener) {
      clientPlayPacketListener.onWorldTimeUpdate(this);
   }

   @Environment(EnvType.CLIENT)
   public long getTime() {
      return this.time;
   }

   @Environment(EnvType.CLIENT)
   public long getTimeOfDay() {
      return this.timeOfDay;
   }
}
