package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class FireSmokeParticle extends SpriteBillboardParticle {
   private final SpriteProvider field_17868;

   protected FireSmokeParticle(World world, double x, double y, double z, double d, double e, double f, float g, SpriteProvider spriteProvider) {
      super(world, x, y, z, 0.0D, 0.0D, 0.0D);
      this.field_17868 = spriteProvider;
      this.velocityX *= 0.10000000149011612D;
      this.velocityY *= 0.10000000149011612D;
      this.velocityZ *= 0.10000000149011612D;
      this.velocityX += d;
      this.velocityY += e;
      this.velocityZ += f;
      float h = (float)(Math.random() * 0.30000001192092896D);
      this.colorRed = h;
      this.colorGreen = h;
      this.colorBlue = h;
      this.scale *= 0.75F * g;
      this.maxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
      this.maxAge = (int)((float)this.maxAge * g);
      this.maxAge = Math.max(this.maxAge, 1);
      this.setSpriteForAge(spriteProvider);
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public float getSize(float tickDelta) {
      return this.scale * MathHelper.clamp(((float)this.age + tickDelta) / (float)this.maxAge * 32.0F, 0.0F, 1.0F);
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.age++ >= this.maxAge) {
         this.markDead();
      } else {
         this.setSpriteForAge(this.field_17868);
         this.velocityY += 0.004D;
         this.move(this.velocityX, this.velocityY, this.velocityZ);
         if (this.y == this.prevPosY) {
            this.velocityX *= 1.1D;
            this.velocityZ *= 1.1D;
         }

         this.velocityX *= 0.9599999785423279D;
         this.velocityY *= 0.9599999785423279D;
         this.velocityZ *= 0.9599999785423279D;
         if (this.onGround) {
            this.velocityX *= 0.699999988079071D;
            this.velocityZ *= 0.699999988079071D;
         }

      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider field_17869;

      public Factory(SpriteProvider spriteProvider) {
         this.field_17869 = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, World world, double d, double e, double f, double g, double h, double i) {
         return new FireSmokeParticle(world, d, e, f, g, h, i, 1.0F, this.field_17869);
      }
   }
}
