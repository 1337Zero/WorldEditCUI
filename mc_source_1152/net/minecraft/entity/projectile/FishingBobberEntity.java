package net.minecraft.entity.projectile;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criterions;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.packet.EntitySpawnS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ProjectileUtil;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;

public class FishingBobberEntity extends Entity {
   private static final TrackedData<Integer> HOOK_ENTITY_ID;
   private boolean stuckOnBlock;
   private int removalTimer;
   private final PlayerEntity owner;
   private int selfHitTimer;
   private int hookCountdown;
   private int waitCountdown;
   private int fishTravelCountdown;
   private float fishAngle;
   public Entity hookedEntity;
   private FishingBobberEntity.State state;
   private final int luckOfTheSeaLevel;
   private final int lureLevel;

   private FishingBobberEntity(World world, PlayerEntity owner, int lureLevel, int luckOfTheSeaLevel) {
      super(EntityType.FISHING_BOBBER, world);
      this.state = FishingBobberEntity.State.FLYING;
      this.ignoreCameraFrustum = true;
      this.owner = owner;
      this.owner.fishHook = this;
      this.luckOfTheSeaLevel = Math.max(0, lureLevel);
      this.lureLevel = Math.max(0, luckOfTheSeaLevel);
   }

   @Environment(EnvType.CLIENT)
   public FishingBobberEntity(World world, PlayerEntity thrower, double x, double y, double z) {
      this((World)world, (PlayerEntity)thrower, 0, 0);
      this.updatePosition(x, y, z);
      this.prevX = this.getX();
      this.prevY = this.getY();
      this.prevZ = this.getZ();
   }

   public FishingBobberEntity(PlayerEntity thrower, World world, int lureLevel, int luckOfTheSeaLevel) {
      this(world, thrower, lureLevel, luckOfTheSeaLevel);
      float f = this.owner.pitch;
      float g = this.owner.yaw;
      float h = MathHelper.cos(-g * 0.017453292F - 3.1415927F);
      float i = MathHelper.sin(-g * 0.017453292F - 3.1415927F);
      float j = -MathHelper.cos(-f * 0.017453292F);
      float k = MathHelper.sin(-f * 0.017453292F);
      double d = this.owner.getX() - (double)i * 0.3D;
      double e = this.owner.getEyeY();
      double l = this.owner.getZ() - (double)h * 0.3D;
      this.refreshPositionAndAngles(d, e, l, g, f);
      Vec3d vec3d = new Vec3d((double)(-i), (double)MathHelper.clamp(-(k / j), -5.0F, 5.0F), (double)(-h));
      double m = vec3d.length();
      vec3d = vec3d.multiply(0.6D / m + 0.5D + this.random.nextGaussian() * 0.0045D, 0.6D / m + 0.5D + this.random.nextGaussian() * 0.0045D, 0.6D / m + 0.5D + this.random.nextGaussian() * 0.0045D);
      this.setVelocity(vec3d);
      this.yaw = (float)(MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875D);
      this.pitch = (float)(MathHelper.atan2(vec3d.y, (double)MathHelper.sqrt(squaredHorizontalLength(vec3d))) * 57.2957763671875D);
      this.prevYaw = this.yaw;
      this.prevPitch = this.pitch;
   }

   protected void initDataTracker() {
      this.getDataTracker().startTracking(HOOK_ENTITY_ID, 0);
   }

   public void onTrackedDataSet(TrackedData<?> data) {
      if (HOOK_ENTITY_ID.equals(data)) {
         int i = (Integer)this.getDataTracker().get(HOOK_ENTITY_ID);
         this.hookedEntity = i > 0 ? this.world.getEntityById(i - 1) : null;
      }

      super.onTrackedDataSet(data);
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldRender(double distance) {
      double d = 64.0D;
      return distance < 4096.0D;
   }

   @Environment(EnvType.CLIENT)
   public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
   }

