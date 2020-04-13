package net.minecraft.resource;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReloadableResourceManagerImpl implements ReloadableResourceManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Map<String, NamespaceResourceManager> namespaceManagers = Maps.newHashMap();
   private final List<ResourceReloadListener> listeners = Lists.newArrayList();
   private final List<ResourceReloadListener> initialListeners = Lists.newArrayList();
   private final Set<String> namespaces = Sets.newLinkedHashSet();
   private final ResourceType type;
   private final Thread mainThread;

   public ReloadableResourceManagerImpl(ResourceType type, Thread mainThread) {
      this.type = type;
      this.mainThread = mainThread;
   }

   public void addPack(ResourcePack pack) {
      NamespaceResourceManager namespaceResourceManager;
      for(Iterator var2 = pack.getNamespaces(this.type).iterator(); var2.hasNext(); namespaceResourceManager.addPack(pack)) {
         String string = (String)var2.next();
         this.namespaces.add(string);
         namespaceResourceManager = (NamespaceResourceManager)this.namespaceManagers.get(string);
         if (namespaceResourceManager == null) {
            namespaceResourceManager = new NamespaceResourceManager(this.type, string);
            this.namespaceManagers.put(string, namespaceResourceManager);
         }
      }

   }

   @Environment(EnvType.CLIENT)
   public Set<String> getAllNamespaces() {
      return this.namespaces;
   }

   public Resource getResource(Identifier id) throws IOException {
      ResourceManager resourceManager = (ResourceManager)this.namespaceManagers.get(id.getNamespace());
      if (resourceManager != null) {
         return resourceManager.getResource(id);
      } else {
         throw new FileNotFoundException(id.toString());
      }
   }

   @Environment(EnvType.CLIENT)
   public boolean containsResource(Identifier id) {
      ResourceManager resourceManager = (ResourceManager)this.namespaceManagers.get(id.getNamespace());
      return resourceManager != null ? resourceManager.containsResource(id) : false;
   }

   public List<Resource> getAllResources(Identifier id) throws IOException {
      ResourceManager resourceManager = (ResourceManager)this.namespaceManagers.get(id.getNamespace());
      if (resourceManager != null) {
         return resourceManager.getAllResources(id);
      } else {
         throw new FileNotFoundException(id.toString());
      }
   }

   public Collection<Identifier> findResources(String resourceType, Predicate<String> pathPredicate) {
      Set<Identifier> set = Sets.newHashSet();
      Iterator var4 = this.namespaceManagers.values().iterator();

      while(var4.hasNext()) {
         NamespaceResourceManager namespaceResourceManager = (NamespaceResourceManager)var4.next();
         set.addAll(namespaceResourceManager.findResources(resourceType, pathPredicate));
      }

      List<Identifier> list = Lists.newArrayList(set);
      Collections.sort(list);
      return list;
   }

   private void clear() {
      this.namespaceManagers.clear();
      this.namespaces.clear();
   }

   public CompletableFuture<Unit> beginReload(Executor prepareExecutor, Executor applyExecutor, List<ResourcePack> packs, CompletableFuture<Unit> initialStage) {
      ResourceReloadMonitor resourceReloadMonitor = this.beginMonitoredReload(prepareExecutor, applyExecutor, initialStage, packs);
      return resourceReloadMonitor.whenComplete();
   }

   public void registerListener(ResourceReloadListener listener) {
      this.listeners.add(listener);
      this.initialListeners.add(listener);
   }

   protected ResourceReloadMonitor beginReloadInner(Executor prepareExecutor, Executor applyExecutor, List<ResourceReloadListener> listeners, CompletableFuture<Unit> initialStage) {
      Object resourceReloadMonitor2;
      if (LOGGER.isDebugEnabled()) {
         resourceReloadMonitor2 = new ProfilingResourceReloader(this, Lists.newArrayList(listeners), prepareExecutor, applyExecutor, initialStage);
      } else {
         resourceReloadMonitor2 = ResourceReloader.create(this, Lists.newArrayList(listeners), prepareExecutor, applyExecutor, initialStage);
      }

      this.initialListeners.clear();
      return (ResourceReloadMonitor)resourceReloadMonitor2;
   }

   public ResourceReloadMonitor beginMonitoredReload(Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage, List<ResourcePack> packs) {
      this.clear();
      LOGGER.info("Reloading ResourceManager: {}", packs.stream().map(ResourcePack::getName).collect(Collectors.joining(", ")));
      Iterator var5 = packs.iterator();

      while(var5.hasNext()) {
         ResourcePack resourcePack = (ResourcePack)var5.next();

         try {
            this.addPack(resourcePack);
         } catch (Exception var8) {
            LOGGER.error("Failed to add resource pack {}", resourcePack.getName(), var8);
            return new ReloadableResourceManagerImpl.FailedResourceReloadMonitor(new ReloadableResourceManagerImpl.PackAdditionFailedException(resourcePack, var8));
         }
      }

      return this.beginReloadInner(prepareExecutor, applyExecutor, this.listeners, initialStage);
   }

   static class FailedResourceReloadMonitor implements ResourceReloadMonitor {
      private final ReloadableResourceManagerImpl.PackAdditionFailedException exception;
      private final CompletableFuture<Unit> future;

      public FailedResourceReloadMonitor(ReloadableResourceManagerImpl.PackAdditionFailedException exception) {
         this.exception = exception;
         this.future = new CompletableFuture();
         this.future.completeExceptionally(exception);
      }

      public CompletableFuture<Unit> whenComplete() {
         return this.future;
      }

      @Environment(EnvType.CLIENT)
      public float getProgress() {
         return 0.0F;
      }

      @Environment(EnvType.CLIENT)
      public boolean isPrepareStageComplete() {
         return false;
      }

      @Environment(EnvType.CLIENT)
      public boolean isApplyStageComplete() {
         return true;
      }

      @Environment(EnvType.CLIENT)
      public void throwExceptions() {
         throw this.exception;
      }
   }

   public static class PackAdditionFailedException extends RuntimeException {
      private final ResourcePack pack;

      public PackAdditionFailedException(ResourcePack pack, Throwable cause) {
         super(pack.getName(), cause);
         this.pack = pack;
      }

      @Environment(EnvType.CLIENT)
      public ResourcePack getPack() {
         return this.pack;
      }
   }
}
