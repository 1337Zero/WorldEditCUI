package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class PortalParticle extends SpriteBillboardParticle {
   private final double startX;
   private final double startY;
   private final double startZ;

   private PortalParticle(World world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      super(world, x, y, z);
      this.velocityX = velocityX;
      this.velocityY = velocityY;
      this.velocityZ = velocityZ;
      this.x = x;
      this.y = y;
      this.z = z;
      this.startX = this.x;
      this.startY = this.y;
      this.startZ = this.z;
      this.scale = 0.1F * (this.random.nextFloat() * 0.2F + 0.5F);
      float f = this.random.nextFloat() * 0.6F + 0.4F;
      this.colorRed = f * 0.9F;
      this.colorGreen = f * 0.3F;
      this.colorBlue = f;
      this.maxAge = (int)(Math.random() * 10.0D) + 40;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public void move(double dx, double dy, double dz) {
      this.setBoundingBox(this.getBoundingBox().offset(dx, dy, dz));
      this.repositionFromBoundingBox();
   }

   public float getSize(float tickDelta) {
      float f = ((float)this.age + tickDelta) / (float)this.maxAge;
      f = 1.0F - f;
      f *= f;
      f = 1.0F - f;
      return this.scale * f;
   }

   public int getColorMultiplier(float tint) {
      int i = super.getColorMultiplier(tint);
      float f = (float)this.age / (float)this.maxAge;
      f *= f;
      f *= f;
      int j = i & 255;
      int k = i >> 16 & 255;
      k += (int)(f * 15.0F * 16.0F);
      if (k > 240) {
         k = 240;
      }

      return j | k << 16;
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.age++ >= this.maxAge) {
         this.markDead();
      } else {
         float f = (float)this.age / (float)this.maxAge;
         float g = f;
         f = -f + f * f * 2.0F;
         f = 1.0F - f;
         this.x = this.startX + this.velocityX * (double)f;
         this.y = this.startY + this.velocityY * (double)f + (double)(1.0F - g);
         this.z = this.startZ + this.velocityZ * (double)f;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider field_17865;

      public Factory(SpriteProvider spriteProvider) {
         this.field_17865 = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, World world, double d, double e, double f, double g, double h, double i) {
         PortalParticle portalParticle = new PortalParticle(world, d, e, f, g, h, i);
         portalParticle.setSprite(this.field_17865);
         return portalParticle;
      }
   }
}
