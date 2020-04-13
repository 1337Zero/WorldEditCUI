package com.mumfrey.worldeditcui.render.points;

/*
 * This file is part of LiteLoader.
 * Copyright (C) 2012-16 Adam Mummery-Smith
 * All Rights Reserved.
 */

import java.util.List;
import java.util.Optional;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.RayTraceContext.FluidHandling;
import net.minecraft.world.RayTraceContext.ShapeType;
public abstract class EntityUtilities {
	
	
	//static final Predicate<Entity> TRACEABLE = Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>() {
	@SuppressWarnings("unchecked")
	static final Predicate<Entity> TRACEABLE = Predicates.and( new Predicate<Entity>() {
		@Override
		public boolean apply(Entity entity) {
			return entity != null && entity.collides();
		}
	});

	static final class EntityTrace {
		Entity entity;
		Vec3d location;
		double distance;

		EntityTrace(double entityDistance) {
			this.distance = entityDistance;
		}
		//net.minecraft.util.hit.EntityHitResult
		//net.minecraft.util.hit.
		//BlockHitResult hit = new BlockHitResult(VEC3D, Directon, BlockPos,boolean);
		EntityHitResult asRayTraceResult() {
			return new EntityHitResult(this.entity, this.location);
		}
	}

	public static EntityHitResult rayTraceFromEntity(Entity source, double traceDistance, float partialTicks,
			boolean includeEntities) {
		EntityHitResult blockRay = EntityUtilities.rayTraceFromEntity(source, traceDistance, partialTicks);

		
		if (!includeEntities) {
			return blockRay;
		}

		Vec3d traceStart = EntityUtilities.getPositionEyes(source, partialTicks);
		double blockDistance = (blockRay != null) ? blockRay.getPos().distanceTo(traceStart) : traceDistance;
		EntityTrace entityRay = EntityUtilities.rayTraceEntities(source, traceDistance, partialTicks, blockDistance,
				traceStart);

		if (entityRay.entity != null && (entityRay.distance < blockDistance || blockRay == null)) {
			return entityRay.asRayTraceResult();
		}

		return blockRay;
	}

	private static EntityTrace rayTraceEntities(Entity source, double traceDistance, float partialTicks,
			double blockDistance, Vec3d traceStart) {
		EntityTrace trace = new EntityTrace(blockDistance);

		
		Vec3d lookDir = source.getCameraPosVec(partialTicks).multiply(traceDistance);
		
		Vec3d traceEnd = traceStart.add(lookDir);

		for (final Entity entity : EntityUtilities.getTraceEntities(source, traceDistance, lookDir,
				EntityUtilities.TRACEABLE)) {
			Box entityBB = entity.getBoundingBox().expand(entity.getCollisionBox().getAverageSideLength());
			
			// = entityBB.rayTrace(boxes, from, to, pos)
			
			
			
			//RayTraceResult entityRay = entityBB.calculateIntercept(traceStart, traceEnd);
			;
			Optional<Vec3d> entityRay = entityBB.intersection(entityBB).rayTrace(traceStart, traceEnd);
			
			
			if (entityBB.contains(traceStart)) {
				if (trace.distance >= 0.0D) {
					trace.entity = entity;
																		//hitVec
					trace.location = entityRay.isPresent() ? traceStart : entityRay.get();
					trace.distance = 0.0D;
				}
				continue;
			}

			if (entityRay.isPresent()) {
				continue;
			}

			double distanceToEntity = traceStart.distanceTo(entityRay.get());

			if (distanceToEntity < trace.distance || trace.distance == 0.0D) {
				if (entity.getPrimaryPassenger() == source.getPrimaryPassenger()) {
					if (trace.distance == 0.0D) {
						trace.entity = entity;
						trace.location = entityRay.get();
					}
				} else {
					trace.entity = entity;
					trace.location = entityRay.get();
					trace.distance = distanceToEntity;
				}
			}
		}

		return trace;
	}

	private static List<Entity> getTraceEntities(Entity source, double traceDistance, Vec3d dir,Predicate<Entity> filter) {		
		Box boundingBox = source.getBoundingBox();
		Box traceBox = boundingBox.expand(dir.x, dir.y, dir.z);		
		List<Entity> entities = source.world.getEntities(source, traceBox.expand(1.0F,1.0F,1.0F),filter);
		return entities;
	}

	public static EntityHitResult rayTraceFromEntity(Entity source, double traceDistance, float partialTicks) {
		Vec3d traceStart = EntityUtilities.getPositionEyes(source, partialTicks);
		Vec3d lookDir = source.getCameraPosVec(partialTicks).multiply(traceDistance);
		//Vec3d lookDir = source.getLook(partialTicks).scale(traceDistance);
		Vec3d traceEnd = traceStart.add(lookDir);
		
		RayTraceContext con = new RayTraceContext(traceStart, traceEnd, ShapeType.OUTLINE, FluidHandling.NONE, source);
		
		//return source.world.rayTraceBlocks(traceStart, traceEnd, RayTraceFluidMode.NEVER, false, true);
		
		return (EntityHitResult) source.rayTrace(traceDistance, (float)traceDistance, true);
	}

	public static Vec3d getPositionEyes(Entity entity, float partialTicks) {
		if (partialTicks == 1.0F) {
			return new Vec3d(entity.getX(), entity.getY() + entity.getEyeHeight(EntityPose.STANDING), entity.getZ());
		}
		
		double interpX = entity.prevX + (entity.getX() - entity.prevX) * partialTicks;
		double interpY = entity.prevY + (entity.getY() - entity.prevY) * partialTicks + entity.getEyeHeight(EntityPose.STANDING);
		double interpZ = entity.prevZ + (entity.getZ() - entity.prevZ) * partialTicks;
		return new Vec3d(interpX, interpY, interpZ);
	}
}