   public void tick() {
      super.tick();
      if (this.owner == null) {
         this.remove();
      } else if (this.world.isClient || !this.removeIfInvalid()) {
         if (this.stuckOnBlock) {
            ++this.removalTimer;
            if (this.removalTimer >= 1200) {
               this.remove();
               return;
            }
         }

         float f = 0.0F;
         BlockPos blockPos = new BlockPos(this);
         FluidState fluidState = this.world.getFluidState(blockPos);
         if (fluidState.matches(FluidTags.WATER)) {
            f = fluidState.getHeight(this.world, blockPos);
         }

         if (this.state == FishingBobberEntity.State.FLYING) {
            if (this.hookedEntity != null) {
               this.setVelocity(Vec3d.ZERO);
               this.state = FishingBobberEntity.State.HOOKED_IN_ENTITY;
               return;
            }

            if (f > 0.0F) {
               this.setVelocity(this.getVelocity().multiply(0.3D, 0.2D, 0.3D));
               this.state = FishingBobberEntity.State.BOBBING;
               return;
            }

            if (!this.world.isClient) {
               this.checkForCollision();
            }

            if (!this.stuckOnBlock && !this.onGround && !this.horizontalCollision) {
               ++this.selfHitTimer;
            } else {
               this.selfHitTimer = 0;
               this.setVelocity(Vec3d.ZERO);
            }
         } else {
            if (this.state == FishingBobberEntity.State.HOOKED_IN_ENTITY) {
               if (this.hookedEntity != null) {
                  if (this.hookedEntity.removed) {
                     this.hookedEntity = null;
                     this.state = FishingBobberEntity.State.FLYING;
                  } else {
                     this.updatePosition(this.hookedEntity.getX(), this.hookedEntity.getBodyY(0.8D), this.hookedEntity.getZ());
                  }
               }

               return;
            }

            if (this.state == FishingBobberEntity.State.BOBBING) {
               Vec3d vec3d = this.getVelocity();
               double d = this.getY() + vec3d.y - (double)blockPos.getY() - (double)f;
               if (Math.abs(d) < 0.01D) {
                  d += Math.signum(d) * 0.1D;
               }

               this.setVelocity(vec3d.x * 0.9D, vec3d.y - d * (double)this.random.nextFloat() * 0.2D, vec3d.z * 0.9D);
               if (!this.world.isClient && f > 0.0F) {
                  this.tickFishingLogic(blockPos);
               }
            }
         }

         if (!fluidState.matches(FluidTags.WATER)) {
            this.setVelocity(this.getVelocity().add(0.0D, -0.03D, 0.0D));
         }

         this.move(MovementType.SELF, this.getVelocity());
         this.smoothenMovement();
         double e = 0.92D;
         this.setVelocity(this.getVelocity().multiply(0.92D));
         this.refreshPosition();
      }
   }

   private boolean removeIfInvalid() {
      ItemStack itemStack = this.owner.getMainHandStack();
      ItemStack itemStack2 = this.owner.getOffHandStack();
      boolean bl = itemStack.getItem() == Items.FISHING_ROD;
      boolean bl2 = itemStack2.getItem() == Items.FISHING_ROD;
      if (!this.owner.removed && this.owner.isAlive() && (bl || bl2) && this.squaredDistanceTo(this.owner) <= 1024.0D) {
         return false;
      } else {
         this.remove();
         return true;
      }
   }

