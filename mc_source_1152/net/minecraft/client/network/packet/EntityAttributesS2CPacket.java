package net.minecraft.client.network.packet;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.util.PacketByteBuf;

public class EntityAttributesS2CPacket implements Packet<ClientPlayPacketListener> {
   private int entityId;
   private final List<EntityAttributesS2CPacket.Entry> entries = Lists.newArrayList();

   public EntityAttributesS2CPacket() {
   }

   public EntityAttributesS2CPacket(int entityId, Collection<EntityAttributeInstance> attributes) {
      this.entityId = entityId;
      Iterator var3 = attributes.iterator();

      while(var3.hasNext()) {
         EntityAttributeInstance entityAttributeInstance = (EntityAttributeInstance)var3.next();
         this.entries.add(new EntityAttributesS2CPacket.Entry(entityAttributeInstance.getAttribute().getId(), entityAttributeInstance.getBaseValue(), entityAttributeInstance.getModifiers()));
      }

   }

   public void read(PacketByteBuf buf) throws IOException {
      this.entityId = buf.readVarInt();
      int i = buf.readInt();

      for(int j = 0; j < i; ++j) {
         String string = buf.readString(64);
         double d = buf.readDouble();
         List<EntityAttributeModifier> list = Lists.newArrayList();
         int k = buf.readVarInt();

         for(int l = 0; l < k; ++l) {
            UUID uUID = buf.readUuid();
            list.add(new EntityAttributeModifier(uUID, "Unknown synced attribute modifier", buf.readDouble(), EntityAttributeModifier.Operation.fromId(buf.readByte())));
         }

         this.entries.add(new EntityAttributesS2CPacket.Entry(string, d, list));
      }

   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeVarInt(this.entityId);
      buf.writeInt(this.entries.size());
      Iterator var2 = this.entries.iterator();

      while(var2.hasNext()) {
         EntityAttributesS2CPacket.Entry entry = (EntityAttributesS2CPacket.Entry)var2.next();
         buf.writeString(entry.getId());
         buf.writeDouble(entry.getBaseValue());
         buf.writeVarInt(entry.getModifiers().size());
         Iterator var4 = entry.getModifiers().iterator();

         while(var4.hasNext()) {
            EntityAttributeModifier entityAttributeModifier = (EntityAttributeModifier)var4.next();
            buf.writeUuid(entityAttributeModifier.getId());
            buf.writeDouble(entityAttributeModifier.getAmount());
            buf.writeByte(entityAttributeModifier.getOperation().getId());
         }
      }

   }

   public void apply(ClientPlayPacketListener clientPlayPacketListener) {
      clientPlayPacketListener.onEntityAttributes(this);
   }

   @Environment(EnvType.CLIENT)
   public int getEntityId() {
      return this.entityId;
   }

   @Environment(EnvType.CLIENT)
   public List<EntityAttributesS2CPacket.Entry> getEntries() {
      return this.entries;
   }

   public class Entry {
      private final String id;
      private final double baseValue;
      private final Collection<EntityAttributeModifier> modifiers;

      public Entry(String baseValue, double d, Collection<EntityAttributeModifier> collection) {
         this.id = baseValue;
         this.baseValue = d;
         this.modifiers = collection;
      }

      public String getId() {
         return this.id;
      }

      public double getBaseValue() {
         return this.baseValue;
      }

      public Collection<EntityAttributeModifier> getModifiers() {
         return this.modifiers;
      }
   }
}
