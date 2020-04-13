package net.minecraft.entity.projectile;

import com.google.common.collect.Lists;
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
import net.minecraft.entity.ProjectileUtil;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.Packet;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;

public class ShulkerBulletEntity extends Entity {
   private LivingEntity owner;
   private Entity target;
   @Nullable
   private Direction direction;
   private int field_7627;
   private double field_7635;
   private double field_7633;
   private double field_7625;
   @Nullable
   private UUID ownerUuid;
   private BlockPos ownerPos;
   @Nullable
   private UUID targetUuid;
   private BlockPos targetPos;

   public ShulkerBulletEntity(EntityType<? extends ShulkerBulletEntity> entityType, World world) {
      super(entityType, world);
      this.noClip = true;
   }

   @Environment(EnvType.CLIENT)
   public ShulkerBulletEntity(World world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      this(EntityType.SHULKER_BULLET, world);
      this.refreshPositionAndAngles(x, y, z, this.yaw, this.pitch);
      this.setVelocity(velocityX, velocityY, velocityZ);
   }

   public ShulkerBulletEntity(World world, LivingEntity owner, Entity target, Direction.Axis axis) {
      this(EntityType.SHULKER_BULLET, world);
      this.owner = owner;
      BlockPos blockPos = new BlockPos(owner);
      double d = (double)blockPos.getX() + 0.5D;
      double e = (double)blockPos.getY() + 0.5D;
      double f = (double)blockPos.getZ() + 0.5D;
      this.refreshPositionAndAngles(d, e, f, this.yaw, this.pitch);
      this.target = target;
      this.direction = Direction.UP;
      this.method_7486(axis);
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.HOSTILE;
   }

   protected void writeCustomDataToTag(CompoundTag tag) {
      BlockPos blockPos2;
      CompoundTag compoundTag2;
      if (this.owner != null) {
         blockPos2 = new BlockPos(this.owner);
         compoundTag2 = NbtHelper.fromUuid(this.owner.getUuid());
         compoundTag2.putInt("X", blockPos2.getX());
         compoundTag2.putInt("Y", blockPos2.getY());
         compoundTag2.putInt("Z", blockPos2.getZ());
         tag.put("Owner", compoundTag2);
      }

      if (this.target != null) {
         blockPos2 = new BlockPos(this.target);
         compoundTag2 = NbtHelper.fromUuid(this.target.getUuid());
         compoundTag2.putInt("X", blockPos2.getX());
         compoundTag2.putInt("Y", blockPos2.getY());
         compoundTag2.putInt("Z", blockPos2.getZ());
         tag.put("Target", compoundTag2);
      }

      if (this.direction != null) {
         tag.putInt("Dir", this.direction.getId());
      }

      tag.putInt("Steps", this.field_7627);
      tag.putDouble("TXD", this.field_7635);
      tag.putDouble("TYD", this.field_7633);
      tag.putDouble("TZD", this.field_7625);
   }

   protected void readCustomDataFromTag(CompoundTag tag) {
      this.field_7627 = tag.getInt("Steps");
      this.field_7635 = tag.getDouble("TXD");
      this.field_7633 = tag.getDouble("TYD");
      this.field_7625 = tag.getDouble("TZD");
      if (tag.contains("Dir", 99)) {
         this.direction = Direction.byId(tag.getInt("Dir"));
      }

      CompoundTag compoundTag2;
      if (tag.contains("Owner", 10)) {
         compoundTag2 = tag.getCompound("Owner");
         this.ownerUuid = NbtHelper.toUuid(compoundTag2);
         this.ownerPos = new BlockPos(compoundTag2.getInt("X"), compoundTag2.getInt("Y"), compoundTag2.getInt("Z"));
      }

      if (tag.contains("Target", 10)) {
         compoundTag2 = tag.getCompound("Target");
         this.targetUuid = NbtHelper.toUuid(compoundTag2);
         this.targetPos = new BlockPos(compoundTag2.getInt("X"), compoundTag2.getInt("Y"), compoundTag2.getInt("Z"));
      }

   }

   protected void initDataTracker() {
   }

   private void setDirection(@Nullable Direction direction) {
      this.direction = direction;
   }

