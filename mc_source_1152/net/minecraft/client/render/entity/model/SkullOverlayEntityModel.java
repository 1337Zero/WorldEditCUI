package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class SkullOverlayEntityModel extends SkullEntityModel {
   private final ModelPart field_3377 = new ModelPart(this, 32, 0);

   public SkullOverlayEntityModel() {
      super(0, 0, 64, 64);
      this.field_3377.addCuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.25F);
      this.field_3377.setPivot(0.0F, 0.0F, 0.0F);
   }

   public void render(float f, float g, float h) {
      super.render(f, g, h);
      this.field_3377.yaw = this.skull.yaw;
      this.field_3377.pitch = this.skull.pitch;
   }

   public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
      super.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
      this.field_3377.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
   }
}
