package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public class ShulkerBulletEntityModel<T extends Entity> extends CompositeEntityModel<T> {
   private final ModelPart field_3556;

   public ShulkerBulletEntityModel() {
      this.textureWidth = 64;
      this.textureHeight = 32;
      this.field_3556 = new ModelPart(this);
      this.field_3556.setTextureOffset(0, 0).addCuboid(-4.0F, -4.0F, -1.0F, 8.0F, 8.0F, 2.0F, 0.0F);
      this.field_3556.setTextureOffset(0, 10).addCuboid(-1.0F, -4.0F, -4.0F, 2.0F, 8.0F, 8.0F, 0.0F);
      this.field_3556.setTextureOffset(20, 0).addCuboid(-4.0F, -1.0F, -4.0F, 8.0F, 2.0F, 8.0F, 0.0F);
      this.field_3556.setPivot(0.0F, 0.0F, 0.0F);
   }

   public Iterable<ModelPart> getParts() {
      return ImmutableList.of(this.field_3556);
   }

   public void setAngles(T entity, float limbAngle, float limbDistance, float customAngle, float headYaw, float headPitch) {
      this.field_3556.yaw = headYaw * 0.017453292F;
      this.field_3556.pitch = headPitch * 0.017453292F;
   }
}
