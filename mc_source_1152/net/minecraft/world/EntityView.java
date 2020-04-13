package net.minecraft.world;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.BooleanBiFunction;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public interface EntityView {
   List<Entity> getEntities(@Nullable Entity except, Box box, @Nullable Predicate<? super Entity> predicate);

   <T extends Entity> List<T> getEntities(Class<? extends T> entityClass, Box box, @Nullable Predicate<? super T> predicate);

   default <T extends Entity> List<T> getEntitiesIncludingUngeneratedChunks(Class<? extends T> entityClass, Box box, @Nullable Predicate<? super T> predicate) {
      return this.getEntities(entityClass, box, predicate);
   }

   List<? extends PlayerEntity> getPlayers();

   default List<Entity> getEntities(@Nullable Entity except, Box box) {
      return this.getEntities(except, box, EntityPredicates.EXCEPT_SPECTATOR);
   }

   default boolean intersectsEntities(@Nullable Entity except, VoxelShape shape) {
      return shape.isEmpty() ? true : this.getEntities(except, shape.getBoundingBox()).stream().filter((e) -> {
         return !e.removed && e.inanimate && (except == null || !e.isConnectedThroughVehicle(except));
      }).noneMatch((entity) -> {
         return VoxelShapes.matchesAnywhere(shape, VoxelShapes.cuboid(entity.getBoundingBox()), BooleanBiFunction.AND);
      });
   }

   default <T extends Entity> List<T> getNonSpectatingEntities(Class<? extends T> entityClass, Box box) {
      return this.getEntities(entityClass, box, EntityPredicates.EXCEPT_SPECTATOR);
   }

   default <T extends Entity> List<T> getEntitiesIncludingUngeneratedChunks(Class<? extends T> entityClass, Box box) {
      return this.getEntitiesIncludingUngeneratedChunks(entityClass, box, EntityPredicates.EXCEPT_SPECTATOR);
   }

   default Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, Box box, Set<Entity> excluded) {
      if (box.getAverageSideLength() < 1.0E-7D) {
         return Stream.empty();
      } else {
         Box box2 = box.expand(1.0E-7D);
         Stream var10000 = this.getEntities(entity, box2).stream().filter((e) -> {
            return !excluded.contains(e);
         }).filter((e) -> {
            return entity == null || !entity.isConnectedThroughVehicle(e);
         }).flatMap((e) -> {
            return Stream.of(e.getCollisionBox(), entity == null ? null : entity.getHardCollisionBox(e));
         }).filter(Objects::nonNull);
         box2.getClass();
         return var10000.filter(box2::intersects).map(VoxelShapes::cuboid);
      }
   }

   @Nullable
   default PlayerEntity getClosestPlayer(double x, double y, double z, double maxDistance, @Nullable Predicate<Entity> targetPredicate) {
      double d = -1.0D;
      PlayerEntity playerEntity = null;
      Iterator var13 = this.getPlayers().iterator();

      while(true) {
         PlayerEntity playerEntity2;
         double e;
         do {
            do {
               do {
                  if (!var13.hasNext()) {
                     return playerEntity;
                  }

                  playerEntity2 = (PlayerEntity)var13.next();
               } while(targetPredicate != null && !targetPredicate.test(playerEntity2));

               e = playerEntity2.squaredDistanceTo(x, y, z);
            } while(maxDistance >= 0.0D && e >= maxDistance * maxDistance);
         } while(d != -1.0D && e >= d);

         d = e;
         playerEntity = playerEntity2;
      }
   }

   @Nullable
   default PlayerEntity getClosestPlayer(Entity entity, double maxDistance) {
      return this.getClosestPlayer(entity.getX(), entity.getY(), entity.getZ(), maxDistance, false);
   }

   @Nullable
   default PlayerEntity getClosestPlayer(double x, double y, double z, double maxDistance, boolean ignoreCreative) {
      Predicate<Entity> predicate = ignoreCreative ? EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR : EntityPredicates.EXCEPT_SPECTATOR;
      return this.getClosestPlayer(x, y, z, maxDistance, predicate);
   }

   @Nullable
   default PlayerEntity getClosestPlayer(double x, double z, double maxDistance) {
      double d = -1.0D;
      PlayerEntity playerEntity = null;
      Iterator var10 = this.getPlayers().iterator();

      while(true) {
         PlayerEntity playerEntity2;
         double e;
         do {
            do {
               do {
                  if (!var10.hasNext()) {
                     return playerEntity;
                  }

                  playerEntity2 = (PlayerEntity)var10.next();
               } while(!EntityPredicates.EXCEPT_SPECTATOR.test(playerEntity2));

               e = playerEntity2.squaredDistanceTo(x, playerEntity2.getY(), z);
            } while(maxDistance >= 0.0D && e >= maxDistance * maxDistance);
         } while(d != -1.0D && e >= d);

         d = e;
         playerEntity = playerEntity2;
      }
   }

   default boolean isPlayerInRange(double x, double y, double z, double range) {
      Iterator var9 = this.getPlayers().iterator();

      double d;
      do {
         PlayerEntity playerEntity;
         do {
            do {
               if (!var9.hasNext()) {
                  return false;
               }

               playerEntity = (PlayerEntity)var9.next();
            } while(!EntityPredicates.EXCEPT_SPECTATOR.test(playerEntity));
         } while(!EntityPredicates.VALID_ENTITY_LIVING.test(playerEntity));

         d = playerEntity.squaredDistanceTo(x, y, z);
      } while(range >= 0.0D && d >= range * range);

      return true;
   }

   @Nullable
   default PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, LivingEntity entity) {
      return (PlayerEntity)this.getClosestEntity(this.getPlayers(), targetPredicate, entity, entity.getX(), entity.getY(), entity.getZ());
   }

   @Nullable
   default PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, LivingEntity entity, double x, double y, double z) {
      return (PlayerEntity)this.getClosestEntity(this.getPlayers(), targetPredicate, entity, x, y, z);
   }

   @Nullable
   default PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, double x, double y, double z) {
      return (PlayerEntity)this.getClosestEntity(this.getPlayers(), targetPredicate, (LivingEntity)null, x, y, z);
   }

   @Nullable
   default <T extends LivingEntity> T getClosestEntity(Class<? extends T> entityClass, TargetPredicate targetPredicate, @Nullable LivingEntity entity, double x, double y, double z, Box box) {
      return this.getClosestEntity(this.getEntities((Class)entityClass, box, (Predicate)null), targetPredicate, entity, x, y, z);
   }

   @Nullable
   default <T extends LivingEntity> T getClosestEntityIncludingUngeneratedChunks(Class<? extends T> entityClass, TargetPredicate targetPredicate, @Nullable LivingEntity entity, double x, double y, double z, Box box) {
      return this.getClosestEntity(this.getEntitiesIncludingUngeneratedChunks(entityClass, box, (Predicate)null), targetPredicate, entity, x, y, z);
   }

   @Nullable
   default <T extends LivingEntity> T getClosestEntity(List<? extends T> entityList, TargetPredicate targetPredicate, @Nullable LivingEntity entity, double x, double y, double z) {
      double d = -1.0D;
      T livingEntity = null;
      Iterator var13 = entityList.iterator();

      while(true) {
         LivingEntity livingEntity2;
         double e;
         do {
            do {
               if (!var13.hasNext()) {
                  return livingEntity;
               }

               livingEntity2 = (LivingEntity)var13.next();
            } while(!targetPredicate.test(entity, livingEntity2));

            e = livingEntity2.squaredDistanceTo(x, y, z);
         } while(d != -1.0D && e >= d);

         d = e;
         livingEntity = livingEntity2;
      }
   }

   default List<PlayerEntity> getPlayers(TargetPredicate targetPredicate, LivingEntity entity, Box box) {
      List<PlayerEntity> list = Lists.newArrayList();
      Iterator var5 = this.getPlayers().iterator();

      while(var5.hasNext()) {
         PlayerEntity playerEntity = (PlayerEntity)var5.next();
         if (box.contains(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ()) && targetPredicate.test(entity, playerEntity)) {
            list.add(playerEntity);
         }
      }

      return list;
   }

   default <T extends LivingEntity> List<T> getTargets(Class<? extends T> entityClass, TargetPredicate targetPredicate, LivingEntity targettingEntity, Box box) {
      List<T> list = this.getEntities((Class)entityClass, box, (Predicate)null);
      List<T> list2 = Lists.newArrayList();
      Iterator var7 = list.iterator();

      while(var7.hasNext()) {
         T livingEntity = (LivingEntity)var7.next();
         if (targetPredicate.test(targettingEntity, livingEntity)) {
            list2.add(livingEntity);
         }
      }

      return list2;
   }

   @Nullable
   default PlayerEntity getPlayerByUuid(UUID uuid) {
      for(int i = 0; i < this.getPlayers().size(); ++i) {
         PlayerEntity playerEntity = (PlayerEntity)this.getPlayers().get(i);
         if (uuid.equals(playerEntity.getUuid())) {
            return playerEntity;
         }
      }

      return null;
   }
}
