package net.minecraft.block.pattern;

import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Iterator;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldView;

public class BlockPattern {
   private final Predicate<CachedBlockPosition>[][][] pattern;
   private final int depth;
   private final int height;
   private final int width;

   public BlockPattern(Predicate<CachedBlockPosition>[][][] pattern) {
      this.pattern = pattern;
      this.depth = pattern.length;
      if (this.depth > 0) {
         this.height = pattern[0].length;
         if (this.height > 0) {
            this.width = pattern[0][0].length;
         } else {
            this.width = 0;
         }
      } else {
         this.height = 0;
         this.width = 0;
      }

   }

   public int getDepth() {
      return this.depth;
   }

   public int getHeight() {
      return this.height;
   }

   public int getWidth() {
      return this.width;
   }

   @Nullable
   private BlockPattern.Result testTransform(BlockPos frontTopLeft, Direction forwards, Direction up, LoadingCache<BlockPos, CachedBlockPosition> loadingCache) {
      for(int i = 0; i < this.width; ++i) {
         for(int j = 0; j < this.height; ++j) {
            for(int k = 0; k < this.depth; ++k) {
               if (!this.pattern[k][j][i].test(loadingCache.getUnchecked(translate(frontTopLeft, forwards, up, i, j, k)))) {
                  return null;
               }
            }
         }
      }

      return new BlockPattern.Result(frontTopLeft, forwards, up, loadingCache, this.width, this.height, this.depth);
   }

   @Nullable
   public BlockPattern.Result searchAround(WorldView worldView, BlockPos blockPos) {
      LoadingCache<BlockPos, CachedBlockPosition> loadingCache = makeCache(worldView, false);
      int i = Math.max(Math.max(this.width, this.height), this.depth);
      Iterator var5 = BlockPos.iterate(blockPos, blockPos.add(i - 1, i - 1, i - 1)).iterator();

      while(var5.hasNext()) {
         BlockPos blockPos2 = (BlockPos)var5.next();
         Direction[] var7 = Direction.values();
         int var8 = var7.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            Direction direction = var7[var9];
            Direction[] var11 = Direction.values();
            int var12 = var11.length;

            for(int var13 = 0; var13 < var12; ++var13) {
               Direction direction2 = var11[var13];
               if (direction2 != direction && direction2 != direction.getOpposite()) {
                  BlockPattern.Result result = this.testTransform(blockPos2, direction, direction2, loadingCache);
                  if (result != null) {
                     return result;
                  }
               }
            }
         }
      }

