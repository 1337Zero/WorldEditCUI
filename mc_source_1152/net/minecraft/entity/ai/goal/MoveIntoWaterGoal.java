package net.minecraft.entity.ai.goal;

import java.util.Iterator;
import net.minecraft.entity.mob.MobEntityWithAi;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class MoveIntoWaterGoal extends Goal {
   private final MobEntityWithAi mob;

   public MoveIntoWaterGoal(MobEntityWithAi mob) {
      this.mob = mob;
   }

   public boolean canStart() {
      return this.mob.onGround && !this.mob.world.getFluidState(new BlockPos(this.mob)).matches(FluidTags.WATER);
   }

   public void start() {
      BlockPos blockPos = null;
      Iterable<BlockPos> iterable = BlockPos.iterate(MathHelper.floor(this.mob.getX() - 2.0D), MathHelper.floor(this.mob.getY() - 2.0D), MathHelper.floor(this.mob.getZ() - 2.0D), MathHelper.floor(this.mob.getX() + 2.0D), MathHelper.floor(this.mob.getY()), MathHelper.floor(this.mob.getZ() + 2.0D));
      Iterator var3 = iterable.iterator();

      while(var3.hasNext()) {
         BlockPos blockPos2 = (BlockPos)var3.next();
         if (this.mob.world.getFluidState(blockPos2).matches(FluidTags.WATER)) {
            blockPos = blockPos2;
            break;
         }
      }

      if (blockPos != null) {
         this.mob.getMoveControl().moveTo((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), 1.0D);
      }

   }
}
