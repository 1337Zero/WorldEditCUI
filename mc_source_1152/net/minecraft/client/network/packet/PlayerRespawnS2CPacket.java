package net.minecraft.client.network.packet;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.GameMode;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelGeneratorType;

public class PlayerRespawnS2CPacket implements Packet<ClientPlayPacketListener> {
   private DimensionType dimension;
   private long field_20667;
   private GameMode gameMode;
   private LevelGeneratorType generatorType;

   public PlayerRespawnS2CPacket() {
   }

   public PlayerRespawnS2CPacket(DimensionType dimension, long l, LevelGeneratorType levelGeneratorType, GameMode gameMode) {
      this.dimension = dimension;
      this.field_20667 = l;
      this.gameMode = gameMode;
      this.generatorType = levelGeneratorType;
   }

   public void apply(ClientPlayPacketListener clientPlayPacketListener) {
      clientPlayPacketListener.onPlayerRespawn(this);
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.dimension = DimensionType.byRawId(buf.readInt());
      this.field_20667 = buf.readLong();
      this.gameMode = GameMode.byId(buf.readUnsignedByte());
      this.generatorType = LevelGeneratorType.getTypeFromName(buf.readString(16));
      if (this.generatorType == null) {
         this.generatorType = LevelGeneratorType.DEFAULT;
      }

   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeInt(this.dimension.getRawId());
      buf.writeLong(this.field_20667);
      buf.writeByte(this.gameMode.getId());
      buf.writeString(this.generatorType.getName());
   }

   @Environment(EnvType.CLIENT)
   public DimensionType getDimension() {
      return this.dimension;
   }

   @Environment(EnvType.CLIENT)
   public long method_22425() {
      return this.field_20667;
   }

   @Environment(EnvType.CLIENT)
   public GameMode getGameMode() {
      return this.gameMode;
   }

   @Environment(EnvType.CLIENT)
   public LevelGeneratorType getGeneratorType() {
      return this.generatorType;
   }
}