   private void smoothenMovement() {
      Vec3d vec3d = this.getVelocity();
      float f = MathHelper.sqrt(squaredHorizontalLength(vec3d));
      this.yaw = (float)(MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875D);

      for(this.pitch = (float)(MathHelper.atan2(vec3d.y, (double)f) * 57.2957763671875D); this.pitch - this.prevPitch < -180.0F; this.prevPitch -= 360.0F) {
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
   }

   private void checkForCollision() {
      HitResult hitResult = ProjectileUtil.getCollision(this, this.getBoundingBox().stretch(this.getVelocity()).expand(1.0D), (entity) -> {
         return !entity.isSpectator() && (entity.collides() || entity instanceof ItemEntity) && (entity != this.owner || this.selfHitTimer >= 5);
      }, RayTraceContext.ShapeType.COLLIDER, true);
      if (hitResult.getType() != HitResult.Type.MISS) {
         if (hitResult.getType() == HitResult.Type.ENTITY) {
            this.hookedEntity = ((EntityHitResult)hitResult).getEntity();
            this.updateHookedEntityId();
         } else {
            this.stuckOnBlock = true;
         }
      }

   }

   private void updateHookedEntityId() {
      this.getDataTracker().set(HOOK_ENTITY_ID, this.hookedEntity.getEntityId() + 1);
   }

   private void tickFishingLogic(BlockPos pos) {
      ServerWorld serverWorld = (ServerWorld)this.world;
      int i = 1;
      BlockPos blockPos = pos.up();
      if (this.random.nextFloat() < 0.25F && this.world.hasRain(blockPos)) {
         ++i;
      }

      if (this.random.nextFloat() < 0.5F && !this.world.isSkyVisible(blockPos)) {
         --i;
      }

      if (this.hookCountdown > 0) {
         --this.hookCountdown;
         if (this.hookCountdown <= 0) {
            this.waitCountdown = 0;
            this.fishTravelCountdown = 0;
         } else {
            this.setVelocity(this.getVelocity().add(0.0D, -0.2D * (double)this.random.nextFloat() * (double)this.random.nextFloat(), 0.0D));
         }
      } else {
         float n;
         float o;
         float p;
         double q;
         double r;
         double s;
         Block block2;
         if (this.fishTravelCountdown > 0) {
            this.fishTravelCountdown -= i;
            if (this.fishTravelCountdown > 0) {
               this.fishAngle = (float)((double)this.fishAngle + this.random.nextGaussian() * 4.0D);
               n = this.fishAngle * 0.017453292F;
               o = MathHelper.sin(n);
               p = MathHelper.cos(n);
               q = this.getX() + (double)(o * (float)this.fishTravelCountdown * 0.1F);
               r = (double)((float)MathHelper.floor(this.getY()) + 1.0F);
               s = this.getZ() + (double)(p * (float)this.fishTravelCountdown * 0.1F);
               block2 = serverWorld.getBlockState(new BlockPos(q, r - 1.0D, s)).getBlock();
               if (block2 == Blocks.WATER) {
                  if (this.random.nextFloat() < 0.15F) {
                     serverWorld.spawnParticles(ParticleTypes.BUBBLE, q, r - 0.10000000149011612D, s, 1, (double)o, 0.1D, (double)p, 0.0D);
                  }

                  float k = o * 0.04F;
                  float l = p * 0.04F;
                  serverWorld.spawnParticles(ParticleTypes.FISHING, q, r, s, 0, (double)l, 0.01D, (double)(-k), 1.0D);
                  serverWorld.spawnParticles(ParticleTypes.FISHING, q, r, s, 0, (double)(-l), 0.01D, (double)k, 1.0D);
               }
            } else {
               Vec3d vec3d = this.getVelocity();
               this.setVelocity(vec3d.x, (double)(-0.4F * MathHelper.nextFloat(this.random, 0.6F, 1.0F)), vec3d.z);
               this.playSound(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
               double m = this.getY() + 0.5D;
               serverWorld.spawnParticles(ParticleTypes.BUBBLE, this.getX(), m, this.getZ(), (int)(1.0F + this.getWidth() * 20.0F), (double)this.getWidth(), 0.0D, (double)this.getWidth(), 0.20000000298023224D);
               serverWorld.spawnParticles(ParticleTypes.FISHING, this.getX(), m, this.getZ(), (int)(1.0F + this.getWidth() * 20.0F), (double)this.getWidth(), 0.0D, (double)this.getWidth(), 0.20000000298023224D);
               this.hookCountdown = MathHelper.nextInt(this.random, 20, 40);
            }
         } else if (this.waitCountdown > 0) {
            this.waitCountdown -= i;
            n = 0.15F;
            if (this.waitCountdown < 20) {
               n = (float)((double)n + (double)(20 - this.waitCountdown) * 0.05D);
            } else if (this.waitCountdown < 40) {
               n = (float)((double)n + (double)(40 - this.waitCountdown) * 0.02D);
            } else if (this.waitCountdown < 60) {
               n = (float)((double)n + (double)(60 - this.waitCountdown) * 0.01D);
            }

            if (this.random.nextFloat() < n) {
               o = MathHelper.nextFloat(this.random, 0.0F, 360.0F) * 0.017453292F;
               p = MathHelper.nextFloat(this.random, 25.0F, 60.0F);
               q = this.getX() + (double)(MathHelper.sin(o) * p * 0.1F);
               r = (double)((float)MathHelper.floor(this.getY()) + 1.0F);
               s = this.getZ() + (double)(MathHelper.cos(o) * p * 0.1F);
               block2 = serverWorld.getBlockState(new BlockPos(q, r - 1.0D, s)).getBlock();
               if (block2 == Blocks.WATER) {
                  serverWorld.spawnParticles(ParticleTypes.SPLASH, q, r, s, 2 + this.random.nextInt(2), 0.10000000149011612D, 0.0D, 0.10000000149011612D, 0.0D);
               }
            }

            if (this.waitCountdown <= 0) {
               this.fishAngle = MathHelper.nextFloat(this.random, 0.0F, 360.0F);
               this.fishTravelCountdown = MathHelper.nextInt(this.random, 20, 80);
            }
         } else {
            this.waitCountdown = MathHelper.nextInt(this.random, 100, 600);
            this.waitCountdown -= this.lureLevel * 20 * 5;
         }
      }

   }

   public void writeCustomDataToTag(CompoundTag tag) {
   }

   public void readCustomDataFromTag(CompoundTag tag) {
   }

   public int use(ItemStack usedItem) {
      if (!this.world.isClient && this.owner != null) {
         int i = 0;
         if (this.hookedEntity != null) {
            this.pullHookedEntity();
            Criterions.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity)this.owner, usedItem, this, Collections.emptyList());
            this.world.sendEntityStatus(this, (byte)31);
            i = this.hookedEntity instanceof ItemEntity ? 3 : 5;
         } else if (this.hookCountdown > 0) {
            LootContext.Builder builder = (new LootContext.Builder((ServerWorld)this.world)).put(LootContextParameters.POSITION, new BlockPos(this)).put(LootContextParameters.TOOL, usedItem).setRandom(this.random).setLuck((float)this.luckOfTheSeaLevel + this.owner.getLuck());
            LootTable lootTable = this.world.getServer().getLootManager().getSupplier(LootTables.FISHING_GAMEPLAY);
            List<ItemStack> list = lootTable.getDrops(builder.build(LootContextTypes.FISHING));
            Criterions.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity)this.owner, usedItem, this, list);
            Iterator var6 = list.iterator();

            while(var6.hasNext()) {
               ItemStack itemStack = (ItemStack)var6.next();
               ItemEntity itemEntity = new ItemEntity(this.world, this.getX(), this.getY(), this.getZ(), itemStack);
               double d = this.owner.getX() - this.getX();
               double e = this.owner.getY() - this.getY();
               double f = this.owner.getZ() - this.getZ();
               double g = 0.1D;
               itemEntity.setVelocity(d * 0.1D, e * 0.1D + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08D, f * 0.1D);
               this.world.spawnEntity(itemEntity);
               this.owner.world.spawnEntity(new ExperienceOrbEntity(this.owner.world, this.owner.getX(), this.owner.getY() + 0.5D, this.owner.getZ() + 0.5D, this.random.nextInt(6) + 1));
               if (itemStack.getItem().isIn(ItemTags.FISHES)) {
                  this.owner.increaseStat((Identifier)Stats.FISH_CAUGHT, 1);
               }
            }

            i = 1;
         }

