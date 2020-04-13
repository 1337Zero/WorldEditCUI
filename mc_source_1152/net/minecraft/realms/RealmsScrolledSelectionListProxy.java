package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ListWidget;

@Environment(EnvType.CLIENT)
public class RealmsScrolledSelectionListProxy extends ListWidget {
   private final RealmsScrolledSelectionList realmsScrolledSelectionList;

   public RealmsScrolledSelectionListProxy(RealmsScrolledSelectionList realmsScrolledSelectionList, int i, int j, int k, int l, int m) {
      super(MinecraftClient.getInstance(), i, j, k, l, m);
      this.realmsScrolledSelectionList = realmsScrolledSelectionList;
   }

   public int getItemCount() {
      return this.realmsScrolledSelectionList.getItemCount();
   }

   public boolean selectItem(int index, int button, double mouseX, double mouseY) {
      return this.realmsScrolledSelectionList.selectItem(index, button, mouseX, mouseY);
   }

   public boolean isSelectedItem(int index) {
      return this.realmsScrolledSelectionList.isSelectedItem(index);
   }

   public void renderBackground() {
      this.realmsScrolledSelectionList.renderBackground();
   }

   public void renderItem(int index, int y, int i, int j, int k, int l, float f) {
      this.realmsScrolledSelectionList.renderItem(index, y, i, j, k, l);
   }

   public int getWidth() {
      return this.width;
   }

   public int getMaxPosition() {
      return this.realmsScrolledSelectionList.getMaxPosition();
   }

   public int getScrollbarPosition() {
      return this.realmsScrolledSelectionList.getScrollbarPosition();
   }

   public boolean mouseScrolled(double d, double e, double amount) {
      return this.realmsScrolledSelectionList.mouseScrolled(d, e, amount) ? true : super.mouseScrolled(d, e, amount);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return this.realmsScrolledSelectionList.mouseClicked(mouseX, mouseY, button) ? true : super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      return this.realmsScrolledSelectionList.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      return this.realmsScrolledSelectionList.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
   }
}
