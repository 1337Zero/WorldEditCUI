package net.minecraft.entity.mob;

import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public abstract class HostileEntity extends MobEntityWithAi implements Monster {
   protected HostileEntity(EntityType<? extends HostileEntity> type, World world) {
      super(type, world);
      this.experiencePoints = 5;
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.HOSTILE;
   }

   public void tickMovement() {
      this.tickHandSwing();
      this.updateDespawnCounter();
      super.tickMovement();
   }

   protected void updateDespawnCounter() {
      float f = this.getBrightnessAtEyes();
      if (f > 0.5F) {
         this.despawnCounter += 2;
      }

   }

   protected boolean method_23734() {
      return true;
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.ENTITY_HOSTILE_SWIM;
   }

   protected SoundEvent getSplashSound() {
      return SoundEvents.ENTITY_HOSTILE_SPLASH;
   }

   public boolean damage(DamageSource source, float amount) {
      return this.isInvulnerableTo(source) ? false : super.damage(source, amount);
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_HOSTILE_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_HOSTILE_DEATH;
   }

   protected SoundEvent getFallSound(int distance) {
      return distance > 4 ? SoundEvents.ENTITY_HOSTILE_BIG_FALL : SoundEvents.ENTITY_HOSTILE_SMALL_FALL;
   }

   public float getPathfindingFavor(BlockPos pos, WorldView worldView) {
      return 0.5F - worldView.getBrightness(pos);
   }

   public static boolean isSpawnDark(IWorld world, BlockPos pos, Random random) {
      if (world.getLightLevel(LightType.SKY, pos) > random.nextInt(32)) {
         return false;
      } else {
         int i = world.getWorld().isThundering() ? world.getLightLevel(pos, 10) : world.getLightLevel(pos);
         return i <= random.nextInt(8);
      }
   }

   public static boolean canSpawnInDark(EntityType<? extends HostileEntity> type, IWorld world, SpawnType spawnType, BlockPos pos, Random random) {
      return world.getDifficulty() != Difficulty.PEACEFUL && isSpawnDark(world, pos, random) && canMobSpawn(type, world, spawnType, pos, random);
   }

   public static boolean canSpawnIgnoreLightLevel(EntityType<? extends HostileEntity> type, IWorld world, SpawnType spawnType, BlockPos pos, Random random) {
      return world.getDifficulty() != Difficulty.PEACEFUL && canMobSpawn(type, world, spawnType, pos, random);
   }

   protected void initAttributes() {
      super.initAttributes();
      this.getAttributes().register(EntityAttributes.ATTACK_DAMAGE);
   }

   protected boolean canDropLootAndXp() {
      return true;
   }

   public boolean isAngryAt(PlayerEntity player) {
      return true;
   }

   public ItemStack getArrowType(ItemStack itemStack) {
      if (itemStack.getItem() instanceof RangedWeaponItem) {
         Predicate<ItemStack> predicate = ((RangedWeaponItem)itemStack.getItem()).getHeldProjectiles();
         ItemStack itemStack2 = RangedWeaponItem.getHeldProjectile(this, predicate);
         return itemStack2.isEmpty() ? new ItemStack(Items.ARROW) : itemStack2;
      } else {
         return ItemStack.EMPTY;
      }
   }
}
