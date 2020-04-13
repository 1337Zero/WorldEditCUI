package net.minecraft.client.gui.screen.recipebook;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeBook;

@Environment(EnvType.CLIENT)
public class RecipeBookResults {
   private final List<AnimatedResultButton> resultButtons = Lists.newArrayListWithCapacity(20);
   private AnimatedResultButton hoveredResultButton;
   private final RecipeAlternativesWidget alternatesWidget = new RecipeAlternativesWidget();
   private MinecraftClient client;
   private final List<RecipeDisplayListener> recipeDisplayListeners = Lists.newArrayList();
   private List<RecipeResultCollection> resultCollections;
   private ToggleButtonWidget nextPageButton;
   private ToggleButtonWidget prevPageButton;
   private int pageCount;
   private int currentPage;
   private RecipeBook recipeBook;
   private Recipe<?> lastClickedRecipe;
   private RecipeResultCollection resultCollection;

   public RecipeBookResults() {
      for(int i = 0; i < 20; ++i) {
         this.resultButtons.add(new AnimatedResultButton());
      }

   }

   public void initialize(MinecraftClient minecraftClient, int parentLeft, int parentTop) {
      this.client = minecraftClient;
      this.recipeBook = minecraftClient.player.getRecipeBook();

      for(int i = 0; i < this.resultButtons.size(); ++i) {
         ((AnimatedResultButton)this.resultButtons.get(i)).setPos(parentLeft + 11 + 25 * (i % 5), parentTop + 31 + 25 * (i / 5));
      }

      this.nextPageButton = new ToggleButtonWidget(parentLeft + 93, parentTop + 137, 12, 17, false);
      this.nextPageButton.setTextureUV(1, 208, 13, 18, RecipeBookWidget.TEXTURE);
      this.prevPageButton = new ToggleButtonWidget(parentLeft + 38, parentTop + 137, 12, 17, true);
      this.prevPageButton.setTextureUV(1, 208, 13, 18, RecipeBookWidget.TEXTURE);
   }

   public void setGui(RecipeBookWidget recipeBookWidget) {
      this.recipeDisplayListeners.remove(recipeBookWidget);
      this.recipeDisplayListeners.add(recipeBookWidget);
   }

   public void setResults(List<RecipeResultCollection> list, boolean resetCurrentPage) {
      this.resultCollections = list;
      this.pageCount = (int)Math.ceil((double)list.size() / 20.0D);
      if (this.pageCount <= this.currentPage || resetCurrentPage) {
         this.currentPage = 0;
      }

      this.refreshResultButtons();
   }

   private void refreshResultButtons() {
      int i = 20 * this.currentPage;

      for(int j = 0; j < this.resultButtons.size(); ++j) {
         AnimatedResultButton animatedResultButton = (AnimatedResultButton)this.resultButtons.get(j);
         if (i + j < this.resultCollections.size()) {
            RecipeResultCollection recipeResultCollection = (RecipeResultCollection)this.resultCollections.get(i + j);
            animatedResultButton.showResultCollection(recipeResultCollection, this);
            animatedResultButton.visible = true;
         } else {
            animatedResultButton.visible = false;
         }
      }

      this.hideShowPageButtons();
   }

   private void hideShowPageButtons() {
      this.nextPageButton.visible = this.pageCount > 1 && this.currentPage < this.pageCount - 1;
      this.prevPageButton.visible = this.pageCount > 1 && this.currentPage > 0;
   }

   public void draw(int left, int top, int mouseX, int mouseY, float delta) {
      if (this.pageCount > 1) {
         String string = this.currentPage + 1 + "/" + this.pageCount;
         int i = this.client.textRenderer.getStringWidth(string);
         this.client.textRenderer.draw(string, (float)(left - i / 2 + 73), (float)(top + 141), -1);
      }

      this.hoveredResultButton = null;
      Iterator var8 = this.resultButtons.iterator();

      while(var8.hasNext()) {
         AnimatedResultButton animatedResultButton = (AnimatedResultButton)var8.next();
         animatedResultButton.render(mouseX, mouseY, delta);
         if (animatedResultButton.visible && animatedResultButton.isHovered()) {
            this.hoveredResultButton = animatedResultButton;
         }
      }

      this.prevPageButton.render(mouseX, mouseY, delta);
      this.nextPageButton.render(mouseX, mouseY, delta);
      this.alternatesWidget.render(mouseX, mouseY, delta);
   }

   public void drawTooltip(int i, int j) {
      if (this.client.currentScreen != null && this.hoveredResultButton != null && !this.alternatesWidget.isVisible()) {
         this.client.currentScreen.renderTooltip(this.hoveredResultButton.getTooltip(this.client.currentScreen), i, j);
      }

   }

   @Nullable
   public Recipe<?> getLastClickedRecipe() {
      return this.lastClickedRecipe;
   }

   @Nullable
   public RecipeResultCollection getLastClickedResults() {
      return this.resultCollection;
   }

   public void hideAlternates() {
      this.alternatesWidget.setVisible(false);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button, int areaLeft, int areaTop, int areaWidth, int areaHeight) {
      this.lastClickedRecipe = null;
      this.resultCollection = null;
      if (this.alternatesWidget.isVisible()) {
         if (this.alternatesWidget.mouseClicked(mouseX, mouseY, button)) {
            this.lastClickedRecipe = this.alternatesWidget.getLastClickedRecipe();
            this.resultCollection = this.alternatesWidget.getResults();
         } else {
            this.alternatesWidget.setVisible(false);
         }

         return true;
      } else if (this.nextPageButton.mouseClicked(mouseX, mouseY, button)) {
         ++this.currentPage;
         this.refreshResultButtons();
         return true;
      } else if (this.prevPageButton.mouseClicked(mouseX, mouseY, button)) {
         --this.currentPage;
         this.refreshResultButtons();
         return true;
      } else {
         Iterator var10 = this.resultButtons.iterator();

         AnimatedResultButton animatedResultButton;
         do {
            if (!var10.hasNext()) {
               return false;
            }

            animatedResultButton = (AnimatedResultButton)var10.next();
         } while(!animatedResultButton.mouseClicked(mouseX, mouseY, button));

         if (button == 0) {
            this.lastClickedRecipe = animatedResultButton.currentRecipe();
            this.resultCollection = animatedResultButton.getResultCollection();
         } else if (button == 1 && !this.alternatesWidget.isVisible() && !animatedResultButton.hasResults()) {
            this.alternatesWidget.showAlternativesForResult(this.client, animatedResultButton.getResultCollection(), animatedResultButton.x, animatedResultButton.y, areaLeft + areaWidth / 2, areaTop + 13 + areaHeight / 2, (float)animatedResultButton.getWidth());
         }

         return true;
      }
   }

   public void onRecipesDisplayed(List<Recipe<?>> list) {
      Iterator var2 = this.recipeDisplayListeners.iterator();

      while(var2.hasNext()) {
         RecipeDisplayListener recipeDisplayListener = (RecipeDisplayListener)var2.next();
         recipeDisplayListener.onRecipesDisplayed(list);
      }

   }

   public MinecraftClient getMinecraftClient() {
      return this.client;
   }

   public RecipeBook getRecipeBook() {
      return this.recipeBook;
   }
}
