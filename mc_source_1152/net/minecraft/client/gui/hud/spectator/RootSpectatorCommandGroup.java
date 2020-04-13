package net.minecraft.client.gui.hud.spectator;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class RootSpectatorCommandGroup implements SpectatorMenuCommandGroup {
   private final List<SpectatorMenuCommand> elements = Lists.newArrayList();

   public RootSpectatorCommandGroup() {
      this.elements.add(new TeleportSpectatorMenu());
      this.elements.add(new TeamTeleportSpectatorMenu());
   }

   public List<SpectatorMenuCommand> getCommands() {
      return this.elements;
   }

   public Text getPrompt() {
      return new TranslatableText("spectatorMenu.root.prompt", new Object[0]);
   }
}
