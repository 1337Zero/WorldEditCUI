package net.minecraft.entity.ai.pathing;

import javax.annotation.Nullable;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.ChunkCache;

public class AmphibiousPathNodeMaker extends LandPathNodeMaker {
   private float field_65;
   private float field_64;

   public void init(ChunkCache chunkCache, MobEntity mobEntity) {
      super.init(chunkCache, mobEntity);
      mobEntity.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
      this.field_65 = mobEntity.getPathfindingPenalty(PathNodeType.WALKABLE);
      mobEntity.setPathfindingPenalty(PathNodeType.WALKABLE, 6.0F);
      this.field_64 = mobEntity.getPathfindingPenalty(PathNodeType.WATER_BORDER);
      mobEntity.setPathfindingPenalty(PathNodeType.WATER_BORDER, 4.0F);
   }

   public void clear() {
      this.entity.setPathfindingPenalty(PathNodeType.WALKABLE, this.field_65);
      this.entity.setPathfindingPenalty(PathNodeType.WATER_BORDER, this.field_64);
      super.clear();
   }

   public PathNode getStart() {
      return this.getNode(MathHelper.floor(this.entity.getBoundingBox().x1), MathHelper.floor(this.entity.getBoundingBox().y1 + 0.5D), MathHelper.floor(this.entity.getBoundingBox().z1));
   }

   public TargetPathNode getNode(double x, double y, double z) {
      return new TargetPathNode(this.getNode(MathHelper.floor(x), MathHelper.floor(y + 0.5D), MathHelper.floor(z)));
   }

   public int getSuccessors(PathNode[] successors, PathNode node) {
      int i = 0;
      int j = true;
      BlockPos blockPos = new BlockPos(node.x, node.y, node.z);
      double d = this.method_66(blockPos);
      PathNode pathNode = this.method_65(node.x, node.y, node.z + 1, 1, d);
      PathNode pathNode2 = this.method_65(node.x - 1, node.y, node.z, 1, d);
      PathNode pathNode3 = this.method_65(node.x + 1, node.y, node.z, 1, d);
      PathNode pathNode4 = this.method_65(node.x, node.y, node.z - 1, 1, d);
      PathNode pathNode5 = this.method_65(node.x, node.y + 1, node.z, 0, d);
      PathNode pathNode6 = this.method_65(node.x, node.y - 1, node.z, 1, d);
      if (pathNode != null && !pathNode.visited) {
         successors[i++] = pathNode;
      }

      if (pathNode2 != null && !pathNode2.visited) {
         successors[i++] = pathNode2;
      }

      if (pathNode3 != null && !pathNode3.visited) {
         successors[i++] = pathNode3;
      }

      if (pathNode4 != null && !pathNode4.visited) {
         successors[i++] = pathNode4;
      }

      if (pathNode5 != null && !pathNode5.visited) {
         successors[i++] = pathNode5;
      }

      if (pathNode6 != null && !pathNode6.visited) {
         successors[i++] = pathNode6;
      }

      boolean bl = pathNode4 == null || pathNode4.type == PathNodeType.OPEN || pathNode4.penalty != 0.0F;
      boolean bl2 = pathNode == null || pathNode.type == PathNodeType.OPEN || pathNode.penalty != 0.0F;
      boolean bl3 = pathNode3 == null || pathNode3.type == PathNodeType.OPEN || pathNode3.penalty != 0.0F;
      boolean bl4 = pathNode2 == null || pathNode2.type == PathNodeType.OPEN || pathNode2.penalty != 0.0F;
      PathNode pathNode10;
      if (bl && bl4) {
         pathNode10 = this.method_65(node.x - 1, node.y, node.z - 1, 1, d);
         if (pathNode10 != null && !pathNode10.visited) {
            successors[i++] = pathNode10;
         }
      }

      if (bl && bl3) {
         pathNode10 = this.method_65(node.x + 1, node.y, node.z - 1, 1, d);
         if (pathNode10 != null && !pathNode10.visited) {
            successors[i++] = pathNode10;
         }
      }

      if (bl2 && bl4) {
         pathNode10 = this.method_65(node.x - 1, node.y, node.z + 1, 1, d);
         if (pathNode10 != null && !pathNode10.visited) {
            successors[i++] = pathNode10;
         }
      }

      if (bl2 && bl3) {
         pathNode10 = this.method_65(node.x + 1, node.y, node.z + 1, 1, d);
         if (pathNode10 != null && !pathNode10.visited) {
            successors[i++] = pathNode10;
         }
      }

      return i;
   }

   private double method_66(BlockPos blockPos) {
      if (!this.entity.isTouchingWater()) {
         BlockPos blockPos2 = blockPos.down();
         VoxelShape voxelShape = this.field_20622.getBlockState(blockPos2).getCollisionShape(this.field_20622, blockPos2);
         return (double)blockPos2.getY() + (voxelShape.isEmpty() ? 0.0D : voxelShape.getMaximum(Direction.Axis.Y));
      } else {
         return (double)blockPos.getY() + 0.5D;
      }
   }

