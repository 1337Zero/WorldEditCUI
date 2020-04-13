package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityPosWrapper;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.GlobalPos;

public class MeetVillagerTask extends Task<LivingEntity> {
   public MeetVillagerTask() {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.MEETING_POINT, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.VISIBLE_MOBS, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.INTERACTION_TARGET, MemoryModuleState.VALUE_ABSENT));
   }

   protected boolean shouldRun(ServerWorld world, LivingEntity entity) {
      Brain<?> brain = entity.getBrain();
      Optional<GlobalPos> optional = brain.getOptionalMemory(MemoryModuleType.MEETING_POINT);
      return world.getRandom().nextInt(100) == 0 && optional.isPresent() && Objects.equals(world.getDimension().getType(), ((GlobalPos)optional.get()).getDimension()) && ((GlobalPos)optional.get()).getPos().isWithinDistance(entity.getPos(), 4.0D) && ((List)brain.getOptionalMemory(MemoryModuleType.VISIBLE_MOBS).get()).stream().anyMatch((livingEntity) -> {
         return EntityType.VILLAGER.equals(livingEntity.getType());
      });
   }

   protected void run(ServerWorld world, LivingEntity entity, long time) {
      Brain<?> brain = entity.getBrain();
      brain.getOptionalMemory(MemoryModuleType.VISIBLE_MOBS).ifPresent((list) -> {
         list.stream().filter((livingEntity) -> {
            return EntityType.VILLAGER.equals(livingEntity.getType());
         }).filter((livingEntity2) -> {
            return livingEntity2.squaredDistanceTo(entity) <= 32.0D;
         }).findFirst().ifPresent((livingEntity) -> {
            brain.putMemory(MemoryModuleType.INTERACTION_TARGET, livingEntity);
            brain.putMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(livingEntity));
            brain.putMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityPosWrapper(livingEntity), 0.3F, 1));
         });
      });
   }
}
