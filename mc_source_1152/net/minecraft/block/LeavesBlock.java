package net.minecraft.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class LeavesBlock extends Block {
   public static final IntProperty DISTANCE;
   public static final BooleanProperty PERSISTENT;

   public LeavesBlock(Block.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(DISTANCE, 7)).with(PERSISTENT, false));
   }

   public boolean hasRandomTicks(BlockState state) {
      return (Integer)state.get(DISTANCE) == 7 && !(Boolean)state.get(PERSISTENT);
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!(Boolean)state.get(PERSISTENT) && (Integer)state.get(DISTANCE) == 7) {
         dropStacks(state, world, pos);
         world.removeBlock(pos, false);
      }

   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      world.setBlockState(pos, updateDistanceFromLogs(state, world, pos), 3);
   }

   public int getOpacity(BlockState state, BlockView view, BlockPos pos) {
      return 1;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
      int i = getDistanceFromLog(neighborState) + 1;
      if (i != 1 || (Integer)state.get(DISTANCE) != i) {
         world.getBlockTickScheduler().schedule(pos, this, 1);
      }

      return state;
   }

   private static BlockState updateDistanceFromLogs(BlockState state, IWorld world, BlockPos pos) {
      int i = 7;
      BlockPos.PooledMutable pooledMutable = BlockPos.PooledMutable.get();
      Throwable var5 = null;

      try {
         Direction[] var6 = Direction.values();
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            Direction direction = var6[var8];
            pooledMutable.set((Vec3i)pos).setOffset(direction);
            i = Math.min(i, getDistanceFromLog(world.getBlockState(pooledMutable)) + 1);
            if (i == 1) {
               break;
            }
         }
      } catch (Throwable var17) {
         var5 = var17;
         throw var17;
      } finally {
         if (pooledMutable != null) {
            if (var5 != null) {
               try {
                  pooledMutable.close();
               } catch (Throwable var16) {
                  var5.addSuppressed(var16);
               }
            } else {
               pooledMutable.close();
            }
         }

      }

      return (BlockState)state.with(DISTANCE, i);
   }

   private static int getDistanceFromLog(BlockState state) {
      if (BlockTags.LOGS.contains(state.getBlock())) {
         return 0;
      } else {
         return state.getBlock() instanceof LeavesBlock ? (Integer)state.get(DISTANCE) : 7;
      }
   }

   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if (world.hasRain(pos.up())) {
         if (random.nextInt(15) == 1) {
            BlockPos blockPos = pos.down();
            BlockState blockState = world.getBlockState(blockPos);
            if (!blockState.isOpaque() || !blockState.isSideSolidFullSquare(world, blockPos, Direction.UP)) {
               double d = (double)((float)pos.getX() + random.nextFloat());
               double e = (double)pos.getY() - 0.05D;
               double f = (double)((float)pos.getZ() + random.nextFloat());
               world.addParticle(ParticleTypes.DRIPPING_WATER, d, e, f, 0.0D, 0.0D, 0.0D);
            }
         }
      }
   }

   public boolean canSuffocate(BlockState state, BlockView view, BlockPos pos) {
      return false;
   }

   public boolean allowsSpawning(BlockState state, BlockView view, BlockPos pos, EntityType<?> type) {
      return type == EntityType.OCELOT || type == EntityType.PARROT;
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(DISTANCE, PERSISTENT);
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return updateDistanceFromLogs((BlockState)this.getDefaultState().with(PERSISTENT, true), ctx.getWorld(), ctx.getBlockPos());
   }

   static {
      DISTANCE = Properties.DISTANCE_1_7;
      PERSISTENT = Properties.PERSISTENT;
   }
}
