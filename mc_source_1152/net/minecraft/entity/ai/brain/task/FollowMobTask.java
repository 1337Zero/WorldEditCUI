package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityPosWrapper;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.server.world.ServerWorld;

public class FollowMobTask extends Task<LivingEntity> {
   private final Predicate<LivingEntity> mobType;
   private final float maxDistanceSquared;

   public FollowMobTask(EntityCategory entityCategory, float maxDistance) {
      this((livingEntity) -> {
         return entityCategory.equals(livingEntity.getType().getCategory());
      }, maxDistance);
   }

   public FollowMobTask(EntityType<?> entityType, float maxDistance) {
      this((livingEntity) -> {
         return entityType.equals(livingEntity.getType());
      }, maxDistance);
   }

   public FollowMobTask(Predicate<LivingEntity> mobType, float maxDistance) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.VISIBLE_MOBS, MemoryModuleState.VALUE_PRESENT));
      this.mobType = mobType;
      this.maxDistanceSquared = maxDistance * maxDistance;
   }

   protected boolean shouldRun(ServerWorld world, LivingEntity entity) {
      return ((List)entity.getBrain().getOptionalMemory(MemoryModuleType.VISIBLE_MOBS).get()).stream().anyMatch(this.mobType);
   }

   protected void run(ServerWorld world, LivingEntity entity, long time) {
      Brain<?> brain = entity.getBrain();
      brain.getOptionalMemory(MemoryModuleType.VISIBLE_MOBS).ifPresent((list) -> {
         list.stream().filter(this.mobType).filter((livingEntity2) -> {
            return livingEntity2.squaredDistanceTo(entity) <= (double)this.maxDistanceSquared;
         }).findFirst().ifPresent((livingEntity) -> {
            brain.putMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(livingEntity));
         });
      });
   }
}
