package net.minecraft.world.chunk;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.IdList;
import net.minecraft.util.PacketByteBuf;
import org.apache.commons.lang3.ArrayUtils;

public class ArrayPalette<T> implements Palette<T> {
   private final IdList<T> idList;
   private final T[] array;
   private final PaletteResizeListener<T> resizeListener;
   private final Function<CompoundTag, T> valueDeserializer;
   private final int indexBits;
   private int size;

   public ArrayPalette(IdList<T> idList, int integer, PaletteResizeListener<T> resizeListener, Function<CompoundTag, T> valueDeserializer) {
      this.idList = idList;
      this.array = (Object[])(new Object[1 << integer]);
      this.indexBits = integer;
      this.resizeListener = resizeListener;
      this.valueDeserializer = valueDeserializer;
   }

   public int getIndex(T object) {
      int j;
      for(j = 0; j < this.size; ++j) {
         if (this.array[j] == object) {
            return j;
         }
      }

      j = this.size;
      if (j < this.array.length) {
         this.array[j] = object;
         ++this.size;
         return j;
      } else {
         return this.resizeListener.onResize(this.indexBits + 1, object);
      }
   }

   public boolean accepts(T object) {
      return ArrayUtils.contains(this.array, object);
   }

   @Nullable
   public T getByIndex(int index) {
      return index >= 0 && index < this.size ? this.array[index] : null;
   }

   @Environment(EnvType.CLIENT)
   public void fromPacket(PacketByteBuf buf) {
      this.size = buf.readVarInt();

      for(int i = 0; i < this.size; ++i) {
         this.array[i] = this.idList.get(buf.readVarInt());
      }

   }

   public void toPacket(PacketByteBuf buf) {
      buf.writeVarInt(this.size);

      for(int i = 0; i < this.size; ++i) {
         buf.writeVarInt(this.idList.getId(this.array[i]));
      }

   }

   public int getPacketSize() {
      int i = PacketByteBuf.getVarIntSizeBytes(this.getSize());

      for(int j = 0; j < this.getSize(); ++j) {
         i += PacketByteBuf.getVarIntSizeBytes(this.idList.getId(this.array[j]));
      }

      return i;
   }

   public int getSize() {
      return this.size;
   }

   public void fromTag(ListTag tag) {
      for(int i = 0; i < tag.size(); ++i) {
         this.array[i] = this.valueDeserializer.apply(tag.getCompound(i));
      }

      this.size = tag.size();
   }
}
