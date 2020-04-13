package net.minecraft.server;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.longs.LongIterator;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.Proxy;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.packet.DifficultyS2CPacket;
import net.minecraft.client.network.packet.WorldTimeUpdateS2CPacket;
import net.minecraft.command.DataCommandStorage;
import net.minecraft.datafixer.Schemas;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.condition.LootConditionManager;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.FileResourcePackProvider;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.scoreboard.ScoreboardState;
import net.minecraft.scoreboard.ScoreboardSynchronizer;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.EulaReader;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.SecondaryServerWorld;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.RegistryTagManager;
import net.minecraft.test.TestManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.MetricsData;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.UncaughtExceptionLogger;
import net.minecraft.util.Unit;
import net.minecraft.util.UserCache;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.DisableableProfiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.snooper.Snooper;
import net.minecraft.util.snooper.SnooperListener;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.Difficulty;
import net.minecraft.world.ForcedChunkState;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.SessionLockException;
import net.minecraft.world.World;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.updater.WorldUpdater;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class MinecraftServer extends ReentrantThreadExecutor<ServerTask> implements SnooperListener, CommandOutput, AutoCloseable, Runnable {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final File USER_CACHE_FILE = new File("usercache.json");
   private static final CompletableFuture<Unit> COMPLETED_UNIT_FUTURE;
   public static final LevelInfo DEMO_LEVEL_INFO;
   private final LevelStorage levelStorage;
   private final Snooper snooper = new Snooper("server", this, Util.getMeasuringTimeMs());
   private final File gameDir;
   private final List<Runnable> serverGuiTickables = Lists.newArrayList();
   private final DisableableProfiler profiler = new DisableableProfiler(this::getTicks);
   private final ServerNetworkIo networkIo;
   protected final WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory;
   private final ServerMetadata metadata = new ServerMetadata();
   private final Random random = new Random();
   private final DataFixer dataFixer;
   private String serverIp;
   private int serverPort = -1;
   private final Map<DimensionType, ServerWorld> worlds = Maps.newIdentityHashMap();
   private PlayerManager playerManager;
   private volatile boolean running = true;
   private boolean stopped;
   private int ticks;
   protected final Proxy proxy;
   private boolean onlineMode;
   private boolean preventProxyConnections;
   private boolean spawnAnimals;
   private boolean spawnNpcs;
   private boolean pvpEnabled;
   private boolean flightEnabled;
   @Nullable
   private String motd;
   private int worldHeight;
   private int playerIdleTimeout;
   public final long[] lastTickLengths = new long[100];
   @Nullable
   private KeyPair keyPair;
   @Nullable
   private String userName;
   private final String levelName;
   @Nullable
   @Environment(EnvType.CLIENT)
   private String displayName;
   private boolean demo;
   private boolean bonusChest;
   private String resourcePackUrl = "";
   private String resourcePackHash = "";
   private volatile boolean loading;
   private long field_4557;
   @Nullable
   private Text loadingStage;
   private boolean profilerStartQueued;
   private boolean forceGameMode;
   @Nullable
   private final YggdrasilAuthenticationService authService;
   private final MinecraftSessionService sessionService;
   private final GameProfileRepository gameProfileRepo;
   private final UserCache userCache;
   private long lastPlayerSampleUpdate;
   protected final Thread serverThread = (Thread)Util.make(new Thread(this, "Server thread"), (thread) -> {
      thread.setUncaughtExceptionHandler((threadx, throwable) -> {
         LOGGER.error(throwable);
      });
   });
   private long timeReference = Util.getMeasuringTimeMs();
   private long field_19248;
   private boolean field_19249;
   @Environment(EnvType.CLIENT)
   private boolean iconFilePresent;
   private final ReloadableResourceManager dataManager;
   private final ResourcePackManager<ResourcePackProfile> dataPackManager;
   @Nullable
   private FileResourcePackProvider fileDataPackProvider;
   private final CommandManager commandManager;
   private final RecipeManager recipeManager;
   private final RegistryTagManager tagManager;
   private final ServerScoreboard scoreboard;
   @Nullable
   private DataCommandStorage dataCommandStorage;
   private final BossBarManager bossBarManager;
   private final LootConditionManager predicateManager;
   private final LootManager lootManager;
   private final ServerAdvancementLoader advancementLoader;
   private final CommandFunctionManager commandFunctionManager;
   private final MetricsData metricsData;
   private boolean enforceWhitelist;
   private boolean forceWorldUpgrade;
   private boolean eraseCache;
   private float tickTime;
   private final Executor workerExecutor;
   @Nullable
   private String serverId;

   public MinecraftServer(File gameDir, Proxy proxy, DataFixer dataFixer, CommandManager commandManager, YggdrasilAuthenticationService authService, MinecraftSessionService sessionService, GameProfileRepository gameProfileRepository, UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, String levelName) {
      super("Server");
      this.dataManager = new ReloadableResourceManagerImpl(ResourceType.SERVER_DATA, this.serverThread);
      this.dataPackManager = new ResourcePackManager(ResourcePackProfile::new);
      this.recipeManager = new RecipeManager();
      this.tagManager = new RegistryTagManager();
      this.scoreboard = new ServerScoreboard(this);
      this.bossBarManager = new BossBarManager(this);
      this.predicateManager = new LootConditionManager();
      this.lootManager = new LootManager(this.predicateManager);
      this.advancementLoader = new ServerAdvancementLoader();
      this.commandFunctionManager = new CommandFunctionManager(this);
      this.metricsData = new MetricsData();
      this.proxy = proxy;
      this.commandManager = commandManager;
      this.authService = authService;
      this.sessionService = sessionService;
      this.gameProfileRepo = gameProfileRepository;
      this.userCache = userCache;
      this.gameDir = gameDir;
      this.networkIo = new ServerNetworkIo(this);
      this.worldGenerationProgressListenerFactory = worldGenerationProgressListenerFactory;
      this.levelStorage = new LevelStorage(gameDir.toPath(), gameDir.toPath().resolve("../backups"), dataFixer);
      this.dataFixer = dataFixer;
      this.dataManager.registerListener(this.tagManager);
      this.dataManager.registerListener(this.predicateManager);
      this.dataManager.registerListener(this.recipeManager);
      this.dataManager.registerListener(this.lootManager);
      this.dataManager.registerListener(this.commandFunctionManager);
      this.dataManager.registerListener(this.advancementLoader);
      this.workerExecutor = Util.getServerWorkerExecutor();
      this.levelName = levelName;
   }

   private void initScoreboard(PersistentStateManager persistentStateManager) {
      ScoreboardState scoreboardState = (ScoreboardState)persistentStateManager.getOrCreate(ScoreboardState::new, "scoreboard");
      scoreboardState.setScoreboard(this.getScoreboard());
      this.getScoreboard().addUpdateListener(new ScoreboardSynchronizer(scoreboardState));
   }

   protected abstract boolean setupServer() throws IOException;

   protected void upgradeWorld(String string) {
      if (this.getLevelStorage().requiresConversion(string)) {
         LOGGER.info("Converting map!");
         this.setLoadingStage(new TranslatableText("menu.convertingLevel", new Object[0]));
         this.getLevelStorage().convertLevel(string, new ProgressListener() {
            private long lastProgressUpdate = Util.getMeasuringTimeMs();

            public void method_15412(Text text) {
            }

            @Environment(EnvType.CLIENT)
            public void method_15413(Text text) {
            }

            public void progressStagePercentage(int i) {
               if (Util.getMeasuringTimeMs() - this.lastProgressUpdate >= 1000L) {
                  this.lastProgressUpdate = Util.getMeasuringTimeMs();
                  MinecraftServer.LOGGER.info("Converting... {}%", i);
               }

            }

            @Environment(EnvType.CLIENT)
            public void setDone() {
            }

            public void method_15414(Text text) {
            }
         });
      }

      if (this.forceWorldUpgrade) {
         LOGGER.info("Forcing world upgrade!");
         LevelProperties levelProperties = this.getLevelStorage().getLevelProperties(this.getLevelName());
         if (levelProperties != null) {
            WorldUpdater worldUpdater = new WorldUpdater(this.getLevelName(), this.getLevelStorage(), levelProperties, this.eraseCache);
            Text text = null;

            while(!worldUpdater.isDone()) {
               Text text2 = worldUpdater.getStatus();
               if (text != text2) {
                  text = text2;
                  LOGGER.info(worldUpdater.getStatus().getString());
               }

               int i = worldUpdater.getTotalChunkCount();
               if (i > 0) {
                  int j = worldUpdater.getUpgradedChunkCount() + worldUpdater.getSkippedChunkCount();
                  LOGGER.info("{}% completed ({} / {} chunks)...", MathHelper.floor((float)j / (float)i * 100.0F), j, i);
               }

               if (this.isStopped()) {
                  worldUpdater.cancel();
               } else {
                  try {
                     Thread.sleep(1000L);
                  } catch (InterruptedException var8) {
                  }
               }
            }
         }
      }

   }

   protected synchronized void setLoadingStage(Text loadingStage) {
      this.loadingStage = loadingStage;
   }

   protected void loadWorld(String name, String serverName, long seed, LevelGeneratorType generatorType, JsonElement generatorSettings) {
      this.upgradeWorld(name);
      this.setLoadingStage(new TranslatableText("menu.loadingLevel", new Object[0]));
      WorldSaveHandler worldSaveHandler = this.getLevelStorage().createSaveHandler(name, this);
      this.loadWorldResourcePack(this.getLevelName(), worldSaveHandler);
      LevelProperties levelProperties = worldSaveHandler.readProperties();
      LevelInfo levelInfo2;
      if (levelProperties == null) {
         if (this.isDemo()) {
            levelInfo2 = DEMO_LEVEL_INFO;
         } else {
            levelInfo2 = new LevelInfo(seed, this.getDefaultGameMode(), this.shouldGenerateStructures(), this.isHardcore(), generatorType);
            levelInfo2.setGeneratorOptions(generatorSettings);
            if (this.bonusChest) {
               levelInfo2.setBonusChest();
            }
         }

         levelProperties = new LevelProperties(levelInfo2, serverName);
      } else {
         levelProperties.setLevelName(serverName);
         levelInfo2 = new LevelInfo(levelProperties);
      }

      levelProperties.method_24285(this.getServerModName(), this.method_24307().isPresent());
      this.loadWorldDataPacks(worldSaveHandler.getWorldDir(), levelProperties);
      WorldGenerationProgressListener worldGenerationProgressListener = this.worldGenerationProgressListenerFactory.create(11);
      this.createWorlds(worldSaveHandler, levelProperties, levelInfo2, worldGenerationProgressListener);
      this.setDifficulty(this.getDefaultDifficulty(), true);
      this.prepareStartRegion(worldGenerationProgressListener);
   }

   protected void createWorlds(WorldSaveHandler worldSaveHandler, LevelProperties properties, LevelInfo levelInfo, WorldGenerationProgressListener worldGenerationProgressListener) {
      if (this.isDemo()) {
         properties.loadLevelInfo(DEMO_LEVEL_INFO);
      }

      ServerWorld serverWorld = new ServerWorld(this, this.workerExecutor, worldSaveHandler, properties, DimensionType.OVERWORLD, this.profiler, worldGenerationProgressListener);
      this.worlds.put(DimensionType.OVERWORLD, serverWorld);
      PersistentStateManager persistentStateManager = serverWorld.getPersistentStateManager();
      this.initScoreboard(persistentStateManager);
      this.dataCommandStorage = new DataCommandStorage(persistentStateManager);
      serverWorld.getWorldBorder().load(properties);
      ServerWorld serverWorld2 = this.getWorld(DimensionType.OVERWORLD);
      if (!properties.isInitialized()) {
         try {
            serverWorld2.init(levelInfo);
            if (properties.getGeneratorType() == LevelGeneratorType.DEBUG_ALL_BLOCK_STATES) {
               this.setToDebugWorldProperties(properties);
            }

            properties.setInitialized(true);
         } catch (Throwable var12) {
            CrashReport crashReport = CrashReport.create(var12, "Exception initializing level");

            try {
               serverWorld2.addDetailsToCrashReport(crashReport);
            } catch (Throwable var11) {
            }

            throw new CrashException(crashReport);
         }

         properties.setInitialized(true);
      }

      this.getPlayerManager().setMainWorld(serverWorld2);
      if (properties.getCustomBossEvents() != null) {
         this.getBossBarManager().fromTag(properties.getCustomBossEvents());
      }

      Iterator var8 = DimensionType.getAll().iterator();

      while(var8.hasNext()) {
         DimensionType dimensionType = (DimensionType)var8.next();
         if (dimensionType != DimensionType.OVERWORLD) {
            this.worlds.put(dimensionType, new SecondaryServerWorld(serverWorld2, this, this.workerExecutor, worldSaveHandler, dimensionType, this.profiler, worldGenerationProgressListener));
         }
      }

   }

   private void setToDebugWorldProperties(LevelProperties properties) {
      properties.setStructures(false);
      properties.setCommandsAllowed(true);
      properties.setRaining(false);
      properties.setThundering(false);
      properties.setClearWeatherTime(1000000000);
      properties.setTimeOfDay(6000L);
      properties.setGameMode(GameMode.SPECTATOR);
      properties.setHardcore(false);
      properties.setDifficulty(Difficulty.PEACEFUL);
      properties.setDifficultyLocked(true);
      ((GameRules.BooleanRule)properties.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE)).set(false, this);
   }

   protected void loadWorldDataPacks(File worldDir, LevelProperties levelProperties) {
      this.dataPackManager.registerProvider(new VanillaDataPackProvider());
      this.fileDataPackProvider = new FileResourcePackProvider(new File(worldDir, "datapacks"));
      this.dataPackManager.registerProvider(this.fileDataPackProvider);
      this.dataPackManager.scanPacks();
      List<ResourcePackProfile> list = Lists.newArrayList();
      Iterator var4 = levelProperties.getEnabledDataPacks().iterator();

      while(var4.hasNext()) {
         String string = (String)var4.next();
         ResourcePackProfile resourcePackProfile = this.dataPackManager.getProfile(string);
         if (resourcePackProfile != null) {
            list.add(resourcePackProfile);
         } else {
            LOGGER.warn("Missing data pack {}", string);
         }
      }

      this.dataPackManager.setEnabledProfiles(list);
      this.reloadDataPacks(levelProperties);
      this.method_24154();
   }

   protected void prepareStartRegion(WorldGenerationProgressListener worldGenerationProgressListener) {
      this.setLoadingStage(new TranslatableText("menu.generatingTerrain", new Object[0]));
      ServerWorld serverWorld = this.getWorld(DimensionType.OVERWORLD);
      LOGGER.info("Preparing start region for dimension " + DimensionType.getId(serverWorld.dimension.getType()));
      BlockPos blockPos = serverWorld.getSpawnPos();
      worldGenerationProgressListener.start(new ChunkPos(blockPos));
      ServerChunkManager serverChunkManager = serverWorld.getChunkManager();
      serverChunkManager.getLightingProvider().setTaskBatchSize(500);
      this.timeReference = Util.getMeasuringTimeMs();
      serverChunkManager.addTicket(ChunkTicketType.START, new ChunkPos(blockPos), 11, Unit.INSTANCE);

      while(serverChunkManager.getTotalChunksLoadedCount() != 441) {
         this.timeReference = Util.getMeasuringTimeMs() + 10L;
         this.method_16208();
      }

      this.timeReference = Util.getMeasuringTimeMs() + 10L;
      this.method_16208();
      Iterator var5 = DimensionType.getAll().iterator();

      while(true) {
         DimensionType dimensionType;
         ForcedChunkState forcedChunkState;
         do {
            if (!var5.hasNext()) {
               this.timeReference = Util.getMeasuringTimeMs() + 10L;
               this.method_16208();
               worldGenerationProgressListener.stop();
               serverChunkManager.getLightingProvider().setTaskBatchSize(5);
               return;
            }

            dimensionType = (DimensionType)var5.next();
            forcedChunkState = (ForcedChunkState)this.getWorld(dimensionType).getPersistentStateManager().get(ForcedChunkState::new, "chunks");
         } while(forcedChunkState == null);

         ServerWorld serverWorld2 = this.getWorld(dimensionType);
         LongIterator longIterator = forcedChunkState.getChunks().iterator();

         while(longIterator.hasNext()) {
            long l = longIterator.nextLong();
            ChunkPos chunkPos = new ChunkPos(l);
            serverWorld2.getChunkManager().setChunkForced(chunkPos, true);
         }
      }
   }

   protected void loadWorldResourcePack(String worldName, WorldSaveHandler worldSaveHandler) {
      File file = new File(worldSaveHandler.getWorldDir(), "resources.zip");
      if (file.isFile()) {
         try {
            this.setResourcePack("level://" + URLEncoder.encode(worldName, StandardCharsets.UTF_8.toString()) + "/" + "resources.zip", "");
         } catch (UnsupportedEncodingException var5) {
            LOGGER.warn("Something went wrong url encoding {}", worldName);
         }
      }

   }

   public abstract boolean shouldGenerateStructures();

   public abstract GameMode getDefaultGameMode();

   public abstract Difficulty getDefaultDifficulty();

   public abstract boolean isHardcore();

   public abstract int getOpPermissionLevel();

   public abstract int getFunctionPermissionLevel();

   public abstract boolean shouldBroadcastRconToOps();

   public boolean save(boolean bl, boolean bl2, boolean bl3) {
      boolean bl4 = false;

      for(Iterator var5 = this.getWorlds().iterator(); var5.hasNext(); bl4 = true) {
         ServerWorld serverWorld = (ServerWorld)var5.next();
         if (!bl) {
            LOGGER.info("Saving chunks for level '{}'/{}", serverWorld.getLevelProperties().getLevelName(), DimensionType.getId(serverWorld.dimension.getType()));
         }

         try {
            serverWorld.save((ProgressListener)null, bl2, serverWorld.savingDisabled && !bl3);
         } catch (SessionLockException var8) {
            LOGGER.warn(var8.getMessage());
         }
      }

      ServerWorld serverWorld2 = this.getWorld(DimensionType.OVERWORLD);
      LevelProperties levelProperties = serverWorld2.getLevelProperties();
      serverWorld2.getWorldBorder().save(levelProperties);
      levelProperties.setCustomBossEvents(this.getBossBarManager().toTag());
      serverWorld2.getSaveHandler().saveWorld(levelProperties, this.getPlayerManager().getUserData());
      return bl4;
   }

   public void close() {
      this.shutdown();
   }

   protected void shutdown() {
      LOGGER.info("Stopping server");
      if (this.getNetworkIo() != null) {
         this.getNetworkIo().stop();
      }

      if (this.playerManager != null) {
         LOGGER.info("Saving players");
         this.playerManager.saveAllPlayerData();
         this.playerManager.disconnectAllPlayers();
      }

      LOGGER.info("Saving worlds");
      Iterator var1 = this.getWorlds().iterator();

      ServerWorld serverWorld2;
      while(var1.hasNext()) {
         serverWorld2 = (ServerWorld)var1.next();
         if (serverWorld2 != null) {
            serverWorld2.savingDisabled = false;
         }
      }

      this.save(false, true, false);
      var1 = this.getWorlds().iterator();

      while(var1.hasNext()) {
         serverWorld2 = (ServerWorld)var1.next();
         if (serverWorld2 != null) {
            try {
               serverWorld2.close();
            } catch (IOException var4) {
               LOGGER.error("Exception closing the level", var4);
            }
         }
      }

      if (this.snooper.isActive()) {
         this.snooper.cancel();
      }

   }

   public String getServerIp() {
      return this.serverIp;
   }

   public void setServerIp(String serverIp) {
      this.serverIp = serverIp;
   }

   public boolean isRunning() {
      return this.running;
   }

   public void stop(boolean bl) {
      this.running = false;
      if (bl) {
         try {
            this.serverThread.join();
         } catch (InterruptedException var3) {
            LOGGER.error("Error while shutting down", var3);
         }
      }

   }

   public void run() {
      try {
         if (this.setupServer()) {
            this.timeReference = Util.getMeasuringTimeMs();
            this.metadata.setDescription(new LiteralText(this.motd));
            this.metadata.setVersion(new ServerMetadata.Version(SharedConstants.getGameVersion().getName(), SharedConstants.getGameVersion().getProtocolVersion()));
            this.setFavicon(this.metadata);

            while(this.running) {
               long l = Util.getMeasuringTimeMs() - this.timeReference;
               if (l > 2000L && this.timeReference - this.field_4557 >= 15000L) {
                  long m = l / 50L;
                  LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", l, m);
                  this.timeReference += m * 50L;
                  this.field_4557 = this.timeReference;
               }

               this.timeReference += 50L;
               if (this.profilerStartQueued) {
                  this.profilerStartQueued = false;
                  this.profiler.getController().enable();
               }

               this.profiler.startTick();
               this.profiler.push("tick");
               this.tick(this::shouldKeepTicking);
               this.profiler.swap("nextTickWait");
               this.field_19249 = true;
               this.field_19248 = Math.max(Util.getMeasuringTimeMs() + 50L, this.timeReference);
               this.method_16208();
               this.profiler.pop();
               this.profiler.endTick();
               this.loading = true;
            }
         } else {
            this.setCrashReport((CrashReport)null);
         }
      } catch (Throwable var44) {
         LOGGER.error("Encountered an unexpected exception", var44);
         CrashReport crashReport2;
         if (var44 instanceof CrashException) {
            crashReport2 = this.populateCrashReport(((CrashException)var44).getReport());
         } else {
            crashReport2 = this.populateCrashReport(new CrashReport("Exception in server tick loop", var44));
         }

         File file = new File(new File(this.getRunDirectory(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");
         if (crashReport2.writeToFile(file)) {
            LOGGER.error("This crash report has been saved to: {}", file.getAbsolutePath());
         } else {
            LOGGER.error("We were unable to save this crash report to disk.");
         }

         this.setCrashReport(crashReport2);
      } finally {
         try {
            this.stopped = true;
            this.shutdown();
         } catch (Throwable var42) {
            LOGGER.error("Exception stopping the server", var42);
         } finally {
            this.exit();
         }

      }

   }

   private boolean shouldKeepTicking() {
      return this.hasRunningTasks() || Util.getMeasuringTimeMs() < (this.field_19249 ? this.field_19248 : this.timeReference);
   }

   protected void method_16208() {
      this.runTasks();
      this.runTasks(() -> {
         return !this.shouldKeepTicking();
      });
   }

   protected ServerTask createTask(Runnable runnable) {
      return new ServerTask(this.ticks, runnable);
   }

   protected boolean canExecute(ServerTask serverTask) {
      return serverTask.getCreationTicks() + 3 < this.ticks || this.shouldKeepTicking();
   }

   public boolean runTask() {
      boolean bl = this.method_20415();
      this.field_19249 = bl;
      return bl;
   }

   private boolean method_20415() {
      if (super.runTask()) {
         return true;
      } else {
         if (this.shouldKeepTicking()) {
            Iterator var1 = this.getWorlds().iterator();

            while(var1.hasNext()) {
               ServerWorld serverWorld = (ServerWorld)var1.next();
               if (serverWorld.getChunkManager().executeQueuedTasks()) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   protected void executeTask(ServerTask serverTask) {
      this.getProfiler().method_24270("runTask");
      super.executeTask(serverTask);
   }

   public void setFavicon(ServerMetadata metadata) {
      File file = this.getFile("server-icon.png");
      if (!file.exists()) {
         file = this.getLevelStorage().resolveFile(this.getLevelName(), "icon.png");
      }

      if (file.isFile()) {
         ByteBuf byteBuf = Unpooled.buffer();

         try {
            BufferedImage bufferedImage = ImageIO.read(file);
            Validate.validState(bufferedImage.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
            Validate.validState(bufferedImage.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
            ImageIO.write(bufferedImage, "PNG", new ByteBufOutputStream(byteBuf));
            ByteBuffer byteBuffer = Base64.getEncoder().encode(byteBuf.nioBuffer());
            metadata.setFavicon("data:image/png;base64," + StandardCharsets.UTF_8.decode(byteBuffer));
         } catch (Exception var9) {
            LOGGER.error("Couldn't load server icon", var9);
         } finally {
            byteBuf.release();
         }
      }

   }

   @Environment(EnvType.CLIENT)
   public boolean hasIconFile() {
      this.iconFilePresent = this.iconFilePresent || this.getIconFile().isFile();
      return this.iconFilePresent;
   }

   @Environment(EnvType.CLIENT)
   public File getIconFile() {
      return this.getLevelStorage().resolveFile(this.getLevelName(), "icon.png");
   }

   public File getRunDirectory() {
      return new File(".");
   }

   protected void setCrashReport(CrashReport crashReport) {
   }

   protected void exit() {
   }

   protected void tick(BooleanSupplier shouldKeepTicking) {
      long l = Util.getMeasuringTimeNano();
      ++this.ticks;
      this.tickWorlds(shouldKeepTicking);
      if (l - this.lastPlayerSampleUpdate >= 5000000000L) {
         this.lastPlayerSampleUpdate = l;
         this.metadata.setPlayers(new ServerMetadata.Players(this.getMaxPlayerCount(), this.getCurrentPlayerCount()));
         GameProfile[] gameProfiles = new GameProfile[Math.min(this.getCurrentPlayerCount(), 12)];
         int i = MathHelper.nextInt(this.random, 0, this.getCurrentPlayerCount() - gameProfiles.length);

         for(int j = 0; j < gameProfiles.length; ++j) {
            gameProfiles[j] = ((ServerPlayerEntity)this.playerManager.getPlayerList().get(i + j)).getGameProfile();
         }

         Collections.shuffle(Arrays.asList(gameProfiles));
         this.metadata.getPlayers().setSample(gameProfiles);
      }

      if (this.ticks % 6000 == 0) {
         LOGGER.debug("Autosave started");
         this.profiler.push("save");
         this.playerManager.saveAllPlayerData();
         this.save(true, false, false);
         this.profiler.pop();
         LOGGER.debug("Autosave finished");
      }

      this.profiler.push("snooper");
      if (!this.snooper.isActive() && this.ticks > 100) {
         this.snooper.method_5482();
      }

      if (this.ticks % 6000 == 0) {
         this.snooper.update();
      }

      this.profiler.pop();
      this.profiler.push("tallying");
      long m = this.lastTickLengths[this.ticks % 100] = Util.getMeasuringTimeNano() - l;
      this.tickTime = this.tickTime * 0.8F + (float)m / 1000000.0F * 0.19999999F;
      long n = Util.getMeasuringTimeNano();
      this.metricsData.pushSample(n - l);
      this.profiler.pop();
   }

   protected void tickWorlds(BooleanSupplier shouldKeepTicking) {
      this.profiler.push("commandFunctions");
      this.getCommandFunctionManager().tick();
      this.profiler.swap("levels");
      Iterator var2 = this.getWorlds().iterator();

      while(true) {
         ServerWorld serverWorld;
         do {
            if (!var2.hasNext()) {
               this.profiler.swap("connection");
               this.getNetworkIo().tick();
               this.profiler.swap("players");
               this.playerManager.updatePlayerLatency();
               if (SharedConstants.isDevelopment) {
                  TestManager.INSTANCE.tick();
               }

               this.profiler.swap("server gui refresh");

               for(int i = 0; i < this.serverGuiTickables.size(); ++i) {
                  ((Runnable)this.serverGuiTickables.get(i)).run();
               }

               this.profiler.pop();
               return;
            }

            serverWorld = (ServerWorld)var2.next();
         } while(serverWorld.dimension.getType() != DimensionType.OVERWORLD && !this.isNetherAllowed());

         this.profiler.push(() -> {
            return serverWorld.getLevelProperties().getLevelName() + " " + Registry.DIMENSION_TYPE.getId(serverWorld.dimension.getType());
         });
         if (this.ticks % 20 == 0) {
            this.profiler.push("timeSync");
            this.playerManager.sendToDimension(new WorldTimeUpdateS2CPacket(serverWorld.getTime(), serverWorld.getTimeOfDay(), serverWorld.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)), serverWorld.dimension.getType());
            this.profiler.pop();
         }

         this.profiler.push("tick");

         try {
            serverWorld.tick(shouldKeepTicking);
         } catch (Throwable var6) {
            CrashReport crashReport = CrashReport.create(var6, "Exception ticking world");
            serverWorld.addDetailsToCrashReport(crashReport);
            throw new CrashException(crashReport);
         }

         this.profiler.pop();
         this.profiler.pop();
      }
   }

   public boolean isNetherAllowed() {
      return true;
   }

   public void addServerGuiTickable(Runnable tickable) {
      this.serverGuiTickables.add(tickable);
   }

   public static void main(String[] args) {
      OptionParser optionParser = new OptionParser();
      OptionSpec<Void> optionSpec = optionParser.accepts("nogui");
      OptionSpec<Void> optionSpec2 = optionParser.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
      OptionSpec<Void> optionSpec3 = optionParser.accepts("demo");
      OptionSpec<Void> optionSpec4 = optionParser.accepts("bonusChest");
      OptionSpec<Void> optionSpec5 = optionParser.accepts("forceUpgrade");
      OptionSpec<Void> optionSpec6 = optionParser.accepts("eraseCache");
      OptionSpec<Void> optionSpec7 = optionParser.accepts("help").forHelp();
      OptionSpec<String> optionSpec8 = optionParser.accepts("singleplayer").withRequiredArg();
      OptionSpec<String> optionSpec9 = optionParser.accepts("universe").withRequiredArg().defaultsTo(".", new String[0]);
      OptionSpec<String> optionSpec10 = optionParser.accepts("world").withRequiredArg();
      OptionSpec<Integer> optionSpec11 = optionParser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(-1, new Integer[0]);
      OptionSpec<String> optionSpec12 = optionParser.accepts("serverId").withRequiredArg();
      NonOptionArgumentSpec optionSpec13 = optionParser.nonOptions();

      try {
         OptionSet optionSet = optionParser.parse(args);
         if (optionSet.has(optionSpec7)) {
            optionParser.printHelpOn(System.err);
            return;
         }

         Path path = Paths.get("server.properties");
         ServerPropertiesLoader serverPropertiesLoader = new ServerPropertiesLoader(path);
         serverPropertiesLoader.store();
         Path path2 = Paths.get("eula.txt");
         EulaReader eulaReader = new EulaReader(path2);
         if (optionSet.has(optionSpec2)) {
            LOGGER.info("Initialized '" + path.toAbsolutePath().toString() + "' and '" + path2.toAbsolutePath().toString() + "'");
            return;
         }

         if (!eulaReader.isEulaAgreedTo()) {
            LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
            return;
         }

         CrashReport.method_24305();
         Bootstrap.initialize();
         Bootstrap.logMissingTranslations();
         String string = (String)optionSet.valueOf(optionSpec9);
         YggdrasilAuthenticationService yggdrasilAuthenticationService = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
         MinecraftSessionService minecraftSessionService = yggdrasilAuthenticationService.createMinecraftSessionService();
         GameProfileRepository gameProfileRepository = yggdrasilAuthenticationService.createProfileRepository();
         UserCache userCache = new UserCache(gameProfileRepository, new File(string, USER_CACHE_FILE.getName()));
         String string2 = (String)Optional.ofNullable(optionSet.valueOf(optionSpec10)).orElse(serverPropertiesLoader.getPropertiesHandler().levelName);
         final MinecraftDedicatedServer minecraftDedicatedServer = new MinecraftDedicatedServer(new File(string), serverPropertiesLoader, Schemas.getFixer(), yggdrasilAuthenticationService, minecraftSessionService, gameProfileRepository, userCache, WorldGenerationProgressLogger::new, string2);
         minecraftDedicatedServer.setUserName((String)optionSet.valueOf(optionSpec8));
         minecraftDedicatedServer.setServerPort((Integer)optionSet.valueOf(optionSpec11));
         minecraftDedicatedServer.setDemo(optionSet.has(optionSpec3));
         minecraftDedicatedServer.setBonusChest(optionSet.has(optionSpec4));
         minecraftDedicatedServer.setForceWorldUpgrade(optionSet.has(optionSpec5));
         minecraftDedicatedServer.setEraseCache(optionSet.has(optionSpec6));
         minecraftDedicatedServer.setServerId((String)optionSet.valueOf(optionSpec12));
         boolean bl = !optionSet.has(optionSpec) && !optionSet.valuesOf(optionSpec13).contains("nogui");
         if (bl && !GraphicsEnvironment.isHeadless()) {
            minecraftDedicatedServer.createGui();
         }

         minecraftDedicatedServer.start();
         Thread thread = new Thread("Server Shutdown Thread") {
            public void run() {
               minecraftDedicatedServer.stop(true);
            }
         };
         thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
         Runtime.getRuntime().addShutdownHook(thread);
      } catch (Exception var29) {
         LOGGER.fatal("Failed to start the minecraft server", var29);
      }

   }

   protected void setServerId(String serverId) {
      this.serverId = serverId;
   }

   protected void setForceWorldUpgrade(boolean bl) {
      this.forceWorldUpgrade = bl;
   }

   protected void setEraseCache(boolean eraseCache) {
      this.eraseCache = eraseCache;
   }

   public void start() {
      this.serverThread.start();
   }

   @Environment(EnvType.CLIENT)
   public boolean isStopping() {
      return !this.serverThread.isAlive();
   }

   public File getFile(String string) {
      return new File(this.getRunDirectory(), string);
   }

   public void info(String string) {
      LOGGER.info(string);
   }

   public void warn(String string) {
      LOGGER.warn(string);
   }

   public ServerWorld getWorld(DimensionType dimensionType) {
      return (ServerWorld)this.worlds.get(dimensionType);
   }

   public Iterable<ServerWorld> getWorlds() {
      return this.worlds.values();
   }

   public String getVersion() {
      return SharedConstants.getGameVersion().getName();
   }

   public int getCurrentPlayerCount() {
      return this.playerManager.getCurrentPlayerCount();
   }

   public int getMaxPlayerCount() {
      return this.playerManager.getMaxPlayerCount();
   }

   public String[] getPlayerNames() {
      return this.playerManager.getPlayerNames();
   }

   public boolean isDebuggingEnabled() {
      return false;
   }

   public void logError(String string) {
      LOGGER.error(string);
   }

   public void log(String string) {
      if (this.isDebuggingEnabled()) {
         LOGGER.info(string);
      }

   }

   public String getServerModName() {
      return "vanilla";
   }

   public CrashReport populateCrashReport(CrashReport crashReport) {
      if (this.playerManager != null) {
         crashReport.getSystemDetailsSection().add("Player Count", () -> {
            return this.playerManager.getCurrentPlayerCount() + " / " + this.playerManager.getMaxPlayerCount() + "; " + this.playerManager.getPlayerList();
         });
      }

      crashReport.getSystemDetailsSection().add("Data Packs", () -> {
         StringBuilder stringBuilder = new StringBuilder();
         Iterator var2 = this.dataPackManager.getEnabledProfiles().iterator();

         while(var2.hasNext()) {
            ResourcePackProfile resourcePackProfile = (ResourcePackProfile)var2.next();
            if (stringBuilder.length() > 0) {
               stringBuilder.append(", ");
            }

            stringBuilder.append(resourcePackProfile.getName());
            if (!resourcePackProfile.getCompatibility().isCompatible()) {
               stringBuilder.append(" (incompatible)");
            }
         }

         return stringBuilder.toString();
      });
      if (this.serverId != null) {
         crashReport.getSystemDetailsSection().add("Server Id", () -> {
            return this.serverId;
         });
      }

      return crashReport;
   }

   public abstract Optional<String> method_24307();

   public boolean hasGameDir() {
      return this.gameDir != null;
   }

   public void sendMessage(Text message) {
      LOGGER.info(message.getString());
   }

   public KeyPair getKeyPair() {
      return this.keyPair;
   }

   public int getServerPort() {
      return this.serverPort;
   }

   public void setServerPort(int serverPort) {
      this.serverPort = serverPort;
   }

   public String getUserName() {
      return this.userName;
   }

   public void setUserName(String userName) {
      this.userName = userName;
   }

   public boolean isSinglePlayer() {
      return this.userName != null;
   }

   public String getLevelName() {
      return this.levelName;
   }

   @Environment(EnvType.CLIENT)
   public void setServerName(String serverName) {
      this.displayName = serverName;
   }

   @Environment(EnvType.CLIENT)
   public String getServerName() {
      return this.displayName;
   }

   public void setKeyPair(KeyPair keyPair) {
      this.keyPair = keyPair;
   }

   public void setDifficulty(Difficulty difficulty, boolean bl) {
      Iterator var3 = this.getWorlds().iterator();

      while(true) {
         ServerWorld serverWorld;
         LevelProperties levelProperties;
         do {
            if (!var3.hasNext()) {
               this.getPlayerManager().getPlayerList().forEach(this::sendDifficulty);
               return;
            }

            serverWorld = (ServerWorld)var3.next();
            levelProperties = serverWorld.getLevelProperties();
         } while(!bl && levelProperties.isDifficultyLocked());

         if (levelProperties.isHardcore()) {
            levelProperties.setDifficulty(Difficulty.HARD);
            serverWorld.setMobSpawnOptions(true, true);
         } else if (this.isSinglePlayer()) {
            levelProperties.setDifficulty(difficulty);
            serverWorld.setMobSpawnOptions(serverWorld.getDifficulty() != Difficulty.PEACEFUL, true);
         } else {
            levelProperties.setDifficulty(difficulty);
            serverWorld.setMobSpawnOptions(this.isMonsterSpawningEnabled(), this.spawnAnimals);
         }
      }
   }

   public void setDifficultyLocked(boolean locked) {
      Iterator var2 = this.getWorlds().iterator();

      while(var2.hasNext()) {
         ServerWorld serverWorld = (ServerWorld)var2.next();
         LevelProperties levelProperties = serverWorld.getLevelProperties();
         levelProperties.setDifficultyLocked(locked);
      }

      this.getPlayerManager().getPlayerList().forEach(this::sendDifficulty);
   }

   private void sendDifficulty(ServerPlayerEntity player) {
      LevelProperties levelProperties = player.getServerWorld().getLevelProperties();
      player.networkHandler.sendPacket(new DifficultyS2CPacket(levelProperties.getDifficulty(), levelProperties.isDifficultyLocked()));
   }

   protected boolean isMonsterSpawningEnabled() {
      return true;
   }

   public boolean isDemo() {
      return this.demo;
   }

   public void setDemo(boolean demo) {
      this.demo = demo;
   }

   public void setBonusChest(boolean bonusChest) {
      this.bonusChest = bonusChest;
   }

   public LevelStorage getLevelStorage() {
      return this.levelStorage;
   }

   public String getResourcePackUrl() {
      return this.resourcePackUrl;
   }

   public String getResourcePackHash() {
      return this.resourcePackHash;
   }

   public void setResourcePack(String url, String hash) {
      this.resourcePackUrl = url;
      this.resourcePackHash = hash;
   }

   public void addSnooperInfo(Snooper snooper) {
      snooper.addInfo("whitelist_enabled", false);
      snooper.addInfo("whitelist_count", 0);
      if (this.playerManager != null) {
         snooper.addInfo("players_current", this.getCurrentPlayerCount());
         snooper.addInfo("players_max", this.getMaxPlayerCount());
         snooper.addInfo("players_seen", this.getWorld(DimensionType.OVERWORLD).getSaveHandler().getSavedPlayerIds().length);
      }

      snooper.addInfo("uses_auth", this.onlineMode);
      snooper.addInfo("gui_state", this.hasGui() ? "enabled" : "disabled");
      snooper.addInfo("run_time", (Util.getMeasuringTimeMs() - snooper.getStartTime()) / 60L * 1000L);
      snooper.addInfo("avg_tick_ms", (int)(MathHelper.average(this.lastTickLengths) * 1.0E-6D));
      int i = 0;
      Iterator var3 = this.getWorlds().iterator();

      while(var3.hasNext()) {
         ServerWorld serverWorld = (ServerWorld)var3.next();
         if (serverWorld != null) {
            LevelProperties levelProperties = serverWorld.getLevelProperties();
            snooper.addInfo("world[" + i + "][dimension]", serverWorld.dimension.getType());
            snooper.addInfo("world[" + i + "][mode]", levelProperties.getGameMode());
            snooper.addInfo("world[" + i + "][difficulty]", serverWorld.getDifficulty());
            snooper.addInfo("world[" + i + "][hardcore]", levelProperties.isHardcore());
            snooper.addInfo("world[" + i + "][generator_name]", levelProperties.getGeneratorType().getName());
            snooper.addInfo("world[" + i + "][generator_version]", levelProperties.getGeneratorType().getVersion());
            snooper.addInfo("world[" + i + "][height]", this.worldHeight);
            snooper.addInfo("world[" + i + "][chunks_loaded]", serverWorld.getChunkManager().getLoadedChunkCount());
            ++i;
         }
      }

      snooper.addInfo("worlds", i);
   }

   public abstract boolean isDedicated();

   public boolean isOnlineMode() {
      return this.onlineMode;
   }

   public void setOnlineMode(boolean onlineMode) {
      this.onlineMode = onlineMode;
   }

   public boolean shouldPreventProxyConnections() {
      return this.preventProxyConnections;
   }

   public void setPreventProxyConnections(boolean preventProxyConnections) {
      this.preventProxyConnections = preventProxyConnections;
   }

   public boolean shouldSpawnAnimals() {
      return this.spawnAnimals;
   }

   public void setSpawnAnimals(boolean spawnAnimals) {
      this.spawnAnimals = spawnAnimals;
   }

   public boolean shouldSpawnNpcs() {
      return this.spawnNpcs;
   }

   public abstract boolean isUsingNativeTransport();

   public void setSpawnNpcs(boolean spawnNpcs) {
      this.spawnNpcs = spawnNpcs;
   }

   public boolean isPvpEnabled() {
      return this.pvpEnabled;
   }

   public void setPvpEnabled(boolean pvpEnabled) {
      this.pvpEnabled = pvpEnabled;
   }

   public boolean isFlightEnabled() {
      return this.flightEnabled;
   }

   public void setFlightEnabled(boolean flightEnabled) {
      this.flightEnabled = flightEnabled;
   }

   public abstract boolean areCommandBlocksEnabled();

   public String getServerMotd() {
      return this.motd;
   }

   public void setMotd(String motd) {
      this.motd = motd;
   }

   public int getWorldHeight() {
      return this.worldHeight;
   }

   public void setWorldHeight(int worldHeight) {
      this.worldHeight = worldHeight;
   }

   public boolean isStopped() {
      return this.stopped;
   }

   public PlayerManager getPlayerManager() {
      return this.playerManager;
   }

   public void setPlayerManager(PlayerManager playerManager) {
      this.playerManager = playerManager;
   }

   public abstract boolean isRemote();

   public void setDefaultGameMode(GameMode gameMode) {
      Iterator var2 = this.getWorlds().iterator();

      while(var2.hasNext()) {
         ServerWorld serverWorld = (ServerWorld)var2.next();
         serverWorld.getLevelProperties().setGameMode(gameMode);
      }

   }

   @Nullable
   public ServerNetworkIo getNetworkIo() {
      return this.networkIo;
   }

   @Environment(EnvType.CLIENT)
   public boolean isLoading() {
      return this.loading;
   }

   public boolean hasGui() {
      return false;
   }

   public abstract boolean openToLan(GameMode gameMode, boolean cheatsAllowed, int port);

   public int getTicks() {
      return this.ticks;
   }

   public void enableProfiler() {
      this.profilerStartQueued = true;
   }

   @Environment(EnvType.CLIENT)
   public Snooper getSnooper() {
      return this.snooper;
   }

   public int getSpawnProtectionRadius() {
      return 16;
   }

   public boolean isSpawnProtected(World world, BlockPos blockPos, PlayerEntity playerEntity) {
      return false;
   }

   public void setForceGameMode(boolean forceGameMode) {
      this.forceGameMode = forceGameMode;
   }

   public boolean shouldForceGameMode() {
      return this.forceGameMode;
   }

   public int getPlayerIdleTimeout() {
      return this.playerIdleTimeout;
   }

   public void setPlayerIdleTimeout(int playerIdleTimeout) {
      this.playerIdleTimeout = playerIdleTimeout;
   }

   public MinecraftSessionService getSessionService() {
      return this.sessionService;
   }

   public GameProfileRepository getGameProfileRepo() {
      return this.gameProfileRepo;
   }

   public UserCache getUserCache() {
      return this.userCache;
   }

   public ServerMetadata getServerMetadata() {
      return this.metadata;
   }

   public void forcePlayerSampleUpdate() {
      this.lastPlayerSampleUpdate = 0L;
   }

   public int getMaxWorldBorderRadius() {
      return 29999984;
   }

   public boolean shouldExecuteAsync() {
      return super.shouldExecuteAsync() && !this.isStopped();
   }

   public Thread getThread() {
      return this.serverThread;
   }

   public int getNetworkCompressionThreshold() {
      return 256;
   }

   public long getServerStartTime() {
      return this.timeReference;
   }

   public DataFixer getDataFixer() {
      return this.dataFixer;
   }

   public int getSpawnRadius(@Nullable ServerWorld world) {
      return world != null ? world.getGameRules().getInt(GameRules.SPAWN_RADIUS) : 10;
   }

   public ServerAdvancementLoader getAdvancementLoader() {
      return this.advancementLoader;
   }

   public CommandFunctionManager getCommandFunctionManager() {
      return this.commandFunctionManager;
   }

   public void reload() {
      if (!this.isOnThread()) {
         this.execute(this::reload);
      } else {
         this.getPlayerManager().saveAllPlayerData();
         this.dataPackManager.scanPacks();
         this.reloadDataPacks(this.getWorld(DimensionType.OVERWORLD).getLevelProperties());
         this.getPlayerManager().onDataPacksReloaded();
         this.method_24154();
      }
   }

   private void reloadDataPacks(LevelProperties levelProperties) {
      List<ResourcePackProfile> list = Lists.newArrayList(this.dataPackManager.getEnabledProfiles());
      Iterator var3 = this.dataPackManager.getProfiles().iterator();

      while(var3.hasNext()) {
         ResourcePackProfile resourcePackProfile = (ResourcePackProfile)var3.next();
         if (!levelProperties.getDisabledDataPacks().contains(resourcePackProfile.getName()) && !list.contains(resourcePackProfile)) {
            LOGGER.info("Found new data pack {}, loading it automatically", resourcePackProfile.getName());
            resourcePackProfile.getInitialPosition().insert(list, resourcePackProfile, (resourcePackProfilex) -> {
               return resourcePackProfilex;
            }, false);
         }
      }

      this.dataPackManager.setEnabledProfiles(list);
      List<ResourcePack> list2 = Lists.newArrayList();
      this.dataPackManager.getEnabledProfiles().forEach((resourcePackProfilex) -> {
         list2.add(resourcePackProfilex.createResourcePack());
      });
      CompletableFuture<Unit> completableFuture = this.dataManager.beginReload(this.workerExecutor, this, list2, COMPLETED_UNIT_FUTURE);
      this.runTasks(completableFuture::isDone);

      try {
         completableFuture.get();
      } catch (Exception var6) {
         LOGGER.error("Failed to reload data packs", var6);
      }

      levelProperties.getEnabledDataPacks().clear();
      levelProperties.getDisabledDataPacks().clear();
      this.dataPackManager.getEnabledProfiles().forEach((resourcePackProfilex) -> {
         levelProperties.getEnabledDataPacks().add(resourcePackProfilex.getName());
      });
      this.dataPackManager.getProfiles().forEach((resourcePackProfilex) -> {
         if (!this.dataPackManager.getEnabledProfiles().contains(resourcePackProfilex)) {
            levelProperties.getDisabledDataPacks().add(resourcePackProfilex.getName());
         }

      });
   }

   public void kickNonWhitelistedPlayers(ServerCommandSource source) {
      if (this.isEnforceWhitelist()) {
         PlayerManager playerManager = source.getMinecraftServer().getPlayerManager();
         Whitelist whitelist = playerManager.getWhitelist();
         if (whitelist.isEnabled()) {
            List<ServerPlayerEntity> list = Lists.newArrayList(playerManager.getPlayerList());
            Iterator var5 = list.iterator();

            while(var5.hasNext()) {
               ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var5.next();
               if (!whitelist.isAllowed(serverPlayerEntity.getGameProfile())) {
                  serverPlayerEntity.networkHandler.disconnect(new TranslatableText("multiplayer.disconnect.not_whitelisted", new Object[0]));
               }
            }

         }
      }
   }

   public ReloadableResourceManager getDataManager() {
      return this.dataManager;
   }

   public ResourcePackManager<ResourcePackProfile> getDataPackManager() {
      return this.dataPackManager;
   }

   public CommandManager getCommandManager() {
      return this.commandManager;
   }

   public ServerCommandSource getCommandSource() {
      return new ServerCommandSource(this, this.getWorld(DimensionType.OVERWORLD) == null ? Vec3d.ZERO : new Vec3d(this.getWorld(DimensionType.OVERWORLD).getSpawnPos()), Vec2f.ZERO, this.getWorld(DimensionType.OVERWORLD), 4, "Server", new LiteralText("Server"), this, (Entity)null);
   }

   public boolean sendCommandFeedback() {
      return true;
   }

   public boolean shouldTrackOutput() {
      return true;
   }

   public RecipeManager getRecipeManager() {
      return this.recipeManager;
   }

   public RegistryTagManager getTagManager() {
      return this.tagManager;
   }

   public ServerScoreboard getScoreboard() {
      return this.scoreboard;
   }

   public DataCommandStorage getDataCommandStorage() {
      if (this.dataCommandStorage == null) {
         throw new NullPointerException("Called before server init");
      } else {
         return this.dataCommandStorage;
      }
   }

   public LootManager getLootManager() {
      return this.lootManager;
   }

   public LootConditionManager getPredicateManager() {
      return this.predicateManager;
   }

   public GameRules getGameRules() {
      return this.getWorld(DimensionType.OVERWORLD).getGameRules();
   }

   public BossBarManager getBossBarManager() {
      return this.bossBarManager;
   }

   public boolean isEnforceWhitelist() {
      return this.enforceWhitelist;
   }

   public void setEnforceWhitelist(boolean whitelistEnabled) {
      this.enforceWhitelist = whitelistEnabled;
   }

   public float getTickTime() {
      return this.tickTime;
   }

   public int getPermissionLevel(GameProfile profile) {
      if (this.getPlayerManager().isOperator(profile)) {
         OperatorEntry operatorEntry = (OperatorEntry)this.getPlayerManager().getOpList().get(profile);
         if (operatorEntry != null) {
            return operatorEntry.getPermissionLevel();
         } else if (this.isOwner(profile)) {
            return 4;
         } else if (this.isSinglePlayer()) {
            return this.getPlayerManager().areCheatsAllowed() ? 4 : 0;
         } else {
            return this.getOpPermissionLevel();
         }
      } else {
         return 0;
      }
   }

   @Environment(EnvType.CLIENT)
   public MetricsData getMetricsData() {
      return this.metricsData;
   }

   public DisableableProfiler getProfiler() {
      return this.profiler;
   }

   public Executor getWorkerExecutor() {
      return this.workerExecutor;
   }

   public abstract boolean isOwner(GameProfile profile);

   public void dump(Path path) throws IOException {
      Path path2 = path.resolve("levels");
      Iterator var3 = this.worlds.entrySet().iterator();

      while(var3.hasNext()) {
         Entry<DimensionType, ServerWorld> entry = (Entry)var3.next();
         Identifier identifier = DimensionType.getId((DimensionType)entry.getKey());
         Path path3 = path2.resolve(identifier.getNamespace()).resolve(identifier.getPath());
         Files.createDirectories(path3);
         ((ServerWorld)entry.getValue()).method_21625(path3);
      }

      this.dumpGamerules(path.resolve("gamerules.txt"));
      this.dumpClasspath(path.resolve("classpath.txt"));
      this.dumpExampleCrash(path.resolve("example_crash.txt"));
      this.dumpStats(path.resolve("stats.txt"));
      this.dumpThreads(path.resolve("threads.txt"));
   }

   private void dumpStats(Path path) throws IOException {
      Writer writer = Files.newBufferedWriter(path);
      Throwable var3 = null;

      try {
         writer.write(String.format("pending_tasks: %d\n", this.getTaskCount()));
         writer.write(String.format("average_tick_time: %f\n", this.getTickTime()));
         writer.write(String.format("tick_times: %s\n", Arrays.toString(this.lastTickLengths)));
         writer.write(String.format("queue: %s\n", Util.getServerWorkerExecutor()));
      } catch (Throwable var12) {
         var3 = var12;
         throw var12;
      } finally {
         if (writer != null) {
            if (var3 != null) {
               try {
                  writer.close();
               } catch (Throwable var11) {
                  var3.addSuppressed(var11);
               }
            } else {
               writer.close();
            }
         }

      }

   }

   private void dumpExampleCrash(Path path) throws IOException {
      CrashReport crashReport = new CrashReport("Server dump", new Exception("dummy"));
      this.populateCrashReport(crashReport);
      Writer writer = Files.newBufferedWriter(path);
      Throwable var4 = null;

      try {
         writer.write(crashReport.asString());
      } catch (Throwable var13) {
         var4 = var13;
         throw var13;
      } finally {
         if (writer != null) {
            if (var4 != null) {
               try {
                  writer.close();
               } catch (Throwable var12) {
                  var4.addSuppressed(var12);
               }
            } else {
               writer.close();
            }
         }

      }

   }

   private void dumpGamerules(Path path) throws IOException {
      Writer writer = Files.newBufferedWriter(path);
      Throwable var3 = null;

      try {
         final List<String> list = Lists.newArrayList();
         final GameRules gameRules = this.getGameRules();
         GameRules.forEachType(new GameRules.RuleTypeConsumer() {
            public <T extends GameRules.Rule<T>> void accept(GameRules.RuleKey<T> key, GameRules.RuleType<T> type) {
               list.add(String.format("%s=%s\n", key.getName(), gameRules.get(key).toString()));
            }
         });
         Iterator var6 = list.iterator();

         while(var6.hasNext()) {
            String string = (String)var6.next();
            writer.write(string);
         }
      } catch (Throwable var15) {
         var3 = var15;
         throw var15;
      } finally {
         if (writer != null) {
            if (var3 != null) {
               try {
                  writer.close();
               } catch (Throwable var14) {
                  var3.addSuppressed(var14);
               }
            } else {
               writer.close();
            }
         }

      }

   }

   private void dumpClasspath(Path path) throws IOException {
      Writer writer = Files.newBufferedWriter(path);
      Throwable var3 = null;

      try {
         String string = System.getProperty("java.class.path");
         String string2 = System.getProperty("path.separator");
         Iterator var6 = Splitter.on(string2).split(string).iterator();

         while(var6.hasNext()) {
            String string3 = (String)var6.next();
            writer.write(string3);
            writer.write("\n");
         }
      } catch (Throwable var15) {
         var3 = var15;
         throw var15;
      } finally {
         if (writer != null) {
            if (var3 != null) {
               try {
                  writer.close();
               } catch (Throwable var14) {
                  var3.addSuppressed(var14);
               }
            } else {
               writer.close();
            }
         }

      }

   }

   private void dumpThreads(Path path) throws IOException {
      ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
      ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
      Arrays.sort(threadInfos, Comparator.comparing(ThreadInfo::getThreadName));
      Writer writer = Files.newBufferedWriter(path);
      Throwable var5 = null;

      try {
         ThreadInfo[] var6 = threadInfos;
         int var7 = threadInfos.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            ThreadInfo threadInfo = var6[var8];
            writer.write(threadInfo.toString());
            writer.write(10);
         }
      } catch (Throwable var17) {
         var5 = var17;
         throw var17;
      } finally {
         if (writer != null) {
            if (var5 != null) {
               try {
                  writer.close();
               } catch (Throwable var16) {
                  var5.addSuppressed(var16);
               }
            } else {
               writer.close();
            }
         }

      }

   }

   private void method_24154() {
      Block.STATE_IDS.forEach(BlockState::initShapeCache);
   }

   static {
      COMPLETED_UNIT_FUTURE = CompletableFuture.completedFuture(Unit.INSTANCE);
      DEMO_LEVEL_INFO = (new LevelInfo((long)"North Carolina".hashCode(), GameMode.SURVIVAL, true, false, LevelGeneratorType.DEFAULT)).setBonusChest();
   }
}
