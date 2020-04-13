package net.minecraft.server.network.packet;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.Difficulty;

public class UpdateDifficultyC2SPacket implements Packet<ServerPlayPacketListener> {
   private Difficulty difficulty;

   public UpdateDifficultyC2SPacket() {
   }

   public UpdateDifficultyC2SPacket(Difficulty difficulty) {
      this.difficulty = difficulty;
   }

   public void apply(ServerPlayPacketListener serverPlayPacketListener) {
      serverPlayPacketListener.onUpdateDifficulty(this);
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.difficulty = Difficulty.byOrdinal(buf.readUnsignedByte());
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeByte(this.difficulty.getId());
   }

   public Difficulty getDifficulty() {
      return this.difficulty;
   }
}
