package net.minecraft.world;

import com.google.common.collect.Streams;
import java.util.Collections;
import java.util.Set;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.util.BooleanBiFunction;
import net.minecraft.util.CuboidBlockIterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.border.WorldBorder;

public interface CollisionView extends BlockView {
   WorldBorder getWorldBorder();

   @Nullable
   BlockView getExistingChunk(int chunkX, int chunkZ);

   default boolean intersectsEntities(@Nullable Entity except, VoxelShape shape) {
      return true;
   }

   default boolean canPlace(BlockState state, BlockPos pos, EntityContext context) {
      VoxelShape voxelShape = state.getCollisionShape(this, pos, context);
      return voxelShape.isEmpty() || this.intersectsEntities((Entity)null, voxelShape.offset((double)pos.getX(), (double)pos.getY(), (double)pos.getZ()));
   }

   default boolean intersectsEntities(Entity entity) {
      return this.intersectsEntities(entity, VoxelShapes.cuboid(entity.getBoundingBox()));
   }

   default boolean doesNotCollide(Box box) {
      return this.doesNotCollide((Entity)null, box, Collections.emptySet());
   }

   default boolean doesNotCollide(Entity entity) {
      return this.doesNotCollide(entity, entity.getBoundingBox(), Collections.emptySet());
   }

   default boolean doesNotCollide(Entity entity, Box box) {
      return this.doesNotCollide(entity, box, Collections.emptySet());
   }

   default boolean doesNotCollide(@Nullable Entity entity, Box entityBoundingBox, Set<Entity> otherEntities) {
      return this.getCollisions(entity, entityBoundingBox, otherEntities).allMatch(VoxelShape::isEmpty);
   }

   default Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, Box box, Set<Entity> excluded) {
      return Stream.empty();
   }

   default Stream<VoxelShape> getCollisions(@Nullable Entity entity, Box box, Set<Entity> excluded) {
      return Streams.concat(new Stream[]{this.getBlockCollisions(entity, box), this.getEntityCollisions(entity, box, excluded)});
   }

   default Stream<VoxelShape> getBlockCollisions(@Nullable final Entity entity, Box box) {
      int i = MathHelper.floor(box.x1 - 1.0E-7D) - 1;
      int j = MathHelper.floor(box.x2 + 1.0E-7D) + 1;
      int k = MathHelper.floor(box.y1 - 1.0E-7D) - 1;
      int l = MathHelper.floor(box.y2 + 1.0E-7D) + 1;
      int m = MathHelper.floor(box.z1 - 1.0E-7D) - 1;
      int n = MathHelper.floor(box.z2 + 1.0E-7D) + 1;
      final EntityContext entityContext = entity == null ? EntityContext.absent() : EntityContext.of(entity);
      final CuboidBlockIterator cuboidBlockIterator = new CuboidBlockIterator(i, k, m, j, l, n);
      final BlockPos.Mutable mutable = new BlockPos.Mutable();
      final VoxelShape voxelShape = VoxelShapes.cuboid(box);
      return StreamSupport.stream(new AbstractSpliterator<VoxelShape>(Long.MAX_VALUE, 1280) {
         boolean field_19296 = entity == null;

         public boolean tryAdvance(Consumer<? super VoxelShape> consumer) {
            if (!this.field_19296) {
               this.field_19296 = true;
               VoxelShape voxelShapex = CollisionView.this.getWorldBorder().asVoxelShape();
               boolean bl = VoxelShapes.matchesAnywhere(voxelShapex, VoxelShapes.cuboid(entity.getBoundingBox().contract(1.0E-7D)), BooleanBiFunction.AND);
               boolean bl2 = VoxelShapes.matchesAnywhere(voxelShapex, VoxelShapes.cuboid(entity.getBoundingBox().expand(1.0E-7D)), BooleanBiFunction.AND);
               if (!bl && bl2) {
                  consumer.accept(voxelShapex);
                  return true;
               }
            }

            VoxelShape voxelShape3;
            do {
               int l;
               BlockState blockState;
               int i;
               int j;
               int k;
               do {
                  do {
                     BlockView blockView;
                     do {
                        do {
                           if (!cuboidBlockIterator.step()) {
                              return false;
                           }

                           i = cuboidBlockIterator.getX();
                           j = cuboidBlockIterator.getY();
                           k = cuboidBlockIterator.getZ();
                           l = cuboidBlockIterator.getEdgeCoordinatesCount();
                        } while(l == 3);

                        int m = i >> 4;
                        int n = k >> 4;
                        blockView = CollisionView.this.getExistingChunk(m, n);
                     } while(blockView == null);

                     mutable.set(i, j, k);
                     blockState = blockView.getBlockState(mutable);
                  } while(l == 1 && !blockState.exceedsCube());
               } while(l == 2 && blockState.getBlock() != Blocks.MOVING_PISTON);

               VoxelShape voxelShape2 = blockState.getCollisionShape(CollisionView.this, mutable, entityContext);
               voxelShape3 = voxelShape2.offset((double)i, (double)j, (double)k);
            } while(!VoxelShapes.matchesAnywhere(voxelShape, voxelShape3, BooleanBiFunction.AND));

            consumer.accept(voxelShape3);
            return true;
         }
      }, false);
   }
}