      return null;
   }

   public static LoadingCache<BlockPos, CachedBlockPosition> makeCache(WorldView worldView, boolean bl) {
      return CacheBuilder.newBuilder().build(new BlockPattern.BlockStateCacheLoader(worldView, bl));
   }

   protected static BlockPos translate(BlockPos pos, Direction forwards, Direction up, int offsetLeft, int offsetDown, int offsetForwards) {
      if (forwards != up && forwards != up.getOpposite()) {
         Vec3i vec3i = new Vec3i(forwards.getOffsetX(), forwards.getOffsetY(), forwards.getOffsetZ());
         Vec3i vec3i2 = new Vec3i(up.getOffsetX(), up.getOffsetY(), up.getOffsetZ());
         Vec3i vec3i3 = vec3i.crossProduct(vec3i2);
         return pos.add(vec3i2.getX() * -offsetDown + vec3i3.getX() * offsetLeft + vec3i.getX() * offsetForwards, vec3i2.getY() * -offsetDown + vec3i3.getY() * offsetLeft + vec3i.getY() * offsetForwards, vec3i2.getZ() * -offsetDown + vec3i3.getZ() * offsetLeft + vec3i.getZ() * offsetForwards);
      } else {
         throw new IllegalArgumentException("Invalid forwards & up combination");
      }
   }

   public static class TeleportTarget {
      public final Vec3d pos;
      public final Vec3d velocity;
      public final int yaw;

      public TeleportTarget(Vec3d pos, Vec3d velocity, int yaw) {
         this.pos = pos;
         this.velocity = velocity;
         this.yaw = yaw;
      }
   }

   public static class Result {
      private final BlockPos frontTopLeft;
      private final Direction forwards;
      private final Direction up;
      private final LoadingCache<BlockPos, CachedBlockPosition> cache;
      private final int width;
      private final int height;
      private final int depth;

      public Result(BlockPos frontTopLeft, Direction forwards, Direction up, LoadingCache<BlockPos, CachedBlockPosition> loadingCache, int width, int height, int depth) {
         this.frontTopLeft = frontTopLeft;
         this.forwards = forwards;
         this.up = up;
         this.cache = loadingCache;
         this.width = width;
         this.height = height;
         this.depth = depth;
      }

      public BlockPos getFrontTopLeft() {
         return this.frontTopLeft;
      }

      public Direction getForwards() {
         return this.forwards;
      }

      public Direction getUp() {
         return this.up;
      }

      public int getWidth() {
         return this.width;
      }

      public int getHeight() {
         return this.height;
      }

      public CachedBlockPosition translate(int i, int j, int k) {
         return (CachedBlockPosition)this.cache.getUnchecked(BlockPattern.translate(this.frontTopLeft, this.getForwards(), this.getUp(), i, j, k));
      }

      public String toString() {
         return MoreObjects.toStringHelper(this).add("up", this.up).add("forwards", this.forwards).add("frontTopLeft", this.frontTopLeft).toString();
      }

      public BlockPattern.TeleportTarget getTeleportTarget(Direction direction, BlockPos blockPos, double d, Vec3d vec3d, double e) {
         Direction direction2 = this.getForwards();
         Direction direction3 = direction2.rotateYClockwise();
         double f = (double)(this.getFrontTopLeft().getY() + 1) - d * (double)this.getHeight();
         double m;
         double n;
         if (direction3 == Direction.NORTH) {
            m = (double)blockPos.getX() + 0.5D;
            n = (double)(this.getFrontTopLeft().getZ() + 1) - (1.0D - e) * (double)this.getWidth();
         } else if (direction3 == Direction.SOUTH) {
            m = (double)blockPos.getX() + 0.5D;
            n = (double)this.getFrontTopLeft().getZ() + (1.0D - e) * (double)this.getWidth();
         } else if (direction3 == Direction.WEST) {
            m = (double)(this.getFrontTopLeft().getX() + 1) - (1.0D - e) * (double)this.getWidth();
            n = (double)blockPos.getZ() + 0.5D;
         } else {
            m = (double)this.getFrontTopLeft().getX() + (1.0D - e) * (double)this.getWidth();
            n = (double)blockPos.getZ() + 0.5D;
         }

         double s;
         double v;
         if (direction2.getOpposite() == direction) {
            s = vec3d.x;
            v = vec3d.z;
         } else if (direction2.getOpposite() == direction.getOpposite()) {
            s = -vec3d.x;
            v = -vec3d.z;
         } else if (direction2.getOpposite() == direction.rotateYClockwise()) {
            s = -vec3d.z;
            v = vec3d.x;
         } else {
            s = vec3d.z;
            v = -vec3d.x;
         }

         int w = (direction2.getHorizontal() - direction.getOpposite().getHorizontal()) * 90;
         return new BlockPattern.TeleportTarget(new Vec3d(m, f, n), new Vec3d(s, vec3d.y, v), w);
      }
   }

   static class BlockStateCacheLoader extends CacheLoader<BlockPos, CachedBlockPosition> {
      private final WorldView world;
      private final boolean forceLoad;

      public BlockStateCacheLoader(WorldView worldView, boolean bl) {
         this.world = worldView;
         this.forceLoad = bl;
      }

      public CachedBlockPosition load(BlockPos blockPos) throws Exception {
         return new CachedBlockPosition(this.world, blockPos, this.forceLoad);
      }
   }
}
