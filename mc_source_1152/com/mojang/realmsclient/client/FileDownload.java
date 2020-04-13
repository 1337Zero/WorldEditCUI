package com.mojang.realmsclient.client;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.exception.RealmsDefaultUncaughtExceptionHandler;
import com.mojang.realmsclient.gui.screens.RealmsDownloadLatestWorldScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsAnvilLevelStorageSource;
import net.minecraft.realms.RealmsLevelSummary;
import net.minecraft.realms.RealmsSharedConstants;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class FileDownload {
   private static final Logger LOGGER = LogManager.getLogger();
   private volatile boolean cancelled;
   private volatile boolean finished;
   private volatile boolean error;
   private volatile boolean extracting;
   private volatile File field_20490;
   private volatile File resourcePackPath;
   private volatile HttpGet field_20491;
   private Thread currentThread;
   private final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(120000).setConnectTimeout(120000).build();
   private static final String[] INVALID_FILE_NAMES = new String[]{"CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

   public long contentLength(String downloadLink) {
      CloseableHttpClient closeableHttpClient = null;
      HttpGet httpGet = null;

      long var5;
      try {
         httpGet = new HttpGet(downloadLink);
         closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
         CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
         var5 = Long.parseLong(closeableHttpResponse.getFirstHeader("Content-Length").getValue());
         return var5;
      } catch (Throwable var16) {
         LOGGER.error("Unable to get content length for download");
         var5 = 0L;
      } finally {
         if (httpGet != null) {
            httpGet.releaseConnection();
         }

         if (closeableHttpClient != null) {
            try {
               closeableHttpClient.close();
            } catch (IOException var15) {
               LOGGER.error("Could not close http client", var15);
            }
         }

      }

      return var5;
   }

   public void method_22100(WorldDownload worldDownload, String string, RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus, RealmsAnvilLevelStorageSource realmsAnvilLevelStorageSource) {
      if (this.currentThread == null) {
         this.currentThread = new Thread(() -> {
            CloseableHttpClient closeableHttpClient = null;
            boolean var90 = false;

            label1408: {
               CloseableHttpResponse httpResponse4;
               FileOutputStream outputStream4;
               FileDownload.DownloadCountingOutputStream downloadCountingOutputStream4;
               FileDownload.ResourcePackProgressListener resourcePackProgressListener3;
               label1402: {
                  try {
                     var90 = true;
                     this.field_20490 = File.createTempFile("backup", ".tar.gz");
                     this.field_20491 = new HttpGet(worldDownload.downloadLink);
                     closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
                     httpResponse4 = closeableHttpClient.execute(this.field_20491);
                     downloadStatus.totalBytes = Long.parseLong(httpResponse4.getFirstHeader("Content-Length").getValue());
                     if (httpResponse4.getStatusLine().getStatusCode() != 200) {
                        this.error = true;
                        this.field_20491.abort();
                        var90 = false;
                        break label1408;
                     }

                     outputStream4 = new FileOutputStream(this.field_20490);
                     FileDownload.ProgressListener progressListener = new FileDownload.ProgressListener(string.trim(), this.field_20490, realmsAnvilLevelStorageSource, downloadStatus, worldDownload);
                     downloadCountingOutputStream4 = new FileDownload.DownloadCountingOutputStream(outputStream4);
                     downloadCountingOutputStream4.setListener(progressListener);
                     IOUtils.copy(httpResponse4.getEntity().getContent(), downloadCountingOutputStream4);
                     var90 = false;
                     break label1402;
                  } catch (Exception var103) {
                     LOGGER.error("Caught exception while downloading: " + var103.getMessage());
                     this.error = true;
                     var90 = false;
                  } finally {
                     if (var90) {
                        this.field_20491.releaseConnection();
                        if (this.field_20490 != null) {
                           this.field_20490.delete();
                        }

                        if (!this.error) {
                           if (!worldDownload.resourcePackUrl.isEmpty() && !worldDownload.resourcePackHash.isEmpty()) {
                              try {
                                 this.field_20490 = File.createTempFile("resources", ".tar.gz");
                                 this.field_20491 = new HttpGet(worldDownload.resourcePackUrl);
                                 HttpResponse httpResponse5 = closeableHttpClient.execute(this.field_20491);
                                 downloadStatus.totalBytes = Long.parseLong(httpResponse5.getFirstHeader("Content-Length").getValue());
                                 if (httpResponse5.getStatusLine().getStatusCode() != 200) {
                                    this.error = true;
                                    this.field_20491.abort();
                                    return;
                                 }

                                 OutputStream outputStream5 = new FileOutputStream(this.field_20490);
                                 FileDownload.ResourcePackProgressListener resourcePackProgressListener4 = new FileDownload.ResourcePackProgressListener(this.field_20490, downloadStatus, worldDownload);
                                 FileDownload.DownloadCountingOutputStream downloadCountingOutputStream5 = new FileDownload.DownloadCountingOutputStream(outputStream5);
                                 downloadCountingOutputStream5.setListener(resourcePackProgressListener4);
                                 IOUtils.copy(httpResponse5.getEntity().getContent(), downloadCountingOutputStream5);
                              } catch (Exception var95) {
                                 LOGGER.error("Caught exception while downloading: " + var95.getMessage());
                                 this.error = true;
                              } finally {
                                 this.field_20491.releaseConnection();
                                 if (this.field_20490 != null) {
                                    this.field_20490.delete();
                                 }

                              }
                           } else {
                              this.finished = true;
                           }
                        }

                        if (closeableHttpClient != null) {
                           try {
                              closeableHttpClient.close();
                           } catch (IOException var91) {
                              LOGGER.error("Failed to close Realms download client");
                           }
                        }

                     }
                  }

                  this.field_20491.releaseConnection();
                  if (this.field_20490 != null) {
                     this.field_20490.delete();
                  }

                  if (!this.error) {
                     if (!worldDownload.resourcePackUrl.isEmpty() && !worldDownload.resourcePackHash.isEmpty()) {
                        try {
                           this.field_20490 = File.createTempFile("resources", ".tar.gz");
                           this.field_20491 = new HttpGet(worldDownload.resourcePackUrl);
                           httpResponse4 = closeableHttpClient.execute(this.field_20491);
                           downloadStatus.totalBytes = Long.parseLong(httpResponse4.getFirstHeader("Content-Length").getValue());
                           if (httpResponse4.getStatusLine().getStatusCode() != 200) {
                              this.error = true;
                              this.field_20491.abort();
                              return;
                           }

                           outputStream4 = new FileOutputStream(this.field_20490);
                           resourcePackProgressListener3 = new FileDownload.ResourcePackProgressListener(this.field_20490, downloadStatus, worldDownload);
                           downloadCountingOutputStream4 = new FileDownload.DownloadCountingOutputStream(outputStream4);
                           downloadCountingOutputStream4.setListener(resourcePackProgressListener3);
                           IOUtils.copy(httpResponse4.getEntity().getContent(), downloadCountingOutputStream4);
                        } catch (Exception var99) {
                           LOGGER.error("Caught exception while downloading: " + var99.getMessage());
                           this.error = true;
                        } finally {
                           this.field_20491.releaseConnection();
                           if (this.field_20490 != null) {
                              this.field_20490.delete();
                           }

                        }
                     } else {
                        this.finished = true;
                     }
                  }

                  if (closeableHttpClient != null) {
                     try {
                        closeableHttpClient.close();
                     } catch (IOException var93) {
                        LOGGER.error("Failed to close Realms download client");
                     }

                     return;
                  }

                  return;
               }

               this.field_20491.releaseConnection();
               if (this.field_20490 != null) {
                  this.field_20490.delete();
               }

               if (!this.error) {
                  if (!worldDownload.resourcePackUrl.isEmpty() && !worldDownload.resourcePackHash.isEmpty()) {
                     try {
                        this.field_20490 = File.createTempFile("resources", ".tar.gz");
                        this.field_20491 = new HttpGet(worldDownload.resourcePackUrl);
                        httpResponse4 = closeableHttpClient.execute(this.field_20491);
                        downloadStatus.totalBytes = Long.parseLong(httpResponse4.getFirstHeader("Content-Length").getValue());
                        if (httpResponse4.getStatusLine().getStatusCode() != 200) {
                           this.error = true;
                           this.field_20491.abort();
                           return;
                        }

                        outputStream4 = new FileOutputStream(this.field_20490);
                        resourcePackProgressListener3 = new FileDownload.ResourcePackProgressListener(this.field_20490, downloadStatus, worldDownload);
                        downloadCountingOutputStream4 = new FileDownload.DownloadCountingOutputStream(outputStream4);
                        downloadCountingOutputStream4.setListener(resourcePackProgressListener3);
                        IOUtils.copy(httpResponse4.getEntity().getContent(), downloadCountingOutputStream4);
                     } catch (Exception var101) {
                        LOGGER.error("Caught exception while downloading: " + var101.getMessage());
                        this.error = true;
                     } finally {
                        this.field_20491.releaseConnection();
                        if (this.field_20490 != null) {
                           this.field_20490.delete();
                        }

                     }
                  } else {
                     this.finished = true;
                  }
               }

               if (closeableHttpClient != null) {
                  try {
                     closeableHttpClient.close();
                  } catch (IOException var94) {
                     LOGGER.error("Failed to close Realms download client");
                  }
               }

               return;
            }

            this.field_20491.releaseConnection();
            if (this.field_20490 != null) {
               this.field_20490.delete();
            }

            if (!this.error) {
               if (!worldDownload.resourcePackUrl.isEmpty() && !worldDownload.resourcePackHash.isEmpty()) {
                  try {
                     this.field_20490 = File.createTempFile("resources", ".tar.gz");
                     this.field_20491 = new HttpGet(worldDownload.resourcePackUrl);
                     HttpResponse httpResponse2 = closeableHttpClient.execute(this.field_20491);
                     downloadStatus.totalBytes = Long.parseLong(httpResponse2.getFirstHeader("Content-Length").getValue());
                     if (httpResponse2.getStatusLine().getStatusCode() != 200) {
                        this.error = true;
                        this.field_20491.abort();
                        return;
                     }

                     OutputStream outputStream = new FileOutputStream(this.field_20490);
                     FileDownload.ResourcePackProgressListener resourcePackProgressListener = new FileDownload.ResourcePackProgressListener(this.field_20490, downloadStatus, worldDownload);
                     FileDownload.DownloadCountingOutputStream downloadCountingOutputStream = new FileDownload.DownloadCountingOutputStream(outputStream);
                     downloadCountingOutputStream.setListener(resourcePackProgressListener);
                     IOUtils.copy(httpResponse2.getEntity().getContent(), downloadCountingOutputStream);
                  } catch (Exception var97) {
                     LOGGER.error("Caught exception while downloading: " + var97.getMessage());
                     this.error = true;
                  } finally {
                     this.field_20491.releaseConnection();
                     if (this.field_20490 != null) {
                        this.field_20490.delete();
                     }

                  }
               } else {
                  this.finished = true;
               }
            }

            if (closeableHttpClient != null) {
               try {
                  closeableHttpClient.close();
               } catch (IOException var92) {
                  LOGGER.error("Failed to close Realms download client");
               }
            }

         });
         this.currentThread.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
         this.currentThread.start();
      }
   }

   public void cancel() {
      if (this.field_20491 != null) {
         this.field_20491.abort();
      }

      if (this.field_20490 != null) {
         this.field_20490.delete();
      }

      this.cancelled = true;
   }

   public boolean isFinished() {
      return this.finished;
   }

   public boolean isError() {
      return this.error;
   }

   public boolean isExtracting() {
      return this.extracting;
   }

   public static String findAvailableFolderName(String folder) {
      folder = folder.replaceAll("[\\./\"]", "_");
      String[] var1 = INVALID_FILE_NAMES;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         String string = var1[var3];
         if (folder.equalsIgnoreCase(string)) {
            folder = "_" + folder + "_";
         }
      }

      return folder;
   }

   private void untarGzipArchive(String name, File file, RealmsAnvilLevelStorageSource levelStorageSource) throws IOException {
      Pattern pattern = Pattern.compile(".*-([0-9]+)$");
      int i = 1;
      char[] var7 = RealmsSharedConstants.ILLEGAL_FILE_CHARACTERS;
      int var8 = var7.length;

      for(int var9 = 0; var9 < var8; ++var9) {
         char c = var7[var9];
         name = name.replace(c, '_');
      }

      if (StringUtils.isEmpty(name)) {
         name = "Realm";
      }

      name = findAvailableFolderName(name);

      try {
         Iterator var24 = levelStorageSource.getLevelList().iterator();

         while(var24.hasNext()) {
            RealmsLevelSummary realmsLevelSummary = (RealmsLevelSummary)var24.next();
            if (realmsLevelSummary.getLevelId().toLowerCase(Locale.ROOT).startsWith(name.toLowerCase(Locale.ROOT))) {
               Matcher matcher = pattern.matcher(realmsLevelSummary.getLevelId());
               if (matcher.matches()) {
                  if (Integer.valueOf(matcher.group(1)) > i) {
                     i = Integer.valueOf(matcher.group(1));
                  }
               } else {
                  ++i;
               }
            }
         }
      } catch (Exception var23) {
         LOGGER.error("Error getting level list", var23);
         this.error = true;
         return;
      }

      String string2;
      if (levelStorageSource.isNewLevelIdAcceptable(name) && i <= 1) {
         string2 = name;
      } else {
         string2 = name + (i == 1 ? "" : "-" + i);
         if (!levelStorageSource.isNewLevelIdAcceptable(string2)) {
            boolean bl = false;

            while(!bl) {
               ++i;
               string2 = name + (i == 1 ? "" : "-" + i);
               if (levelStorageSource.isNewLevelIdAcceptable(string2)) {
                  bl = true;
               }
            }
         }
      }

      TarArchiveInputStream tarArchiveInputStream = null;
      File file2 = new File(Realms.getGameDirectoryPath(), "saves");
      boolean var20 = false;

      File file5;
      label301: {
         try {
            var20 = true;
            file2.mkdir();
            tarArchiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(file))));

            for(TarArchiveEntry tarArchiveEntry = tarArchiveInputStream.getNextTarEntry(); tarArchiveEntry != null; tarArchiveEntry = tarArchiveInputStream.getNextTarEntry()) {
               file5 = new File(file2, tarArchiveEntry.getName().replace("world", string2));
               if (tarArchiveEntry.isDirectory()) {
                  file5.mkdirs();
               } else {
                  file5.createNewFile();
                  byte[] bs = new byte[1024];
                  BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file5));
                  boolean var13 = false;

                  int j;
                  while((j = tarArchiveInputStream.read(bs)) != -1) {
                     bufferedOutputStream.write(bs, 0, j);
                  }

                  bufferedOutputStream.close();
                  Object var32 = null;
               }
            }

            var20 = false;
            break label301;
         } catch (Exception var21) {
            LOGGER.error("Error extracting world", var21);
            this.error = true;
            var20 = false;
         } finally {
            if (var20) {
               if (tarArchiveInputStream != null) {
                  tarArchiveInputStream.close();
               }

               if (file != null) {
                  file.delete();
               }

               levelStorageSource.renameLevel(string2, string2.trim());
               File file6 = new File(file2, string2 + File.separator + "level.dat");
               Realms.deletePlayerTag(file6);
               this.resourcePackPath = new File(file2, string2 + File.separator + "resources.zip");
            }
         }

         if (tarArchiveInputStream != null) {
            tarArchiveInputStream.close();
         }

         if (file != null) {
            file.delete();
         }

         levelStorageSource.renameLevel(string2, string2.trim());
         file5 = new File(file2, string2 + File.separator + "level.dat");
         Realms.deletePlayerTag(file5);
         this.resourcePackPath = new File(file2, string2 + File.separator + "resources.zip");
         return;
      }

      if (tarArchiveInputStream != null) {
         tarArchiveInputStream.close();
      }

      if (file != null) {
         file.delete();
      }

      levelStorageSource.renameLevel(string2, string2.trim());
      file5 = new File(file2, string2 + File.separator + "level.dat");
      Realms.deletePlayerTag(file5);
      this.resourcePackPath = new File(file2, string2 + File.separator + "resources.zip");
   }

   @Environment(EnvType.CLIENT)
   class DownloadCountingOutputStream extends CountingOutputStream {
      private ActionListener listener;

      public DownloadCountingOutputStream(OutputStream out) {
         super(out);
      }

      public void setListener(ActionListener listener) {
         this.listener = listener;
      }

      protected void afterWrite(int n) throws IOException {
         super.afterWrite(n);
         if (this.listener != null) {
            this.listener.actionPerformed(new ActionEvent(this, 0, (String)null));
         }

      }
   }

   @Environment(EnvType.CLIENT)
   class ResourcePackProgressListener implements ActionListener {
      private final File tempFile;
      private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;
      private final WorldDownload worldDownload;

      private ResourcePackProgressListener(File tempFile, RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus, WorldDownload worldDownload) {
         this.tempFile = tempFile;
         this.downloadStatus = downloadStatus;
         this.worldDownload = worldDownload;
      }

      public void actionPerformed(ActionEvent e) {
         this.downloadStatus.bytesWritten = ((FileDownload.DownloadCountingOutputStream)e.getSource()).getByteCount();
         if (this.downloadStatus.bytesWritten >= this.downloadStatus.totalBytes && !FileDownload.this.cancelled) {
            try {
               String string = Hashing.sha1().hashBytes(Files.toByteArray(this.tempFile)).toString();
               if (string.equals(this.worldDownload.resourcePackHash)) {
                  FileUtils.copyFile(this.tempFile, FileDownload.this.resourcePackPath);
                  FileDownload.this.finished = true;
               } else {
                  FileDownload.LOGGER.error("Resourcepack had wrong hash (expected " + this.worldDownload.resourcePackHash + ", found " + string + "). Deleting it.");
                  FileUtils.deleteQuietly(this.tempFile);
                  FileDownload.this.error = true;
               }
            } catch (IOException var3) {
               FileDownload.LOGGER.error("Error copying resourcepack file", var3.getMessage());
               FileDownload.this.error = true;
            }
         }

      }
   }

   @Environment(EnvType.CLIENT)
   class ProgressListener implements ActionListener {
      private final String worldName;
      private final File tempFile;
      private final RealmsAnvilLevelStorageSource levelStorageSource;
      private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;
      private final WorldDownload worldDownload;

      private ProgressListener(String worldName, File tempFile, RealmsAnvilLevelStorageSource levelStorageSource, RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus, WorldDownload worldDownload) {
         this.worldName = worldName;
         this.tempFile = tempFile;
         this.levelStorageSource = levelStorageSource;
         this.downloadStatus = downloadStatus;
         this.worldDownload = worldDownload;
      }

      public void actionPerformed(ActionEvent e) {
         this.downloadStatus.bytesWritten = ((FileDownload.DownloadCountingOutputStream)e.getSource()).getByteCount();
         if (this.downloadStatus.bytesWritten >= this.downloadStatus.totalBytes && !FileDownload.this.cancelled && !FileDownload.this.error) {
            try {
               FileDownload.this.extracting = true;
               FileDownload.this.untarGzipArchive(this.worldName, this.tempFile, this.levelStorageSource);
            } catch (IOException var3) {
               FileDownload.LOGGER.error("Error extracting archive", var3);
               FileDownload.this.error = true;
            }
         }

      }
   }
}
