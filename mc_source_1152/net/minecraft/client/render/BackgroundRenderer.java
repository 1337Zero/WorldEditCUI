package net.minecraft.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

@Environment(EnvType.CLIENT)
public class BackgroundRenderer {
   private static float red;
   private static float green;
   private static float blue;
   private static int waterFogColor = -1;
   private static int nextWaterFogColor = -1;
   private static long lastWaterFogColorUpdateTime = -1L;

   public static void render(Camera camera, float f, ClientWorld clientWorld, int i, float g) {
      FluidState fluidState = camera.getSubmergedFluidState();
      int af;
      float ag;
      float ah;
      if (fluidState.matches(FluidTags.WATER)) {
         long l = Util.getMeasuringTimeMs();
         af = clientWorld.getBiome(new BlockPos(camera.getPos())).getWaterFogColor();
         if (lastWaterFogColorUpdateTime < 0L) {
            waterFogColor = af;
            nextWaterFogColor = af;
            lastWaterFogColorUpdateTime = l;
         }

         int k = waterFogColor >> 16 & 255;
         int m = waterFogColor >> 8 & 255;
         int n = waterFogColor & 255;
         int o = nextWaterFogColor >> 16 & 255;
         int p = nextWaterFogColor >> 8 & 255;
         int q = nextWaterFogColor & 255;
         float h = MathHelper.clamp((float)(l - lastWaterFogColorUpdateTime) / 5000.0F, 0.0F, 1.0F);
         float r = MathHelper.lerp(h, (float)o, (float)k);
         float s = MathHelper.lerp(h, (float)p, (float)m);
         float t = MathHelper.lerp(h, (float)q, (float)n);
         red = r / 255.0F;
         green = s / 255.0F;
         blue = t / 255.0F;
         if (waterFogColor != af) {
            waterFogColor = af;
            nextWaterFogColor = MathHelper.floor(r) << 16 | MathHelper.floor(s) << 8 | MathHelper.floor(t);
            lastWaterFogColorUpdateTime = l;
         }
      } else if (fluidState.matches(FluidTags.LAVA)) {
         red = 0.6F;
         green = 0.1F;
         blue = 0.0F;
         lastWaterFogColorUpdateTime = -1L;
      } else {
         float u = 0.25F + 0.75F * (float)i / 32.0F;
         u = 1.0F - (float)Math.pow((double)u, 0.25D);
         Vec3d vec3d = clientWorld.method_23777(camera.getBlockPos(), f);
         ag = (float)vec3d.x;
         ah = (float)vec3d.y;
         float x = (float)vec3d.z;
         Vec3d vec3d2 = clientWorld.getFogColor(f);
         red = (float)vec3d2.x;
         green = (float)vec3d2.y;
         blue = (float)vec3d2.z;
         float aa;
         float z;
         if (i >= 4) {
            aa = MathHelper.sin(clientWorld.getSkyAngleRadians(f)) > 0.0F ? -1.0F : 1.0F;
            Vector3f vector3f = new Vector3f(aa, 0.0F, 0.0F);
            z = camera.getHorizontalPlane().dot(vector3f);
            if (z < 0.0F) {
               z = 0.0F;
            }

            if (z > 0.0F) {
               float[] fs = clientWorld.dimension.getBackgroundColor(clientWorld.getSkyAngle(f), f);
               if (fs != null) {
                  z *= fs[3];
                  red = red * (1.0F - z) + fs[0] * z;
                  green = green * (1.0F - z) + fs[1] * z;
                  blue = blue * (1.0F - z) + fs[2] * z;
               }
            }
         }

         red += (ag - red) * u;
         green += (ah - green) * u;
         blue += (x - blue) * u;
         aa = clientWorld.getRainGradient(f);
         float ad;
         if (aa > 0.0F) {
            ad = 1.0F - aa * 0.5F;
            z = 1.0F - aa * 0.4F;
            red *= ad;
            green *= ad;
            blue *= z;
         }

         ad = clientWorld.getThunderGradient(f);
         if (ad > 0.0F) {
            z = 1.0F - ad * 0.5F;
            red *= z;
            green *= z;
            blue *= z;
         }

         lastWaterFogColorUpdateTime = -1L;
      }

      double d = camera.getPos().y * clientWorld.dimension.getHorizonShadingRatio();
      if (camera.getFocusedEntity() instanceof LivingEntity && ((LivingEntity)camera.getFocusedEntity()).hasStatusEffect(StatusEffects.BLINDNESS)) {
         af = ((LivingEntity)camera.getFocusedEntity()).getStatusEffect(StatusEffects.BLINDNESS).getDuration();
         if (af < 20) {
            d *= (double)(1.0F - (float)af / 20.0F);
         } else {
            d = 0.0D;
         }
      }

      if (d < 1.0D) {
         if (d < 0.0D) {
            d = 0.0D;
         }

         d *= d;
         red = (float)((double)red * d);
         green = (float)((double)green * d);
         blue = (float)((double)blue * d);
      }

      if (g > 0.0F) {
         red = red * (1.0F - g) + red * 0.7F * g;
         green = green * (1.0F - g) + green * 0.6F * g;
         blue = blue * (1.0F - g) + blue * 0.6F * g;
      }

      if (fluidState.matches(FluidTags.WATER)) {
         ag = 0.0F;
         if (camera.getFocusedEntity() instanceof ClientPlayerEntity) {
            ClientPlayerEntity clientPlayerEntity = (ClientPlayerEntity)camera.getFocusedEntity();
            ag = clientPlayerEntity.method_3140();
         }

         ah = Math.min(1.0F / red, Math.min(1.0F / green, 1.0F / blue));
         red = red * (1.0F - ag) + red * ah * ag;
         green = green * (1.0F - ag) + green * ah * ag;
         blue = blue * (1.0F - ag) + blue * ah * ag;
      } else if (camera.getFocusedEntity() instanceof LivingEntity && ((LivingEntity)camera.getFocusedEntity()).hasStatusEffect(StatusEffects.NIGHT_VISION)) {
         ag = GameRenderer.getNightVisionStrength((LivingEntity)camera.getFocusedEntity(), f);
         ah = Math.min(1.0F / red, Math.min(1.0F / green, 1.0F / blue));
         red = red * (1.0F - ag) + red * ah * ag;
         green = green * (1.0F - ag) + green * ah * ag;
         blue = blue * (1.0F - ag) + blue * ah * ag;
      }

      RenderSystem.clearColor(red, green, blue, 0.0F);
   }

