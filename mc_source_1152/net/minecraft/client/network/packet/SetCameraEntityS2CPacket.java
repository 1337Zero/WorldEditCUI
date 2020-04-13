package net.minecraft.client.network.packet;

import java.io.IOException;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.World;

public class SetCameraEntityS2CPacket implements Packet<ClientPlayPacketListener> {
   public int id;

   public SetCameraEntityS2CPacket() {
   }

   public SetCameraEntityS2CPacket(Entity entity) {
      this.id = entity.getEntityId();
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.id = buf.readVarInt();
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeVarInt(this.id);
   }

   public void apply(ClientPlayPacketListener clientPlayPacketListener) {
      clientPlayPacketListener.onSetCameraEntity(this);
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   public Entity getEntity(World world) {
      return world.getEntityById(this.id);
   }
}
