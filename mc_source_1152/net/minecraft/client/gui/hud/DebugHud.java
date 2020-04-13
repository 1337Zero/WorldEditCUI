package net.minecraft.client.gui.hud;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.blaze3d.platform.GlDebugInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.DataFixUtils;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.Rotation3;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.MetricsData;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;

@Environment(EnvType.CLIENT)
public class DebugHud extends DrawableHelper {
   private static final Map<Heightmap.Type, String> HEIGHT_MAP_TYPES = (Map)Util.make(new EnumMap(Heightmap.Type.class), (enumMap) -> {
      enumMap.put(Heightmap.Type.WORLD_SURFACE_WG, "SW");
      enumMap.put(Heightmap.Type.WORLD_SURFACE, "S");
      enumMap.put(Heightmap.Type.OCEAN_FLOOR_WG, "OW");
      enumMap.put(Heightmap.Type.OCEAN_FLOOR, "O");
      enumMap.put(Heightmap.Type.MOTION_BLOCKING, "M");
      enumMap.put(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, "ML");
   });
   private final MinecraftClient client;
   private final TextRenderer fontRenderer;
   private HitResult blockHit;
   private HitResult fluidHit;
   @Nullable
   private ChunkPos pos;
   @Nullable
   private WorldChunk chunk;
   @Nullable
   private CompletableFuture<WorldChunk> chunkFuture;

   public DebugHud(MinecraftClient client) {
      this.client = client;
      this.fontRenderer = client.textRenderer;
   }

   public void resetChunk() {
      this.chunkFuture = null;
      this.chunk = null;
   }

   public void render() {
      this.client.getProfiler().push("debug");
      RenderSystem.pushMatrix();
      Entity entity = this.client.getCameraEntity();
      this.blockHit = entity.rayTrace(20.0D, 0.0F, false);
      this.fluidHit = entity.rayTrace(20.0D, 0.0F, true);
      this.renderLeftText();
      this.renderRightText();
      RenderSystem.popMatrix();
      if (this.client.options.debugTpsEnabled) {
         int i = this.client.getWindow().getScaledWidth();
         this.drawMetricsData(this.client.getMetricsData(), 0, i / 2, true);
         IntegratedServer integratedServer = this.client.getServer();
         if (integratedServer != null) {
            this.drawMetricsData(integratedServer.getMetricsData(), i - Math.min(i / 2, 240), i / 2, false);
         }
      }

      this.client.getProfiler().pop();
   }

   protected void renderLeftText() {
      List<String> list = this.getLeftText();
      list.add("");
      boolean bl = this.client.getServer() != null;
      list.add("Debug: Pie [shift]: " + (this.client.options.debugProfilerEnabled ? "visible" : "hidden") + (bl ? " FPS + TPS" : " FPS") + " [alt]: " + (this.client.options.debugTpsEnabled ? "visible" : "hidden"));
      list.add("For help: press F3 + Q");

      for(int i = 0; i < list.size(); ++i) {
         String string = (String)list.get(i);
         if (!Strings.isNullOrEmpty(string)) {
            this.fontRenderer.getClass();
            int j = 9;
            int k = this.fontRenderer.getStringWidth(string);
            int l = true;
            int m = 2 + j * i;
            fill(1, m - 1, 2 + k + 1, m + j - 1, -1873784752);
            this.fontRenderer.draw(string, 2.0F, (float)m, 14737632);
         }
      }

   }

   protected void renderRightText() {
      List<String> list = this.getRightText();

      for(int i = 0; i < list.size(); ++i) {
         String string = (String)list.get(i);
         if (!Strings.isNullOrEmpty(string)) {
            this.fontRenderer.getClass();
            int j = 9;
            int k = this.fontRenderer.getStringWidth(string);
            int l = this.client.getWindow().getScaledWidth() - 2 - k;
            int m = 2 + j * i;
            fill(l - 1, m - 1, l + k + 1, m + j - 1, -1873784752);
            this.fontRenderer.draw(string, (float)l, (float)m, 14737632);
         }
      }

   }

