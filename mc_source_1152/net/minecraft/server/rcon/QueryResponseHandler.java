package net.minecraft.server.rcon;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.Util;

public class QueryResponseHandler extends RconBase {
   private long lastQueryTime;
   private final int queryPort;
   private final int port;
   private final int maxPlayerCount;
   private final String motd;
   private final String levelName;
   private DatagramSocket socket;
   private final byte[] packetBuffer = new byte[1460];
   private DatagramPacket currentPacket;
   private final Map<SocketAddress, String> field_14448;
   private String ip;
   private String hostname;
   private final Map<SocketAddress, QueryResponseHandler.Query> queries;
   private final long creationTime;
   private final DataStreamHelper data;
   private long lastResponseTime;

   public QueryResponseHandler(DedicatedServer server) {
      super(server, "Query Listener");
      this.queryPort = server.getProperties().queryPort;
      this.hostname = server.getHostname();
      this.port = server.getPort();
      this.motd = server.getMotd();
      this.maxPlayerCount = server.getMaxPlayerCount();
      this.levelName = server.getLevelName();
      this.lastResponseTime = 0L;
      this.ip = "0.0.0.0";
      if (!this.hostname.isEmpty() && !this.ip.equals(this.hostname)) {
         this.ip = this.hostname;
      } else {
         this.hostname = "0.0.0.0";

         try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            this.ip = inetAddress.getHostAddress();
         } catch (UnknownHostException var3) {
            this.warn("Unable to determine local host IP, please set server-ip in server.properties: " + var3.getMessage());
         }
      }