   private void method_7486(@Nullable Direction.Axis axis) {
      double d = 0.5D;
      BlockPos blockPos2;
      if (this.target == null) {
         blockPos2 = (new BlockPos(this)).down();
      } else {
         d = (double)this.target.getHeight() * 0.5D;
         blockPos2 = new BlockPos(this.target.getX(), this.target.getY() + d, this.target.getZ());
      }

      double e = (double)blockPos2.getX() + 0.5D;
      double f = (double)blockPos2.getY() + d;
      double g = (double)blockPos2.getZ() + 0.5D;
      Direction direction = null;
      if (!blockPos2.isWithinDistance(this.getPos(), 2.0D)) {
         BlockPos blockPos3 = new BlockPos(this);
         List<Direction> list = Lists.newArrayList();
         if (axis != Direction.Axis.X) {
            if (blockPos3.getX() < blockPos2.getX() && this.world.isAir(blockPos3.east())) {
               list.add(Direction.EAST);
            } else if (blockPos3.getX() > blockPos2.getX() && this.world.isAir(blockPos3.west())) {
               list.add(Direction.WEST);
            }
         }

         if (axis != Direction.Axis.Y) {
            if (blockPos3.getY() < blockPos2.getY() && this.world.isAir(blockPos3.up())) {
               list.add(Direction.UP);
            } else if (blockPos3.getY() > blockPos2.getY() && this.world.isAir(blockPos3.down())) {
               list.add(Direction.DOWN);
            }
         }

         if (axis != Direction.Axis.Z) {
            if (blockPos3.getZ() < blockPos2.getZ() && this.world.isAir(blockPos3.south())) {
               list.add(Direction.SOUTH);
            } else if (blockPos3.getZ() > blockPos2.getZ() && this.world.isAir(blockPos3.north())) {
               list.add(Direction.NORTH);
            }
         }

         direction = Direction.random(this.random);
         if (list.isEmpty()) {
            for(int i = 5; !this.world.isAir(blockPos3.offset(direction)) && i > 0; --i) {
               direction = Direction.random(this.random);
            }
         } else {
            direction = (Direction)list.get(this.random.nextInt(list.size()));
         }

         e = this.getX() + (double)direction.getOffsetX();
         f = this.getY() + (double)direction.getOffsetY();
         g = this.getZ() + (double)direction.getOffsetZ();
      }

      this.setDirection(direction);
      double h = e - this.getX();
      double j = f - this.getY();
      double k = g - this.getZ();
      double l = (double)MathHelper.sqrt(h * h + j * j + k * k);
      if (l == 0.0D) {
         this.field_7635 = 0.0D;
         this.field_7633 = 0.0D;
         this.field_7625 = 0.0D;
      } else {
         this.field_7635 = h / l * 0.15D;
         this.field_7633 = j / l * 0.15D;
         this.field_7625 = k / l * 0.15D;
      }

      this.velocityDirty = true;
      this.field_7627 = 10 + this.random.nextInt(5) * 10;
   }

   public void checkDespawn() {
      if (this.world.getDifficulty() == Difficulty.PEACEFUL) {
         this.remove();
      }

   }

