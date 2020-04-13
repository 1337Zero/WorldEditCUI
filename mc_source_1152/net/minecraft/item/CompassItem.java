package net.minecraft.item;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class CompassItem extends Item {
   public CompassItem(Item.Settings settings) {
      super(settings);
      this.addPropertyGetter(new Identifier("angle"), new ItemPropertyGetter() {
         @Environment(EnvType.CLIENT)
         private double angle;
         @Environment(EnvType.CLIENT)
         private double step;
         @Environment(EnvType.CLIENT)
         private long lastTick;

         @Environment(EnvType.CLIENT)
         public float call(ItemStack stack, @Nullable World world, @Nullable LivingEntity user) {
            if (user == null && !stack.isInFrame()) {
               return 0.0F;
            } else {
               boolean bl = user != null;
               Entity entity = bl ? user : stack.getFrame();
               if (world == null) {
                  world = ((Entity)entity).world;
               }

               double g;
               if (world.dimension.hasVisibleSky()) {
                  double d = bl ? (double)((Entity)entity).yaw : this.getYaw((ItemFrameEntity)entity);
                  d = MathHelper.floorMod(d / 360.0D, 1.0D);
                  double e = this.getAngleToSpawn(world, (Entity)entity) / 6.2831854820251465D;
                  g = 0.5D - (d - 0.25D - e);
               } else {
                  g = Math.random();
               }

               if (bl) {
                  g = this.getAngle(world, g);
               }

               return MathHelper.floorMod((float)g, 1.0F);
            }
         }

         @Environment(EnvType.CLIENT)
         private double getAngle(World world, double entityYaw) {
            if (world.getTime() != this.lastTick) {
               this.lastTick = world.getTime();
               double d = entityYaw - this.angle;
               d = MathHelper.floorMod(d + 0.5D, 1.0D) - 0.5D;
               this.step += d * 0.1D;
               this.step *= 0.8D;
               this.angle = MathHelper.floorMod(this.angle + this.step, 1.0D);
            }

            return this.angle;
         }

         @Environment(EnvType.CLIENT)
         private double getYaw(ItemFrameEntity entity) {
            return (double)MathHelper.wrapDegrees(180 + entity.getHorizontalFacing().getHorizontal() * 90);
         }

         @Environment(EnvType.CLIENT)
         private double getAngleToSpawn(IWorld world, Entity entity) {
            BlockPos blockPos = world.getSpawnPos();
            return Math.atan2((double)blockPos.getZ() - entity.getZ(), (double)blockPos.getX() - entity.getX());
         }
      });
   }
}
