package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerNetworkIo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LegacyQueryHandler extends ChannelInboundHandlerAdapter {
   private static final Logger LOGGER = LogManager.getLogger();
   private final ServerNetworkIo networkIo;

   public LegacyQueryHandler(ServerNetworkIo networkIo) {
      this.networkIo = networkIo;
   }

   public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
      ByteBuf byteBuf = (ByteBuf)object;
      byteBuf.markReaderIndex();
      boolean bl = true;

      try {
         if (byteBuf.readUnsignedByte() == 254) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress)channelHandlerContext.channel().remoteAddress();
            MinecraftServer minecraftServer = this.networkIo.getServer();
            int i = byteBuf.readableBytes();
            String string2;
            switch(i) {
            case 0:
               LOGGER.debug("Ping: (<1.3.x) from {}:{}", inetSocketAddress.getAddress(), inetSocketAddress.getPort());
               string2 = String.format("%s§%d§%d", minecraftServer.getServerMotd(), minecraftServer.getCurrentPlayerCount(), minecraftServer.getMaxPlayerCount());
               this.reply(channelHandlerContext, this.toBuffer(string2));
               break;
            case 1:
               if (byteBuf.readUnsignedByte() != 1) {
                  return;
               }

               LOGGER.debug("Ping: (1.4-1.5.x) from {}:{}", inetSocketAddress.getAddress(), inetSocketAddress.getPort());
               string2 = String.format("§1\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, minecraftServer.getVersion(), minecraftServer.getServerMotd(), minecraftServer.getCurrentPlayerCount(), minecraftServer.getMaxPlayerCount());
               this.reply(channelHandlerContext, this.toBuffer(string2));
               break;
            default:
               boolean bl2 = byteBuf.readUnsignedByte() == 1;
               bl2 &= byteBuf.readUnsignedByte() == 250;
               bl2 &= "MC|PingHost".equals(new String(byteBuf.readBytes(byteBuf.readShort() * 2).array(), StandardCharsets.UTF_16BE));
               int j = byteBuf.readUnsignedShort();
               bl2 &= byteBuf.readUnsignedByte() >= 73;
               bl2 &= 3 + byteBuf.readBytes(byteBuf.readShort() * 2).array().length + 4 == j;
               bl2 &= byteBuf.readInt() <= 65535;
               bl2 &= byteBuf.readableBytes() == 0;
               if (!bl2) {
                  return;
               }

               LOGGER.debug("Ping: (1.6) from {}:{}", inetSocketAddress.getAddress(), inetSocketAddress.getPort());
               String string3 = String.format("§1\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, minecraftServer.getVersion(), minecraftServer.getServerMotd(), minecraftServer.getCurrentPlayerCount(), minecraftServer.getMaxPlayerCount());
               ByteBuf byteBuf2 = this.toBuffer(string3);

               try {
                  this.reply(channelHandlerContext, byteBuf2);
               } finally {
                  byteBuf2.release();
               }
            }

            byteBuf.release();
            bl = false;
            return;
         }
      } catch (RuntimeException var21) {
         return;
      } finally {
         if (bl) {
            byteBuf.resetReaderIndex();
            channelHandlerContext.channel().pipeline().remove("legacy_query");
            channelHandlerContext.fireChannelRead(object);
         }

      }

   }

   private void reply(ChannelHandlerContext ctx, ByteBuf buf) {
      ctx.pipeline().firstContext().writeAndFlush(buf).addListener(ChannelFutureListener.CLOSE);
   }

   private ByteBuf toBuffer(String s) {
      ByteBuf byteBuf = Unpooled.buffer();
      byteBuf.writeByte(255);
      char[] cs = s.toCharArray();
      byteBuf.writeShort(cs.length);
      char[] var4 = cs;
      int var5 = cs.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         char c = var4[var6];
         byteBuf.writeChar(c);
      }

      return byteBuf;
   }
}
