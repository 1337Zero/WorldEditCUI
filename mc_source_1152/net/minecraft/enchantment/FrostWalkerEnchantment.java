package net.minecraft.enchantment;

import java.util.Iterator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.Material;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class FrostWalkerEnchantment extends Enchantment {
   public FrostWalkerEnchantment(Enchantment.Weight weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.ARMOR_FEET, slotTypes);
   }

   public int getMinimumPower(int level) {
      return level * 10;
   }

   public int getMaximumPower(int level) {
      return this.getMinimumPower(level) + 15;
   }

   public boolean isTreasure() {
      return true;
   }

   public int getMaximumLevel() {
      return 2;
   }

   public static void freezeWater(LivingEntity entity, World world, BlockPos blockPos, int level) {
      if (entity.onGround) {
         BlockState blockState = Blocks.FROSTED_ICE.getDefaultState();
         float f = (float)Math.min(16, 2 + level);
         BlockPos.Mutable mutable = new BlockPos.Mutable();
         Iterator var7 = BlockPos.iterate(blockPos.add((double)(-f), -1.0D, (double)(-f)), blockPos.add((double)f, -1.0D, (double)f)).iterator();

         while(var7.hasNext()) {
            BlockPos blockPos2 = (BlockPos)var7.next();
            if (blockPos2.isWithinDistance(entity.getPos(), (double)f)) {
               mutable.set(blockPos2.getX(), blockPos2.getY() + 1, blockPos2.getZ());
               BlockState blockState2 = world.getBlockState(mutable);
               if (blockState2.isAir()) {
                  BlockState blockState3 = world.getBlockState(blockPos2);
                  if (blockState3.getMaterial() == Material.WATER && (Integer)blockState3.get(FluidBlock.LEVEL) == 0 && blockState.canPlaceAt(world, blockPos2) && world.canPlace(blockState, blockPos2, EntityContext.absent())) {
                     world.setBlockState(blockPos2, blockState);
                     world.getBlockTickScheduler().schedule(blockPos2, Blocks.FROSTED_ICE, MathHelper.nextInt(entity.getRandom(), 60, 120));
                  }
               }
            }
         }

      }
   }

   public boolean differs(Enchantment other) {
      return super.differs(other) && other != Enchantments.DEPTH_STRIDER;
   }
}
