package net.minecraft.client.gui;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ParentElement extends Element {
   List<? extends Element> children();

   default Optional<Element> hoveredElement(double mouseX, double mouseY) {
      Iterator var5 = this.children().iterator();

      Element element;
      do {
         if (!var5.hasNext()) {
            return Optional.empty();
         }

         element = (Element)var5.next();
      } while(!element.isMouseOver(mouseX, mouseY));

      return Optional.of(element);
   }

   default boolean mouseClicked(double mouseX, double mouseY, int button) {
      Iterator var6 = this.children().iterator();

      Element element;
      do {
         if (!var6.hasNext()) {
            return false;
         }

         element = (Element)var6.next();
      } while(!element.mouseClicked(mouseX, mouseY, button));

      this.setFocused(element);
      if (button == 0) {
         this.setDragging(true);
      }

      return true;
   }

   default boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.setDragging(false);
      return this.hoveredElement(mouseX, mouseY).filter((element) -> {
         return element.mouseReleased(mouseX, mouseY, button);
      }).isPresent();
   }

   default boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      return this.getFocused() != null && this.isDragging() && button == 0 ? this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY) : false;
   }

   boolean isDragging();

   void setDragging(boolean dragging);

   default boolean mouseScrolled(double d, double e, double amount) {
      return this.hoveredElement(d, e).filter((element) -> {
         return element.mouseScrolled(d, e, amount);
      }).isPresent();
   }

   default boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.getFocused() != null && this.getFocused().keyPressed(keyCode, scanCode, modifiers);
   }

   default boolean keyReleased(int keyCode, int scanCode, int modifiers) {
      return this.getFocused() != null && this.getFocused().keyReleased(keyCode, scanCode, modifiers);
   }

   default boolean charTyped(char chr, int keyCode) {
      return this.getFocused() != null && this.getFocused().charTyped(chr, keyCode);
   }

   @Nullable
   Element getFocused();

   void setFocused(@Nullable Element focused);

   default void setInitialFocus(@Nullable Element element) {
      this.setFocused(element);
   }

   default void focusOn(@Nullable Element element) {
      this.setFocused(element);
   }

   default boolean changeFocus(boolean bl) {
      Element element = this.getFocused();
      boolean bl2 = element != null;
      if (bl2 && element.changeFocus(bl)) {
         return true;
      } else {
         List<? extends Element> list = this.children();
         int i = list.indexOf(element);
         int l;
         if (bl2 && i >= 0) {
            l = i + (bl ? 1 : 0);
         } else if (bl) {
            l = 0;
         } else {
            l = list.size();
         }

         ListIterator<? extends Element> listIterator = list.listIterator(l);
         BooleanSupplier booleanSupplier = bl ? listIterator::hasNext : listIterator::hasPrevious;
         Supplier supplier = bl ? listIterator::next : listIterator::previous;

         Element element2;
         do {
            if (!booleanSupplier.getAsBoolean()) {
               this.setFocused((Element)null);
               return false;
            }

            element2 = (Element)supplier.get();
         } while(!element2.changeFocus(bl));

         this.setFocused(element2);
         return true;
      }
   }
}
