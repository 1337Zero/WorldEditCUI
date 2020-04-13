package net.minecraft.entity.boss.dragon.phase;

import javax.annotation.Nullable;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.util.math.Vec3d;

public class HoverPhase extends AbstractPhase {
   private Vec3d field_7042;

   public HoverPhase(EnderDragonEntity dragon) {
      super(dragon);
   }

   public void serverTick() {
      if (this.field_7042 == null) {
         this.field_7042 = this.dragon.getPos();
      }

   }

   public boolean method_6848() {
      return true;
   }

   public void beginPhase() {
      this.field_7042 = null;
   }

   public float method_6846() {
      return 1.0F;
   }

   @Nullable
   public Vec3d getTarget() {
      return this.field_7042;
   }

   public PhaseType<HoverPhase> getType() {
      return PhaseType.HOVER;
   }
}
