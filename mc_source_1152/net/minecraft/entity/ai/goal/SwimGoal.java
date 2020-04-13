package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.mob.MobEntity;

public class SwimGoal extends Goal {
   private final MobEntity mob;

   public SwimGoal(MobEntity mob) {
      this.mob = mob;
      this.setControls(EnumSet.of(Goal.Control.JUMP));
      mob.getNavigation().setCanSwim(true);
   }

   public boolean canStart() {
      double d = (double)this.mob.getStandingEyeHeight() < 0.4D ? 0.2D : 0.4D;
      return this.mob.isTouchingWater() && this.mob.getWaterHeight() > d || this.mob.isInLava();
   }

   public void tick() {
      if (this.mob.getRandom().nextFloat() < 0.8F) {
         this.mob.getJumpControl().setActive();
      }

   }
}
