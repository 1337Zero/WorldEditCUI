package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class CurrentDownParticle extends SpriteBillboardParticle {
   private float field_3897;

   private CurrentDownParticle(World world, double x, double y, double z) {
      super(world, x, y, z);
      this.maxAge = (int)(Math.random() * 60.0D) + 30;
      this.collidesWithWorld = false;
      this.velocityX = 0.0D;
      this.velocityY = -0.05D;
      this.velocityZ = 0.0D;
      this.setBoundingBoxSpacing(0.02F, 0.02F);
      this.scale *= this.random.nextFloat() * 0.6F + 0.2F;
      this.gravityStrength = 0.002F;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.age++ >= this.maxAge) {
         this.markDead();
      } else {
         float f = 0.6F;
         this.velocityX += (double)(0.6F * MathHelper.cos(this.field_3897));
         this.velocityZ += (double)(0.6F * MathHelper.sin(this.field_3897));
         this.velocityX *= 0.07D;
         this.velocityZ *= 0.07D;
         this.move(this.velocityX, this.velocityY, this.velocityZ);
         if (!this.world.getFluidState(new BlockPos(this.x, this.y, this.z)).matches(FluidTags.WATER) || this.onGround) {
            this.markDead();
         }

         this.field_3897 = (float)((double)this.field_3897 + 0.08D);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider field_17890;

      public Factory(SpriteProvider spriteProvider) {
         this.field_17890 = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, World world, double d, double e, double f, double g, double h, double i) {
         CurrentDownParticle currentDownParticle = new CurrentDownParticle(world, d, e, f);
         currentDownParticle.setSprite(this.field_17890);
         return currentDownParticle;
      }
   }
}
