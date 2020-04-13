package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.server.world.ServerWorld;

public class NearestLivingEntitiesSensor extends Sensor<LivingEntity> {
   private static final TargetPredicate CLOSE_ENTITY_PREDICATE = (new TargetPredicate()).setBaseMaxDistance(16.0D).includeTeammates().ignoreEntityTargetRules().includeHidden();

   protected void sense(ServerWorld world, LivingEntity entity) {
      List<LivingEntity> list = world.getEntities(LivingEntity.class, entity.getBoundingBox().expand(16.0D, 16.0D, 16.0D), (livingEntity2) -> {
         return livingEntity2 != entity && livingEntity2.isAlive();
      });
      entity.getClass();
      list.sort(Comparator.comparingDouble(entity::squaredDistanceTo));
      Brain<?> brain = entity.getBrain();
      brain.putMemory(MemoryModuleType.MOBS, list);
      MemoryModuleType var10001 = MemoryModuleType.VISIBLE_MOBS;
      Stream var10002 = list.stream().filter((livingEntity2) -> {
         return CLOSE_ENTITY_PREDICATE.test(entity, livingEntity2);
      });
      entity.getClass();
      brain.putMemory(var10001, var10002.filter(entity::canSee).collect(Collectors.toList()));
   }

   public Set<MemoryModuleType<?>> getOutputMemoryModules() {
      return ImmutableSet.of(MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS);
   }
}
