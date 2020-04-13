package net.minecraft.world.updater;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenCustomHashMap;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.storage.RegionFile;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldUpdater {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final ThreadFactory UPDATE_THREAD_FACTORY = (new ThreadFactoryBuilder()).setDaemon(true).build();
   private final String levelName;
   private final boolean eraseCache;
   private final WorldSaveHandler worldSaveHandler;
   private final Thread updateThread;
   private final File worldDirectory;
   private volatile boolean keepUpgradingChunks = true;
   private volatile boolean isDone;
   private volatile float progress;
   private volatile int totalChunkCount;
   private volatile int upgradedChunkCount;
   private volatile int skippedChunkCount;
   private final Object2FloatMap<DimensionType> dimensionProgress = Object2FloatMaps.synchronize(new Object2FloatOpenCustomHashMap(Util.identityHashStrategy()));
   private volatile Text status = new TranslatableText("optimizeWorld.stage.counting", new Object[0]);
   private static final Pattern REGION_FILE_PATTERN = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");
   private final PersistentStateManager persistentStateManager;

   public WorldUpdater(String string, LevelStorage levelStorage, LevelProperties levelProperties, boolean eraseCache) {
      this.levelName = levelProperties.getLevelName();
      this.eraseCache = eraseCache;
      this.worldSaveHandler = levelStorage.createSaveHandler(string, (MinecraftServer)null);
      this.worldSaveHandler.saveWorld(levelProperties);
      this.persistentStateManager = new PersistentStateManager(new File(DimensionType.OVERWORLD.getSaveDirectory(this.worldSaveHandler.getWorldDir()), "data"), this.worldSaveHandler.getDataFixer());
      this.worldDirectory = this.worldSaveHandler.getWorldDir();
      this.updateThread = UPDATE_THREAD_FACTORY.newThread(this::updateWorld);
      this.updateThread.setUncaughtExceptionHandler((thread, throwable) -> {
         LOGGER.error("Error upgrading world", throwable);
         this.status = new TranslatableText("optimizeWorld.stage.failed", new Object[0]);
         this.isDone = true;
      });
      this.updateThread.start();
   }

   public void cancel() {
      this.keepUpgradingChunks = false;

      try {
         this.updateThread.join();
      } catch (InterruptedException var2) {
      }

   }

   private void updateWorld() {
      File file = this.worldSaveHandler.getWorldDir();
      this.totalChunkCount = 0;
      Builder<DimensionType, ListIterator<ChunkPos>> builder = ImmutableMap.builder();

      List list;
      for(Iterator var3 = DimensionType.getAll().iterator(); var3.hasNext(); this.totalChunkCount += list.size()) {
         DimensionType dimensionType = (DimensionType)var3.next();
         list = this.getChunkPositions(dimensionType);
         builder.put(dimensionType, list.listIterator());
      }

      if (this.totalChunkCount == 0) {
         this.isDone = true;
      } else {
         float f = (float)this.totalChunkCount;
         ImmutableMap<DimensionType, ListIterator<ChunkPos>> immutableMap = builder.build();
         Builder<DimensionType, VersionedChunkStorage> builder2 = ImmutableMap.builder();
         Iterator var6 = DimensionType.getAll().iterator();

         while(var6.hasNext()) {
            DimensionType dimensionType2 = (DimensionType)var6.next();
            File file2 = dimensionType2.getSaveDirectory(file);
            builder2.put(dimensionType2, new VersionedChunkStorage(new File(file2, "region"), this.worldSaveHandler.getDataFixer()));
         }

         ImmutableMap<DimensionType, VersionedChunkStorage> immutableMap2 = builder2.build();
         long l = Util.getMeasuringTimeMs();
         this.status = new TranslatableText("optimizeWorld.stage.upgrading", new Object[0]);

         while(this.keepUpgradingChunks) {
            boolean bl = false;
            float g = 0.0F;

            float h;
            for(Iterator var11 = DimensionType.getAll().iterator(); var11.hasNext(); g += h) {
               DimensionType dimensionType3 = (DimensionType)var11.next();
               ListIterator<ChunkPos> listIterator = (ListIterator)immutableMap.get(dimensionType3);
               VersionedChunkStorage versionedChunkStorage = (VersionedChunkStorage)immutableMap2.get(dimensionType3);
               if (listIterator.hasNext()) {
                  ChunkPos chunkPos = (ChunkPos)listIterator.next();
                  boolean bl2 = false;

                  try {
                     CompoundTag compoundTag = versionedChunkStorage.getNbt(chunkPos);
                     if (compoundTag != null) {
                        int i = VersionedChunkStorage.getDataVersion(compoundTag);
                        CompoundTag compoundTag2 = versionedChunkStorage.updateChunkTag(dimensionType3, () -> {
                           return this.persistentStateManager;
                        }, compoundTag);
                        CompoundTag compoundTag3 = compoundTag2.getCompound("Level");
                        ChunkPos chunkPos2 = new ChunkPos(compoundTag3.getInt("xPos"), compoundTag3.getInt("zPos"));
                        if (!chunkPos2.equals(chunkPos)) {
                           LOGGER.warn("Chunk {} has invalid position {}", chunkPos, chunkPos2);
                        }

                        boolean bl3 = i < SharedConstants.getGameVersion().getWorldVersion();
                        if (this.eraseCache) {
                           bl3 = bl3 || compoundTag3.contains("Heightmaps");
                           compoundTag3.remove("Heightmaps");
                           bl3 = bl3 || compoundTag3.contains("isLightOn");
                           compoundTag3.remove("isLightOn");
                        }

                        if (bl3) {
                           versionedChunkStorage.setTagAt(chunkPos, compoundTag2);
                           bl2 = true;
                        }
                     }
                  } catch (CrashException var24) {
                     Throwable throwable = var24.getCause();
                     if (!(throwable instanceof IOException)) {
                        throw var24;
                     }

                     LOGGER.error("Error upgrading chunk {}", chunkPos, throwable);
                  } catch (IOException var25) {
                     LOGGER.error("Error upgrading chunk {}", chunkPos, var25);
                  }

                  if (bl2) {
                     ++this.upgradedChunkCount;
                  } else {
                     ++this.skippedChunkCount;
                  }

                  bl = true;
               }

               h = (float)listIterator.nextIndex() / f;
               this.dimensionProgress.put(dimensionType3, h);
            }

            this.progress = g;
            if (!bl) {
               this.keepUpgradingChunks = false;
            }
         }

         this.status = new TranslatableText("optimizeWorld.stage.finished", new Object[0]);
         UnmodifiableIterator var31 = immutableMap2.values().iterator();

         while(var31.hasNext()) {
            VersionedChunkStorage versionedChunkStorage2 = (VersionedChunkStorage)var31.next();

            try {
               versionedChunkStorage2.close();
            } catch (IOException var23) {
               LOGGER.error("Error upgrading chunk", var23);
            }
         }

         this.persistentStateManager.save();
         l = Util.getMeasuringTimeMs() - l;
         LOGGER.info("World optimizaton finished after {} ms", l);
         this.isDone = true;
      }
   }

   private List<ChunkPos> getChunkPositions(DimensionType dimensionType) {
      File file = dimensionType.getSaveDirectory(this.worldDirectory);
      File file2 = new File(file, "region");
      File[] files = file2.listFiles((filex, string) -> {
         return string.endsWith(".mca");
      });
      if (files == null) {
         return ImmutableList.of();
      } else {
         List<ChunkPos> list = Lists.newArrayList();
         File[] var6 = files;
         int var7 = files.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            File file3 = var6[var8];
            Matcher matcher = REGION_FILE_PATTERN.matcher(file3.getName());
            if (matcher.matches()) {
               int i = Integer.parseInt(matcher.group(1)) << 5;
               int j = Integer.parseInt(matcher.group(2)) << 5;

               try {
                  RegionFile regionFile = new RegionFile(file3, file2);
                  Throwable var14 = null;

                  try {
                     for(int k = 0; k < 32; ++k) {
                        for(int l = 0; l < 32; ++l) {
                           ChunkPos chunkPos = new ChunkPos(k + i, l + j);
                           if (regionFile.isChunkValid(chunkPos)) {
                              list.add(chunkPos);
                           }
                        }
                     }
                  } catch (Throwable var26) {
                     var14 = var26;
                     throw var26;
                  } finally {
                     if (regionFile != null) {
                        if (var14 != null) {
                           try {
                              regionFile.close();
                           } catch (Throwable var25) {
                              var14.addSuppressed(var25);
                           }
                        } else {
                           regionFile.close();
                        }
                     }

                  }
               } catch (Throwable var28) {
               }
            }
         }

         return list;
      }
   }

   public boolean isDone() {
      return this.isDone;
   }

   @Environment(EnvType.CLIENT)
   public float getProgress(DimensionType dimensionType) {
      return this.dimensionProgress.getFloat(dimensionType);
   }

   @Environment(EnvType.CLIENT)
   public float getProgress() {
      return this.progress;
   }

   public int getTotalChunkCount() {
      return this.totalChunkCount;
   }

   public int getUpgradedChunkCount() {
      return this.upgradedChunkCount;
   }

   public int getSkippedChunkCount() {
      return this.skippedChunkCount;
   }

   public Text getStatus() {
      return this.status;
   }
}
