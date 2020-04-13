package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.structure.Structure;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class JigsawBlock extends FacingBlock implements BlockEntityProvider {
   protected JigsawBlock(Block.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.UP));
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(FACING, ctx.getSide());
   }

   @Nullable
   public BlockEntity createBlockEntity(BlockView view) {
      return new JigsawBlockEntity();
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof JigsawBlockEntity && player.isCreativeLevelTwoOp()) {
         player.openJigsawScreen((JigsawBlockEntity)blockEntity);
         return ActionResult.SUCCESS;
      } else {
         return ActionResult.PASS;
      }
   }

   public static boolean attachmentMatches(Structure.StructureBlockInfo info1, Structure.StructureBlockInfo info2) {
      return info1.state.get(FACING) == ((Direction)info2.state.get(FACING)).getOpposite() && info1.tag.getString("attachement_type").equals(info2.tag.getString("attachement_type"));
   }
}
