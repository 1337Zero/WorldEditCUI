package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.AbstractButtonWidget;

@Environment(EnvType.CLIENT)
public abstract class AbstractRealmsButton<P extends AbstractButtonWidget & RealmsAbstractButtonProxy<?>> {
   public abstract P getProxy();

   public boolean active() {
      return ((RealmsAbstractButtonProxy)this.getProxy()).active();
   }

   public void active(boolean bl) {
      ((RealmsAbstractButtonProxy)this.getProxy()).active(bl);
   }

   public boolean isVisible() {
      return ((RealmsAbstractButtonProxy)this.getProxy()).isVisible();
   }

   public void setVisible(boolean bl) {
      ((RealmsAbstractButtonProxy)this.getProxy()).setVisible(bl);
   }

   public void render(int i, int j, float f) {
      this.getProxy().render(i, j, f);
   }

   public void blit(int i, int j, int k, int l, int m, int n) {
      this.getProxy().blit(i, j, k, l, m, n);
   }

   public void tick() {
   }
}
