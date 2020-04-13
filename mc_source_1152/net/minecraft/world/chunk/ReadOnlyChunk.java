package net.minecraft.world.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.BitSet;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkTickScheduler;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.GenerationStep;

public class ReadOnlyChunk extends ProtoChunk {
   private final WorldChunk wrapped;

   public ReadOnlyChunk(WorldChunk wrapped) {
      super(wrapped.getPos(), UpgradeData.NO_UPGRADE_DATA);
      this.wrapped = wrapped;
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pos) {
      return this.wrapped.getBlockEntity(pos);
   }

   @Nullable
   public BlockState getBlockState(BlockPos pos) {
      return this.wrapped.getBlockState(pos);
   }

   public FluidState getFluidState(BlockPos pos) {
      return this.wrapped.getFluidState(pos);
   }

   public int getMaxLightLevel() {
      return this.wrapped.getMaxLightLevel();
   }

   @Nullable
   public BlockState setBlockState(BlockPos pos, BlockState state, boolean bl) {
      return null;
   }

   public void setBlockEntity(BlockPos pos, BlockEntity blockEntity) {
   }

   public void addEntity(Entity entity) {
   }

   public void setStatus(ChunkStatus chunkStatus) {
   }

   public ChunkSection[] getSectionArray() {
      return this.wrapped.getSectionArray();
   }

   @Nullable
   public LightingProvider getLightingProvider() {
      return this.wrapped.getLightingProvider();
   }

   public void setHeightmap(Heightmap.Type type, long[] heightmap) {
   }

   private Heightmap.Type transformHeightmapType(Heightmap.Type type) {
      if (type == Heightmap.Type.WORLD_SURFACE_WG) {
         return Heightmap.Type.WORLD_SURFACE;
      } else {
         return type == Heightmap.Type.OCEAN_FLOOR_WG ? Heightmap.Type.OCEAN_FLOOR : type;
      }
   }

   public int sampleHeightmap(Heightmap.Type type, int x, int z) {
      return this.wrapped.sampleHeightmap(this.transformHeightmapType(type), x, z);
   }

   public ChunkPos getPos() {
      return this.wrapped.getPos();
   }

   public void setLastSaveTime(long lastSaveTime) {
   }

   @Nullable
   public StructureStart getStructureStart(String structure) {
      return this.wrapped.getStructureStart(structure);
   }

   public void setStructureStart(String structure, StructureStart start) {
   }

   public Map<String, StructureStart> getStructureStarts() {
      return this.wrapped.getStructureStarts();
   }

   public void setStructureStarts(Map<String, StructureStart> map) {
   }

   public LongSet getStructureReferences(String structure) {
      return this.wrapped.getStructureReferences(structure);
   }

   public void addStructureReference(String structure, long reference) {
   }

   public Map<String, LongSet> getStructureReferences() {
      return this.wrapped.getStructureReferences();
   }

   public void setStructureReferences(Map<String, LongSet> structureReferences) {
   }

   public BiomeArray getBiomeArray() {
      return this.wrapped.getBiomeArray();
   }

   public void setShouldSave(boolean shouldSave) {
   }

   public boolean needsSaving() {
      return false;
   }

   public ChunkStatus getStatus() {
      return this.wrapped.getStatus();
   }

   public void removeBlockEntity(BlockPos blockPos) {
   }

   public void markBlockForPostProcessing(BlockPos blockPos) {
   }

   public void addPendingBlockEntityTag(CompoundTag compoundTag) {
   }

   @Nullable
   public CompoundTag getBlockEntityTagAt(BlockPos pos) {
      return this.wrapped.getBlockEntityTagAt(pos);
   }

   @Nullable
   public CompoundTag method_20598(BlockPos blockPos) {
      return this.wrapped.method_20598(blockPos);
   }

   public void method_22405(BiomeArray biomeArray) {
   }

   public Stream<BlockPos> getLightSourcesStream() {
      return this.wrapped.getLightSourcesStream();
   }

   public ChunkTickScheduler<Block> getBlockTickScheduler() {
      return new ChunkTickScheduler((block) -> {
         return block.getDefaultState().isAir();
      }, this.getPos());
   }

   public ChunkTickScheduler<Fluid> getFluidTickScheduler() {
      return new ChunkTickScheduler((fluid) -> {
         return fluid == Fluids.EMPTY;
      }, this.getPos());
   }

   public BitSet getCarvingMask(GenerationStep.Carver carver) {
      return this.wrapped.getCarvingMask(carver);
   }

   public WorldChunk getWrappedChunk() {
      return this.wrapped;
   }

   public boolean isLightOn() {
      return this.wrapped.isLightOn();
   }

   public void setLightOn(boolean lightOn) {
      this.wrapped.setLightOn(lightOn);
   }
}
