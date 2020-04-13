package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public class SquidEntityModel<T extends Entity> extends CompositeEntityModel<T> {
   private final ModelPart head;
   private final ModelPart[] field_3574 = new ModelPart[8];
   private final ImmutableList<ModelPart> field_20942;

   public SquidEntityModel() {
      int i = true;
      this.head = new ModelPart(this, 0, 0);
      this.head.addCuboid(-6.0F, -8.0F, -6.0F, 12.0F, 16.0F, 12.0F);
      ModelPart var10000 = this.head;
      var10000.pivotY += 8.0F;

      for(int j = 0; j < this.field_3574.length; ++j) {
         this.field_3574[j] = new ModelPart(this, 48, 0);
         double d = (double)j * 3.141592653589793D * 2.0D / (double)this.field_3574.length;
         float f = (float)Math.cos(d) * 5.0F;
         float g = (float)Math.sin(d) * 5.0F;
         this.field_3574[j].addCuboid(-1.0F, 0.0F, -1.0F, 2.0F, 18.0F, 2.0F);
         this.field_3574[j].pivotX = f;
         this.field_3574[j].pivotZ = g;
         this.field_3574[j].pivotY = 15.0F;
         d = (double)j * 3.141592653589793D * -2.0D / (double)this.field_3574.length + 1.5707963267948966D;
         this.field_3574[j].yaw = (float)d;
      }

      Builder<ModelPart> builder = ImmutableList.builder();
      builder.add(this.head);
      builder.addAll(Arrays.asList(this.field_3574));
      this.field_20942 = builder.build();
   }

   public void setAngles(T entity, float limbAngle, float limbDistance, float customAngle, float headYaw, float headPitch) {
      ModelPart[] var7 = this.field_3574;
      int var8 = var7.length;

      for(int var9 = 0; var9 < var8; ++var9) {
         ModelPart modelPart = var7[var9];
         modelPart.pitch = customAngle;
      }

   }

   public Iterable<ModelPart> getParts() {
      return this.field_20942;
   }
}
