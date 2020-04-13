package net.minecraft.client.gui.screen.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.container.MerchantContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.packet.SelectVillagerTradeC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TraderOfferList;
import net.minecraft.village.VillagerData;

@Environment(EnvType.CLIENT)
public class MerchantScreen extends AbstractContainerScreen<MerchantContainer> {
   private static final Identifier TEXTURE = new Identifier("textures/gui/container/villager2.png");
   private int selectedIndex;
   private final MerchantScreen.WidgetButtonPage[] offers = new MerchantScreen.WidgetButtonPage[7];
   private int indexStartOffset;
   private boolean scrolling;

   public MerchantScreen(MerchantContainer container, PlayerInventory inventory, Text title) {
      super(container, inventory, title);
      this.containerWidth = 276;
   }

   private void syncRecipeIndex() {
      ((MerchantContainer)this.container).setRecipeIndex(this.selectedIndex);
      ((MerchantContainer)this.container).switchTo(this.selectedIndex);
      this.minecraft.getNetworkHandler().sendPacket(new SelectVillagerTradeC2SPacket(this.selectedIndex));
   }

   protected void init() {
      super.init();
      int i = (this.width - this.containerWidth) / 2;
      int j = (this.height - this.containerHeight) / 2;
      int k = j + 16 + 2;

      for(int l = 0; l < 7; ++l) {
         this.offers[l] = (MerchantScreen.WidgetButtonPage)this.addButton(new MerchantScreen.WidgetButtonPage(i + 5, k, l, (buttonWidget) -> {
            if (buttonWidget instanceof MerchantScreen.WidgetButtonPage) {
               this.selectedIndex = ((MerchantScreen.WidgetButtonPage)buttonWidget).getIndex() + this.indexStartOffset;
               this.syncRecipeIndex();
            }

         }));
         k += 20;
      }

   }

   protected void drawForeground(int mouseX, int mouseY) {
      int i = ((MerchantContainer)this.container).getLevelProgress();
      int j = this.containerHeight - 94;
      String string4;
      if (i > 0 && i <= 5 && ((MerchantContainer)this.container).isLevelled()) {
         string4 = this.title.asFormattedString();
         String string2 = "- " + I18n.translate("merchant.level." + i);
         int k = this.font.getStringWidth(string4);
         int l = this.font.getStringWidth(string2);
         int m = k + l + 3;
         int n = 49 + this.containerWidth / 2 - m / 2;
         this.font.draw(string4, (float)n, 6.0F, 4210752);
         this.font.draw(this.playerInventory.getDisplayName().asFormattedString(), 107.0F, (float)j, 4210752);
         this.font.draw(string2, (float)(n + k + 3), 6.0F, 4210752);
      } else {
         string4 = this.title.asFormattedString();
         this.font.draw(string4, (float)(49 + this.containerWidth / 2 - this.font.getStringWidth(string4) / 2), 6.0F, 4210752);
         this.font.draw(this.playerInventory.getDisplayName().asFormattedString(), 107.0F, (float)j, 4210752);
      }

      string4 = I18n.translate("merchant.trades");
      int o = this.font.getStringWidth(string4);
      this.font.draw(string4, (float)(5 - o / 2 + 48), 6.0F, 4210752);
   }

