package net.minecraft.client.network;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.StructureBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.recipe.book.ClientRecipeBook;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.PosAndRot;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.network.packet.ButtonClickC2SPacket;
import net.minecraft.server.network.packet.ClickWindowC2SPacket;
import net.minecraft.server.network.packet.CraftRequestC2SPacket;
import net.minecraft.server.network.packet.CreativeInventoryActionC2SPacket;
import net.minecraft.server.network.packet.PickFromInventoryC2SPacket;
import net.minecraft.server.network.packet.PlayerActionC2SPacket;
import net.minecraft.server.network.packet.PlayerInteractBlockC2SPacket;
import net.minecraft.server.network.packet.PlayerInteractEntityC2SPacket;
import net.minecraft.server.network.packet.PlayerInteractItemC2SPacket;
import net.minecraft.server.network.packet.UpdateSelectedSlotC2SPacket;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientPlayerInteractionManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private final MinecraftClient client;
   private final ClientPlayNetworkHandler networkHandler;
   private BlockPos currentBreakingPos = new BlockPos(-1, -1, -1);
   private ItemStack selectedStack;
   private float currentBreakingProgress;
   private float blockBreakingSoundCooldown;
   private int blockBreakingCooldown;
   private boolean breakingBlock;
   private GameMode gameMode;
   private final Object2ObjectLinkedOpenHashMap<Pair<BlockPos, PlayerActionC2SPacket.Action>, PosAndRot> unacknowledgedPlayerActions;
   private int lastSelectedSlot;

   public ClientPlayerInteractionManager(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler) {
      this.selectedStack = ItemStack.EMPTY;
      this.gameMode = GameMode.SURVIVAL;
      this.unacknowledgedPlayerActions = new Object2ObjectLinkedOpenHashMap();
      this.client = minecraftClient;
      this.networkHandler = clientPlayNetworkHandler;
   }

   public static void breakBlockOrFire(MinecraftClient minecraftClient, ClientPlayerInteractionManager clientPlayerInteractionManager, BlockPos blockPos, Direction direction) {
      if (!minecraftClient.world.extinguishFire(minecraftClient.player, blockPos, direction)) {
         clientPlayerInteractionManager.breakBlock(blockPos);
      }

   }

   public void copyAbilities(PlayerEntity playerEntity) {
      this.gameMode.setAbilitites(playerEntity.abilities);
   }

   public void setGameMode(GameMode gameMode) {
      this.gameMode = gameMode;
      this.gameMode.setAbilitites(this.client.player.abilities);
   }

   public boolean hasStatusBars() {
      return this.gameMode.isSurvivalLike();
   }

   public boolean breakBlock(BlockPos blockPos) {
      if (this.client.player.canMine(this.client.world, blockPos, this.gameMode)) {
         return false;
      } else {
         World world = this.client.world;
         BlockState blockState = world.getBlockState(blockPos);
         if (!this.client.player.getMainHandStack().getItem().canMine(blockState, world, blockPos, this.client.player)) {
            return false;
         } else {
            Block block = blockState.getBlock();
            if ((block instanceof CommandBlock || block instanceof StructureBlock || block instanceof JigsawBlock) && !this.client.player.isCreativeLevelTwoOp()) {
               return false;
            } else if (blockState.isAir()) {
               return false;
            } else {
               block.onBreak(world, blockPos, blockState, this.client.player);
               FluidState fluidState = world.getFluidState(blockPos);
               boolean bl = world.setBlockState(blockPos, fluidState.getBlockState(), 11);
               if (bl) {
                  block.onBroken(world, blockPos, blockState);
               }

               return bl;
            }
         }
      }
   }

   public boolean attackBlock(BlockPos pos, Direction direction) {
      if (this.client.player.canMine(this.client.world, pos, this.gameMode)) {
         return false;
      } else if (!this.client.world.getWorldBorder().contains(pos)) {
         return false;
      } else {
         BlockState blockState2;
         if (this.gameMode.isCreative()) {
            blockState2 = this.client.world.getBlockState(pos);
            this.client.getTutorialManager().onBlockAttacked(this.client.world, pos, blockState2, 1.0F);
            this.sendPlayerAction(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction);
            breakBlockOrFire(this.client, this, pos, direction);
            this.blockBreakingCooldown = 5;
         } else if (!this.breakingBlock || !this.isCurrentlyBreaking(pos)) {
            if (this.breakingBlock) {
               this.sendPlayerAction(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, this.currentBreakingPos, direction);
            }

            blockState2 = this.client.world.getBlockState(pos);
            this.client.getTutorialManager().onBlockAttacked(this.client.world, pos, blockState2, 0.0F);
            this.sendPlayerAction(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction);
            boolean bl = !blockState2.isAir();
            if (bl && this.currentBreakingProgress == 0.0F) {
               blockState2.onBlockBreakStart(this.client.world, pos, this.client.player);
            }

            if (bl && blockState2.calcBlockBreakingDelta(this.client.player, this.client.player.world, pos) >= 1.0F) {
               this.breakBlock(pos);
            } else {
               this.breakingBlock = true;
               this.currentBreakingPos = pos;
               this.selectedStack = this.client.player.getMainHandStack();
               this.currentBreakingProgress = 0.0F;
               this.blockBreakingSoundCooldown = 0.0F;
               this.client.world.setBlockBreakingInfo(this.client.player.getEntityId(), this.currentBreakingPos, (int)(this.currentBreakingProgress * 10.0F) - 1);
            }
         }

         return true;
      }
   }

   public void cancelBlockBreaking() {
      if (this.breakingBlock) {
         BlockState blockState = this.client.world.getBlockState(this.currentBreakingPos);
         this.client.getTutorialManager().onBlockAttacked(this.client.world, this.currentBreakingPos, blockState, -1.0F);
         this.sendPlayerAction(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, this.currentBreakingPos, Direction.DOWN);
         this.breakingBlock = false;
         this.currentBreakingProgress = 0.0F;
         this.client.world.setBlockBreakingInfo(this.client.player.getEntityId(), this.currentBreakingPos, -1);
         this.client.player.resetLastAttackedTicks();
      }

   }

   public boolean updateBlockBreakingProgress(BlockPos blockPos, Direction direction) {
      this.syncSelectedSlot();
      if (this.blockBreakingCooldown > 0) {
         --this.blockBreakingCooldown;
         return true;
      } else {
         BlockState blockState2;
         if (this.gameMode.isCreative() && this.client.world.getWorldBorder().contains(blockPos)) {
            this.blockBreakingCooldown = 5;
            blockState2 = this.client.world.getBlockState(blockPos);
            this.client.getTutorialManager().onBlockAttacked(this.client.world, blockPos, blockState2, 1.0F);
            this.sendPlayerAction(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, direction);
            breakBlockOrFire(this.client, this, blockPos, direction);
            return true;
         } else if (this.isCurrentlyBreaking(blockPos)) {
            blockState2 = this.client.world.getBlockState(blockPos);
            if (blockState2.isAir()) {
               this.breakingBlock = false;
               return false;
            } else {
               this.currentBreakingProgress += blockState2.calcBlockBreakingDelta(this.client.player, this.client.player.world, blockPos);
               if (this.blockBreakingSoundCooldown % 4.0F == 0.0F) {
                  BlockSoundGroup blockSoundGroup = blockState2.getSoundGroup();
                  this.client.getSoundManager().play(new PositionedSoundInstance(blockSoundGroup.getHitSound(), SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 8.0F, blockSoundGroup.getPitch() * 0.5F, blockPos));
               }

               ++this.blockBreakingSoundCooldown;
               this.client.getTutorialManager().onBlockAttacked(this.client.world, blockPos, blockState2, MathHelper.clamp(this.currentBreakingProgress, 0.0F, 1.0F));
               if (this.currentBreakingProgress >= 1.0F) {
                  this.breakingBlock = false;
                  this.sendPlayerAction(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction);
                  this.breakBlock(blockPos);
                  this.currentBreakingProgress = 0.0F;
                  this.blockBreakingSoundCooldown = 0.0F;
                  this.blockBreakingCooldown = 5;
               }

               this.client.world.setBlockBreakingInfo(this.client.player.getEntityId(), this.currentBreakingPos, (int)(this.currentBreakingProgress * 10.0F) - 1);
               return true;
            }
         } else {
            return this.attackBlock(blockPos, direction);
         }
      }
   }

   public float getReachDistance() {
      return this.gameMode.isCreative() ? 5.0F : 4.5F;
   }

   public void tick() {
      this.syncSelectedSlot();
      if (this.networkHandler.getConnection().isOpen()) {
         this.networkHandler.getConnection().tick();
      } else {
         this.networkHandler.getConnection().handleDisconnection();
      }

   }

   private boolean isCurrentlyBreaking(BlockPos pos) {
      ItemStack itemStack = this.client.player.getMainHandStack();
      boolean bl = this.selectedStack.isEmpty() && itemStack.isEmpty();
      if (!this.selectedStack.isEmpty() && !itemStack.isEmpty()) {
         bl = itemStack.getItem() == this.selectedStack.getItem() && ItemStack.areTagsEqual(itemStack, this.selectedStack) && (itemStack.isDamageable() || itemStack.getDamage() == this.selectedStack.getDamage());
      }

      return pos.equals(this.currentBreakingPos) && bl;
   }

   private void syncSelectedSlot() {
      int i = this.client.player.inventory.selectedSlot;
      if (i != this.lastSelectedSlot) {
         this.lastSelectedSlot = i;
         this.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.lastSelectedSlot));
      }

   }

   public ActionResult interactBlock(ClientPlayerEntity player, ClientWorld world, Hand hand, BlockHitResult hitResult) {
      this.syncSelectedSlot();
      BlockPos blockPos = hitResult.getBlockPos();
      if (!this.client.world.getWorldBorder().contains(blockPos)) {
         return ActionResult.FAIL;
      } else {
         ItemStack itemStack = player.getStackInHand(hand);
         if (this.gameMode == GameMode.SPECTATOR) {
            this.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, hitResult));
            return ActionResult.SUCCESS;
         } else {
            boolean bl = !player.getMainHandStack().isEmpty() || !player.getOffHandStack().isEmpty();
            boolean bl2 = player.shouldCancelInteraction() && bl;
            ActionResult actionResult3;
            if (!bl2) {
               actionResult3 = world.getBlockState(blockPos).onUse(world, player, hand, hitResult);
               if (actionResult3.isAccepted()) {
                  this.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, hitResult));
                  return actionResult3;
               }
            }

            this.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, hitResult));
            if (!itemStack.isEmpty() && !player.getItemCooldownManager().isCoolingDown(itemStack.getItem())) {
               ItemUsageContext itemUsageContext = new ItemUsageContext(player, hand, hitResult);
               if (this.gameMode.isCreative()) {
                  int i = itemStack.getCount();
                  actionResult3 = itemStack.useOnBlock(itemUsageContext);
                  itemStack.setCount(i);
               } else {
                  actionResult3 = itemStack.useOnBlock(itemUsageContext);
               }

               return actionResult3;
            } else {
               return ActionResult.PASS;
            }
         }
      }
   }

   public ActionResult interactItem(PlayerEntity playerEntity, World world, Hand hand) {
      if (this.gameMode == GameMode.SPECTATOR) {
         return ActionResult.PASS;
      } else {
         this.syncSelectedSlot();
         this.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(hand));
         ItemStack itemStack = playerEntity.getStackInHand(hand);
         if (playerEntity.getItemCooldownManager().isCoolingDown(itemStack.getItem())) {
            return ActionResult.PASS;
         } else {
            int i = itemStack.getCount();
            TypedActionResult<ItemStack> typedActionResult = itemStack.use(world, playerEntity, hand);
            ItemStack itemStack2 = (ItemStack)typedActionResult.getValue();
            if (itemStack2 != itemStack || itemStack2.getCount() != i) {
               playerEntity.setStackInHand(hand, itemStack2);
            }

            return typedActionResult.getResult();
         }
      }
   }

   public ClientPlayerEntity createPlayer(ClientWorld world, StatHandler stateHandler, ClientRecipeBook recipeBook) {
      return new ClientPlayerEntity(this.client, world, this.networkHandler, stateHandler, recipeBook);
   }

   public void attackEntity(PlayerEntity player, Entity target) {
      this.syncSelectedSlot();
      this.networkHandler.sendPacket(new PlayerInteractEntityC2SPacket(target));
      if (this.gameMode != GameMode.SPECTATOR) {
         player.attack(target);
         player.resetLastAttackedTicks();
      }

   }

   public ActionResult interactEntity(PlayerEntity player, Entity entity, Hand hand) {
      this.syncSelectedSlot();
      this.networkHandler.sendPacket(new PlayerInteractEntityC2SPacket(entity, hand));
      return this.gameMode == GameMode.SPECTATOR ? ActionResult.PASS : player.interact(entity, hand);
   }

   public ActionResult interactEntityAtLocation(PlayerEntity player, Entity entity, EntityHitResult hitResult, Hand hand) {
      this.syncSelectedSlot();
      Vec3d vec3d = hitResult.getPos().subtract(entity.getX(), entity.getY(), entity.getZ());
      this.networkHandler.sendPacket(new PlayerInteractEntityC2SPacket(entity, hand, vec3d));
      return this.gameMode == GameMode.SPECTATOR ? ActionResult.PASS : entity.interactAt(player, vec3d, hand);
   }

   public ItemStack clickSlot(int syncId, int slotId, int mouseButton, SlotActionType actionType, PlayerEntity player) {
      short s = player.container.getNextActionId(player.inventory);
      ItemStack itemStack = player.container.onSlotClick(slotId, mouseButton, actionType, player);
      this.networkHandler.sendPacket(new ClickWindowC2SPacket(syncId, slotId, mouseButton, actionType, itemStack, s));
      return itemStack;
   }

   public void clickRecipe(int syncId, Recipe<?> recipe, boolean craftAll) {
      this.networkHandler.sendPacket(new CraftRequestC2SPacket(syncId, recipe, craftAll));
   }

   public void clickButton(int syncId, int buttonId) {
      this.networkHandler.sendPacket(new ButtonClickC2SPacket(syncId, buttonId));
   }

   public void clickCreativeStack(ItemStack stack, int slotId) {
      if (this.gameMode.isCreative()) {
         this.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(slotId, stack));
      }

   }

   public void dropCreativeStack(ItemStack stack) {
      if (this.gameMode.isCreative() && !stack.isEmpty()) {
         this.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(-1, stack));
      }

   }

   public void stopUsingItem(PlayerEntity player) {
      this.syncSelectedSlot();
      this.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
      player.stopUsingItem();
   }

   public boolean hasExperienceBar() {
      return this.gameMode.isSurvivalLike();
   }

   public boolean hasLimitedAttackSpeed() {
      return !this.gameMode.isCreative();
   }

   public boolean hasCreativeInventory() {
      return this.gameMode.isCreative();
   }

   public boolean hasExtendedReach() {
      return this.gameMode.isCreative();
   }

   public boolean hasRidingInventory() {
      return this.client.player.hasVehicle() && this.client.player.getVehicle() instanceof HorseBaseEntity;
   }

   public boolean isFlyingLocked() {
      return this.gameMode == GameMode.SPECTATOR;
   }

   public GameMode getCurrentGameMode() {
      return this.gameMode;
   }

   public boolean isBreakingBlock() {
      return this.breakingBlock;
   }

   public void pickFromInventory(int slot) {
      this.networkHandler.sendPacket(new PickFromInventoryC2SPacket(slot));
   }

   private void sendPlayerAction(PlayerActionC2SPacket.Action action, BlockPos blockPos, Direction direction) {
      ClientPlayerEntity clientPlayerEntity = this.client.player;
      this.unacknowledgedPlayerActions.put(Pair.of(blockPos, action), new PosAndRot(clientPlayerEntity.getPos(), clientPlayerEntity.pitch, clientPlayerEntity.yaw));
      this.networkHandler.sendPacket(new PlayerActionC2SPacket(action, blockPos, direction));
   }

   public void processPlayerActionResponse(ClientWorld world, BlockPos pos, BlockState blockState, PlayerActionC2SPacket.Action action, boolean approved) {
      PosAndRot posAndRot = (PosAndRot)this.unacknowledgedPlayerActions.remove(Pair.of(pos, action));
      if (posAndRot == null || !approved || action != PlayerActionC2SPacket.Action.START_DESTROY_BLOCK && world.getBlockState(pos) != blockState) {
         world.setBlockStateWithoutNeighborUpdates(pos, blockState);
         if (posAndRot != null) {
            Vec3d vec3d = posAndRot.getPos();
            this.client.player.updatePositionAndAngles(vec3d.x, vec3d.y, vec3d.z, posAndRot.getYaw(), posAndRot.getPitch());
         }
      }

      while(this.unacknowledgedPlayerActions.size() >= 50) {
         Pair<BlockPos, PlayerActionC2SPacket.Action> pair = (Pair)this.unacknowledgedPlayerActions.firstKey();
         this.unacknowledgedPlayerActions.removeFirst();
         LOGGER.error("Too many unacked block actions, dropping " + pair);
      }

   }
}
