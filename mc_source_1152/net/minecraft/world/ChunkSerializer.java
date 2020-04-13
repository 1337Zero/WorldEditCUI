package net.minecraft.world;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.SimpleTickScheduler;
import net.minecraft.structure.StructureFeatures;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkSerializer {
   private static final Logger LOGGER = LogManager.getLogger();

   public static ProtoChunk deserialize(ServerWorld serverWorld, StructureManager structureManager, PointOfInterestStorage pointOfInterestStorage, ChunkPos chunkPos, CompoundTag compoundTag) {
      ChunkGenerator<?> chunkGenerator = serverWorld.getChunkManager().getChunkGenerator();
      BiomeSource biomeSource = chunkGenerator.getBiomeSource();
      CompoundTag compoundTag2 = compoundTag.getCompound("Level");
      ChunkPos chunkPos2 = new ChunkPos(compoundTag2.getInt("xPos"), compoundTag2.getInt("zPos"));
      if (!Objects.equals(chunkPos, chunkPos2)) {
         LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", chunkPos, chunkPos, chunkPos2);
      }

      BiomeArray biomeArray = new BiomeArray(chunkPos, biomeSource, compoundTag2.contains("Biomes", 11) ? compoundTag2.getIntArray("Biomes") : null);
      UpgradeData upgradeData = compoundTag2.contains("UpgradeData", 10) ? new UpgradeData(compoundTag2.getCompound("UpgradeData")) : UpgradeData.NO_UPGRADE_DATA;
      ChunkTickScheduler<Block> chunkTickScheduler = new ChunkTickScheduler((block) -> {
         return block == null || block.getDefaultState().isAir();
      }, chunkPos, compoundTag2.getList("ToBeTicked", 9));
      ChunkTickScheduler<Fluid> chunkTickScheduler2 = new ChunkTickScheduler((fluid) -> {
         return fluid == null || fluid == Fluids.EMPTY;
      }, chunkPos, compoundTag2.getList("LiquidsToBeTicked", 9));
      boolean bl = compoundTag2.getBoolean("isLightOn");
      ListTag listTag = compoundTag2.getList("Sections", 10);
      int i = true;
      ChunkSection[] chunkSections = new ChunkSection[16];
      boolean bl2 = serverWorld.getDimension().hasSkyLight();
      ChunkManager chunkManager = serverWorld.getChunkManager();
      LightingProvider lightingProvider = chunkManager.getLightingProvider();
      if (bl) {
         lightingProvider.setRetainData(chunkPos, true);
      }

      for(int j = 0; j < listTag.size(); ++j) {
         CompoundTag compoundTag3 = listTag.getCompound(j);
         int k = compoundTag3.getByte("Y");
         if (compoundTag3.contains("Palette", 9) && compoundTag3.contains("BlockStates", 12)) {
            ChunkSection chunkSection = new ChunkSection(k << 4);
            chunkSection.getContainer().read(compoundTag3.getList("Palette", 10), compoundTag3.getLongArray("BlockStates"));
            chunkSection.calculateCounts();
            if (!chunkSection.isEmpty()) {
               chunkSections[k] = chunkSection;
            }

            pointOfInterestStorage.initForPalette(chunkPos, chunkSection);
         }

         if (bl) {
            if (compoundTag3.contains("BlockLight", 7)) {
               lightingProvider.queueData(LightType.BLOCK, ChunkSectionPos.from(chunkPos, k), new ChunkNibbleArray(compoundTag3.getByteArray("BlockLight")));
            }

            if (bl2 && compoundTag3.contains("SkyLight", 7)) {
               lightingProvider.queueData(LightType.SKY, ChunkSectionPos.from(chunkPos, k), new ChunkNibbleArray(compoundTag3.getByteArray("SkyLight")));
            }
         }
      }

      long l = compoundTag2.getLong("InhabitedTime");
      ChunkStatus.ChunkType chunkType = getChunkType(compoundTag);
      Object chunk2;
      if (chunkType == ChunkStatus.ChunkType.LEVELCHUNK) {
         ListTag var10000;
         Function var10001;
         DefaultedRegistry var10002;
         Object tickScheduler2;
         if (compoundTag2.contains("TileTicks", 9)) {
            var10000 = compoundTag2.getList("TileTicks", 10);
            var10001 = Registry.BLOCK::getId;
            var10002 = Registry.BLOCK;
            var10002.getClass();
            tickScheduler2 = SimpleTickScheduler.fromNbt(var10000, var10001, var10002::get);
         } else {
            tickScheduler2 = chunkTickScheduler;
         }

         Object tickScheduler4;
         if (compoundTag2.contains("LiquidTicks", 9)) {
            var10000 = compoundTag2.getList("LiquidTicks", 10);
            var10001 = Registry.FLUID::getId;
            var10002 = Registry.FLUID;
            var10002.getClass();
            tickScheduler4 = SimpleTickScheduler.fromNbt(var10000, var10001, var10002::get);
         } else {
            tickScheduler4 = chunkTickScheduler2;
         }

         chunk2 = new WorldChunk(serverWorld.getWorld(), chunkPos, biomeArray, upgradeData, (TickScheduler)tickScheduler2, (TickScheduler)tickScheduler4, l, chunkSections, (worldChunk) -> {
            writeEntities(compoundTag2, worldChunk);
         });
      } else {
         ProtoChunk protoChunk = new ProtoChunk(chunkPos, upgradeData, chunkSections, chunkTickScheduler, chunkTickScheduler2);
         protoChunk.method_22405(biomeArray);
         chunk2 = protoChunk;
         protoChunk.setInhabitedTime(l);
         protoChunk.setStatus(ChunkStatus.get(compoundTag2.getString("Status")));
         if (protoChunk.getStatus().isAtLeast(ChunkStatus.FEATURES)) {
            protoChunk.setLightingProvider(lightingProvider);
         }

         if (!bl && protoChunk.getStatus().isAtLeast(ChunkStatus.LIGHT)) {
            Iterator var41 = BlockPos.iterate(chunkPos.getStartX(), 0, chunkPos.getStartZ(), chunkPos.getEndX(), 255, chunkPos.getEndZ()).iterator();

            while(var41.hasNext()) {
               BlockPos blockPos = (BlockPos)var41.next();
               if (((Chunk)chunk2).getBlockState(blockPos).getLuminance() != 0) {
                  protoChunk.addLightSource(blockPos);
               }
            }
         }
      }

      ((Chunk)chunk2).setLightOn(bl);
      CompoundTag compoundTag4 = compoundTag2.getCompound("Heightmaps");
      EnumSet<Heightmap.Type> enumSet = EnumSet.noneOf(Heightmap.Type.class);
      Iterator var43 = ((Chunk)chunk2).getStatus().getHeightmapTypes().iterator();

      while(var43.hasNext()) {
         Heightmap.Type type = (Heightmap.Type)var43.next();
         String string = type.getName();
         if (compoundTag4.contains(string, 12)) {
            ((Chunk)chunk2).setHeightmap(type, compoundTag4.getLongArray(string));
         } else {
            enumSet.add(type);
         }
      }

      Heightmap.populateHeightmaps((Chunk)chunk2, enumSet);
      CompoundTag compoundTag5 = compoundTag2.getCompound("Structures");
      ((Chunk)chunk2).setStructureStarts(readStructureStarts(chunkGenerator, structureManager, compoundTag5));
      ((Chunk)chunk2).setStructureReferences(readStructureReferences(chunkPos, compoundTag5));
      if (compoundTag2.getBoolean("shouldSave")) {
         ((Chunk)chunk2).setShouldSave(true);
      }

      ListTag listTag2 = compoundTag2.getList("PostProcessing", 9);

      ListTag listTag4;
      int o;
      for(int m = 0; m < listTag2.size(); ++m) {
         listTag4 = listTag2.getList(m);

         for(o = 0; o < listTag4.size(); ++o) {
            ((Chunk)chunk2).markBlockForPostProcessing(listTag4.getShort(o), m);
         }
      }

      if (chunkType == ChunkStatus.ChunkType.LEVELCHUNK) {
         return new ReadOnlyChunk((WorldChunk)chunk2);
      } else {
         ProtoChunk protoChunk2 = (ProtoChunk)chunk2;
         listTag4 = compoundTag2.getList("Entities", 10);

         for(o = 0; o < listTag4.size(); ++o) {
            protoChunk2.addEntity(listTag4.getCompound(o));
         }

         ListTag listTag5 = compoundTag2.getList("TileEntities", 10);

         CompoundTag compoundTag7;
         for(int p = 0; p < listTag5.size(); ++p) {
            compoundTag7 = listTag5.getCompound(p);
            ((Chunk)chunk2).addPendingBlockEntityTag(compoundTag7);
         }

         ListTag listTag6 = compoundTag2.getList("Lights", 9);

         for(int q = 0; q < listTag6.size(); ++q) {
            ListTag listTag7 = listTag6.getList(q);

            for(int r = 0; r < listTag7.size(); ++r) {
               protoChunk2.addLightSource(listTag7.getShort(r), q);
            }
         }

         compoundTag7 = compoundTag2.getCompound("CarvingMasks");
         Iterator var51 = compoundTag7.getKeys().iterator();

         while(var51.hasNext()) {
            String string2 = (String)var51.next();
            GenerationStep.Carver carver = GenerationStep.Carver.valueOf(string2);
            protoChunk2.setCarvingMask(carver, BitSet.valueOf(compoundTag7.getByteArray(string2)));
         }

         return protoChunk2;
      }
   }

   public static CompoundTag serialize(ServerWorld serverWorld, Chunk chunk) {
      ChunkPos chunkPos = chunk.getPos();
      CompoundTag compoundTag = new CompoundTag();
      CompoundTag compoundTag2 = new CompoundTag();
      compoundTag.putInt("DataVersion", SharedConstants.getGameVersion().getWorldVersion());
      compoundTag.put("Level", compoundTag2);
      compoundTag2.putInt("xPos", chunkPos.x);
      compoundTag2.putInt("zPos", chunkPos.z);
      compoundTag2.putLong("LastUpdate", serverWorld.getTime());
      compoundTag2.putLong("InhabitedTime", chunk.getInhabitedTime());
      compoundTag2.putString("Status", chunk.getStatus().getId());
      UpgradeData upgradeData = chunk.getUpgradeData();
      if (!upgradeData.isDone()) {
         compoundTag2.put("UpgradeData", upgradeData.toTag());
      }

      ChunkSection[] chunkSections = chunk.getSectionArray();
      ListTag listTag = new ListTag();
      LightingProvider lightingProvider = serverWorld.getChunkManager().getLightingProvider();
      boolean bl = chunk.isLightOn();

      CompoundTag compoundTag7;
      for(int i = -1; i < 17; ++i) {
         ChunkSection chunkSection = (ChunkSection)Arrays.stream(chunkSections).filter((chunkSectionx) -> {
            return chunkSectionx != null && chunkSectionx.getYOffset() >> 4 == i;
         }).findFirst().orElse(WorldChunk.EMPTY_SECTION);
         ChunkNibbleArray chunkNibbleArray = lightingProvider.get(LightType.BLOCK).getLightArray(ChunkSectionPos.from(chunkPos, i));
         ChunkNibbleArray chunkNibbleArray2 = lightingProvider.get(LightType.SKY).getLightArray(ChunkSectionPos.from(chunkPos, i));
         if (chunkSection != WorldChunk.EMPTY_SECTION || chunkNibbleArray != null || chunkNibbleArray2 != null) {
            compoundTag7 = new CompoundTag();
            compoundTag7.putByte("Y", (byte)(i & 255));
            if (chunkSection != WorldChunk.EMPTY_SECTION) {
               chunkSection.getContainer().write(compoundTag7, "Palette", "BlockStates");
            }

            if (chunkNibbleArray != null && !chunkNibbleArray.isUninitialized()) {
               compoundTag7.putByteArray("BlockLight", chunkNibbleArray.asByteArray());
            }

            if (chunkNibbleArray2 != null && !chunkNibbleArray2.isUninitialized()) {
               compoundTag7.putByteArray("SkyLight", chunkNibbleArray2.asByteArray());
            }

            listTag.add(compoundTag7);
         }
      }

      compoundTag2.put("Sections", listTag);
      if (bl) {
         compoundTag2.putBoolean("isLightOn", true);
      }

      BiomeArray biomeArray = chunk.getBiomeArray();
      if (biomeArray != null) {
         compoundTag2.putIntArray("Biomes", biomeArray.toIntArray());
      }

      ListTag listTag2 = new ListTag();
      Iterator var20 = chunk.getBlockEntityPositions().iterator();

      CompoundTag compoundTag6;
      while(var20.hasNext()) {
         BlockPos blockPos = (BlockPos)var20.next();
         compoundTag6 = chunk.method_20598(blockPos);
         if (compoundTag6 != null) {
            listTag2.add(compoundTag6);
         }
      }

      compoundTag2.put("TileEntities", listTag2);
      ListTag listTag3 = new ListTag();
      if (chunk.getStatus().getChunkType() == ChunkStatus.ChunkType.LEVELCHUNK) {
         WorldChunk worldChunk = (WorldChunk)chunk;
         worldChunk.setUnsaved(false);

         for(int k = 0; k < worldChunk.getEntitySectionArray().length; ++k) {
            Iterator var28 = worldChunk.getEntitySectionArray()[k].iterator();

            while(var28.hasNext()) {
               Entity entity = (Entity)var28.next();
               CompoundTag compoundTag5 = new CompoundTag();
               if (entity.saveToTag(compoundTag5)) {
                  worldChunk.setUnsaved(true);
                  listTag3.add(compoundTag5);
               }
            }
         }
      } else {
         ProtoChunk protoChunk = (ProtoChunk)chunk;
         listTag3.addAll(protoChunk.getEntities());
         compoundTag2.put("Lights", toNbt(protoChunk.getLightSourcesBySection()));
         compoundTag6 = new CompoundTag();
         GenerationStep.Carver[] var29 = GenerationStep.Carver.values();
         int var31 = var29.length;

         for(int var33 = 0; var33 < var31; ++var33) {
            GenerationStep.Carver carver = var29[var33];
            compoundTag6.putByteArray(carver.toString(), chunk.getCarvingMask(carver).toByteArray());
         }

         compoundTag2.put("CarvingMasks", compoundTag6);
      }

      compoundTag2.put("Entities", listTag3);
      TickScheduler<Block> tickScheduler = chunk.getBlockTickScheduler();
      if (tickScheduler instanceof ChunkTickScheduler) {
         compoundTag2.put("ToBeTicked", ((ChunkTickScheduler)tickScheduler).toNbt());
      } else if (tickScheduler instanceof SimpleTickScheduler) {
         compoundTag2.put("TileTicks", ((SimpleTickScheduler)tickScheduler).toNbt(serverWorld.getTime()));
      } else {
         compoundTag2.put("TileTicks", serverWorld.getBlockTickScheduler().toTag(chunkPos));
      }

      TickScheduler<Fluid> tickScheduler2 = chunk.getFluidTickScheduler();
      if (tickScheduler2 instanceof ChunkTickScheduler) {
         compoundTag2.put("LiquidsToBeTicked", ((ChunkTickScheduler)tickScheduler2).toNbt());
      } else if (tickScheduler2 instanceof SimpleTickScheduler) {
         compoundTag2.put("LiquidTicks", ((SimpleTickScheduler)tickScheduler2).toNbt(serverWorld.getTime()));
      } else {
         compoundTag2.put("LiquidTicks", serverWorld.getFluidTickScheduler().toTag(chunkPos));
      }

      compoundTag2.put("PostProcessing", toNbt(chunk.getPostProcessingLists()));
      compoundTag7 = new CompoundTag();
      Iterator var32 = chunk.getHeightmaps().iterator();

      while(var32.hasNext()) {
         Entry<Heightmap.Type, Heightmap> entry = (Entry)var32.next();
         if (chunk.getStatus().getHeightmapTypes().contains(entry.getKey())) {
            compoundTag7.put(((Heightmap.Type)entry.getKey()).getName(), new LongArrayTag(((Heightmap)entry.getValue()).asLongArray()));
         }
      }

      compoundTag2.put("Heightmaps", compoundTag7);
      compoundTag2.put("Structures", writeStructures(chunkPos, chunk.getStructureStarts(), chunk.getStructureReferences()));
      return compoundTag;
   }

   public static ChunkStatus.ChunkType getChunkType(@Nullable CompoundTag tag) {
      if (tag != null) {
         ChunkStatus chunkStatus = ChunkStatus.get(tag.getCompound("Level").getString("Status"));
         if (chunkStatus != null) {
            return chunkStatus.getChunkType();
         }
      }

      return ChunkStatus.ChunkType.PROTOCHUNK;
   }

   private static void writeEntities(CompoundTag tag, WorldChunk chunk) {
      ListTag listTag = tag.getList("Entities", 10);
      World world = chunk.getWorld();

      for(int i = 0; i < listTag.size(); ++i) {
         CompoundTag compoundTag = listTag.getCompound(i);
         EntityType.loadEntityWithPassengers(compoundTag, world, (entity) -> {
            chunk.addEntity(entity);
            return entity;
         });
         chunk.setUnsaved(true);
      }

      ListTag listTag2 = tag.getList("TileEntities", 10);

      for(int j = 0; j < listTag2.size(); ++j) {
         CompoundTag compoundTag2 = listTag2.getCompound(j);
         boolean bl = compoundTag2.getBoolean("keepPacked");
         if (bl) {
            chunk.addPendingBlockEntityTag(compoundTag2);
         } else {
            BlockEntity blockEntity = BlockEntity.createFromTag(compoundTag2);
            if (blockEntity != null) {
               chunk.addBlockEntity(blockEntity);
            }
         }
      }

   }

   private static CompoundTag writeStructures(ChunkPos pos, Map<String, StructureStart> structureStarts, Map<String, LongSet> structureReferences) {
      CompoundTag compoundTag = new CompoundTag();
      CompoundTag compoundTag2 = new CompoundTag();
      Iterator var5 = structureStarts.entrySet().iterator();

      while(var5.hasNext()) {
         Entry<String, StructureStart> entry = (Entry)var5.next();
         compoundTag2.put((String)entry.getKey(), ((StructureStart)entry.getValue()).toTag(pos.x, pos.z));
      }

      compoundTag.put("Starts", compoundTag2);
      CompoundTag compoundTag3 = new CompoundTag();
      Iterator var9 = structureReferences.entrySet().iterator();

      while(var9.hasNext()) {
         Entry<String, LongSet> entry2 = (Entry)var9.next();
         compoundTag3.put((String)entry2.getKey(), new LongArrayTag((LongSet)entry2.getValue()));
      }

      compoundTag.put("References", compoundTag3);
      return compoundTag;
   }

   private static Map<String, StructureStart> readStructureStarts(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, CompoundTag compoundTag) {
      Map<String, StructureStart> map = Maps.newHashMap();
      CompoundTag compoundTag2 = compoundTag.getCompound("Starts");
      Iterator var5 = compoundTag2.getKeys().iterator();

      while(var5.hasNext()) {
         String string = (String)var5.next();
         map.put(string, StructureFeatures.readStructureStart(chunkGenerator, structureManager, compoundTag2.getCompound(string)));
      }

      return map;
   }

   private static Map<String, LongSet> readStructureReferences(ChunkPos chunkPos, CompoundTag compoundTag) {
      Map<String, LongSet> map = Maps.newHashMap();
      CompoundTag compoundTag2 = compoundTag.getCompound("References");
      Iterator var4 = compoundTag2.getKeys().iterator();

      while(var4.hasNext()) {
         String string = (String)var4.next();
         map.put(string, new LongOpenHashSet(Arrays.stream(compoundTag2.getLongArray(string)).filter((l) -> {
            ChunkPos chunkPos2 = new ChunkPos(l);
            if (chunkPos2.method_24022(chunkPos) > 8) {
               LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", string, chunkPos2, chunkPos);
               return false;
            } else {
               return true;
            }
         }).toArray()));
      }

      return map;
   }

   public static ListTag toNbt(ShortList[] lists) {
      ListTag listTag = new ListTag();
      ShortList[] var2 = lists;
      int var3 = lists.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         ShortList shortList = var2[var4];
         ListTag listTag2 = new ListTag();
         if (shortList != null) {
            ShortListIterator var7 = shortList.iterator();

            while(var7.hasNext()) {
               Short var8 = (Short)var7.next();
               listTag2.add(ShortTag.of(var8));
            }
         }

         listTag.add(listTag2);
      }

      return listTag;
   }
}
