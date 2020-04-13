package net.minecraft.entity.passive;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class DonkeyEntity extends AbstractDonkeyEntity {
   public DonkeyEntity(EntityType<? extends DonkeyEntity> entityType, World world) {
      super(entityType, world);
   }

   protected SoundEvent getAmbientSound() {
      super.getAmbientSound();
      return SoundEvents.ENTITY_DONKEY_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      super.getDeathSound();
      return SoundEvents.ENTITY_DONKEY_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      super.getHurtSound(source);
      return SoundEvents.ENTITY_DONKEY_HURT;
   }

   public boolean canBreedWith(AnimalEntity other) {
      if (other == this) {
         return false;
      } else if (!(other instanceof DonkeyEntity) && !(other instanceof HorseEntity)) {
         return false;
      } else {
         return this.canBreed() && ((HorseBaseEntity)other).canBreed();
      }
   }

   public PassiveEntity createChild(PassiveEntity mate) {
      EntityType<? extends HorseBaseEntity> entityType = mate instanceof HorseEntity ? EntityType.MULE : EntityType.DONKEY;
      HorseBaseEntity horseBaseEntity = (HorseBaseEntity)entityType.create(this.world);
      this.setChildAttributes(mate, horseBaseEntity);
      return horseBaseEntity;
   }
}
