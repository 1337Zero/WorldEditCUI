package net.minecraft.world.chunk;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.DummyClientTickScheduler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.SimpleTickScheduler;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.TypeFilterableList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkTickScheduler;
import net.minecraft.world.Heightmap;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import net.minecraft.world.level.LevelGeneratorType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldChunk implements Chunk {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final ChunkSection EMPTY_SECTION = null;
   private final ChunkSection[] sections;
   private BiomeArray biomeArray;
   private final Map<BlockPos, CompoundTag> pendingBlockEntityTags;
   private boolean loadedToWorld;
   private final World world;
   private final Map<Heightmap.Type, Heightmap> heightmaps;
   private final UpgradeData upgradeData;
   private final Map<BlockPos, BlockEntity> blockEntities;
   private final TypeFilterableList<Entity>[] entitySections;
   private final Map<String, StructureStart> structureStarts;
   private final Map<String, LongSet> structureReferences;
   private final ShortList[] postProcessingLists;
   private TickScheduler<Block> blockTickScheduler;
   private TickScheduler<Fluid> fluidTickScheduler;
   private boolean unsaved;
   private long lastSaveTime;
   private volatile boolean shouldSave;
   private long inhabitedTime;
   @Nullable
   private Supplier<ChunkHolder.LevelType> levelTypeProvider;
   @Nullable
   private Consumer<WorldChunk> loadToWorldConsumer;
   private final ChunkPos pos;
   private volatile boolean isLightOn;

   public WorldChunk(World world, ChunkPos chunkPos, BiomeArray biomeArray) {
      this(world, chunkPos, biomeArray, UpgradeData.NO_UPGRADE_DATA, DummyClientTickScheduler.get(), DummyClientTickScheduler.get(), 0L, (ChunkSection[])null, (Consumer)null);
   }

   public WorldChunk(World world, ChunkPos chunkPos, BiomeArray biomeArray, UpgradeData upgradeData, TickScheduler<Block> tickScheduler, TickScheduler<Fluid> tickScheduler2, long l, @Nullable ChunkSection[] chunkSections, @Nullable Consumer<WorldChunk> consumer) {
      this.sections = new ChunkSection[16];
      this.pendingBlockEntityTags = Maps.newHashMap();
      this.heightmaps = Maps.newEnumMap(Heightmap.Type.class);
      this.blockEntities = Maps.newHashMap();
      this.structureStarts = Maps.newHashMap();
      this.structureReferences = Maps.newHashMap();
      this.postProcessingLists = new ShortList[16];
      this.entitySections = (TypeFilterableList[])(new TypeFilterableList[16]);
      this.world = world;
      this.pos = chunkPos;
      this.upgradeData = upgradeData;
      Heightmap.Type[] var11 = Heightmap.Type.values();
      int var12 = var11.length;

      for(int var13 = 0; var13 < var12; ++var13) {
         Heightmap.Type type = var11[var13];
         if (ChunkStatus.FULL.getHeightmapTypes().contains(type)) {
            this.heightmaps.put(type, new Heightmap(this, type));
         }
      }

      for(int i = 0; i < this.entitySections.length; ++i) {
         this.entitySections[i] = new TypeFilterableList(Entity.class);
      }

      this.biomeArray = biomeArray;
      this.blockTickScheduler = tickScheduler;
      this.fluidTickScheduler = tickScheduler2;
      this.inhabitedTime = l;
      this.loadToWorldConsumer = consumer;
      if (chunkSections != null) {
         if (this.sections.length == chunkSections.length) {
            System.arraycopy(chunkSections, 0, this.sections, 0, this.sections.length);
         } else {
            LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", chunkSections.length, this.sections.length);
         }
      }

   }

   public WorldChunk(World world, ProtoChunk protoChunk) {
      this(world, protoChunk.getPos(), protoChunk.getBiomeArray(), protoChunk.getUpgradeData(), protoChunk.getBlockTickScheduler(), protoChunk.getFluidTickScheduler(), protoChunk.getInhabitedTime(), protoChunk.getSectionArray(), (Consumer)null);
      Iterator var3 = protoChunk.getEntities().iterator();

      while(var3.hasNext()) {
         CompoundTag compoundTag = (CompoundTag)var3.next();
         EntityType.loadEntityWithPassengers(compoundTag, world, (entity) -> {
            this.addEntity(entity);
            return entity;
         });
      }

      var3 = protoChunk.getBlockEntities().values().iterator();

      while(var3.hasNext()) {
         BlockEntity blockEntity = (BlockEntity)var3.next();
         this.addBlockEntity(blockEntity);
      }

      this.pendingBlockEntityTags.putAll(protoChunk.getBlockEntityTags());

      for(int i = 0; i < protoChunk.getPostProcessingLists().length; ++i) {
         this.postProcessingLists[i] = protoChunk.getPostProcessingLists()[i];
      }

      this.setStructureStarts(protoChunk.getStructureStarts());
      this.setStructureReferences(protoChunk.getStructureReferences());
      var3 = protoChunk.getHeightmaps().iterator();

      while(var3.hasNext()) {
         Entry<Heightmap.Type, Heightmap> entry = (Entry)var3.next();
         if (ChunkStatus.FULL.getHeightmapTypes().contains(entry.getKey())) {
            this.getHeightmap((Heightmap.Type)entry.getKey()).setTo(((Heightmap)entry.getValue()).asLongArray());
         }
      }

      this.setLightOn(protoChunk.isLightOn());
      this.shouldSave = true;
   }

   public Heightmap getHeightmap(Heightmap.Type type) {
      return (Heightmap)this.heightmaps.computeIfAbsent(type, (typex) -> {
         return new Heightmap(this, typex);
      });
   }

   public Set<BlockPos> getBlockEntityPositions() {
      Set<BlockPos> set = Sets.newHashSet(this.pendingBlockEntityTags.keySet());
      set.addAll(this.blockEntities.keySet());
      return set;
   }

   public ChunkSection[] getSectionArray() {
      return this.sections;
   }

   public BlockState getBlockState(BlockPos pos) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      if (this.world.getGeneratorType() == LevelGeneratorType.DEBUG_ALL_BLOCK_STATES) {
         BlockState blockState = null;
         if (j == 60) {
            blockState = Blocks.BARRIER.getDefaultState();
         }

         if (j == 70) {
            blockState = DebugChunkGenerator.getBlockState(i, k);
         }

         return blockState == null ? Blocks.AIR.getDefaultState() : blockState;
      } else {
         try {
            if (j >= 0 && j >> 4 < this.sections.length) {
               ChunkSection chunkSection = this.sections[j >> 4];
               if (!ChunkSection.isEmpty(chunkSection)) {
                  return chunkSection.getBlockState(i & 15, j & 15, k & 15);
               }
            }

            return Blocks.AIR.getDefaultState();
         } catch (Throwable var8) {
            CrashReport crashReport = CrashReport.create(var8, "Getting block state");
            CrashReportSection crashReportSection = crashReport.addElement("Block being got");
            crashReportSection.add("Location", () -> {
               return CrashReportSection.createPositionString(i, j, k);
            });
            throw new CrashException(crashReport);
         }
      }
   }

   public FluidState getFluidState(BlockPos pos) {
      return this.getFluidState(pos.getX(), pos.getY(), pos.getZ());
   }

   public FluidState getFluidState(int x, int y, int i) {
      try {
         if (y >= 0 && y >> 4 < this.sections.length) {
            ChunkSection chunkSection = this.sections[y >> 4];
            if (!ChunkSection.isEmpty(chunkSection)) {
               return chunkSection.getFluidState(x & 15, y & 15, i & 15);
            }
         }

         return Fluids.EMPTY.getDefaultState();
      } catch (Throwable var7) {
         CrashReport crashReport = CrashReport.create(var7, "Getting fluid state");
         CrashReportSection crashReportSection = crashReport.addElement("Block being got");
         crashReportSection.add("Location", () -> {
            return CrashReportSection.createPositionString(x, y, i);
         });
         throw new CrashException(crashReport);
      }
   }

   @Nullable
   public BlockState setBlockState(BlockPos pos, BlockState state, boolean bl) {
      int i = pos.getX() & 15;
      int j = pos.getY();
      int k = pos.getZ() & 15;
      ChunkSection chunkSection = this.sections[j >> 4];
      if (chunkSection == EMPTY_SECTION) {
         if (state.isAir()) {
            return null;
         }

         chunkSection = new ChunkSection(j >> 4 << 4);
         this.sections[j >> 4] = chunkSection;
      }

      boolean bl2 = chunkSection.isEmpty();
      BlockState blockState = chunkSection.setBlockState(i, j & 15, k, state);
      if (blockState == state) {
         return null;
      } else {
         Block block = state.getBlock();
         Block block2 = blockState.getBlock();
         ((Heightmap)this.heightmaps.get(Heightmap.Type.MOTION_BLOCKING)).trackUpdate(i, j, k, state);
         ((Heightmap)this.heightmaps.get(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES)).trackUpdate(i, j, k, state);
         ((Heightmap)this.heightmaps.get(Heightmap.Type.OCEAN_FLOOR)).trackUpdate(i, j, k, state);
         ((Heightmap)this.heightmaps.get(Heightmap.Type.WORLD_SURFACE)).trackUpdate(i, j, k, state);
         boolean bl3 = chunkSection.isEmpty();
         if (bl2 != bl3) {
            this.world.getChunkManager().getLightingProvider().updateSectionStatus(pos, bl3);
         }

         if (!this.world.isClient) {
            blockState.onBlockRemoved(this.world, pos, state, bl);
         } else if (block2 != block && block2 instanceof BlockEntityProvider) {
            this.world.removeBlockEntity(pos);
         }

         if (chunkSection.getBlockState(i, j & 15, k).getBlock() != block) {
            return null;
         } else {
            BlockEntity blockEntity2;
            if (block2 instanceof BlockEntityProvider) {
               blockEntity2 = this.getBlockEntity(pos, WorldChunk.CreationType.CHECK);
               if (blockEntity2 != null) {
                  blockEntity2.resetBlock();
               }
            }

            if (!this.world.isClient) {
               state.onBlockAdded(this.world, pos, blockState, bl);
            }

            if (block instanceof BlockEntityProvider) {
               blockEntity2 = this.getBlockEntity(pos, WorldChunk.CreationType.CHECK);
               if (blockEntity2 == null) {
                  blockEntity2 = ((BlockEntityProvider)block).createBlockEntity(this.world);
                  this.world.setBlockEntity(pos, blockEntity2);
               } else {
                  blockEntity2.resetBlock();
               }
            }

            this.shouldSave = true;
            return blockState;
         }
      }
   }

   @Nullable
   public LightingProvider getLightingProvider() {
      return this.world.getChunkManager().getLightingProvider();
   }

   public void addEntity(Entity entity) {
      this.unsaved = true;
      int i = MathHelper.floor(entity.getX() / 16.0D);
      int j = MathHelper.floor(entity.getZ() / 16.0D);
      if (i != this.pos.x || j != this.pos.z) {
         LOGGER.warn("Wrong location! ({}, {}) should be ({}, {}), {}", i, j, this.pos.x, this.pos.z, entity);
         entity.removed = true;
      }

      int k = MathHelper.floor(entity.getY() / 16.0D);
      if (k < 0) {
         k = 0;
      }

      if (k >= this.entitySections.length) {
         k = this.entitySections.length - 1;
      }

      entity.updateNeeded = true;
      entity.chunkX = this.pos.x;
      entity.chunkY = k;
      entity.chunkZ = this.pos.z;
      this.entitySections[k].add(entity);
   }

   public void setHeightmap(Heightmap.Type type, long[] heightmap) {
      ((Heightmap)this.heightmaps.get(type)).setTo(heightmap);
   }

   public void remove(Entity entity) {
      this.remove(entity, entity.chunkY);
   }

   public void remove(Entity entity, int i) {
      if (i < 0) {
         i = 0;
      }

      if (i >= this.entitySections.length) {
         i = this.entitySections.length - 1;
      }

      this.entitySections[i].remove(entity);
   }

   public int sampleHeightmap(Heightmap.Type type, int x, int z) {
      return ((Heightmap)this.heightmaps.get(type)).get(x & 15, z & 15) - 1;
   }

   @Nullable
   private BlockEntity createBlockEntity(BlockPos blockPos) {
      BlockState blockState = this.getBlockState(blockPos);
      Block block = blockState.getBlock();
      return !block.hasBlockEntity() ? null : ((BlockEntityProvider)block).createBlockEntity(this.world);
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pos) {
      return this.getBlockEntity(pos, WorldChunk.CreationType.CHECK);
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pos, WorldChunk.CreationType creationType) {
      BlockEntity blockEntity = (BlockEntity)this.blockEntities.get(pos);
      if (blockEntity == null) {
         CompoundTag compoundTag = (CompoundTag)this.pendingBlockEntityTags.remove(pos);
         if (compoundTag != null) {
            BlockEntity blockEntity2 = this.loadBlockEntity(pos, compoundTag);
            if (blockEntity2 != null) {
               return blockEntity2;
            }
         }
      }

      if (blockEntity == null) {
         if (creationType == WorldChunk.CreationType.IMMEDIATE) {
            blockEntity = this.createBlockEntity(pos);
            this.world.setBlockEntity(pos, blockEntity);
         }
      } else if (blockEntity.isRemoved()) {
         this.blockEntities.remove(pos);
         return null;
      }

      return blockEntity;
   }

   public void addBlockEntity(BlockEntity blockEntity) {
      this.setBlockEntity(blockEntity.getPos(), blockEntity);
      if (this.loadedToWorld || this.world.isClient()) {
         this.world.setBlockEntity(blockEntity.getPos(), blockEntity);
      }

   }

   public void setBlockEntity(BlockPos pos, BlockEntity blockEntity) {
      if (this.getBlockState(pos).getBlock() instanceof BlockEntityProvider) {
         blockEntity.setLocation(this.world, pos);
         blockEntity.cancelRemoval();
         BlockEntity blockEntity2 = (BlockEntity)this.blockEntities.put(pos.toImmutable(), blockEntity);
         if (blockEntity2 != null && blockEntity2 != blockEntity) {
            blockEntity2.markRemoved();
         }

      }
   }

   public void addPendingBlockEntityTag(CompoundTag compoundTag) {
      this.pendingBlockEntityTags.put(new BlockPos(compoundTag.getInt("x"), compoundTag.getInt("y"), compoundTag.getInt("z")), compoundTag);
   }

   @Nullable
   public CompoundTag method_20598(BlockPos blockPos) {
      BlockEntity blockEntity = this.getBlockEntity(blockPos);
      CompoundTag compoundTag2;
      if (blockEntity != null && !blockEntity.isRemoved()) {
         compoundTag2 = blockEntity.toTag(new CompoundTag());
         compoundTag2.putBoolean("keepPacked", false);
         return compoundTag2;
      } else {
         compoundTag2 = (CompoundTag)this.pendingBlockEntityTags.get(blockPos);
         if (compoundTag2 != null) {
            compoundTag2 = compoundTag2.copy();
            compoundTag2.putBoolean("keepPacked", true);
         }

         return compoundTag2;
      }
   }

   public void removeBlockEntity(BlockPos blockPos) {
      if (this.loadedToWorld || this.world.isClient()) {
         BlockEntity blockEntity = (BlockEntity)this.blockEntities.remove(blockPos);
         if (blockEntity != null) {
            blockEntity.markRemoved();
         }
      }

   }

   public void loadToWorld() {
      if (this.loadToWorldConsumer != null) {
         this.loadToWorldConsumer.accept(this);
         this.loadToWorldConsumer = null;
      }

   }

   public void markDirty() {
      this.shouldSave = true;
   }

   public void getEntities(@Nullable Entity except, Box box, List<Entity> entityList, @Nullable Predicate<? super Entity> predicate) {
      int i = MathHelper.floor((box.y1 - 2.0D) / 16.0D);
      int j = MathHelper.floor((box.y2 + 2.0D) / 16.0D);
      i = MathHelper.clamp(i, 0, this.entitySections.length - 1);
      j = MathHelper.clamp(j, 0, this.entitySections.length - 1);

      label68:
      for(int k = i; k <= j; ++k) {
         if (!this.entitySections[k].isEmpty()) {
            Iterator var8 = this.entitySections[k].iterator();

            while(true) {
               Entity entity;
               do {
                  do {
                     do {
                        if (!var8.hasNext()) {
                           continue label68;
                        }

                        entity = (Entity)var8.next();
                     } while(!entity.getBoundingBox().intersects(box));
                  } while(entity == except);

                  if (predicate == null || predicate.test(entity)) {
                     entityList.add(entity);
                  }
               } while(!(entity instanceof EnderDragonEntity));

               EnderDragonPart[] var10 = ((EnderDragonEntity)entity).getBodyParts();
               int var11 = var10.length;

               for(int var12 = 0; var12 < var11; ++var12) {
                  EnderDragonPart enderDragonPart = var10[var12];
                  if (enderDragonPart != except && enderDragonPart.getBoundingBox().intersects(box) && (predicate == null || predicate.test(enderDragonPart))) {
                     entityList.add(enderDragonPart);
                  }
               }
            }
         }
      }

   }

   public <T extends Entity> void getEntities(@Nullable EntityType<?> type, Box box, List<? super T> list, Predicate<? super T> predicate) {
      int i = MathHelper.floor((box.y1 - 2.0D) / 16.0D);
      int j = MathHelper.floor((box.y2 + 2.0D) / 16.0D);
      i = MathHelper.clamp(i, 0, this.entitySections.length - 1);
      j = MathHelper.clamp(j, 0, this.entitySections.length - 1);

      label33:
      for(int k = i; k <= j; ++k) {
         Iterator var8 = this.entitySections[k].getAllOfType(Entity.class).iterator();

         while(true) {
            Entity entity;
            do {
               if (!var8.hasNext()) {
                  continue label33;
               }

               entity = (Entity)var8.next();
            } while(type != null && entity.getType() != type);

            if (entity.getBoundingBox().intersects(box) && predicate.test(entity)) {
               list.add(entity);
            }
         }
      }

   }

   public <T extends Entity> void getEntities(Class<? extends T> entityClass, Box box, List<T> result, @Nullable Predicate<? super T> predicate) {
      int i = MathHelper.floor((box.y1 - 2.0D) / 16.0D);
      int j = MathHelper.floor((box.y2 + 2.0D) / 16.0D);
      i = MathHelper.clamp(i, 0, this.entitySections.length - 1);
      j = MathHelper.clamp(j, 0, this.entitySections.length - 1);

      label33:
      for(int k = i; k <= j; ++k) {
         Iterator var8 = this.entitySections[k].getAllOfType(entityClass).iterator();

         while(true) {
            Entity entity;
            do {
               do {
                  if (!var8.hasNext()) {
                     continue label33;
                  }

                  entity = (Entity)var8.next();
               } while(!entity.getBoundingBox().intersects(box));
            } while(predicate != null && !predicate.test(entity));

            result.add(entity);
         }
      }

   }

   public boolean isEmpty() {
      return false;
   }

   public ChunkPos getPos() {
      return this.pos;
   }

   @Environment(EnvType.CLIENT)
   public void loadFromPacket(@Nullable BiomeArray biomeArray, PacketByteBuf packetByteBuf, CompoundTag compoundTag, int i) {
      boolean bl = biomeArray != null;
      Predicate<BlockPos> predicate = bl ? (blockPos) -> {
         return true;
      } : (blockPos) -> {
         return (i & 1 << (blockPos.getY() >> 4)) != 0;
      };
      Stream var10000 = Sets.newHashSet(this.blockEntities.keySet()).stream().filter(predicate);
      World var10001 = this.world;
      var10000.forEach(var10001::removeBlockEntity);

      for(int j = 0; j < this.sections.length; ++j) {
         ChunkSection chunkSection = this.sections[j];
         if ((i & 1 << j) == 0) {
            if (bl && chunkSection != EMPTY_SECTION) {
               this.sections[j] = EMPTY_SECTION;
            }
         } else {
            if (chunkSection == EMPTY_SECTION) {
               chunkSection = new ChunkSection(j << 4);
               this.sections[j] = chunkSection;
            }

            chunkSection.fromPacket(packetByteBuf);
         }
      }

      if (biomeArray != null) {
         this.biomeArray = biomeArray;
      }

      Heightmap.Type[] var12 = Heightmap.Type.values();
      int var14 = var12.length;

      for(int var9 = 0; var9 < var14; ++var9) {
         Heightmap.Type type = var12[var9];
         String string = type.getName();
         if (compoundTag.contains(string, 12)) {
            this.setHeightmap(type, compoundTag.getLongArray(string));
         }
      }

      Iterator var13 = this.blockEntities.values().iterator();

      while(var13.hasNext()) {
         BlockEntity blockEntity = (BlockEntity)var13.next();
         blockEntity.resetBlock();
      }

   }

   public BiomeArray getBiomeArray() {
      return this.biomeArray;
   }

   public void setLoadedToWorld(boolean bl) {
      this.loadedToWorld = bl;
   }

   public World getWorld() {
      return this.world;
   }

   public Collection<Entry<Heightmap.Type, Heightmap>> getHeightmaps() {
      return Collections.unmodifiableSet(this.heightmaps.entrySet());
   }

   public Map<BlockPos, BlockEntity> getBlockEntities() {
      return this.blockEntities;
   }

   public TypeFilterableList<Entity>[] getEntitySectionArray() {
      return this.entitySections;
   }

   public CompoundTag getBlockEntityTagAt(BlockPos pos) {
      return (CompoundTag)this.pendingBlockEntityTags.get(pos);
   }

   public Stream<BlockPos> getLightSourcesStream() {
      return StreamSupport.stream(BlockPos.iterate(this.pos.getStartX(), 0, this.pos.getStartZ(), this.pos.getEndX(), 255, this.pos.getEndZ()).spliterator(), false).filter((blockPos) -> {
         return this.getBlockState(blockPos).getLuminance() != 0;
      });
   }

   public TickScheduler<Block> getBlockTickScheduler() {
      return this.blockTickScheduler;
   }

   public TickScheduler<Fluid> getFluidTickScheduler() {
      return this.fluidTickScheduler;
   }

   public void setShouldSave(boolean shouldSave) {
      this.shouldSave = shouldSave;
   }

   public boolean needsSaving() {
      return this.shouldSave || this.unsaved && this.world.getTime() != this.lastSaveTime;
   }

   public void setUnsaved(boolean unsaved) {
      this.unsaved = unsaved;
   }

   public void setLastSaveTime(long lastSaveTime) {
      this.lastSaveTime = lastSaveTime;
   }

   @Nullable
   public StructureStart getStructureStart(String structure) {
      return (StructureStart)this.structureStarts.get(structure);
   }

   public void setStructureStart(String structure, StructureStart start) {
      this.structureStarts.put(structure, start);
   }

   public Map<String, StructureStart> getStructureStarts() {
      return this.structureStarts;
   }

   public void setStructureStarts(Map<String, StructureStart> map) {
      this.structureStarts.clear();
      this.structureStarts.putAll(map);
   }

   public LongSet getStructureReferences(String structure) {
      return (LongSet)this.structureReferences.computeIfAbsent(structure, (string) -> {
         return new LongOpenHashSet();
      });
   }

   public void addStructureReference(String structure, long reference) {
      ((LongSet)this.structureReferences.computeIfAbsent(structure, (string) -> {
         return new LongOpenHashSet();
      })).add(reference);
   }

   public Map<String, LongSet> getStructureReferences() {
      return this.structureReferences;
   }

   public void setStructureReferences(Map<String, LongSet> structureReferences) {
      this.structureReferences.clear();
      this.structureReferences.putAll(structureReferences);
   }

   public long getInhabitedTime() {
      return this.inhabitedTime;
   }

   public void setInhabitedTime(long inhabitedTime) {
      this.inhabitedTime = inhabitedTime;
   }

   public void runPostProcessing() {
      ChunkPos chunkPos = this.getPos();

      for(int i = 0; i < this.postProcessingLists.length; ++i) {
         if (this.postProcessingLists[i] != null) {
            ShortListIterator var3 = this.postProcessingLists[i].iterator();

            while(var3.hasNext()) {
               Short var4 = (Short)var3.next();
               BlockPos blockPos = ProtoChunk.joinBlockPos(var4, i, chunkPos);
               BlockState blockState = this.getBlockState(blockPos);
               BlockState blockState2 = Block.getRenderingState(blockState, this.world, blockPos);
               this.world.setBlockState(blockPos, blockState2, 20);
            }

            this.postProcessingLists[i].clear();
         }
      }

      this.disableTickSchedulers();
      Iterator var8 = Sets.newHashSet(this.pendingBlockEntityTags.keySet()).iterator();

      while(var8.hasNext()) {
         BlockPos blockPos2 = (BlockPos)var8.next();
         this.getBlockEntity(blockPos2);
      }

      this.pendingBlockEntityTags.clear();
      this.upgradeData.upgrade(this);
   }

   @Nullable
   private BlockEntity loadBlockEntity(BlockPos pos, CompoundTag compoundTag) {
      BlockEntity blockEntity3;
      if ("DUMMY".equals(compoundTag.getString("id"))) {
         Block block = this.getBlockState(pos).getBlock();
         if (block instanceof BlockEntityProvider) {
            blockEntity3 = ((BlockEntityProvider)block).createBlockEntity(this.world);
         } else {
            blockEntity3 = null;
            LOGGER.warn("Tried to load a DUMMY block entity @ {} but found not block entity block {} at location", pos, this.getBlockState(pos));
         }
      } else {
         blockEntity3 = BlockEntity.createFromTag(compoundTag);
      }

      if (blockEntity3 != null) {
         blockEntity3.setLocation(this.world, pos);
         this.addBlockEntity(blockEntity3);
      } else {
         LOGGER.warn("Tried to load a block entity for block {} but failed at location {}", this.getBlockState(pos), pos);
      }

      return blockEntity3;
   }

   public UpgradeData getUpgradeData() {
      return this.upgradeData;
   }

   public ShortList[] getPostProcessingLists() {
      return this.postProcessingLists;
   }

   public void disableTickSchedulers() {
      if (this.blockTickScheduler instanceof ChunkTickScheduler) {
         ((ChunkTickScheduler)this.blockTickScheduler).tick(this.world.getBlockTickScheduler(), (blockPos) -> {
            return this.getBlockState(blockPos).getBlock();
         });
         this.blockTickScheduler = DummyClientTickScheduler.get();
      } else if (this.blockTickScheduler instanceof SimpleTickScheduler) {
         this.world.getBlockTickScheduler().scheduleAll(((SimpleTickScheduler)this.blockTickScheduler).stream());
         this.blockTickScheduler = DummyClientTickScheduler.get();
      }

      if (this.fluidTickScheduler instanceof ChunkTickScheduler) {
         ((ChunkTickScheduler)this.fluidTickScheduler).tick(this.world.getFluidTickScheduler(), (blockPos) -> {
            return this.getFluidState(blockPos).getFluid();
         });
         this.fluidTickScheduler = DummyClientTickScheduler.get();
      } else if (this.fluidTickScheduler instanceof SimpleTickScheduler) {
         this.world.getFluidTickScheduler().scheduleAll(((SimpleTickScheduler)this.fluidTickScheduler).stream());
         this.fluidTickScheduler = DummyClientTickScheduler.get();
      }

   }

   public void enableTickSchedulers(ServerWorld world) {
      if (this.blockTickScheduler == DummyClientTickScheduler.get()) {
         this.blockTickScheduler = new SimpleTickScheduler(Registry.BLOCK::getId, world.getBlockTickScheduler().getScheduledTicksInChunk(this.pos, true, false));
         this.setShouldSave(true);
      }

      if (this.fluidTickScheduler == DummyClientTickScheduler.get()) {
         this.fluidTickScheduler = new SimpleTickScheduler(Registry.FLUID::getId, world.getFluidTickScheduler().getScheduledTicksInChunk(this.pos, true, false));
         this.setShouldSave(true);
      }

   }

   public ChunkStatus getStatus() {
      return ChunkStatus.FULL;
   }

   public ChunkHolder.LevelType getLevelType() {
      return this.levelTypeProvider == null ? ChunkHolder.LevelType.BORDER : (ChunkHolder.LevelType)this.levelTypeProvider.get();
   }

   public void setLevelTypeProvider(Supplier<ChunkHolder.LevelType> levelTypeProvider) {
      this.levelTypeProvider = levelTypeProvider;
   }

   public boolean isLightOn() {
      return this.isLightOn;
   }

   public void setLightOn(boolean lightOn) {
      this.isLightOn = lightOn;
      this.setShouldSave(true);
   }

   public static enum CreationType {
      IMMEDIATE,
      QUEUED,
      CHECK;
   }
}
