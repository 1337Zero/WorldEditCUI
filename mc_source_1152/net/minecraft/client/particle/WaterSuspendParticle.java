package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class WaterSuspendParticle extends SpriteBillboardParticle {
   private WaterSuspendParticle(World world, double x, double y, double z) {
      super(world, x, y - 0.125D, z);
      this.colorRed = 0.4F;
      this.colorGreen = 0.4F;
      this.colorBlue = 0.7F;
      this.setBoundingBoxSpacing(0.01F, 0.01F);
      this.scale *= this.random.nextFloat() * 0.6F + 0.2F;
      this.maxAge = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.maxAge-- <= 0) {
         this.markDead();
      } else {
         this.move(this.velocityX, this.velocityY, this.velocityZ);
         if (!this.world.getFluidState(new BlockPos(this.x, this.y, this.z)).matches(FluidTags.WATER)) {
            this.markDead();
         }

      }
   }

   @Environment(EnvType.CLIENT)
   public static class UnderwaterFactory implements ParticleFactory<DefaultParticleType> {
      private final SpriteProvider spriteProvider;

      public UnderwaterFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(DefaultParticleType defaultParticleType, World world, double d, double e, double f, double g, double h, double i) {
         WaterSuspendParticle waterSuspendParticle = new WaterSuspendParticle(world, d, e, f);
         waterSuspendParticle.setSprite(this.spriteProvider);
         return waterSuspendParticle;
      }
   }
}
