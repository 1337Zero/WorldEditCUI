package net.minecraft.entity.vehicle;

import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.client.network.packet.EntitySpawnS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.packet.BoatPaddleStateC2SPacket;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.BooleanBiFunction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class BoatEntity extends Entity {
   private static final TrackedData<Integer> DAMAGE_WOBBLE_TICKS;
   private static final TrackedData<Integer> DAMAGE_WOBBLE_SIDE;
   private static final TrackedData<Float> DAMAGE_WOBBLE_STRENGTH;
   private static final TrackedData<Integer> BOAT_TYPE;
   private static final TrackedData<Boolean> LEFT_PADDLE_MOVING;
   private static final TrackedData<Boolean> RIGHT_PADDLE_MOVING;
   private static final TrackedData<Integer> BUBBLE_WOBBLE_TICKS;
   private final float[] paddlePhases;
   private float velocityDecay;
   private float ticksUnderwater;
   private float yawVelocity;
   private int field_7708;
   private double field_7686;
   private double field_7700;
   private double field_7685;
   private double field_7699;
   private double field_7684;
   private boolean pressingLeft;
   private boolean pressingRight;
   private boolean pressingForward;
   private boolean pressingBack;
   private double waterLevel;
   private float field_7714;
   private BoatEntity.Location location;
   private BoatEntity.Location lastLocation;
   private double fallVelocity;
   private boolean onBubbleColumnSurface;
   private boolean bubbleColumnIsDrag;
   private float bubbleWobbleStrength;
   private float bubbleWobble;
   private float lastBubbleWobble;

   public BoatEntity(EntityType<? extends BoatEntity> entityType, World world) {
      super(entityType, world);
      this.paddlePhases = new float[2];
      this.inanimate = true;
   }

   public BoatEntity(World world, double x, double y, double z) {
      this(EntityType.BOAT, world);
      this.updatePosition(x, y, z);
      this.setVelocity(Vec3d.ZERO);
      this.prevX = x;
      this.prevY = y;
      this.prevZ = z;
   }

   protected boolean canClimb() {
      return false;
   }

   protected void initDataTracker() {
      this.dataTracker.startTracking(DAMAGE_WOBBLE_TICKS, 0);
      this.dataTracker.startTracking(DAMAGE_WOBBLE_SIDE, 1);
      this.dataTracker.startTracking(DAMAGE_WOBBLE_STRENGTH, 0.0F);
      this.dataTracker.startTracking(BOAT_TYPE, BoatEntity.Type.OAK.ordinal());
      this.dataTracker.startTracking(LEFT_PADDLE_MOVING, false);
      this.dataTracker.startTracking(RIGHT_PADDLE_MOVING, false);
      this.dataTracker.startTracking(BUBBLE_WOBBLE_TICKS, 0);
   }

   @Nullable
   public Box getHardCollisionBox(Entity collidingEntity) {
      return collidingEntity.isPushable() ? collidingEntity.getBoundingBox() : null;
   }

   @Nullable
   public Box getCollisionBox() {
      return this.getBoundingBox();
   }

   public boolean isPushable() {
      return true;
   }

   public double getMountedHeightOffset() {
      return -0.1D;
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else if (!this.world.isClient && !this.removed) {
         if (source instanceof ProjectileDamageSource && source.getAttacker() != null && this.hasPassenger(source.getAttacker())) {
            return false;
         } else {
            this.setDamageWobbleSide(-this.getDamageWobbleSide());
            this.setDamageWobbleTicks(10);
            this.setDamageWobbleStrength(this.getDamageWobbleStrength() + amount * 10.0F);
            this.scheduleVelocityUpdate();
            boolean bl = source.getAttacker() instanceof PlayerEntity && ((PlayerEntity)source.getAttacker()).abilities.creativeMode;
            if (bl || this.getDamageWobbleStrength() > 40.0F) {
               if (!bl && this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                  this.dropItem(this.asItem());
               }

               this.remove();
            }

            return true;
         }
      } else {
         return true;
      }
   }

   public void onBubbleColumnSurfaceCollision(boolean drag) {
      if (!this.world.isClient) {
         this.onBubbleColumnSurface = true;
         this.bubbleColumnIsDrag = drag;
         if (this.getBubbleWobbleTicks() == 0) {
            this.setBubbleWobbleTicks(60);
         }
      }

      this.world.addParticle(ParticleTypes.SPLASH, this.getX() + (double)this.random.nextFloat(), this.getY() + 0.7D, this.getZ() + (double)this.random.nextFloat(), 0.0D, 0.0D, 0.0D);
      if (this.random.nextInt(20) == 0) {
         this.world.playSound(this.getX(), this.getY(), this.getZ(), this.getSplashSound(), this.getSoundCategory(), 1.0F, 0.8F + 0.4F * this.random.nextFloat(), false);
      }

   }

   public void pushAwayFrom(Entity entity) {
      if (entity instanceof BoatEntity) {
         if (entity.getBoundingBox().y1 < this.getBoundingBox().y2) {
            super.pushAwayFrom(entity);
         }
      } else if (entity.getBoundingBox().y1 <= this.getBoundingBox().y1) {
         super.pushAwayFrom(entity);
      }

   }

   public Item asItem() {
      switch(this.getBoatType()) {
      case OAK:
      default:
         return Items.OAK_BOAT;
      case SPRUCE:
         return Items.SPRUCE_BOAT;
      case BIRCH:
         return Items.BIRCH_BOAT;
      case JUNGLE:
         return Items.JUNGLE_BOAT;
      case ACACIA:
         return Items.ACACIA_BOAT;
      case DARK_OAK:
         return Items.DARK_OAK_BOAT;
      }
   }

   @Environment(EnvType.CLIENT)
   public void animateDamage() {
      this.setDamageWobbleSide(-this.getDamageWobbleSide());
      this.setDamageWobbleTicks(10);
      this.setDamageWobbleStrength(this.getDamageWobbleStrength() * 11.0F);
   }

   public boolean collides() {
      return !this.removed;
   }

   @Environment(EnvType.CLIENT)
   public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
      this.field_7686 = x;
      this.field_7700 = y;
      this.field_7685 = z;
      this.field_7699 = (double)yaw;
      this.field_7684 = (double)pitch;
      this.field_7708 = 10;
   }

   public Direction getMovementDirection() {
      return this.getHorizontalFacing().rotateYClockwise();
   }

   public void tick() {
      this.lastLocation = this.location;
      this.location = this.checkLocation();
      if (this.location != BoatEntity.Location.UNDER_WATER && this.location != BoatEntity.Location.UNDER_FLOWING_WATER) {
         this.ticksUnderwater = 0.0F;
      } else {
         ++this.ticksUnderwater;
      }

      if (!this.world.isClient && this.ticksUnderwater >= 60.0F) {
         this.removeAllPassengers();
      }

      if (this.getDamageWobbleTicks() > 0) {
         this.setDamageWobbleTicks(this.getDamageWobbleTicks() - 1);
      }

      if (this.getDamageWobbleStrength() > 0.0F) {
         this.setDamageWobbleStrength(this.getDamageWobbleStrength() - 1.0F);
      }

      super.tick();
      this.method_7555();
      if (this.isLogicalSideForUpdatingMovement()) {
         if (this.getPassengerList().isEmpty() || !(this.getPassengerList().get(0) instanceof PlayerEntity)) {
            this.setPaddleMovings(false, false);
         }

         this.updateVelocity();
         if (this.world.isClient) {
            this.updatePaddles();
            this.world.sendPacket(new BoatPaddleStateC2SPacket(this.isPaddleMoving(0), this.isPaddleMoving(1)));
         }

         this.move(MovementType.SELF, this.getVelocity());
      } else {
         this.setVelocity(Vec3d.ZERO);
      }

      this.handleBubbleColumn();

      for(int i = 0; i <= 1; ++i) {
         if (this.isPaddleMoving(i)) {
            if (!this.isSilent() && (double)(this.paddlePhases[i] % 6.2831855F) <= 0.7853981852531433D && ((double)this.paddlePhases[i] + 0.39269909262657166D) % 6.2831854820251465D >= 0.7853981852531433D) {
               SoundEvent soundEvent = this.getPaddleSoundEvent();
               if (soundEvent != null) {
                  Vec3d vec3d = this.getRotationVec(1.0F);
                  double d = i == 1 ? -vec3d.z : vec3d.z;
                  double e = i == 1 ? vec3d.x : -vec3d.x;
                  this.world.playSound((PlayerEntity)null, this.getX() + d, this.getY(), this.getZ() + e, soundEvent, this.getSoundCategory(), 1.0F, 0.8F + 0.4F * this.random.nextFloat());
               }
            }

            float[] var10000 = this.paddlePhases;
            var10000[i] = (float)((double)var10000[i] + 0.39269909262657166D);
         } else {
            this.paddlePhases[i] = 0.0F;
         }
      }

      this.checkBlockCollision();
      List<Entity> list = this.world.getEntities((Entity)this, this.getBoundingBox().expand(0.20000000298023224D, -0.009999999776482582D, 0.20000000298023224D), EntityPredicates.canBePushedBy(this));
      if (!list.isEmpty()) {
         boolean bl = !this.world.isClient && !(this.getPrimaryPassenger() instanceof PlayerEntity);

         for(int j = 0; j < list.size(); ++j) {
            Entity entity = (Entity)list.get(j);
            if (!entity.hasPassenger(this)) {
               if (bl && this.getPassengerList().size() < 2 && !entity.hasVehicle() && entity.getWidth() < this.getWidth() && entity instanceof LivingEntity && !(entity instanceof WaterCreatureEntity) && !(entity instanceof PlayerEntity)) {
                  entity.startRiding(this);
               } else {
                  this.pushAwayFrom(entity);
               }
            }
         }
      }

   }

   private void handleBubbleColumn() {
      int j;
      if (this.world.isClient) {
         j = this.getBubbleWobbleTicks();
         if (j > 0) {
            this.bubbleWobbleStrength += 0.05F;
         } else {
            this.bubbleWobbleStrength -= 0.1F;
         }

         this.bubbleWobbleStrength = MathHelper.clamp(this.bubbleWobbleStrength, 0.0F, 1.0F);
         this.lastBubbleWobble = this.bubbleWobble;
         this.bubbleWobble = 10.0F * (float)Math.sin((double)(0.5F * (float)this.world.getTime())) * this.bubbleWobbleStrength;
      } else {
         if (!this.onBubbleColumnSurface) {
            this.setBubbleWobbleTicks(0);
         }

         j = this.getBubbleWobbleTicks();
         if (j > 0) {
            --j;
            this.setBubbleWobbleTicks(j);
            int k = 60 - j - 1;
            if (k > 0 && j == 0) {
               this.setBubbleWobbleTicks(0);
               Vec3d vec3d = this.getVelocity();
               if (this.bubbleColumnIsDrag) {
                  this.setVelocity(vec3d.add(0.0D, -0.7D, 0.0D));
                  this.removeAllPassengers();
               } else {
                  this.setVelocity(vec3d.x, this.hasPassengerType(PlayerEntity.class) ? 2.7D : 0.6D, vec3d.z);
               }
            }

            this.onBubbleColumnSurface = false;
         }
      }

   }

   @Nullable
   protected SoundEvent getPaddleSoundEvent() {
      switch(this.checkLocation()) {
      case IN_WATER:
      case UNDER_WATER:
      case UNDER_FLOWING_WATER:
         return SoundEvents.ENTITY_BOAT_PADDLE_WATER;
      case ON_LAND:
         return SoundEvents.ENTITY_BOAT_PADDLE_LAND;
      case IN_AIR:
      default:
         return null;
      }
   }

   private void method_7555() {
      if (this.isLogicalSideForUpdatingMovement()) {
         this.field_7708 = 0;
         this.updateTrackedPosition(this.getX(), this.getY(), this.getZ());
      }

      if (this.field_7708 > 0) {
         double d = this.getX() + (this.field_7686 - this.getX()) / (double)this.field_7708;
         double e = this.getY() + (this.field_7700 - this.getY()) / (double)this.field_7708;
         double f = this.getZ() + (this.field_7685 - this.getZ()) / (double)this.field_7708;
         double g = MathHelper.wrapDegrees(this.field_7699 - (double)this.yaw);
         this.yaw = (float)((double)this.yaw + g / (double)this.field_7708);
         this.pitch = (float)((double)this.pitch + (this.field_7684 - (double)this.pitch) / (double)this.field_7708);
         --this.field_7708;
         this.updatePosition(d, e, f);
         this.setRotation(this.yaw, this.pitch);
      }
   }

   public void setPaddleMovings(boolean leftMoving, boolean rightMoving) {
      this.dataTracker.set(LEFT_PADDLE_MOVING, leftMoving);
      this.dataTracker.set(RIGHT_PADDLE_MOVING, rightMoving);
   }

   @Environment(EnvType.CLIENT)
   public float interpolatePaddlePhase(int paddle, float tickDelta) {
      return this.isPaddleMoving(paddle) ? (float)MathHelper.clampedLerp((double)this.paddlePhases[paddle] - 0.39269909262657166D, (double)this.paddlePhases[paddle], (double)tickDelta) : 0.0F;
   }

   private BoatEntity.Location checkLocation() {
      BoatEntity.Location location = this.getUnderWaterLocation();
      if (location != null) {
         this.waterLevel = this.getBoundingBox().y2;
         return location;
      } else if (this.checkBoatInWater()) {
         return BoatEntity.Location.IN_WATER;
      } else {
         float f = this.method_7548();
         if (f > 0.0F) {
            this.field_7714 = f;
            return BoatEntity.Location.ON_LAND;
         } else {
            return BoatEntity.Location.IN_AIR;
         }
      }
   }

   public float method_7544() {
      Box box = this.getBoundingBox();
      int i = MathHelper.floor(box.x1);
      int j = MathHelper.ceil(box.x2);
      int k = MathHelper.floor(box.y2);
      int l = MathHelper.ceil(box.y2 - this.fallVelocity);
      int m = MathHelper.floor(box.z1);
      int n = MathHelper.ceil(box.z2);
      BlockPos.PooledMutable pooledMutable = BlockPos.PooledMutable.get();
      Throwable var9 = null;

      try {
         label160:
         for(int o = k; o < l; ++o) {
            float f = 0.0F;

            for(int p = i; p < j; ++p) {
               for(int q = m; q < n; ++q) {
                  pooledMutable.set(p, o, q);
                  FluidState fluidState = this.world.getFluidState(pooledMutable);
                  if (fluidState.matches(FluidTags.WATER)) {
                     f = Math.max(f, fluidState.getHeight(this.world, pooledMutable));
                  }

                  if (f >= 1.0F) {
                     continue label160;
                  }
               }
            }

            if (f < 1.0F) {
               float var26 = (float)pooledMutable.getY() + f;
               return var26;
            }
         }

         float var25 = (float)(l + 1);
         return var25;
      } catch (Throwable var23) {
         var9 = var23;
         throw var23;
      } finally {
         if (pooledMutable != null) {
            if (var9 != null) {
               try {
                  pooledMutable.close();
               } catch (Throwable var22) {
                  var9.addSuppressed(var22);
               }
            } else {
               pooledMutable.close();
            }
         }

      }
   }

   public float method_7548() {
      Box box = this.getBoundingBox();
      Box box2 = new Box(box.x1, box.y1 - 0.001D, box.z1, box.x2, box.y1, box.z2);
      int i = MathHelper.floor(box2.x1) - 1;
      int j = MathHelper.ceil(box2.x2) + 1;
      int k = MathHelper.floor(box2.y1) - 1;
      int l = MathHelper.ceil(box2.y2) + 1;
      int m = MathHelper.floor(box2.z1) - 1;
      int n = MathHelper.ceil(box2.z2) + 1;
      VoxelShape voxelShape = VoxelShapes.cuboid(box2);
      float f = 0.0F;
      int o = 0;
      BlockPos.PooledMutable pooledMutable = BlockPos.PooledMutable.get();
      Throwable var13 = null;

      try {
         for(int p = i; p < j; ++p) {
            for(int q = m; q < n; ++q) {
               int r = (p != i && p != j - 1 ? 0 : 1) + (q != m && q != n - 1 ? 0 : 1);
               if (r != 2) {
                  for(int s = k; s < l; ++s) {
                     if (r <= 0 || s != k && s != l - 1) {
                        pooledMutable.set(p, s, q);
                        BlockState blockState = this.world.getBlockState(pooledMutable);
                        if (!(blockState.getBlock() instanceof LilyPadBlock) && VoxelShapes.matchesAnywhere(blockState.getCollisionShape(this.world, pooledMutable).offset((double)p, (double)s, (double)q), voxelShape, BooleanBiFunction.AND)) {
                           f += blockState.getBlock().getSlipperiness();
                           ++o;
                        }
                     }
                  }
               }
            }
         }
      } catch (Throwable var26) {
         var13 = var26;
         throw var26;
      } finally {
         if (pooledMutable != null) {
            if (var13 != null) {
               try {
                  pooledMutable.close();
               } catch (Throwable var25) {
                  var13.addSuppressed(var25);
               }
            } else {
               pooledMutable.close();
            }
         }

      }

      return f / (float)o;
   }

   private boolean checkBoatInWater() {
      Box box = this.getBoundingBox();
      int i = MathHelper.floor(box.x1);
      int j = MathHelper.ceil(box.x2);
      int k = MathHelper.floor(box.y1);
      int l = MathHelper.ceil(box.y1 + 0.001D);
      int m = MathHelper.floor(box.z1);
      int n = MathHelper.ceil(box.z2);
      boolean bl = false;
      this.waterLevel = Double.MIN_VALUE;
      BlockPos.PooledMutable pooledMutable = BlockPos.PooledMutable.get();
      Throwable var10 = null;

      try {
         for(int o = i; o < j; ++o) {
            for(int p = k; p < l; ++p) {
               for(int q = m; q < n; ++q) {
                  pooledMutable.set(o, p, q);
                  FluidState fluidState = this.world.getFluidState(pooledMutable);
                  if (fluidState.matches(FluidTags.WATER)) {
                     float f = (float)p + fluidState.getHeight(this.world, pooledMutable);
                     this.waterLevel = Math.max((double)f, this.waterLevel);
                     bl |= box.y1 < (double)f;
                  }
               }
            }
         }
      } catch (Throwable var23) {
         var10 = var23;
         throw var23;
      } finally {
         if (pooledMutable != null) {
            if (var10 != null) {
               try {
                  pooledMutable.close();
               } catch (Throwable var22) {
                  var10.addSuppressed(var22);
               }
            } else {
               pooledMutable.close();
            }
         }

      }

      return bl;
   }

   @Nullable
   private BoatEntity.Location getUnderWaterLocation() {
      Box box = this.getBoundingBox();
      double d = box.y2 + 0.001D;
      int i = MathHelper.floor(box.x1);
      int j = MathHelper.ceil(box.x2);
      int k = MathHelper.floor(box.y2);
      int l = MathHelper.ceil(d);
      int m = MathHelper.floor(box.z1);
      int n = MathHelper.ceil(box.z2);
      boolean bl = false;
      BlockPos.PooledMutable pooledMutable = BlockPos.PooledMutable.get();
      Throwable var12 = null;

      try {
         for(int o = i; o < j; ++o) {
            for(int p = k; p < l; ++p) {
               for(int q = m; q < n; ++q) {
                  pooledMutable.set(o, p, q);
                  FluidState fluidState = this.world.getFluidState(pooledMutable);
                  if (fluidState.matches(FluidTags.WATER) && d < (double)((float)pooledMutable.getY() + fluidState.getHeight(this.world, pooledMutable))) {
                     if (!fluidState.isStill()) {
                        BoatEntity.Location var17 = BoatEntity.Location.UNDER_FLOWING_WATER;
                        return var17;
                     }

                     bl = true;
                  }
               }
            }
         }
      } catch (Throwable var27) {
         var12 = var27;
         throw var27;
      } finally {
         if (pooledMutable != null) {
            if (var12 != null) {
               try {
                  pooledMutable.close();
               } catch (Throwable var26) {
                  var12.addSuppressed(var26);
               }
            } else {
               pooledMutable.close();
            }
         }

      }

      return bl ? BoatEntity.Location.UNDER_WATER : null;
   }

   private void updateVelocity() {
      double d = -0.03999999910593033D;
      double e = this.hasNoGravity() ? 0.0D : -0.03999999910593033D;
      double f = 0.0D;
      this.velocityDecay = 0.05F;
      if (this.lastLocation == BoatEntity.Location.IN_AIR && this.location != BoatEntity.Location.IN_AIR && this.location != BoatEntity.Location.ON_LAND) {
         this.waterLevel = this.getBodyY(1.0D);
         this.updatePosition(this.getX(), (double)(this.method_7544() - this.getHeight()) + 0.101D, this.getZ());
         this.setVelocity(this.getVelocity().multiply(1.0D, 0.0D, 1.0D));
         this.fallVelocity = 0.0D;
         this.location = BoatEntity.Location.IN_WATER;
      } else {
         if (this.location == BoatEntity.Location.IN_WATER) {
            f = (this.waterLevel - this.getY()) / (double)this.getHeight();
            this.velocityDecay = 0.9F;
         } else if (this.location == BoatEntity.Location.UNDER_FLOWING_WATER) {
            e = -7.0E-4D;
            this.velocityDecay = 0.9F;
         } else if (this.location == BoatEntity.Location.UNDER_WATER) {
            f = 0.009999999776482582D;
            this.velocityDecay = 0.45F;
         } else if (this.location == BoatEntity.Location.IN_AIR) {
            this.velocityDecay = 0.9F;
         } else if (this.location == BoatEntity.Location.ON_LAND) {
            this.velocityDecay = this.field_7714;
            if (this.getPrimaryPassenger() instanceof PlayerEntity) {
               this.field_7714 /= 2.0F;
            }
         }

         Vec3d vec3d = this.getVelocity();
         this.setVelocity(vec3d.x * (double)this.velocityDecay, vec3d.y + e, vec3d.z * (double)this.velocityDecay);
         this.yawVelocity *= this.velocityDecay;
         if (f > 0.0D) {
            Vec3d vec3d2 = this.getVelocity();
            this.setVelocity(vec3d2.x, (vec3d2.y + f * 0.06153846016296973D) * 0.75D, vec3d2.z);
         }
      }

   }

   private void updatePaddles() {
      if (this.hasPassengers()) {
         float f = 0.0F;
         if (this.pressingLeft) {
            --this.yawVelocity;
         }

         if (this.pressingRight) {
            ++this.yawVelocity;
         }

         if (this.pressingRight != this.pressingLeft && !this.pressingForward && !this.pressingBack) {
            f += 0.005F;
         }

         this.yaw += this.yawVelocity;
         if (this.pressingForward) {
            f += 0.04F;
         }

         if (this.pressingBack) {
            f -= 0.005F;
         }

         this.setVelocity(this.getVelocity().add((double)(MathHelper.sin(-this.yaw * 0.017453292F) * f), 0.0D, (double)(MathHelper.cos(this.yaw * 0.017453292F) * f)));
         this.setPaddleMovings(this.pressingRight && !this.pressingLeft || this.pressingForward, this.pressingLeft && !this.pressingRight || this.pressingForward);
      }
   }

   public void updatePassengerPosition(Entity passenger) {
      if (this.hasPassenger(passenger)) {
         float f = 0.0F;
         float g = (float)((this.removed ? 0.009999999776482582D : this.getMountedHeightOffset()) + passenger.getHeightOffset());
         if (this.getPassengerList().size() > 1) {
            int i = this.getPassengerList().indexOf(passenger);
            if (i == 0) {
               f = 0.2F;
            } else {
               f = -0.6F;
            }

            if (passenger instanceof AnimalEntity) {
               f = (float)((double)f + 0.2D);
            }
         }

         Vec3d vec3d = (new Vec3d((double)f, 0.0D, 0.0D)).rotateY(-this.yaw * 0.017453292F - 1.5707964F);
         passenger.updatePosition(this.getX() + vec3d.x, this.getY() + (double)g, this.getZ() + vec3d.z);
         passenger.yaw += this.yawVelocity;
         passenger.setHeadYaw(passenger.getHeadYaw() + this.yawVelocity);
         this.copyEntityData(passenger);
         if (passenger instanceof AnimalEntity && this.getPassengerList().size() > 1) {
            int j = passenger.getEntityId() % 2 == 0 ? 90 : 270;
            passenger.setYaw(((AnimalEntity)passenger).bodyYaw + (float)j);
            passenger.setHeadYaw(passenger.getHeadYaw() + (float)j);
         }

      }
   }

   protected void copyEntityData(Entity entity) {
      entity.setYaw(this.yaw);
      float f = MathHelper.wrapDegrees(entity.yaw - this.yaw);
      float g = MathHelper.clamp(f, -105.0F, 105.0F);
      entity.prevYaw += g - f;
      entity.yaw += g - f;
      entity.setHeadYaw(entity.yaw);
   }

   @Environment(EnvType.CLIENT)
   public void onPassengerLookAround(Entity passenger) {
      this.copyEntityData(passenger);
   }

   protected void writeCustomDataToTag(CompoundTag tag) {
      tag.putString("Type", this.getBoatType().getName());
   }

   protected void readCustomDataFromTag(CompoundTag tag) {
      if (tag.contains("Type", 8)) {
         this.setBoatType(BoatEntity.Type.getType(tag.getString("Type")));
      }

   }

   public boolean interact(PlayerEntity player, Hand hand) {
      if (player.shouldCancelInteraction()) {
         return false;
      } else {
         return !this.world.isClient && this.ticksUnderwater < 60.0F ? player.startRiding(this) : false;
      }
   }

   protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
      this.fallVelocity = this.getVelocity().y;
      if (!this.hasVehicle()) {
         if (onGround) {
            if (this.fallDistance > 3.0F) {
               if (this.location != BoatEntity.Location.ON_LAND) {
                  this.fallDistance = 0.0F;
                  return;
               }

               this.handleFallDamage(this.fallDistance, 1.0F);
               if (!this.world.isClient && !this.removed) {
                  this.remove();
                  if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                     int j;
                     for(j = 0; j < 3; ++j) {
                        this.dropItem(this.getBoatType().getBaseBlock());
                     }

                     for(j = 0; j < 2; ++j) {
                        this.dropItem(Items.STICK);
                     }
                  }
               }
            }

            this.fallDistance = 0.0F;
         } else if (!this.world.getFluidState((new BlockPos(this)).down()).matches(FluidTags.WATER) && heightDifference < 0.0D) {
            this.fallDistance = (float)((double)this.fallDistance - heightDifference);
         }

      }
   }

   public boolean isPaddleMoving(int paddle) {
      return (Boolean)this.dataTracker.get(paddle == 0 ? LEFT_PADDLE_MOVING : RIGHT_PADDLE_MOVING) && this.getPrimaryPassenger() != null;
   }

   public void setDamageWobbleStrength(float wobbleStrength) {
      this.dataTracker.set(DAMAGE_WOBBLE_STRENGTH, wobbleStrength);
   }

   public float getDamageWobbleStrength() {
      return (Float)this.dataTracker.get(DAMAGE_WOBBLE_STRENGTH);
   }

   public void setDamageWobbleTicks(int wobbleTicks) {
      this.dataTracker.set(DAMAGE_WOBBLE_TICKS, wobbleTicks);
   }

   public int getDamageWobbleTicks() {
      return (Integer)this.dataTracker.get(DAMAGE_WOBBLE_TICKS);
   }

   private void setBubbleWobbleTicks(int wobbleTicks) {
      this.dataTracker.set(BUBBLE_WOBBLE_TICKS, wobbleTicks);
   }

   private int getBubbleWobbleTicks() {
      return (Integer)this.dataTracker.get(BUBBLE_WOBBLE_TICKS);
   }

   @Environment(EnvType.CLIENT)
   public float interpolateBubbleWobble(float tickDelta) {
      return MathHelper.lerp(tickDelta, this.lastBubbleWobble, this.bubbleWobble);
   }

   public void setDamageWobbleSide(int side) {
      this.dataTracker.set(DAMAGE_WOBBLE_SIDE, side);
   }

   public int getDamageWobbleSide() {
      return (Integer)this.dataTracker.get(DAMAGE_WOBBLE_SIDE);
   }

   public void setBoatType(BoatEntity.Type type) {
      this.dataTracker.set(BOAT_TYPE, type.ordinal());
   }

   public BoatEntity.Type getBoatType() {
      return BoatEntity.Type.getType((Integer)this.dataTracker.get(BOAT_TYPE));
   }

   protected boolean canAddPassenger(Entity passenger) {
      return this.getPassengerList().size() < 2 && !this.isInFluid(FluidTags.WATER);
   }

   @Nullable
   public Entity getPrimaryPassenger() {
      List<Entity> list = this.getPassengerList();
      return list.isEmpty() ? null : (Entity)list.get(0);
   }

   @Environment(EnvType.CLIENT)
   public void setInputs(boolean pressingLeft, boolean pressingRight, boolean pressingForward, boolean pressingBack) {
      this.pressingLeft = pressingLeft;
      this.pressingRight = pressingRight;
      this.pressingForward = pressingForward;
      this.pressingBack = pressingBack;
   }

   public Packet<?> createSpawnPacket() {
      return new EntitySpawnS2CPacket(this);
   }

   static {
      DAMAGE_WOBBLE_TICKS = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.INTEGER);
      DAMAGE_WOBBLE_SIDE = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.INTEGER);
      DAMAGE_WOBBLE_STRENGTH = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.FLOAT);
      BOAT_TYPE = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.INTEGER);
      LEFT_PADDLE_MOVING = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      RIGHT_PADDLE_MOVING = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      BUBBLE_WOBBLE_TICKS = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.INTEGER);
   }

   public static enum Type {
      OAK(Blocks.OAK_PLANKS, "oak"),
      SPRUCE(Blocks.SPRUCE_PLANKS, "spruce"),
      BIRCH(Blocks.BIRCH_PLANKS, "birch"),
      JUNGLE(Blocks.JUNGLE_PLANKS, "jungle"),
      ACACIA(Blocks.ACACIA_PLANKS, "acacia"),
      DARK_OAK(Blocks.DARK_OAK_PLANKS, "dark_oak");

      private final String name;
      private final Block baseBlock;

      private Type(Block block, String string2) {
         this.name = string2;
         this.baseBlock = block;
      }

      public String getName() {
         return this.name;
      }

      public Block getBaseBlock() {
         return this.baseBlock;
      }

      public String toString() {
         return this.name;
      }

      public static BoatEntity.Type getType(int i) {
         BoatEntity.Type[] types = values();
         if (i < 0 || i >= types.length) {
            i = 0;
         }

         return types[i];
      }

      public static BoatEntity.Type getType(String string) {
         BoatEntity.Type[] types = values();

         for(int i = 0; i < types.length; ++i) {
            if (types[i].getName().equals(string)) {
               return types[i];
            }
         }

         return types[0];
      }
   }

   public static enum Location {
      IN_WATER,
      UNDER_WATER,
      UNDER_FLOWING_WATER,
      ON_LAND,
      IN_AIR;
   }
}
