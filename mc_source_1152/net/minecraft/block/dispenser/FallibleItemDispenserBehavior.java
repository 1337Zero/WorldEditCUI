package net.minecraft.block.dispenser;

import net.minecraft.util.math.BlockPointer;

public abstract class FallibleItemDispenserBehavior extends ItemDispenserBehavior {
   protected boolean success = true;

   protected void playSound(BlockPointer pointer) {
      pointer.getWorld().playLevelEvent(this.success ? 1000 : 1001, pointer.getBlockPos(), 0);
   }
}