   protected List<String> getLeftText() {
      IntegratedServer integratedServer = this.client.getServer();
      ClientConnection clientConnection = this.client.getNetworkHandler().getConnection();
      float f = clientConnection.getAveragePacketsSent();
      float g = clientConnection.getAveragePacketsReceived();
      String string2;
      if (integratedServer != null) {
         string2 = String.format("Integrated server @ %.0f ms ticks, %.0f tx, %.0f rx", integratedServer.getTickTime(), f, g);
      } else {
         string2 = String.format("\"%s\" server, %.0f tx, %.0f rx", this.client.player.getServerBrand(), f, g);
      }

      BlockPos blockPos = new BlockPos(this.client.getCameraEntity());
      if (this.client.hasReducedDebugInfo()) {
         return Lists.newArrayList(new String[]{"Minecraft " + SharedConstants.getGameVersion().getName() + " (" + this.client.getGameVersion() + "/" + ClientBrandRetriever.getClientModName() + ")", this.client.fpsDebugString, string2, this.client.worldRenderer.getChunksDebugString(), this.client.worldRenderer.getEntitiesDebugString(), "P: " + this.client.particleManager.getDebugString() + ". T: " + this.client.world.getRegularEntityCount(), this.client.world.getDebugString(), "", String.format("Chunk-relative: %d %d %d", blockPos.getX() & 15, blockPos.getY() & 15, blockPos.getZ() & 15)});
      } else {
         Entity entity = this.client.getCameraEntity();
         Direction direction = entity.getHorizontalFacing();
         String string7;
         switch(direction) {
         case NORTH:
            string7 = "Towards negative Z";
            break;
         case SOUTH:
            string7 = "Towards positive Z";
            break;
         case WEST:
            string7 = "Towards negative X";
            break;
         case EAST:
            string7 = "Towards positive X";
            break;
         default:
            string7 = "Invalid";
         }

         ChunkPos chunkPos = new ChunkPos(blockPos);
         if (!Objects.equals(this.pos, chunkPos)) {
            this.pos = chunkPos;
            this.resetChunk();
         }

         World world = this.getWorld();
         LongSet longSet = world instanceof ServerWorld ? ((ServerWorld)world).getForcedChunks() : LongSets.EMPTY_SET;
         List<String> list = Lists.newArrayList(new String[]{"Minecraft " + SharedConstants.getGameVersion().getName() + " (" + this.client.getGameVersion() + "/" + ClientBrandRetriever.getClientModName() + ("release".equalsIgnoreCase(this.client.getVersionType()) ? "" : "/" + this.client.getVersionType()) + ")", this.client.fpsDebugString, string2, this.client.worldRenderer.getChunksDebugString(), this.client.worldRenderer.getEntitiesDebugString(), "P: " + this.client.particleManager.getDebugString() + ". T: " + this.client.world.getRegularEntityCount(), this.client.world.getDebugString()});
         String string8 = this.getServerWorldDebugString();
         if (string8 != null) {
            list.add(string8);
         }

         list.add(DimensionType.getId(this.client.world.dimension.getType()).toString() + " FC: " + Integer.toString(((LongSet)longSet).size()));
         list.add("");
         list.add(String.format(Locale.ROOT, "XYZ: %.3f / %.5f / %.3f", this.client.getCameraEntity().getX(), this.client.getCameraEntity().getY(), this.client.getCameraEntity().getZ()));
         list.add(String.format("Block: %d %d %d", blockPos.getX(), blockPos.getY(), blockPos.getZ()));
         list.add(String.format("Chunk: %d %d %d in %d %d %d", blockPos.getX() & 15, blockPos.getY() & 15, blockPos.getZ() & 15, blockPos.getX() >> 4, blockPos.getY() >> 4, blockPos.getZ() >> 4));
         list.add(String.format(Locale.ROOT, "Facing: %s (%s) (%.1f / %.1f)", direction, string7, MathHelper.wrapDegrees(entity.yaw), MathHelper.wrapDegrees(entity.pitch)));
         if (this.client.world != null) {
            if (this.client.world.isChunkLoaded(blockPos)) {
               WorldChunk worldChunk = this.getClientChunk();
               if (worldChunk.isEmpty()) {
                  list.add("Waiting for chunk...");
               } else {
                  int i = this.client.world.getChunkManager().getLightingProvider().getLight(blockPos, 0);
                  int j = this.client.world.getLightLevel(LightType.SKY, blockPos);
                  int k = this.client.world.getLightLevel(LightType.BLOCK, blockPos);
                  list.add("Client Light: " + i + " (" + j + " sky, " + k + " block)");
                  WorldChunk worldChunk2 = this.getChunk();
                  if (worldChunk2 != null) {
                     LightingProvider lightingProvider = world.getChunkManager().getLightingProvider();
                     list.add("Server Light: (" + lightingProvider.get(LightType.SKY).getLightLevel(blockPos) + " sky, " + lightingProvider.get(LightType.BLOCK).getLightLevel(blockPos) + " block)");
                  } else {
                     list.add("Server Light: (?? sky, ?? block)");
                  }

                  StringBuilder stringBuilder = new StringBuilder("CH");
                  Heightmap.Type[] var21 = Heightmap.Type.values();
                  int var22 = var21.length;

                  int var23;
                  Heightmap.Type type2;
                  for(var23 = 0; var23 < var22; ++var23) {
                     type2 = var21[var23];
                     if (type2.shouldSendToClient()) {
                        stringBuilder.append(" ").append((String)HEIGHT_MAP_TYPES.get(type2)).append(": ").append(worldChunk.sampleHeightmap(type2, blockPos.getX(), blockPos.getZ()));
                     }
                  }

                  list.add(stringBuilder.toString());
                  stringBuilder.setLength(0);
                  stringBuilder.append("SH");
                  var21 = Heightmap.Type.values();
                  var22 = var21.length;

                  for(var23 = 0; var23 < var22; ++var23) {
                     type2 = var21[var23];
                     if (type2.isStoredServerSide()) {
                        stringBuilder.append(" ").append((String)HEIGHT_MAP_TYPES.get(type2)).append(": ");
                        if (worldChunk2 != null) {
                           stringBuilder.append(worldChunk2.sampleHeightmap(type2, blockPos.getX(), blockPos.getZ()));
                        } else {
                           stringBuilder.append("??");
                        }
                     }
                  }

                  list.add(stringBuilder.toString());
                  if (blockPos.getY() >= 0 && blockPos.getY() < 256) {
                     list.add("Biome: " + Registry.BIOME.getId(this.client.world.getBiome(blockPos)));
                     long l = 0L;
                     float h = 0.0F;
                     if (worldChunk2 != null) {
                        h = world.getMoonSize();
                        l = worldChunk2.getInhabitedTime();
                     }

                     LocalDifficulty localDifficulty = new LocalDifficulty(world.getDifficulty(), world.getTimeOfDay(), l, h);
                     list.add(String.format(Locale.ROOT, "Local Difficulty: %.2f // %.2f (Day %d)", localDifficulty.getLocalDifficulty(), localDifficulty.getClampedLocalDifficulty(), this.client.world.getTimeOfDay() / 24000L));
                  }
               }
            } else {
               list.add("Outside of world...");
            }
         } else {
            list.add("Outside of world...");
         }

         ShaderEffect shaderEffect = this.client.gameRenderer.getShader();
         if (shaderEffect != null) {
            list.add("Shader: " + shaderEffect.getName());
         }

         BlockPos blockPos3;
         if (this.blockHit.getType() == HitResult.Type.BLOCK) {
            blockPos3 = ((BlockHitResult)this.blockHit).getBlockPos();
            list.add(String.format("Looking at block: %d %d %d", blockPos3.getX(), blockPos3.getY(), blockPos3.getZ()));
         }

         if (this.fluidHit.getType() == HitResult.Type.BLOCK) {
            blockPos3 = ((BlockHitResult)this.fluidHit).getBlockPos();
            list.add(String.format("Looking at liquid: %d %d %d", blockPos3.getX(), blockPos3.getY(), blockPos3.getZ()));
         }

         list.add(this.client.getSoundManager().getDebugString());
         return list;
      }
   }

