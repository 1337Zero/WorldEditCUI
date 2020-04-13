package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;

@Environment(EnvType.CLIENT)
public abstract class RealmListEntry extends AlwaysSelectedEntryListWidget.Entry<RealmListEntry> {
   public abstract void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f);

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return false;
   }
}
