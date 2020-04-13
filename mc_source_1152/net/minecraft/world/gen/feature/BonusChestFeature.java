package net.minecraft.world.gen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.loot.LootTables;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;

public class BonusChestFeature extends Feature<DefaultFeatureConfig> {
   public BonusChestFeature(Function<Dynamic<?>, ? extends DefaultFeatureConfig> configFactory) {
      super(configFactory);
   }

   public boolean generate(IWorld iWorld, ChunkGenerator<? extends ChunkGeneratorConfig> chunkGenerator, Random random, BlockPos blockPos, DefaultFeatureConfig defaultFeatureConfig) {
      ChunkPos chunkPos = new ChunkPos(blockPos);
      List<Integer> list = (List)IntStream.rangeClosed(chunkPos.getStartX(), chunkPos.getEndX()).boxed().collect(Collectors.toList());
      Collections.shuffle(list, random);
      List<Integer> list2 = (List)IntStream.rangeClosed(chunkPos.getStartZ(), chunkPos.getEndZ()).boxed().collect(Collectors.toList());
      Collections.shuffle(list2, random);
      BlockPos.Mutable mutable = new BlockPos.Mutable();
      Iterator var10 = list.iterator();

      while(var10.hasNext()) {
         Integer integer = (Integer)var10.next();
         Iterator var12 = list2.iterator();

         while(var12.hasNext()) {
            Integer integer2 = (Integer)var12.next();
            mutable.set(integer, 0, integer2);
            BlockPos blockPos2 = iWorld.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, mutable);
            if (iWorld.isAir(blockPos2) || iWorld.getBlockState(blockPos2).getCollisionShape(iWorld, blockPos2).isEmpty()) {
               iWorld.setBlockState(blockPos2, Blocks.CHEST.getDefaultState(), 2);
               LootableContainerBlockEntity.setLootTable(iWorld, random, blockPos2, LootTables.SPAWN_BONUS_CHEST);
               BlockState blockState = Blocks.TORCH.getDefaultState();
               Iterator var16 = Direction.Type.HORIZONTAL.iterator();

               while(var16.hasNext()) {
                  Direction direction = (Direction)var16.next();
                  BlockPos blockPos3 = blockPos2.offset(direction);
                  if (blockState.canPlaceAt(iWorld, blockPos3)) {
                     iWorld.setBlockState(blockPos3, blockState, 2);
                  }
               }

               return true;
            }
         }
      }

      return false;
   }
}