   @Nullable
   private String getServerWorldDebugString() {
      IntegratedServer integratedServer = this.client.getServer();
      if (integratedServer != null) {
         ServerWorld serverWorld = integratedServer.getWorld(this.client.world.getDimension().getType());
         if (serverWorld != null) {
            return serverWorld.getDebugString();
         }
      }

      return null;
   }

   private World getWorld() {
      return (World)DataFixUtils.orElse(Optional.ofNullable(this.client.getServer()).map((integratedServer) -> {
         return integratedServer.getWorld(this.client.world.dimension.getType());
      }), this.client.world);
   }

   @Nullable
   private WorldChunk getChunk() {
      if (this.chunkFuture == null) {
         IntegratedServer integratedServer = this.client.getServer();
         if (integratedServer != null) {
            ServerWorld serverWorld = integratedServer.getWorld(this.client.world.dimension.getType());
            if (serverWorld != null) {
               this.chunkFuture = serverWorld.getChunkManager().getChunkFutureSyncOnMainThread(this.pos.x, this.pos.z, ChunkStatus.FULL, false).thenApply((either) -> {
                  return (WorldChunk)either.map((chunk) -> {
                     return (WorldChunk)chunk;
                  }, (unloaded) -> {
                     return null;
                  });
               });
            }
         }

         if (this.chunkFuture == null) {
            this.chunkFuture = CompletableFuture.completedFuture(this.getClientChunk());
         }
      }

      return (WorldChunk)this.chunkFuture.getNow((Object)null);
   }

