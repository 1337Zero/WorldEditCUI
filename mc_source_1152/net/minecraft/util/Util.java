package net.minecraft.util;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import it.unimi.dsi.fastutil.Hash.Strategy;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.state.property.Property;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Util {
   private static final AtomicInteger NEXT_SERVER_WORKER_ID = new AtomicInteger(1);
   private static final ExecutorService SERVER_WORKER_EXECUTOR = createServerWorkerExecutor();
   public static LongSupplier nanoTimeSupplier = System::nanoTime;
   private static final Logger LOGGER = LogManager.getLogger();

   public static <K, V> Collector<Entry<? extends K, ? extends V>, ?, Map<K, V>> toMap() {
      return Collectors.toMap(Entry::getKey, Entry::getValue);
   }

   public static <T extends Comparable<T>> String getValueAsString(Property<T> property, Object object) {
      return property.name((Comparable)object);
   }

   public static String createTranslationKey(String type, @Nullable Identifier id) {
      return id == null ? type + ".unregistered_sadface" : type + '.' + id.getNamespace() + '.' + id.getPath().replace('/', '.');
   }

   public static long getMeasuringTimeMs() {
      return getMeasuringTimeNano() / 1000000L;
   }

   public static long getMeasuringTimeNano() {
      return nanoTimeSupplier.getAsLong();
   }

   public static long getEpochTimeMs() {
      return Instant.now().toEpochMilli();
   }

   private static ExecutorService createServerWorkerExecutor() {
      int i = MathHelper.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, 7);
      Object executorService2;
      if (i <= 0) {
         executorService2 = MoreExecutors.newDirectExecutorService();
      } else {
         executorService2 = new ForkJoinPool(i, (forkJoinPool) -> {
            ForkJoinWorkerThread forkJoinWorkerThread = new ForkJoinWorkerThread(forkJoinPool) {
               protected void onTermination(Throwable throwable) {
                  if (throwable != null) {
                     Util.LOGGER.warn("{} died", this.getName(), throwable);
                  } else {
                     Util.LOGGER.debug("{} shutdown", this.getName());
                  }

                  super.onTermination(throwable);
               }
            };
            forkJoinWorkerThread.setName("Server-Worker-" + NEXT_SERVER_WORKER_ID.getAndIncrement());
            return forkJoinWorkerThread;
         }, (thread, throwable) -> {
            throwOrPause(throwable);
            if (throwable instanceof CompletionException) {
               throwable = throwable.getCause();
            }

            if (throwable instanceof CrashException) {
               Bootstrap.println(((CrashException)throwable).getReport().asString());
               System.exit(-1);
            }

            LOGGER.error(String.format("Caught exception in thread %s", thread), throwable);
         }, true);
      }

      return (ExecutorService)executorService2;
   }

   public static Executor getServerWorkerExecutor() {
      return SERVER_WORKER_EXECUTOR;
   }

   public static void shutdownServerWorkerExecutor() {
      SERVER_WORKER_EXECUTOR.shutdown();

      boolean bl2;
      try {
         bl2 = SERVER_WORKER_EXECUTOR.awaitTermination(3L, TimeUnit.SECONDS);
      } catch (InterruptedException var2) {
         bl2 = false;
      }

      if (!bl2) {
         SERVER_WORKER_EXECUTOR.shutdownNow();
      }

   }

   @Environment(EnvType.CLIENT)
   public static <T> CompletableFuture<T> completeExceptionally(Throwable throwable) {
      CompletableFuture<T> completableFuture = new CompletableFuture();
      completableFuture.completeExceptionally(throwable);
      return completableFuture;
   }

   @Environment(EnvType.CLIENT)
   public static void throwUnchecked(Throwable throwable) {
      throw throwable instanceof RuntimeException ? (RuntimeException)throwable : new RuntimeException(throwable);
   }

   public static Util.OperatingSystem getOperatingSystem() {
      String string = System.getProperty("os.name").toLowerCase(Locale.ROOT);
      if (string.contains("win")) {
         return Util.OperatingSystem.WINDOWS;
      } else if (string.contains("mac")) {
         return Util.OperatingSystem.OSX;
      } else if (string.contains("solaris")) {
         return Util.OperatingSystem.SOLARIS;
      } else if (string.contains("sunos")) {
         return Util.OperatingSystem.SOLARIS;
      } else if (string.contains("linux")) {
         return Util.OperatingSystem.LINUX;
      } else {
         return string.contains("unix") ? Util.OperatingSystem.LINUX : Util.OperatingSystem.UNKNOWN;
      }
   }

   public static Stream<String> getJVMFlags() {
      RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
      return runtimeMXBean.getInputArguments().stream().filter((string) -> {
         return string.startsWith("-X");
      });
   }

   public static <T> T getLast(List<T> list) {
      return list.get(list.size() - 1);
   }

   public static <T> T next(Iterable<T> iterable, @Nullable T object) {
      Iterator<T> iterator = iterable.iterator();
      T object2 = iterator.next();
      if (object != null) {
         Object object3 = object2;

         while(object3 != object) {
            if (iterator.hasNext()) {
               object3 = iterator.next();
            }
         }

         if (iterator.hasNext()) {
            return iterator.next();
         }
      }

      return object2;
   }

   public static <T> T previous(Iterable<T> iterable, @Nullable T object) {
      Iterator<T> iterator = iterable.iterator();

      Object object2;
      Object object3;
      for(object2 = null; iterator.hasNext(); object2 = object3) {
         object3 = iterator.next();
         if (object3 == object) {
            if (object2 == null) {
               object2 = iterator.hasNext() ? Iterators.getLast(iterator) : object;
            }
            break;
         }
      }

      return object2;
   }

   public static <T> T make(Supplier<T> factory) {
      return factory.get();
   }

   public static <T> T make(T object, Consumer<T> initializer) {
      initializer.accept(object);
      return object;
   }

   public static <K> Strategy<K> identityHashStrategy() {
      return Util.IdentityHashStrategy.INSTANCE;
   }

   public static <V> CompletableFuture<List<V>> combine(List<? extends CompletableFuture<? extends V>> futures) {
      List<V> list = Lists.newArrayListWithCapacity(futures.size());
      CompletableFuture<?>[] completableFutures = new CompletableFuture[futures.size()];
      CompletableFuture<Void> completableFuture = new CompletableFuture();
      futures.forEach((completableFuture2) -> {
         int i = list.size();
         list.add((Object)null);
         completableFutures[i] = completableFuture2.whenComplete((object, throwable) -> {
            if (throwable != null) {
               completableFuture.completeExceptionally(throwable);
            } else {
               list.set(i, object);
            }

         });
      });
      return CompletableFuture.allOf(completableFutures).applyToEither(completableFuture, (var1) -> {
         return list;
      });
   }

   public static <T> Stream<T> stream(Optional<? extends T> optional) {
      return (Stream)DataFixUtils.orElseGet(optional.map(Stream::of), Stream::empty);
   }

   public static <T> Optional<T> ifPresentOrElse(Optional<T> optional, Consumer<T> consumer, Runnable runnable) {
      if (optional.isPresent()) {
         consumer.accept(optional.get());
      } else {
         runnable.run();
      }

      return optional;
   }

   public static Runnable debugRunnable(Runnable runnable, Supplier<String> messageSupplier) {
      return runnable;
   }

   public static Optional<UUID> readUuid(String name, Dynamic<?> dynamic) {
      return dynamic.get(name + "Most").asNumber().flatMap((number) -> {
         return dynamic.get(name + "Least").asNumber().map((number2) -> {
            return new UUID(number.longValue(), number2.longValue());
         });
      });
   }

   public static <T> Dynamic<T> writeUuid(String name, UUID uuid, Dynamic<T> dynamic) {
      return dynamic.set(name + "Most", dynamic.createLong(uuid.getMostSignificantBits())).set(name + "Least", dynamic.createLong(uuid.getLeastSignificantBits()));
   }

   public static <T extends Throwable> T throwOrPause(T t) {
      if (SharedConstants.isDevelopment) {
         LOGGER.error("Trying to throw a fatal exception, pausing in IDE", t);

         while(true) {
            try {
               Thread.sleep(1000L);
               LOGGER.error("paused");
            } catch (InterruptedException var2) {
               return t;
            }
         }
      } else {
         return t;
      }
   }

   public static String getInnermostMessage(Throwable t) {
      if (t.getCause() != null) {
         return getInnermostMessage(t.getCause());
      } else {
         return t.getMessage() != null ? t.getMessage() : t.toString();
      }
   }

   static enum IdentityHashStrategy implements Strategy<Object> {
      INSTANCE;

      public int hashCode(Object object) {
         return System.identityHashCode(object);
      }

      public boolean equals(Object object, Object object2) {
         return object == object2;
      }
   }

   public static enum OperatingSystem {
      LINUX,
      SOLARIS,
      WINDOWS {
         @Environment(EnvType.CLIENT)
         protected String[] getURLOpenCommand(URL url) {
            return new String[]{"rundll32", "url.dll,FileProtocolHandler", url.toString()};
         }
      },
      OSX {
         @Environment(EnvType.CLIENT)
         protected String[] getURLOpenCommand(URL url) {
            return new String[]{"open", url.toString()};
         }
      },
      UNKNOWN;

      private OperatingSystem() {
      }

      @Environment(EnvType.CLIENT)
      public void open(URL url) {
         try {
            Process process = (Process)AccessController.doPrivileged(() -> {
               return Runtime.getRuntime().exec(this.getURLOpenCommand(url));
            });
            Iterator var3 = IOUtils.readLines(process.getErrorStream()).iterator();

            while(var3.hasNext()) {
               String string = (String)var3.next();
               Util.LOGGER.error(string);
            }

            process.getInputStream().close();
            process.getErrorStream().close();
            process.getOutputStream().close();
         } catch (IOException | PrivilegedActionException var5) {
            Util.LOGGER.error("Couldn't open url '{}'", url, var5);
         }

      }

      @Environment(EnvType.CLIENT)
      public void open(URI uRI) {
         try {
            this.open(uRI.toURL());
         } catch (MalformedURLException var3) {
            Util.LOGGER.error("Couldn't open uri '{}'", uRI, var3);
         }

      }

      @Environment(EnvType.CLIENT)
      public void open(File file) {
         try {
            this.open(file.toURI().toURL());
         } catch (MalformedURLException var3) {
            Util.LOGGER.error("Couldn't open file '{}'", file, var3);
         }

      }

      @Environment(EnvType.CLIENT)
      protected String[] getURLOpenCommand(URL url) {
         String string = url.toString();
         if ("file".equals(url.getProtocol())) {
            string = string.replace("file:", "file://");
         }

         return new String[]{"xdg-open", string};
      }

      @Environment(EnvType.CLIENT)
      public void open(String string) {
         try {
            this.open((new URI(string)).toURL());
         } catch (MalformedURLException | IllegalArgumentException | URISyntaxException var3) {
            Util.LOGGER.error("Couldn't open uri '{}'", string, var3);
         }

      }
   }
}
