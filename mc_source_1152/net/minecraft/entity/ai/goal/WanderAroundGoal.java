package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.entity.ai.TargetFinder;
import net.minecraft.entity.mob.MobEntityWithAi;
import net.minecraft.util.math.Vec3d;

public class WanderAroundGoal extends Goal {
   protected final MobEntityWithAi mob;
   protected double targetX;
   protected double targetY;
   protected double targetZ;
   protected final double speed;
   protected int chance;
   protected boolean ignoringChance;

   public WanderAroundGoal(MobEntityWithAi mob, double speed) {
      this(mob, speed, 120);
   }

   public WanderAroundGoal(MobEntityWithAi mob, double speed, int chance) {
      this.mob = mob;
      this.speed = speed;
      this.chance = chance;
      this.setControls(EnumSet.of(Goal.Control.MOVE));
   }

   public boolean canStart() {
      if (this.mob.hasPassengers()) {
         return false;
      } else {
         if (!this.ignoringChance) {
            if (this.mob.getDespawnCounter() >= 100) {
               return false;
            }

            if (this.mob.getRandom().nextInt(this.chance) != 0) {
               return false;
            }
         }

         Vec3d vec3d = this.getWanderTarget();
         if (vec3d == null) {
            return false;
         } else {
            this.targetX = vec3d.x;
            this.targetY = vec3d.y;
            this.targetZ = vec3d.z;
            this.ignoringChance = false;
            return true;
         }
      }
   }

   @Nullable
   protected Vec3d getWanderTarget() {
      return TargetFinder.findTarget(this.mob, 10, 7);
   }

   public boolean shouldContinue() {
      return !this.mob.getNavigation().isIdle() && !this.mob.hasPassengers();
   }

   public void start() {
      this.mob.getNavigation().startMovingTo(this.targetX, this.targetY, this.targetZ, this.speed);
   }

   public void stop() {
      this.mob.getNavigation().stop();
      super.stop();
   }

   public void ignoreChanceOnce() {
      this.ignoringChance = true;
   }

   public void setChance(int chance) {
      this.chance = chance;
   }
}
