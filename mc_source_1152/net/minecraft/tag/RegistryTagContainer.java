package net.minecraft.tag;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

public class RegistryTagContainer<T> extends TagContainer<T> {
   private final Registry<T> registry;

   public RegistryTagContainer(Registry<T> registry, String path, String type) {
      super(registry::getOrEmpty, path, false, type);
      this.registry = registry;
   }

   public void toPacket(PacketByteBuf packetByteBuf) {
      Map<Identifier, Tag<T>> map = this.getEntries();
      packetByteBuf.writeVarInt(map.size());
      Iterator var3 = map.entrySet().iterator();

      while(var3.hasNext()) {
         Entry<Identifier, Tag<T>> entry = (Entry)var3.next();
         packetByteBuf.writeIdentifier((Identifier)entry.getKey());
         packetByteBuf.writeVarInt(((Tag)entry.getValue()).values().size());
         Iterator var5 = ((Tag)entry.getValue()).values().iterator();

         while(var5.hasNext()) {
            T object = var5.next();
            packetByteBuf.writeVarInt(this.registry.getRawId(object));
         }
      }

   }

   public void fromPacket(PacketByteBuf buf) {
      Map<Identifier, Tag<T>> map = Maps.newHashMap();
      int i = buf.readVarInt();

      for(int j = 0; j < i; ++j) {
         Identifier identifier = buf.readIdentifier();
         int k = buf.readVarInt();
         Tag.Builder<T> builder = Tag.Builder.create();

         for(int l = 0; l < k; ++l) {
            builder.add(this.registry.get(buf.readVarInt()));
         }

         map.put(identifier, builder.build(identifier));
      }

      this.setEntries(map);
   }
}
