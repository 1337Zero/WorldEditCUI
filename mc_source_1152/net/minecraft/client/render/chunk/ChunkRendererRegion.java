package net.minecraft.client.render.chunk;

import java.util.Iterator;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.level.ColorResolver;

@Environment(EnvType.CLIENT)
public class ChunkRendererRegion implements BlockRenderView {
   protected final int chunkXOffset;
   protected final int chunkZOffset;
   protected final BlockPos offset;
   protected final int xSize;
   protected final int ySize;
   protected final int zSize;
   protected final WorldChunk[][] chunks;
   protected final BlockState[] blockStates;
   protected final FluidState[] fluidStates;
   protected final World world;

   @Nullable
   public static ChunkRendererRegion create(World world, BlockPos startPos, BlockPos endPos, int chunkRadius) {
      int i = startPos.getX() - chunkRadius >> 4;
      int j = startPos.getZ() - chunkRadius >> 4;
      int k = endPos.getX() + chunkRadius >> 4;
      int l = endPos.getZ() + chunkRadius >> 4;
      WorldChunk[][] worldChunks = new WorldChunk[k - i + 1][l - j + 1];

      int o;
      for(int m = i; m <= k; ++m) {
         for(o = j; o <= l; ++o) {
            worldChunks[m - i][o - j] = world.getChunk(m, o);
         }
      }

      boolean bl = true;

      for(o = startPos.getX() >> 4; o <= endPos.getX() >> 4; ++o) {
         for(int p = startPos.getZ() >> 4; p <= endPos.getZ() >> 4; ++p) {
            WorldChunk worldChunk = worldChunks[o - i][p - j];
            if (!worldChunk.method_12228(startPos.getY(), endPos.getY())) {
               bl = false;
            }
         }
      }

      if (bl) {
         return null;
      } else {
         int q = true;
         BlockPos blockPos = startPos.add(-1, -1, -1);
         BlockPos blockPos2 = endPos.add(1, 1, 1);
         return new ChunkRendererRegion(world, i, j, worldChunks, blockPos, blockPos2);
      }
   }

   public ChunkRendererRegion(World world, int chunkX, int chunkZ, WorldChunk[][] chunks, BlockPos startPos, BlockPos endPos) {
      this.world = world;
      this.chunkXOffset = chunkX;
      this.chunkZOffset = chunkZ;
      this.chunks = chunks;
      this.offset = startPos;
      this.xSize = endPos.getX() - startPos.getX() + 1;
      this.ySize = endPos.getY() - startPos.getY() + 1;
      this.zSize = endPos.getZ() - startPos.getZ() + 1;
      this.blockStates = new BlockState[this.xSize * this.ySize * this.zSize];
      this.fluidStates = new FluidState[this.xSize * this.ySize * this.zSize];

      BlockPos blockPos;
      WorldChunk worldChunk;
      int k;
      for(Iterator var7 = BlockPos.iterate(startPos, endPos).iterator(); var7.hasNext(); this.fluidStates[k] = worldChunk.getFluidState(blockPos)) {
         blockPos = (BlockPos)var7.next();
         int i = (blockPos.getX() >> 4) - chunkX;
         int j = (blockPos.getZ() >> 4) - chunkZ;
         worldChunk = chunks[i][j];
         k = this.getIndex(blockPos);
         this.blockStates[k] = worldChunk.getBlockState(blockPos);
      }

   }

   protected final int getIndex(BlockPos pos) {
      return this.getIndex(pos.getX(), pos.getY(), pos.getZ());
   }

   protected int getIndex(int x, int y, int z) {
      int i = x - this.offset.getX();
      int j = y - this.offset.getY();
      int k = z - this.offset.getZ();
      return k * this.xSize * this.ySize + j * this.xSize + i;
   }

   public BlockState getBlockState(BlockPos pos) {
      return this.blockStates[this.getIndex(pos)];
   }

   public FluidState getFluidState(BlockPos pos) {
      return this.fluidStates[this.getIndex(pos)];
   }

   public LightingProvider getLightingProvider() {
      return this.world.getLightingProvider();
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pos) {
      return this.getBlockEntity(pos, WorldChunk.CreationType.IMMEDIATE);
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos blockPos, WorldChunk.CreationType creationType) {
      int i = (blockPos.getX() >> 4) - this.chunkXOffset;
      int j = (blockPos.getZ() >> 4) - this.chunkZOffset;
      return this.chunks[i][j].getBlockEntity(blockPos, creationType);
   }

   public int getColor(BlockPos pos, ColorResolver colorResolver) {
      return this.world.getColor(pos, colorResolver);
   }
}
