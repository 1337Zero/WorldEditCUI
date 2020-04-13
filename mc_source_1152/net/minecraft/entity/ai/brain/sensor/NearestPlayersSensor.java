package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;

public class NearestPlayersSensor extends Sensor<LivingEntity> {
   protected void sense(ServerWorld world, LivingEntity entity) {
      Stream var10000 = world.getPlayers().stream().filter(EntityPredicates.EXCEPT_SPECTATOR).filter((serverPlayerEntity) -> {
         return entity.squaredDistanceTo(serverPlayerEntity) < 256.0D;
      });
      entity.getClass();
      List<PlayerEntity> list = (List)var10000.sorted(Comparator.comparingDouble(entity::squaredDistanceTo)).collect(Collectors.toList());
      Brain<?> brain = entity.getBrain();
      brain.putMemory(MemoryModuleType.NEAREST_PLAYERS, list);
      MemoryModuleType var10001 = MemoryModuleType.NEAREST_VISIBLE_PLAYER;
      Stream var10002 = list.stream();
      entity.getClass();
      brain.setMemory(var10001, var10002.filter(entity::canSee).findFirst());
   }

   public Set<MemoryModuleType<?>> getOutputMemoryModules() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER);
   }
}
