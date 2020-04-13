package net.minecraft.entity.mob;

import java.util.Iterator;
import java.util.List;
import net.minecraft.client.network.packet.GameStateChangeS2CPacket;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ElderGuardianEntity extends GuardianEntity {
   public static final float field_17492;

   public ElderGuardianEntity(EntityType<? extends ElderGuardianEntity> entityType, World world) {
      super(entityType, world);
      this.setPersistent();
      if (this.wanderGoal != null) {
         this.wanderGoal.setChance(400);
      }

   }

   protected void initAttributes() {
      super.initAttributes();
      this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.30000001192092896D);
      this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(8.0D);
      this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(80.0D);
   }

   public int getWarmupTime() {
      return 60;
   }

   protected SoundEvent getAmbientSound() {
      return this.isInsideWaterOrBubbleColumn() ? SoundEvents.ENTITY_ELDER_GUARDIAN_AMBIENT : SoundEvents.ENTITY_ELDER_GUARDIAN_AMBIENT_LAND;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return this.isInsideWaterOrBubbleColumn() ? SoundEvents.ENTITY_ELDER_GUARDIAN_HURT : SoundEvents.ENTITY_ELDER_GUARDIAN_HURT_LAND;
   }

   protected SoundEvent getDeathSound() {
      return this.isInsideWaterOrBubbleColumn() ? SoundEvents.ENTITY_ELDER_GUARDIAN_DEATH : SoundEvents.ENTITY_ELDER_GUARDIAN_DEATH_LAND;
   }

   protected SoundEvent getFlopSound() {
      return SoundEvents.ENTITY_ELDER_GUARDIAN_FLOP;
   }

   protected void mobTick() {
      super.mobTick();
      int i = true;
      if ((this.age + this.getEntityId()) % 1200 == 0) {
         StatusEffect statusEffect = StatusEffects.MINING_FATIGUE;
         List<ServerPlayerEntity> list = ((ServerWorld)this.world).getPlayers((serverPlayerEntityx) -> {
            return this.squaredDistanceTo(serverPlayerEntityx) < 2500.0D && serverPlayerEntityx.interactionManager.isSurvivalLike();
         });
         int j = true;
         int k = true;
         int l = true;
         Iterator var7 = list.iterator();

         label28:
         while(true) {
            ServerPlayerEntity serverPlayerEntity;
            do {
               if (!var7.hasNext()) {
                  break label28;
               }

               serverPlayerEntity = (ServerPlayerEntity)var7.next();
            } while(serverPlayerEntity.hasStatusEffect(statusEffect) && serverPlayerEntity.getStatusEffect(statusEffect).getAmplifier() >= 2 && serverPlayerEntity.getStatusEffect(statusEffect).getDuration() >= 1200);

            serverPlayerEntity.networkHandler.sendPacket(new GameStateChangeS2CPacket(10, 0.0F));
            serverPlayerEntity.addStatusEffect(new StatusEffectInstance(statusEffect, 6000, 2));
         }
      }

      if (!this.hasPositionTarget()) {
         this.setPositionTarget(new BlockPos(this), 16);
      }

   }

   static {
      field_17492 = EntityType.ELDER_GUARDIAN.getWidth() / EntityType.GUARDIAN.getWidth();
   }
}
