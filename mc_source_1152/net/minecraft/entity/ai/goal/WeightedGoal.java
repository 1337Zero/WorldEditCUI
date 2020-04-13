package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;

public class WeightedGoal extends Goal {
   private final Goal goal;
   private final int weight;
   private boolean running;

   public WeightedGoal(int weight, Goal goal) {
      this.weight = weight;
      this.goal = goal;
   }

   public boolean canBeReplacedBy(WeightedGoal goal) {
      return this.canStop() && goal.getWeight() < this.getWeight();
   }

   public boolean canStart() {
      return this.goal.canStart();
   }

   public boolean shouldContinue() {
      return this.goal.shouldContinue();
   }

   public boolean canStop() {
      return this.goal.canStop();
   }

   public void start() {
      if (!this.running) {
         this.running = true;
         this.goal.start();
      }
   }

   public void stop() {
      if (this.running) {
         this.running = false;
         this.goal.stop();
      }
   }

   public void tick() {
      this.goal.tick();
   }

   public void setControls(EnumSet<Goal.Control> controls) {
      this.goal.setControls(controls);
   }

   public EnumSet<Goal.Control> getControls() {
      return this.goal.getControls();
   }

   public boolean isRunning() {
      return this.running;
   }

   public int getWeight() {
      return this.weight;
   }

   public Goal getGoal() {
      return this.goal;
   }

   public boolean equals(@Nullable Object object) {
      if (this == object) {
         return true;
      } else {
         return object != null && this.getClass() == object.getClass() ? this.goal.equals(((WeightedGoal)object).goal) : false;
      }
   }

   public int hashCode() {
      return this.goal.hashCode();
   }
}
