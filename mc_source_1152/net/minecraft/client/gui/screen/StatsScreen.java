package net.minecraft.client.gui.screen;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.packet.ClientStatusC2SPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

@Environment(EnvType.CLIENT)
public class StatsScreen extends Screen implements StatsListener {
   protected final Screen parent;
   private StatsScreen.GeneralStatsListWidget generalStats;
   private StatsScreen.ItemStatsListWidget itemStats;
   private StatsScreen.EntityStatsListWidget mobStats;
   private final StatHandler statHandler;
   @Nullable
   private AlwaysSelectedEntryListWidget<?> selectedList;
   private boolean downloadingStats = true;

   public StatsScreen(Screen parent, StatHandler statHandler) {
      super(new TranslatableText("gui.stats", new Object[0]));
      this.parent = parent;
      this.statHandler = statHandler;
   }

   protected void init() {
      this.downloadingStats = true;
      this.minecraft.getNetworkHandler().sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS));
   }

   public void createLists() {
      this.generalStats = new StatsScreen.GeneralStatsListWidget(this.minecraft);
      this.itemStats = new StatsScreen.ItemStatsListWidget(this.minecraft);
      this.mobStats = new StatsScreen.EntityStatsListWidget(this.minecraft);
   }

   public void createButtons() {
      this.addButton(new ButtonWidget(this.width / 2 - 120, this.height - 52, 80, 20, I18n.translate("stat.generalButton"), (buttonWidgetx) -> {
         this.selectStatList(this.generalStats);
      }));
      ButtonWidget buttonWidget = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 40, this.height - 52, 80, 20, I18n.translate("stat.itemsButton"), (buttonWidgetx) -> {
         this.selectStatList(this.itemStats);
      }));
      ButtonWidget buttonWidget2 = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 + 40, this.height - 52, 80, 20, I18n.translate("stat.mobsButton"), (buttonWidgetx) -> {
         this.selectStatList(this.mobStats);
      }));
      this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 28, 200, 20, I18n.translate("gui.done"), (buttonWidgetx) -> {
         this.minecraft.openScreen(this.parent);
      }));
      if (this.itemStats.children().isEmpty()) {
         buttonWidget.active = false;
      }

      if (this.mobStats.children().isEmpty()) {
         buttonWidget2.active = false;
      }

   }

   public void render(int mouseX, int mouseY, float delta) {
      if (this.downloadingStats) {
         this.renderBackground();
         this.drawCenteredString(this.font, I18n.translate("multiplayer.downloadingStats"), this.width / 2, this.height / 2, 16777215);
         TextRenderer var10001 = this.font;
         String var10002 = PROGRESS_BAR_STAGES[(int)(Util.getMeasuringTimeMs() / 150L % (long)PROGRESS_BAR_STAGES.length)];
         int var10003 = this.width / 2;
         int var10004 = this.height / 2;
         this.font.getClass();
         this.drawCenteredString(var10001, var10002, var10003, var10004 + 9 * 2, 16777215);
      } else {
         this.getSelectedStatList().render(mouseX, mouseY, delta);
         this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, 20, 16777215);
         super.render(mouseX, mouseY, delta);
      }

   }

   public void onStatsReady() {
      if (this.downloadingStats) {
         this.createLists();
         this.createButtons();
         this.selectStatList(this.generalStats);
         this.downloadingStats = false;
      }

   }

   public boolean isPauseScreen() {
      return !this.downloadingStats;
   }

   @Nullable
   public AlwaysSelectedEntryListWidget<?> getSelectedStatList() {
      return this.selectedList;
   }

   public void selectStatList(@Nullable AlwaysSelectedEntryListWidget<?> list) {
      this.children.remove(this.generalStats);
      this.children.remove(this.itemStats);
      this.children.remove(this.mobStats);
      if (list != null) {
         this.children.add(0, list);
         this.selectedList = list;
      }

   }

   private int getColumnX(int index) {
      return 115 + 40 * index;
   }

   private void renderStatItem(int x, int y, Item item) {
      this.renderIcon(x + 1, y + 1, 0, 0);
      RenderSystem.enableRescaleNormal();
      this.itemRenderer.renderGuiItemIcon(item.getStackForRender(), x + 2, y + 2);
      RenderSystem.disableRescaleNormal();
   }

   private void renderIcon(int x, int y, int u, int v) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.minecraft.getTextureManager().bindTexture(STATS_ICON_LOCATION);
      blit(x, y, this.getBlitOffset(), (float)u, (float)v, 18, 18, 128, 128);
   }

   @Environment(EnvType.CLIENT)
   class EntityStatsListWidget extends AlwaysSelectedEntryListWidget<StatsScreen.EntityStatsListWidget.Entry> {
      public EntityStatsListWidget(MinecraftClient minecraftClient) {
         int var10002 = StatsScreen.this.width;
         int var10003 = StatsScreen.this.height;
         int var10005 = StatsScreen.this.height - 64;
         StatsScreen.this.font.getClass();
         super(minecraftClient, var10002, var10003, 32, var10005, 9 * 4);
         Iterator var3 = Registry.ENTITY_TYPE.iterator();

         while(true) {
            EntityType entityType;
            do {
               if (!var3.hasNext()) {
                  return;
               }

               entityType = (EntityType)var3.next();
            } while(StatsScreen.this.statHandler.getStat(Stats.KILLED.getOrCreateStat(entityType)) <= 0 && StatsScreen.this.statHandler.getStat(Stats.KILLED_BY.getOrCreateStat(entityType)) <= 0);

            this.addEntry(new StatsScreen.EntityStatsListWidget.Entry(entityType));
         }
      }

      protected void renderBackground() {
         StatsScreen.this.renderBackground();
      }

      @Environment(EnvType.CLIENT)
      class Entry extends AlwaysSelectedEntryListWidget.Entry<StatsScreen.EntityStatsListWidget.Entry> {
         private final EntityType<?> entityType;

         public Entry(EntityType<?> entityType) {
            this.entityType = entityType;
         }

         public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            String string = I18n.translate(Util.createTranslationKey("entity", EntityType.getId(this.entityType)));
            int p = StatsScreen.this.statHandler.getStat(Stats.KILLED.getOrCreateStat(this.entityType));
            int q = StatsScreen.this.statHandler.getStat(Stats.KILLED_BY.getOrCreateStat(this.entityType));
            EntityStatsListWidget.this.drawString(StatsScreen.this.font, string, k + 2, j + 1, 16777215);
            StatsScreen.EntityStatsListWidget var10000 = EntityStatsListWidget.this;
            TextRenderer var10001 = StatsScreen.this.font;
            String var10002 = this.getKilledString(string, p);
            int var10003 = k + 2 + 10;
            int var10004 = j + 1;
            StatsScreen.this.font.getClass();
            var10000.drawString(var10001, var10002, var10003, var10004 + 9, p == 0 ? 6316128 : 9474192);
            var10000 = EntityStatsListWidget.this;
            var10001 = StatsScreen.this.font;
            var10002 = this.getKilledByString(string, q);
            var10003 = k + 2 + 10;
            var10004 = j + 1;
            StatsScreen.this.font.getClass();
            var10000.drawString(var10001, var10002, var10003, var10004 + 9 * 2, q == 0 ? 6316128 : 9474192);
         }

         private String getKilledString(String entityName, int killCount) {
            String string = Stats.KILLED.getTranslationKey();
            return killCount == 0 ? I18n.translate(string + ".none", entityName) : I18n.translate(string, killCount, entityName);
         }

         private String getKilledByString(String entityName, int killCount) {
            String string = Stats.KILLED_BY.getTranslationKey();
            return killCount == 0 ? I18n.translate(string + ".none", entityName) : I18n.translate(string, entityName, killCount);
         }
      }
   }

   @Environment(EnvType.CLIENT)
   class ItemStatsListWidget extends AlwaysSelectedEntryListWidget<StatsScreen.ItemStatsListWidget.Entry> {
      protected final List<StatType<Block>> blockStatTypes = Lists.newArrayList();
      protected final List<StatType<Item>> itemStatTypes;
      private final int[] HEADER_ICON_SPRITE_INDICES = new int[]{3, 4, 1, 2, 5, 6};
      protected int selectedHeaderColumn = -1;
      protected final List<Item> items;
      protected final Comparator<Item> comparator = new StatsScreen.ItemStatsListWidget.ItemComparator();
      @Nullable
      protected StatType<?> selectedStatType;
      protected int field_18760;

      public ItemStatsListWidget(MinecraftClient client) {
         super(client, StatsScreen.this.width, StatsScreen.this.height, 32, StatsScreen.this.height - 64, 20);
         this.blockStatTypes.add(Stats.MINED);
         this.itemStatTypes = Lists.newArrayList(new StatType[]{Stats.BROKEN, Stats.CRAFTED, Stats.USED, Stats.PICKED_UP, Stats.DROPPED});
         this.setRenderHeader(true, 20);
         Set<Item> set = Sets.newIdentityHashSet();
         Iterator var4 = Registry.ITEM.iterator();

         boolean bl2;
         Iterator var7;
         StatType statType2;
         while(var4.hasNext()) {
            Item item = (Item)var4.next();
            bl2 = false;
            var7 = this.itemStatTypes.iterator();

            while(var7.hasNext()) {
               statType2 = (StatType)var7.next();
               if (statType2.hasStat(item) && StatsScreen.this.statHandler.getStat(statType2.getOrCreateStat(item)) > 0) {
                  bl2 = true;
               }
            }

            if (bl2) {
               set.add(item);
            }
         }

         var4 = Registry.BLOCK.iterator();

         while(var4.hasNext()) {
            Block block = (Block)var4.next();
            bl2 = false;
            var7 = this.blockStatTypes.iterator();

            while(var7.hasNext()) {
               statType2 = (StatType)var7.next();
               if (statType2.hasStat(block) && StatsScreen.this.statHandler.getStat(statType2.getOrCreateStat(block)) > 0) {
                  bl2 = true;
               }
            }

            if (bl2) {
               set.add(block.asItem());
            }
         }

         set.remove(Items.AIR);
         this.items = Lists.newArrayList(set);

         for(int i = 0; i < this.items.size(); ++i) {
            this.addEntry(new StatsScreen.ItemStatsListWidget.Entry());
         }

      }

      protected void renderHeader(int i, int j, Tessellator tessellator) {
         if (!this.minecraft.mouse.wasLeftButtonClicked()) {
            this.selectedHeaderColumn = -1;
         }

         int n;
         for(n = 0; n < this.HEADER_ICON_SPRITE_INDICES.length; ++n) {
            StatsScreen.this.renderIcon(i + StatsScreen.this.getColumnX(n) - 18, j + 1, 0, this.selectedHeaderColumn == n ? 0 : 18);
         }

         int o;
         if (this.selectedStatType != null) {
            n = StatsScreen.this.getColumnX(this.getHeaderIndex(this.selectedStatType)) - 36;
            o = this.field_18760 == 1 ? 2 : 1;
            StatsScreen.this.renderIcon(i + n, j + 1, 18 * o, 0);
         }

         for(n = 0; n < this.HEADER_ICON_SPRITE_INDICES.length; ++n) {
            o = this.selectedHeaderColumn == n ? 1 : 0;
            StatsScreen.this.renderIcon(i + StatsScreen.this.getColumnX(n) - 18 + o, j + 1 + o, 18 * this.HEADER_ICON_SPRITE_INDICES[n], 18);
         }

      }

      public int getRowWidth() {
         return 375;
      }

      protected int getScrollbarPosition() {
         return this.width / 2 + 140;
      }

      protected void renderBackground() {
         StatsScreen.this.renderBackground();
      }

      protected void clickedHeader(int i, int j) {
         this.selectedHeaderColumn = -1;

         for(int k = 0; k < this.HEADER_ICON_SPRITE_INDICES.length; ++k) {
            int l = i - StatsScreen.this.getColumnX(k);
            if (l >= -36 && l <= 0) {
               this.selectedHeaderColumn = k;
               break;
            }
         }

         if (this.selectedHeaderColumn >= 0) {
            this.selectStatType(this.getStatType(this.selectedHeaderColumn));
            this.minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         }

      }

      private StatType<?> getStatType(int headerColumn) {
         return headerColumn < this.blockStatTypes.size() ? (StatType)this.blockStatTypes.get(headerColumn) : (StatType)this.itemStatTypes.get(headerColumn - this.blockStatTypes.size());
      }

      private int getHeaderIndex(StatType<?> statType) {
         int i = this.blockStatTypes.indexOf(statType);
         if (i >= 0) {
            return i;
         } else {
            int j = this.itemStatTypes.indexOf(statType);
            return j >= 0 ? j + this.blockStatTypes.size() : -1;
         }
      }

      protected void renderDecorations(int mouseX, int mouseY) {
         if (mouseY >= this.top && mouseY <= this.bottom) {
            StatsScreen.ItemStatsListWidget.Entry entry = (StatsScreen.ItemStatsListWidget.Entry)this.getEntryAtPosition((double)mouseX, (double)mouseY);
            int i = (this.width - this.getRowWidth()) / 2;
            if (entry != null) {
               if (mouseX < i + 40 || mouseX > i + 40 + 20) {
                  return;
               }

               Item item = (Item)this.items.get(this.children().indexOf(entry));
               this.render(this.getText(item), mouseX, mouseY);
            } else {
               Text text = null;
               int j = mouseX - i;

               for(int k = 0; k < this.HEADER_ICON_SPRITE_INDICES.length; ++k) {
                  int l = StatsScreen.this.getColumnX(k);
                  if (j >= l - 18 && j <= l) {
                     text = new TranslatableText(this.getStatType(k).getTranslationKey(), new Object[0]);
                     break;
                  }
               }

               this.render(text, mouseX, mouseY);
            }

         }
      }

      protected void render(@Nullable Text text, int x, int y) {
         if (text != null) {
            String string = text.asFormattedString();
            int i = x + 12;
            int j = y - 12;
            int k = StatsScreen.this.font.getStringWidth(string);
            this.fillGradient(i - 3, j - 3, i + k + 3, j + 8 + 3, -1073741824, -1073741824);
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0.0F, 0.0F, 400.0F);
            StatsScreen.this.font.drawWithShadow(string, (float)i, (float)j, -1);
            RenderSystem.popMatrix();
         }
      }

      protected Text getText(Item item) {
         return item.getName();
      }

      protected void selectStatType(StatType<?> statType) {
         if (statType != this.selectedStatType) {
            this.selectedStatType = statType;
            this.field_18760 = -1;
         } else if (this.field_18760 == -1) {
            this.field_18760 = 1;
         } else {
            this.selectedStatType = null;
            this.field_18760 = 0;
         }

         this.items.sort(this.comparator);
      }

      @Environment(EnvType.CLIENT)
      class Entry extends AlwaysSelectedEntryListWidget.Entry<StatsScreen.ItemStatsListWidget.Entry> {
         private Entry() {
         }

         public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            Item item = (Item)StatsScreen.this.itemStats.items.get(i);
            StatsScreen.this.renderStatItem(k + 40, j, item);

            int p;
            for(p = 0; p < StatsScreen.this.itemStats.blockStatTypes.size(); ++p) {
               Stat stat2;
               if (item instanceof BlockItem) {
                  stat2 = ((StatType)StatsScreen.this.itemStats.blockStatTypes.get(p)).getOrCreateStat(((BlockItem)item).getBlock());
               } else {
                  stat2 = null;
               }

               this.render(stat2, k + StatsScreen.this.getColumnX(p), j, i % 2 == 0);
            }

            for(p = 0; p < StatsScreen.this.itemStats.itemStatTypes.size(); ++p) {
               this.render(((StatType)StatsScreen.this.itemStats.itemStatTypes.get(p)).getOrCreateStat(item), k + StatsScreen.this.getColumnX(p + StatsScreen.this.itemStats.blockStatTypes.size()), j, i % 2 == 0);
            }

         }

         protected void render(@Nullable Stat<?> stat, int x, int y, boolean light) {
            String string = stat == null ? "-" : stat.format(StatsScreen.this.statHandler.getStat(stat));
            ItemStatsListWidget.this.drawString(StatsScreen.this.font, string, x - StatsScreen.this.font.getStringWidth(string), y + 5, light ? 16777215 : 9474192);
         }
      }

      @Environment(EnvType.CLIENT)
      class ItemComparator implements Comparator<Item> {
         private ItemComparator() {
         }

         public int compare(Item item, Item item2) {
            int k;
            int n;
            if (ItemStatsListWidget.this.selectedStatType == null) {
               k = 0;
               n = 0;
            } else {
               StatType statType;
               if (ItemStatsListWidget.this.blockStatTypes.contains(ItemStatsListWidget.this.selectedStatType)) {
                  statType = ItemStatsListWidget.this.selectedStatType;
                  k = item instanceof BlockItem ? StatsScreen.this.statHandler.getStat(statType, ((BlockItem)item).getBlock()) : -1;
                  n = item2 instanceof BlockItem ? StatsScreen.this.statHandler.getStat(statType, ((BlockItem)item2).getBlock()) : -1;
               } else {
                  statType = ItemStatsListWidget.this.selectedStatType;
                  k = StatsScreen.this.statHandler.getStat(statType, item);
                  n = StatsScreen.this.statHandler.getStat(statType, item2);
               }
            }

            return k == n ? ItemStatsListWidget.this.field_18760 * Integer.compare(Item.getRawId(item), Item.getRawId(item2)) : ItemStatsListWidget.this.field_18760 * Integer.compare(k, n);
         }
      }
   }

   @Environment(EnvType.CLIENT)
   class GeneralStatsListWidget extends AlwaysSelectedEntryListWidget<StatsScreen.GeneralStatsListWidget.Entry> {
      public GeneralStatsListWidget(MinecraftClient minecraftClient) {
         super(minecraftClient, StatsScreen.this.width, StatsScreen.this.height, 32, StatsScreen.this.height - 64, 10);
         Iterator var3 = Stats.CUSTOM.iterator();

         while(var3.hasNext()) {
            Stat<Identifier> stat = (Stat)var3.next();
            this.addEntry(new StatsScreen.GeneralStatsListWidget.Entry(stat));
         }

      }

      protected void renderBackground() {
         StatsScreen.this.renderBackground();
      }

      @Environment(EnvType.CLIENT)
      class Entry extends AlwaysSelectedEntryListWidget.Entry<StatsScreen.GeneralStatsListWidget.Entry> {
         private final Stat<Identifier> stat;

         private Entry(Stat<Identifier> stat) {
            this.stat = stat;
         }

         public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            Text text = (new TranslatableText("stat." + ((Identifier)this.stat.getValue()).toString().replace(':', '.'), new Object[0])).formatted(Formatting.GRAY);
            GeneralStatsListWidget.this.drawString(StatsScreen.this.font, text.getString(), k + 2, j + 1, i % 2 == 0 ? 16777215 : 9474192);
            String string = this.stat.format(StatsScreen.this.statHandler.getStat(this.stat));
            GeneralStatsListWidget.this.drawString(StatsScreen.this.font, string, k + 2 + 213 - StatsScreen.this.font.getStringWidth(string), j + 1, i % 2 == 0 ? 16777215 : 9474192);
         }
      }
   }
}
