package net.minecraft.client.tutorial;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.text.KeybindText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

@Environment(EnvType.CLIENT)
public class TutorialManager {
   private final MinecraftClient client;
   @Nullable
   private TutorialStepHandler currentHandler;

   public TutorialManager(MinecraftClient minecraftClient) {
      this.client = minecraftClient;
   }

   public void onMovement(Input input) {
      if (this.currentHandler != null) {
         this.currentHandler.onMovement(input);
      }

   }

   public void onUpdateMouse(double deltaX, double deltaY) {
      if (this.currentHandler != null) {
         this.currentHandler.onMouseUpdate(deltaX, deltaY);
      }

   }

   public void tick(@Nullable ClientWorld world, @Nullable HitResult hitResult) {
      if (this.currentHandler != null && hitResult != null && world != null) {
         this.currentHandler.onTarget(world, hitResult);
      }

   }

   public void onBlockAttacked(ClientWorld world, BlockPos pos, BlockState state, float f) {
      if (this.currentHandler != null) {
         this.currentHandler.onBlockAttacked(world, pos, state, f);
      }

   }

   public void onInventoryOpened() {
      if (this.currentHandler != null) {
         this.currentHandler.onInventoryOpened();
      }

   }

   public void onSlotUpdate(ItemStack itemStack) {
      if (this.currentHandler != null) {
         this.currentHandler.onSlotUpdate(itemStack);
      }

   }

   public void destroyHandler() {
      if (this.currentHandler != null) {
         this.currentHandler.destroy();
         this.currentHandler = null;
      }
   }

   public void createHandler() {
      if (this.currentHandler != null) {
         this.destroyHandler();
      }

      this.currentHandler = this.client.options.tutorialStep.createHandler(this);
   }

   public void tick() {
      if (this.currentHandler != null) {
         if (this.client.world != null) {
            this.currentHandler.tick();
         } else {
            this.destroyHandler();
         }
      } else if (this.client.world != null) {
         this.createHandler();
      }

   }

   public void setStep(TutorialStep tutorialStep) {
      this.client.options.tutorialStep = tutorialStep;
      this.client.options.write();
      if (this.currentHandler != null) {
         this.currentHandler.destroy();
         this.currentHandler = tutorialStep.createHandler(this);
      }

   }

   public MinecraftClient getClient() {
      return this.client;
   }

   public GameMode getGameMode() {
      return this.client.interactionManager == null ? GameMode.NOT_SET : this.client.interactionManager.getCurrentGameMode();
   }

   public static Text getKeybindName(String string) {
      return (new KeybindText("key." + string)).formatted(Formatting.BOLD);
   }
}
