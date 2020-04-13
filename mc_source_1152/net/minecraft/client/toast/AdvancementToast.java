package net.minecraft.client.toast;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class AdvancementToast implements Toast {
   private final Advancement advancement;
   private boolean soundPlayed;

   public AdvancementToast(Advancement advancement) {
      this.advancement = advancement;
   }

   public Toast.Visibility draw(ToastManager manager, long currentTime) {
      manager.getGame().getTextureManager().bindTexture(TOASTS_TEX);
      RenderSystem.color3f(1.0F, 1.0F, 1.0F);
      AdvancementDisplay advancementDisplay = this.advancement.getDisplay();
      manager.blit(0, 0, 0, 0, 160, 32);
      if (advancementDisplay != null) {
         List<String> list = manager.getGame().textRenderer.wrapStringToWidthAsList(advancementDisplay.getTitle().asFormattedString(), 125);
         int i = advancementDisplay.getFrame() == AdvancementFrame.CHALLENGE ? 16746751 : 16776960;
         if (list.size() == 1) {
            manager.getGame().textRenderer.draw(I18n.translate("advancements.toast." + advancementDisplay.getFrame().getId()), 30.0F, 7.0F, i | -16777216);
            manager.getGame().textRenderer.draw(advancementDisplay.getTitle().asFormattedString(), 30.0F, 18.0F, -1);
         } else {
            int j = true;
            float f = 300.0F;
            int l;
            if (currentTime < 1500L) {
               l = MathHelper.floor(MathHelper.clamp((float)(1500L - currentTime) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
               manager.getGame().textRenderer.draw(I18n.translate("advancements.toast." + advancementDisplay.getFrame().getId()), 30.0F, 11.0F, i | l);
            } else {
               l = MathHelper.floor(MathHelper.clamp((float)(currentTime - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
               int var10001 = list.size();
               manager.getGame().textRenderer.getClass();
               int m = 16 - var10001 * 9 / 2;

               for(Iterator var11 = list.iterator(); var11.hasNext(); m += 9) {
                  String string = (String)var11.next();
                  manager.getGame().textRenderer.draw(string, 30.0F, (float)m, 16777215 | l);
                  manager.getGame().textRenderer.getClass();
               }
            }
         }

         if (!this.soundPlayed && currentTime > 0L) {
            this.soundPlayed = true;
            if (advancementDisplay.getFrame() == AdvancementFrame.CHALLENGE) {
               manager.getGame().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F));
            }
         }

         manager.getGame().getItemRenderer().renderGuiItem((LivingEntity)null, advancementDisplay.getIcon(), 8, 8);
         return currentTime >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
      } else {
         return Toast.Visibility.HIDE;
      }
   }
}
