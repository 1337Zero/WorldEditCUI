package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemConvertible;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class BarrierParticle extends SpriteBillboardParticle {
   private BarrierParticle(World world, double x, double y, double z, ItemConvertible itemConvertible) {
      super(world, x, y, z);
      this.setSprite(MinecraftClient.getInstance().getItemRenderer().getModels().getSprite(itemConvertible));
      this.gravityStrength = 0.0F;
      this.maxAge = 80;
      this.collidesWithWorld = false;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.TERRAIN_SHEET;
   }

   public float getSize(float tickDelta) {
      return 0.5F;
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory<DefaultParticleType> {
      public Particle createParticle(DefaultParticleType defaultParticleType, World world, double d, double e, double f, double g, double h, double i) {
         return new BarrierParticle(world, d, e, f, Blocks.BARRIER.asItem());
      }
   }
}
