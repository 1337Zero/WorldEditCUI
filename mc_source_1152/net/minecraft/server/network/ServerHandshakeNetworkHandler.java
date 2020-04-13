package net.minecraft.server.network;

import net.minecraft.SharedConstants;
import net.minecraft.client.network.packet.LoginDisconnectS2CPacket;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.listener.ServerHandshakePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.packet.HandshakeC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class ServerHandshakeNetworkHandler implements ServerHandshakePacketListener {
   private final MinecraftServer server;
   private final ClientConnection client;

   public ServerHandshakeNetworkHandler(MinecraftServer minecraftServer, ClientConnection clientConnection) {
      this.server = minecraftServer;
      this.client = clientConnection;
   }

   public void onHandshake(HandshakeC2SPacket handshakeC2SPacket) {
      switch(handshakeC2SPacket.getIntendedState()) {
      case LOGIN:
         this.client.setState(NetworkState.LOGIN);
         TranslatableText text2;
         if (handshakeC2SPacket.getProtocolVersion() > SharedConstants.getGameVersion().getProtocolVersion()) {
            text2 = new TranslatableText("multiplayer.disconnect.outdated_server", new Object[]{SharedConstants.getGameVersion().getName()});
            this.client.send(new LoginDisconnectS2CPacket(text2));
            this.client.disconnect(text2);
         } else if (handshakeC2SPacket.getProtocolVersion() < SharedConstants.getGameVersion().getProtocolVersion()) {
            text2 = new TranslatableText("multiplayer.disconnect.outdated_client", new Object[]{SharedConstants.getGameVersion().getName()});
            this.client.send(new LoginDisconnectS2CPacket(text2));
            this.client.disconnect(text2);
         } else {
            this.client.setPacketListener(new ServerLoginNetworkHandler(this.server, this.client));
         }
         break;
      case STATUS:
         this.client.setState(NetworkState.STATUS);
         this.client.setPacketListener(new ServerQueryNetworkHandler(this.server, this.client));
         break;
      default:
         throw new UnsupportedOperationException("Invalid intention " + handshakeC2SPacket.getIntendedState());
      }

   }

   public void onDisconnected(Text reason) {
   }

   public ClientConnection getConnection() {
      return this.client;
   }
}