   private WorldChunk getClientChunk() {
      if (this.chunk == null) {
         this.chunk = this.client.world.getChunk(this.pos.x, this.pos.z);
      }

      return this.chunk;
   }

   protected List<String> getRightText() {
      long l = Runtime.getRuntime().maxMemory();
      long m = Runtime.getRuntime().totalMemory();
      long n = Runtime.getRuntime().freeMemory();
      long o = m - n;
      List<String> list = Lists.newArrayList(new String[]{String.format("Java: %s %dbit", System.getProperty("java.version"), this.client.is64Bit() ? 64 : 32), String.format("Mem: % 2d%% %03d/%03dMB", o * 100L / l, toMiB(o), toMiB(l)), String.format("Allocated: % 2d%% %03dMB", m * 100L / l, toMiB(m)), "", String.format("CPU: %s", GlDebugInfo.getCpuInfo()), "", String.format("Display: %dx%d (%s)", MinecraftClient.getInstance().getWindow().getFramebufferWidth(), MinecraftClient.getInstance().getWindow().getFramebufferHeight(), GlDebugInfo.getVendor()), GlDebugInfo.getRenderer(), GlDebugInfo.getVersion()});
      if (this.client.hasReducedDebugInfo()) {
         return list;
      } else {
         BlockPos blockPos2;
         UnmodifiableIterator var12;
         Entry entry2;
         Iterator var16;
         Identifier identifier2;
         if (this.blockHit.getType() == HitResult.Type.BLOCK) {
            blockPos2 = ((BlockHitResult)this.blockHit).getBlockPos();
            BlockState blockState = this.client.world.getBlockState(blockPos2);
            list.add("");
            list.add(Formatting.UNDERLINE + "Targeted Block");
            list.add(String.valueOf(Registry.BLOCK.getId(blockState.getBlock())));
            var12 = blockState.getEntries().entrySet().iterator();

            while(var12.hasNext()) {
               entry2 = (Entry)var12.next();
               list.add(this.propertyToString(entry2));
            }

            var16 = this.client.getNetworkHandler().getTagManager().blocks().getTagsFor(blockState.getBlock()).iterator();

            while(var16.hasNext()) {
               identifier2 = (Identifier)var16.next();
               list.add("#" + identifier2);
            }
         }

         if (this.fluidHit.getType() == HitResult.Type.BLOCK) {
            blockPos2 = ((BlockHitResult)this.fluidHit).getBlockPos();
            FluidState fluidState = this.client.world.getFluidState(blockPos2);
            list.add("");
            list.add(Formatting.UNDERLINE + "Targeted Fluid");
            list.add(String.valueOf(Registry.FLUID.getId(fluidState.getFluid())));
            var12 = fluidState.getEntries().entrySet().iterator();

            while(var12.hasNext()) {
               entry2 = (Entry)var12.next();
               list.add(this.propertyToString(entry2));
            }

            var16 = this.client.getNetworkHandler().getTagManager().fluids().getTagsFor(fluidState.getFluid()).iterator();

            while(var16.hasNext()) {
               identifier2 = (Identifier)var16.next();
               list.add("#" + identifier2);
            }
         }

         Entity entity = this.client.targetedEntity;
         if (entity != null) {
            list.add("");
            list.add(Formatting.UNDERLINE + "Targeted Entity");
            list.add(String.valueOf(Registry.ENTITY_TYPE.getId(entity.getType())));
         }

         return list;
      }
   }

