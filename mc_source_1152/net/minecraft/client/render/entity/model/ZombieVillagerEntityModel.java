package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class ZombieVillagerEntityModel<T extends ZombieEntity> extends BipedEntityModel<T> implements ModelWithHat {
   private ModelPart hat;

   public ZombieVillagerEntityModel(float f, boolean bl) {
      super(f, 0.0F, 64, bl ? 32 : 64);
      if (bl) {
         this.head = new ModelPart(this, 0, 0);
         this.head.addCuboid(-4.0F, -10.0F, -4.0F, 8.0F, 8.0F, 8.0F, f);
         this.torso = new ModelPart(this, 16, 16);
         this.torso.addCuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, f + 0.1F);
         this.rightLeg = new ModelPart(this, 0, 16);
         this.rightLeg.setPivot(-2.0F, 12.0F, 0.0F);
         this.rightLeg.addCuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, f + 0.1F);
         this.leftLeg = new ModelPart(this, 0, 16);
         this.leftLeg.mirror = true;
         this.leftLeg.setPivot(2.0F, 12.0F, 0.0F);
         this.leftLeg.addCuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, f + 0.1F);
      } else {
         this.head = new ModelPart(this, 0, 0);
         this.head.setTextureOffset(0, 0).addCuboid(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, f);
         this.head.setTextureOffset(24, 0).addCuboid(-1.0F, -3.0F, -6.0F, 2.0F, 4.0F, 2.0F, f);
         this.helmet = new ModelPart(this, 32, 0);
         this.helmet.addCuboid(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, f + 0.5F);
         this.hat = new ModelPart(this);
         this.hat.setTextureOffset(30, 47).addCuboid(-8.0F, -8.0F, -6.0F, 16.0F, 16.0F, 1.0F, f);
         this.hat.pitch = -1.5707964F;
         this.helmet.addChild(this.hat);
         this.torso = new ModelPart(this, 16, 20);
         this.torso.addCuboid(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F, f);
         this.torso.setTextureOffset(0, 38).addCuboid(-4.0F, 0.0F, -3.0F, 8.0F, 18.0F, 6.0F, f + 0.05F);
         this.rightArm = new ModelPart(this, 44, 22);
         this.rightArm.addCuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
         this.rightArm.setPivot(-5.0F, 2.0F, 0.0F);
         this.leftArm = new ModelPart(this, 44, 22);
         this.leftArm.mirror = true;
         this.leftArm.addCuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
         this.leftArm.setPivot(5.0F, 2.0F, 0.0F);
         this.rightLeg = new ModelPart(this, 0, 22);
         this.rightLeg.setPivot(-2.0F, 12.0F, 0.0F);
         this.rightLeg.addCuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
         this.leftLeg = new ModelPart(this, 0, 22);
         this.leftLeg.mirror = true;
         this.leftLeg.setPivot(2.0F, 12.0F, 0.0F);
         this.leftLeg.addCuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
      }

   }

   public void setAngles(T zombieEntity, float f, float g, float h, float i, float j) {
      super.setAngles((LivingEntity)zombieEntity, f, g, h, i, j);
      float k = MathHelper.sin(this.handSwingProgress * 3.1415927F);
      float l = MathHelper.sin((1.0F - (1.0F - this.handSwingProgress) * (1.0F - this.handSwingProgress)) * 3.1415927F);
      this.rightArm.roll = 0.0F;
      this.leftArm.roll = 0.0F;
      this.rightArm.yaw = -(0.1F - k * 0.6F);
      this.leftArm.yaw = 0.1F - k * 0.6F;
      float m = -3.1415927F / (zombieEntity.isAttacking() ? 1.5F : 2.25F);
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

   public void setHatVisible(boolean visible) {
      this.head.visible = visible;
      this.helmet.visible = visible;
      this.hat.visible = visible;
   }
}
