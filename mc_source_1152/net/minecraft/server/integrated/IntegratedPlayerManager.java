package net.minecraft.server.integrated;

import com.mojang.authlib.GameProfile;
import java.net.SocketAddress;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class IntegratedPlayerManager extends PlayerManager {
   private CompoundTag userData;

   public IntegratedPlayerManager(IntegratedServer integratedServer) {
      super(integratedServer, 8);
      this.setViewDistance(10);
   }

   protected void savePlayerData(ServerPlayerEntity player) {
      if (player.getName().getString().equals(this.getServer().getUserName())) {
         this.userData = player.toTag(new CompoundTag());
      }

      super.savePlayerData(player);
   }

   public Text checkCanJoin(SocketAddress socketAddress, GameProfile gameProfile) {
      return (Text)(gameProfile.getName().equalsIgnoreCase(this.getServer().getUserName()) && this.getPlayer(gameProfile.getName()) != null ? new TranslatableText("multiplayer.disconnect.name_taken", new Object[0]) : super.checkCanJoin(socketAddress, gameProfile));
   }

   public IntegratedServer getServer() {
      return (IntegratedServer)super.getServer();
   }

   public CompoundTag getUserData() {
      return this.userData;
   }
}
