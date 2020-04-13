package net.minecraft.client.gui.screen;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.ServerAddress;
import net.minecraft.server.network.packet.HandshakeC2SPacket;
import net.minecraft.server.network.packet.LoginHelloC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.UncaughtExceptionLogger;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ConnectScreen extends Screen {
   private static final AtomicInteger CONNECTOR_THREADS_COUNT = new AtomicInteger(0);
   private static final Logger LOGGER = LogManager.getLogger();
   private ClientConnection connection;
   private boolean connectingCancelled;
   private final Screen parent;
   private Text status = new TranslatableText("connect.connecting", new Object[0]);
   private long narratorTimer = -1L;

   public ConnectScreen(Screen parent, MinecraftClient client, ServerInfo entry) {
      super(NarratorManager.EMPTY);
      this.minecraft = client;
      this.parent = parent;
      ServerAddress serverAddress = ServerAddress.parse(entry.address);
      client.disconnect();
      client.setCurrentServerEntry(entry);
      this.connect(serverAddress.getAddress(), serverAddress.getPort());
   }

   public ConnectScreen(Screen parent, MinecraftClient client, String address, int port) {
      super(NarratorManager.EMPTY);
      this.minecraft = client;
      this.parent = parent;
      client.disconnect();
      this.connect(address, port);
   }

   private void connect(final String address, final int port) {
      LOGGER.info("Connecting to {}, {}", address, port);
      Thread thread = new Thread("Server Connector #" + CONNECTOR_THREADS_COUNT.incrementAndGet()) {
         public void run() {
            InetAddress inetAddress = null;

            try {
               if (ConnectScreen.this.connectingCancelled) {
                  return;
               }

               inetAddress = InetAddress.getByName(address);
               ConnectScreen.this.connection = ClientConnection.connect(inetAddress, port, ConnectScreen.this.minecraft.options.shouldUseNativeTransport());
               ConnectScreen.this.connection.setPacketListener(new ClientLoginNetworkHandler(ConnectScreen.this.connection, ConnectScreen.this.minecraft, ConnectScreen.this.parent, (text) -> {
                  ConnectScreen.this.setStatus(text);
               }));
               ConnectScreen.this.connection.send(new HandshakeC2SPacket(address, port, NetworkState.LOGIN));
               ConnectScreen.this.connection.send(new LoginHelloC2SPacket(ConnectScreen.this.minecraft.getSession().getProfile()));
            } catch (UnknownHostException var4) {
               if (ConnectScreen.this.connectingCancelled) {
                  return;
               }

               ConnectScreen.LOGGER.error("Couldn't connect to server", var4);
               ConnectScreen.this.minecraft.execute(() -> {
                  ConnectScreen.this.minecraft.openScreen(new DisconnectedScreen(ConnectScreen.this.parent, "connect.failed", new TranslatableText("disconnect.genericReason", new Object[]{"Unknown host"})));
               });
            } catch (Exception var5) {
               if (ConnectScreen.this.connectingCancelled) {
                  return;
               }

               ConnectScreen.LOGGER.error("Couldn't connect to server", var5);
               String string = inetAddress == null ? var5.toString() : var5.toString().replaceAll(inetAddress + ":" + port, "");
               ConnectScreen.this.minecraft.execute(() -> {
                  ConnectScreen.this.minecraft.openScreen(new DisconnectedScreen(ConnectScreen.this.parent, "connect.failed", new TranslatableText("disconnect.genericReason", new Object[]{string})));
               });
            }

         }
      };
      thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
      thread.start();
   }

   private void setStatus(Text status) {
      this.status = status;
   }

   public void tick() {
      if (this.connection != null) {
         if (this.connection.isOpen()) {
            this.connection.tick();
         } else {
            this.connection.handleDisconnection();
         }
      }

   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   protected void init() {
      this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20, I18n.translate("gui.cancel"), (buttonWidget) -> {
         this.connectingCancelled = true;
         if (this.connection != null) {
            this.connection.disconnect(new TranslatableText("connect.aborted", new Object[0]));
         }

         this.minecraft.openScreen(this.parent);
      }));
   }

   public void render(int mouseX, int mouseY, float delta) {
      this.renderBackground();
      long l = Util.getMeasuringTimeMs();
      if (l - this.narratorTimer > 2000L) {
         this.narratorTimer = l;
         NarratorManager.INSTANCE.narrate((new TranslatableText("narrator.joining", new Object[0])).getString());
      }

      this.drawCenteredString(this.font, this.status.asFormattedString(), this.width / 2, this.height / 2 - 50, 16777215);
      super.render(mouseX, mouseY, delta);
   }
}
