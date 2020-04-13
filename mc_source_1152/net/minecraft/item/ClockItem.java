package net.minecraft.item;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ClockItem extends Item {
   public ClockItem(Item.Settings settings) {
      super(settings);
      this.addPropertyGetter(new Identifier("time"), new ItemPropertyGetter() {
         @Environment(EnvType.CLIENT)
         private double time;
         @Environment(EnvType.CLIENT)
         private double step;
         @Environment(EnvType.CLIENT)
         private long lastTick;

         @Environment(EnvType.CLIENT)
         public float call(ItemStack stack, @Nullable World world, @Nullable LivingEntity user) {
            boolean bl = user != null;
            Entity entity = bl ? user : stack.getFrame();
            if (world == null && entity != null) {
               world = ((Entity)entity).world;
            }

            if (world == null) {
               return 0.0F;
            } else {
               double e;
               if (world.dimension.hasVisibleSky()) {
                  e = (double)world.getSkyAngle(1.0F);
               } else {
                  e = Math.random();
               }

               e = this.getTime(world, e);
               return (float)e;
            }
         }

         @Environment(EnvType.CLIENT)
         private double getTime(World world, double skyAngle) {
            if (world.getTime() != this.lastTick) {
               this.lastTick = world.getTime();
               double d = skyAngle - this.time;
               d = MathHelper.floorMod(d + 0.5D, 1.0D) - 0.5D;
               this.step += d * 0.1D;
               this.step *= 0.9D;
               this.time = MathHelper.floorMod(this.time + this.step, 1.0D);
            }

            return this.time;
         }
      });
   }
}
