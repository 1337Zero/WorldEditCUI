package net.minecraft.entity.boss.dragon.phase;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EnderCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public abstract class AbstractPhase implements Phase {
   protected final EnderDragonEntity dragon;

   public AbstractPhase(EnderDragonEntity dragon) {
      this.dragon = dragon;
   }

   public boolean method_6848() {
      return false;
   }

   public void clientTick() {
   }

   public void serverTick() {
   }

   public void crystalDestroyed(EnderCrystalEntity crystal, BlockPos pos, DamageSource source, @Nullable PlayerEntity player) {
   }

   public void beginPhase() {
   }

   public void endPhase() {
   }

   public float method_6846() {
      return 0.6F;
   }

   @Nullable
   public Vec3d getTarget() {
      return null;
   }

   public float modifyDamageTaken(DamageSource damageSource, float f) {
      return f;
   }

   public float method_6847() {
      float f = MathHelper.sqrt(Entity.squaredHorizontalLength(this.dragon.getVelocity())) + 1.0F;
      float g = Math.min(f, 40.0F);
      return 0.7F / g / f;
   }
}