         if (this.stuckOnBlock) {
            i = 2;
         }

         this.remove();
         return i;
      } else {
         return 0;
      }
   }

   @Environment(EnvType.CLIENT)
   public void handleStatus(byte status) {
      if (status == 31 && this.world.isClient && this.hookedEntity instanceof PlayerEntity && ((PlayerEntity)this.hookedEntity).isMainPlayer()) {
         this.pullHookedEntity();
      }

      super.handleStatus(status);
   }

   protected void pullHookedEntity() {
      if (this.owner != null) {
         Vec3d vec3d = (new Vec3d(this.owner.getX() - this.getX(), this.owner.getY() - this.getY(), this.owner.getZ() - this.getZ())).multiply(0.1D);
         this.hookedEntity.setVelocity(this.hookedEntity.getVelocity().add(vec3d));
      }
   }

   protected boolean canClimb() {
      return false;
   }

   public void remove() {
      super.remove();
      if (this.owner != null) {
         this.owner.fishHook = null;
      }

   }

   @Nullable
   public PlayerEntity getOwner() {
      return this.owner;
   }

   public boolean canUsePortals() {
      return false;
   }

   public Packet<?> createSpawnPacket() {
      Entity entity = this.getOwner();
      return new EntitySpawnS2CPacket(this, entity == null ? this.getEntityId() : entity.getEntityId());
   }

   static {
      HOOK_ENTITY_ID = DataTracker.registerData(FishingBobberEntity.class, TrackedDataHandlerRegistry.INTEGER);
   }

   static enum State {
      FLYING,
      HOOKED_IN_ENTITY,
      BOBBING;
   }
}
