package net.minecraft.world.level.storage;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.BiomeSourceType;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.biome.source.FixedBiomeSourceConfig;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSourceConfig;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.storage.RegionFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnvilLevelStorage {
   private static final Logger LOGGER = LogManager.getLogger();

   static boolean convertLevel(Path path, DataFixer dataFixer, String string, ProgressListener progressListener) {
      progressListener.progressStagePercentage(0);
      List<File> list = Lists.newArrayList();
      List<File> list2 = Lists.newArrayList();
      List<File> list3 = Lists.newArrayList();
      File file = new File(path.toFile(), string);
      File file2 = DimensionType.THE_NETHER.getSaveDirectory(file);
      File file3 = DimensionType.THE_END.getSaveDirectory(file);
      LOGGER.info("Scanning folders...");
      addRegionFiles(file, list);
      if (file2.exists()) {
         addRegionFiles(file2, list2);
      }

      if (file3.exists()) {
         addRegionFiles(file3, list3);
      }

      int i = list.size() + list2.size() + list3.size();
      LOGGER.info("Total conversion count is {}", i);
      LevelProperties levelProperties = LevelStorage.getLevelProperties(path, dataFixer, string);
      BiomeSourceType<FixedBiomeSourceConfig, FixedBiomeSource> biomeSourceType = BiomeSourceType.FIXED;
      BiomeSourceType<VanillaLayeredBiomeSourceConfig, VanillaLayeredBiomeSource> biomeSourceType2 = BiomeSourceType.VANILLA_LAYERED;
      BiomeSource biomeSource2;
      if (levelProperties != null && levelProperties.getGeneratorType() == LevelGeneratorType.FLAT) {
         biomeSource2 = biomeSourceType.applyConfig(((FixedBiomeSourceConfig)biomeSourceType.getConfig(levelProperties)).setBiome(Biomes.PLAINS));
      } else {
         biomeSource2 = biomeSourceType2.applyConfig(biomeSourceType2.getConfig(levelProperties));
      }

      convertRegions(new File(file, "region"), list, biomeSource2, 0, i, progressListener);
      convertRegions(new File(file2, "region"), list2, biomeSourceType.applyConfig(((FixedBiomeSourceConfig)biomeSourceType.getConfig(levelProperties)).setBiome(Biomes.NETHER)), list.size(), i, progressListener);
      convertRegions(new File(file3, "region"), list3, biomeSourceType.applyConfig(((FixedBiomeSourceConfig)biomeSourceType.getConfig(levelProperties)).setBiome(Biomes.THE_END)), list.size() + list2.size(), i, progressListener);
      levelProperties.setVersion(19133);
      if (levelProperties.getGeneratorType() == LevelGeneratorType.DEFAULT_1_1) {
         levelProperties.setGeneratorType(LevelGeneratorType.DEFAULT);
      }

      makeMcrLevelDatBackup(path, string);
      WorldSaveHandler worldSaveHandler = LevelStorage.createSaveHandler(path, dataFixer, string, (MinecraftServer)null);
      worldSaveHandler.saveWorld(levelProperties);
      return true;
   }

   private static void makeMcrLevelDatBackup(Path path, String string) {
      File file = new File(path.toFile(), string);
      if (!file.exists()) {
         LOGGER.warn("Unable to create level.dat_mcr backup");
      } else {
         File file2 = new File(file, "level.dat");
         if (!file2.exists()) {
            LOGGER.warn("Unable to create level.dat_mcr backup");
         } else {
            File file3 = new File(file, "level.dat_mcr");
            if (!file2.renameTo(file3)) {
               LOGGER.warn("Unable to create level.dat_mcr backup");
            }

         }
      }
   }

   private static void convertRegions(File file, Iterable<File> iterable, BiomeSource biomeSource, int i, int currentCount, ProgressListener progressListener) {
      Iterator var6 = iterable.iterator();

      while(var6.hasNext()) {
         File file2 = (File)var6.next();
         convertRegion(file, file2, biomeSource, i, currentCount, progressListener);
         ++i;
         int j = (int)Math.round(100.0D * (double)i / (double)currentCount);
         progressListener.progressStagePercentage(j);
      }

   }

   private static void convertRegion(File file, File baseFolder, BiomeSource biomeSource, int i, int progressStart, ProgressListener progressListener) {
      String string = baseFolder.getName();

      try {
         RegionFile regionFile = new RegionFile(baseFolder, file);
         Throwable var8 = null;

         try {
            RegionFile regionFile2 = new RegionFile(new File(file, string.substring(0, string.length() - ".mcr".length()) + ".mca"), file);
            Throwable var10 = null;

            try {
               for(int j = 0; j < 32; ++j) {
                  int l;
                  for(l = 0; l < 32; ++l) {
                     ChunkPos chunkPos = new ChunkPos(j, l);
                     if (regionFile.hasChunk(chunkPos) && !regionFile2.hasChunk(chunkPos)) {
                        CompoundTag compoundTag3;
                        try {
                           DataInputStream dataInputStream = regionFile.getChunkInputStream(chunkPos);
                           Throwable var16 = null;

                           try {
                              if (dataInputStream == null) {
                                 LOGGER.warn("Failed to fetch input stream for chunk {}", chunkPos);
                                 continue;
                              }

                              compoundTag3 = NbtIo.read(dataInputStream);
                           } catch (Throwable var104) {
                              var16 = var104;
                              throw var104;
                           } finally {
                              if (dataInputStream != null) {
                                 if (var16 != null) {
                                    try {
                                       dataInputStream.close();
                                    } catch (Throwable var101) {
                                       var16.addSuppressed(var101);
                                    }
                                 } else {
                                    dataInputStream.close();
                                 }
                              }

                           }
                        } catch (IOException var106) {
                           LOGGER.warn("Failed to read data for chunk {}", chunkPos, var106);
                           continue;
                        }

                        CompoundTag compoundTag4 = compoundTag3.getCompound("Level");
                        AlphaChunkIo.AlphaChunk alphaChunk = AlphaChunkIo.readAlphaChunk(compoundTag4);
                        CompoundTag compoundTag5 = new CompoundTag();
                        CompoundTag compoundTag6 = new CompoundTag();
                        compoundTag5.put("Level", compoundTag6);
                        AlphaChunkIo.convertAlphaChunk(alphaChunk, compoundTag6, biomeSource);
                        DataOutputStream dataOutputStream = regionFile2.getChunkOutputStream(chunkPos);
                        Throwable var20 = null;

                        try {
                           NbtIo.write((CompoundTag)compoundTag5, (DataOutput)dataOutputStream);
                        } catch (Throwable var102) {
                           var20 = var102;
                           throw var102;
                        } finally {
                           if (dataOutputStream != null) {
                              if (var20 != null) {
                                 try {
                                    dataOutputStream.close();
                                 } catch (Throwable var100) {
                                    var20.addSuppressed(var100);
                                 }
                              } else {
                                 dataOutputStream.close();
                              }
                           }

                        }
                     }
                  }

                  l = (int)Math.round(100.0D * (double)(i * 1024) / (double)(progressStart * 1024));
                  int m = (int)Math.round(100.0D * (double)((j + 1) * 32 + i * 1024) / (double)(progressStart * 1024));
                  if (m > l) {
                     progressListener.progressStagePercentage(m);
                  }
               }
            } catch (Throwable var107) {
               var10 = var107;
               throw var107;
            } finally {
               if (regionFile2 != null) {
                  if (var10 != null) {
                     try {
                        regionFile2.close();
                     } catch (Throwable var99) {
                        var10.addSuppressed(var99);
                     }
                  } else {
                     regionFile2.close();
                  }
               }

            }
         } catch (Throwable var109) {
            var8 = var109;
            throw var109;
         } finally {
            if (regionFile != null) {
               if (var8 != null) {
                  try {
                     regionFile.close();
                  } catch (Throwable var98) {
                     var8.addSuppressed(var98);
                  }
               } else {
                  regionFile.close();
               }
            }

         }
      } catch (IOException var111) {
         LOGGER.error("Failed to upgrade region file {}", baseFolder, var111);
      }

   }

   private static void addRegionFiles(File file, Collection<File> collection) {
      File file2 = new File(file, "region");
      File[] files = file2.listFiles((filex, string) -> {
         return string.endsWith(".mcr");
      });
      if (files != null) {
         Collections.addAll(collection, files);
      }

   }
}
