package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.client.network.packet.ChatMessageS2CPacket;
import net.minecraft.client.network.packet.ChunkLoadDistanceS2CPacket;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.client.network.packet.DifficultyS2CPacket;
import net.minecraft.client.network.packet.EntityPotionEffectS2CPacket;
import net.minecraft.client.network.packet.EntityStatusS2CPacket;
import net.minecraft.client.network.packet.ExperienceBarUpdateS2CPacket;
import net.minecraft.client.network.packet.GameJoinS2CPacket;
import net.minecraft.client.network.packet.GameStateChangeS2CPacket;
import net.minecraft.client.network.packet.HeldItemChangeS2CPacket;
import net.minecraft.client.network.packet.PlayerAbilitiesS2CPacket;
import net.minecraft.client.network.packet.PlayerListS2CPacket;
import net.minecraft.client.network.packet.PlayerRespawnS2CPacket;
import net.minecraft.client.network.packet.PlayerSpawnPositionS2CPacket;
import net.minecraft.client.network.packet.SynchronizeRecipesS2CPacket;
import net.minecraft.client.network.packet.SynchronizeTagsS2CPacket;
import net.minecraft.client.network.packet.TeamS2CPacket;
import net.minecraft.client.network.packet.WorldBorderS2CPacket;
import net.minecraft.client.network.packet.WorldTimeUpdateS2CPacket;
import net.minecraft.container.Container;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.MessageType;
import net.minecraft.network.Packet;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.DemoServerPlayerInteractionManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.UserCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.PlayerSaveHandler;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class PlayerManager {
   public static final File BANNED_PLAYERS_FILE = new File("banned-players.json");
   public static final File BANNED_IPS_FILE = new File("banned-ips.json");
   public static final File OPERATORS_FILE = new File("ops.json");
   public static final File WHITELIST_FILE = new File("whitelist.json");
   private static final Logger LOGGER = LogManager.getLogger();
   private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
   private final MinecraftServer server;
   private final List<ServerPlayerEntity> players = Lists.newArrayList();
   private final Map<UUID, ServerPlayerEntity> playerMap = Maps.newHashMap();
   private final BannedPlayerList bannedProfiles;
   private final BannedIpList bannedIps;
   private final OperatorList ops;
   private final Whitelist whitelist;
   private final Map<UUID, ServerStatHandler> statisticsMap;
   private final Map<UUID, PlayerAdvancementTracker> advancementTrackers;
   private PlayerSaveHandler saveHandler;
   private boolean whitelistEnabled;
   protected final int maxPlayers;
   private int viewDistance;
   private GameMode gameMode;
   private boolean cheatsAllowed;
   private int latencyUpdateTimer;

   public PlayerManager(MinecraftServer server, int maxPlayers) {
      this.bannedProfiles = new BannedPlayerList(BANNED_PLAYERS_FILE);
      this.bannedIps = new BannedIpList(BANNED_IPS_FILE);
      this.ops = new OperatorList(OPERATORS_FILE);
      this.whitelist = new Whitelist(WHITELIST_FILE);
      this.statisticsMap = Maps.newHashMap();
      this.advancementTrackers = Maps.newHashMap();
      this.server = server;
      this.maxPlayers = maxPlayers;
      this.getUserBanList().setEnabled(true);
      this.getIpBanList().setEnabled(true);
   }

   public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player) {
      GameProfile gameProfile = player.getGameProfile();
      UserCache userCache = this.server.getUserCache();
      GameProfile gameProfile2 = userCache.getByUuid(gameProfile.getId());
      String string = gameProfile2 == null ? gameProfile.getName() : gameProfile2.getName();
      userCache.add(gameProfile);
      CompoundTag compoundTag = this.loadPlayerData(player);
      ServerWorld serverWorld = this.server.getWorld(player.dimension);
      player.setWorld(serverWorld);
      player.interactionManager.setWorld((ServerWorld)player.world);
      String string2 = "local";
      if (connection.getAddress() != null) {
         string2 = connection.getAddress().toString();
      }

      LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", player.getName().getString(), string2, player.getEntityId(), player.getX(), player.getY(), player.getZ());
      LevelProperties levelProperties = serverWorld.getLevelProperties();
      this.setGameMode(player, (ServerPlayerEntity)null, serverWorld);
      ServerPlayNetworkHandler serverPlayNetworkHandler = new ServerPlayNetworkHandler(this.server, connection, player);
      GameRules gameRules = serverWorld.getGameRules();
      boolean bl = gameRules.getBoolean(GameRules.DO_IMMEDIATE_RESPAWN);
      boolean bl2 = gameRules.getBoolean(GameRules.REDUCED_DEBUG_INFO);
      serverPlayNetworkHandler.sendPacket(new GameJoinS2CPacket(player.getEntityId(), player.interactionManager.getGameMode(), LevelProperties.sha256Hash(levelProperties.getSeed()), levelProperties.isHardcore(), serverWorld.dimension.getType(), this.getMaxPlayerCount(), levelProperties.getGeneratorType(), this.viewDistance, bl2, !bl));
      serverPlayNetworkHandler.sendPacket(new CustomPayloadS2CPacket(CustomPayloadS2CPacket.BRAND, (new PacketByteBuf(Unpooled.buffer())).writeString(this.getServer().getServerModName())));
      serverPlayNetworkHandler.sendPacket(new DifficultyS2CPacket(levelProperties.getDifficulty(), levelProperties.isDifficultyLocked()));
      serverPlayNetworkHandler.sendPacket(new PlayerAbilitiesS2CPacket(player.abilities));
      serverPlayNetworkHandler.sendPacket(new HeldItemChangeS2CPacket(player.inventory.selectedSlot));
      serverPlayNetworkHandler.sendPacket(new SynchronizeRecipesS2CPacket(this.server.getRecipeManager().values()));
      serverPlayNetworkHandler.sendPacket(new SynchronizeTagsS2CPacket(this.server.getTagManager()));
      this.sendCommandTree(player);
      player.getStatHandler().updateStatSet();
      player.getRecipeBook().sendInitRecipesPacket(player);
      this.sendScoreboard(serverWorld.getScoreboard(), player);
      this.server.forcePlayerSampleUpdate();
      TranslatableText text2;
      if (player.getGameProfile().getName().equalsIgnoreCase(string)) {
         text2 = new TranslatableText("multiplayer.player.joined", new Object[]{player.getDisplayName()});
      } else {
         text2 = new TranslatableText("multiplayer.player.joined.renamed", new Object[]{player.getDisplayName(), string});
      }

      this.sendToAll(text2.formatted(Formatting.YELLOW));
      serverPlayNetworkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(), player.yaw, player.pitch);
      this.players.add(player);
      this.playerMap.put(player.getUuid(), player);
      this.sendToAll((Packet)(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, new ServerPlayerEntity[]{player})));

      for(int i = 0; i < this.players.size(); ++i) {
         player.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, new ServerPlayerEntity[]{(ServerPlayerEntity)this.players.get(i)}));
      }

      serverWorld.onPlayerConnected(player);
      this.server.getBossBarManager().onPlayerConnect(player);
      this.sendWorldInfo(player, serverWorld);
      if (!this.server.getResourcePackUrl().isEmpty()) {
         player.sendResourcePackUrl(this.server.getResourcePackUrl(), this.server.getResourcePackHash());
      }

      Iterator var21 = player.getStatusEffects().iterator();

      while(var21.hasNext()) {
         StatusEffectInstance statusEffectInstance = (StatusEffectInstance)var21.next();
         serverPlayNetworkHandler.sendPacket(new EntityPotionEffectS2CPacket(player.getEntityId(), statusEffectInstance));
      }

      if (compoundTag != null && compoundTag.contains("RootVehicle", 10)) {
         CompoundTag compoundTag2 = compoundTag.getCompound("RootVehicle");
         Entity entity = EntityType.loadEntityWithPassengers(compoundTag2.getCompound("Entity"), serverWorld, (entityx) -> {
            return !serverWorld.tryLoadEntity(entityx) ? null : entityx;
         });
         if (entity != null) {
            UUID uUID = compoundTag2.getUuid("Attach");
            Iterator var19;
            Entity entity3;
            if (entity.getUuid().equals(uUID)) {
               player.startRiding(entity, true);
            } else {
               var19 = entity.getPassengersDeep().iterator();

               while(var19.hasNext()) {
                  entity3 = (Entity)var19.next();
                  if (entity3.getUuid().equals(uUID)) {
                     player.startRiding(entity3, true);
                     break;
                  }
               }
            }

            if (!player.hasVehicle()) {
               LOGGER.warn("Couldn't reattach entity to player");
               serverWorld.removeEntity(entity);
               var19 = entity.getPassengersDeep().iterator();

               while(var19.hasNext()) {
                  entity3 = (Entity)var19.next();
                  serverWorld.removeEntity(entity3);
               }
            }
         }
      }

      player.method_14235();
   }

   protected void sendScoreboard(ServerScoreboard scoreboard, ServerPlayerEntity player) {
      Set<ScoreboardObjective> set = Sets.newHashSet();
      Iterator var4 = scoreboard.getTeams().iterator();

      while(var4.hasNext()) {
         Team team = (Team)var4.next();
         player.networkHandler.sendPacket(new TeamS2CPacket(team, 0));
      }

      for(int i = 0; i < 19; ++i) {
         ScoreboardObjective scoreboardObjective = scoreboard.getObjectiveForSlot(i);
         if (scoreboardObjective != null && !set.contains(scoreboardObjective)) {
            List<Packet<?>> list = scoreboard.createChangePackets(scoreboardObjective);
            Iterator var7 = list.iterator();

            while(var7.hasNext()) {
               Packet<?> packet = (Packet)var7.next();
               player.networkHandler.sendPacket(packet);
            }

            set.add(scoreboardObjective);
         }
      }

   }

   public void setMainWorld(ServerWorld world) {
      this.saveHandler = world.getSaveHandler();
      world.getWorldBorder().addListener(new WorldBorderListener() {
         public void onSizeChange(WorldBorder worldBorder, double d) {
            PlayerManager.this.sendToAll((Packet)(new WorldBorderS2CPacket(worldBorder, WorldBorderS2CPacket.Type.SET_SIZE)));
         }

         public void onInterpolateSize(WorldBorder border, double fromSize, double toSize, long time) {
            PlayerManager.this.sendToAll((Packet)(new WorldBorderS2CPacket(border, WorldBorderS2CPacket.Type.LERP_SIZE)));
         }

         public void onCenterChanged(WorldBorder centerX, double centerZ, double d) {
            PlayerManager.this.sendToAll((Packet)(new WorldBorderS2CPacket(centerX, WorldBorderS2CPacket.Type.SET_CENTER)));
         }

         public void onWarningTimeChanged(WorldBorder warningTime, int i) {
            PlayerManager.this.sendToAll((Packet)(new WorldBorderS2CPacket(warningTime, WorldBorderS2CPacket.Type.SET_WARNING_TIME)));
         }

         public void onWarningBlocksChanged(WorldBorder warningBlocks, int i) {
            PlayerManager.this.sendToAll((Packet)(new WorldBorderS2CPacket(warningBlocks, WorldBorderS2CPacket.Type.SET_WARNING_BLOCKS)));
         }

         public void onDamagePerBlockChanged(WorldBorder damagePerBlock, double d) {
         }

         public void onSafeZoneChanged(WorldBorder safeZoneRadius, double d) {
         }
      });
   }

   @Nullable
   public CompoundTag loadPlayerData(ServerPlayerEntity player) {
      CompoundTag compoundTag = this.server.getWorld(DimensionType.OVERWORLD).getLevelProperties().getPlayerData();
      CompoundTag compoundTag3;
      if (player.getName().getString().equals(this.server.getUserName()) && compoundTag != null) {
         compoundTag3 = compoundTag;
         player.fromTag(compoundTag);
         LOGGER.debug("loading single player");
      } else {
         compoundTag3 = this.saveHandler.loadPlayerData(player);
      }

      return compoundTag3;
   }

   protected void savePlayerData(ServerPlayerEntity player) {
      this.saveHandler.savePlayerData(player);
      ServerStatHandler serverStatHandler = (ServerStatHandler)this.statisticsMap.get(player.getUuid());
      if (serverStatHandler != null) {
         serverStatHandler.save();
      }

      PlayerAdvancementTracker playerAdvancementTracker = (PlayerAdvancementTracker)this.advancementTrackers.get(player.getUuid());
      if (playerAdvancementTracker != null) {
         playerAdvancementTracker.save();
      }

   }

   public void remove(ServerPlayerEntity player) {
      ServerWorld serverWorld = player.getServerWorld();
      player.incrementStat(Stats.LEAVE_GAME);
      this.savePlayerData(player);
      if (player.hasVehicle()) {
         Entity entity = player.getRootVehicle();
         if (entity.hasPlayerRider()) {
            LOGGER.debug("Removing player mount");
            player.stopRiding();
            serverWorld.removeEntity(entity);
            Iterator var4 = entity.getPassengersDeep().iterator();

            while(var4.hasNext()) {
               Entity entity2 = (Entity)var4.next();
               serverWorld.removeEntity(entity2);
            }

            serverWorld.getChunk(player.chunkX, player.chunkZ).markDirty();
         }
      }

      player.detach();
      serverWorld.removePlayer(player);
      player.getAdvancementTracker().clearCriterions();
      this.players.remove(player);
      this.server.getBossBarManager().onPlayerDisconnenct(player);
      UUID uUID = player.getUuid();
      ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)this.playerMap.get(uUID);
      if (serverPlayerEntity == player) {
         this.playerMap.remove(uUID);
         this.statisticsMap.remove(uUID);
         this.advancementTrackers.remove(uUID);
      }

      this.sendToAll((Packet)(new PlayerListS2CPacket(PlayerListS2CPacket.Action.REMOVE_PLAYER, new ServerPlayerEntity[]{player})));
   }

   @Nullable
   public Text checkCanJoin(SocketAddress socketAddress, GameProfile gameProfile) {
      TranslatableText text2;
      if (this.bannedProfiles.contains(gameProfile)) {
         BannedPlayerEntry bannedPlayerEntry = (BannedPlayerEntry)this.bannedProfiles.get(gameProfile);
         text2 = new TranslatableText("multiplayer.disconnect.banned.reason", new Object[]{bannedPlayerEntry.getReason()});
         if (bannedPlayerEntry.getExpiryDate() != null) {
            text2.append((Text)(new TranslatableText("multiplayer.disconnect.banned.expiration", new Object[]{DATE_FORMATTER.format(bannedPlayerEntry.getExpiryDate())})));
         }

         return text2;
      } else if (!this.isWhitelisted(gameProfile)) {
         return new TranslatableText("multiplayer.disconnect.not_whitelisted", new Object[0]);
      } else if (this.bannedIps.isBanned(socketAddress)) {
         BannedIpEntry bannedIpEntry = this.bannedIps.get(socketAddress);
         text2 = new TranslatableText("multiplayer.disconnect.banned_ip.reason", new Object[]{bannedIpEntry.getReason()});
         if (bannedIpEntry.getExpiryDate() != null) {
            text2.append((Text)(new TranslatableText("multiplayer.disconnect.banned_ip.expiration", new Object[]{DATE_FORMATTER.format(bannedIpEntry.getExpiryDate())})));
         }

         return text2;
      } else {
         return this.players.size() >= this.maxPlayers && !this.canBypassPlayerLimit(gameProfile) ? new TranslatableText("multiplayer.disconnect.server_full", new Object[0]) : null;
      }
   }

   public ServerPlayerEntity createPlayer(GameProfile profile) {
      UUID uUID = PlayerEntity.getUuidFromProfile(profile);
      List<ServerPlayerEntity> list = Lists.newArrayList();

      for(int i = 0; i < this.players.size(); ++i) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)this.players.get(i);
         if (serverPlayerEntity.getUuid().equals(uUID)) {
            list.add(serverPlayerEntity);
         }
      }

      ServerPlayerEntity serverPlayerEntity2 = (ServerPlayerEntity)this.playerMap.get(profile.getId());
      if (serverPlayerEntity2 != null && !list.contains(serverPlayerEntity2)) {
         list.add(serverPlayerEntity2);
      }

      Iterator var8 = list.iterator();

      while(var8.hasNext()) {
         ServerPlayerEntity serverPlayerEntity3 = (ServerPlayerEntity)var8.next();
         serverPlayerEntity3.networkHandler.disconnect(new TranslatableText("multiplayer.disconnect.duplicate_login", new Object[0]));
      }

      Object serverPlayerInteractionManager2;
      if (this.server.isDemo()) {
         serverPlayerInteractionManager2 = new DemoServerPlayerInteractionManager(this.server.getWorld(DimensionType.OVERWORLD));
      } else {
         serverPlayerInteractionManager2 = new ServerPlayerInteractionManager(this.server.getWorld(DimensionType.OVERWORLD));
      }

      return new ServerPlayerEntity(this.server, this.server.getWorld(DimensionType.OVERWORLD), profile, (ServerPlayerInteractionManager)serverPlayerInteractionManager2);
   }

   public ServerPlayerEntity respawnPlayer(ServerPlayerEntity player, DimensionType dimension, boolean alive) {
      this.players.remove(player);
      player.getServerWorld().removePlayer(player);
      BlockPos blockPos = player.getSpawnPosition();
      boolean bl = player.isSpawnForced();
      player.dimension = dimension;
      Object serverPlayerInteractionManager2;
      if (this.server.isDemo()) {
         serverPlayerInteractionManager2 = new DemoServerPlayerInteractionManager(this.server.getWorld(player.dimension));
      } else {
         serverPlayerInteractionManager2 = new ServerPlayerInteractionManager(this.server.getWorld(player.dimension));
      }

      ServerPlayerEntity serverPlayerEntity = new ServerPlayerEntity(this.server, this.server.getWorld(player.dimension), player.getGameProfile(), (ServerPlayerInteractionManager)serverPlayerInteractionManager2);
      serverPlayerEntity.networkHandler = player.networkHandler;
      serverPlayerEntity.copyFrom(player, alive);
      serverPlayerEntity.setEntityId(player.getEntityId());
      serverPlayerEntity.setMainArm(player.getMainArm());
      Iterator var8 = player.getScoreboardTags().iterator();

      while(var8.hasNext()) {
         String string = (String)var8.next();
         serverPlayerEntity.addScoreboardTag(string);
      }

      ServerWorld serverWorld = this.server.getWorld(player.dimension);
      this.setGameMode(serverPlayerEntity, player, serverWorld);
      if (blockPos != null) {
         Optional<Vec3d> optional = PlayerEntity.findRespawnPosition(this.server.getWorld(player.dimension), blockPos, bl);
         if (optional.isPresent()) {
            Vec3d vec3d = (Vec3d)optional.get();
            serverPlayerEntity.refreshPositionAndAngles(vec3d.x, vec3d.y, vec3d.z, 0.0F, 0.0F);
            serverPlayerEntity.setPlayerSpawn(blockPos, bl, false);
         } else {
            serverPlayerEntity.networkHandler.sendPacket(new GameStateChangeS2CPacket(0, 0.0F));
         }
      }

      while(!serverWorld.doesNotCollide(serverPlayerEntity) && serverPlayerEntity.getY() < 256.0D) {
         serverPlayerEntity.updatePosition(serverPlayerEntity.getX(), serverPlayerEntity.getY() + 1.0D, serverPlayerEntity.getZ());
      }

      LevelProperties levelProperties = serverPlayerEntity.world.getLevelProperties();
      serverPlayerEntity.networkHandler.sendPacket(new PlayerRespawnS2CPacket(serverPlayerEntity.dimension, LevelProperties.sha256Hash(levelProperties.getSeed()), levelProperties.getGeneratorType(), serverPlayerEntity.interactionManager.getGameMode()));
      BlockPos blockPos2 = serverWorld.getSpawnPos();
      serverPlayerEntity.networkHandler.requestTeleport(serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ(), serverPlayerEntity.yaw, serverPlayerEntity.pitch);
      serverPlayerEntity.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(blockPos2));
      serverPlayerEntity.networkHandler.sendPacket(new DifficultyS2CPacket(levelProperties.getDifficulty(), levelProperties.isDifficultyLocked()));
      serverPlayerEntity.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(serverPlayerEntity.experienceProgress, serverPlayerEntity.totalExperience, serverPlayerEntity.experienceLevel));
      this.sendWorldInfo(serverPlayerEntity, serverWorld);
      this.sendCommandTree(serverPlayerEntity);
      serverWorld.onPlayerRespawned(serverPlayerEntity);
      this.players.add(serverPlayerEntity);
      this.playerMap.put(serverPlayerEntity.getUuid(), serverPlayerEntity);
      serverPlayerEntity.method_14235();
      serverPlayerEntity.setHealth(serverPlayerEntity.getHealth());
      return serverPlayerEntity;
   }

   public void sendCommandTree(ServerPlayerEntity player) {
      GameProfile gameProfile = player.getGameProfile();
      int i = this.server.getPermissionLevel(gameProfile);
      this.sendCommandTree(player, i);
   }

   public void updatePlayerLatency() {
      if (++this.latencyUpdateTimer > 600) {
         this.sendToAll((Packet)(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_LATENCY, this.players)));
         this.latencyUpdateTimer = 0;
      }

   }

   public void sendToAll(Packet<?> packet) {
      for(int i = 0; i < this.players.size(); ++i) {
         ((ServerPlayerEntity)this.players.get(i)).networkHandler.sendPacket(packet);
      }

   }

   public void sendToDimension(Packet<?> packet, DimensionType dimension) {
      for(int i = 0; i < this.players.size(); ++i) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)this.players.get(i);
         if (serverPlayerEntity.dimension == dimension) {
            serverPlayerEntity.networkHandler.sendPacket(packet);
         }
      }

   }

   public void sendToTeam(PlayerEntity source, Text text) {
      AbstractTeam abstractTeam = source.getScoreboardTeam();
      if (abstractTeam != null) {
         Collection<String> collection = abstractTeam.getPlayerList();
         Iterator var5 = collection.iterator();

         while(var5.hasNext()) {
            String string = (String)var5.next();
            ServerPlayerEntity serverPlayerEntity = this.getPlayer(string);
            if (serverPlayerEntity != null && serverPlayerEntity != source) {
               serverPlayerEntity.sendMessage(text);
            }
         }

      }
   }

   public void sendToOtherTeams(PlayerEntity source, Text text) {
      AbstractTeam abstractTeam = source.getScoreboardTeam();
      if (abstractTeam == null) {
         this.sendToAll(text);
      } else {
         for(int i = 0; i < this.players.size(); ++i) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)this.players.get(i);
            if (serverPlayerEntity.getScoreboardTeam() != abstractTeam) {
               serverPlayerEntity.sendMessage(text);
            }
         }

      }
   }

   public String[] getPlayerNames() {
      String[] strings = new String[this.players.size()];

      for(int i = 0; i < this.players.size(); ++i) {
         strings[i] = ((ServerPlayerEntity)this.players.get(i)).getGameProfile().getName();
      }

      return strings;
   }

   public BannedPlayerList getUserBanList() {
      return this.bannedProfiles;
   }

   public BannedIpList getIpBanList() {
      return this.bannedIps;
   }

   public void addToOperators(GameProfile gameProfile) {
      this.ops.add(new OperatorEntry(gameProfile, this.server.getOpPermissionLevel(), this.ops.isOp(gameProfile)));
      ServerPlayerEntity serverPlayerEntity = this.getPlayer(gameProfile.getId());
      if (serverPlayerEntity != null) {
         this.sendCommandTree(serverPlayerEntity);
      }

   }

   public void removeFromOperators(GameProfile gameProfile) {
      this.ops.remove(gameProfile);
      ServerPlayerEntity serverPlayerEntity = this.getPlayer(gameProfile.getId());
      if (serverPlayerEntity != null) {
         this.sendCommandTree(serverPlayerEntity);
      }

   }

   private void sendCommandTree(ServerPlayerEntity player, int permissionLevel) {
      if (player.networkHandler != null) {
         byte d;
         if (permissionLevel <= 0) {
            d = 24;
         } else if (permissionLevel >= 4) {
            d = 28;
         } else {
            d = (byte)(24 + permissionLevel);
         }

         player.networkHandler.sendPacket(new EntityStatusS2CPacket(player, d));
      }

      this.server.getCommandManager().sendCommandTree(player);
   }

   public boolean isWhitelisted(GameProfile gameProfile) {
      return !this.whitelistEnabled || this.ops.contains(gameProfile) || this.whitelist.contains(gameProfile);
   }

   public boolean isOperator(GameProfile gameProfile) {
      return this.ops.contains(gameProfile) || this.server.isOwner(gameProfile) && this.server.getWorld(DimensionType.OVERWORLD).getLevelProperties().areCommandsAllowed() || this.cheatsAllowed;
   }

   @Nullable
   public ServerPlayerEntity getPlayer(String string) {
      Iterator var2 = this.players.iterator();

      ServerPlayerEntity serverPlayerEntity;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         serverPlayerEntity = (ServerPlayerEntity)var2.next();
      } while(!serverPlayerEntity.getGameProfile().getName().equalsIgnoreCase(string));

      return serverPlayerEntity;
   }

   public void sendToAround(@Nullable PlayerEntity player, double x, double y, double z, double d, DimensionType dimension, Packet<?> packet) {
      for(int i = 0; i < this.players.size(); ++i) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)this.players.get(i);
         if (serverPlayerEntity != player && serverPlayerEntity.dimension == dimension) {
            double e = x - serverPlayerEntity.getX();
            double f = y - serverPlayerEntity.getY();
            double g = z - serverPlayerEntity.getZ();
            if (e * e + f * f + g * g < d * d) {
               serverPlayerEntity.networkHandler.sendPacket(packet);
            }
         }
      }

   }

   public void saveAllPlayerData() {
      for(int i = 0; i < this.players.size(); ++i) {
         this.savePlayerData((ServerPlayerEntity)this.players.get(i));
      }

   }

   public Whitelist getWhitelist() {
      return this.whitelist;
   }

   public String[] getWhitelistedNames() {
      return this.whitelist.getNames();
   }

   public OperatorList getOpList() {
      return this.ops;
   }

   public String[] getOpNames() {
      return this.ops.getNames();
   }

   public void reloadWhitelist() {
   }

   public void sendWorldInfo(ServerPlayerEntity player, ServerWorld world) {
      WorldBorder worldBorder = this.server.getWorld(DimensionType.OVERWORLD).getWorldBorder();
      player.networkHandler.sendPacket(new WorldBorderS2CPacket(worldBorder, WorldBorderS2CPacket.Type.INITIALIZE));
      player.networkHandler.sendPacket(new WorldTimeUpdateS2CPacket(world.getTime(), world.getTimeOfDay(), world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)));
      BlockPos blockPos = world.getSpawnPos();
      player.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(blockPos));
      if (world.isRaining()) {
         player.networkHandler.sendPacket(new GameStateChangeS2CPacket(1, 0.0F));
         player.networkHandler.sendPacket(new GameStateChangeS2CPacket(7, world.getRainGradient(1.0F)));
         player.networkHandler.sendPacket(new GameStateChangeS2CPacket(8, world.getThunderGradient(1.0F)));
      }

   }

   public void method_14594(ServerPlayerEntity player) {
      player.openContainer((Container)player.playerContainer);
      player.markHealthDirty();
      player.networkHandler.sendPacket(new HeldItemChangeS2CPacket(player.inventory.selectedSlot));
   }

   public int getCurrentPlayerCount() {
      return this.players.size();
   }

   public int getMaxPlayerCount() {
      return this.maxPlayers;
   }

   public boolean isWhitelistEnabled() {
      return this.whitelistEnabled;
   }

   public void setWhitelistEnabled(boolean whitelistEnabled) {
      this.whitelistEnabled = whitelistEnabled;
   }

   public List<ServerPlayerEntity> getPlayersByIp(String string) {
      List<ServerPlayerEntity> list = Lists.newArrayList();
      Iterator var3 = this.players.iterator();

      while(var3.hasNext()) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var3.next();
         if (serverPlayerEntity.getServerBrand().equals(string)) {
            list.add(serverPlayerEntity);
         }
      }

      return list;
   }

   public int getViewDistance() {
      return this.viewDistance;
   }

   public MinecraftServer getServer() {
      return this.server;
   }

   public CompoundTag getUserData() {
      return null;
   }

   @Environment(EnvType.CLIENT)
   public void setGameMode(GameMode gameMode) {
      this.gameMode = gameMode;
   }

   private void setGameMode(ServerPlayerEntity player, ServerPlayerEntity oldPlayer, IWorld world) {
      if (oldPlayer != null) {
         player.interactionManager.setGameMode(oldPlayer.interactionManager.getGameMode());
      } else if (this.gameMode != null) {
         player.interactionManager.setGameMode(this.gameMode);
      }

      player.interactionManager.setGameModeIfNotPresent(world.getLevelProperties().getGameMode());
   }

   @Environment(EnvType.CLIENT)
   public void setCheatsAllowed(boolean cheatsAllowed) {
      this.cheatsAllowed = cheatsAllowed;
   }

   public void disconnectAllPlayers() {
      for(int i = 0; i < this.players.size(); ++i) {
         ((ServerPlayerEntity)this.players.get(i)).networkHandler.disconnect(new TranslatableText("multiplayer.disconnect.server_shutdown", new Object[0]));
      }

   }

   public void broadcastChatMessage(Text text, boolean system) {
      this.server.sendMessage(text);
      MessageType messageType = system ? MessageType.SYSTEM : MessageType.CHAT;
      this.sendToAll((Packet)(new ChatMessageS2CPacket(text, messageType)));
   }

   public void sendToAll(Text text) {
      this.broadcastChatMessage(text, true);
   }

   public ServerStatHandler createStatHandler(PlayerEntity player) {
      UUID uUID = player.getUuid();
      ServerStatHandler serverStatHandler = uUID == null ? null : (ServerStatHandler)this.statisticsMap.get(uUID);
      if (serverStatHandler == null) {
         File file = new File(this.server.getWorld(DimensionType.OVERWORLD).getSaveHandler().getWorldDir(), "stats");
         File file2 = new File(file, uUID + ".json");
         if (!file2.exists()) {
            File file3 = new File(file, player.getName().getString() + ".json");
            if (file3.exists() && file3.isFile()) {
               file3.renameTo(file2);
            }
         }

         serverStatHandler = new ServerStatHandler(this.server, file2);
         this.statisticsMap.put(uUID, serverStatHandler);
      }

      return serverStatHandler;
   }

   public PlayerAdvancementTracker getAdvancementTracker(ServerPlayerEntity player) {
      UUID uUID = player.getUuid();
      PlayerAdvancementTracker playerAdvancementTracker = (PlayerAdvancementTracker)this.advancementTrackers.get(uUID);
      if (playerAdvancementTracker == null) {
         File file = new File(this.server.getWorld(DimensionType.OVERWORLD).getSaveHandler().getWorldDir(), "advancements");
         File file2 = new File(file, uUID + ".json");
         playerAdvancementTracker = new PlayerAdvancementTracker(this.server, file2, player);
         this.advancementTrackers.put(uUID, playerAdvancementTracker);
      }

      playerAdvancementTracker.setOwner(player);
      return playerAdvancementTracker;
   }

   public void setViewDistance(int viewDistance) {
      this.viewDistance = viewDistance;
      this.sendToAll((Packet)(new ChunkLoadDistanceS2CPacket(viewDistance)));
      Iterator var2 = this.server.getWorlds().iterator();

      while(var2.hasNext()) {
         ServerWorld serverWorld = (ServerWorld)var2.next();
         if (serverWorld != null) {
            serverWorld.getChunkManager().applyViewDistance(viewDistance);
         }
      }

   }

   public List<ServerPlayerEntity> getPlayerList() {
      return this.players;
   }

   @Nullable
   public ServerPlayerEntity getPlayer(UUID uuid) {
      return (ServerPlayerEntity)this.playerMap.get(uuid);
   }

   public boolean canBypassPlayerLimit(GameProfile gameProfile) {
      return false;
   }

   public void onDataPacksReloaded() {
      Iterator var1 = this.advancementTrackers.values().iterator();

      while(var1.hasNext()) {
         PlayerAdvancementTracker playerAdvancementTracker = (PlayerAdvancementTracker)var1.next();
         playerAdvancementTracker.reload();
      }

      this.sendToAll((Packet)(new SynchronizeTagsS2CPacket(this.server.getTagManager())));
      SynchronizeRecipesS2CPacket synchronizeRecipesS2CPacket = new SynchronizeRecipesS2CPacket(this.server.getRecipeManager().values());
      Iterator var5 = this.players.iterator();

      while(var5.hasNext()) {
         ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var5.next();
         serverPlayerEntity.networkHandler.sendPacket(synchronizeRecipesS2CPacket);
         serverPlayerEntity.getRecipeBook().sendInitRecipesPacket(serverPlayerEntity);
      }

   }

   public boolean areCheatsAllowed() {
      return this.cheatsAllowed;
   }
}
