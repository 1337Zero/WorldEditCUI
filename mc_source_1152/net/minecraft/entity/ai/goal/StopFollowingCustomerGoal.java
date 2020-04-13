package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.passive.AbstractTraderEntity;
import net.minecraft.entity.player.PlayerEntity;

public class StopFollowingCustomerGoal extends Goal {
   private final AbstractTraderEntity trader;

   public StopFollowingCustomerGoal(AbstractTraderEntity trader) {
      this.trader = trader;
      this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
   }

   public boolean canStart() {
      if (!this.trader.isAlive()) {
         return false;
      } else if (this.trader.isTouchingWater()) {
         return false;
      } else if (!this.trader.onGround) {
         return false;
      } else if (this.trader.velocityModified) {
         return false;
      } else {
         PlayerEntity playerEntity = this.trader.getCurrentCustomer();
         if (playerEntity == null) {
            return false;
         } else if (this.trader.squaredDistanceTo(playerEntity) > 16.0D) {
            return false;
         } else {
            return playerEntity.container != null;
         }
      }
   }

   public void start() {
      this.trader.getNavigation().stop();
   }

   public void stop() {
      this.trader.setCurrentCustomer((PlayerEntity)null);
   }
}
