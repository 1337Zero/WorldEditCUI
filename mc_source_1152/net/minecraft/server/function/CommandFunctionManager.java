package net.minecraft.server.function;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloadListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.TagContainer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.DisableableProfiler;
import net.minecraft.world.GameRules;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandFunctionManager implements SynchronousResourceReloadListener {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Identifier TICK_FUNCTION = new Identifier("tick");
   private static final Identifier LOAD_FUNCTION = new Identifier("load");
   public static final int PATH_PREFIX_LENGTH = "functions/".length();
   public static final int EXTENSION_LENGTH = ".mcfunction".length();
   private final MinecraftServer server;
   private final Map<Identifier, CommandFunction> idMap = Maps.newHashMap();
   private boolean executing;
   private final ArrayDeque<CommandFunctionManager.Entry> chain = new ArrayDeque();
   private final List<CommandFunctionManager.Entry> pending = Lists.newArrayList();
   private final TagContainer<CommandFunction> tags = new TagContainer(this::getFunction, "tags/functions", true, "function");
   private final List<CommandFunction> tickFunctions = Lists.newArrayList();
   private boolean needToRunLoadFunctions;

   public CommandFunctionManager(MinecraftServer server) {
      this.server = server;
   }

   public Optional<CommandFunction> getFunction(Identifier identifier) {
      return Optional.ofNullable(this.idMap.get(identifier));
   }

   public MinecraftServer getServer() {
      return this.server;
   }

   public int getMaxCommandChainLength() {
      return this.server.getGameRules().getInt(GameRules.MAX_COMMAND_CHAIN_LENGTH);
   }

   public Map<Identifier, CommandFunction> getFunctions() {
      return this.idMap;
   }

   public CommandDispatcher<ServerCommandSource> getDispatcher() {
      return this.server.getCommandManager().getDispatcher();
   }

   public void tick() {
      DisableableProfiler var10000 = this.server.getProfiler();
      Identifier var10001 = TICK_FUNCTION;
      var10000.push(var10001::toString);
      Iterator var1 = this.tickFunctions.iterator();

      while(var1.hasNext()) {
         CommandFunction commandFunction = (CommandFunction)var1.next();
         this.execute(commandFunction, this.getTaggedFunctionSource());
      }

      this.server.getProfiler().pop();
      if (this.needToRunLoadFunctions) {
         this.needToRunLoadFunctions = false;
         Collection<CommandFunction> collection = this.getTags().getOrCreate(LOAD_FUNCTION).values();
         var10000 = this.server.getProfiler();
         var10001 = LOAD_FUNCTION;
         var10000.push(var10001::toString);
         Iterator var5 = collection.iterator();

         while(var5.hasNext()) {
            CommandFunction commandFunction2 = (CommandFunction)var5.next();
            this.execute(commandFunction2, this.getTaggedFunctionSource());
         }

         this.server.getProfiler().pop();
      }

   }

   public int execute(CommandFunction function, ServerCommandSource source) {
      int i = this.getMaxCommandChainLength();
      if (this.executing) {
         if (this.chain.size() + this.pending.size() < i) {
            this.pending.add(new CommandFunctionManager.Entry(this, source, new CommandFunction.FunctionElement(function)));
         }

         return 0;
      } else {
         int k;
         try {
            this.executing = true;
            int j = 0;
            CommandFunction.Element[] elements = function.getElements();

            for(k = elements.length - 1; k >= 0; --k) {
               this.chain.push(new CommandFunctionManager.Entry(this, source, elements[k]));
            }

            while(!this.chain.isEmpty()) {
               try {
                  CommandFunctionManager.Entry entry = (CommandFunctionManager.Entry)this.chain.removeFirst();
                  this.server.getProfiler().push(entry::toString);
                  entry.execute(this.chain, i);
                  if (!this.pending.isEmpty()) {
                     List var10000 = Lists.reverse(this.pending);
                     ArrayDeque var10001 = this.chain;
                     var10000.forEach(var10001::addFirst);
                     this.pending.clear();
                  }
               } finally {
                  this.server.getProfiler().pop();
               }

               ++j;
               if (j >= i) {
                  k = j;
                  return k;
               }
            }

            k = j;
         } finally {
            this.chain.clear();
            this.pending.clear();
            this.executing = false;
         }

         return k;
      }
   }

   public void apply(ResourceManager manager) {
      this.idMap.clear();
      this.tickFunctions.clear();
      Collection<Identifier> collection = manager.findResources("functions", (stringx) -> {
         return stringx.endsWith(".mcfunction");
      });
      List<CompletableFuture<CommandFunction>> list = Lists.newArrayList();
      Iterator var4 = collection.iterator();

      while(var4.hasNext()) {
         Identifier identifier = (Identifier)var4.next();
         String string = identifier.getPath();
         Identifier identifier2 = new Identifier(identifier.getNamespace(), string.substring(PATH_PREFIX_LENGTH, string.length() - EXTENSION_LENGTH));
         list.add(CompletableFuture.supplyAsync(() -> {
            return readLines(manager, identifier);
         }, ResourceImpl.RESOURCE_IO_EXECUTOR).thenApplyAsync((listx) -> {
            return CommandFunction.create(identifier2, this, listx);
         }, this.server.getWorkerExecutor()).handle((commandFunction, throwable) -> {
            return this.load(commandFunction, throwable, identifier);
         }));
      }

      CompletableFuture.allOf((CompletableFuture[])list.toArray(new CompletableFuture[0])).join();
      if (!this.idMap.isEmpty()) {
         LOGGER.info("Loaded {} custom command functions", this.idMap.size());
      }

      this.tags.applyReload((Map)this.tags.prepareReload(manager, this.server.getWorkerExecutor()).join());
      this.tickFunctions.addAll(this.tags.getOrCreate(TICK_FUNCTION).values());
      this.needToRunLoadFunctions = true;
   }

   @Nullable
   private CommandFunction load(CommandFunction function, @Nullable Throwable exception, Identifier identifier) {
      if (exception != null) {
         LOGGER.error("Couldn't load function at {}", identifier, exception);
         return null;
      } else {
         synchronized(this.idMap) {
            this.idMap.put(function.getId(), function);
            return function;
         }
      }
   }

   private static List<String> readLines(ResourceManager resourceManager, Identifier identifier) {
      try {
         Resource resource = resourceManager.getResource(identifier);
         Throwable var3 = null;

         List var4;
         try {
            var4 = IOUtils.readLines(resource.getInputStream(), StandardCharsets.UTF_8);
         } catch (Throwable var14) {
            var3 = var14;
            throw var14;
         } finally {
            if (resource != null) {
               if (var3 != null) {
                  try {
                     resource.close();
                  } catch (Throwable var13) {
                     var3.addSuppressed(var13);
                  }
               } else {
                  resource.close();
               }
            }

         }

         return var4;
      } catch (IOException var16) {
         throw new CompletionException(var16);
      }
   }

   public ServerCommandSource getTaggedFunctionSource() {
      return this.server.getCommandSource().withLevel(2).withSilent();
   }

   public ServerCommandSource getCommandFunctionSource() {
      return new ServerCommandSource(CommandOutput.DUMMY, Vec3d.ZERO, Vec2f.ZERO, (ServerWorld)null, this.server.getFunctionPermissionLevel(), "", new LiteralText(""), this.server, (Entity)null);
   }

   public TagContainer<CommandFunction> getTags() {
      return this.tags;
   }

   public static class Entry {
      private final CommandFunctionManager manager;
      private final ServerCommandSource source;
      private final CommandFunction.Element element;

      public Entry(CommandFunctionManager manger, ServerCommandSource source, CommandFunction.Element element) {
         this.manager = manger;
         this.source = source;
         this.element = element;
      }

      public void execute(ArrayDeque<CommandFunctionManager.Entry> arrayDeque, int i) {
         try {
            this.element.execute(this.manager, this.source, arrayDeque, i);
         } catch (Throwable var4) {
         }

      }

      public String toString() {
         return this.element.toString();
      }
   }
}
