package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.container.NameableContainerProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockWithEntity extends Block implements BlockEntityProvider {
   protected BlockWithEntity(Block.Settings settings) {
      super(settings);
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.INVISIBLE;
   }

   public boolean onBlockAction(BlockState state, World world, BlockPos pos, int type, int data) {
      super.onBlockAction(state, world, pos, type, data);
      BlockEntity blockEntity = world.getBlockEntity(pos);
      return blockEntity == null ? false : blockEntity.onBlockAction(type, data);
   }

   @Nullable
   public NameableContainerProvider createContainerProvider(BlockState state, World world, BlockPos pos) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      return blockEntity instanceof NameableContainerProvider ? (NameableContainerProvider)blockEntity : null;
   }
}
