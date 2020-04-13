package net.minecraft.entity.mob;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.packet.EntitySpawnS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class EvokerFangsEntity extends Entity {
   private int warmup;
   private boolean field_7610;
   private int ticksLeft;
   private boolean hasAttacked;
   private LivingEntity owner;
   private UUID ownerUuid;

   public EvokerFangsEntity(EntityType<? extends EvokerFangsEntity> entityType, World world) {
      super(entityType, world);
      this.ticksLeft = 22;
   }

   public EvokerFangsEntity(World world, double x, double y, double z, float f, int warmup, LivingEntity owner) {
      this(EntityType.EVOKER_FANGS, world);
      this.warmup = warmup;
      this.setOwner(owner);
      this.yaw = f * 57.295776F;
      this.updatePosition(x, y, z);
   }

   protected void initDataTracker() {
   }

   public void setOwner(@Nullable LivingEntity owner) {
      this.owner = owner;
      this.ownerUuid = owner == null ? null : owner.getUuid();
   }

   @Nullable
   public LivingEntity getOwner() {
      if (this.owner == null && this.ownerUuid != null && this.world instanceof ServerWorld) {
         Entity entity = ((ServerWorld)this.world).getEntity(this.ownerUuid);
         if (entity instanceof LivingEntity) {
            this.owner = (LivingEntity)entity;
         }
      }

      return this.owner;
   }

   protected void readCustomDataFromTag(CompoundTag tag) {
      this.warmup = tag.getInt("Warmup");
      if (tag.containsUuid("OwnerUUID")) {
         this.ownerUuid = tag.getUuid("OwnerUUID");
      }

   }

   protected void writeCustomDataToTag(CompoundTag tag) {
      tag.putInt("Warmup", this.warmup);
      if (this.ownerUuid != null) {
         tag.putUuid("OwnerUUID", this.ownerUuid);
      }

   }

   public void tick() {
      super.tick();
      if (this.world.isClient) {
         if (this.hasAttacked) {
            --this.ticksLeft;
            if (this.ticksLeft == 14) {
               for(int i = 0; i < 12; ++i) {
                  double d = this.getX() + (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.getWidth() * 0.5D;
                  double e = this.getY() + 0.05D + this.random.nextDouble();
                  double f = this.getZ() + (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.getWidth() * 0.5D;
                  double g = (this.random.nextDouble() * 2.0D - 1.0D) * 0.3D;
                  double h = 0.3D + this.random.nextDouble() * 0.3D;
                  double j = (this.random.nextDouble() * 2.0D - 1.0D) * 0.3D;
                  this.world.addParticle(ParticleTypes.CRIT, d, e + 1.0D, f, g, h, j);
               }
            }
         }
      } else if (--this.warmup < 0) {
         if (this.warmup == -8) {
            List<LivingEntity> list = this.world.getNonSpectatingEntities(LivingEntity.class, this.getBoundingBox().expand(0.2D, 0.0D, 0.2D));
            Iterator var15 = list.iterator();

            while(var15.hasNext()) {
               LivingEntity livingEntity = (LivingEntity)var15.next();
               this.damage(livingEntity);
            }
         }

         if (!this.field_7610) {
            this.world.sendEntityStatus(this, (byte)4);
            this.field_7610 = true;
         }

         if (--this.ticksLeft < 0) {
            this.remove();
         }
      }

   }

   private void damage(LivingEntity target) {
      LivingEntity livingEntity = this.getOwner();
      if (target.isAlive() && !target.isInvulnerable() && target != livingEntity) {
         if (livingEntity == null) {
            target.damage(DamageSource.MAGIC, 6.0F);
         } else {
            if (livingEntity.isTeammate(target)) {
               return;
            }

            target.damage(DamageSource.magic(this, livingEntity), 6.0F);
         }

      }
   }

   @Environment(EnvType.CLIENT)
   public void handleStatus(byte status) {
      super.handleStatus(status);
      if (status == 4) {
         this.hasAttacked = true;
         if (!this.isSilent()) {
            this.world.playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_EVOKER_FANGS_ATTACK, this.getSoundCategory(), 1.0F, this.random.nextFloat() * 0.2F + 0.85F, false);
         }
      }

   }

   @Environment(EnvType.CLIENT)
   public float getAnimationProgress(float tickDelta) {
      if (!this.hasAttacked) {
         return 0.0F;
      } else {
         int i = this.ticksLeft - 2;
         return i <= 0 ? 1.0F : 1.0F - ((float)i - tickDelta) / 20.0F;
      }
   }

   public Packet<?> createSpawnPacket() {
      return new EntitySpawnS2CPacket(this);
   }
}
