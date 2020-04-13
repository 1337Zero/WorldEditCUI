package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.network.packet.PlayerPositionLookS2CPacket;
import net.minecraft.command.arguments.DefaultPosArgument;
import net.minecraft.command.arguments.EntityAnchorArgumentType;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.command.arguments.PosArgument;
import net.minecraft.command.arguments.RotationArgumentType;
import net.minecraft.command.arguments.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class TeleportCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("teleport").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.entities()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("location", Vec3ArgumentType.vec3()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntities(commandContext, "targets"), ((ServerCommandSource)commandContext.getSource()).getWorld(), Vec3ArgumentType.getPosArgument(commandContext, "location"), (PosArgument)null, (TeleportCommand.LookTarget)null);
      })).then(CommandManager.argument("rotation", RotationArgumentType.rotation()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntities(commandContext, "targets"), ((ServerCommandSource)commandContext.getSource()).getWorld(), Vec3ArgumentType.getPosArgument(commandContext, "location"), RotationArgumentType.getRotation(commandContext, "rotation"), (TeleportCommand.LookTarget)null);
      }))).then(((LiteralArgumentBuilder)CommandManager.literal("facing").then(CommandManager.literal("entity").then(((RequiredArgumentBuilder)CommandManager.argument("facingEntity", EntityArgumentType.entity()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntities(commandContext, "targets"), ((ServerCommandSource)commandContext.getSource()).getWorld(), Vec3ArgumentType.getPosArgument(commandContext, "location"), (PosArgument)null, new TeleportCommand.LookTarget(EntityArgumentType.getEntity(commandContext, "facingEntity"), EntityAnchorArgumentType.EntityAnchor.FEET));
      })).then(CommandManager.argument("facingAnchor", EntityAnchorArgumentType.entityAnchor()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntities(commandContext, "targets"), ((ServerCommandSource)commandContext.getSource()).getWorld(), Vec3ArgumentType.getPosArgument(commandContext, "location"), (PosArgument)null, new TeleportCommand.LookTarget(EntityArgumentType.getEntity(commandContext, "facingEntity"), EntityAnchorArgumentType.getEntityAnchor(commandContext, "facingAnchor")));
      }))))).then(CommandManager.argument("facingLocation", Vec3ArgumentType.vec3()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntities(commandContext, "targets"), ((ServerCommandSource)commandContext.getSource()).getWorld(), Vec3ArgumentType.getPosArgument(commandContext, "location"), (PosArgument)null, new TeleportCommand.LookTarget(Vec3ArgumentType.getVec3(commandContext, "facingLocation")));
      }))))).then(CommandManager.argument("destination", EntityArgumentType.entity()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getEntities(commandContext, "targets"), EntityArgumentType.getEntity(commandContext, "destination"));
      })))).then(CommandManager.argument("location", Vec3ArgumentType.vec3()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), Collections.singleton(((ServerCommandSource)commandContext.getSource()).getEntityOrThrow()), ((ServerCommandSource)commandContext.getSource()).getWorld(), Vec3ArgumentType.getPosArgument(commandContext, "location"), DefaultPosArgument.zero(), (TeleportCommand.LookTarget)null);
      }))).then(CommandManager.argument("destination", EntityArgumentType.entity()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), Collections.singleton(((ServerCommandSource)commandContext.getSource()).getEntityOrThrow()), EntityArgumentType.getEntity(commandContext, "destination"));
      })));
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("tp").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).redirect(literalCommandNode));
   }

   private static int execute(ServerCommandSource source, Collection<? extends Entity> targets, Entity destination) {
      Iterator var3 = targets.iterator();

      while(var3.hasNext()) {
         Entity entity = (Entity)var3.next();
         teleport(source, entity, (ServerWorld)destination.world, destination.getX(), destination.getY(), destination.getZ(), EnumSet.noneOf(PlayerPositionLookS2CPacket.Flag.class), destination.yaw, destination.pitch, (TeleportCommand.LookTarget)null);
      }

      if (targets.size() == 1) {
         source.sendFeedback(new TranslatableText("commands.teleport.success.entity.single", new Object[]{((Entity)targets.iterator().next()).getDisplayName(), destination.getDisplayName()}), true);
      } else {
         source.sendFeedback(new TranslatableText("commands.teleport.success.entity.multiple", new Object[]{targets.size(), destination.getDisplayName()}), true);
      }

      return targets.size();
   }

   private static int execute(ServerCommandSource source, Collection<? extends Entity> targets, ServerWorld world, PosArgument location, @Nullable PosArgument rotation, @Nullable TeleportCommand.LookTarget facingLocation) throws CommandSyntaxException {
      Vec3d vec3d = location.toAbsolutePos(source);
      Vec2f vec2f = rotation == null ? null : rotation.toAbsoluteRotation(source);
      Set<PlayerPositionLookS2CPacket.Flag> set = EnumSet.noneOf(PlayerPositionLookS2CPacket.Flag.class);
      if (location.isXRelative()) {
         set.add(PlayerPositionLookS2CPacket.Flag.X);
      }

      if (location.isYRelative()) {
         set.add(PlayerPositionLookS2CPacket.Flag.Y);
      }

      if (location.isZRelative()) {
         set.add(PlayerPositionLookS2CPacket.Flag.Z);
      }

      if (rotation == null) {
         set.add(PlayerPositionLookS2CPacket.Flag.X_ROT);
         set.add(PlayerPositionLookS2CPacket.Flag.Y_ROT);
      } else {
         if (rotation.isXRelative()) {
            set.add(PlayerPositionLookS2CPacket.Flag.X_ROT);
         }

         if (rotation.isYRelative()) {
            set.add(PlayerPositionLookS2CPacket.Flag.Y_ROT);
         }
      }

      Iterator var9 = targets.iterator();

      while(var9.hasNext()) {
         Entity entity = (Entity)var9.next();
         if (rotation == null) {
            teleport(source, entity, world, vec3d.x, vec3d.y, vec3d.z, set, entity.yaw, entity.pitch, facingLocation);
         } else {
            teleport(source, entity, world, vec3d.x, vec3d.y, vec3d.z, set, vec2f.y, vec2f.x, facingLocation);
         }
      }

      if (targets.size() == 1) {
         source.sendFeedback(new TranslatableText("commands.teleport.success.location.single", new Object[]{((Entity)targets.iterator().next()).getDisplayName(), vec3d.x, vec3d.y, vec3d.z}), true);
      } else {
         source.sendFeedback(new TranslatableText("commands.teleport.success.location.multiple", new Object[]{targets.size(), vec3d.x, vec3d.y, vec3d.z}), true);
      }

      return targets.size();
   }

   private static void teleport(ServerCommandSource source, Entity target, ServerWorld world, double x, double y, double z, Set<PlayerPositionLookS2CPacket.Flag> movementFlags, float yaw, float pitch, @Nullable TeleportCommand.LookTarget facingLocation) {
      if (target instanceof ServerPlayerEntity) {
         ChunkPos chunkPos = new ChunkPos(new BlockPos(x, y, z));
         world.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, target.getEntityId());
         target.stopRiding();
         if (((ServerPlayerEntity)target).isSleeping()) {
            ((ServerPlayerEntity)target).wakeUp(true, true);
         }

         if (world == target.world) {
            ((ServerPlayerEntity)target).networkHandler.teleportRequest(x, y, z, yaw, pitch, movementFlags);
         } else {
            ((ServerPlayerEntity)target).teleport(world, x, y, z, yaw, pitch);
         }

         target.setHeadYaw(yaw);
      } else {
         float f = MathHelper.wrapDegrees(yaw);
         float g = MathHelper.wrapDegrees(pitch);
         g = MathHelper.clamp(g, -90.0F, 90.0F);
         if (world == target.world) {
            target.refreshPositionAndAngles(x, y, z, f, g);
            target.setHeadYaw(f);
         } else {
            target.detach();
            target.dimension = world.dimension.getType();
            Entity entity = target;
            target = target.getType().create(world);
            if (target == null) {
               return;
            }

            target.copyFrom(entity);
            target.refreshPositionAndAngles(x, y, z, f, g);
            target.setHeadYaw(f);
            world.onDimensionChanged(target);
            entity.removed = true;
         }
      }

      if (facingLocation != null) {
         facingLocation.look(source, target);
      }

      if (!(target instanceof LivingEntity) || !((LivingEntity)target).isFallFlying()) {
         target.setVelocity(target.getVelocity().multiply(1.0D, 0.0D, 1.0D));
         target.onGround = true;
      }

   }

   static class LookTarget {
      private final Vec3d targetPos;
      private final Entity targetEntity;
      private final EntityAnchorArgumentType.EntityAnchor targetEntityAnchor;

      public LookTarget(Entity entity, EntityAnchorArgumentType.EntityAnchor entityAnchor) {
         this.targetEntity = entity;
         this.targetEntityAnchor = entityAnchor;
         this.targetPos = entityAnchor.positionAt(entity);
      }

      public LookTarget(Vec3d vec3d) {
         this.targetEntity = null;
         this.targetPos = vec3d;
         this.targetEntityAnchor = null;
      }

      public void look(ServerCommandSource source, Entity entity) {
         if (this.targetEntity != null) {
            if (entity instanceof ServerPlayerEntity) {
               ((ServerPlayerEntity)entity).method_14222(source.getEntityAnchor(), this.targetEntity, this.targetEntityAnchor);
            } else {
               entity.lookAt(source.getEntityAnchor(), this.targetPos);
            }
         } else {
            entity.lookAt(source.getEntityAnchor(), this.targetPos);
         }

      }
   }
}
