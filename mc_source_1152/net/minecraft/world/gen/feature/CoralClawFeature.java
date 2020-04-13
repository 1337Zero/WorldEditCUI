package net.minecraft.world.gen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;

public class CoralClawFeature extends CoralFeature {
   public CoralClawFeature(Function<Dynamic<?>, ? extends DefaultFeatureConfig> configFactory) {
      super(configFactory);
   }

   protected boolean spawnCoral(IWorld world, Random random, BlockPos pos, BlockState state) {
      if (!this.spawnCoralPiece(world, random, pos, state)) {
         return false;
      } else {
         Direction direction = Direction.Type.HORIZONTAL.random(random);
         int i = random.nextInt(2) + 2;
         List<Direction> list = Lists.newArrayList(new Direction[]{direction, direction.rotateYClockwise(), direction.rotateYCounterclockwise()});
         Collections.shuffle(list, random);
         List<Direction> list2 = list.subList(0, i);
         Iterator var9 = list2.iterator();

         while(var9.hasNext()) {
            Direction direction2 = (Direction)var9.next();
            BlockPos.Mutable mutable = new BlockPos.Mutable(pos);
            int j = random.nextInt(2) + 1;
            mutable.setOffset(direction2);
            int l;
            Direction direction4;
            if (direction2 == direction) {
               direction4 = direction;
               l = random.nextInt(3) + 2;
            } else {
               mutable.setOffset(Direction.UP);
               Direction[] directions = new Direction[]{direction2, Direction.UP};
               direction4 = directions[random.nextInt(directions.length)];
               l = random.nextInt(3) + 3;
            }

            int n;
            for(n = 0; n < j && this.spawnCoralPiece(world, random, mutable, state); ++n) {
               mutable.setOffset(direction4);
            }

            mutable.setOffset(direction4.getOpposite());
            mutable.setOffset(Direction.UP);

            for(n = 0; n < l; ++n) {
               mutable.setOffset(direction);
               if (!this.spawnCoralPiece(world, random, mutable, state)) {
                  break;
               }

               if (random.nextFloat() < 0.25F) {
                  mutable.setOffset(Direction.UP);
               }
            }
         }

         return true;
      }
   }
}