   public static void method_23792() {
      RenderSystem.fogDensity(0.0F);
      RenderSystem.fogMode(GlStateManager.FogMode.EXP2);
   }

   public static void applyFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog) {
      FluidState fluidState = camera.getSubmergedFluidState();
      Entity entity = camera.getFocusedEntity();
      boolean bl = fluidState.getFluid() != Fluids.EMPTY;
      float o;
      if (bl) {
         o = 1.0F;
         if (fluidState.matches(FluidTags.WATER)) {
            o = 0.05F;
            if (entity instanceof ClientPlayerEntity) {
               ClientPlayerEntity clientPlayerEntity = (ClientPlayerEntity)entity;
               o -= clientPlayerEntity.method_3140() * clientPlayerEntity.method_3140() * 0.03F;
               Biome biome = clientPlayerEntity.world.getBiome(new BlockPos(clientPlayerEntity));
               if (biome == Biomes.SWAMP || biome == Biomes.SWAMP_HILLS) {
                  o += 0.005F;
               }
            }
         } else if (fluidState.matches(FluidTags.LAVA)) {
            o = 2.0F;
         }

         RenderSystem.fogDensity(o);
         RenderSystem.fogMode(GlStateManager.FogMode.EXP2);
      } else {
         float r;
         if (entity instanceof LivingEntity && ((LivingEntity)entity).hasStatusEffect(StatusEffects.BLINDNESS)) {
            int i = ((LivingEntity)entity).getStatusEffect(StatusEffects.BLINDNESS).getDuration();
            float g = MathHelper.lerp(Math.min(1.0F, (float)i / 20.0F), viewDistance, 5.0F);
            if (fogType == BackgroundRenderer.FogType.FOG_SKY) {
               o = 0.0F;
               r = g * 0.8F;
            } else {
               o = g * 0.25F;
               r = g;
            }
         } else if (thickFog) {
            o = viewDistance * 0.05F;
            r = Math.min(viewDistance, 192.0F) * 0.5F;
         } else if (fogType == BackgroundRenderer.FogType.FOG_SKY) {
            o = 0.0F;
            r = viewDistance;
         } else {
            o = viewDistance * 0.75F;
            r = viewDistance;
         }

         RenderSystem.fogStart(o);
         RenderSystem.fogEnd(r);
         RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
         RenderSystem.setupNvFogDistance();
      }

   }

   public static void setFogBlack() {
      RenderSystem.fog(2918, red, green, blue, 1.0F);
   }

   @Environment(EnvType.CLIENT)
   public static enum FogType {
      FOG_SKY,
      FOG_TERRAIN;
   }
}
