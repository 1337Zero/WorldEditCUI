package net.minecraft.entity.passive;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntityWithAi;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

public abstract class GolemEntity extends MobEntityWithAi {
   protected GolemEntity(EntityType<? extends GolemEntity> type, World world) {
      super(type, world);
   }

   public boolean handleFallDamage(float fallDistance, float damageMultiplier) {
      return false;
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return null;
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource source) {
      return null;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return null;
   }

   public int getMinAmbientSoundDelay() {
      return 120;
   }

   public boolean canImmediatelyDespawn(double distanceSquared) {
      return false;
   }
}
