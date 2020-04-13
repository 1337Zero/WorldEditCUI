package net.minecraft.client.gui.screen.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.recipebook.AbstractFurnaceRecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.container.AbstractFurnaceContainer;
import net.minecraft.container.CraftingContainer;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public abstract class AbstractFurnaceScreen<T extends AbstractFurnaceContainer> extends AbstractContainerScreen<T> implements RecipeBookProvider {
   private static final Identifier RECIPE_BUTTON_TEXTURE = new Identifier("textures/gui/recipe_button.png");
   public final AbstractFurnaceRecipeBookScreen recipeBook;
   private boolean narrow;
   private final Identifier background;

   public AbstractFurnaceScreen(T container, AbstractFurnaceRecipeBookScreen recipeBook, PlayerInventory inventory, Text title, Identifier background) {
      super(container, inventory, title);
      this.recipeBook = recipeBook;
      this.background = background;
   }

   public void init() {
      super.init();
      this.narrow = this.width < 379;
      this.recipeBook.initialize(this.width, this.height, this.minecraft, this.narrow, (CraftingContainer)this.container);
      this.x = this.recipeBook.findLeftEdge(this.narrow, this.width, this.containerWidth);
      this.addButton(new TexturedButtonWidget(this.x + 20, this.height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, (buttonWidget) -> {
         this.recipeBook.reset(this.narrow);
         this.recipeBook.toggleOpen();
         this.x = this.recipeBook.findLeftEdge(this.narrow, this.width, this.containerWidth);
         ((TexturedButtonWidget)buttonWidget).setPos(this.x + 20, this.height / 2 - 49);
      }));
   }

   public void tick() {
      super.tick();
      this.recipeBook.update();
   }

   public void render(int mouseX, int mouseY, float delta) {
      this.renderBackground();
      if (this.recipeBook.isOpen() && this.narrow) {
         this.drawBackground(delta, mouseX, mouseY);
         this.recipeBook.render(mouseX, mouseY, delta);
      } else {
         this.recipeBook.render(mouseX, mouseY, delta);
         super.render(mouseX, mouseY, delta);
         this.recipeBook.drawGhostSlots(this.x, this.y, true, delta);
      }

      this.drawMouseoverTooltip(mouseX, mouseY);
      this.recipeBook.drawTooltip(this.x, this.y, mouseX, mouseY);
   }

   protected void drawForeground(int mouseX, int mouseY) {
      String string = this.title.asFormattedString();
      this.font.draw(string, (float)(this.containerWidth / 2 - this.font.getStringWidth(string) / 2), 6.0F, 4210752);
      this.font.draw(this.playerInventory.getDisplayName().asFormattedString(), 8.0F, (float)(this.containerHeight - 96 + 2), 4210752);
   }

   protected void drawBackground(float delta, int mouseX, int mouseY) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.minecraft.getTextureManager().bindTexture(this.background);
      int i = this.x;
      int j = this.y;
      this.blit(i, j, 0, 0, this.containerWidth, this.containerHeight);
      int l;
      if (((AbstractFurnaceContainer)this.container).isBurning()) {
         l = ((AbstractFurnaceContainer)this.container).getFuelProgress();
         this.blit(i + 56, j + 36 + 12 - l, 176, 12 - l, 14, l + 1);
      }

      l = ((AbstractFurnaceContainer)this.container).getCookProgress();
      this.blit(i + 79, j + 34, 176, 14, l + 1, 16);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.recipeBook.mouseClicked(mouseX, mouseY, button)) {
         return true;
      } else {
         return this.narrow && this.recipeBook.isOpen() ? true : super.mouseClicked(mouseX, mouseY, button);
      }
   }

   protected void onMouseClick(Slot slot, int invSlot, int button, SlotActionType slotActionType) {
      super.onMouseClick(slot, invSlot, button, slotActionType);
      this.recipeBook.slotClicked(slot);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.recipeBook.keyPressed(keyCode, scanCode, modifiers) ? false : super.keyPressed(keyCode, scanCode, modifiers);
   }

   protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
      boolean bl = mouseX < (double)left || mouseY < (double)top || mouseX >= (double)(left + this.containerWidth) || mouseY >= (double)(top + this.containerHeight);
      return this.recipeBook.isClickOutsideBounds(mouseX, mouseY, this.x, this.y, this.containerWidth, this.containerHeight, button) && bl;
   }

   public boolean charTyped(char chr, int keyCode) {
      return this.recipeBook.charTyped(chr, keyCode) ? true : super.charTyped(chr, keyCode);
   }

   public void refreshRecipeBook() {
      this.recipeBook.refresh();
   }

   public RecipeBookWidget getRecipeBookWidget() {
      return this.recipeBook;
   }

   public void removed() {
      this.recipeBook.close();
      super.removed();
   }
}