   protected void drawBackground(float delta, int mouseX, int mouseY) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.minecraft.getTextureManager().bindTexture(TEXTURE);
      int i = (this.width - this.containerWidth) / 2;
      int j = (this.height - this.containerHeight) / 2;
      blit(i, j, this.getBlitOffset(), 0.0F, 0.0F, this.containerWidth, this.containerHeight, 256, 512);
      TraderOfferList traderOfferList = ((MerchantContainer)this.container).getRecipes();
      if (!traderOfferList.isEmpty()) {
         int k = this.selectedIndex;
         if (k < 0 || k >= traderOfferList.size()) {
            return;
         }

         TradeOffer tradeOffer = (TradeOffer)traderOfferList.get(k);
         if (tradeOffer.isDisabled()) {
            this.minecraft.getTextureManager().bindTexture(TEXTURE);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            blit(this.x + 83 + 99, this.y + 35, this.getBlitOffset(), 311.0F, 0.0F, 28, 21, 256, 512);
         }
      }

   }

   private void drawLevelInfo(int i, int j, TradeOffer tradeOffer) {
      this.minecraft.getTextureManager().bindTexture(TEXTURE);
      int k = ((MerchantContainer)this.container).getLevelProgress();
      int l = ((MerchantContainer)this.container).getExperience();
      if (k < 5) {
         blit(i + 136, j + 16, this.getBlitOffset(), 0.0F, 186.0F, 102, 5, 256, 512);
         int m = VillagerData.getLowerLevelExperience(k);
         if (l >= m && VillagerData.canLevelUp(k)) {
            int n = true;
            float f = (float)(100 / (VillagerData.getUpperLevelExperience(k) - m));
            int o = Math.min(MathHelper.floor(f * (float)(l - m)), 100);
            blit(i + 136, j + 16, this.getBlitOffset(), 0.0F, 191.0F, o + 1, 5, 256, 512);
            int p = ((MerchantContainer)this.container).getTraderRewardedExperience();
            if (p > 0) {
               int q = Math.min(MathHelper.floor((float)p * f), 100 - o);
               blit(i + 136 + o + 1, j + 16 + 1, this.getBlitOffset(), 2.0F, 182.0F, q, 3, 256, 512);
            }

         }
      }
   }

   private void method_20221(int i, int j, TraderOfferList traderOfferList) {
      int k = traderOfferList.size() + 1 - 7;
      if (k > 1) {
         int l = 139 - (27 + (k - 1) * 139 / k);
         int m = 1 + l / k + 139 / k;
         int n = true;
         int o = Math.min(113, this.indexStartOffset * m);
         if (this.indexStartOffset == k - 1) {
            o = 113;
         }

         blit(i + 94, j + 18 + o, this.getBlitOffset(), 0.0F, 199.0F, 6, 27, 256, 512);
      } else {
         blit(i + 94, j + 18, this.getBlitOffset(), 6.0F, 199.0F, 6, 27, 256, 512);
      }

   }

   public void render(int mouseX, int mouseY, float delta) {
      this.renderBackground();
      super.render(mouseX, mouseY, delta);
      TraderOfferList traderOfferList = ((MerchantContainer)this.container).getRecipes();
      if (!traderOfferList.isEmpty()) {
         int i = (this.width - this.containerWidth) / 2;
         int j = (this.height - this.containerHeight) / 2;
         int k = j + 16 + 1;
         int l = i + 5 + 5;
         RenderSystem.pushMatrix();
         RenderSystem.enableRescaleNormal();
         this.minecraft.getTextureManager().bindTexture(TEXTURE);
         this.method_20221(i, j, traderOfferList);
         int m = 0;
         Iterator var10 = traderOfferList.iterator();

         while(true) {
            TradeOffer tradeOffer;
            while(var10.hasNext()) {
               tradeOffer = (TradeOffer)var10.next();
               if (this.canScroll(traderOfferList.size()) && (m < this.indexStartOffset || m >= 7 + this.indexStartOffset)) {
                  ++m;
               } else {
                  ItemStack itemStack = tradeOffer.getOriginalFirstBuyItem();
                  ItemStack itemStack2 = tradeOffer.getAdjustedFirstBuyItem();
                  ItemStack itemStack3 = tradeOffer.getSecondBuyItem();
                  ItemStack itemStack4 = tradeOffer.getMutableSellItem();
                  this.itemRenderer.zOffset = 100.0F;
                  int n = k + 2;
                  this.method_20222(itemStack2, itemStack, l, n);
                  if (!itemStack3.isEmpty()) {
                     this.itemRenderer.renderGuiItem(itemStack3, i + 5 + 35, n);
                     this.itemRenderer.renderGuiItemOverlay(this.font, itemStack3, i + 5 + 35, n);
                  }

                  this.method_20223(tradeOffer, i, n);
                  this.itemRenderer.renderGuiItem(itemStack4, i + 5 + 68, n);
                  this.itemRenderer.renderGuiItemOverlay(this.font, itemStack4, i + 5 + 68, n);
                  this.itemRenderer.zOffset = 0.0F;
                  k += 20;
                  ++m;
               }
            }

            int o = this.selectedIndex;
            tradeOffer = (TradeOffer)traderOfferList.get(o);
            if (((MerchantContainer)this.container).isLevelled()) {
               this.drawLevelInfo(i, j, tradeOffer);
            }

            if (tradeOffer.isDisabled() && this.isPointWithinBounds(186, 35, 22, 21, (double)mouseX, (double)mouseY) && ((MerchantContainer)this.container).canRefreshTrades()) {
               this.renderTooltip(I18n.translate("merchant.deprecated"), mouseX, mouseY);
            }

            MerchantScreen.WidgetButtonPage[] var18 = this.offers;
            int var19 = var18.length;

            for(int var20 = 0; var20 < var19; ++var20) {
               MerchantScreen.WidgetButtonPage widgetButtonPage = var18[var20];
               if (widgetButtonPage.isHovered()) {
                  widgetButtonPage.renderToolTip(mouseX, mouseY);
               }

               widgetButtonPage.visible = widgetButtonPage.index < ((MerchantContainer)this.container).getRecipes().size();
            }

            RenderSystem.popMatrix();
            RenderSystem.enableDepthTest();
            break;
         }
      }

      this.drawMouseoverTooltip(mouseX, mouseY);
   }

   private void method_20223(TradeOffer tradeOffer, int i, int j) {
      RenderSystem.enableBlend();
      this.minecraft.getTextureManager().bindTexture(TEXTURE);
      if (tradeOffer.isDisabled()) {
         blit(i + 5 + 35 + 20, j + 3, this.getBlitOffset(), 25.0F, 171.0F, 10, 9, 256, 512);
      } else {
         blit(i + 5 + 35 + 20, j + 3, this.getBlitOffset(), 15.0F, 171.0F, 10, 9, 256, 512);
      }

   }

   private void method_20222(ItemStack itemStack, ItemStack itemStack2, int i, int j) {
      this.itemRenderer.renderGuiItem(itemStack, i, j);
      if (itemStack2.getCount() == itemStack.getCount()) {
         this.itemRenderer.renderGuiItemOverlay(this.font, itemStack, i, j);
      } else {
         this.itemRenderer.renderGuiItemOverlay(this.font, itemStack2, i, j, itemStack2.getCount() == 1 ? "1" : null);
         this.itemRenderer.renderGuiItemOverlay(this.font, itemStack, i + 14, j, itemStack.getCount() == 1 ? "1" : null);
         this.minecraft.getTextureManager().bindTexture(TEXTURE);
         this.setBlitOffset(this.getBlitOffset() + 300);
         blit(i + 7, j + 12, this.getBlitOffset(), 0.0F, 176.0F, 9, 2, 256, 512);
         this.setBlitOffset(this.getBlitOffset() - 300);
      }

   }

   private boolean canScroll(int listSize) {
      return listSize > 7;
   }

   public boolean mouseScrolled(double d, double e, double amount) {
      int i = ((MerchantContainer)this.container).getRecipes().size();
      if (this.canScroll(i)) {
         int j = i - 7;
         this.indexStartOffset = (int)((double)this.indexStartOffset - amount);
         this.indexStartOffset = MathHelper.clamp(this.indexStartOffset, 0, j);
      }

      return true;
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      int i = ((MerchantContainer)this.container).getRecipes().size();
      if (this.scrolling) {
         int j = this.y + 18;
         int k = j + 139;
         int l = i - 7;
         float f = ((float)mouseY - (float)j - 13.5F) / ((float)(k - j) - 27.0F);
         f = f * (float)l + 0.5F;
         this.indexStartOffset = MathHelper.clamp((int)f, 0, l);
         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.scrolling = false;
      int i = (this.width - this.containerWidth) / 2;
      int j = (this.height - this.containerHeight) / 2;
      if (this.canScroll(((MerchantContainer)this.container).getRecipes().size()) && mouseX > (double)(i + 94) && mouseX < (double)(i + 94 + 6) && mouseY > (double)(j + 18) && mouseY <= (double)(j + 18 + 139 + 1)) {
         this.scrolling = true;
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   @Environment(EnvType.CLIENT)
   class WidgetButtonPage extends ButtonWidget {
      final int index;

      public WidgetButtonPage(int i, int j, int k, ButtonWidget.PressAction pressAction) {
         super(i, j, 89, 20, "", pressAction);
         this.index = k;
         this.visible = false;
      }

      public int getIndex() {
         return this.index;
      }

      public void renderToolTip(int mouseX, int mouseY) {
         if (this.isHovered && ((MerchantContainer)MerchantScreen.this.container).getRecipes().size() > this.index + MerchantScreen.this.indexStartOffset) {
            ItemStack itemStack3;
            if (mouseX < this.x + 20) {
               itemStack3 = ((TradeOffer)((MerchantContainer)MerchantScreen.this.container).getRecipes().get(this.index + MerchantScreen.this.indexStartOffset)).getAdjustedFirstBuyItem();
               MerchantScreen.this.renderTooltip(itemStack3, mouseX, mouseY);
            } else if (mouseX < this.x + 50 && mouseX > this.x + 30) {
               itemStack3 = ((TradeOffer)((MerchantContainer)MerchantScreen.this.container).getRecipes().get(this.index + MerchantScreen.this.indexStartOffset)).getSecondBuyItem();
               if (!itemStack3.isEmpty()) {
                  MerchantScreen.this.renderTooltip(itemStack3, mouseX, mouseY);
               }
            } else if (mouseX > this.x + 65) {
               itemStack3 = ((TradeOffer)((MerchantContainer)MerchantScreen.this.container).getRecipes().get(this.index + MerchantScreen.this.indexStartOffset)).getMutableSellItem();
               MerchantScreen.this.renderTooltip(itemStack3, mouseX, mouseY);
            }
         }

      }
   }
}
