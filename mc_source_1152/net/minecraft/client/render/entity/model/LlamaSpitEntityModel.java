package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public class LlamaSpitEntityModel<T extends Entity> extends CompositeEntityModel<T> {
   private final ModelPart field_3433;

   public LlamaSpitEntityModel() {
      this(0.0F);
   }

   public LlamaSpitEntityModel(float f) {
      this.field_3433 = new ModelPart(this);
      int i = true;
      this.field_3433.setTextureOffset(0, 0).addCuboid(-4.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F, f);
      this.field_3433.setTextureOffset(0, 0).addCuboid(0.0F, -4.0F, 0.0F, 2.0F, 2.0F, 2.0F, f);
      this.field_3433.setTextureOffset(0, 0).addCuboid(0.0F, 0.0F, -4.0F, 2.0F, 2.0F, 2.0F, f);
      this.field_3433.setTextureOffset(0, 0).addCuboid(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F, f);
      this.field_3433.setTextureOffset(0, 0).addCuboid(2.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F, f);
      this.field_3433.setTextureOffset(0, 0).addCuboid(0.0F, 2.0F, 0.0F, 2.0F, 2.0F, 2.0F, f);
      this.field_3433.setTextureOffset(0, 0).addCuboid(0.0F, 0.0F, 2.0F, 2.0F, 2.0F, 2.0F, f);
      this.field_3433.setPivot(0.0F, 0.0F, 0.0F);
   }

   public void setAngles(T entity, float limbAngle, float limbDistance, float customAngle, float headYaw, float headPitch) {
   }

   public Iterable<ModelPart> getParts() {
      return ImmutableList.of(this.field_3433);
   }
}