   @Nullable
   private PathNode method_65(int i, int j, int k, int l, double d) {
      PathNode pathNode = null;
      BlockPos blockPos = new BlockPos(i, j, k);
      double e = this.method_66(blockPos);
      if (e - d > 1.125D) {
         return null;
      } else {
         PathNodeType pathNodeType = this.getNodeType(this.field_20622, i, j, k, this.entity, this.field_31, this.field_30, this.field_28, false, false);
         float f = this.entity.getPathfindingPenalty(pathNodeType);
         double g = (double)this.entity.getWidth() / 2.0D;
         if (f >= 0.0F) {
            pathNode = this.getNode(i, j, k);
            pathNode.type = pathNodeType;
            pathNode.penalty = Math.max(pathNode.penalty, f);
         }

         if (pathNodeType != PathNodeType.WATER && pathNodeType != PathNodeType.WALKABLE) {
            if (pathNode == null && l > 0 && pathNodeType != PathNodeType.FENCE && pathNodeType != PathNodeType.TRAPDOOR) {
               pathNode = this.method_65(i, j + 1, k, l - 1, d);
            }

            if (pathNodeType == PathNodeType.OPEN) {
               Box box = new Box((double)i - g + 0.5D, (double)j + 0.001D, (double)k - g + 0.5D, (double)i + g + 0.5D, (double)((float)j + this.entity.getHeight()), (double)k + g + 0.5D);
               if (!this.entity.world.doesNotCollide(this.entity, box)) {
                  return null;
               }

               PathNodeType pathNodeType2 = this.getNodeType(this.field_20622, i, j - 1, k, this.entity, this.field_31, this.field_30, this.field_28, false, false);
               if (pathNodeType2 == PathNodeType.BLOCKED) {
                  pathNode = this.getNode(i, j, k);
                  pathNode.type = PathNodeType.WALKABLE;
                  pathNode.penalty = Math.max(pathNode.penalty, f);
                  return pathNode;
               }

               if (pathNodeType2 == PathNodeType.WATER) {
                  pathNode = this.getNode(i, j, k);
                  pathNode.type = PathNodeType.WATER;
                  pathNode.penalty = Math.max(pathNode.penalty, f);
                  return pathNode;
               }

               int var17 = 0;

               while(j > 0 && pathNodeType == PathNodeType.OPEN) {
                  --j;
                  if (var17++ >= this.entity.getSafeFallDistance()) {
                     return null;
                  }

                  pathNodeType = this.getNodeType(this.field_20622, i, j, k, this.entity, this.field_31, this.field_30, this.field_28, false, false);
                  f = this.entity.getPathfindingPenalty(pathNodeType);
                  if (pathNodeType != PathNodeType.OPEN && f >= 0.0F) {
                     pathNode = this.getNode(i, j, k);
                     pathNode.type = pathNodeType;
                     pathNode.penalty = Math.max(pathNode.penalty, f);
                     break;
                  }

                  if (f < 0.0F) {
                     return null;
                  }
               }
            }

            return pathNode;
         } else {
            if (j < this.entity.world.getSeaLevel() - 10 && pathNode != null) {
               ++pathNode.penalty;
            }

            return pathNode;
         }
      }
   }

   protected PathNodeType adjustNodeType(BlockView world, boolean canOpenDoors, boolean canEnterOpenDoors, BlockPos pos, PathNodeType type) {
      if (type == PathNodeType.RAIL && !(world.getBlockState(pos).getBlock() instanceof AbstractRailBlock) && !(world.getBlockState(pos.down()).getBlock() instanceof AbstractRailBlock)) {
         type = PathNodeType.FENCE;
      }

      if (type == PathNodeType.DOOR_OPEN || type == PathNodeType.DOOR_WOOD_CLOSED || type == PathNodeType.DOOR_IRON_CLOSED) {
         type = PathNodeType.BLOCKED;
      }

      if (type == PathNodeType.LEAVES) {
         type = PathNodeType.BLOCKED;
      }

      return type;
   }

   public PathNodeType getNodeType(BlockView world, int x, int y, int z) {
      PathNodeType pathNodeType = getBasicPathNodeType(world, x, y, z);
      if (pathNodeType == PathNodeType.WATER) {
         Direction[] var11 = Direction.values();
         int var12 = var11.length;

         for(int var8 = 0; var8 < var12; ++var8) {
            Direction direction = var11[var8];
            PathNodeType pathNodeType2 = getBasicPathNodeType(world, x + direction.getOffsetX(), y + direction.getOffsetY(), z + direction.getOffsetZ());
            if (pathNodeType2 == PathNodeType.BLOCKED) {
               return PathNodeType.WATER_BORDER;
            }
         }

         return PathNodeType.WATER;
      } else {
         if (pathNodeType == PathNodeType.OPEN && y >= 1) {
            Block block = world.getBlockState(new BlockPos(x, y - 1, z)).getBlock();
            PathNodeType pathNodeType3 = getBasicPathNodeType(world, x, y - 1, z);
            if (pathNodeType3 != PathNodeType.WALKABLE && pathNodeType3 != PathNodeType.OPEN && pathNodeType3 != PathNodeType.LAVA) {
               pathNodeType = PathNodeType.WALKABLE;
            } else {
               pathNodeType = PathNodeType.OPEN;
            }

            if (pathNodeType3 == PathNodeType.DAMAGE_FIRE || block == Blocks.MAGMA_BLOCK || block == Blocks.CAMPFIRE) {
               pathNodeType = PathNodeType.DAMAGE_FIRE;
            }

            if (pathNodeType3 == PathNodeType.DAMAGE_CACTUS) {
               pathNodeType = PathNodeType.DAMAGE_CACTUS;
            }

            if (pathNodeType3 == PathNodeType.DAMAGE_OTHER) {
               pathNodeType = PathNodeType.DAMAGE_OTHER;
            }
         }

         if (pathNodeType == PathNodeType.WALKABLE) {
            pathNodeType = method_59(world, x, y, z, pathNodeType);
         }

         return pathNodeType;
      }
   }
}
