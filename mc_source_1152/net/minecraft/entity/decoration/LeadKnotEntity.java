package net.minecraft.entity.decoration;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.packet.EntitySpawnS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class LeadKnotEntity extends AbstractDecorationEntity {
   public LeadKnotEntity(EntityType<? extends LeadKnotEntity> entityType, World world) {
      super(entityType, world);
   }

   public LeadKnotEntity(World world, BlockPos blockPos) {
      super(EntityType.LEASH_KNOT, world, blockPos);
      this.updatePosition((double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D);
      float f = 0.125F;
      float g = 0.1875F;
      float h = 0.25F;
      this.setBoundingBox(new Box(this.getX() - 0.1875D, this.getY() - 0.25D + 0.125D, this.getZ() - 0.1875D, this.getX() + 0.1875D, this.getY() + 0.25D + 0.125D, this.getZ() + 0.1875D));
      this.teleporting = true;
   }

   public void updatePosition(double x, double y, double z) {
      super.updatePosition((double)MathHelper.floor(x) + 0.5D, (double)MathHelper.floor(y) + 0.5D, (double)MathHelper.floor(z) + 0.5D);
   }

   protected void method_6895() {
      this.setPos((double)this.blockPos.getX() + 0.5D, (double)this.blockPos.getY() + 0.5D, (double)this.blockPos.getZ() + 0.5D);
   }

   public void setFacing(Direction direction) {
   }

   public int getWidthPixels() {
      return 9;
   }

   public int getHeightPixels() {
      return 9;
   }

   protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return -0.0625F;
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldRender(double distance) {
      return distance < 1024.0D;
   }

   public void onBreak(@Nullable Entity entity) {
      this.playSound(SoundEvents.ENTITY_LEASH_KNOT_BREAK, 1.0F, 1.0F);
   }

   public void writeCustomDataToTag(CompoundTag tag) {
   }

   public void readCustomDataFromTag(CompoundTag tag) {
   }

   public boolean interact(PlayerEntity player, Hand hand) {
      if (this.world.isClient) {
         return true;
      } else {
         boolean bl = false;
         double d = 7.0D;
         List<MobEntity> list = this.world.getNonSpectatingEntities(MobEntity.class, new Box(this.getX() - 7.0D, this.getY() - 7.0D, this.getZ() - 7.0D, this.getX() + 7.0D, this.getY() + 7.0D, this.getZ() + 7.0D));
         Iterator var7 = list.iterator();

         MobEntity mobEntity2;
         while(var7.hasNext()) {
            mobEntity2 = (MobEntity)var7.next();
            if (mobEntity2.getHoldingEntity() == player) {
               mobEntity2.attachLeash(this, true);
               bl = true;
            }
         }

         if (!bl) {
            this.remove();
            if (player.abilities.creativeMode) {
               var7 = list.iterator();

               while(var7.hasNext()) {
                  mobEntity2 = (MobEntity)var7.next();
                  if (mobEntity2.isLeashed() && mobEntity2.getHoldingEntity() == this) {
                     mobEntity2.detachLeash(true, false);
                  }
               }
            }
         }

         return true;
      }
   }

   public boolean method_6888() {
      return this.world.getBlockState(this.blockPos).getBlock().matches(BlockTags.FENCES);
   }

   public static LeadKnotEntity getOrCreate(World world, BlockPos pos) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      List<LeadKnotEntity> list = world.getNonSpectatingEntities(LeadKnotEntity.class, new Box((double)i - 1.0D, (double)j - 1.0D, (double)k - 1.0D, (double)i + 1.0D, (double)j + 1.0D, (double)k + 1.0D));
      Iterator var6 = list.iterator();

      LeadKnotEntity leadKnotEntity;
      do {
         if (!var6.hasNext()) {
            LeadKnotEntity leadKnotEntity2 = new LeadKnotEntity(world, pos);
            world.spawnEntity(leadKnotEntity2);
            leadKnotEntity2.onPlace();
            return leadKnotEntity2;
         }

         leadKnotEntity = (LeadKnotEntity)var6.next();
      } while(!leadKnotEntity.getDecorationBlockPos().equals(pos));

      return leadKnotEntity;
   }

   public void onPlace() {
      this.playSound(SoundEvents.ENTITY_LEASH_KNOT_PLACE, 1.0F, 1.0F);
   }

   public Packet<?> createSpawnPacket() {
      return new EntitySpawnS2CPacket(this, this.getType(), 0, this.getDecorationBlockPos());
   }
}
