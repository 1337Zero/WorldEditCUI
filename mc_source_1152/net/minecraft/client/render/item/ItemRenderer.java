package net.minecraft.client.render.item;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloadListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class ItemRenderer implements SynchronousResourceReloadListener {
   public static final Identifier ENCHANTED_ITEM_GLINT = new Identifier("textures/misc/enchanted_item_glint.png");
   private static final Set<Item> WITHOUT_MODELS;
   public float zOffset;
   private final ItemModels models;
   private final TextureManager textureManager;
   private final ItemColors colorMap;

   public ItemRenderer(TextureManager manager, BakedModelManager bakery, ItemColors colorMap) {
      this.textureManager = manager;
      this.models = new ItemModels(bakery);
      Iterator var4 = Registry.ITEM.iterator();

      while(var4.hasNext()) {
         Item item = (Item)var4.next();
         if (!WITHOUT_MODELS.contains(item)) {
            this.models.putModel(item, new ModelIdentifier(Registry.ITEM.getId(item), "inventory"));
         }
      }

      this.colorMap = colorMap;
   }

   public ItemModels getModels() {
      return this.models;
   }

   private void renderBakedItemModel(BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices) {
      Random random = new Random();
      long l = 42L;
      Direction[] var10 = Direction.values();
      int var11 = var10.length;

      for(int var12 = 0; var12 < var11; ++var12) {
         Direction direction = var10[var12];
         random.setSeed(42L);
         this.renderBakedItemQuads(matrices, vertices, model.getQuads((BlockState)null, direction, random), stack, light, overlay);
      }

      random.setSeed(42L);
      this.renderBakedItemQuads(matrices, vertices, model.getQuads((BlockState)null, (Direction)null, random), stack, light, overlay);
   }

   public void renderItem(ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model) {
      if (!stack.isEmpty()) {
         matrices.push();
         boolean bl = renderMode == ModelTransformation.Mode.GUI;
         boolean bl2 = bl || renderMode == ModelTransformation.Mode.GROUND || renderMode == ModelTransformation.Mode.FIXED;
         if (stack.getItem() == Items.TRIDENT && bl2) {
            model = this.models.getModelManager().getModel(new ModelIdentifier("minecraft:trident#inventory"));
         }

         model.getTransformation().getTransformation(renderMode).apply(leftHanded, matrices);
         matrices.translate(-0.5D, -0.5D, -0.5D);
         if (!model.isBuiltin() && (stack.getItem() != Items.TRIDENT || bl2)) {
            RenderLayer renderLayer = RenderLayers.getItemLayer(stack);
            RenderLayer renderLayer3;
            if (bl && Objects.equals(renderLayer, TexturedRenderLayers.getEntityTranslucent())) {
               renderLayer3 = TexturedRenderLayers.getEntityTranslucentCull();
            } else {
               renderLayer3 = renderLayer;
            }

            VertexConsumer vertexConsumer = getArmorVertexConsumer(vertexConsumers, renderLayer3, true, stack.hasEnchantmentGlint());
            this.renderBakedItemModel(model, stack, light, overlay, matrices, vertexConsumer);
         } else {
            BuiltinModelItemRenderer.INSTANCE.render(stack, matrices, vertexConsumers, light, overlay);
         }

         matrices.pop();
      }
   }

   public static VertexConsumer getArmorVertexConsumer(VertexConsumerProvider vertexConsumers, RenderLayer layer, boolean solid, boolean glint) {
      return glint ? VertexConsumers.dual(vertexConsumers.getBuffer(solid ? RenderLayer.getGlint() : RenderLayer.getEntityGlint()), vertexConsumers.getBuffer(layer)) : vertexConsumers.getBuffer(layer);
   }

   private void renderBakedItemQuads(MatrixStack matrices, VertexConsumer vertices, List<BakedQuad> quads, ItemStack stack, int light, int overlay) {
      boolean bl = !stack.isEmpty();
      MatrixStack.Entry entry = matrices.peek();
      Iterator var9 = quads.iterator();

      while(var9.hasNext()) {
         BakedQuad bakedQuad = (BakedQuad)var9.next();
         int i = -1;
         if (bl && bakedQuad.hasColor()) {
            i = this.colorMap.getColorMultiplier(stack, bakedQuad.getColorIndex());
         }

         float f = (float)(i >> 16 & 255) / 255.0F;
         float g = (float)(i >> 8 & 255) / 255.0F;
         float h = (float)(i & 255) / 255.0F;
         vertices.quad(entry, bakedQuad, f, g, h, light, overlay);
      }

   }

   public BakedModel getHeldItemModel(ItemStack stack, @Nullable World world, @Nullable LivingEntity entity) {
      Item item = stack.getItem();
      BakedModel bakedModel2;
      if (item == Items.TRIDENT) {
         bakedModel2 = this.models.getModelManager().getModel(new ModelIdentifier("minecraft:trident_in_hand#inventory"));
      } else {
         bakedModel2 = this.models.getModel(stack);
      }

      return !item.hasPropertyGetters() ? bakedModel2 : this.getOverriddenModel(bakedModel2, stack, world, entity);
   }

   private BakedModel getOverriddenModel(BakedModel model, ItemStack stack, @Nullable World world, @Nullable LivingEntity entity) {
      BakedModel bakedModel = model.getItemPropertyOverrides().apply(model, stack, world, entity);
      return bakedModel == null ? this.models.getModelManager().getMissingModel() : bakedModel;
   }

   public void renderItem(ItemStack stack, ModelTransformation.Mode transformationType, int light, int overlay, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
      this.renderItem((LivingEntity)null, stack, transformationType, false, matrices, vertexConsumers, (World)null, light, overlay);
   }

   public void renderItem(@Nullable LivingEntity entity, ItemStack item, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, @Nullable World world, int light, int overlay) {
      if (!item.isEmpty()) {
         BakedModel bakedModel = this.getHeldItemModel(item, world, entity);
         this.renderItem(item, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, bakedModel);
      }
   }

   public void renderGuiItemIcon(ItemStack stack, int x, int y) {
      this.renderGuiItemModel(stack, x, y, this.getHeldItemModel(stack, (World)null, (LivingEntity)null));
   }

   protected void renderGuiItemModel(ItemStack stack, int x, int y, BakedModel model) {
      RenderSystem.pushMatrix();
      this.textureManager.bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
      this.textureManager.getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX).setFilter(false, false);
      RenderSystem.enableRescaleNormal();
      RenderSystem.enableAlphaTest();
      RenderSystem.defaultAlphaFunc();
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.translatef((float)x, (float)y, 100.0F + this.zOffset);
      RenderSystem.translatef(8.0F, 8.0F, 0.0F);
      RenderSystem.scalef(1.0F, -1.0F, 1.0F);
      RenderSystem.scalef(16.0F, 16.0F, 16.0F);
      MatrixStack matrixStack = new MatrixStack();
      VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
      boolean bl = !model.isSideLit();
      if (bl) {
         DiffuseLighting.disableGuiDepthLighting();
      }

      this.renderItem(stack, ModelTransformation.Mode.GUI, false, matrixStack, immediate, 15728880, OverlayTexture.DEFAULT_UV, model);
      immediate.draw();
      RenderSystem.enableDepthTest();
      if (bl) {
         DiffuseLighting.enableGuiDepthLighting();
      }

      RenderSystem.disableAlphaTest();
      RenderSystem.disableRescaleNormal();
      RenderSystem.popMatrix();
   }

   public void renderGuiItem(ItemStack stack, int x, int y) {
      this.renderGuiItem(MinecraftClient.getInstance().player, stack, x, y);
   }

   public void renderGuiItem(@Nullable LivingEntity entity, ItemStack itemStack, int x, int y) {
      if (!itemStack.isEmpty()) {
         this.zOffset += 50.0F;

         try {
            this.renderGuiItemModel(itemStack, x, y, this.getHeldItemModel(itemStack, (World)null, entity));
         } catch (Throwable var8) {
            CrashReport crashReport = CrashReport.create(var8, "Rendering item");
            CrashReportSection crashReportSection = crashReport.addElement("Item being rendered");
            crashReportSection.add("Item Type", () -> {
               return String.valueOf(itemStack.getItem());
            });
            crashReportSection.add("Item Damage", () -> {
               return String.valueOf(itemStack.getDamage());
            });
            crashReportSection.add("Item NBT", () -> {
               return String.valueOf(itemStack.getTag());
            });
            crashReportSection.add("Item Foil", () -> {
               return String.valueOf(itemStack.hasEnchantmentGlint());
            });
            throw new CrashException(crashReport);
         }

         this.zOffset -= 50.0F;
      }
   }

   public void renderGuiItemOverlay(TextRenderer fontRenderer, ItemStack stack, int x, int y) {
      this.renderGuiItemOverlay(fontRenderer, stack, x, y, (String)null);
   }

   public void renderGuiItemOverlay(TextRenderer fontRenderer, ItemStack stack, int x, int y, @Nullable String amountText) {
      if (!stack.isEmpty()) {
         MatrixStack matrixStack = new MatrixStack();
         if (stack.getCount() != 1 || amountText != null) {
            String string = amountText == null ? String.valueOf(stack.getCount()) : amountText;
            matrixStack.translate(0.0D, 0.0D, (double)(this.zOffset + 200.0F));
            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            fontRenderer.draw(string, (float)(x + 19 - 2 - fontRenderer.getStringWidth(string)), (float)(y + 6 + 3), 16777215, true, matrixStack.peek().getModel(), immediate, false, 0, 15728880);
            immediate.draw();
         }

         if (stack.isDamaged()) {
            RenderSystem.disableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.disableAlphaTest();
            RenderSystem.disableBlend();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            float f = (float)stack.getDamage();
            float g = (float)stack.getMaxDamage();
            float h = Math.max(0.0F, (g - f) / g);
            int i = Math.round(13.0F - f * 13.0F / g);
            int j = MathHelper.hsvToRgb(h / 3.0F, 1.0F, 1.0F);
            this.renderGuiQuad(bufferBuilder, x + 2, y + 13, 13, 2, 0, 0, 0, 255);
            this.renderGuiQuad(bufferBuilder, x + 2, y + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
            RenderSystem.enableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();
            RenderSystem.enableDepthTest();
         }

         ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
         float k = clientPlayerEntity == null ? 0.0F : clientPlayerEntity.getItemCooldownManager().getCooldownProgress(stack.getItem(), MinecraftClient.getInstance().getTickDelta());
         if (k > 0.0F) {
            RenderSystem.disableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            Tessellator tessellator2 = Tessellator.getInstance();
            BufferBuilder bufferBuilder2 = tessellator2.getBuffer();
            this.renderGuiQuad(bufferBuilder2, x, y + MathHelper.floor(16.0F * (1.0F - k)), 16, MathHelper.ceil(16.0F * k), 255, 255, 255, 127);
            RenderSystem.enableTexture();
            RenderSystem.enableDepthTest();
         }

      }
   }

   private void renderGuiQuad(BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
      buffer.begin(7, VertexFormats.POSITION_COLOR);
      buffer.vertex((double)(x + 0), (double)(y + 0), 0.0D).color(red, green, blue, alpha).next();
      buffer.vertex((double)(x + 0), (double)(y + height), 0.0D).color(red, green, blue, alpha).next();
      buffer.vertex((double)(x + width), (double)(y + height), 0.0D).color(red, green, blue, alpha).next();
      buffer.vertex((double)(x + width), (double)(y + 0), 0.0D).color(red, green, blue, alpha).next();
      Tessellator.getInstance().draw();
   }

   public void apply(ResourceManager manager) {
      this.models.reloadModels();
   }

   static {
      WITHOUT_MODELS = Sets.newHashSet(new Item[]{Items.AIR});
   }
}
