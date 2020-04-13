package net.minecraft.entity;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.packet.EntitySpawnS2CPacket;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.Packet;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class ItemEntity extends Entity {
   private static final TrackedData<ItemStack> STACK;
   private int age;
   private int pickupDelay;
   private int health;
   private UUID thrower;
   private UUID owner;
   public final float hoverHeight;

   public ItemEntity(EntityType<? extends ItemEntity> entityType, World world) {
      super(entityType, world);
      this.health = 5;
      this.hoverHeight = (float)(Math.random() * 3.141592653589793D * 2.0D);
   }

   public ItemEntity(World world, double x, double y, double z) {
      this(EntityType.ITEM, world);
      this.updatePosition(x, y, z);
      this.yaw = this.random.nextFloat() * 360.0F;
      this.setVelocity(this.random.nextDouble() * 0.2D - 0.1D, 0.2D, this.random.nextDouble() * 0.2D - 0.1D);
   }

   public ItemEntity(World world, double x, double y, double z, ItemStack stack) {
      this(world, x, y, z);
      this.setStack(stack);
   }

   protected boolean canClimb() {
      return false;
   }

   protected void initDataTracker() {
      this.getDataTracker().startTracking(STACK, ItemStack.EMPTY);
   }

   public void tick() {
      if (this.getStack().isEmpty()) {
         this.remove();
      } else {
         super.tick();
         if (this.pickupDelay > 0 && this.pickupDelay != 32767) {
            --this.pickupDelay;
         }

         this.prevX = this.getX();
         this.prevY = this.getY();
         this.prevZ = this.getZ();
         Vec3d vec3d = this.getVelocity();
         if (this.isInFluid(FluidTags.WATER)) {
            this.applyBuoyancy();
         } else if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0D, -0.04D, 0.0D));
         }

         if (this.world.isClient) {
            this.noClip = false;
         } else {
            this.noClip = !this.world.doesNotCollide(this);
            if (this.noClip) {
               this.pushOutOfBlocks(this.getX(), (this.getBoundingBox().y1 + this.getBoundingBox().y2) / 2.0D, this.getZ());
            }
         }

         if (!this.onGround || squaredHorizontalLength(this.getVelocity()) > 9.999999747378752E-6D || (this.age + this.getEntityId()) % 4 == 0) {
            this.move(MovementType.SELF, this.getVelocity());
            float f = 0.98F;
            if (this.onGround) {
               f = this.world.getBlockState(new BlockPos(this.getX(), this.getY() - 1.0D, this.getZ())).getBlock().getSlipperiness() * 0.98F;
            }

            this.setVelocity(this.getVelocity().multiply((double)f, 0.98D, (double)f));
            if (this.onGround) {
               this.setVelocity(this.getVelocity().multiply(1.0D, -0.5D, 1.0D));
            }
         }

         boolean bl = MathHelper.floor(this.prevX) != MathHelper.floor(this.getX()) || MathHelper.floor(this.prevY) != MathHelper.floor(this.getY()) || MathHelper.floor(this.prevZ) != MathHelper.floor(this.getZ());
         int i = bl ? 2 : 40;
         if (this.age % i == 0) {
            if (this.world.getFluidState(new BlockPos(this)).matches(FluidTags.LAVA)) {
               this.setVelocity((double)((this.random.nextFloat() - this.random.nextFloat()) * 0.2F), 0.20000000298023224D, (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.2F));
               this.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
            }

            if (!this.world.isClient && this.canMerge()) {
               this.tryMerge();
            }
         }

         if (this.age != -32768) {
            ++this.age;
         }

         this.velocityDirty |= this.checkWaterState();
         if (!this.world.isClient) {
            double d = this.getVelocity().subtract(vec3d).lengthSquared();
            if (d > 0.01D) {
               this.velocityDirty = true;
            }
         }

         if (!this.world.isClient && this.age >= 6000) {
            this.remove();
         }

      }
   }

   private void applyBuoyancy() {
      Vec3d vec3d = this.getVelocity();
      this.setVelocity(vec3d.x * 0.9900000095367432D, vec3d.y + (double)(vec3d.y < 0.05999999865889549D ? 5.0E-4F : 0.0F), vec3d.z * 0.9900000095367432D);
   }

   private void tryMerge() {
      if (this.canMerge()) {
         List<ItemEntity> list = this.world.getEntities(ItemEntity.class, this.getBoundingBox().expand(0.5D, 0.0D, 0.5D), (itemEntityx) -> {
            return itemEntityx != this && itemEntityx.canMerge();
         });
         Iterator var2 = list.iterator();

         while(var2.hasNext()) {
            ItemEntity itemEntity = (ItemEntity)var2.next();
            if (itemEntity.canMerge()) {
               this.tryMerge(itemEntity);
               if (this.removed) {
                  break;
               }
            }
         }

      }
   }

   private boolean canMerge() {
      ItemStack itemStack = this.getStack();
      return this.isAlive() && this.pickupDelay != 32767 && this.age != -32768 && this.age < 6000 && itemStack.getCount() < itemStack.getMaxCount();
   }

   private void tryMerge(ItemEntity other) {
      ItemStack itemStack = this.getStack();
      ItemStack itemStack2 = other.getStack();
      if (Objects.equals(this.getOwner(), other.getOwner()) && method_24017(itemStack, itemStack2)) {
         if (itemStack2.getCount() < itemStack.getCount()) {
            merge(this, itemStack, other, itemStack2);
         } else {
            merge(other, itemStack2, this, itemStack);
         }

      }
   }

   public static boolean method_24017(ItemStack itemStack, ItemStack itemStack2) {
      if (itemStack2.getItem() != itemStack.getItem()) {
         return false;
      } else if (itemStack2.getCount() + itemStack.getCount() > itemStack2.getMaxCount()) {
         return false;
      } else if (itemStack2.hasTag() ^ itemStack.hasTag()) {
         return false;
      } else {
         return !itemStack2.hasTag() || itemStack2.getTag().equals(itemStack.getTag());
      }
   }

   public static ItemStack method_24018(ItemStack itemStack, ItemStack itemStack2, int i) {
      int j = Math.min(Math.min(itemStack.getMaxCount(), i) - itemStack.getCount(), itemStack2.getCount());
      ItemStack itemStack3 = itemStack.copy();
      itemStack3.increment(j);
      itemStack2.decrement(j);
      return itemStack3;
   }

   private static void method_24016(ItemEntity itemEntity, ItemStack itemStack, ItemStack itemStack2) {
      ItemStack itemStack3 = method_24018(itemStack, itemStack2, 64);
      itemEntity.setStack(itemStack3);
   }

   private static void merge(ItemEntity targetEntity, ItemStack targetStack, ItemEntity sourceEntity, ItemStack sourceStack) {
      method_24016(targetEntity, targetStack, sourceStack);
      targetEntity.pickupDelay = Math.max(targetEntity.pickupDelay, sourceEntity.pickupDelay);
      targetEntity.age = Math.min(targetEntity.age, sourceEntity.age);
      if (sourceStack.isEmpty()) {
         sourceEntity.remove();
      }

   }

   protected void burn(int time) {
      this.damage(DamageSource.IN_FIRE, (float)time);
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else if (!this.getStack().isEmpty() && this.getStack().getItem() == Items.NETHER_STAR && source.isExplosive()) {
         return false;
      } else {
         this.scheduleVelocityUpdate();
         this.health = (int)((float)this.health - amount);
         if (this.health <= 0) {
            this.remove();
         }

         return false;
      }
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      tag.putShort("Health", (short)this.health);
      tag.putShort("Age", (short)this.age);
      tag.putShort("PickupDelay", (short)this.pickupDelay);
      if (this.getThrower() != null) {
         tag.put("Thrower", NbtHelper.fromUuid(this.getThrower()));
      }

      if (this.getOwner() != null) {
         tag.put("Owner", NbtHelper.fromUuid(this.getOwner()));
      }

      if (!this.getStack().isEmpty()) {
         tag.put("Item", this.getStack().toTag(new CompoundTag()));
      }

   }

   public void readCustomDataFromTag(CompoundTag tag) {
      this.health = tag.getShort("Health");
      this.age = tag.getShort("Age");
      if (tag.contains("PickupDelay")) {
         this.pickupDelay = tag.getShort("PickupDelay");
      }

      if (tag.contains("Owner", 10)) {
         this.owner = NbtHelper.toUuid(tag.getCompound("Owner"));
      }

      if (tag.contains("Thrower", 10)) {
         this.thrower = NbtHelper.toUuid(tag.getCompound("Thrower"));
      }

      CompoundTag compoundTag = tag.getCompound("Item");
      this.setStack(ItemStack.fromTag(compoundTag));
      if (this.getStack().isEmpty()) {
         this.remove();
      }

   }

   public void onPlayerCollision(PlayerEntity player) {
      if (!this.world.isClient) {
         ItemStack itemStack = this.getStack();
         Item item = itemStack.getItem();
         int i = itemStack.getCount();
         if (this.pickupDelay == 0 && (this.owner == null || this.owner.equals(player.getUuid())) && player.inventory.insertStack(itemStack)) {
            player.sendPickup(this, i);
            if (itemStack.isEmpty()) {
               this.remove();
               itemStack.setCount(i);
            }

            player.increaseStat(Stats.PICKED_UP.getOrCreateStat(item), i);
         }

      }
   }

   public Text getName() {
      Text text = this.getCustomName();
      return (Text)(text != null ? text : new TranslatableText(this.getStack().getTranslationKey(), new Object[0]));
   }

   public boolean isAttackable() {
      return false;
   }

   @Nullable
   public Entity changeDimension(DimensionType newDimension) {
      Entity entity = super.changeDimension(newDimension);
      if (!this.world.isClient && entity instanceof ItemEntity) {
         ((ItemEntity)entity).tryMerge();
      }

      return entity;
   }

   public ItemStack getStack() {
      return (ItemStack)this.getDataTracker().get(STACK);
   }

   public void setStack(ItemStack stack) {
      this.getDataTracker().set(STACK, stack);
   }

   @Nullable
   public UUID getOwner() {
      return this.owner;
   }

   public void setOwner(@Nullable UUID uuid) {
      this.owner = uuid;
   }

   @Nullable
   public UUID getThrower() {
      return this.thrower;
   }

   public void setThrower(@Nullable UUID uuid) {
      this.thrower = uuid;
   }

   @Environment(EnvType.CLIENT)
   public int getAge() {
      return this.age;
   }

   public void setToDefaultPickupDelay() {
      this.pickupDelay = 10;
   }

   public void resetPickupDelay() {
      this.pickupDelay = 0;
   }

   public void setPickupDelayInfinite() {
      this.pickupDelay = 32767;
   }

   public void setPickupDelay(int pickupDelay) {
      this.pickupDelay = pickupDelay;
   }

   public boolean cannotPickup() {
      return this.pickupDelay > 0;
   }

   public void setCovetedItem() {
      this.age = -6000;
   }

   public void setDespawnImmediately() {
      this.setPickupDelayInfinite();
      this.age = 5999;
   }

   public Packet<?> createSpawnPacket() {
      return new EntitySpawnS2CPacket(this);
   }

   static {
      STACK = DataTracker.registerData(ItemEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
   }
}
