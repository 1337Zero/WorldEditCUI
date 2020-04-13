package net.minecraft.world.gen.decorator;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.VineBlock;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.AbstractTreeFeature;

public class TrunkVineTreeDecorator extends TreeDecorator {
   public TrunkVineTreeDecorator() {
      super(TreeDecoratorType.TRUNK_VINE);
   }

   public <T> TrunkVineTreeDecorator(Dynamic<T> dynamic) {
      this();
   }

   public void generate(IWorld world, Random random, List<BlockPos> list, List<BlockPos> list2, Set<BlockPos> set, BlockBox box) {
      list.forEach((blockPos) -> {
         BlockPos blockPos5;
         if (random.nextInt(3) > 0) {
            blockPos5 = blockPos.west();
            if (AbstractTreeFeature.isAir(world, blockPos5)) {
               this.method_23471(world, blockPos5, VineBlock.EAST, set, box);
            }
         }

         if (random.nextInt(3) > 0) {
            blockPos5 = blockPos.east();
            if (AbstractTreeFeature.isAir(world, blockPos5)) {
               this.method_23471(world, blockPos5, VineBlock.WEST, set, box);
            }
         }

         if (random.nextInt(3) > 0) {
            blockPos5 = blockPos.north();
            if (AbstractTreeFeature.isAir(world, blockPos5)) {
               this.method_23471(world, blockPos5, VineBlock.SOUTH, set, box);
            }
         }

         if (random.nextInt(3) > 0) {
            blockPos5 = blockPos.south();
            if (AbstractTreeFeature.isAir(world, blockPos5)) {
               this.method_23471(world, blockPos5, VineBlock.NORTH, set, box);
            }
         }

      });
   }

   public <T> T serialize(DynamicOps<T> ops) {
      return (new Dynamic(ops, ops.createMap(ImmutableMap.of(ops.createString("type"), ops.createString(Registry.TREE_DECORATOR_TYPE.getId(this.field_21319).toString()))))).getValue();
   }
}
