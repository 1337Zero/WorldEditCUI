package net.minecraft.entity.thrown;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ThrownExperienceBottleEntity extends ThrownItemEntity {
   public ThrownExperienceBottleEntity(EntityType<? extends ThrownExperienceBottleEntity> entityType, World world) {
      super(entityType, world);
   }

   public ThrownExperienceBottleEntity(World world, LivingEntity owner) {
      super(EntityType.EXPERIENCE_BOTTLE, owner, world);
   }

   public ThrownExperienceBottleEntity(World world, double x, double y, double z) {
      super(EntityType.EXPERIENCE_BOTTLE, x, y, z, world);
   }

   protected Item getDefaultItem() {
      return Items.EXPERIENCE_BOTTLE;
   }

   protected float getGravity() {
      return 0.07F;
   }

   protected void onCollision(HitResult hitResult) {
      if (!this.world.isClient) {
         this.world.playLevelEvent(2002, new BlockPos(this), PotionUtil.getColor(Potions.WATER));
         int i = 3 + this.world.random.nextInt(5) + this.world.random.nextInt(5);

         while(i > 0) {
            int j = ExperienceOrbEntity.roundToOrbSize(i);
            i -= j;
            this.world.spawnEntity(new ExperienceOrbEntity(this.world, this.getX(), this.getY(), this.getZ(), j));
         }

         this.remove();
      }

   }
}
