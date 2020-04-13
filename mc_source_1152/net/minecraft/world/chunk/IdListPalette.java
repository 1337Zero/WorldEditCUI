package net.minecraft.world.chunk;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.IdList;
import net.minecraft.util.PacketByteBuf;

public class IdListPalette<T> implements Palette<T> {
   private final IdList<T> idList;
   private final T fallback;

   public IdListPalette(IdList<T> idList, T defaultValue) {
      this.idList = idList;
      this.fallback = defaultValue;
   }

   public int getIndex(T object) {
      int i = this.idList.getId(object);
      return i == -1 ? 0 : i;
   }

   public boolean accepts(T object) {
      return true;
   }

   public T getByIndex(int index) {
      T object = this.idList.get(index);
      return object == null ? this.fallback : object;
   }

   @Environment(EnvType.CLIENT)
   public void fromPacket(PacketByteBuf buf) {
   }

   public void toPacket(PacketByteBuf buf) {
   }

   public int getPacketSize() {
      return PacketByteBuf.getVarIntSizeBytes(0);
   }

   public void fromTag(ListTag tag) {
   }
}
