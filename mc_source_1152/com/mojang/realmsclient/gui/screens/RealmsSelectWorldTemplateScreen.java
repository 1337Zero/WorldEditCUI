package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.RealmsUtil;
import com.mojang.realmsclient.util.TextRenderingUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsSelectWorldTemplateScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private final RealmsScreenWithCallback<WorldTemplate> lastScreen;
   private RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList field_20071;
   private int selectedTemplate;
   private String title;
   private RealmsButton selectButton;
   private RealmsButton trailerButton;
   private RealmsButton publisherButton;
   private String toolTip;
   private String currentLink;
   private final RealmsServer.WorldType worldType;
   private int clicks;
   private String warning;
   private String warningURL;
   private boolean displayWarning;
   private boolean hoverWarning;
   private List<TextRenderingUtils.Line> noTemplatesMessage;

   public RealmsSelectWorldTemplateScreen(RealmsScreenWithCallback<WorldTemplate> realmsScreenWithCallback, RealmsServer.WorldType worldType) {
      this(realmsScreenWithCallback, worldType, (WorldTemplatePaginatedList)null);
   }

   public RealmsSelectWorldTemplateScreen(RealmsScreenWithCallback<WorldTemplate> realmsScreenWithCallback, RealmsServer.WorldType worldType, @Nullable WorldTemplatePaginatedList worldTemplatePaginatedList) {
      this.selectedTemplate = -1;
      this.lastScreen = realmsScreenWithCallback;
      this.worldType = worldType;
      if (worldTemplatePaginatedList == null) {
         this.field_20071 = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList();
         this.method_21415(new WorldTemplatePaginatedList(10));
      } else {
         this.field_20071 = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList(Lists.newArrayList(worldTemplatePaginatedList.templates));
         this.method_21415(worldTemplatePaginatedList);
      }

      this.title = getLocalizedString("mco.template.title");
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public void setWarning(String string) {
      this.warning = string;
      this.displayWarning = true;
   }

   public boolean mouseClicked(double x, double y, int buttonNum) {
      if (this.hoverWarning && this.warningURL != null) {
         RealmsUtil.browseTo("https://beta.minecraft.net/realms/adventure-maps-in-1-9");
         return true;
      } else {
         return super.mouseClicked(x, y, buttonNum);
      }
   }

   public void init() {
      this.setKeyboardHandlerSendRepeatsToGui(true);
      this.field_20071 = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList(this.field_20071.method_21450());
      this.buttonsAdd(this.trailerButton = new RealmsButton(2, this.width() / 2 - 206, this.height() - 32, 100, 20, getLocalizedString("mco.template.button.trailer")) {
         public void onPress() {
            RealmsSelectWorldTemplateScreen.this.onTrailer();
         }
      });
      this.buttonsAdd(this.selectButton = new RealmsButton(1, this.width() / 2 - 100, this.height() - 32, 100, 20, getLocalizedString("mco.template.button.select")) {
         public void onPress() {
            RealmsSelectWorldTemplateScreen.this.selectTemplate();
         }
      });
      this.buttonsAdd(new RealmsButton(0, this.width() / 2 + 6, this.height() - 32, 100, 20, getLocalizedString(this.worldType == RealmsServer.WorldType.MINIGAME ? "gui.cancel" : "gui.back")) {
         public void onPress() {
            RealmsSelectWorldTemplateScreen.this.backButtonClicked();
         }
      });
      this.publisherButton = new RealmsButton(3, this.width() / 2 + 112, this.height() - 32, 100, 20, getLocalizedString("mco.template.button.publisher")) {
         public void onPress() {
            RealmsSelectWorldTemplateScreen.this.onPublish();
         }
      };
      this.buttonsAdd(this.publisherButton);
      this.selectButton.active(false);
      this.trailerButton.setVisible(false);
      this.publisherButton.setVisible(false);
      this.addWidget(this.field_20071);
      this.focusOn(this.field_20071);
      Realms.narrateNow((Iterable)Stream.of(this.title, this.warning).filter(Objects::nonNull).collect(Collectors.toList()));
   }

   private void updateButtonStates() {
      this.publisherButton.setVisible(this.shouldPublisherBeVisible());
      this.trailerButton.setVisible(this.shouldTrailerBeVisible());
      this.selectButton.active(this.shouldSelectButtonBeActive());
   }

   private boolean shouldSelectButtonBeActive() {
      return this.selectedTemplate != -1;
   }

   private boolean shouldPublisherBeVisible() {
      return this.selectedTemplate != -1 && !this.method_21434().link.isEmpty();
   }

   private WorldTemplate method_21434() {
      return this.field_20071.method_21447(this.selectedTemplate);
   }

   private boolean shouldTrailerBeVisible() {
      return this.selectedTemplate != -1 && !this.method_21434().trailer.isEmpty();
   }

   public void tick() {
      super.tick();
      --this.clicks;
      if (this.clicks < 0) {
         this.clicks = 0;
      }

   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      switch(eventKey) {
      case 256:
         this.backButtonClicked();
         return true;
      default:
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   private void backButtonClicked() {
      this.lastScreen.callback((Object)null);
      Realms.setScreen(this.lastScreen);
   }

   private void selectTemplate() {
      if (this.selectedTemplate >= 0 && this.selectedTemplate < this.field_20071.getItemCount()) {
         WorldTemplate worldTemplate = this.method_21434();
         this.lastScreen.callback(worldTemplate);
      }

   }

   private void onTrailer() {
      if (this.selectedTemplate >= 0 && this.selectedTemplate < this.field_20071.getItemCount()) {
         WorldTemplate worldTemplate = this.method_21434();
         if (!"".equals(worldTemplate.trailer)) {
            RealmsUtil.browseTo(worldTemplate.trailer);
         }
      }

   }

   private void onPublish() {
      if (this.selectedTemplate >= 0 && this.selectedTemplate < this.field_20071.getItemCount()) {
         WorldTemplate worldTemplate = this.method_21434();
         if (!"".equals(worldTemplate.link)) {
            RealmsUtil.browseTo(worldTemplate.link);
         }
      }

   }

   private void method_21415(final WorldTemplatePaginatedList worldTemplatePaginatedList) {
      (new Thread("realms-template-fetcher") {
         public void run() {
            WorldTemplatePaginatedList worldTemplatePaginatedListx = worldTemplatePaginatedList;

            Either either;
            for(RealmsClient realmsClient = RealmsClient.createRealmsClient(); worldTemplatePaginatedListx != null; worldTemplatePaginatedListx = (WorldTemplatePaginatedList)Realms.execute(() -> {
               if (either.right().isPresent()) {
                  RealmsSelectWorldTemplateScreen.LOGGER.error("Couldn't fetch templates: {}", either.right().get());
                  if (RealmsSelectWorldTemplateScreen.this.field_20071.method_21446()) {
                     RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(RealmsScreen.getLocalizedString("mco.template.select.failure"));
                  }

                  return null;
               } else {
                  assert either.left().isPresent();

                  WorldTemplatePaginatedList worldTemplatePaginatedListx = (WorldTemplatePaginatedList)either.left().get();
                  Iterator var3 = worldTemplatePaginatedListx.templates.iterator();

                  while(var3.hasNext()) {
                     WorldTemplate worldTemplate = (WorldTemplate)var3.next();
                     RealmsSelectWorldTemplateScreen.this.field_20071.addEntry(worldTemplate);
                  }

                  if (worldTemplatePaginatedListx.templates.isEmpty()) {
                     if (RealmsSelectWorldTemplateScreen.this.field_20071.method_21446()) {
                        String string = RealmsScreen.getLocalizedString("mco.template.select.none", "%link");
                        TextRenderingUtils.LineSegment lineSegment = TextRenderingUtils.LineSegment.link(RealmsScreen.getLocalizedString("mco.template.select.none.linkTitle"), "https://minecraft.net/realms/content-creator/");
                        RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(string, lineSegment);
                     }

                     return null;
                  } else {
                     return worldTemplatePaginatedListx;
                  }
               }
            }).join()) {
               either = RealmsSelectWorldTemplateScreen.this.method_21416(worldTemplatePaginatedListx, realmsClient);
            }

         }
      }).start();
   }

   private Either<WorldTemplatePaginatedList, String> method_21416(WorldTemplatePaginatedList worldTemplatePaginatedList, RealmsClient realmsClient) {
      try {
         return Either.left(realmsClient.fetchWorldTemplates(worldTemplatePaginatedList.page + 1, worldTemplatePaginatedList.size, this.worldType));
      } catch (RealmsServiceException var4) {
         return Either.right(var4.getMessage());
      }
   }

   public void render(int xm, int ym, float a) {
      this.toolTip = null;
      this.currentLink = null;
      this.hoverWarning = false;
      this.renderBackground();
      this.field_20071.render(xm, ym, a);
      if (this.noTemplatesMessage != null) {
         this.method_21414(xm, ym, this.noTemplatesMessage);
      }

      this.drawCenteredString(this.title, this.width() / 2, 13, 16777215);
      if (this.displayWarning) {
         String[] strings = this.warning.split("\\\\n");

         int m;
         int n;
         for(m = 0; m < strings.length; ++m) {
            int j = this.fontWidth(strings[m]);
            n = this.width() / 2 - j / 2;
            int l = RealmsConstants.row(-1 + m);
            if (xm >= n && xm <= n + j && ym >= l && ym <= l + this.fontLineHeight()) {
               this.hoverWarning = true;
            }
         }

         for(m = 0; m < strings.length; ++m) {
            String string = strings[m];
            n = 10526880;
            if (this.warningURL != null) {
               if (this.hoverWarning) {
                  n = 7107012;
                  string = "§n" + string;
               } else {
                  n = 3368635;
               }
            }

            this.drawCenteredString(string, this.width() / 2, RealmsConstants.row(-1 + m), n);
         }
      }

      super.render(xm, ym, a);
      if (this.toolTip != null) {
         this.renderMousehoverTooltip(this.toolTip, xm, ym);
      }

   }

   private void method_21414(int i, int j, List<TextRenderingUtils.Line> list) {
      for(int k = 0; k < list.size(); ++k) {
         TextRenderingUtils.Line line = (TextRenderingUtils.Line)list.get(k);
         int l = RealmsConstants.row(4 + k);
         int m = line.segments.stream().mapToInt((lineSegmentx) -> {
            return this.fontWidth(lineSegmentx.renderedText());
         }).sum();
         int n = this.width() / 2 - m / 2;

         int p;
         for(Iterator var9 = line.segments.iterator(); var9.hasNext(); n = p) {
            TextRenderingUtils.LineSegment lineSegment = (TextRenderingUtils.LineSegment)var9.next();
            int o = lineSegment.isLink() ? 3368635 : 16777215;
            p = this.draw(lineSegment.renderedText(), n, l, o, true);
            if (lineSegment.isLink() && i > n && i < p && j > l - 3 && j < l + 8) {
               this.toolTip = lineSegment.getLinkUrl();
               this.currentLink = lineSegment.getLinkUrl();
            }
         }
      }

   }

   protected void renderMousehoverTooltip(String msg, int x, int y) {
      if (msg != null) {
         int i = x + 12;
         int j = y - 12;
         int k = this.fontWidth(msg);
         this.fillGradient(i - 3, j - 3, i + k + 3, j + 8 + 3, -1073741824, -1073741824);
         this.fontDrawShadow(msg, i, j, 16777215);
      }
   }

   @Environment(EnvType.CLIENT)
   class WorldTemplateObjectSelectionListEntry extends RealmListEntry {
      final WorldTemplate mTemplate;

      public WorldTemplateObjectSelectionListEntry(WorldTemplate template) {
         this.mTemplate = template;
      }

      public void render(int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float a) {
         this.renderWorldTemplateItem(this.mTemplate, rowLeft, rowTop, mouseX, mouseY);
      }

      private void renderWorldTemplateItem(WorldTemplate worldTemplate, int x, int y, int mouseX, int mouseY) {
         int i = x + 45 + 20;
         RealmsSelectWorldTemplateScreen.this.drawString(worldTemplate.name, i, y + 2, 16777215);
         RealmsSelectWorldTemplateScreen.this.drawString(worldTemplate.author, i, y + 15, 8421504);
         RealmsSelectWorldTemplateScreen.this.drawString(worldTemplate.version, i + 227 - RealmsSelectWorldTemplateScreen.this.fontWidth(worldTemplate.version), y + 1, 8421504);
         if (!"".equals(worldTemplate.link) || !"".equals(worldTemplate.trailer) || !"".equals(worldTemplate.recommendedPlayers)) {
            this.drawIcons(i - 1, y + 25, mouseX, mouseY, worldTemplate.link, worldTemplate.trailer, worldTemplate.recommendedPlayers);
         }

         this.drawImage(x, y + 1, mouseX, mouseY, worldTemplate);
      }

      private void drawImage(int x, int y, int xm, int ym, WorldTemplate worldTemplate) {
         RealmsTextureManager.bindWorldTemplate(worldTemplate.id, worldTemplate.image);
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         RealmsScreen.blit(x + 1, y + 1, 0.0F, 0.0F, 38, 38, 38, 38);
         RealmsScreen.bind("realms:textures/gui/realms/slot_frame.png");
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         RealmsScreen.blit(x, y, 0.0F, 0.0F, 40, 40, 40, 40);
      }

      private void drawIcons(int x, int y, int xm, int ym, String link, String trailerLink, String recommendedPlayers) {
         if (!"".equals(recommendedPlayers)) {
            RealmsSelectWorldTemplateScreen.this.drawString(recommendedPlayers, x, y + 4, 8421504);
         }

         int i = "".equals(recommendedPlayers) ? 0 : RealmsSelectWorldTemplateScreen.this.fontWidth(recommendedPlayers) + 2;
         boolean bl = false;
         boolean bl2 = false;
         if (xm >= x + i && xm <= x + i + 32 && ym >= y && ym <= y + 15 && ym < RealmsSelectWorldTemplateScreen.this.height() - 15 && ym > 32) {
            if (xm <= x + 15 + i && xm > i) {
               if ("".equals(link)) {
                  bl2 = true;
               } else {
                  bl = true;
               }
            } else if (!"".equals(link)) {
               bl2 = true;
            }
         }

         if (!"".equals(link)) {
            RealmsScreen.bind("realms:textures/gui/realms/link_icons.png");
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.pushMatrix();
            RenderSystem.scalef(1.0F, 1.0F, 1.0F);
            RealmsScreen.blit(x + i, y, bl ? 15.0F : 0.0F, 0.0F, 15, 15, 30, 15);
            RenderSystem.popMatrix();
         }

         if (!"".equals(trailerLink)) {
            RealmsScreen.bind("realms:textures/gui/realms/trailer_icons.png");
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.pushMatrix();
            RenderSystem.scalef(1.0F, 1.0F, 1.0F);
            RealmsScreen.blit(x + i + ("".equals(link) ? 0 : 17), y, bl2 ? 15.0F : 0.0F, 0.0F, 15, 15, 30, 15);
            RenderSystem.popMatrix();
         }

         if (bl && !"".equals(link)) {
            RealmsSelectWorldTemplateScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.template.info.tooltip");
            RealmsSelectWorldTemplateScreen.this.currentLink = link;
         } else if (bl2 && !"".equals(trailerLink)) {
            RealmsSelectWorldTemplateScreen.this.toolTip = RealmsScreen.getLocalizedString("mco.template.trailer.tooltip");
            RealmsSelectWorldTemplateScreen.this.currentLink = trailerLink;
         }

      }
   }

   @Environment(EnvType.CLIENT)
   class WorldTemplateObjectSelectionList extends RealmsObjectSelectionList<RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionListEntry> {
      public WorldTemplateObjectSelectionList() {
         this(Collections.emptyList());
      }

      public WorldTemplateObjectSelectionList(Iterable<WorldTemplate> iterable) {
         super(RealmsSelectWorldTemplateScreen.this.width(), RealmsSelectWorldTemplateScreen.this.height(), RealmsSelectWorldTemplateScreen.this.displayWarning ? RealmsConstants.row(1) : 32, RealmsSelectWorldTemplateScreen.this.height() - 40, 46);
         iterable.forEach(this::addEntry);
      }

      public void addEntry(WorldTemplate template) {
         this.addEntry(RealmsSelectWorldTemplateScreen.this.new WorldTemplateObjectSelectionListEntry(template));
      }

      public boolean mouseClicked(double xm, double ym, int buttonNum) {
         if (buttonNum == 0 && ym >= (double)this.y0() && ym <= (double)this.y1()) {
            int i = this.width() / 2 - 150;
            if (RealmsSelectWorldTemplateScreen.this.currentLink != null) {
               RealmsUtil.browseTo(RealmsSelectWorldTemplateScreen.this.currentLink);
            }

            int j = (int)Math.floor(ym - (double)this.y0()) - this.headerHeight() + this.getScroll() - 4;
            int k = j / this.itemHeight();
            if (xm >= (double)i && xm < (double)this.getScrollbarPosition() && k >= 0 && j >= 0 && k < this.getItemCount()) {
               this.selectItem(k);
               this.itemClicked(j, k, xm, ym, this.width());
               if (k >= RealmsSelectWorldTemplateScreen.this.field_20071.getItemCount()) {
                  return super.mouseClicked(xm, ym, buttonNum);
               }

               RealmsSelectWorldTemplateScreen.this.selectedTemplate = k;
               RealmsSelectWorldTemplateScreen.this.updateButtonStates();
               RealmsSelectWorldTemplateScreen.this.clicks = RealmsSelectWorldTemplateScreen.this.clicks + 7;
               if (RealmsSelectWorldTemplateScreen.this.clicks >= 10) {
                  RealmsSelectWorldTemplateScreen.this.selectTemplate();
               }

               return true;
            }
         }

         return super.mouseClicked(xm, ym, buttonNum);
      }

      public void selectItem(int item) {
         RealmsSelectWorldTemplateScreen.this.selectedTemplate = item;
         this.setSelected(item);
         if (item != -1) {
            WorldTemplate worldTemplate = RealmsSelectWorldTemplateScreen.this.field_20071.method_21447(item);
            String string = RealmsScreen.getLocalizedString("narrator.select.list.position", item + 1, RealmsSelectWorldTemplateScreen.this.field_20071.getItemCount());
            String string2 = RealmsScreen.getLocalizedString("mco.template.select.narrate.version", worldTemplate.version);
            String string3 = RealmsScreen.getLocalizedString("mco.template.select.narrate.authors", worldTemplate.author);
            String string4 = Realms.joinNarrations(Arrays.asList(worldTemplate.name, string3, worldTemplate.recommendedPlayers, string2, string));
            Realms.narrateNow(RealmsScreen.getLocalizedString("narrator.select", string4));
         }

         RealmsSelectWorldTemplateScreen.this.updateButtonStates();
      }

      public void itemClicked(int clickSlotPos, int slot, double xm, double ym, int width) {
         if (slot < RealmsSelectWorldTemplateScreen.this.field_20071.getItemCount()) {
            ;
         }
      }

      public int getMaxPosition() {
         return this.getItemCount() * 46;
      }

      public int getRowWidth() {
         return 300;
      }

      public void renderBackground() {
         RealmsSelectWorldTemplateScreen.this.renderBackground();
      }

      public boolean isFocused() {
         return RealmsSelectWorldTemplateScreen.this.isFocused(this);
      }

      public boolean method_21446() {
         return this.getItemCount() == 0;
      }

      public WorldTemplate method_21447(int i) {
         return ((RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionListEntry)this.children().get(i)).mTemplate;
      }

      public List<WorldTemplate> method_21450() {
         return (List)this.children().stream().map((worldTemplateObjectSelectionListEntry) -> {
            return worldTemplateObjectSelectionListEntry.mTemplate;
         }).collect(Collectors.toList());
      }
   }
}
