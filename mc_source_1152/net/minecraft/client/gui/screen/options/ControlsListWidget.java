package net.minecraft.client.gui.screen.options;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.ArrayUtils;

@Environment(EnvType.CLIENT)
public class ControlsListWidget extends ElementListWidget<ControlsListWidget.Entry> {
   private final ControlsOptionsScreen gui;
   private int maxKeyNameLength;

   public ControlsListWidget(ControlsOptionsScreen gui, MinecraftClient client) {
      super(client, gui.width + 45, gui.height, 43, gui.height - 32, 20);
      this.gui = gui;
      KeyBinding[] keyBindings = (KeyBinding[])ArrayUtils.clone(client.options.keysAll);
      Arrays.sort(keyBindings);
      String string = null;
      KeyBinding[] var5 = keyBindings;
      int var6 = keyBindings.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         KeyBinding keyBinding = var5[var7];
         String string2 = keyBinding.getCategory();
         if (!string2.equals(string)) {
            string = string2;
            this.addEntry(new ControlsListWidget.CategoryEntry(string2));
         }

         int i = client.textRenderer.getStringWidth(I18n.translate(keyBinding.getId()));
         if (i > this.maxKeyNameLength) {
            this.maxKeyNameLength = i;
         }

         this.addEntry(new ControlsListWidget.KeyBindingEntry(keyBinding));
      }

   }

   protected int getScrollbarPosition() {
      return super.getScrollbarPosition() + 15;
   }

   public int getRowWidth() {
      return super.getRowWidth() + 32;
   }

   @Environment(EnvType.CLIENT)
   public class KeyBindingEntry extends ControlsListWidget.Entry {
      private final KeyBinding binding;
      private final String bindingName;
      private final ButtonWidget editButton;
      private final ButtonWidget resetButton;

      private KeyBindingEntry(final KeyBinding binding) {
         this.binding = binding;
         this.bindingName = I18n.translate(binding.getId());
         this.editButton = new ButtonWidget(0, 0, 75, 20, this.bindingName, (buttonWidget) -> {
            ControlsListWidget.this.gui.focusedBinding = binding;
         }) {
            protected String getNarrationMessage() {
               return binding.isNotBound() ? I18n.translate("narrator.controls.unbound", KeyBindingEntry.this.bindingName) : I18n.translate("narrator.controls.bound", KeyBindingEntry.this.bindingName, super.getNarrationMessage());
            }
         };
         this.resetButton = new ButtonWidget(0, 0, 50, 20, I18n.translate("controls.reset"), (buttonWidget) -> {
            ControlsListWidget.this.minecraft.options.setKeyCode(binding, binding.getDefaultKeyCode());
            KeyBinding.updateKeysByCode();
         }) {
            protected String getNarrationMessage() {
               return I18n.translate("narrator.controls.reset", KeyBindingEntry.this.bindingName);
            }
         };
      }

      public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
         boolean bl2 = ControlsListWidget.this.gui.focusedBinding == this.binding;
         TextRenderer var10000 = ControlsListWidget.this.minecraft.textRenderer;
         String var10001 = this.bindingName;
         float var10002 = (float)(k + 90 - ControlsListWidget.this.maxKeyNameLength);
         int var10003 = j + m / 2;
         ControlsListWidget.this.minecraft.textRenderer.getClass();
         var10000.draw(var10001, var10002, (float)(var10003 - 9 / 2), 16777215);
         this.resetButton.x = k + 190;
         this.resetButton.y = j;
         this.resetButton.active = !this.binding.isDefault();
         this.resetButton.render(n, o, f);
         this.editButton.x = k + 105;
         this.editButton.y = j;
         this.editButton.setMessage(this.binding.getLocalizedName());
         boolean bl3 = false;
         if (!this.binding.isNotBound()) {
            KeyBinding[] var12 = ControlsListWidget.this.minecraft.options.keysAll;
            int var13 = var12.length;

            for(int var14 = 0; var14 < var13; ++var14) {
               KeyBinding keyBinding = var12[var14];
               if (keyBinding != this.binding && this.binding.equals(keyBinding)) {
                  bl3 = true;
                  break;
               }
            }
         }

         if (bl2) {
            this.editButton.setMessage(Formatting.WHITE + "> " + Formatting.YELLOW + this.editButton.getMessage() + Formatting.WHITE + " <");
         } else if (bl3) {
            this.editButton.setMessage(Formatting.RED + this.editButton.getMessage());
         }

         this.editButton.render(n, o, f);
      }

      public List<? extends Element> children() {
         return ImmutableList.of(this.editButton, this.resetButton);
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         if (this.editButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
         } else {
            return this.resetButton.mouseClicked(mouseX, mouseY, button);
         }
      }

      public boolean mouseReleased(double mouseX, double mouseY, int button) {
         return this.editButton.mouseReleased(mouseX, mouseY, button) || this.resetButton.mouseReleased(mouseX, mouseY, button);
      }
   }

   @Environment(EnvType.CLIENT)
   public class CategoryEntry extends ControlsListWidget.Entry {
      private final String name;
      private final int nameWidth;

      public CategoryEntry(String translationKey) {
         this.name = I18n.translate(translationKey);
         this.nameWidth = ControlsListWidget.this.minecraft.textRenderer.getStringWidth(this.name);
      }

      public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
         TextRenderer var10000 = ControlsListWidget.this.minecraft.textRenderer;
         String var10001 = this.name;
         float var10002 = (float)(ControlsListWidget.this.minecraft.currentScreen.width / 2 - this.nameWidth / 2);
         int var10003 = j + m;
         ControlsListWidget.this.minecraft.textRenderer.getClass();
         var10000.draw(var10001, var10002, (float)(var10003 - 9 - 1), 16777215);
      }

      public boolean changeFocus(boolean bl) {
         return false;
      }

      public List<? extends Element> children() {
         return Collections.emptyList();
      }
   }

   @Environment(EnvType.CLIENT)
   public abstract static class Entry extends ElementListWidget.Entry<ControlsListWidget.Entry> {
   }
}
