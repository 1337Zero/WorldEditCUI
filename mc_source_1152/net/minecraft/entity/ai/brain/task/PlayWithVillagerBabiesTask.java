package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetFinder;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityPosWrapper;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.mob.MobEntityWithAi;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PlayWithVillagerBabiesTask extends Task<MobEntityWithAi> {
   public PlayWithVillagerBabiesTask() {
      super(ImmutableMap.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.INTERACTION_TARGET, MemoryModuleState.REGISTERED));
   }

   protected boolean shouldRun(ServerWorld serverWorld, MobEntityWithAi mobEntityWithAi) {
      return serverWorld.getRandom().nextInt(10) == 0 && this.hasVisibleVillagerBabies(mobEntityWithAi);
   }

   protected void run(ServerWorld serverWorld, MobEntityWithAi mobEntityWithAi, long l) {
      LivingEntity livingEntity = this.findVisibleVillagerBaby(mobEntityWithAi);
      if (livingEntity != null) {
         this.setGroundTarget(serverWorld, mobEntityWithAi, livingEntity);
      } else {
         Optional<LivingEntity> optional = this.getLeastPopularBabyInteractionTarget(mobEntityWithAi);
         if (optional.isPresent()) {
            setPlayTarget(mobEntityWithAi, (LivingEntity)optional.get());
         } else {
            this.getVisibleMob(mobEntityWithAi).ifPresent((livingEntityx) -> {
               setPlayTarget(mobEntityWithAi, livingEntityx);
            });
         }
      }
   }

   private void setGroundTarget(ServerWorld world, MobEntityWithAi entity, LivingEntity unusedBaby) {
      for(int i = 0; i < 10; ++i) {
         Vec3d vec3d = TargetFinder.findGroundTarget(entity, 20, 8);
         if (vec3d != null && world.isNearOccupiedPointOfInterest(new BlockPos(vec3d))) {
            entity.getBrain().putMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3d, 0.6F, 0));
            return;
         }
      }

   }

   private static void setPlayTarget(MobEntityWithAi entity, LivingEntity target) {
      Brain<?> brain = entity.getBrain();
      brain.putMemory(MemoryModuleType.INTERACTION_TARGET, target);
      brain.putMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(target));
      brain.putMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityPosWrapper(target), 0.6F, 1));
   }

   private Optional<LivingEntity> getVisibleMob(MobEntityWithAi entity) {
      return this.getVisibleVillagerBabies(entity).stream().findAny();
   }

   private Optional<LivingEntity> getLeastPopularBabyInteractionTarget(MobEntityWithAi entity) {
      Map<LivingEntity, Integer> map = this.getBabyInteractionTargetCounts(entity);
      return map.entrySet().stream().sorted(Comparator.comparingInt(Entry::getValue)).filter((entry) -> {
         return (Integer)entry.getValue() > 0 && (Integer)entry.getValue() <= 5;
      }).map(Entry::getKey).findFirst();
   }

   private Map<LivingEntity, Integer> getBabyInteractionTargetCounts(MobEntityWithAi entity) {
      Map<LivingEntity, Integer> map = Maps.newHashMap();
      this.getVisibleVillagerBabies(entity).stream().filter(this::hasInteractionTarget).forEach((livingEntity) -> {
         Integer var10000 = (Integer)map.compute(this.getInteractionTarget(livingEntity), (livingEntityx, integer) -> {
            return integer == null ? 1 : integer + 1;
         });
      });
      return map;
   }

   private List<LivingEntity> getVisibleVillagerBabies(MobEntityWithAi entity) {
      return (List)entity.getBrain().getOptionalMemory(MemoryModuleType.VISIBLE_VILLAGER_BABIES).get();
   }

   private LivingEntity getInteractionTarget(LivingEntity entity) {
      return (LivingEntity)entity.getBrain().getOptionalMemory(MemoryModuleType.INTERACTION_TARGET).get();
   }

   @Nullable
   private LivingEntity findVisibleVillagerBaby(LivingEntity entity) {
      return (LivingEntity)((List)entity.getBrain().getOptionalMemory(MemoryModuleType.VISIBLE_VILLAGER_BABIES).get()).stream().filter((livingEntity2) -> {
         return this.isInteractionTargetOf(entity, livingEntity2);
      }).findAny().orElse((Object)null);
   }

   private boolean hasInteractionTarget(LivingEntity entity) {
      return entity.getBrain().getOptionalMemory(MemoryModuleType.INTERACTION_TARGET).isPresent();
   }

   private boolean isInteractionTargetOf(LivingEntity entity, LivingEntity other) {
      return other.getBrain().getOptionalMemory(MemoryModuleType.INTERACTION_TARGET).filter((livingEntity2) -> {
         return livingEntity2 == entity;
      }).isPresent();
   }

   private boolean hasVisibleVillagerBabies(MobEntityWithAi entity) {
      return entity.getBrain().hasMemoryModule(MemoryModuleType.VISIBLE_VILLAGER_BABIES);
   }
}
