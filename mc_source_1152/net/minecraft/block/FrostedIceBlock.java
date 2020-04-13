package net.minecraft.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class FrostedIceBlock extends IceBlock {
   public static final IntProperty AGE;

   public FrostedIceBlock(Block.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0));
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if ((random.nextInt(3) == 0 || this.canMelt(world, pos, 4)) && world.getLightLevel(pos) > 11 - (Integer)state.get(AGE) - state.getOpacity(world, pos) && this.increaseAge(state, world, pos)) {
         BlockPos.PooledMutable pooledMutable = BlockPos.PooledMutable.get();
         Throwable var6 = null;

         try {
            Direction[] var7 = Direction.values();
            int var8 = var7.length;

            for(int var9 = 0; var9 < var8; ++var9) {
               Direction direction = var7[var9];
               pooledMutable.set((Vec3i)pos).setOffset(direction);
               BlockState blockState = world.getBlockState(pooledMutable);
               if (blockState.getBlock() == this && !this.increaseAge(blockState, world, pooledMutable)) {
                  world.getBlockTickScheduler().schedule(pooledMutable, this, MathHelper.nextInt(random, 20, 40));
               }
            }
         } catch (Throwable var19) {
            var6 = var19;
            throw var19;
         } finally {
            if (pooledMutable != null) {
               if (var6 != null) {
                  try {
                     pooledMutable.close();
                  } catch (Throwable var18) {
                     var6.addSuppressed(var18);
                  }
               } else {
                  pooledMutable.close();
               }
            }

         }

      } else {
         world.getBlockTickScheduler().schedule(pos, this, MathHelper.nextInt(random, 20, 40));
      }
   }

   private boolean increaseAge(BlockState state, World world, BlockPos pos) {
      int i = (Integer)state.get(AGE);
      if (i < 3) {
         world.setBlockState(pos, (BlockState)state.with(AGE, i + 1), 2);
         return false;
      } else {
         this.melt(state, world, pos);
         return true;
      }
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos neighborPos, boolean moved) {
      if (block == this && this.canMelt(world, pos, 2)) {
         this.melt(state, world, pos);
      }

      super.neighborUpdate(state, world, pos, block, neighborPos, moved);
   }

   private boolean canMelt(BlockView world, BlockPos pos, int maxNeighbors) {
      int i = 0;
      BlockPos.PooledMutable pooledMutable = BlockPos.PooledMutable.get();
      Throwable var6 = null;

      try {
         Direction[] var7 = Direction.values();
         int var8 = var7.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            Direction direction = var7[var9];
            pooledMutable.set((Vec3i)pos).setOffset(direction);
            if (world.getBlockState(pooledMutable).getBlock() == this) {
               ++i;
               if (i >= maxNeighbors) {
                  boolean var11 = false;
                  return var11;
               }
            }
         }
      } catch (Throwable var21) {
         var6 = var21;
         throw var21;
      } finally {
         if (pooledMutable != null) {
            if (var6 != null) {
               try {
                  pooledMutable.close();
               } catch (Throwable var20) {
                  var6.addSuppressed(var20);
               }
            } else {
               pooledMutable.close();
            }
         }

      }

      return true;
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(AGE);
   }

   @Environment(EnvType.CLIENT)
   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      return ItemStack.EMPTY;
   }

   static {
      AGE = Properties.AGE_3;
   }
}
