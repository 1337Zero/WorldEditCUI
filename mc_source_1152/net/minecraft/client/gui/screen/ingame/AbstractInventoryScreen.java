package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collection;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.container.Container;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public abstract class AbstractInventoryScreen<T extends Container> extends AbstractContainerScreen<T> {
   protected boolean offsetGuiForEffects;

   public AbstractInventoryScreen(T container, PlayerInventory playerInventory, Text text) {
      super(container, playerInventory, text);
   }

   protected void init() {
      super.init();
      this.applyStatusEffectOffset();
   }

   protected void applyStatusEffectOffset() {
      if (this.minecraft.player.getStatusEffects().isEmpty()) {
         this.x = (this.width - this.containerWidth) / 2;
         this.offsetGuiForEffects = false;
      } else {
         this.x = 160 + (this.width - this.containerWidth - 200) / 2;
         this.offsetGuiForEffects = true;
      }

   }

   public void render(int mouseX, int mouseY, float delta) {
      super.render(mouseX, mouseY, delta);
      if (this.offsetGuiForEffects) {
         this.drawStatusEffects();
      }

   }

   private void drawStatusEffects() {
      int i = this.x - 124;
      Collection<StatusEffectInstance> collection = this.minecraft.player.getStatusEffects();
      if (!collection.isEmpty()) {
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         int j = 33;
         if (collection.size() > 5) {
            j = 132 / (collection.size() - 1);
         }

         Iterable<StatusEffectInstance> iterable = Ordering.natural().sortedCopy(collection);
         this.drawStatusEffectBackgrounds(i, j, iterable);
         this.drawStatusEffectSprites(i, j, iterable);
         this.drawStatusEffectDescriptions(i, j, iterable);
      }
   }

   private void drawStatusEffectBackgrounds(int x, int yIncrement, Iterable<StatusEffectInstance> effects) {
      this.minecraft.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
      int i = this.y;

      for(Iterator var5 = effects.iterator(); var5.hasNext(); i += yIncrement) {
         StatusEffectInstance statusEffectInstance = (StatusEffectInstance)var5.next();
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         this.blit(x, i, 0, 166, 140, 32);
      }

   }

   private void drawStatusEffectSprites(int x, int yIncrement, Iterable<StatusEffectInstance> effects) {
      StatusEffectSpriteManager statusEffectSpriteManager = this.minecraft.getStatusEffectSpriteManager();
      int i = this.y;

      for(Iterator var6 = effects.iterator(); var6.hasNext(); i += yIncrement) {
         StatusEffectInstance statusEffectInstance = (StatusEffectInstance)var6.next();
         StatusEffect statusEffect = statusEffectInstance.getEffectType();
         Sprite sprite = statusEffectSpriteManager.getSprite(statusEffect);
         this.minecraft.getTextureManager().bindTexture(sprite.getAtlas().getId());
         blit(x + 6, i + 7, this.getBlitOffset(), 18, 18, sprite);
      }

   }

   private void drawStatusEffectDescriptions(int x, int yIncrement, Iterable<StatusEffectInstance> effects) {
      int i = this.y;

      for(Iterator var5 = effects.iterator(); var5.hasNext(); i += yIncrement) {
         StatusEffectInstance statusEffectInstance = (StatusEffectInstance)var5.next();
         String string = I18n.translate(statusEffectInstance.getEffectType().getTranslationKey());
         if (statusEffectInstance.getAmplifier() >= 1 && statusEffectInstance.getAmplifier() <= 9) {
            string = string + ' ' + I18n.translate("enchantment.level." + (statusEffectInstance.getAmplifier() + 1));
         }

         this.font.drawWithShadow(string, (float)(x + 10 + 18), (float)(i + 6), 16777215);
         String string2 = StatusEffectUtil.durationToString(statusEffectInstance, 1.0F);
         this.font.drawWithShadow(string2, (float)(x + 10 + 18), (float)(i + 6 + 10), 8355711);
      }

   }
}
