package net.minecraft.client.render.entity;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public abstract class LivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {
   private static final Logger LOGGER = LogManager.getLogger();
   protected M model;
   protected final List<FeatureRenderer<T, M>> features = Lists.newArrayList();

   public LivingEntityRenderer(EntityRenderDispatcher entityRenderDispatcher, M entityModel, float f) {
      super(entityRenderDispatcher);
      this.model = entityModel;
      this.shadowSize = f;
   }

   protected final boolean addFeature(FeatureRenderer<T, M> featureRenderer) {
      return this.features.add(featureRenderer);
   }

   public M getModel() {
      return this.model;
   }

   public void render(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
      matrixStack.push();
      this.model.handSwingProgress = this.getHandSwingProgress(livingEntity, g);
      this.model.riding = livingEntity.hasVehicle();
      this.model.child = livingEntity.isBaby();
      float h = MathHelper.lerpAngleDegrees(g, livingEntity.prevBodyYaw, livingEntity.bodyYaw);
      float j = MathHelper.lerpAngleDegrees(g, livingEntity.prevHeadYaw, livingEntity.headYaw);
      float k = j - h;
      float o;
      if (livingEntity.hasVehicle() && livingEntity.getVehicle() instanceof LivingEntity) {
         LivingEntity livingEntity2 = (LivingEntity)livingEntity.getVehicle();
         h = MathHelper.lerpAngleDegrees(g, livingEntity2.prevBodyYaw, livingEntity2.bodyYaw);
         k = j - h;
         o = MathHelper.wrapDegrees(k);
         if (o < -85.0F) {
            o = -85.0F;
         }

         if (o >= 85.0F) {
            o = 85.0F;
         }

         h = j - o;
         if (o * o > 2500.0F) {
            h += o * 0.2F;
         }

         k = j - h;
      }

      float m = MathHelper.lerp(g, livingEntity.prevPitch, livingEntity.pitch);
      float p;
      if (livingEntity.getPose() == EntityPose.SLEEPING) {
         Direction direction = livingEntity.getSleepingDirection();
         if (direction != null) {
            p = livingEntity.getEyeHeight(EntityPose.STANDING) - 0.1F;
            matrixStack.translate((double)((float)(-direction.getOffsetX()) * p), 0.0D, (double)((float)(-direction.getOffsetZ()) * p));
         }
      }

      o = this.getCustomAngle(livingEntity, g);
      this.setupTransforms(livingEntity, matrixStack, o, h, g);
      matrixStack.scale(-1.0F, -1.0F, 1.0F);
      this.scale(livingEntity, matrixStack, g);
      matrixStack.translate(0.0D, -1.5010000467300415D, 0.0D);
      p = 0.0F;
      float q = 0.0F;
      if (!livingEntity.hasVehicle() && livingEntity.isAlive()) {
         p = MathHelper.lerp(g, livingEntity.lastLimbDistance, livingEntity.limbDistance);
         q = livingEntity.limbAngle - livingEntity.limbDistance * (1.0F - g);
         if (livingEntity.isBaby()) {
            q *= 3.0F;
         }

         if (p > 1.0F) {
            p = 1.0F;
         }
      }

      this.model.animateModel(livingEntity, q, p, g);
      this.model.setAngles(livingEntity, q, p, o, k, m);
      boolean bl = this.method_4056(livingEntity);
      boolean bl2 = !bl && !livingEntity.canSeePlayer(MinecraftClient.getInstance().player);
      RenderLayer renderLayer = this.method_24302(livingEntity, bl, bl2);
      if (renderLayer != null) {
         VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
         int r = getOverlay(livingEntity, this.getWhiteOverlayProgress(livingEntity, g));
         this.model.render(matrixStack, vertexConsumer, i, r, 1.0F, 1.0F, 1.0F, bl2 ? 0.15F : 1.0F);
      }

      if (!livingEntity.isSpectator()) {
         Iterator var21 = this.features.iterator();

         while(var21.hasNext()) {
            FeatureRenderer<T, M> featureRenderer = (FeatureRenderer)var21.next();
            featureRenderer.render(matrixStack, vertexConsumerProvider, i, livingEntity, q, p, g, o, k, m);
         }
      }

      matrixStack.pop();
      super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
   }

   @Nullable
   protected RenderLayer method_24302(T livingEntity, boolean bl, boolean bl2) {
      Identifier identifier = this.getTexture(livingEntity);
      if (bl2) {
         return RenderLayer.getEntityTranslucent(identifier);
      } else if (bl) {
         return this.model.getLayer(identifier);
      } else {
         return livingEntity.isGlowing() ? RenderLayer.getOutline(identifier) : null;
      }
   }

   public static int getOverlay(LivingEntity entity, float whiteOverlayProgress) {
      return OverlayTexture.packUv(OverlayTexture.getU(whiteOverlayProgress), OverlayTexture.getV(entity.hurtTime > 0 || entity.deathTime > 0));
   }

   protected boolean method_4056(T livingEntity) {
      return !livingEntity.isInvisible();
   }

   private static float getYaw(Direction direction) {
      switch(direction) {
      case SOUTH:
         return 90.0F;
      case WEST:
         return 0.0F;
      case NORTH:
         return 270.0F;
      case EAST:
         return 180.0F;
      default:
         return 0.0F;
      }
   }

   protected void setupTransforms(T entity, MatrixStack matrixStack, float f, float g, float h) {
      EntityPose entityPose = entity.getPose();
      if (entityPose != EntityPose.SLEEPING) {
         matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0F - g));
      }

      if (entity.deathTime > 0) {
         float i = ((float)entity.deathTime + h - 1.0F) / 20.0F * 1.6F;
         i = MathHelper.sqrt(i);
         if (i > 1.0F) {
            i = 1.0F;
         }

         matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(i * this.getLyingAngle(entity)));
      } else if (entity.isUsingRiptide()) {
         matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-90.0F - entity.pitch));
         matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(((float)entity.age + h) * -75.0F));
      } else if (entityPose == EntityPose.SLEEPING) {
         Direction direction = entity.getSleepingDirection();
         float j = direction != null ? getYaw(direction) : g;
         matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(j));
         matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(this.getLyingAngle(entity)));
         matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(270.0F));
      } else if (entity.hasCustomName() || entity instanceof PlayerEntity) {
         String string = Formatting.strip(entity.getName().getString());
         if (("Dinnerbone".equals(string) || "Grumm".equals(string)) && (!(entity instanceof PlayerEntity) || ((PlayerEntity)entity).isPartVisible(PlayerModelPart.CAPE))) {
            matrixStack.translate(0.0D, (double)(entity.getHeight() + 0.1F), 0.0D);
            matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(180.0F));
         }
      }

   }

   protected float getHandSwingProgress(T entity, float tickDelta) {
      return entity.getHandSwingProgress(tickDelta);
   }

   protected float getCustomAngle(T entity, float tickDelta) {
      return (float)entity.age + tickDelta;
   }

   protected float getLyingAngle(T livingEntity) {
      return 90.0F;
   }

   protected float getWhiteOverlayProgress(T entity, float tickDelta) {
      return 0.0F;
   }

   protected void scale(T entity, MatrixStack matrixStack, float f) {
   }

   protected boolean hasLabel(T livingEntity) {
      double d = this.renderManager.getSquaredDistanceToCamera(livingEntity);
      float f = livingEntity.isSneaky() ? 32.0F : 64.0F;
      if (d >= (double)(f * f)) {
         return false;
      } else {
         MinecraftClient minecraftClient = MinecraftClient.getInstance();
         ClientPlayerEntity clientPlayerEntity = minecraftClient.player;
         boolean bl = !livingEntity.canSeePlayer(clientPlayerEntity);
         if (livingEntity != clientPlayerEntity) {
            AbstractTeam abstractTeam = livingEntity.getScoreboardTeam();
            AbstractTeam abstractTeam2 = clientPlayerEntity.getScoreboardTeam();
            if (abstractTeam != null) {
               AbstractTeam.VisibilityRule visibilityRule = abstractTeam.getNameTagVisibilityRule();
               switch(visibilityRule) {
               case ALWAYS:
                  return bl;
               case NEVER:
                  return false;
               case HIDE_FOR_OTHER_TEAMS:
                  return abstractTeam2 == null ? bl : abstractTeam.isEqual(abstractTeam2) && (abstractTeam.shouldShowFriendlyInvisibles() || bl);
               case HIDE_FOR_OWN_TEAM:
                  return abstractTeam2 == null ? bl : !abstractTeam.isEqual(abstractTeam2) && bl;
               default:
                  return true;
               }
            }
         }

         return MinecraftClient.isHudEnabled() && livingEntity != minecraftClient.getCameraEntity() && bl && !livingEntity.hasPassengers();
      }
   }
}
