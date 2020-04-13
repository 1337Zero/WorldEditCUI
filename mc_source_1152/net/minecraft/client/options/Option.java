package net.minecraft.client.options;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.Window;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public abstract class Option {
   public static final DoubleOption BIOME_BLEND_RADIUS = new DoubleOption("options.biomeBlendRadius", 0.0D, 7.0D, 1.0F, (gameOptions) -> {
      return (double)gameOptions.biomeBlendRadius;
   }, (gameOptions, var1) -> {
      gameOptions.biomeBlendRadius = MathHelper.clamp((int)var1, 0, 7);
      MinecraftClient.getInstance().worldRenderer.reload();
   }, (gameOptions, doubleOption) -> {
      double d = doubleOption.get(gameOptions);
      String string = doubleOption.getDisplayPrefix();
      int i = (int)d * 2 + 1;
      return string + I18n.translate("options.biomeBlendRadius." + i);
   });
   public static final DoubleOption CHAT_HEIGHT_FOCUSED = new DoubleOption("options.chat.height.focused", 0.0D, 1.0D, 0.0F, (gameOptions) -> {
      return gameOptions.chatHeightFocused;
   }, (gameOptions, var1) -> {
      gameOptions.chatHeightFocused = var1;
      MinecraftClient.getInstance().inGameHud.getChatHud().reset();
   }, (gameOptions, doubleOption) -> {
      double d = doubleOption.getRatio(doubleOption.get(gameOptions));
      return doubleOption.getDisplayPrefix() + ChatHud.getHeight(d) + "px";
   });
   public static final DoubleOption SATURATION = new DoubleOption("options.chat.height.unfocused", 0.0D, 1.0D, 0.0F, (gameOptions) -> {
      return gameOptions.chatHeightUnfocused;
   }, (gameOptions, var1) -> {
      gameOptions.chatHeightUnfocused = var1;
      MinecraftClient.getInstance().inGameHud.getChatHud().reset();
   }, (gameOptions, doubleOption) -> {
      double d = doubleOption.getRatio(doubleOption.get(gameOptions));
      return doubleOption.getDisplayPrefix() + ChatHud.getHeight(d) + "px";
   });
   public static final DoubleOption CHAT_OPACITY = new DoubleOption("options.chat.opacity", 0.0D, 1.0D, 0.0F, (gameOptions) -> {
      return gameOptions.chatOpacity;
   }, (gameOptions, var1) -> {
      gameOptions.chatOpacity = var1;
      MinecraftClient.getInstance().inGameHud.getChatHud().reset();
   }, (gameOptions, doubleOption) -> {
      double d = doubleOption.getRatio(doubleOption.get(gameOptions));
      return doubleOption.getDisplayPrefix() + (int)(d * 90.0D + 10.0D) + "%";
   });
   public static final DoubleOption CHAT_SCALE = new DoubleOption("options.chat.scale", 0.0D, 1.0D, 0.0F, (gameOptions) -> {
      return gameOptions.chatScale;
   }, (gameOptions, var1) -> {
      gameOptions.chatScale = var1;
      MinecraftClient.getInstance().inGameHud.getChatHud().reset();
   }, (gameOptions, doubleOption) -> {
      double d = doubleOption.getRatio(doubleOption.get(gameOptions));
      String string = doubleOption.getDisplayPrefix();
      return d == 0.0D ? string + I18n.translate("options.off") : string + (int)(d * 100.0D) + "%";
   });
   public static final DoubleOption CHAT_WIDTH = new DoubleOption("options.chat.width", 0.0D, 1.0D, 0.0F, (gameOptions) -> {
      return gameOptions.chatWidth;
   }, (gameOptions, var1) -> {
      gameOptions.chatWidth = var1;
      MinecraftClient.getInstance().inGameHud.getChatHud().reset();
   }, (gameOptions, doubleOption) -> {
      double d = doubleOption.getRatio(doubleOption.get(gameOptions));
      return doubleOption.getDisplayPrefix() + ChatHud.getWidth(d) + "px";
   });
   public static final DoubleOption FOV = new DoubleOption("options.fov", 30.0D, 110.0D, 1.0F, (gameOptions) -> {
      return gameOptions.fov;
   }, (gameOptions, var1) -> {
      gameOptions.fov = var1;
   }, (gameOptions, doubleOption) -> {
      double d = doubleOption.get(gameOptions);
      String string = doubleOption.getDisplayPrefix();
      if (d == 70.0D) {
         return string + I18n.translate("options.fov.min");
      } else {
         return d == doubleOption.getMax() ? string + I18n.translate("options.fov.max") : string + (int)d;
      }
   });
   public static final DoubleOption FRAMERATE_LIMIT = new DoubleOption("options.framerateLimit", 10.0D, 260.0D, 10.0F, (gameOptions) -> {
      return (double)gameOptions.maxFps;
   }, (gameOptions, var1) -> {
      gameOptions.maxFps = (int)var1;
      MinecraftClient.getInstance().getWindow().setFramerateLimit(gameOptions.maxFps);
   }, (gameOptions, doubleOption) -> {
      double d = doubleOption.get(gameOptions);
      String string = doubleOption.getDisplayPrefix();
      return d == doubleOption.getMax() ? string + I18n.translate("options.framerateLimit.max") : string + I18n.translate("options.framerate", (int)d);
   });
   public static final DoubleOption GAMMA = new DoubleOption("options.gamma", 0.0D, 1.0D, 0.0F, (gameOptions) -> {
      return gameOptions.gamma;
   }, (gameOptions, var1) -> {
      gameOptions.gamma = var1;
   }, (gameOptions, doubleOption) -> {
      double d = doubleOption.getRatio(doubleOption.get(gameOptions));
      String string = doubleOption.getDisplayPrefix();
      if (d == 0.0D) {
         return string + I18n.translate("options.gamma.min");
      } else {
         return d == 1.0D ? string + I18n.translate("options.gamma.max") : string + "+" + (int)(d * 100.0D) + "%";
      }
   });
   public static final DoubleOption MIPMAP_LEVELS = new DoubleOption("options.mipmapLevels", 0.0D, 4.0D, 1.0F, (gameOptions) -> {
      return (double)gameOptions.mipmapLevels;
   }, (gameOptions, var1) -> {
      gameOptions.mipmapLevels = (int)var1;
   }, (gameOptions, doubleOption) -> {
      double d = doubleOption.get(gameOptions);
      String string = doubleOption.getDisplayPrefix();
      return d == 0.0D ? string + I18n.translate("options.off") : string + (int)d;
   });
   public static final DoubleOption MOUSE_WHEEL_SENSITIVITY = new LogarithmicOption("options.mouseWheelSensitivity", 0.01D, 10.0D, 0.01F, (gameOptions) -> {
      return gameOptions.mouseWheelSensitivity;
   }, (gameOptions, var1) -> {
      gameOptions.mouseWheelSensitivity = var1;
   }, (gameOptions, doubleOption) -> {
      double d = doubleOption.getRatio(doubleOption.get(gameOptions));
      return doubleOption.getDisplayPrefix() + String.format("%.2f", doubleOption.getValue(d));
   });
   public static final BooleanOption RAW_MOUSE_INPUT = new BooleanOption("options.rawMouseInput", (gameOptions) -> {
      return gameOptions.rawMouseInput;
   }, (gameOptions, var1) -> {
      gameOptions.rawMouseInput = var1;
      Window window = MinecraftClient.getInstance().getWindow();
      if (window != null) {
         window.setRawMouseMotion(var1);
      }

   });
   public static final DoubleOption RENDER_DISTANCE = new DoubleOption("options.renderDistance", 2.0D, 16.0D, 1.0F, (gameOptions) -> {
      return (double)gameOptions.viewDistance;
   }, (gameOptions, var1) -> {
      gameOptions.viewDistance = (int)var1;
      MinecraftClient.getInstance().worldRenderer.scheduleTerrainUpdate();
   }, (gameOptions, doubleOption) -> {
      double d = doubleOption.get(gameOptions);
      return doubleOption.getDisplayPrefix() + I18n.translate("options.chunks", (int)d);
   });
   public static final DoubleOption SENSITIVITY = new DoubleOption("options.sensitivity", 0.0D, 1.0D, 0.0F, (gameOptions) -> {
      return gameOptions.mouseSensitivity;
   }, (gameOptions, var1) -> {
      gameOptions.mouseSensitivity = var1;
   }, (gameOptions, doubleOption) -> {
      double d = doubleOption.getRatio(doubleOption.get(gameOptions));
      String string = doubleOption.getDisplayPrefix();
      if (d == 0.0D) {
         return string + I18n.translate("options.sensitivity.min");
      } else {
         return d == 1.0D ? string + I18n.translate("options.sensitivity.max") : string + (int)(d * 200.0D) + "%";
      }
   });
   public static final DoubleOption TEXT_BACKGROUND_OPACITY = new DoubleOption("options.accessibility.text_background_opacity", 0.0D, 1.0D, 0.0F, (gameOptions) -> {
      return gameOptions.textBackgroundOpacity;
   }, (gameOptions, var1) -> {
      gameOptions.textBackgroundOpacity = var1;
      MinecraftClient.getInstance().inGameHud.getChatHud().reset();
   }, (gameOptions, doubleOption) -> {
      return doubleOption.getDisplayPrefix() + (int)(doubleOption.getRatio(doubleOption.get(gameOptions)) * 100.0D) + "%";
   });
   public static final CyclingOption AO = new CyclingOption("options.ao", (gameOptions, integer) -> {
      gameOptions.ao = AoOption.getOption(gameOptions.ao.getValue() + integer);
      MinecraftClient.getInstance().worldRenderer.reload();
   }, (gameOptions, cyclingOption) -> {
      return cyclingOption.getDisplayPrefix() + I18n.translate(gameOptions.ao.getTranslationKey());
   });
   public static final CyclingOption ATTACK_INDICATOR = new CyclingOption("options.attackIndicator", (gameOptions, integer) -> {
      gameOptions.attackIndicator = AttackIndicator.byId(gameOptions.attackIndicator.getId() + integer);
   }, (gameOptions, cyclingOption) -> {
      return cyclingOption.getDisplayPrefix() + I18n.translate(gameOptions.attackIndicator.getTranslationKey());
   });
   public static final CyclingOption VISIBILITY = new CyclingOption("options.chat.visibility", (gameOptions, integer) -> {
      gameOptions.chatVisibility = ChatVisibility.byId((gameOptions.chatVisibility.getId() + integer) % 3);
   }, (gameOptions, cyclingOption) -> {
      return cyclingOption.getDisplayPrefix() + I18n.translate(gameOptions.chatVisibility.getTranslationKey());
   });
   public static final CyclingOption GRAPHICS = new CyclingOption("options.graphics", (gameOptions, integer) -> {
      gameOptions.fancyGraphics = !gameOptions.fancyGraphics;
      MinecraftClient.getInstance().worldRenderer.reload();
   }, (gameOptions, cyclingOption) -> {
      return gameOptions.fancyGraphics ? cyclingOption.getDisplayPrefix() + I18n.translate("options.graphics.fancy") : cyclingOption.getDisplayPrefix() + I18n.translate("options.graphics.fast");
   });
   public static final CyclingOption GUI_SCALE = new CyclingOption("options.guiScale", (gameOptions, integer) -> {
      gameOptions.guiScale = Integer.remainderUnsigned(gameOptions.guiScale + integer, MinecraftClient.getInstance().getWindow().calculateScaleFactor(0, MinecraftClient.getInstance().forcesUnicodeFont()) + 1);
   }, (gameOptions, cyclingOption) -> {
      return cyclingOption.getDisplayPrefix() + (gameOptions.guiScale == 0 ? I18n.translate("options.guiScale.auto") : gameOptions.guiScale);
   });
   public static final CyclingOption MAIN_HAND = new CyclingOption("options.mainHand", (gameOptions, integer) -> {
      gameOptions.mainArm = gameOptions.mainArm.getOpposite();
   }, (gameOptions, cyclingOption) -> {
      return cyclingOption.getDisplayPrefix() + gameOptions.mainArm;
   });
   public static final CyclingOption NARRATOR = new CyclingOption("options.narrator", (gameOptions, integer) -> {
      if (NarratorManager.INSTANCE.isActive()) {
         gameOptions.narrator = NarratorOption.byId(gameOptions.narrator.getId() + integer);
      } else {
         gameOptions.narrator = NarratorOption.OFF;
      }

      NarratorManager.INSTANCE.addToast(gameOptions.narrator);
   }, (gameOptions, cyclingOption) -> {
      return NarratorManager.INSTANCE.isActive() ? cyclingOption.getDisplayPrefix() + I18n.translate(gameOptions.narrator.getTranslationKey()) : cyclingOption.getDisplayPrefix() + I18n.translate("options.narrator.notavailable");
   });
   public static final CyclingOption PARTICLES = new CyclingOption("options.particles", (gameOptions, integer) -> {
      gameOptions.particles = ParticlesOption.byId(gameOptions.particles.getId() + integer);
   }, (gameOptions, cyclingOption) -> {
      return cyclingOption.getDisplayPrefix() + I18n.translate(gameOptions.particles.getTranslationKey());
   });
   public static final CyclingOption CLOUDS = new CyclingOption("options.renderClouds", (gameOptions, integer) -> {
      gameOptions.cloudRenderMode = CloudRenderMode.getOption(gameOptions.cloudRenderMode.getValue() + integer);
   }, (gameOptions, cyclingOption) -> {
      return cyclingOption.getDisplayPrefix() + I18n.translate(gameOptions.cloudRenderMode.getTranslationKey());
   });
   public static final CyclingOption TEXT_BACKGROUND = new CyclingOption("options.accessibility.text_background", (gameOptions, integer) -> {
      gameOptions.backgroundForChatOnly = !gameOptions.backgroundForChatOnly;
   }, (gameOptions, cyclingOption) -> {
      return cyclingOption.getDisplayPrefix() + I18n.translate(gameOptions.backgroundForChatOnly ? "options.accessibility.text_background.chat" : "options.accessibility.text_background.everywhere");
   });
   public static final BooleanOption AUTO_JUMP = new BooleanOption("options.autoJump", (gameOptions) -> {
      return gameOptions.autoJump;
   }, (gameOptions, var1) -> {
      gameOptions.autoJump = var1;
   });
   public static final BooleanOption AUTO_SUGGESTIONS = new BooleanOption("options.autoSuggestCommands", (gameOptions) -> {
      return gameOptions.autoSuggestions;
   }, (gameOptions, var1) -> {
      gameOptions.autoSuggestions = var1;
   });
   public static final BooleanOption CHAT_COLOR = new BooleanOption("options.chat.color", (gameOptions) -> {
      return gameOptions.chatColors;
   }, (gameOptions, var1) -> {
      gameOptions.chatColors = var1;
   });
   public static final BooleanOption CHAT_LINKS = new BooleanOption("options.chat.links", (gameOptions) -> {
      return gameOptions.chatLinks;
   }, (gameOptions, var1) -> {
      gameOptions.chatLinks = var1;
   });
   public static final BooleanOption CHAT_LINKS_PROMPT = new BooleanOption("options.chat.links.prompt", (gameOptions) -> {
      return gameOptions.chatLinksPrompt;
   }, (gameOptions, var1) -> {
      gameOptions.chatLinksPrompt = var1;
   });
   public static final BooleanOption DISCRETE_MOUSE_SCROLL = new BooleanOption("options.discrete_mouse_scroll", (gameOptions) -> {
      return gameOptions.discreteMouseScroll;
   }, (gameOptions, var1) -> {
      gameOptions.discreteMouseScroll = var1;
   });
   public static final BooleanOption VSYNC = new BooleanOption("options.vsync", (gameOptions) -> {
      return gameOptions.enableVsync;
   }, (gameOptions, var1) -> {
      gameOptions.enableVsync = var1;
      if (MinecraftClient.getInstance().getWindow() != null) {
         MinecraftClient.getInstance().getWindow().setVsync(gameOptions.enableVsync);
      }

   });
   public static final BooleanOption ENTITY_SHADOWS = new BooleanOption("options.entityShadows", (gameOptions) -> {
      return gameOptions.entityShadows;
   }, (gameOptions, var1) -> {
      gameOptions.entityShadows = var1;
   });
   public static final BooleanOption FORCE_UNICODE_FONT = new BooleanOption("options.forceUnicodeFont", (gameOptions) -> {
      return gameOptions.forceUnicodeFont;
   }, (gameOptions, var1) -> {
      gameOptions.forceUnicodeFont = var1;
      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      if (minecraftClient.getFontManager() != null) {
         minecraftClient.getFontManager().setForceUnicodeFont(gameOptions.forceUnicodeFont, Util.getServerWorkerExecutor(), minecraftClient);
      }

   });
   public static final BooleanOption INVERT_MOUSE = new BooleanOption("options.invertMouse", (gameOptions) -> {
      return gameOptions.invertYMouse;
   }, (gameOptions, var1) -> {
      gameOptions.invertYMouse = var1;
   });
   public static final BooleanOption REALMS_NOTIFICATIONS = new BooleanOption("options.realmsNotifications", (gameOptions) -> {
      return gameOptions.realmsNotifications;
   }, (gameOptions, var1) -> {
      gameOptions.realmsNotifications = var1;
   });
   public static final BooleanOption REDUCED_DEBUG_INFO = new BooleanOption("options.reducedDebugInfo", (gameOptions) -> {
      return gameOptions.reducedDebugInfo;
   }, (gameOptions, var1) -> {
      gameOptions.reducedDebugInfo = var1;
   });
   public static final BooleanOption SUBTITLES = new BooleanOption("options.showSubtitles", (gameOptions) -> {
      return gameOptions.showSubtitles;
   }, (gameOptions, var1) -> {
      gameOptions.showSubtitles = var1;
   });
   public static final BooleanOption SNOOPER = new BooleanOption("options.snooper", (gameOptions) -> {
      if (gameOptions.snooperEnabled) {
      }

      return false;
   }, (gameOptions, var1) -> {
      gameOptions.snooperEnabled = var1;
   });
   public static final CyclingOption SNEAK_TOGGLED = new CyclingOption("key.sneak", (gameOptions, integer) -> {
      gameOptions.sneakToggled = !gameOptions.sneakToggled;
   }, (gameOptions, cyclingOption) -> {
      return cyclingOption.getDisplayPrefix() + I18n.translate(gameOptions.sneakToggled ? "options.key.toggle" : "options.key.hold");
   });
   public static final CyclingOption SPRINT_TOGGLED = new CyclingOption("key.sprint", (gameOptions, integer) -> {
      gameOptions.sprintToggled = !gameOptions.sprintToggled;
   }, (gameOptions, cyclingOption) -> {
      return cyclingOption.getDisplayPrefix() + I18n.translate(gameOptions.sprintToggled ? "options.key.toggle" : "options.key.hold");
   });
   public static final BooleanOption TOUCHSCREEN = new BooleanOption("options.touchscreen", (gameOptions) -> {
      return gameOptions.touchscreen;
   }, (gameOptions, var1) -> {
      gameOptions.touchscreen = var1;
   });
   public static final BooleanOption FULLSCREEN = new BooleanOption("options.fullscreen", (gameOptions) -> {
      return gameOptions.fullscreen;
   }, (gameOptions, var1) -> {
      gameOptions.fullscreen = var1;
      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      if (minecraftClient.getWindow() != null && minecraftClient.getWindow().isFullscreen() != gameOptions.fullscreen) {
         minecraftClient.getWindow().toggleFullscreen();
         gameOptions.fullscreen = minecraftClient.getWindow().isFullscreen();
      }

   });
   public static final BooleanOption VIEW_BOBBING = new BooleanOption("options.viewBobbing", (gameOptions) -> {
      return gameOptions.bobView;
   }, (gameOptions, var1) -> {
      gameOptions.bobView = var1;
   });
   private final String key;

   public Option(String key) {
      this.key = key;
   }

   public abstract AbstractButtonWidget createButton(GameOptions options, int x, int y, int width);

   public String getDisplayPrefix() {
      return I18n.translate(this.key) + ": ";
   }
}
