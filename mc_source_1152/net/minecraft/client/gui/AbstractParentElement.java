package net.minecraft.client.gui;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class AbstractParentElement extends DrawableHelper implements ParentElement {
   @Nullable
   private Element focused;
   private boolean isDragging;

   public final boolean isDragging() {
      return this.isDragging;
   }

   public final void setDragging(boolean dragging) {
      this.isDragging = dragging;
   }

   @Nullable
   public Element getFocused() {
      return this.focused;
   }

   public void setFocused(@Nullable Element focused) {
      this.focused = focused;
   }
}
