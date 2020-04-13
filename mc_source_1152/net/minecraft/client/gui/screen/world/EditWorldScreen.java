package net.minecraft.client.gui.screen.world;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.BackupPromptScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.io.FileUtils;

@Environment(EnvType.CLIENT)
public class EditWorldScreen extends Screen {
   private ButtonWidget saveButton;
   private final BooleanConsumer callback;
   private TextFieldWidget levelNameTextField;
   private final String levelName;

   public EditWorldScreen(BooleanConsumer callback, String levelName) {
      super(new TranslatableText("selectWorld.edit.title", new Object[0]));
      this.callback = callback;
      this.levelName = levelName;
   }

   public void tick() {
      this.levelNameTextField.tick();
   }

   protected void init() {
      this.minecraft.keyboard.enableRepeatEvents(true);
      ButtonWidget buttonWidget = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 24 + 5, 200, 20, I18n.translate("selectWorld.edit.resetIcon"), (buttonWidgetx) -> {
         LevelStorage levelStorage = this.minecraft.getLevelStorage();
         FileUtils.deleteQuietly(levelStorage.resolveFile(this.levelName, "icon.png"));
         buttonWidgetx.active = false;
      }));
      this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 48 + 5, 200, 20, I18n.translate("selectWorld.edit.openFolder"), (buttonWidgetx) -> {
         LevelStorage levelStorage = this.minecraft.getLevelStorage();
         Util.getOperatingSystem().open(levelStorage.resolveFile(this.levelName, "icon.png").getParentFile());
      }));
      this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 72 + 5, 200, 20, I18n.translate("selectWorld.edit.backup"), (buttonWidgetx) -> {
         LevelStorage levelStorage = this.minecraft.getLevelStorage();
         backupLevel(levelStorage, this.levelName);
         this.callback.accept(false);
      }));
      this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 96 + 5, 200, 20, I18n.translate("selectWorld.edit.backupFolder"), (buttonWidgetx) -> {
         LevelStorage levelStorage = this.minecraft.getLevelStorage();
         Path path = levelStorage.getBackupsDirectory();

         try {
            Files.createDirectories(Files.exists(path, new LinkOption[0]) ? path.toRealPath() : path);
         } catch (IOException var5) {
            throw new RuntimeException(var5);
         }

         Util.getOperatingSystem().open(path.toFile());
      }));
      this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 120 + 5, 200, 20, I18n.translate("selectWorld.edit.optimize"), (buttonWidgetx) -> {
         this.minecraft.openScreen(new BackupPromptScreen(this, (bl, bl2) -> {
            if (bl) {
               backupLevel(this.minecraft.getLevelStorage(), this.levelName);
            }

            this.minecraft.openScreen(new OptimizeWorldScreen(this.callback, this.levelName, this.minecraft.getLevelStorage(), bl2));
         }, new TranslatableText("optimizeWorld.confirm.title", new Object[0]), new TranslatableText("optimizeWorld.confirm.description", new Object[0]), true));
      }));
      this.saveButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 144 + 5, 98, 20, I18n.translate("selectWorld.edit.save"), (buttonWidgetx) -> {
         this.commit();
      }));
      this.addButton(new ButtonWidget(this.width / 2 + 2, this.height / 4 + 144 + 5, 98, 20, I18n.translate("gui.cancel"), (buttonWidgetx) -> {
         this.callback.accept(false);
      }));
      buttonWidget.active = this.minecraft.getLevelStorage().resolveFile(this.levelName, "icon.png").isFile();
      LevelStorage levelStorage = this.minecraft.getLevelStorage();
      LevelProperties levelProperties = levelStorage.getLevelProperties(this.levelName);
      String string = levelProperties == null ? "" : levelProperties.getLevelName();
      this.levelNameTextField = new TextFieldWidget(this.font, this.width / 2 - 100, 53, 200, 20, I18n.translate("selectWorld.enterName"));
      this.levelNameTextField.setText(string);
      this.levelNameTextField.setChangedListener((stringx) -> {
         this.saveButton.active = !stringx.trim().isEmpty();
      });
      this.children.add(this.levelNameTextField);
      this.setInitialFocus(this.levelNameTextField);
   }

   public void resize(MinecraftClient client, int width, int height) {
      String string = this.levelNameTextField.getText();
      this.init(client, width, height);
      this.levelNameTextField.setText(string);
   }

   public void removed() {
      this.minecraft.keyboard.enableRepeatEvents(false);
   }

   private void commit() {
      LevelStorage levelStorage = this.minecraft.getLevelStorage();
      levelStorage.renameLevel(this.levelName, this.levelNameTextField.getText().trim());
      this.callback.accept(true);
   }

   public static void backupLevel(LevelStorage level, String name) {
      ToastManager toastManager = MinecraftClient.getInstance().getToastManager();
      long l = 0L;
      IOException iOException = null;

      try {
         l = level.backupLevel(name);
      } catch (IOException var8) {
         iOException = var8;
      }

      TranslatableText text3;
      Object text4;
      if (iOException != null) {
         text3 = new TranslatableText("selectWorld.edit.backupFailed", new Object[0]);
         text4 = new LiteralText(iOException.getMessage());
      } else {
         text3 = new TranslatableText("selectWorld.edit.backupCreated", new Object[]{name});
         text4 = new TranslatableText("selectWorld.edit.backupSize", new Object[]{MathHelper.ceil((double)l / 1048576.0D)});
      }

      toastManager.add(new SystemToast(SystemToast.Type.WORLD_BACKUP, text3, (Text)text4));
   }

   public void render(int mouseX, int mouseY, float delta) {
      this.renderBackground();
      this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, 20, 16777215);
      this.drawString(this.font, I18n.translate("selectWorld.enterName"), this.width / 2 - 100, 40, 10526880);
      this.levelNameTextField.render(mouseX, mouseY, delta);
      super.render(mouseX, mouseY, delta);
   }
}
