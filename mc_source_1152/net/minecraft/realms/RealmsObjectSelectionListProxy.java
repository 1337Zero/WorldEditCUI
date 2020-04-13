package net.minecraft.realms;

import java.util.Collection;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;

@Environment(EnvType.CLIENT)
public class RealmsObjectSelectionListProxy<E extends AlwaysSelectedEntryListWidget.Entry<E>> extends AlwaysSelectedEntryListWidget<E> {
   private final RealmsObjectSelectionList realmsObjectSelectionList;

   public RealmsObjectSelectionListProxy(RealmsObjectSelectionList realmsObjectSelectionList, int i, int j, int k, int l, int m) {
      super(MinecraftClient.getInstance(), i, j, k, l, m);
      this.realmsObjectSelectionList = realmsObjectSelectionList;
   }

   public int getItemCount() {
      return super.getItemCount();
   }

   public void clear() {
      super.clearEntries();
   }

   public boolean isFocused() {
      return this.realmsObjectSelectionList.isFocused();
   }

   protected void setSelectedItem(int i) {
      if (i == -1) {
         super.setSelected((EntryListWidget.Entry)null);
      } else if (super.getItemCount() != 0) {
         E entry = (AlwaysSelectedEntryListWidget.Entry)super.getEntry(i);
         super.setSelected(entry);
      }

   }

   public void setSelected(@Nullable E entry) {
      super.setSelected(entry);
      this.realmsObjectSelectionList.selectItem(super.children().indexOf(entry));
   }

   public void renderBackground() {
      this.realmsObjectSelectionList.renderBackground();
   }

   public int getWidth() {
      return this.width;
   }

   public int getMaxPosition() {
      return this.realmsObjectSelectionList.getMaxPosition();
   }

   public int getScrollbarPosition() {
      return this.realmsObjectSelectionList.getScrollbarPosition();
   }

   public boolean mouseScrolled(double d, double e, double amount) {
      return this.realmsObjectSelectionList.mouseScrolled(d, e, amount) ? true : super.mouseScrolled(d, e, amount);
   }

   public int getRowWidth() {
      return this.realmsObjectSelectionList.getRowWidth();
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return this.realmsObjectSelectionList.mouseClicked(mouseX, mouseY, button) ? true : access$001(this, mouseX, mouseY, button);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      return this.realmsObjectSelectionList.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      return this.realmsObjectSelectionList.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) ? true : super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
   }

   protected final int addEntry(E entry) {
      return super.addEntry(entry);
   }

   public E remove(int i) {
      return (AlwaysSelectedEntryListWidget.Entry)super.remove(i);
   }

   public boolean removeEntry(E entry) {
      return super.removeEntry(entry);
   }

   public void setScrollAmount(double d) {
      super.setScrollAmount(d);
   }

   public int y0() {
      return this.top;
   }

   public int y1() {
      return this.bottom;
   }

   public int headerHeight() {
      return this.headerHeight;
   }

   public int itemHeight() {
      return this.itemHeight;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return super.keyPressed(keyCode, scanCode, modifiers) ? true : this.realmsObjectSelectionList.keyPressed(keyCode, scanCode, modifiers);
   }

   public void replaceEntries(Collection<E> collection) {
      super.replaceEntries(collection);
   }

   public int getRowTop(int i) {
      return super.getRowTop(i);
   }

   public int getRowLeft() {
      return super.getRowLeft();
   }
}
