package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexFormatElement;

@Environment(EnvType.CLIENT)
public class RealmsVertexFormatElement {
   private final VertexFormatElement v;

   public RealmsVertexFormatElement(VertexFormatElement vertexFormatElement) {
      this.v = vertexFormatElement;
   }

   public VertexFormatElement getVertexFormatElement() {
      return this.v;
   }

   public boolean isPosition() {
      return this.v.isPosition();
   }

   public int getIndex() {
      return this.v.getIndex();
   }

   public int getByteSize() {
      return this.v.getSize();
   }

   public int getCount() {
      return this.v.getCount();
   }

   public int hashCode() {
      return this.v.hashCode();
   }

   public boolean equals(Object o) {
      return this.v.equals(o);
   }

   public String toString() {
      return this.v.toString();
   }
}
