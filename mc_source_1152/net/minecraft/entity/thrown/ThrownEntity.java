package net.minecraft.entity.thrown;

import java.util.Iterator;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.packet.EntitySpawnS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ProjectileUtil;
import net.minecraft.entity.projectile.Projectile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.Packet;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;

public abstract class ThrownEntity extends Entity implements Projectile {
   private int blockX;
   private int blockY;
   private int blockZ;
   protected boolean inGround;
   public int shake;
   protected LivingEntity owner;
   private UUID ownerUuid;
   private Entity field_7637;
   private int field_7638;

   protected ThrownEntity(EntityType<? extends ThrownEntity> type, World world) {
      super(type, world);
      this.blockX = -1;
      this.blockY = -1;
      this.blockZ = -1;
   }

   protected ThrownEntity(EntityType<? extends ThrownEntity> type, double x, double y, double z, World world) {
      this(type, world);
      this.updatePosition(x, y, z);
   }

   protected ThrownEntity(EntityType<? extends ThrownEntity> type, LivingEntity owner, World world) {
      this(type, owner.getX(), owner.getEyeY() - 0.10000000149011612D, owner.getZ(), world);
      this.owner = owner;
      this.ownerUuid = owner.getUuid();
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

   public void setProperties(Entity user, float pitch, float yaw, float f, float g, float h) {
      float i = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
      float j = -MathHelper.sin((pitch + f) * 0.017453292F);
      float k = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
      this.setVelocity((double)i, (double)j, (double)k, g, h);
      Vec3d vec3d = user.getVelocity();
      this.setVelocity(this.getVelocity().add(vec3d.x, user.onGround ? 0.0D : vec3d.y, vec3d.z));
   }

   public void setVelocity(double x, double y, double z, float speed, float divergence) {
      Vec3d vec3d = (new Vec3d(x, y, z)).normalize().add(this.random.nextGaussian() * 0.007499999832361937D * (double)divergence, this.random.nextGaussian() * 0.007499999832361937D * (double)divergence, this.random.nextGaussian() * 0.007499999832361937D * (double)divergence).multiply((double)speed);
      this.setVelocity(vec3d);
      float f = MathHelper.sqrt(squaredHorizontalLength(vec3d));
      this.yaw = (float)(MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875D);
      this.pitch = (float)(MathHelper.atan2(vec3d.y, (double)f) * 57.2957763671875D);
      this.prevYaw = this.yaw;
      this.prevPitch = this.pitch;
   }

   @Environment(EnvType.CLIENT)
   public void setVelocityClient(double x, double y, double z) {
      this.setVelocity(x, y, z);
      if (this.prevPitch == 0.0F && this.prevYaw == 0.0F) {
         float f = MathHelper.sqrt(x * x + z * z);
         this.yaw = (float)(MathHelper.atan2(x, z) * 57.2957763671875D);
         this.pitch = (float)(MathHelper.atan2(y, (double)f) * 57.2957763671875D);
         this.prevYaw = this.yaw;
         this.prevPitch = this.pitch;
      }

   }

   public void tick() {
      super.tick();
      if (this.shake > 0) {
         --this.shake;
      }

      if (this.inGround) {
         this.inGround = false;
         this.setVelocity(this.getVelocity().multiply((double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F)));
      }

      Box box = this.getBoundingBox().stretch(this.getVelocity()).expand(1.0D);
      Iterator var2 = this.world.getEntities((Entity)this, box, (entityx) -> {
         return !entityx.isSpectator() && entityx.collides();
      }).iterator();

      while(var2.hasNext()) {
         Entity entity = (Entity)var2.next();
         if (entity == this.field_7637) {
            ++this.field_7638;
            break;
         }

         if (this.owner != null && this.age < 2 && this.field_7637 == null) {
            this.field_7637 = entity;
            this.field_7638 = 3;
            break;
         }
      }

      HitResult hitResult = ProjectileUtil.getCollision(this, box, (entityx) -> {
         return !entityx.isSpectator() && entityx.collides() && entityx != this.field_7637;
      }, RayTraceContext.ShapeType.OUTLINE, true);
      if (this.field_7637 != null && this.field_7638-- <= 0) {
         this.field_7637 = null;
      }

      if (hitResult.getType() != HitResult.Type.MISS) {
         if (hitResult.getType() == HitResult.Type.BLOCK && this.world.getBlockState(((BlockHitResult)hitResult).getBlockPos()).getBlock() == Blocks.NETHER_PORTAL) {
            this.setInNetherPortal(((BlockHitResult)hitResult).getBlockPos());
         } else {
            this.onCollision(hitResult);
         }
      }

      Vec3d vec3d = this.getVelocity();
      double d = this.getX() + vec3d.x;
      double e = this.getY() + vec3d.y;
      double f = this.getZ() + vec3d.z;
      float g = MathHelper.sqrt(squaredHorizontalLength(vec3d));
      this.yaw = (float)(MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875D);

      for(this.pitch = (float)(MathHelper.atan2(vec3d.y, (double)g) * 57.2957763671875D); this.pitch - this.prevPitch < -180.0F; this.prevPitch -= 360.0F) {
      }

      while(this.pitch - this.prevPitch >= 180.0F) {
         this.prevPitch += 360.0F;
      }

      while(this.yaw - this.prevYaw < -180.0F) {
         this.prevYaw -= 360.0F;
      }

      while(this.yaw - this.prevYaw >= 180.0F) {
         this.prevYaw += 360.0F;
      }

      this.pitch = MathHelper.lerp(0.2F, this.prevPitch, this.pitch);
      this.yaw = MathHelper.lerp(0.2F, this.prevYaw, this.yaw);
      float k;
      if (this.isTouchingWater()) {
         for(int i = 0; i < 4; ++i) {
            float h = 0.25F;
            this.world.addParticle(ParticleTypes.BUBBLE, d - vec3d.x * 0.25D, e - vec3d.y * 0.25D, f - vec3d.z * 0.25D, vec3d.x, vec3d.y, vec3d.z);
         }

         k = 0.8F;
      } else {
         k = 0.99F;
      }

      this.setVelocity(vec3d.multiply((double)k));
      if (!this.hasNoGravity()) {
         Vec3d vec3d2 = this.getVelocity();
         this.setVelocity(vec3d2.x, vec3d2.y - (double)this.getGravity(), vec3d2.z);
      }

      this.updatePosition(d, e, f);
   }

   protected float getGravity() {
      return 0.03F;
   }

   protected abstract void onCollision(HitResult hitResult);

   public void writeCustomDataToTag(CompoundTag tag) {
      tag.putInt("xTile", this.blockX);
      tag.putInt("yTile", this.blockY);
      tag.putInt("zTile", this.blockZ);
      tag.putByte("shake", (byte)this.shake);
      tag.putBoolean("inGround", this.inGround);
      if (this.ownerUuid != null) {
         tag.put("owner", NbtHelper.fromUuid(this.ownerUuid));
      }

   }

   public void readCustomDataFromTag(CompoundTag tag) {
      this.blockX = tag.getInt("xTile");
      this.blockY = tag.getInt("yTile");
      this.blockZ = tag.getInt("zTile");
      this.shake = tag.getByte("shake") & 255;
      this.inGround = tag.getBoolean("inGround");
      this.owner = null;
      if (tag.contains("owner", 10)) {
         this.ownerUuid = NbtHelper.toUuid(tag.getCompound("owner"));
      }

   }

   @Nullable
   public LivingEntity getOwner() {
      if ((this.owner == null || this.owner.removed) && this.ownerUuid != null && this.world instanceof ServerWorld) {
         Entity entity = ((ServerWorld)this.world).getEntity(this.ownerUuid);
         if (entity instanceof LivingEntity) {
            this.owner = (LivingEntity)entity;
         } else {
            this.owner = null;
         }
      }

      return this.owner;
   }

   public Packet<?> createSpawnPacket() {
      return new EntitySpawnS2CPacket(this);
   }
}