   private String propertyToString(Entry<Property<?>, Comparable<?>> propEntry) {
      Property<?> property = (Property)propEntry.getKey();
      Comparable<?> comparable = (Comparable)propEntry.getValue();
      String string = Util.getValueAsString(property, comparable);
      if (Boolean.TRUE.equals(comparable)) {
         string = Formatting.GREEN + string;
      } else if (Boolean.FALSE.equals(comparable)) {
         string = Formatting.RED + string;
      }

      return property.getName() + ": " + string;
   }

   private void drawMetricsData(MetricsData metricsData, int i, int j, boolean bl) {
      RenderSystem.disableDepthTest();
      int k = metricsData.getStartIndex();
      int l = metricsData.getCurrentIndex();
      long[] ls = metricsData.getSamples();
      int n = i;
      int o = Math.max(0, ls.length - j);
      int p = ls.length - o;
      int m = metricsData.wrapIndex(k + o);
      long q = 0L;
      int r = Integer.MAX_VALUE;
      int s = Integer.MIN_VALUE;

      int v;
      for(v = 0; v < p; ++v) {
         int u = (int)(ls[metricsData.wrapIndex(m + v)] / 1000000L);
         r = Math.min(r, u);
         s = Math.max(s, u);
         q += (long)u;
      }

      v = this.client.getWindow().getScaledHeight();
      fill(i, v - 60, i + p, v, -1873784752);
      BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
      RenderSystem.enableBlend();
      RenderSystem.disableTexture();
      RenderSystem.defaultBlendFunc();
      bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);

      for(Matrix4f matrix4f = Rotation3.identity().getMatrix(); m != l; m = metricsData.wrapIndex(m + 1)) {
         int w = metricsData.method_15248(ls[m], bl ? 30 : 60, bl ? 60 : 20);
         int x = bl ? 100 : 60;
         int y = this.getMetricsLineColor(MathHelper.clamp(w, 0, x), 0, x / 2, x);
         int z = y >> 24 & 255;
         int aa = y >> 16 & 255;
         int ab = y >> 8 & 255;
         int ac = y & 255;
         bufferBuilder.vertex(matrix4f, (float)(n + 1), (float)v, 0.0F).color(aa, ab, ac, z).next();
         bufferBuilder.vertex(matrix4f, (float)n, (float)v, 0.0F).color(aa, ab, ac, z).next();
         bufferBuilder.vertex(matrix4f, (float)n, (float)(v - w + 1), 0.0F).color(aa, ab, ac, z).next();
         bufferBuilder.vertex(matrix4f, (float)(n + 1), (float)(v - w + 1), 0.0F).color(aa, ab, ac, z).next();
         ++n;
      }