   public void tick() {
      super.tick();
      Vec3d vec3d;
      if (!this.world.isClient) {
         List list2;
         Iterator var2;
         LivingEntity livingEntity2;
         if (this.target == null && this.targetUuid != null) {
            list2 = this.world.getNonSpectatingEntities(LivingEntity.class, new Box(this.targetPos.add(-2, -2, -2), this.targetPos.add(2, 2, 2)));
            var2 = list2.iterator();

            while(var2.hasNext()) {
               livingEntity2 = (LivingEntity)var2.next();
               if (livingEntity2.getUuid().equals(this.targetUuid)) {
                  this.target = livingEntity2;
                  break;
               }
            }

            this.targetUuid = null;
         }

         if (this.owner == null && this.ownerUuid != null) {
            list2 = this.world.getNonSpectatingEntities(LivingEntity.class, new Box(this.ownerPos.add(-2, -2, -2), this.ownerPos.add(2, 2, 2)));
            var2 = list2.iterator();

            while(var2.hasNext()) {
               livingEntity2 = (LivingEntity)var2.next();
               if (livingEntity2.getUuid().equals(this.ownerUuid)) {
                  this.owner = livingEntity2;
                  break;
               }
            }

            this.ownerUuid = null;
         }

         if (this.target == null || !this.target.isAlive() || this.target instanceof PlayerEntity && ((PlayerEntity)this.target).isSpectator()) {
            if (!this.hasNoGravity()) {
               this.setVelocity(this.getVelocity().add(0.0D, -0.04D, 0.0D));
            }
         } else {
            this.field_7635 = MathHelper.clamp(this.field_7635 * 1.025D, -1.0D, 1.0D);
            this.field_7633 = MathHelper.clamp(this.field_7633 * 1.025D, -1.0D, 1.0D);
            this.field_7625 = MathHelper.clamp(this.field_7625 * 1.025D, -1.0D, 1.0D);
            vec3d = this.getVelocity();
            this.setVelocity(vec3d.add((this.field_7635 - vec3d.x) * 0.2D, (this.field_7633 - vec3d.y) * 0.2D, (this.field_7625 - vec3d.z) * 0.2D));
         }

         HitResult hitResult = ProjectileUtil.getCollision(this, true, false, this.owner, RayTraceContext.ShapeType.COLLIDER);
         if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
         }
      }

      vec3d = this.getVelocity();
      this.updatePosition(this.getX() + vec3d.x, this.getY() + vec3d.y, this.getZ() + vec3d.z);
      ProjectileUtil.method_7484(this, 0.5F);
      if (this.world.isClient) {
         this.world.addParticle(ParticleTypes.END_ROD, this.getX() - vec3d.x, this.getY() - vec3d.y + 0.15D, this.getZ() - vec3d.z, 0.0D, 0.0D, 0.0D);
      } else if (this.target != null && !this.target.removed) {
         if (this.field_7627 > 0) {
            --this.field_7627;
            if (this.field_7627 == 0) {
               this.method_7486(this.direction == null ? null : this.direction.getAxis());
            }
         }

         if (this.direction != null) {
            BlockPos blockPos = new BlockPos(this);
            Direction.Axis axis = this.direction.getAxis();
            if (this.world.isTopSolid(blockPos.offset(this.direction), this)) {
               this.method_7486(axis);
            } else {
               BlockPos blockPos2 = new BlockPos(this.target);
               if (axis == Direction.Axis.X && blockPos.getX() == blockPos2.getX() || axis == Direction.Axis.Z && blockPos.getZ() == blockPos2.getZ() || axis == Direction.Axis.Y && blockPos.getY() == blockPos2.getY()) {
                  this.method_7486(axis);
               }
            }
         }
      }

   }

   public boolean isOnFire() {
      return false;
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldRender(double distance) {
      return distance < 16384.0D;
   }

   public float getBrightnessAtEyes() {
      return 1.0F;
   }

   protected void onHit(HitResult hitResult) {
      if (hitResult.getType() == HitResult.Type.ENTITY) {
         Entity entity = ((EntityHitResult)hitResult).getEntity();
         boolean bl = entity.damage(DamageSource.mobProjectile(this, this.owner).setProjectile(), 4.0F);
         if (bl) {
            this.dealDamage(this.owner, entity);
            if (entity instanceof LivingEntity) {
               ((LivingEntity)entity).addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 200));
            }
         }
      } else {
         ((ServerWorld)this.world).spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.0D);
         this.playSound(SoundEvents.ENTITY_SHULKER_BULLET_HIT, 1.0F, 1.0F);
      }

      this.remove();
   }

   public boolean collides() {
      return true;
   }

   public boolean damage(DamageSource source, float amount) {
      if (!this.world.isClient) {
         this.playSound(SoundEvents.ENTITY_SHULKER_BULLET_HURT, 1.0F, 1.0F);
         ((ServerWorld)this.world).spawnParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 15, 0.2D, 0.2D, 0.2D, 0.0D);
         this.remove();
      }

      return true;
   }

   public Packet<?> createSpawnPacket() {
      return new EntitySpawnS2CPacket(this);
   }
}
