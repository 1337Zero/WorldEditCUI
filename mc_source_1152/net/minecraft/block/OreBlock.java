package net.minecraft.block;

import java.util.Random;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class OreBlock extends Block {
   public OreBlock(Block.Settings settings) {
      super(settings);
   }

   protected int getExperienceWhenMined(Random random) {
      if (this == Blocks.COAL_ORE) {
         return MathHelper.nextInt(random, 0, 2);
      } else if (this == Blocks.DIAMOND_ORE) {
         return MathHelper.nextInt(random, 3, 7);
      } else if (this == Blocks.EMERALD_ORE) {
         return MathHelper.nextInt(random, 3, 7);
      } else if (this == Blocks.LAPIS_ORE) {
         return MathHelper.nextInt(random, 2, 5);
      } else {
         return this == Blocks.NETHER_QUARTZ_ORE ? MathHelper.nextInt(random, 2, 5) : 0;
      }
   }

   public void onStacksDropped(BlockState state, World world, BlockPos pos, ItemStack stack) {
      super.onStacksDropped(state, world, pos, stack);
      if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, stack) == 0) {
         int i = this.getExperienceWhenMined(world.random);
         if (i > 0) {
            this.dropExperience(world, pos, i);
         }
      }

   }
}
