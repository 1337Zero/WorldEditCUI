package net.minecraft.client.render.model;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.ModelItemPropertyOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class BuiltinBakedModel implements BakedModel {
   private final ModelTransformation transformation;
   private final ModelItemPropertyOverrideList itemPropertyOverrides;
   private final Sprite sprite;
   private final boolean field_21862;

   public BuiltinBakedModel(ModelTransformation transformation, ModelItemPropertyOverrideList itemPropertyOverrides, Sprite sprite, boolean bl) {
      this.transformation = transformation;
      this.itemPropertyOverrides = itemPropertyOverrides;
      this.sprite = sprite;
      this.field_21862 = bl;
   }

   public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
      return Collections.emptyList();
   }

   public boolean useAmbientOcclusion() {
      return false;
   }

   public boolean hasDepth() {
      return true;
   }

   public boolean isSideLit() {
      return this.field_21862;
   }

   public boolean isBuiltin() {
      return true;
   }

   public Sprite getSprite() {
      return this.sprite;
   }

   public ModelTransformation getTransformation() {
      return this.transformation;
   }

   public ModelItemPropertyOverrideList getItemPropertyOverrides() {
      return this.itemPropertyOverrides;
   }
}
