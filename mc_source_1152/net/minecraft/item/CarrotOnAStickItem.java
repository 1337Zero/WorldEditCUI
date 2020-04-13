package net.minecraft.item;

import java.util.function.Consumer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class CarrotOnAStickItem extends Item {
   public CarrotOnAStickItem(Item.Settings settings) {
      super(settings);
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack itemStack = user.getStackInHand(hand);
      if (world.isClient) {
         return TypedActionResult.pass(itemStack);
      } else {
         if (user.hasVehicle() && user.getVehicle() instanceof PigEntity) {
            PigEntity pigEntity = (PigEntity)user.getVehicle();
            if (itemStack.getMaxDamage() - itemStack.getDamage() >= 7 && pigEntity.method_6577()) {
               itemStack.damage(7, (LivingEntity)user, (Consumer)((p) -> {
                  p.sendToolBreakStatus(hand);
               }));
               if (itemStack.isEmpty()) {
                  ItemStack itemStack2 = new ItemStack(Items.FISHING_ROD);
                  itemStack2.setTag(itemStack.getTag());
                  return TypedActionResult.success(itemStack2);
               }

               return TypedActionResult.success(itemStack);
            }
         }

         user.incrementStat(Stats.USED.getOrCreateStat(this));
         return TypedActionResult.pass(itemStack);
      }
   }
}