      this.field_14448 = Maps.newHashMap();
      this.data = new DataStreamHelper(1460);
      this.queries = Maps.newHashMap();
      this.creationTime = (new Date()).getTime();
   }

   private void reply(byte[] buf, DatagramPacket datagramPacket) throws IOException {
      this.socket.send(new DatagramPacket(buf, buf.length, datagramPacket.getSocketAddress()));
   }

   private boolean handle(DatagramPacket packet) throws IOException {
      byte[] bs = packet.getData();
      int i = packet.getLength();
      SocketAddress socketAddress = packet.getSocketAddress();
      this.log("Packet len " + i + " [" + socketAddress + "]");
      if (3 <= i && -2 == bs[0] && -3 == bs[1]) {
         this.log("Packet '" + BufferHelper.toHex(bs[2]) + "' [" + socketAddress + "]");
         switch(bs[2]) {
         case 0:
            if (!this.isValidQuery(packet)) {
               this.log("Invalid challenge [" + socketAddress + "]");
               return false;
            } else if (15 == i) {
               this.reply(this.createRulesReply(packet), packet);
               this.log("Rules [" + socketAddress + "]");
            } else {
               DataStreamHelper dataStreamHelper = new DataStreamHelper(1460);
               dataStreamHelper.write(0);
               dataStreamHelper.write(this.getMessageBytes(packet.getSocketAddress()));
               dataStreamHelper.writeBytes(this.motd);
               dataStreamHelper.writeBytes("SMP");
               dataStreamHelper.writeBytes(this.levelName);
               dataStreamHelper.writeBytes(Integer.toString(this.getCurrentPlayerCount()));
               dataStreamHelper.writeBytes(Integer.toString(this.maxPlayerCount));
               dataStreamHelper.writeShort((short)this.port);
               dataStreamHelper.writeBytes(this.ip);
               this.reply(dataStreamHelper.bytes(), packet);
               this.log("Status [" + socketAddress + "]");
            }
         default:
            return true;
         case 9:
            this.createQuery(packet);
            this.log("Challenge [" + socketAddress + "]");
            return true;
         }
      } else {
         this.log("Invalid packet [" + socketAddress + "]");
         return false;
      }
   }

   private byte[] createRulesReply(DatagramPacket packet) throws IOException {
      long l = Util.getMeasuringTimeMs();
      if (l < this.lastResponseTime + 5000L) {
         byte[] bs = this.data.bytes();
         byte[] cs = this.getMessageBytes(packet.getSocketAddress());
         bs[1] = cs[0];
         bs[2] = cs[1];
         bs[3] = cs[2];
         bs[4] = cs[3];
         return bs;
      } else {
         this.lastResponseTime = l;
         this.data.reset();
         this.data.write(0);
         this.data.write(this.getMessageBytes(packet.getSocketAddress()));
         this.data.writeBytes("splitnum");
         this.data.write(128);
         this.data.write(0);
         this.data.writeBytes("hostname");
         this.data.writeBytes(this.motd);
         this.data.writeBytes("gametype");
         this.data.writeBytes("SMP");
         this.data.writeBytes("game_id");
         this.data.writeBytes("MINECRAFT");
         this.data.writeBytes("version");
         this.data.writeBytes(this.server.getVersion());
         this.data.writeBytes("plugins");
         this.data.writeBytes(this.server.getPlugins());
         this.data.writeBytes("map");
         this.data.writeBytes(this.levelName);
         this.data.writeBytes("numplayers");
         this.data.writeBytes("" + this.getCurrentPlayerCount());
         this.data.writeBytes("maxplayers");
         this.data.writeBytes("" + this.maxPlayerCount);
         this.data.writeBytes("hostport");
         this.data.writeBytes("" + this.port);
         this.data.writeBytes("hostip");
         this.data.writeBytes(this.ip);
         this.data.write(0);
         this.data.write(1);
         this.data.writeBytes("player_");
         this.data.write(0);
         String[] strings = this.server.getPlayerNames();
         String[] var5 = strings;
         int var6 = strings.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            String string = var5[var7];
            this.data.writeBytes(string);
         }

         this.data.write(0);
         return this.data.bytes();
      }
   }

   private byte[] getMessageBytes(SocketAddress socketAddress) {
      return ((QueryResponseHandler.Query)this.queries.get(socketAddress)).getMessageBytes();
   }

   private Boolean isValidQuery(DatagramPacket datagramPacket) {
      SocketAddress socketAddress = datagramPacket.getSocketAddress();
      if (!this.queries.containsKey(socketAddress)) {
         return false;
      } else {
         byte[] bs = datagramPacket.getData();
         return ((QueryResponseHandler.Query)this.queries.get(socketAddress)).getId() != BufferHelper.getIntBE(bs, 7, datagramPacket.getLength()) ? false : true;
      }
   }

   private void createQuery(DatagramPacket datagramPacket) throws IOException {
      QueryResponseHandler.Query query = new QueryResponseHandler.Query(datagramPacket);
      this.queries.put(datagramPacket.getSocketAddress(), query);
      this.reply(query.getReplyBuf(), datagramPacket);
   }

   private void cleanUp() {
      if (this.running) {
         long l = Util.getMeasuringTimeMs();
         if (l >= this.lastQueryTime + 30000L) {
            this.lastQueryTime = l;
            Iterator iterator = this.queries.entrySet().iterator();

            while(iterator.hasNext()) {
               Entry<SocketAddress, QueryResponseHandler.Query> entry = (Entry)iterator.next();
               if (((QueryResponseHandler.Query)entry.getValue()).startedBefore(l)) {
                  iterator.remove();
               }
            }

         }
      }
   }

   public void run() {
      this.info("Query running on " + this.hostname + ":" + this.queryPort);
      this.lastQueryTime = Util.getMeasuringTimeMs();
      this.currentPacket = new DatagramPacket(this.packetBuffer, this.packetBuffer.length);

      try {
         while(this.running) {
            try {
               this.socket.receive(this.currentPacket);
               this.cleanUp();
               this.handle(this.currentPacket);
            } catch (SocketTimeoutException var7) {
               this.cleanUp();
            } catch (PortUnreachableException var8) {
            } catch (IOException var9) {
               this.handleIoException(var9);
            }
         }
      } finally {
         this.forceClose();
      }

   }

   public void start() {
      if (!this.running) {
         if (0 < this.queryPort && 65535 >= this.queryPort) {
            if (this.initialize()) {
               super.start();
            }

         } else {
            this.warn("Invalid query port " + this.queryPort + " found in server.properties (queries disabled)");
         }
      }
   }

   private void handleIoException(Exception e) {
      if (this.running) {
         this.warn("Unexpected exception, buggy JRE? (" + e + ")");
         if (!this.initialize()) {
            this.logError("Failed to recover from buggy JRE, shutting down!");
            this.running = false;
         }

      }
   }

   private boolean initialize() {
      try {
         this.socket = new DatagramSocket(this.queryPort, InetAddress.getByName(this.hostname));
         this.registerSocket(this.socket);
         this.socket.setSoTimeout(500);
         return true;
      } catch (SocketException var2) {
         this.warn("Unable to initialise query system on " + this.hostname + ":" + this.queryPort + " (Socket): " + var2.getMessage());
      } catch (UnknownHostException var3) {
         this.warn("Unable to initialise query system on " + this.hostname + ":" + this.queryPort + " (Unknown Host): " + var3.getMessage());
      } catch (Exception var4) {
         this.warn("Unable to initialise query system on " + this.hostname + ":" + this.queryPort + " (E): " + var4.getMessage());
      }

      return false;
   }

   class Query {
      private final long startTime = (new Date()).getTime();
      private final int id;
      private final byte[] messageBytes;
      private final byte[] replyBuf;
      private final String message;

      public Query(DatagramPacket datagramPacket) {
         byte[] bs = datagramPacket.getData();
         this.messageBytes = new byte[4];
         this.messageBytes[0] = bs[3];
         this.messageBytes[1] = bs[4];
         this.messageBytes[2] = bs[5];
         this.messageBytes[3] = bs[6];
         this.message = new String(this.messageBytes, StandardCharsets.UTF_8);
         this.id = (new Random()).nextInt(16777216);
         this.replyBuf = String.format("\t%s%d\u0000", this.message, this.id).getBytes(StandardCharsets.UTF_8);
      }

      public Boolean startedBefore(long lastQueryTime) {
         return this.startTime < lastQueryTime;
      }

      public int getId() {
         return this.id;
      }

      public byte[] getReplyBuf() {
         return this.replyBuf;
      }

      public byte[] getMessageBytes() {
         return this.messageBytes;
      }
   }
}
