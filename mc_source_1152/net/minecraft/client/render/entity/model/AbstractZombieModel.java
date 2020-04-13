package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public abstract class AbstractZombieModel<T extends HostileEntity> extends BipedEntityModel<T> {
   protected AbstractZombieModel(float f, float g, int i, int j) {
      super(f, g, i, j);
   }

   public void setAngles(T hostileEntity, float f, float g, float h, float i, float j) {
      super.setAngles((LivingEntity)hostileEntity, f, g, h, i, j);
      boolean bl = this.isAttacking(hostileEntity);
      float k = MathHelper.sin(this.handSwingProgress * 3.1415927F);
      float l = MathHelper.sin((1.0F - (1.0F - this.handSwingProgress) * (1.0F - this.handSwingProgress)) * 3.1415927F);
      this.rightArm.roll = 0.0F;
      this.leftArm.roll = 0.0F;
      this.rightArm.yaw = -(0.1F - k * 0.6F);
      this.leftArm.yaw = 0.1F - k * 0.6F;
      float m = -3.1415927F / (bl ? 1.5F : 2.25F);
      this.rightArm.pitch = m;
      this.leftArm.pitch = m;
      ModelPart var10000 = this.rightArm;
      var10000.pitch += k * 1.2F - l * 0.4F;
      var10000 = this.leftArm;
      var10000.pitch += k * 1.2F - l * 0.4F;
      var10000 = this.rightArm;
      var10000.roll += MathHelper.cos(h * 0.09F) * 0.05F + 0.05F;
      var10000 = this.leftArm;
      var10000.roll -= MathHelper.cos(h * 0.09F) * 0.05F + 0.05F;
      var10000 = this.rightArm;
      var10000.pitch += MathHelper.sin(h * 0.067F) * 0.05F;
      var10000 = this.leftArm;
      var10000.pitch -= MathHelper.sin(h * 0.067F) * 0.05F;
   }

   public abstract boolean isAttacking(T hostileEntity);
}
