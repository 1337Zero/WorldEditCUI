package net.minecraft.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.GlfwUtil;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.SmoothUtil;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class Mouse {
   private final MinecraftClient client;
   private boolean leftButtonClicked;
   private boolean middleButtonClicked;
   private boolean rightButtonClicked;
   private double x;
   private double y;
   private int controlLeftTicks;
   private int activeButton = -1;
   private boolean hasResolutionChanged = true;
   private int field_1796;
   private double glfwTime;
   private final SmoothUtil cursorXSmoother = new SmoothUtil();
   private final SmoothUtil cursorYSmoother = new SmoothUtil();
   private double cursorDeltaX;
   private double cursorDeltaY;
   private double eventDeltaWheel;
   private double lastMouseUpdateTime = Double.MIN_VALUE;
   private boolean isCursorLocked;

   public Mouse(MinecraftClient client) {
      this.client = client;
   }

   private void onMouseButton(long window, int button, int action, int mods) {
      if (window == this.client.getWindow().getHandle()) {
         boolean bl = action == 1;
         if (MinecraftClient.IS_SYSTEM_MAC && button == 0) {
            if (bl) {
               if ((mods & 2) == 2) {
                  button = 1;
                  ++this.controlLeftTicks;
               }
            } else if (this.controlLeftTicks > 0) {
               button = 1;
               --this.controlLeftTicks;
            }
         }

         if (bl) {
            if (this.client.options.touchscreen && this.field_1796++ > 0) {
               return;
            }

            this.activeButton = button;
            this.glfwTime = GlfwUtil.getTime();
         } else if (this.activeButton != -1) {
            if (this.client.options.touchscreen && --this.field_1796 > 0) {
               return;
            }

            this.activeButton = -1;
         }

         boolean[] bls = new boolean[]{false};
         if (this.client.overlay == null) {
            if (this.client.currentScreen == null) {
               if (!this.isCursorLocked && bl) {
                  this.lockCursor();
               }
            } else {
               double d = this.x * (double)this.client.getWindow().getScaledWidth() / (double)this.client.getWindow().getWidth();
               double e = this.y * (double)this.client.getWindow().getScaledHeight() / (double)this.client.getWindow().getHeight();
               if (bl) {
                  Screen.wrapScreenError(() -> {
                     bls[0] = this.client.currentScreen.mouseClicked(d, e, button);
                  }, "mouseClicked event handler", this.client.currentScreen.getClass().getCanonicalName());
               } else {
                  Screen.wrapScreenError(() -> {
                     bls[0] = this.client.currentScreen.mouseReleased(d, e, button);
                  }, "mouseReleased event handler", this.client.currentScreen.getClass().getCanonicalName());
               }
            }
         }

         if (!bls[0] && (this.client.currentScreen == null || this.client.currentScreen.passEvents) && this.client.overlay == null) {
            if (button == 0) {
               this.leftButtonClicked = bl;
            } else if (button == 2) {
               this.middleButtonClicked = bl;
            } else if (button == 1) {
               this.rightButtonClicked = bl;
            }

            KeyBinding.setKeyPressed(InputUtil.Type.MOUSE.createFromCode(button), bl);
            if (bl) {
               if (this.client.player.isSpectator() && button == 2) {
                  this.client.inGameHud.getSpectatorHud().useSelectedCommand();
               } else {
                  KeyBinding.onKeyPressed(InputUtil.Type.MOUSE.createFromCode(button));
               }
            }
         }

      }
   }

   private void onMouseScroll(long window, double d, double e) {
      if (window == MinecraftClient.getInstance().getWindow().getHandle()) {
         double f = (this.client.options.discreteMouseScroll ? Math.signum(e) : e) * this.client.options.mouseWheelSensitivity;
         if (this.client.overlay == null) {
            if (this.client.currentScreen != null) {
               double g = this.x * (double)this.client.getWindow().getScaledWidth() / (double)this.client.getWindow().getWidth();
               double h = this.y * (double)this.client.getWindow().getScaledHeight() / (double)this.client.getWindow().getHeight();
               this.client.currentScreen.mouseScrolled(g, h, f);
            } else if (this.client.player != null) {
               if (this.eventDeltaWheel != 0.0D && Math.signum(f) != Math.signum(this.eventDeltaWheel)) {
                  this.eventDeltaWheel = 0.0D;
               }

               this.eventDeltaWheel += f;
               float i = (float)((int)this.eventDeltaWheel);
               if (i == 0.0F) {
                  return;
               }

               this.eventDeltaWheel -= (double)i;
               if (this.client.player.isSpectator()) {
                  if (this.client.inGameHud.getSpectatorHud().isOpen()) {
                     this.client.inGameHud.getSpectatorHud().cycleSlot((double)(-i));
                  } else {
                     float j = MathHelper.clamp(this.client.player.abilities.getFlySpeed() + i * 0.005F, 0.0F, 0.2F);
                     this.client.player.abilities.setFlySpeed(j);
                  }
               } else {
                  this.client.player.inventory.scrollInHotbar((double)i);
               }
            }
         }
      }

   }

   public void setup(long l) {
      InputUtil.setMouseCallbacks(l, (lx, d, e) -> {
         this.client.execute(() -> {
            this.onCursorPos(lx, d, e);
         });
      }, (lx, i, j, k) -> {
         this.client.execute(() -> {
            this.onMouseButton(lx, i, j, k);
         });
      }, (lx, d, e) -> {
         this.client.execute(() -> {
            this.onMouseScroll(lx, d, e);
         });
      });
   }

   private void onCursorPos(long window, double x, double y) {
      if (window == MinecraftClient.getInstance().getWindow().getHandle()) {
         if (this.hasResolutionChanged) {
            this.x = x;
            this.y = y;
            this.hasResolutionChanged = false;
         }

         Element element = this.client.currentScreen;
         if (element != null && this.client.overlay == null) {
            double d = x * (double)this.client.getWindow().getScaledWidth() / (double)this.client.getWindow().getWidth();
            double e = y * (double)this.client.getWindow().getScaledHeight() / (double)this.client.getWindow().getHeight();
            Screen.wrapScreenError(() -> {
               element.mouseMoved(d, e);
            }, "mouseMoved event handler", element.getClass().getCanonicalName());
            if (this.activeButton != -1 && this.glfwTime > 0.0D) {
               double f = (x - this.x) * (double)this.client.getWindow().getScaledWidth() / (double)this.client.getWindow().getWidth();
               double g = (y - this.y) * (double)this.client.getWindow().getScaledHeight() / (double)this.client.getWindow().getHeight();
               Screen.wrapScreenError(() -> {
                  element.mouseDragged(d, e, this.activeButton, f, g);
               }, "mouseDragged event handler", element.getClass().getCanonicalName());
            }
         }

         this.client.getProfiler().push("mouse");
         if (this.isCursorLocked() && this.client.isWindowFocused()) {
            this.cursorDeltaX += x - this.x;
            this.cursorDeltaY += y - this.y;
         }

         this.updateMouse();
         this.x = x;
         this.y = y;
         this.client.getProfiler().pop();
      }
   }

   public void updateMouse() {
      double d = GlfwUtil.getTime();
      double e = d - this.lastMouseUpdateTime;
      this.lastMouseUpdateTime = d;
      if (this.isCursorLocked() && this.client.isWindowFocused()) {
         double f = this.client.options.mouseSensitivity * 0.6000000238418579D + 0.20000000298023224D;
         double g = f * f * f * 8.0D;
         double l;
         double m;
         if (this.client.options.smoothCameraEnabled) {
            double h = this.cursorXSmoother.smooth(this.cursorDeltaX * g, e * g);
            double i = this.cursorYSmoother.smooth(this.cursorDeltaY * g, e * g);
            l = h;
            m = i;
         } else {
            this.cursorXSmoother.clear();
            this.cursorYSmoother.clear();
            l = this.cursorDeltaX * g;
            m = this.cursorDeltaY * g;
         }

         this.cursorDeltaX = 0.0D;
         this.cursorDeltaY = 0.0D;
         int n = 1;
         if (this.client.options.invertYMouse) {
            n = -1;
         }

         this.client.getTutorialManager().onUpdateMouse(l, m);
         if (this.client.player != null) {
            this.client.player.changeLookDirection(l, m * (double)n);
         }

      } else {
         this.cursorDeltaX = 0.0D;
         this.cursorDeltaY = 0.0D;
      }
   }

   public boolean wasLeftButtonClicked() {
      return this.leftButtonClicked;
   }

   public boolean wasRightButtonClicked() {
      return this.rightButtonClicked;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public void onResolutionChanged() {
      this.hasResolutionChanged = true;
   }

   public boolean isCursorLocked() {
      return this.isCursorLocked;
   }

   public void lockCursor() {
      if (this.client.isWindowFocused()) {
         if (!this.isCursorLocked) {
            if (!MinecraftClient.IS_SYSTEM_MAC) {
               KeyBinding.updatePressedStates();
            }

            this.isCursorLocked = true;
            this.x = (double)(this.client.getWindow().getWidth() / 2);
            this.y = (double)(this.client.getWindow().getHeight() / 2);
            InputUtil.setCursorParameters(this.client.getWindow().getHandle(), 212995, this.x, this.y);
            this.client.openScreen((Screen)null);
            this.client.attackCooldown = 10000;
            this.hasResolutionChanged = true;
         }
      }
   }

   public void unlockCursor() {
      if (this.isCursorLocked) {
         this.isCursorLocked = false;
         this.x = (double)(this.client.getWindow().getWidth() / 2);
         this.y = (double)(this.client.getWindow().getHeight() / 2);
         InputUtil.setCursorParameters(this.client.getWindow().getHandle(), 212993, this.x, this.y);
      }
   }
}
