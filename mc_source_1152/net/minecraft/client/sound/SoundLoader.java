package net.minecraft.client.sound;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class SoundLoader {
   private final ResourceManager resourceManager;
   private final Map<Identifier, CompletableFuture<StaticSound>> loadedSounds = Maps.newHashMap();

   public SoundLoader(ResourceManager resourceManager) {
      this.resourceManager = resourceManager;
   }

   public CompletableFuture<StaticSound> loadStatic(Identifier id) {
      return (CompletableFuture)this.loadedSounds.computeIfAbsent(id, (identifier) -> {
         return CompletableFuture.supplyAsync(() -> {
            try {
               Resource resource = this.resourceManager.getResource(identifier);
               Throwable var3 = null;

               StaticSound var9;
               try {
                  InputStream inputStream = resource.getInputStream();
                  Throwable var5 = null;

                  try {
                     AudioStream audioStream = new OggAudioStream(inputStream);
                     Throwable var7 = null;

                     try {
                        ByteBuffer byteBuffer = audioStream.getBuffer();
                        var9 = new StaticSound(byteBuffer, audioStream.getFormat());
                     } catch (Throwable var56) {
                        var7 = var56;
                        throw var56;
                     } finally {
                        if (audioStream != null) {
                           if (var7 != null) {
                              try {
                                 audioStream.close();
                              } catch (Throwable var55) {
                                 var7.addSuppressed(var55);
                              }
                           } else {
                              audioStream.close();
                           }
                        }

                     }
                  } catch (Throwable var58) {
                     var5 = var58;
                     throw var58;
                  } finally {
                     if (inputStream != null) {
                        if (var5 != null) {
                           try {
                              inputStream.close();
                           } catch (Throwable var54) {
                              var5.addSuppressed(var54);
                           }
                        } else {
                           inputStream.close();
                        }
                     }

                  }
               } catch (Throwable var60) {
                  var3 = var60;
                  throw var60;
               } finally {
                  if (resource != null) {
                     if (var3 != null) {
                        try {
                           resource.close();
                        } catch (Throwable var53) {
                           var3.addSuppressed(var53);
                        }
                     } else {
                        resource.close();
                     }
                  }

               }

               return var9;
            } catch (IOException var62) {
               throw new CompletionException(var62);
            }
         }, Util.getServerWorkerExecutor());
      });
   }

   public CompletableFuture<AudioStream> loadStreamed(Identifier id) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            Resource resource = this.resourceManager.getResource(id);
            InputStream inputStream = resource.getInputStream();
            return new OggAudioStream(inputStream);
         } catch (IOException var4) {
            throw new CompletionException(var4);
         }
      }, Util.getServerWorkerExecutor());
   }

   public void close() {
      this.loadedSounds.values().forEach((completableFuture) -> {
         completableFuture.thenAccept(StaticSound::close);
      });
      this.loadedSounds.clear();
   }

   public CompletableFuture<?> loadStatic(Collection<Sound> sounds) {
      return CompletableFuture.allOf((CompletableFuture[])sounds.stream().map((sound) -> {
         return this.loadStatic(sound.getLocation());
      }).toArray((i) -> {
         return new CompletableFuture[i];
      }));
   }
}
