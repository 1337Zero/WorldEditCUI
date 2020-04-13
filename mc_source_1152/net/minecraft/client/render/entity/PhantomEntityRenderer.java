package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.PhantomEyesFeatureRenderer;
import net.minecraft.client.render.entity.model.PhantomEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class PhantomEntityRenderer extends MobEntityRenderer<PhantomEntity, PhantomEntityModel<PhantomEntity>> {
   private static final Identifier SKIN = new Identifier("textures/entity/phantom.png");

   public PhantomEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
      super(entityRenderDispatcher, new PhantomEntityModel(), 0.75F);
      this.addFeature(new PhantomEyesFeatureRenderer(this));
   }

   public Identifier getTexture(PhantomEntity phantomEntity) {
      return SKIN;
   }

   protected void scale(PhantomEntity phantomEntity, MatrixStack matrixStack, float f) {
      int i = phantomEntity.getPhantomSize();
      float g = 1.0F + 0.15F * (float)i;
      matrixStack.scale(g, g, g);
      matrixStack.translate(0.0D, 1.3125D, 0.1875D);
   }

   protected void setupTransforms(PhantomEntity phantomEntity, MatrixStack matrixStack, float f, float g, float h) {
      super.setupTransforms(phantomEntity, matrixStack, f, g, h);
      matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(phantomEntity.pitch));
   }
}
