package net.minecraft.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.block.BlockPlacementEnvironment;
import net.minecraft.entity.ai.TargetFinder;
import net.minecraft.entity.mob.MobEntityWithAi;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SwimAroundGoal extends WanderAroundGoal {
   public SwimAroundGoal(MobEntityWithAi mobEntityWithAi, double d, int i) {
      super(mobEntityWithAi, d, i);
   }

   @Nullable
   protected Vec3d getWanderTarget() {
      Vec3d vec3d = TargetFinder.findTarget(this.mob, 10, 7);

      for(int var2 = 0; vec3d != null && !this.mob.world.getBlockState(new BlockPos(vec3d)).canPlaceAtSide(this.mob.world, new BlockPos(vec3d), BlockPlacementEnvironment.WATER) && var2++ < 10; vec3d = TargetFinder.findTarget(this.mob, 10, 7)) {
      }

      return vec3d;
   }
}
