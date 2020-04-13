package net.minecraft.server.network.packet;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.util.PacketByteBuf;

public class CreativeInventoryActionC2SPacket implements Packet<ServerPlayPacketListener> {
   private int slot;
   private ItemStack stack;

   public CreativeInventoryActionC2SPacket() {
      this.stack = ItemStack.EMPTY;
   }

   @Environment(EnvType.CLIENT)
   public CreativeInventoryActionC2SPacket(int i, ItemStack itemStack) {
      this.stack = ItemStack.EMPTY;
      this.slot = i;
      this.stack = itemStack.copy();
   }

   public void apply(ServerPlayPacketListener serverPlayPacketListener) {
      serverPlayPacketListener.onCreativeInventoryAction(this);
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.slot = buf.readShort();
      this.stack = buf.readItemStack();
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeShort(this.slot);
      buf.writeItemStack(this.stack);
   }

   public int getSlot() {
      return this.slot;
   }

   public ItemStack getItemStack() {
      return this.stack;
   }
}
