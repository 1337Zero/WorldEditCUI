package net.minecraft.entity.ai;

import java.util.Random;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntityWithAi;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class TargetFinder {
   @Nullable
   public static Vec3d findTarget(MobEntityWithAi mob, int maxHorizontalDistance, int maxVerticalDistance) {
      return findTarget(mob, maxHorizontalDistance, maxVerticalDistance, 0, (Vec3d)null, true, 1.5707963705062866D, mob::getPathfindingFavor, false, 0, 0, true);
   }

   @Nullable
   public static Vec3d findGroundTarget(MobEntityWithAi mob, int maxHorizontalDistance, int maxVerticalDistance, int preferredYDifference, @Nullable Vec3d preferredAngle, double maxAngleDifference) {
      return findTarget(mob, maxHorizontalDistance, maxVerticalDistance, preferredYDifference, preferredAngle, true, maxAngleDifference, mob::getPathfindingFavor, true, 0, 0, false);
   }

   @Nullable
   public static Vec3d findGroundTarget(MobEntityWithAi mob, int maxHorizontalDistance, int maxVerticalDistance) {
      mob.getClass();
      return findGroundTarget(mob, maxHorizontalDistance, maxVerticalDistance, mob::getPathfindingFavor);
   }

   @Nullable
   public static Vec3d findGroundTarget(MobEntityWithAi mob, int maxHorizontalDistance, int maxVerticalDistance, ToDoubleFunction<BlockPos> pathfindingFavor) {
      return findTarget(mob, maxHorizontalDistance, maxVerticalDistance, 0, (Vec3d)null, false, 0.0D, pathfindingFavor, true, 0, 0, true);
   }

   @Nullable
   public static Vec3d findAirTarget(MobEntityWithAi mob, int maxHorizontalDistance, int maxVerticalDistance, Vec3d preferredAngle, float maxAngleDifference, int distanceAboveGroundRange, int minDistanceAboveGround) {
      return findTarget(mob, maxHorizontalDistance, maxVerticalDistance, 0, preferredAngle, false, (double)maxAngleDifference, mob::getPathfindingFavor, true, distanceAboveGroundRange, minDistanceAboveGround, true);
   }

   @Nullable
   public static Vec3d findTargetTowards(MobEntityWithAi mob, int maxHorizontalDistance, int maxVerticalDistance, Vec3d pos) {
      Vec3d vec3d = pos.subtract(mob.getX(), mob.getY(), mob.getZ());
      return findTarget(mob, maxHorizontalDistance, maxVerticalDistance, 0, vec3d, true, 1.5707963705062866D, mob::getPathfindingFavor, false, 0, 0, true);
   }

   @Nullable
   public static Vec3d findTargetTowards(MobEntityWithAi mob, int maxHorizontalDistance, int maxVerticalDistance, Vec3d pos, double maxAngleDifference) {
      Vec3d vec3d = pos.subtract(mob.getX(), mob.getY(), mob.getZ());
      return findTarget(mob, maxHorizontalDistance, maxVerticalDistance, 0, vec3d, true, maxAngleDifference, mob::getPathfindingFavor, false, 0, 0, true);
   }

   @Nullable
   public static Vec3d findGroundTargetTowards(MobEntityWithAi mob, int maxHorizontalDistance, int maxVerticalDistance, int preferredYDifference, Vec3d pos, double maxAngleDifference) {
      Vec3d vec3d = pos.subtract(mob.getX(), mob.getY(), mob.getZ());
      return findTarget(mob, maxHorizontalDistance, maxVerticalDistance, preferredYDifference, vec3d, false, maxAngleDifference, mob::getPathfindingFavor, true, 0, 0, false);
   }

   @Nullable
   public static Vec3d findTargetAwayFrom(MobEntityWithAi mob, int maxHorizontalDistance, int maxVerticalDistance, Vec3d pos) {
      Vec3d vec3d = mob.getPos().subtract(pos);
      return findTarget(mob, maxHorizontalDistance, maxVerticalDistance, 0, vec3d, true, 1.5707963705062866D, mob::getPathfindingFavor, false, 0, 0, true);
   }

   @Nullable
   public static Vec3d findGroundTargetAwayFrom(MobEntityWithAi mob, int maxHorizontalDistance, int maxVerticalDistance, Vec3d pos) {
      Vec3d vec3d = mob.getPos().subtract(pos);
      return findTarget(mob, maxHorizontalDistance, maxVerticalDistance, 0, vec3d, false, 1.5707963705062866D, mob::getPathfindingFavor, true, 0, 0, true);
   }

   @Nullable
   private static Vec3d findTarget(MobEntityWithAi mob, int maxHorizontalDistance, int maxVerticalDistance, int preferredYDifference, @Nullable Vec3d preferredAngle, boolean notInWater, double maxAngleDifference, ToDoubleFunction<BlockPos> favorProvider, boolean aboveGround, int distanceAboveGroundRange, int minDistanceAboveGround, boolean validPositionsOnly) {
      EntityNavigation entityNavigation = mob.getNavigation();
      Random random = mob.getRandom();
      boolean bl2;
      if (mob.hasPositionTarget()) {
         bl2 = mob.getPositionTarget().isWithinDistance(mob.getPos(), (double)(mob.getPositionTargetRange() + (float)maxHorizontalDistance) + 1.0D);
      } else {
         bl2 = false;
      }

      boolean bl3 = false;
      double d = Double.NEGATIVE_INFINITY;
      BlockPos blockPos = new BlockPos(mob);

      for(int i = 0; i < 10; ++i) {
         BlockPos blockPos2 = getRandomOffset(random, maxHorizontalDistance, maxVerticalDistance, preferredYDifference, preferredAngle, maxAngleDifference);
         if (blockPos2 != null) {
            int j = blockPos2.getX();
            int k = blockPos2.getY();
            int l = blockPos2.getZ();
            BlockPos blockPos4;
            if (mob.hasPositionTarget() && maxHorizontalDistance > 1) {
               blockPos4 = mob.getPositionTarget();
               if (mob.getX() > (double)blockPos4.getX()) {
                  j -= random.nextInt(maxHorizontalDistance / 2);
               } else {
                  j += random.nextInt(maxHorizontalDistance / 2);
               }

               if (mob.getZ() > (double)blockPos4.getZ()) {
                  l -= random.nextInt(maxHorizontalDistance / 2);
               } else {
                  l += random.nextInt(maxHorizontalDistance / 2);
               }
            }

            blockPos4 = new BlockPos((double)j + mob.getX(), (double)k + mob.getY(), (double)l + mob.getZ());
            if (blockPos4.getY() >= 0 && blockPos4.getY() <= mob.world.getHeight() && (!bl2 || mob.isInWalkTargetRange(blockPos4)) && (!validPositionsOnly || entityNavigation.isValidPosition(blockPos4))) {
               if (aboveGround) {
                  blockPos4 = findValidPositionAbove(blockPos4, random.nextInt(distanceAboveGroundRange + 1) + minDistanceAboveGround, mob.world.getHeight(), (blockPosx) -> {
                     return mob.world.getBlockState(blockPosx).getMaterial().isSolid();
                  });
               }

               if (notInWater || !mob.world.getFluidState(blockPos4).matches(FluidTags.WATER)) {
                  PathNodeType pathNodeType = LandPathNodeMaker.getPathNodeType(mob.world, blockPos4.getX(), blockPos4.getY(), blockPos4.getZ());
                  if (mob.getPathfindingPenalty(pathNodeType) == 0.0F) {
                     double e = favorProvider.applyAsDouble(blockPos4);
                     if (e > d) {
                        d = e;
                        blockPos = blockPos4;
                        bl3 = true;
                     }
                  }
               }
            }
         }
      }

      if (bl3) {
         return new Vec3d(blockPos);
      } else {
         return null;
      }
   }

   @Nullable
   private static BlockPos getRandomOffset(Random random, int maxHorizontalDistance, int maxVerticalDistance, int preferredYDifference, @Nullable Vec3d preferredAngle, double maxAngleDifference) {
      if (preferredAngle != null && maxAngleDifference < 3.141592653589793D) {
         double d = MathHelper.atan2(preferredAngle.z, preferredAngle.x) - 1.5707963705062866D;
         double e = d + (double)(2.0F * random.nextFloat() - 1.0F) * maxAngleDifference;
         double f = Math.sqrt(random.nextDouble()) * (double)MathHelper.SQUARE_ROOT_OF_TWO * (double)maxHorizontalDistance;
         double g = -f * Math.sin(e);
         double h = f * Math.cos(e);
         if (Math.abs(g) <= (double)maxHorizontalDistance && Math.abs(h) <= (double)maxHorizontalDistance) {
            int l = random.nextInt(2 * maxVerticalDistance + 1) - maxVerticalDistance + preferredYDifference;
            return new BlockPos(g, (double)l, h);
         } else {
            return null;
         }
      } else {
         int i = random.nextInt(2 * maxHorizontalDistance + 1) - maxHorizontalDistance;
         int j = random.nextInt(2 * maxVerticalDistance + 1) - maxVerticalDistance + preferredYDifference;
         int k = random.nextInt(2 * maxHorizontalDistance + 1) - maxHorizontalDistance;
         return new BlockPos(i, j, k);
      }
   }

   static BlockPos findValidPositionAbove(BlockPos pos, int minDistanceAboveIllegal, int maxOffset, Predicate<BlockPos> isIllegalPredicate) {
      if (minDistanceAboveIllegal < 0) {
         throw new IllegalArgumentException("aboveSolidAmount was " + minDistanceAboveIllegal + ", expected >= 0");
      } else if (!isIllegalPredicate.test(pos)) {
         return pos;
      } else {
         BlockPos blockPos;
         for(blockPos = pos.up(); blockPos.getY() < maxOffset && isIllegalPredicate.test(blockPos); blockPos = blockPos.up()) {
         }

         BlockPos blockPos2;
         BlockPos blockPos3;
         for(blockPos2 = blockPos; blockPos2.getY() < maxOffset && blockPos2.getY() - blockPos.getY() < minDistanceAboveIllegal; blockPos2 = blockPos3) {
            blockPos3 = blockPos2.up();
            if (isIllegalPredicate.test(blockPos3)) {
               break;
            }
         }

         return blockPos2;
      }
   }
}
