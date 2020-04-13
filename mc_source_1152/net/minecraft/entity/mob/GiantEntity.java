package net.minecraft.entity.mob;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class GiantEntity extends HostileEntity {
   public GiantEntity(EntityType<? extends GiantEntity> entityType, World world) {
      super(entityType, world);
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 10.440001F;
   }

   protected void initAttributes() {
      super.initAttributes();
      this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(100.0D);
      this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);
      this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(50.0D);
   }

   public float getPathfindingFavor(BlockPos pos, WorldView worldView) {
      return worldView.getBrightness(pos) - 0.5F;
   }
}
