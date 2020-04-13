package net.minecraft.client.render.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Position;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class VillageDebugRenderer implements DebugRenderer.Renderer {
   private static final Logger LOGGER = LogManager.getLogger();
   private final MinecraftClient client;
   private final Map<BlockPos, VillageDebugRenderer.PointOfInterest> pointsOfInterest = Maps.newHashMap();
   private final Set<ChunkSectionPos> sections = Sets.newHashSet();
   private final Map<UUID, VillageDebugRenderer.Brain> brains = Maps.newHashMap();
   private UUID targetedEntity;

   public VillageDebugRenderer(MinecraftClient minecraftClient) {
      this.client = minecraftClient;
   }

   public void clear() {
      this.pointsOfInterest.clear();
      this.sections.clear();
      this.brains.clear();
      this.targetedEntity = null;
   }

   public void addPointOfInterest(VillageDebugRenderer.PointOfInterest pointOfInterest) {
      this.pointsOfInterest.put(pointOfInterest.pos, pointOfInterest);
   }

   public void removePointOfInterest(BlockPos blockPos) {
      this.pointsOfInterest.remove(blockPos);
   }

   public void setFreeTicketCount(BlockPos pos, int freeTicketCount) {
      VillageDebugRenderer.PointOfInterest pointOfInterest = (VillageDebugRenderer.PointOfInterest)this.pointsOfInterest.get(pos);
      if (pointOfInterest == null) {
         LOGGER.warn("Strange, setFreeTicketCount was called for an unknown POI: " + pos);
      } else {
         pointOfInterest.freeTicketCount = freeTicketCount;
      }
   }

   public void addSection(ChunkSectionPos pos) {
      this.sections.add(pos);
   }

   public void removeSection(ChunkSectionPos pos) {
      this.sections.remove(pos);
   }

   public void addBrain(VillageDebugRenderer.Brain brain) {
      this.brains.put(brain.uuid, brain);
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      RenderSystem.pushMatrix();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableTexture();
      this.method_23135(cameraX, cameraY, cameraZ);
      RenderSystem.enableTexture();
      RenderSystem.disableBlend();
      RenderSystem.popMatrix();
      if (!this.client.player.isSpectator()) {
         this.updateTargetedEntity();
      }

   }

   private void method_23135(double d, double e, double f) {
      BlockPos blockPos = new BlockPos(d, e, f);
      this.sections.forEach((chunkSectionPos) -> {
         if (blockPos.isWithinDistance(chunkSectionPos.getCenterPos(), 60.0D)) {
            drawSection(chunkSectionPos);
         }

      });
      this.brains.values().forEach((brain) -> {
         if (this.isClose(brain)) {
            this.drawBrain(brain, d, e, f);
         }

      });
      Iterator var8 = this.pointsOfInterest.keySet().iterator();

      while(var8.hasNext()) {
         BlockPos blockPos2 = (BlockPos)var8.next();
         if (blockPos.isWithinDistance(blockPos2, 30.0D)) {
            drawPointOfInterest(blockPos2);
         }
      }

      this.pointsOfInterest.values().forEach((pointOfInterest) -> {
         if (blockPos.isWithinDistance(pointOfInterest.pos, 30.0D)) {
            this.drawPointOfInterestInfo(pointOfInterest);
         }

      });
      this.getGhostPointsOfInterest().forEach((blockPos2x, list) -> {
         if (blockPos.isWithinDistance(blockPos2x, 30.0D)) {
            this.drawGhostPointOfInterest(blockPos2x, list);
         }

      });
   }

   private static void drawSection(ChunkSectionPos pos) {
      float f = 1.0F;
      BlockPos blockPos = pos.getCenterPos();
      BlockPos blockPos2 = blockPos.add(-1.0D, -1.0D, -1.0D);
      BlockPos blockPos3 = blockPos.add(1.0D, 1.0D, 1.0D);
      DebugRenderer.drawBox(blockPos2, blockPos3, 0.2F, 1.0F, 0.2F, 0.15F);
   }

   private static void drawPointOfInterest(BlockPos pos) {
      float f = 0.05F;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      DebugRenderer.drawBox(pos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
   }

   private void drawGhostPointOfInterest(BlockPos pos, List<String> brains) {
      float f = 0.05F;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      DebugRenderer.drawBox(pos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
      drawString("" + brains, (BlockPos)pos, 0, -256);
      drawString("Ghost POI", (BlockPos)pos, 1, -65536);
   }

   private void drawPointOfInterestInfo(VillageDebugRenderer.PointOfInterest pointOfInterest) {
      int i = 0;
      if (this.getVillagerNames(pointOfInterest).size() < 4) {
         drawString("" + this.getVillagerNames(pointOfInterest), (VillageDebugRenderer.PointOfInterest)pointOfInterest, i, -256);
      } else {
         drawString("" + this.getVillagerNames(pointOfInterest).size() + " ticket holders", (VillageDebugRenderer.PointOfInterest)pointOfInterest, i, -256);
      }

      int i = i + 1;
      drawString("Free tickets: " + pointOfInterest.freeTicketCount, (VillageDebugRenderer.PointOfInterest)pointOfInterest, i, -256);
      ++i;
      drawString(pointOfInterest.field_18932, (VillageDebugRenderer.PointOfInterest)pointOfInterest, i, -1);
   }

   private void drawPath(VillageDebugRenderer.Brain brain, double cameraX, double cameraY, double cameraZ) {
      if (brain.path != null) {
         PathfindingDebugRenderer.drawPath(brain.path, 0.5F, false, false, cameraX, cameraY, cameraZ);
      }

   }

   private void drawBrain(VillageDebugRenderer.Brain brain, double cameraX, double cameraY, double cameraZ) {
      boolean bl = this.isTargeted(brain);
      int i = 0;
      drawString(brain.pos, i, brain.field_19328, -1, 0.03F);
      int i = i + 1;
      if (bl) {
         drawString(brain.pos, i, brain.profession + " " + brain.xp + "xp", -1, 0.02F);
         ++i;
      }

      if (bl && !brain.field_19372.equals("")) {
         drawString(brain.pos, i, brain.field_19372, -98404, 0.02F);
         ++i;
      }

      Iterator var10;
      String string4;
      if (bl) {
         for(var10 = brain.field_18928.iterator(); var10.hasNext(); ++i) {
            string4 = (String)var10.next();
            drawString(brain.pos, i, string4, -16711681, 0.02F);
         }
      }

      if (bl) {
         for(var10 = brain.field_18927.iterator(); var10.hasNext(); ++i) {
            string4 = (String)var10.next();
            drawString(brain.pos, i, string4, -16711936, 0.02F);
         }
      }

      if (brain.wantsGolem) {
         drawString(brain.pos, i, "Wants Golem", -23296, 0.02F);
         ++i;
      }

      if (bl) {
         for(var10 = brain.field_19375.iterator(); var10.hasNext(); ++i) {
            string4 = (String)var10.next();
            if (string4.startsWith(brain.field_19328)) {
               drawString(brain.pos, i, string4, -1, 0.02F);
            } else {
               drawString(brain.pos, i, string4, -23296, 0.02F);
            }
         }
      }

      if (bl) {
         for(var10 = Lists.reverse(brain.field_19374).iterator(); var10.hasNext(); ++i) {
            string4 = (String)var10.next();
            drawString(brain.pos, i, string4, -3355444, 0.02F);
         }
      }

      if (bl) {
         this.drawPath(brain, cameraX, cameraY, cameraZ);
      }

   }

   private static void drawString(String string, VillageDebugRenderer.PointOfInterest pointOfInterest, int offsetY, int color) {
      BlockPos blockPos = pointOfInterest.pos;
      drawString(string, blockPos, offsetY, color);
   }

   private static void drawString(String string, BlockPos pos, int offsetY, int color) {
      double d = 1.3D;
      double e = 0.2D;
      double f = (double)pos.getX() + 0.5D;
      double g = (double)pos.getY() + 1.3D + (double)offsetY * 0.2D;
      double h = (double)pos.getZ() + 0.5D;
      DebugRenderer.drawString(string, f, g, h, color, 0.02F, true, 0.0F, true);
   }

   private static void drawString(Position pos, int offsetY, String string, int color, float size) {
      double d = 2.4D;
      double e = 0.25D;
      BlockPos blockPos = new BlockPos(pos);
      double f = (double)blockPos.getX() + 0.5D;
      double g = pos.getY() + 2.4D + (double)offsetY * 0.25D;
      double h = (double)blockPos.getZ() + 0.5D;
      float i = 0.5F;
      DebugRenderer.drawString(string, f, g, h, color, size, false, 0.5F, true);
   }

   private Set<String> getVillagerNames(VillageDebugRenderer.PointOfInterest pointOfInterest) {
      return (Set)this.getBrains(pointOfInterest.pos).stream().map(NameGenerator::name).collect(Collectors.toSet());
   }

   private boolean isTargeted(VillageDebugRenderer.Brain brain) {
      return Objects.equals(this.targetedEntity, brain.uuid);
   }

   private boolean isClose(VillageDebugRenderer.Brain brain) {
      PlayerEntity playerEntity = this.client.player;
      BlockPos blockPos = new BlockPos(playerEntity.getX(), brain.pos.getY(), playerEntity.getZ());
      BlockPos blockPos2 = new BlockPos(brain.pos);
      return blockPos.isWithinDistance(blockPos2, 30.0D);
   }

   private Collection<UUID> getBrains(BlockPos pointOfInterest) {
      return (Collection)this.brains.values().stream().filter((brain) -> {
         return brain.isPointOfInterest(pointOfInterest);
      }).map(VillageDebugRenderer.Brain::getUuid).collect(Collectors.toSet());
   }

   private Map<BlockPos, List<String>> getGhostPointsOfInterest() {
      Map<BlockPos, List<String>> map = Maps.newHashMap();
      Iterator var2 = this.brains.values().iterator();

      while(var2.hasNext()) {
         VillageDebugRenderer.Brain brain = (VillageDebugRenderer.Brain)var2.next();
         Iterator var4 = brain.pointsOfInterest.iterator();

         while(var4.hasNext()) {
            BlockPos blockPos = (BlockPos)var4.next();
            if (!this.pointsOfInterest.containsKey(blockPos)) {
               List<String> list = (List)map.get(blockPos);
               if (list == null) {
                  list = Lists.newArrayList();
                  map.put(blockPos, list);
               }

               ((List)list).add(brain.field_19328);
            }
         }
      }

      return map;
   }

   private void updateTargetedEntity() {
      DebugRenderer.getTargetedEntity(this.client.getCameraEntity(), 8).ifPresent((entity) -> {
         this.targetedEntity = entity.getUuid();
      });
   }

   @Environment(EnvType.CLIENT)
   public static class Brain {
      public final UUID uuid;
      public final int field_18924;
      public final String field_19328;
      public final String profession;
      public final int xp;
      public final Position pos;
      public final String field_19372;
      public final Path path;
      public final boolean wantsGolem;
      public final List<String> field_18927 = Lists.newArrayList();
      public final List<String> field_18928 = Lists.newArrayList();
      public final List<String> field_19374 = Lists.newArrayList();
      public final List<String> field_19375 = Lists.newArrayList();
      public final Set<BlockPos> pointsOfInterest = Sets.newHashSet();

      public Brain(UUID uuid, int i, String string, String profession, int xp, Position pos, String string2, @Nullable Path path, boolean wantsGolem) {
         this.uuid = uuid;
         this.field_18924 = i;
         this.field_19328 = string;
         this.profession = profession;
         this.xp = xp;
         this.pos = pos;
         this.field_19372 = string2;
         this.path = path;
         this.wantsGolem = wantsGolem;
      }

      private boolean isPointOfInterest(BlockPos blockPos) {
         Stream var10000 = this.pointsOfInterest.stream();
         blockPos.getClass();
         return var10000.anyMatch(blockPos::equals);
      }

      public UUID getUuid() {
         return this.uuid;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class PointOfInterest {
      public final BlockPos pos;
      public String field_18932;
      public int freeTicketCount;

      public PointOfInterest(BlockPos blockPos, String string, int i) {
         this.pos = blockPos;
         this.field_18932 = string;
         this.freeTicketCount = i;
      }
   }
}
