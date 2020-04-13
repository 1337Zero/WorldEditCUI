package net.minecraft.entity.ai.pathing;

import net.minecraft.client.network.DebugRendererInfoManager;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.util.Util;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;

public class SwimNavigation extends EntityNavigation {
   private boolean field_6689;

   public SwimNavigation(MobEntity entity, World world) {
      super(entity, world);
   }

   protected PathNodeNavigator createPathNodeNavigator(int i) {
      this.field_6689 = this.entity instanceof DolphinEntity;
      this.nodeMaker = new WaterPathNodeMaker(this.field_6689);
      return new PathNodeNavigator(this.nodeMaker, i);
   }

   protected boolean isAtValidPosition() {
      return this.field_6689 || this.isInLiquid();
   }

   protected Vec3d getPos() {
      return new Vec3d(this.entity.getX(), this.entity.getBodyY(0.5D), this.entity.getZ());
   }

   public void tick() {
      ++this.tickCount;
      if (this.shouldRecalculate) {
         this.recalculatePath();
      }

      if (!this.isIdle()) {
         Vec3d vec3d2;
         if (this.isAtValidPosition()) {
            this.method_6339();
         } else if (this.currentPath != null && this.currentPath.getCurrentNodeIndex() < this.currentPath.getLength()) {
            vec3d2 = this.currentPath.getNodePosition(this.entity, this.currentPath.getCurrentNodeIndex());
            if (MathHelper.floor(this.entity.getX()) == MathHelper.floor(vec3d2.x) && MathHelper.floor(this.entity.getY()) == MathHelper.floor(vec3d2.y) && MathHelper.floor(this.entity.getZ()) == MathHelper.floor(vec3d2.z)) {
               this.currentPath.setCurrentNodeIndex(this.currentPath.getCurrentNodeIndex() + 1);
            }
         }

         DebugRendererInfoManager.sendPathfindingData(this.world, this.entity, this.currentPath, this.field_6683);
         if (!this.isIdle()) {
            vec3d2 = this.currentPath.getNodePosition(this.entity);
            this.entity.getMoveControl().moveTo(vec3d2.x, vec3d2.y, vec3d2.z, this.speed);
         }
      }
   }

   protected void method_6339() {
      if (this.currentPath != null) {
         Vec3d vec3d = this.getPos();
         float f = this.entity.getWidth();
         float g = f > 0.75F ? f / 2.0F : 0.75F - f / 2.0F;
         Vec3d vec3d2 = this.entity.getVelocity();
         if (Math.abs(vec3d2.x) > 0.2D || Math.abs(vec3d2.z) > 0.2D) {
            g = (float)((double)g * vec3d2.length() * 6.0D);
         }

         int i = true;
         Vec3d vec3d3 = this.currentPath.getCurrentPosition();
         if (Math.abs(this.entity.getX() - (vec3d3.x + 0.5D)) < (double)g && Math.abs(this.entity.getZ() - (vec3d3.z + 0.5D)) < (double)g && Math.abs(this.entity.getY() - vec3d3.y) < (double)(g * 2.0F)) {
            this.currentPath.next();
         }

         for(int j = Math.min(this.currentPath.getCurrentNodeIndex() + 6, this.currentPath.getLength() - 1); j > this.currentPath.getCurrentNodeIndex(); --j) {
            vec3d3 = this.currentPath.getNodePosition(this.entity, j);
            if (vec3d3.squaredDistanceTo(vec3d) <= 36.0D && this.canPathDirectlyThrough(vec3d, vec3d3, 0, 0, 0)) {
               this.currentPath.setCurrentNodeIndex(j);
               break;
            }
         }

         this.method_6346(vec3d);
      }
   }

   protected void method_6346(Vec3d vec3d) {
      if (this.tickCount - this.field_6674 > 100) {
         if (vec3d.squaredDistanceTo(this.field_6672) < 2.25D) {
            this.stop();
         }

         this.field_6674 = this.tickCount;
         this.field_6672 = vec3d;
      }

      if (this.currentPath != null && !this.currentPath.isFinished()) {
         Vec3d vec3d2 = this.currentPath.getCurrentPosition();
         if (vec3d2.equals(this.field_6680)) {
            this.field_6670 += Util.getMeasuringTimeMs() - this.field_6669;
         } else {
            this.field_6680 = vec3d2;
            double d = vec3d.distanceTo(this.field_6680);
            this.field_6682 = this.entity.getMovementSpeed() > 0.0F ? d / (double)this.entity.getMovementSpeed() * 100.0D : 0.0D;
         }

         if (this.field_6682 > 0.0D && (double)this.field_6670 > this.field_6682 * 2.0D) {
            this.field_6680 = Vec3d.ZERO;
            this.field_6670 = 0L;
            this.field_6682 = 0.0D;
            this.stop();
         }

         this.field_6669 = Util.getMeasuringTimeMs();
      }

   }

   protected boolean canPathDirectlyThrough(Vec3d origin, Vec3d target, int sizeX, int sizeY, int sizeZ) {
      Vec3d vec3d = new Vec3d(target.x, target.y + (double)this.entity.getHeight() * 0.5D, target.z);
      return this.world.rayTrace(new RayTraceContext(origin, vec3d, RayTraceContext.ShapeType.COLLIDER, RayTraceContext.FluidHandling.NONE, this.entity)).getType() == HitResult.Type.MISS;
   }

   public boolean isValidPosition(BlockPos pos) {
      return !this.world.getBlockState(pos).isFullOpaque(this.world, pos);
   }

   public void setCanSwim(boolean canSwim) {
   }
}
