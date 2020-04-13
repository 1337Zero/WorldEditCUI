package net.minecraft.entity.projectile;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.packet.EntitySpawnS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ProjectileUtil;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Packet;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;

public abstract class ExplosiveProjectileEntity extends Entity {
   public LivingEntity owner;
   private int life;
   private int ticks;
   public double posX;
   public double posY;
   public double posZ;

   protected ExplosiveProjectileEntity(EntityType<? extends ExplosiveProjectileEntity> type, World world) {
      super(type, world);
   }

   public ExplosiveProjectileEntity(EntityType<? extends ExplosiveProjectileEntity> type, double x, double y, double z, double directionX, double directionY, double directionZ, World world) {
      this(type, world);
      this.refreshPositionAndAngles(x, y, z, this.yaw, this.pitch);
      this.updatePosition(x, y, z);
      double d = (double)MathHelper.sqrt(directionX * directionX + directionY * directionY + directionZ * directionZ);
      this.posX = directionX / d * 0.1D;
      this.posY = directionY / d * 0.1D;
      this.posZ = directionZ / d * 0.1D;
   }

   public ExplosiveProjectileEntity(EntityType<? extends ExplosiveProjectileEntity> type, LivingEntity owner, double directionX, double directionY, double directionZ, World world) {
      this(type, world);
      this.owner = owner;
      this.refreshPositionAndAngles(owner.getX(), owner.getY(), owner.getZ(), owner.yaw, owner.pitch);
      this.refreshPosition();
      this.setVelocity(Vec3d.ZERO);
      directionX += this.random.nextGaussian() * 0.4D;
      directionY += this.random.nextGaussian() * 0.4D;
      directionZ += this.random.nextGaussian() * 0.4D;
      double d = (double)MathHelper.sqrt(directionX * directionX + directionY * directionY + directionZ * directionZ);
      this.posX = directionX / d * 0.1D;
      this.posY = directionY / d * 0.1D;
      this.posZ = directionZ / d * 0.1D;
   }

   protected void initDataTracker() {
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldRender(double distance) {
      double d = this.getBoundingBox().getAverageSideLength() * 4.0D;
      if (Double.isNaN(d)) {
         d = 4.0D;
      }

      d *= 64.0D;
      return distance < d * d;
   }

   public void tick() {
      if (!this.world.isClient && (this.owner != null && this.owner.removed || !this.world.isChunkLoaded(new BlockPos(this)))) {
         this.remove();
      } else {
         super.tick();
         if (this.isBurning()) {
            this.setOnFireFor(1);
         }

         ++this.ticks;
         HitResult hitResult = ProjectileUtil.getCollision(this, true, this.ticks >= 25, this.owner, RayTraceContext.ShapeType.COLLIDER);
         if (hitResult.getType() != HitResult.Type.MISS) {
            this.onCollision(hitResult);
         }

         Vec3d vec3d = this.getVelocity();
         double d = this.getX() + vec3d.x;
         double e = this.getY() + vec3d.y;
         double f = this.getZ() + vec3d.z;
         ProjectileUtil.method_7484(this, 0.2F);
         float g = this.getDrag();
         if (this.isTouchingWater()) {
            for(int i = 0; i < 4; ++i) {
               float h = 0.25F;
               this.world.addParticle(ParticleTypes.BUBBLE, d - vec3d.x * 0.25D, e - vec3d.y * 0.25D, f - vec3d.z * 0.25D, vec3d.x, vec3d.y, vec3d.z);
            }

            g = 0.8F;
         }

         this.setVelocity(vec3d.add(this.posX, this.posY, this.posZ).multiply((double)g));
         this.world.addParticle(this.getParticleType(), d, e + 0.5D, f, 0.0D, 0.0D, 0.0D);
         this.updatePosition(d, e, f);
      }
   }

   protected boolean isBurning() {
      return true;
   }

   protected ParticleEffect getParticleType() {
      return ParticleTypes.SMOKE;
   }

   protected float getDrag() {
      return 0.95F;
   }

   protected void onCollision(HitResult hitResult) {
      HitResult.Type type = hitResult.getType();
      if (type == HitResult.Type.BLOCK) {
         BlockHitResult blockHitResult = (BlockHitResult)hitResult;
         BlockState blockState = this.world.getBlockState(blockHitResult.getBlockPos());
         blockState.onProjectileHit(this.world, blockState, blockHitResult, this);
      }

   }

   public void writeCustomDataToTag(CompoundTag tag) {
      Vec3d vec3d = this.getVelocity();
      tag.put("direction", this.toListTag(new double[]{vec3d.x, vec3d.y, vec3d.z}));
      tag.put("power", this.toListTag(new double[]{this.posX, this.posY, this.posZ}));
      tag.putInt("life", this.life);
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      ListTag listTag2;
      if (tag.contains("power", 9)) {
         listTag2 = tag.getList("power", 6);
         if (listTag2.size() == 3) {
            this.posX = listTag2.getDouble(0);
            this.posY = listTag2.getDouble(1);
            this.posZ = listTag2.getDouble(2);
         }
      }

      this.life = tag.getInt("life");
      if (tag.contains("direction", 9) && tag.getList("direction", 6).size() == 3) {
         listTag2 = tag.getList("direction", 6);
         this.setVelocity(listTag2.getDouble(0), listTag2.getDouble(1), listTag2.getDouble(2));
      } else {
         this.remove();
      }

   }

   public boolean collides() {
      return true;
   }

   public float getTargetingMargin() {
      return 1.0F;
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else {
         this.scheduleVelocityUpdate();
         if (source.getAttacker() != null) {
            Vec3d vec3d = source.getAttacker().getRotationVector();
            this.setVelocity(vec3d);
            this.posX = vec3d.x * 0.1D;
            this.posY = vec3d.y * 0.1D;
            this.posZ = vec3d.z * 0.1D;
            if (source.getAttacker() instanceof LivingEntity) {
               this.owner = (LivingEntity)source.getAttacker();
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public float getBrightnessAtEyes() {
      return 1.0F;
   }

   public Packet<?> createSpawnPacket() {
      int i = this.owner == null ? 0 : this.owner.getEntityId();
      return new EntitySpawnS2CPacket(this.getEntityId(), this.getUuid(), this.getX(), this.getY(), this.getZ(), this.pitch, this.yaw, this.getType(), i, new Vec3d(this.posX, this.posY, this.posZ));
   }
}
