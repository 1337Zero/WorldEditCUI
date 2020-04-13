package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;

@Environment(EnvType.CLIENT)
public abstract class EntityRenderer<T extends Entity> {
   protected final EntityRenderDispatcher renderManager;
   protected float shadowSize;
   protected float shadowDarkness = 1.0F;

   protected EntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
      this.renderManager = entityRenderDispatcher;
   }

   public final int getLight(T entity, float tickDelta) {
      return LightmapTextureManager.pack(this.getBlockLight(entity, tickDelta), entity.world.getLightLevel(LightType.SKY, new BlockPos(entity.getCameraPosVec(tickDelta))));
   }

   protected int getBlockLight(T entity, float tickDelta) {
      return entity.isOnFire() ? 15 : entity.world.getLightLevel(LightType.BLOCK, new BlockPos(entity.getCameraPosVec(tickDelta)));
   }

   public boolean shouldRender(T entity, Frustum visibleRegion, double cameraX, double cameraY, double cameraZ) {
      if (!entity.shouldRender(cameraX, cameraY, cameraZ)) {
         return false;
      } else if (entity.ignoreCameraFrustum) {
         return true;
      } else {
         Box box = entity.getVisibilityBoundingBox().expand(0.5D);
         if (box.isValid() || box.getAverageSideLength() == 0.0D) {
            box = new Box(entity.getX() - 2.0D, entity.getY() - 2.0D, entity.getZ() - 2.0D, entity.getX() + 2.0D, entity.getY() + 2.0D, entity.getZ() + 2.0D);
         }

         return visibleRegion.isVisible(box);
      }
   }

   public Vec3d getPositionOffset(T entity, float tickDelta) {
      return Vec3d.ZERO;
   }

   public void render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      if (this.hasLabel(entity)) {
         this.renderLabelIfPresent(entity, entity.getDisplayName().asFormattedString(), matrices, vertexConsumers, light);
      }
   }

   protected boolean hasLabel(T entity) {
      return entity.shouldRenderName() && entity.hasCustomName();
   }

   public abstract Identifier getTexture(T entity);

   public TextRenderer getFontRenderer() {
      return this.renderManager.getTextRenderer();
   }

   protected void renderLabelIfPresent(T entity, String string, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
      double d = this.renderManager.getSquaredDistanceToCamera(entity);
      if (d <= 4096.0D) {
         boolean bl = !entity.isSneaky();
         float f = entity.getHeight() + 0.5F;
         int j = "deadmau5".equals(string) ? -10 : 0;
         matrixStack.push();
         matrixStack.translate(0.0D, (double)f, 0.0D);
         matrixStack.multiply(this.renderManager.getRotation());
         matrixStack.scale(-0.025F, -0.025F, 0.025F);
         Matrix4f matrix4f = matrixStack.peek().getModel();
         float g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
         int k = (int)(g * 255.0F) << 24;
         TextRenderer textRenderer = this.getFontRenderer();
         float h = (float)(-textRenderer.getStringWidth(string) / 2);
         textRenderer.draw(string, h, (float)j, 553648127, false, matrix4f, vertexConsumerProvider, bl, k, i);
         if (bl) {
            textRenderer.draw(string, h, (float)j, -1, false, matrix4f, vertexConsumerProvider, false, 0, i);
         }

         matrixStack.pop();
      }
   }

   public EntityRenderDispatcher getRenderManager() {
      return this.renderManager;
   }
}
