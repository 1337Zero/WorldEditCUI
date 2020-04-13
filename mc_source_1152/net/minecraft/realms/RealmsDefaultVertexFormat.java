package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexFormats;

@Environment(EnvType.CLIENT)
public class RealmsDefaultVertexFormat {
   public static final RealmsVertexFormat POSITION_COLOR;
   public static final RealmsVertexFormat POSITION_TEX_COLOR;

   static {
      POSITION_COLOR = new RealmsVertexFormat(VertexFormats.POSITION_COLOR);
      POSITION_TEX_COLOR = new RealmsVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR);
   }
}
