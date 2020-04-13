package net.minecraft.client.gui.screen.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.network.LanServerInfo;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.options.ServerList;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.UncaughtExceptionLogger;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class MultiplayerServerListWidget extends AlwaysSelectedEntryListWidget<MultiplayerServerListWidget.Entry> {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final ThreadPoolExecutor SERVER_PINGER_THREAD_POOL;
   private static final Identifier UNKNOWN_SERVER_TEXTURE;
   private static final Identifier SERVER_SELECTION_TEXTURE;
   private final MultiplayerScreen screen;
   private final List<MultiplayerServerListWidget.ServerEntry> servers = Lists.newArrayList();
   private final MultiplayerServerListWidget.Entry scanningEntry = new MultiplayerServerListWidget.ScanningEntry();
   private final List<MultiplayerServerListWidget.LanServerEntry> lanServers = Lists.newArrayList();

   public MultiplayerServerListWidget(MultiplayerScreen screen, MinecraftClient client, int width, int height, int top, int bottom, int entryHeight) {
      super(client, width, height, top, bottom, entryHeight);
      this.screen = screen;
   }

   private void updateEntries() {
      this.clearEntries();
      this.servers.forEach(this::addEntry);
      this.addEntry(this.scanningEntry);
      this.lanServers.forEach(this::addEntry);
   }

   public void setSelected(MultiplayerServerListWidget.Entry entry) {
      super.setSelected(entry);
      if (this.getSelected() instanceof MultiplayerServerListWidget.ServerEntry) {
         NarratorManager.INSTANCE.narrate((new TranslatableText("narrator.select", new Object[]{((MultiplayerServerListWidget.ServerEntry)this.getSelected()).server.name})).getString());
      }

   }

   public boolean keyPressed(int i, int j, int k) {
      MultiplayerServerListWidget.Entry entry = (MultiplayerServerListWidget.Entry)this.getSelected();
      return entry != null && entry.keyPressed(i, j, k) || super.keyPressed(i, j, k);
   }

   protected void moveSelection(int i) {
      int j = this.children().indexOf(this.getSelected());
      int k = MathHelper.clamp(j + i, 0, this.getItemCount() - 1);
      MultiplayerServerListWidget.Entry entry = (MultiplayerServerListWidget.Entry)this.children().get(k);
      if (entry instanceof MultiplayerServerListWidget.ScanningEntry) {
         k = MathHelper.clamp(k + (i > 0 ? 1 : -1), 0, this.getItemCount() - 1);
         entry = (MultiplayerServerListWidget.Entry)this.children().get(k);
      }

      super.setSelected(entry);
      this.ensureVisible(entry);
      this.screen.updateButtonActivationStates();
   }

   public void setServers(ServerList servers) {
      this.servers.clear();

      for(int i = 0; i < servers.size(); ++i) {
         this.servers.add(new MultiplayerServerListWidget.ServerEntry(this.screen, servers.get(i)));
      }

      this.updateEntries();
   }

   public void setLanServers(List<LanServerInfo> lanServers) {
      this.lanServers.clear();
      Iterator var2 = lanServers.iterator();

      while(var2.hasNext()) {
         LanServerInfo lanServerInfo = (LanServerInfo)var2.next();
         this.lanServers.add(new MultiplayerServerListWidget.LanServerEntry(this.screen, lanServerInfo));
      }

      this.updateEntries();
   }

   protected int getScrollbarPosition() {
      return super.getScrollbarPosition() + 30;
   }

   public int getRowWidth() {
      return super.getRowWidth() + 85;
   }

   protected boolean isFocused() {
      return this.screen.getFocused() == this;
   }

   static {
      SERVER_PINGER_THREAD_POOL = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER)).build());
      UNKNOWN_SERVER_TEXTURE = new Identifier("textures/misc/unknown_server.png");
      SERVER_SELECTION_TEXTURE = new Identifier("textures/gui/server_selection.png");
   }

   @Environment(EnvType.CLIENT)
   public class ServerEntry extends MultiplayerServerListWidget.Entry {
      private final MultiplayerScreen screen;
      private final MinecraftClient client;
      private final ServerInfo server;
      private final Identifier iconTextureId;
      private String iconUri;
      private NativeImageBackedTexture icon;
      private long time;

      protected ServerEntry(MultiplayerScreen screen, ServerInfo server) {
         this.screen = screen;
         this.server = server;
         this.client = MinecraftClient.getInstance();
         this.iconTextureId = new Identifier("servers/" + Hashing.sha1().hashUnencodedChars(server.address) + "/icon");
         this.icon = (NativeImageBackedTexture)this.client.getTextureManager().getTexture(this.iconTextureId);
      }

      public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
         if (!this.server.online) {
            this.server.online = true;
            this.server.ping = -2L;
            this.server.label = "";
            this.server.playerCountLabel = "";
            MultiplayerServerListWidget.SERVER_PINGER_THREAD_POOL.submit(() -> {
               try {
                  this.screen.getServerListPinger().add(this.server);
               } catch (UnknownHostException var2) {
                  this.server.ping = -1L;
                  this.server.label = Formatting.DARK_RED + I18n.translate("multiplayer.status.cannot_resolve");
               } catch (Exception var3) {
                  this.server.ping = -1L;
                  this.server.label = Formatting.DARK_RED + I18n.translate("multiplayer.status.cannot_connect");
               }

            });
         }

         boolean bl2 = this.server.protocolVersion > SharedConstants.getGameVersion().getProtocolVersion();
         boolean bl3 = this.server.protocolVersion < SharedConstants.getGameVersion().getProtocolVersion();
         boolean bl4 = bl2 || bl3;
         this.client.textRenderer.draw(this.server.name, (float)(k + 32 + 3), (float)(j + 1), 16777215);
         List<String> list = this.client.textRenderer.wrapStringToWidthAsList(this.server.label, l - 32 - 2);

         for(int p = 0; p < Math.min(list.size(), 2); ++p) {
            TextRenderer var10000 = this.client.textRenderer;
            String var10001 = (String)list.get(p);
            float var10002 = (float)(k + 32 + 3);
            int var10003 = j + 12;
            this.client.textRenderer.getClass();
            var10000.draw(var10001, var10002, (float)(var10003 + 9 * p), 8421504);
         }

         String string = bl4 ? Formatting.DARK_RED + this.server.version : this.server.playerCountLabel;
         int q = this.client.textRenderer.getStringWidth(string);
         this.client.textRenderer.draw(string, (float)(k + l - q - 15 - 2), (float)(j + 1), 8421504);
         int r = 0;
         String string2 = null;
         int z;
         String string6;
         if (bl4) {
            z = 5;
            string6 = I18n.translate(bl2 ? "multiplayer.status.client_out_of_date" : "multiplayer.status.server_out_of_date");
            string2 = this.server.playerListSummary;
         } else if (this.server.online && this.server.ping != -2L) {
            if (this.server.ping < 0L) {
               z = 5;
            } else if (this.server.ping < 150L) {
               z = 0;
            } else if (this.server.ping < 300L) {
               z = 1;
            } else if (this.server.ping < 600L) {
               z = 2;
            } else if (this.server.ping < 1000L) {
               z = 3;
            } else {
               z = 4;
            }

            if (this.server.ping < 0L) {
               string6 = I18n.translate("multiplayer.status.no_connection");
            } else {
               string6 = this.server.ping + "ms";
               string2 = this.server.playerListSummary;
            }
         } else {
            r = 1;
            z = (int)(Util.getMeasuringTimeMs() / 100L + (long)(i * 2) & 7L);
            if (z > 4) {
               z = 8 - z;
            }

            string6 = I18n.translate("multiplayer.status.pinging");
         }

         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         this.client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_LOCATION);
         DrawableHelper.blit(k + l - 15, j, (float)(r * 10), (float)(176 + z * 8), 10, 8, 256, 256);
         if (this.server.getIcon() != null && !this.server.getIcon().equals(this.iconUri)) {
            this.iconUri = this.server.getIcon();
            this.updateIcon();
            this.screen.getServerList().saveFile();
         }

         if (this.icon != null) {
            this.draw(k, j, this.iconTextureId);
         } else {
            this.draw(k, j, MultiplayerServerListWidget.UNKNOWN_SERVER_TEXTURE);
         }

         int aa = n - k;
         int ab = o - j;
         if (aa >= l - 15 && aa <= l - 5 && ab >= 0 && ab <= 8) {
            this.screen.setTooltip(string6);
         } else if (aa >= l - q - 15 - 2 && aa <= l - 15 - 2 && ab >= 0 && ab <= 8) {
            this.screen.setTooltip(string2);
         }

         if (this.client.options.touchscreen || bl) {
            this.client.getTextureManager().bindTexture(MultiplayerServerListWidget.SERVER_SELECTION_TEXTURE);
            DrawableHelper.fill(k, j, k + 32, j + 32, -1601138544);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            int ac = n - k;
            int ad = o - j;
            if (this.method_20136()) {
               if (ac < 32 && ac > 16) {
                  DrawableHelper.blit(k, j, 0.0F, 32.0F, 32, 32, 256, 256);
               } else {
                  DrawableHelper.blit(k, j, 0.0F, 0.0F, 32, 32, 256, 256);
               }
            }

            if (i > 0) {
               if (ac < 16 && ad < 16) {
                  DrawableHelper.blit(k, j, 96.0F, 32.0F, 32, 32, 256, 256);
               } else {
                  DrawableHelper.blit(k, j, 96.0F, 0.0F, 32, 32, 256, 256);
               }
            }

            if (i < this.screen.getServerList().size() - 1) {
               if (ac < 16 && ad > 16) {
                  DrawableHelper.blit(k, j, 64.0F, 32.0F, 32, 32, 256, 256);
               } else {
                  DrawableHelper.blit(k, j, 64.0F, 0.0F, 32, 32, 256, 256);
               }
            }
         }

      }

      protected void draw(int x, int y, Identifier textureId) {
         this.client.getTextureManager().bindTexture(textureId);
         RenderSystem.enableBlend();
         DrawableHelper.blit(x, y, 0.0F, 0.0F, 32, 32, 32, 32);
         RenderSystem.disableBlend();
      }

      private boolean method_20136() {
         return true;
      }

      private void updateIcon() {
         String string = this.server.getIcon();
         if (string == null) {
            this.client.getTextureManager().destroyTexture(this.iconTextureId);
            if (this.icon != null && this.icon.getImage() != null) {
               this.icon.getImage().close();
            }

            this.icon = null;
         } else {
            try {
               NativeImage nativeImage = NativeImage.read(string);
               Validate.validState(nativeImage.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
               Validate.validState(nativeImage.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
               if (this.icon == null) {
                  this.icon = new NativeImageBackedTexture(nativeImage);
               } else {
                  this.icon.setImage(nativeImage);
                  this.icon.upload();
               }

               this.client.getTextureManager().registerTexture(this.iconTextureId, this.icon);
            } catch (Throwable var3) {
               MultiplayerServerListWidget.LOGGER.error("Invalid icon for server {} ({})", this.server.name, this.server.address, var3);
               this.server.setIcon((String)null);
            }
         }

      }

      public boolean keyPressed(int i, int j, int k) {
         if (Screen.hasShiftDown()) {
            MultiplayerServerListWidget multiplayerServerListWidget = this.screen.serverListWidget;
            int l = multiplayerServerListWidget.children().indexOf(this);
            if (i == 264 && l < this.screen.getServerList().size() - 1 || i == 265 && l > 0) {
               this.swapEntries(l, i == 264 ? l + 1 : l - 1);
               return true;
            }
         }

         return super.keyPressed(i, j, k);
      }

      private void swapEntries(int i, int j) {
         this.screen.getServerList().swapEntries(i, j);
         this.screen.serverListWidget.setServers(this.screen.getServerList());
         MultiplayerServerListWidget.Entry entry = (MultiplayerServerListWidget.Entry)this.screen.serverListWidget.children().get(j);
         this.screen.serverListWidget.setSelected(entry);
         MultiplayerServerListWidget.this.ensureVisible(entry);
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         double d = mouseX - (double)MultiplayerServerListWidget.this.getRowLeft();
         double e = mouseY - (double)MultiplayerServerListWidget.this.getRowTop(MultiplayerServerListWidget.this.children().indexOf(this));
         if (d <= 32.0D) {
            if (d < 32.0D && d > 16.0D && this.method_20136()) {
               this.screen.select(this);
               this.screen.connect();
               return true;
            }

            int i = this.screen.serverListWidget.children().indexOf(this);
            if (d < 16.0D && e < 16.0D && i > 0) {
               this.swapEntries(i, i - 1);
               return true;
            }

            if (d < 16.0D && e > 16.0D && i < this.screen.getServerList().size() - 1) {
               this.swapEntries(i, i + 1);
               return true;
            }
         }

         this.screen.select(this);
         if (Util.getMeasuringTimeMs() - this.time < 250L) {
            this.screen.connect();
         }

         this.time = Util.getMeasuringTimeMs();
         return false;
      }

      public ServerInfo getServer() {
         return this.server;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class LanServerEntry extends MultiplayerServerListWidget.Entry {
      private final MultiplayerScreen screen;
      protected final MinecraftClient client;
      protected final LanServerInfo server;
      private long time;

      protected LanServerEntry(MultiplayerScreen screen, LanServerInfo server) {
         this.screen = screen;
         this.server = server;
         this.client = MinecraftClient.getInstance();
      }

      public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
         this.client.textRenderer.draw(I18n.translate("lanServer.title"), (float)(k + 32 + 3), (float)(j + 1), 16777215);
         this.client.textRenderer.draw(this.server.getMotd(), (float)(k + 32 + 3), (float)(j + 12), 8421504);
         if (this.client.options.hideServerAddress) {
            this.client.textRenderer.draw(I18n.translate("selectServer.hiddenAddress"), (float)(k + 32 + 3), (float)(j + 12 + 11), 3158064);
         } else {
            this.client.textRenderer.draw(this.server.getAddressPort(), (float)(k + 32 + 3), (float)(j + 12 + 11), 3158064);
         }

      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         this.screen.select(this);
         if (Util.getMeasuringTimeMs() - this.time < 250L) {
            this.screen.connect();
         }

         this.time = Util.getMeasuringTimeMs();
         return false;
      }

      public LanServerInfo getLanServerEntry() {
         return this.server;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class ScanningEntry extends MultiplayerServerListWidget.Entry {
      private final MinecraftClient client = MinecraftClient.getInstance();

      public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
         int var10000 = j + m / 2;
         this.client.textRenderer.getClass();
         int p = var10000 - 9 / 2;
         this.client.textRenderer.draw(I18n.translate("lanServer.scanning"), (float)(this.client.currentScreen.width / 2 - this.client.textRenderer.getStringWidth(I18n.translate("lanServer.scanning")) / 2), (float)p, 16777215);
         String string3;
         switch((int)(Util.getMeasuringTimeMs() / 300L % 4L)) {
         case 0:
         default:
            string3 = "O o o";
            break;
         case 1:
         case 3:
            string3 = "o O o";
            break;
         case 2:
            string3 = "o o O";
         }

         TextRenderer var12 = this.client.textRenderer;
         float var10002 = (float)(this.client.currentScreen.width / 2 - this.client.textRenderer.getStringWidth(string3) / 2);
         this.client.textRenderer.getClass();
         var12.draw(string3, var10002, (float)(p + 9), 8421504);
      }
   }

   @Environment(EnvType.CLIENT)
   public abstract static class Entry extends AlwaysSelectedEntryListWidget.Entry<MultiplayerServerListWidget.Entry> {
   }
}
