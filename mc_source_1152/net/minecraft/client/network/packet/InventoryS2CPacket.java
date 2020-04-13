package net.minecraft.client.network.packet;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.PacketByteBuf;

public class InventoryS2CPacket implements Packet<ClientPlayPacketListener> {
   private int guiId;
   private List<ItemStack> slotStackList;

   public InventoryS2CPacket() {
   }

   public InventoryS2CPacket(int guiId, DefaultedList<ItemStack> slotStackList) {
      this.guiId = guiId;
      this.slotStackList = DefaultedList.ofSize(slotStackList.size(), ItemStack.EMPTY);

      for(int i = 0; i < this.slotStackList.size(); ++i) {
         this.slotStackList.set(i, ((ItemStack)slotStackList.get(i)).copy());
      }

   }

   public void read(PacketByteBuf buf) throws IOException {
      this.guiId = buf.readUnsignedByte();
      int i = buf.readShort();
      this.slotStackList = DefaultedList.ofSize(i, ItemStack.EMPTY);

      for(int j = 0; j < i; ++j) {
         this.slotStackList.set(j, buf.readItemStack());
      }

   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeByte(this.guiId);
      buf.writeShort(this.slotStackList.size());
      Iterator var2 = this.slotStackList.iterator();

      while(var2.hasNext()) {
         ItemStack itemStack = (ItemStack)var2.next();
         buf.writeItemStack(itemStack);
      }

   }

   public void apply(ClientPlayPacketListener clientPlayPacketListener) {
      clientPlayPacketListener.onInventory(this);
   }

   @Environment(EnvType.CLIENT)
   public int getGuiId() {
      return this.guiId;
   }

   @Environment(EnvType.CLIENT)
   public List<ItemStack> getSlotStacks() {
      return this.slotStackList;
   }
}
