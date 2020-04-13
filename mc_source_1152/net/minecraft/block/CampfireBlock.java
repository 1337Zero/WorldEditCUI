package net.minecraft.block;

import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.BooleanBiFunction;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class CampfireBlock extends BlockWithEntity implements Waterloggable {
   protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 7.0D, 16.0D);
   public static final BooleanProperty LIT;
   public static final BooleanProperty SIGNAL_FIRE;
   public static final BooleanProperty WATERLOGGED;
   public static final DirectionProperty FACING;
   private static final VoxelShape field_21580;

   public CampfireBlock(Block.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(LIT, true)).with(SIGNAL_FIRE, false)).with(WATERLOGGED, false)).with(FACING, Direction.NORTH));
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if ((Boolean)state.get(LIT)) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof CampfireBlockEntity) {
            CampfireBlockEntity campfireBlockEntity = (CampfireBlockEntity)blockEntity;
            ItemStack itemStack = player.getStackInHand(hand);
            Optional<CampfireCookingRecipe> optional = campfireBlockEntity.getRecipeFor(itemStack);
            if (optional.isPresent()) {
               if (!world.isClient && campfireBlockEntity.addItem(player.abilities.creativeMode ? itemStack.copy() : itemStack, ((CampfireCookingRecipe)optional.get()).getCookTime())) {
                  player.incrementStat(Stats.INTERACT_WITH_CAMPFIRE);
                  return ActionResult.SUCCESS;
               }

               return ActionResult.CONSUME;
            }
         }
      }

      return ActionResult.PASS;
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if (!entity.isFireImmune() && (Boolean)state.get(LIT) && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entity)) {
         entity.damage(DamageSource.IN_FIRE, 1.0F);
      }

      super.onEntityCollision(state, world, pos, entity);
   }

   public void onBlockRemoved(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (state.getBlock() != newState.getBlock()) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof CampfireBlockEntity) {
            ItemScatterer.spawn(world, pos, ((CampfireBlockEntity)blockEntity).getItemsBeingCooked());
         }

         super.onBlockRemoved(state, world, pos, newState, moved);
      }
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      IWorld iWorld = ctx.getWorld();
      BlockPos blockPos = ctx.getBlockPos();
      boolean bl = iWorld.getFluidState(blockPos).getFluid() == Fluids.WATER;
      return (BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(WATERLOGGED, bl)).with(SIGNAL_FIRE, this.doesBlockCauseSignalFire(iWorld.getBlockState(blockPos.down())))).with(LIT, !bl)).with(FACING, ctx.getPlayerFacing());
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return facing == Direction.DOWN ? (BlockState)state.with(SIGNAL_FIRE, this.doesBlockCauseSignalFire(neighborState)) : super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
   }

   private boolean doesBlockCauseSignalFire(BlockState state) {
      return state.getBlock() == Blocks.HAY_BLOCK;
   }

   public int getLuminance(BlockState state) {
      return (Boolean)state.get(LIT) ? super.getLuminance(state) : 0;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return SHAPE;
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if ((Boolean)state.get(LIT)) {
         if (random.nextInt(10) == 0) {
            world.playSound((double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() + 0.5F), (double)((float)pos.getZ() + 0.5F), SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
         }

         if (random.nextInt(5) == 0) {
            for(int i = 0; i < random.nextInt(1) + 1; ++i) {
               world.addParticle(ParticleTypes.LAVA, (double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() + 0.5F), (double)((float)pos.getZ() + 0.5F), (double)(random.nextFloat() / 2.0F), 5.0E-5D, (double)(random.nextFloat() / 2.0F));
            }
         }

      }
   }

   public boolean tryFillWithFluid(IWorld world, BlockPos pos, BlockState state, FluidState fluidState) {
      if (!(Boolean)state.get(Properties.WATERLOGGED) && fluidState.getFluid() == Fluids.WATER) {
         boolean bl = (Boolean)state.get(LIT);
         if (bl) {
            if (world.isClient()) {
               for(int i = 0; i < 20; ++i) {
                  spawnSmokeParticle(world.getWorld(), pos, (Boolean)state.get(SIGNAL_FIRE), true);
               }
            } else {
               world.playSound((PlayerEntity)null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CampfireBlockEntity) {
               ((CampfireBlockEntity)blockEntity).spawnItemsBeingCooked();
            }
         }

         world.setBlockState(pos, (BlockState)((BlockState)state.with(WATERLOGGED, true)).with(LIT, false), 3);
         world.getFluidTickScheduler().schedule(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
         return true;
      } else {
         return false;
      }
   }

   @Nullable
   private Entity method_23756(Entity entity) {
      if (entity instanceof AbstractFireballEntity) {
         return ((AbstractFireballEntity)entity).owner;
      } else {
         return entity instanceof ProjectileEntity ? ((ProjectileEntity)entity).getOwner() : null;
      }
   }

   public void onProjectileHit(World world, BlockState state, BlockHitResult hitResult, Entity entity) {
      if (!world.isClient) {
         boolean bl = entity instanceof AbstractFireballEntity || entity instanceof ProjectileEntity && entity.isOnFire();
         if (bl) {
            Entity entity2 = this.method_23756(entity);
            boolean bl2 = entity2 == null || entity2 instanceof PlayerEntity || world.getGameRules().getBoolean(GameRules.MOB_GRIEFING);
            if (bl2 && !(Boolean)state.get(LIT) && !(Boolean)state.get(WATERLOGGED)) {
               BlockPos blockPos = hitResult.getBlockPos();
               world.setBlockState(blockPos, (BlockState)state.with(Properties.LIT, true), 11);
            }
         }
      }

   }

   public static void spawnSmokeParticle(World world, BlockPos pos, boolean isSignal, boolean lotsOfSmoke) {
      Random random = world.getRandom();
      DefaultParticleType defaultParticleType = isSignal ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE : ParticleTypes.CAMPFIRE_COSY_SMOKE;
      world.addImportantParticle(defaultParticleType, true, (double)pos.getX() + 0.5D + random.nextDouble() / 3.0D * (double)(random.nextBoolean() ? 1 : -1), (double)pos.getY() + random.nextDouble() + random.nextDouble(), (double)pos.getZ() + 0.5D + random.nextDouble() / 3.0D * (double)(random.nextBoolean() ? 1 : -1), 0.0D, 0.07D, 0.0D);
      if (lotsOfSmoke) {
         world.addParticle(ParticleTypes.SMOKE, (double)pos.getX() + 0.25D + random.nextDouble() / 2.0D * (double)(random.nextBoolean() ? 1 : -1), (double)pos.getY() + 0.4D, (double)pos.getZ() + 0.25D + random.nextDouble() / 2.0D * (double)(random.nextBoolean() ? 1 : -1), 0.0D, 0.005D, 0.0D);
      }

   }

   public static boolean isLitCampfireInRange(World world, BlockPos pos, int range) {
      for(int i = 1; i <= range; ++i) {
         BlockPos blockPos = pos.down(i);
         BlockState blockState = world.getBlockState(blockPos);
         if (isLitCampfire(blockState)) {
            return true;
         }

         boolean bl = VoxelShapes.matchesAnywhere(field_21580, blockState.getCollisionShape(world, pos, EntityContext.absent()), BooleanBiFunction.AND);
         if (bl) {
            BlockState blockState2 = world.getBlockState(blockPos.down());
            return isLitCampfire(blockState2);
         }
      }

      return false;
   }

   private static boolean isLitCampfire(BlockState state) {
      return state.getBlock() == Blocks.CAMPFIRE && (Boolean)state.get(LIT);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(LIT, SIGNAL_FIRE, WATERLOGGED, FACING);
   }

   public BlockEntity createBlockEntity(BlockView view) {
      return new CampfireBlockEntity();
   }

   public boolean canPlaceAtSide(BlockState world, BlockView view, BlockPos pos, BlockPlacementEnvironment env) {
      return false;
   }

   static {
      LIT = Properties.LIT;
      SIGNAL_FIRE = Properties.SIGNAL_FIRE;
      WATERLOGGED = Properties.WATERLOGGED;
      FACING = Properties.HORIZONTAL_FACING;
      field_21580 = Block.createCuboidShape(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D);
   }
}
