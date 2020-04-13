package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class SpitParticle extends ExplosionSmokeParticle {
   private SpitParticle(World world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
      super(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider);
      this.gravityStrength = 0.5F;
   }

   public void tick() {
      super.tick();
      this.velocityY -= 0.004D + 0.04D * (double)this.gravityStrength;
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, World world, double d, double e, double f, double g, double h, double i) {
         return new SpitParticle(world, d, e, f, g, h, i, this.spriteProvider);
      }
   }
}
