package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class CrackParticle extends SpriteBillboardParticle {
   private final float field_17783;
   private final float field_17784;

   private CrackParticle(World world, double x, double y, double z, double d, double e, double f, ItemStack itemStack) {
      this(world, x, y, z, itemStack);
      this.velocityX *= 0.10000000149011612D;
      this.velocityY *= 0.10000000149011612D;
      this.velocityZ *= 0.10000000149011612D;
      this.velocityX += d;
      this.velocityY += e;
      this.velocityZ += f;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.TERRAIN_SHEET;
   }

   protected CrackParticle(World world, double d, double e, double f, ItemStack itemStack) {
      super(world, d, e, f, 0.0D, 0.0D, 0.0D);
      this.setSprite(MinecraftClient.getInstance().getItemRenderer().getHeldItemModel(itemStack, world, (LivingEntity)null).getSprite());
      this.gravityStrength = 1.0F;
      this.scale /= 2.0F;
      this.field_17783 = this.random.nextFloat() * 3.0F;
      this.field_17784 = this.random.nextFloat() * 3.0F;
   }

   protected float getMinU() {
      return this.sprite.getFrameU((double)((this.field_17783 + 1.0F) / 4.0F * 16.0F));
   }

   protected float getMaxU() {
      return this.sprite.getFrameU((double)(this.field_17783 / 4.0F * 16.0F));
   }

   protected float getMinV() {
      return this.sprite.getFrameV((double)(this.field_17784 / 4.0F * 16.0F));
   }

   protected float getMaxV() {
      return this.sprite.getFrameV((double)((this.field_17784 + 1.0F) / 4.0F * 16.0F));
   }

   @Environment(EnvType.CLIENT)
   public static class SnowballFactory implements ParticleFactory<DefaultParticleType> {
      public Particle createParticle(DefaultParticleType defaultParticleType, World world, double d, double e, double f, double g, double h, double i) {
         return new CrackParticle(world, d, e, f, new ItemStack(Items.SNOWBALL));
      }
   }

   @Environment(EnvType.CLIENT)
   public static class SlimeballFactory implements ParticleFactory<DefaultParticleType> {
      public Particle createParticle(DefaultParticleType defaultParticleType, World world, double d, double e, double f, double g, double h, double i) {
         return new CrackParticle(world, d, e, f, new ItemStack(Items.SLIME_BALL));
      }
   }

   @Environment(EnvType.CLIENT)
   public static class ItemFactory implements ParticleFactory<ItemStackParticleEffect> {
      public Particle createParticle(ItemStackParticleEffect itemStackParticleEffect, World world, double d, double e, double f, double g, double h, double i) {
         return new CrackParticle(world, d, e, f, g, h, i, itemStackParticleEffect.getItemStack());
      }
   }
}
