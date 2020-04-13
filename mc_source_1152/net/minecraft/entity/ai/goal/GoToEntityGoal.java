package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;

public class GoToEntityGoal extends LookAtEntityGoal {
   public GoToEntityGoal(MobEntity mobEntity, Class<? extends LivingEntity> var2, float f, float g) {
      super(mobEntity, var2, f, g);
      this.setControls(EnumSet.of(Goal.Control.LOOK, Goal.Control.MOVE));
   }
}
