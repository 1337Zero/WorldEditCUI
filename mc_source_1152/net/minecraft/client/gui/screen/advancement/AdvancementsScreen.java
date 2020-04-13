package net.minecraft.client.gui.screen.advancement;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.server.network.packet.AdvancementTabC2SPacket;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class AdvancementsScreen extends Screen implements ClientAdvancementManager.Listener {
   private static final Identifier WINDOW_TEXTURE = new Identifier("textures/gui/advancements/window.png");
   private static final Identifier TABS_TEXTURE = new Identifier("textures/gui/advancements/tabs.png");
   private final ClientAdvancementManager advancementHandler;
   private final Map<Advancement, AdvancementTab> tabs = Maps.newLinkedHashMap();
   private AdvancementTab selectedTab;
   private boolean movingTab;

   public AdvancementsScreen(ClientAdvancementManager advancementHandler) {
      super(NarratorManager.EMPTY);
      this.advancementHandler = advancementHandler;
   }

   protected void init() {
      this.tabs.clear();
      this.selectedTab = null;
      this.advancementHandler.setListener(this);
      if (this.selectedTab == null && !this.tabs.isEmpty()) {
         this.advancementHandler.selectTab(((AdvancementTab)this.tabs.values().iterator().next()).getRoot(), true);
      } else {
         this.advancementHandler.selectTab(this.selectedTab == null ? null : this.selectedTab.getRoot(), true);
      }

   }

   public void removed() {
      this.advancementHandler.setListener((ClientAdvancementManager.Listener)null);
      ClientPlayNetworkHandler clientPlayNetworkHandler = this.minecraft.getNetworkHandler();
      if (clientPlayNetworkHandler != null) {
         clientPlayNetworkHandler.sendPacket(AdvancementTabC2SPacket.close());
      }

   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         int i = (this.width - 252) / 2;
         int j = (this.height - 140) / 2;
         Iterator var8 = this.tabs.values().iterator();

         while(var8.hasNext()) {
            AdvancementTab advancementTab = (AdvancementTab)var8.next();
            if (advancementTab.isClickOnTab(i, j, mouseX, mouseY)) {
               this.advancementHandler.selectTab(advancementTab.getRoot(), true);
               break;
            }
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.minecraft.options.keyAdvancements.matchesKey(keyCode, scanCode)) {
         this.minecraft.openScreen((Screen)null);
         this.minecraft.mouse.lockCursor();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public void render(int mouseX, int mouseY, float delta) {
      int i = (this.width - 252) / 2;
      int j = (this.height - 140) / 2;
      this.renderBackground();
      this.drawAdvancementTree(mouseX, mouseY, i, j);
      this.drawWidgets(i, j);
      this.drawWidgetTooltip(mouseX, mouseY, i, j);
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (button != 0) {
         this.movingTab = false;
         return false;
      } else {
         if (!this.movingTab) {
            this.movingTab = true;
         } else if (this.selectedTab != null) {
            this.selectedTab.move(deltaX, deltaY);
         }

         return true;
      }
   }

   private void drawAdvancementTree(int mouseX, int mouseY, int x, int y) {
      AdvancementTab advancementTab = this.selectedTab;
      if (advancementTab == null) {
         fill(x + 9, y + 18, x + 9 + 234, y + 18 + 113, -16777216);
         String string = I18n.translate("advancements.empty");
         int i = this.font.getStringWidth(string);
         TextRenderer var10000 = this.font;
         float var10002 = (float)(x + 9 + 117 - i / 2);
         int var10003 = y + 18 + 56;
         this.font.getClass();
         var10000.draw(string, var10002, (float)(var10003 - 9 / 2), -1);
         var10000 = this.font;
         var10002 = (float)(x + 9 + 117 - this.font.getStringWidth(":(") / 2);
         var10003 = y + 18 + 113;
         this.font.getClass();
         var10000.draw(":(", var10002, (float)(var10003 - 9), -1);
      } else {
         RenderSystem.pushMatrix();
         RenderSystem.translatef((float)(x + 9), (float)(y + 18), 0.0F);
         advancementTab.render();
         RenderSystem.popMatrix();
         RenderSystem.depthFunc(515);
         RenderSystem.disableDepthTest();
      }
   }

   public void drawWidgets(int x, int y) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      this.minecraft.getTextureManager().bindTexture(WINDOW_TEXTURE);
      this.blit(x, y, 0, 0, 252, 140);
      if (this.tabs.size() > 1) {
         this.minecraft.getTextureManager().bindTexture(TABS_TEXTURE);
         Iterator var3 = this.tabs.values().iterator();

         AdvancementTab advancementTab2;
         while(var3.hasNext()) {
            advancementTab2 = (AdvancementTab)var3.next();
            advancementTab2.drawBackground(x, y, advancementTab2 == this.selectedTab);
         }

         RenderSystem.enableRescaleNormal();
         RenderSystem.defaultBlendFunc();
         var3 = this.tabs.values().iterator();

         while(var3.hasNext()) {
            advancementTab2 = (AdvancementTab)var3.next();
            advancementTab2.drawIcon(x, y, this.itemRenderer);
         }

         RenderSystem.disableBlend();
      }

      this.font.draw(I18n.translate("gui.advancements"), (float)(x + 8), (float)(y + 6), 4210752);
   }

   private void drawWidgetTooltip(int mouseX, int mouseY, int x, int y) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      if (this.selectedTab != null) {
         RenderSystem.pushMatrix();
         RenderSystem.enableDepthTest();
         RenderSystem.translatef((float)(x + 9), (float)(y + 18), 400.0F);
         this.selectedTab.drawWidgetTooltip(mouseX - x - 9, mouseY - y - 18, x, y);
         RenderSystem.disableDepthTest();
         RenderSystem.popMatrix();
      }

      if (this.tabs.size() > 1) {
         Iterator var5 = this.tabs.values().iterator();

         while(var5.hasNext()) {
            AdvancementTab advancementTab = (AdvancementTab)var5.next();
            if (advancementTab.isClickOnTab(x, y, (double)mouseX, (double)mouseY)) {
               this.renderTooltip(advancementTab.getTitle(), mouseX, mouseY);
            }
         }
      }

   }

   public void onRootAdded(Advancement root) {
      AdvancementTab advancementTab = AdvancementTab.create(this.minecraft, this, this.tabs.size(), root);
      if (advancementTab != null) {
         this.tabs.put(root, advancementTab);
      }
   }

   public void onRootRemoved(Advancement root) {
   }

   public void onDependentAdded(Advancement dependent) {
      AdvancementTab advancementTab = this.getTab(dependent);
      if (advancementTab != null) {
         advancementTab.addAdvancement(dependent);
      }

   }

   public void onDependentRemoved(Advancement dependent) {
   }

   public void setProgress(Advancement advancement, AdvancementProgress progress) {
      AdvancementWidget advancementWidget = this.getAdvancementWidget(advancement);
      if (advancementWidget != null) {
         advancementWidget.setProgress(progress);
      }

   }

   public void selectTab(@Nullable Advancement advancement) {
      this.selectedTab = (AdvancementTab)this.tabs.get(advancement);
   }

   public void onClear() {
      this.tabs.clear();
      this.selectedTab = null;
   }

   @Nullable
   public AdvancementWidget getAdvancementWidget(Advancement advancement) {
      AdvancementTab advancementTab = this.getTab(advancement);
      return advancementTab == null ? null : advancementTab.getWidget(advancement);
   }

   @Nullable
   private AdvancementTab getTab(Advancement advancement) {
      while(advancement.getParent() != null) {
         advancement = advancement.getParent();
      }

      return (AdvancementTab)this.tabs.get(advancement);
   }
}