      bufferBuilder.end();
      BufferRenderer.draw(bufferBuilder);
      RenderSystem.enableTexture();
      RenderSystem.disableBlend();
      if (bl) {
         fill(i + 1, v - 30 + 1, i + 14, v - 30 + 10, -1873784752);
         this.fontRenderer.draw("60 FPS", (float)(i + 2), (float)(v - 30 + 2), 14737632);
         this.hLine(i, i + p - 1, v - 30, -1);
         fill(i + 1, v - 60 + 1, i + 14, v - 60 + 10, -1873784752);
         this.fontRenderer.draw("30 FPS", (float)(i + 2), (float)(v - 60 + 2), 14737632);
         this.hLine(i, i + p - 1, v - 60, -1);
      } else {
         fill(i + 1, v - 60 + 1, i + 14, v - 60 + 10, -1873784752);
         this.fontRenderer.draw("20 TPS", (float)(i + 2), (float)(v - 60 + 2), 14737632);
         this.hLine(i, i + p - 1, v - 60, -1);
      }

      this.hLine(i, i + p - 1, v - 1, -1);
      this.vLine(i, v - 60, v, -1);
      this.vLine(i + p - 1, v - 60, v, -1);
      if (bl && this.client.options.maxFps > 0 && this.client.options.maxFps <= 250) {
         this.hLine(i, i + p - 1, v - 1 - (int)(1800.0D / (double)this.client.options.maxFps), -16711681);
      }

      String string = r + " ms min";
      String string2 = q / (long)p + " ms avg";
      String string3 = s + " ms max";
      TextRenderer var10000 = this.fontRenderer;
      float var10002 = (float)(i + 2);
      int var10003 = v - 60;
      this.fontRenderer.getClass();
      var10000.drawWithShadow(string, var10002, (float)(var10003 - 9), 14737632);
      var10000 = this.fontRenderer;
      var10002 = (float)(i + p / 2 - this.fontRenderer.getStringWidth(string2) / 2);
      var10003 = v - 60;
      this.fontRenderer.getClass();
      var10000.drawWithShadow(string2, var10002, (float)(var10003 - 9), 14737632);
      var10000 = this.fontRenderer;
      var10002 = (float)(i + p - this.fontRenderer.getStringWidth(string3));
      var10003 = v - 60;
      this.fontRenderer.getClass();
      var10000.drawWithShadow(string3, var10002, (float)(var10003 - 9), 14737632);
      RenderSystem.enableDepthTest();
   }

   private int getMetricsLineColor(int value, int greenValue, int yellowValue, int redValue) {
      return value < yellowValue ? this.interpolateColor(-16711936, -256, (float)value / (float)yellowValue) : this.interpolateColor(-256, -65536, (float)(value - yellowValue) / (float)(redValue - yellowValue));
   }

   private int interpolateColor(int color1, int color2, float dt) {
      int i = color1 >> 24 & 255;
      int j = color1 >> 16 & 255;
      int k = color1 >> 8 & 255;
      int l = color1 & 255;
      int m = color2 >> 24 & 255;
      int n = color2 >> 16 & 255;
      int o = color2 >> 8 & 255;
      int p = color2 & 255;
      int q = MathHelper.clamp((int)MathHelper.lerp(dt, (float)i, (float)m), 0, 255);
      int r = MathHelper.clamp((int)MathHelper.lerp(dt, (float)j, (float)n), 0, 255);
      int s = MathHelper.clamp((int)MathHelper.lerp(dt, (float)k, (float)o), 0, 255);
      int t = MathHelper.clamp((int)MathHelper.lerp(dt, (float)l, (float)p), 0, 255);
      return q << 24 | r << 16 | s << 8 | t;
   }

   private static long toMiB(long bytes) {
      return bytes / 1024L / 1024L;
   }
}
