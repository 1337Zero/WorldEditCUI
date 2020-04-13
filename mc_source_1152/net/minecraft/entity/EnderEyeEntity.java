package net.minecraft.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.api.EnvironmentInterfaces;
import net.minecraft.client.network.packet.EntitySpawnS2CPacket;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@EnvironmentInterfaces({@EnvironmentInterface(
   value = EnvType.CLIENT,
   itf = FlyingItemEntity.class
)})
public class EnderEyeEntity extends Entity implements FlyingItemEntity {
   private static final TrackedData<ItemStack> ITEM;
   private double velocityX;
   private double velocityY;
   private double velocityZ;
   private int useCount;
   private boolean dropsItem;

   public EnderEyeEntity(EntityType<? extends EnderEyeEntity> entityType, World world) {
      super(entityType, world);
   }

   public EnderEyeEntity(World world, double x, double y, double z) {
      this(EntityType.EYE_OF_ENDER, world);
      this.useCount = 0;
      this.updatePosition(x, y, z);
   }

   public void setItem(ItemStack stack) {
      if (stack.getItem() != Items.ENDER_EYE || stack.hasTag()) {
         this.getDataTracker().set(ITEM, Util.make(stack.copy(), (stackx) -> {
            stackx.setCount(1);
         }));
      }

   }

   private ItemStack getTrackedItem() {
      return (ItemStack)this.getDataTracker().get(ITEM);
   }

   public ItemStack getStack() {
      ItemStack itemStack = this.getTrackedItem();
      return itemStack.isEmpty() ? new ItemStack(Items.ENDER_EYE) : itemStack;
   }

   protected void initDataTracker() {
      this.getDataTracker().startTracking(ITEM, ItemStack.EMPTY);
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

   public void moveTowards(BlockPos pos) {
      double d = (double)pos.getX();
      int i = pos.getY();
      double e = (double)pos.getZ();
      double f = d - this.getX();
      double g = e - this.getZ();
      float h = MathHelper.sqrt(f * f + g * g);
      if (h > 12.0F) {
         this.velocityX = this.getX() + f / (double)h * 12.0D;
         this.velocityZ = this.getZ() + g / (double)h * 12.0D;
         this.velocityY = this.getY() + 8.0D;
      } else {
         this.velocityX = d;
         this.velocityY = (double)i;
         this.velocityZ = e;
      }

      this.useCount = 0;
      this.dropsItem = this.random.nextInt(5) > 0;
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
      if (!this.world.isClient) {
         double h = this.velocityX - d;
         double i = this.velocityZ - f;
         float j = (float)Math.sqrt(h * h + i * i);
         float k = (float)MathHelper.atan2(i, h);
         double l = MathHelper.lerp(0.0025D, (double)g, (double)j);
         double m = vec3d.y;
         if (j < 1.0F) {
            l *= 0.8D;
            m *= 0.8D;
         }

         int n = this.getY() < this.velocityY ? 1 : -1;
         vec3d = new Vec3d(Math.cos((double)k) * l, m + ((double)n - m) * 0.014999999664723873D, Math.sin((double)k) * l);
         this.setVelocity(vec3d);
      }

      float o = 0.25F;
      if (this.isTouchingWater()) {
         for(int p = 0; p < 4; ++p) {
            this.world.addParticle(ParticleTypes.BUBBLE, d - vec3d.x * 0.25D, e - vec3d.y * 0.25D, f - vec3d.z * 0.25D, vec3d.x, vec3d.y, vec3d.z);
         }
      } else {
         this.world.addParticle(ParticleTypes.PORTAL, d - vec3d.x * 0.25D + this.random.nextDouble() * 0.6D - 0.3D, e - vec3d.y * 0.25D - 0.5D, f - vec3d.z * 0.25D + this.random.nextDouble() * 0.6D - 0.3D, vec3d.x, vec3d.y, vec3d.z);
      }

      if (!this.world.isClient) {
         this.updatePosition(d, e, f);
         ++this.useCount;
         if (this.useCount > 80 && !this.world.isClient) {
            this.playSound(SoundEvents.ENTITY_ENDER_EYE_DEATH, 1.0F, 1.0F);
            this.remove();
            if (this.dropsItem) {
               this.world.spawnEntity(new ItemEntity(this.world, this.getX(), this.getY(), this.getZ(), this.getStack()));
            } else {
               this.world.playLevelEvent(2003, new BlockPos(this), 0);
            }
         }
      } else {
         this.setPos(d, e, f);
      }

   }

   public void writeCustomDataToTag(CompoundTag tag) {
      ItemStack itemStack = this.getTrackedItem();
      if (!itemStack.isEmpty()) {
         tag.put("Item", itemStack.toTag(new CompoundTag()));
      }

   }

   public void readCustomDataFromTag(CompoundTag tag) {
      ItemStack itemStack = ItemStack.fromTag(tag.getCompound("Item"));
      this.setItem(itemStack);
   }

   public float getBrightnessAtEyes() {
      return 1.0F;
   }

   public boolean isAttackable() {
      return false;
   }

   public Packet<?> createSpawnPacket() {
      return new EntitySpawnS2CPacket(this);
   }

   static {
      ITEM = DataTracker.registerData(EnderEyeEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
   }
}
