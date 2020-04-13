package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.client.network.ClientDummyContainerProvider;
import net.minecraft.container.BlockContext;
import net.minecraft.container.EnchantingTableContainer;
import net.minecraft.container.NameableContainerProvider;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Nameable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class EnchantingTableBlock extends BlockWithEntity {
   protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

   protected EnchantingTableBlock(Block.Settings settings) {
      super(settings);
   }

   public boolean hasSidedTransparency(BlockState state) {
      return true;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      return SHAPE;
   }

   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      super.randomDisplayTick(state, world, pos, random);

      for(int i = -2; i <= 2; ++i) {
         for(int j = -2; j <= 2; ++j) {
            if (i > -2 && i < 2 && j == -1) {
               j = 2;
            }

            if (random.nextInt(16) == 0) {
               for(int k = 0; k <= 1; ++k) {
                  BlockPos blockPos = pos.add(i, k, j);
                  if (world.getBlockState(blockPos).getBlock() == Blocks.BOOKSHELF) {
                     if (!world.isAir(pos.add(i / 2, 0, j / 2))) {
                        break;
                     }

                     world.addParticle(ParticleTypes.ENCHANT, (double)pos.getX() + 0.5D, (double)pos.getY() + 2.0D, (double)pos.getZ() + 0.5D, (double)((float)i + random.nextFloat()) - 0.5D, (double)((float)k - random.nextFloat() - 1.0F), (double)((float)j + random.nextFloat()) - 0.5D);
                  }
               }
            }
         }
      }

   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public BlockEntity createBlockEntity(BlockView view) {
      return new EnchantingTableBlockEntity();
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         return ActionResult.SUCCESS;
      } else {
         player.openContainer(state.createContainerProvider(world, pos));
         return ActionResult.SUCCESS;
      }
   }

   @Nullable
   public NameableContainerProvider createContainerProvider(BlockState state, World world, BlockPos pos) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof EnchantingTableBlockEntity) {
         Text text = ((Nameable)blockEntity).getDisplayName();
         return new ClientDummyContainerProvider((i, playerInventory, playerEntity) -> {
            return new EnchantingTableContainer(i, playerInventory, BlockContext.create(world, pos));
         }, text);
      } else {
         return null;
      }
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      if (itemStack.hasCustomName()) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof EnchantingTableBlockEntity) {
            ((EnchantingTableBlockEntity)blockEntity).setCustomName(itemStack.getName());
         }
      }

   }

   public boolean canPlaceAtSide(BlockState world, BlockView view, BlockPos pos, BlockPlacementEnvironment env) {
      return false;
   }
}
