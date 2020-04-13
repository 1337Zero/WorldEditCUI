package net.minecraft.block;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.container.Container;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ShulkerBoxBlock extends BlockWithEntity {
   public static final EnumProperty<Direction> FACING;
   public static final Identifier CONTENTS;
   @Nullable
   private final DyeColor color;

   public ShulkerBoxBlock(@Nullable DyeColor color, Block.Settings settings) {
      super(settings);
      this.color = color;
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.UP));
   }

   public BlockEntity createBlockEntity(BlockView view) {
      return new ShulkerBoxBlockEntity(this.color);
   }

   public boolean canSuffocate(BlockState state, BlockView view, BlockPos pos) {
      return true;
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.ENTITYBLOCK_ANIMATED;
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         return ActionResult.SUCCESS;
      } else if (player.isSpectator()) {
         return ActionResult.SUCCESS;
      } else {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof ShulkerBoxBlockEntity) {
            Direction direction = (Direction)state.get(FACING);
            ShulkerBoxBlockEntity shulkerBoxBlockEntity = (ShulkerBoxBlockEntity)blockEntity;
            boolean bl2;
            if (shulkerBoxBlockEntity.getAnimationStage() == ShulkerBoxBlockEntity.AnimationStage.CLOSED) {
               Box box = VoxelShapes.fullCube().getBoundingBox().stretch((double)(0.5F * (float)direction.getOffsetX()), (double)(0.5F * (float)direction.getOffsetY()), (double)(0.5F * (float)direction.getOffsetZ())).shrink((double)direction.getOffsetX(), (double)direction.getOffsetY(), (double)direction.getOffsetZ());
               bl2 = world.doesNotCollide(box.offset(pos.offset(direction)));
            } else {
               bl2 = true;
            }

            if (bl2) {
               player.openContainer(shulkerBoxBlockEntity);
               player.incrementStat(Stats.OPEN_SHULKER_BOX);
            }

            return ActionResult.SUCCESS;
         } else {
            return ActionResult.PASS;
         }
      }
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(FACING, ctx.getSide());
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }

   public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof ShulkerBoxBlockEntity) {
         ShulkerBoxBlockEntity shulkerBoxBlockEntity = (ShulkerBoxBlockEntity)blockEntity;
         if (!world.isClient && player.isCreative() && !shulkerBoxBlockEntity.isInvEmpty()) {
            ItemStack itemStack = getItemStack(this.getColor());
            CompoundTag compoundTag = shulkerBoxBlockEntity.serializeInventory(new CompoundTag());
            if (!compoundTag.isEmpty()) {
               itemStack.putSubTag("BlockEntityTag", compoundTag);
            }

            if (shulkerBoxBlockEntity.hasCustomName()) {
               itemStack.setCustomName(shulkerBoxBlockEntity.getCustomName());
            }

            ItemEntity itemEntity = new ItemEntity(world, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), itemStack);
            itemEntity.setToDefaultPickupDelay();
            world.spawnEntity(itemEntity);
         } else {
            shulkerBoxBlockEntity.checkLootInteraction(player);
         }
      }

      super.onBreak(world, pos, state, player);
   }

   public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
      BlockEntity blockEntity = (BlockEntity)builder.getNullable(LootContextParameters.BLOCK_ENTITY);
      if (blockEntity instanceof ShulkerBoxBlockEntity) {
         ShulkerBoxBlockEntity shulkerBoxBlockEntity = (ShulkerBoxBlockEntity)blockEntity;
         builder = builder.putDrop(CONTENTS, (lootContext, consumer) -> {
            for(int i = 0; i < shulkerBoxBlockEntity.getInvSize(); ++i) {
               consumer.accept(shulkerBoxBlockEntity.getInvStack(i));
            }

         });
      }

      return super.getDroppedStacks(state, builder);
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      if (itemStack.hasCustomName()) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof ShulkerBoxBlockEntity) {
            ((ShulkerBoxBlockEntity)blockEntity).setCustomName(itemStack.getName());
         }
      }

   }

   public void onBlockRemoved(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (state.getBlock() != newState.getBlock()) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof ShulkerBoxBlockEntity) {
            world.updateHorizontalAdjacent(pos, state.getBlock());
         }

         super.onBlockRemoved(state, world, pos, newState, moved);
      }
   }

   @Environment(EnvType.CLIENT)
   public void buildTooltip(ItemStack stack, @Nullable BlockView view, List<Text> tooltip, TooltipContext options) {
      super.buildTooltip(stack, view, tooltip, options);
      CompoundTag compoundTag = stack.getSubTag("BlockEntityTag");
      if (compoundTag != null) {
         if (compoundTag.contains("LootTable", 8)) {
            tooltip.add(new LiteralText("???????"));
         }

         if (compoundTag.contains("Items", 9)) {
            DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(27, ItemStack.EMPTY);
            Inventories.fromTag(compoundTag, defaultedList);
            int i = 0;
            int j = 0;
            Iterator var9 = defaultedList.iterator();

            while(var9.hasNext()) {
               ItemStack itemStack = (ItemStack)var9.next();
               if (!itemStack.isEmpty()) {
                  ++j;
                  if (i <= 4) {
                     ++i;
                     Text text = itemStack.getName().deepCopy();
                     text.append(" x").append(String.valueOf(itemStack.getCount()));
                     tooltip.add(text);
                  }
               }
            }

            if (j - i > 0) {
               tooltip.add((new TranslatableText("container.shulkerBox.more", new Object[]{j - i})).formatted(Formatting.ITALIC));
            }
         }
      }

   }

   public PistonBehavior getPistonBehavior(BlockState state) {
      return PistonBehavior.DESTROY;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
      BlockEntity blockEntity = view.getBlockEntity(pos);
      return blockEntity instanceof ShulkerBoxBlockEntity ? VoxelShapes.cuboid(((ShulkerBoxBlockEntity)blockEntity).getBoundingBox(state)) : VoxelShapes.fullCube();
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      return Container.calculateComparatorOutput((Inventory)world.getBlockEntity(pos));
   }

   @Environment(EnvType.CLIENT)
   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      ItemStack itemStack = super.getPickStack(world, pos, state);
      ShulkerBoxBlockEntity shulkerBoxBlockEntity = (ShulkerBoxBlockEntity)world.getBlockEntity(pos);
      CompoundTag compoundTag = shulkerBoxBlockEntity.serializeInventory(new CompoundTag());
      if (!compoundTag.isEmpty()) {
         itemStack.putSubTag("BlockEntityTag", compoundTag);
      }

      return itemStack;
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   public static DyeColor getColor(Item item) {
      return getColor(Block.getBlockFromItem(item));
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   public static DyeColor getColor(Block block) {
      return block instanceof ShulkerBoxBlock ? ((ShulkerBoxBlock)block).getColor() : null;
   }

   public static Block get(@Nullable DyeColor dyeColor) {
      if (dyeColor == null) {
         return Blocks.SHULKER_BOX;
      } else {
         switch(dyeColor) {
         case WHITE:
            return Blocks.WHITE_SHULKER_BOX;
         case ORANGE:
            return Blocks.ORANGE_SHULKER_BOX;
         case MAGENTA:
            return Blocks.MAGENTA_SHULKER_BOX;
         case LIGHT_BLUE:
            return Blocks.LIGHT_BLUE_SHULKER_BOX;
         case YELLOW:
            return Blocks.YELLOW_SHULKER_BOX;
         case LIME:
            return Blocks.LIME_SHULKER_BOX;
         case PINK:
            return Blocks.PINK_SHULKER_BOX;
         case GRAY:
            return Blocks.GRAY_SHULKER_BOX;
         case LIGHT_GRAY:
            return Blocks.LIGHT_GRAY_SHULKER_BOX;
         case CYAN:
            return Blocks.CYAN_SHULKER_BOX;
         case PURPLE:
         default:
            return Blocks.PURPLE_SHULKER_BOX;
         case BLUE:
            return Blocks.BLUE_SHULKER_BOX;
         case BROWN:
            return Blocks.BROWN_SHULKER_BOX;
         case GREEN:
            return Blocks.GREEN_SHULKER_BOX;
         case RED:
            return Blocks.RED_SHULKER_BOX;
         case BLACK:
            return Blocks.BLACK_SHULKER_BOX;
         }
      }
   }

   @Nullable
   public DyeColor getColor() {
      return this.color;
   }

   public static ItemStack getItemStack(@Nullable DyeColor color) {
      return new ItemStack(get(color));
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   static {
      FACING = FacingBlock.FACING;
      CONTENTS = new Identifier("contents");
   }
}
