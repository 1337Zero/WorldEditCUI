package net.minecraft.server.dedicated;

import com.mojang.authlib.GameProfile;
import java.io.IOException;
import net.minecraft.server.PlayerManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DedicatedPlayerManager extends PlayerManager {
   private static final Logger LOGGER = LogManager.getLogger();

   public DedicatedPlayerManager(MinecraftDedicatedServer minecraftDedicatedServer) {
      super(minecraftDedicatedServer, minecraftDedicatedServer.getProperties().maxPlayers);
      ServerPropertiesHandler serverPropertiesHandler = minecraftDedicatedServer.getProperties();
      this.setViewDistance(serverPropertiesHandler.viewDistance);
      super.setWhitelistEnabled((Boolean)serverPropertiesHandler.whiteList.get());
      if (!minecraftDedicatedServer.isSinglePlayer()) {
         this.getUserBanList().setEnabled(true);
         this.getIpBanList().setEnabled(true);
      }

      this.loadUserBanList();
      this.saveUserBanList();
      this.loadIpBanList();
      this.saveIpBanList();
      this.loadOpList();
      this.loadWhitelist();
      this.saveOpList();
      if (!this.getWhitelist().getFile().exists()) {
         this.saveWhitelist();
      }

   }

   public void setWhitelistEnabled(boolean whitelistEnabled) {
      super.setWhitelistEnabled(whitelistEnabled);
      this.getServer().setUseWhitelist(whitelistEnabled);
   }

   public void addToOperators(GameProfile gameProfile) {
      super.addToOperators(gameProfile);
      this.saveOpList();
   }

   public void removeFromOperators(GameProfile gameProfile) {
      super.removeFromOperators(gameProfile);
      this.saveOpList();
   }

   public void reloadWhitelist() {
      this.loadWhitelist();
   }

   private void saveIpBanList() {
      try {
         this.getIpBanList().save();
      } catch (IOException var2) {
         LOGGER.warn("Failed to save ip banlist: ", var2);
      }

   }

   private void saveUserBanList() {
      try {
         this.getUserBanList().save();
      } catch (IOException var2) {
         LOGGER.warn("Failed to save user banlist: ", var2);
      }

   }

   private void loadIpBanList() {
      try {
         this.getIpBanList().load();
      } catch (IOException var2) {
         LOGGER.warn("Failed to load ip banlist: ", var2);
      }

   }

   private void loadUserBanList() {
      try {
         this.getUserBanList().load();
      } catch (IOException var2) {
         LOGGER.warn("Failed to load user banlist: ", var2);
      }

   }

   private void loadOpList() {
      try {
         this.getOpList().load();
      } catch (Exception var2) {
         LOGGER.warn("Failed to load operators list: ", var2);
      }

   }

   private void saveOpList() {
      try {
         this.getOpList().save();
      } catch (Exception var2) {
         LOGGER.warn("Failed to save operators list: ", var2);
      }

   }

   private void loadWhitelist() {
      try {
         this.getWhitelist().load();
      } catch (Exception var2) {
         LOGGER.warn("Failed to load white-list: ", var2);
      }

   }

   private void saveWhitelist() {
      try {
         this.getWhitelist().save();
      } catch (Exception var2) {
         LOGGER.warn("Failed to save white-list: ", var2);
      }

   }

   public boolean isWhitelisted(GameProfile gameProfile) {
      return !this.isWhitelistEnabled() || this.isOperator(gameProfile) || this.getWhitelist().isAllowed(gameProfile);
   }

   public MinecraftDedicatedServer getServer() {
      return (MinecraftDedicatedServer)super.getServer();
   }

   public boolean canBypassPlayerLimit(GameProfile gameProfile) {
      return this.getOpList().isOp(gameProfile);
   }
}
