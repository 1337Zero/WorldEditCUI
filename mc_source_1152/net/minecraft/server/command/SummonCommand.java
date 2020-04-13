package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.arguments.EntitySummonArgumentType;
import net.minecraft.command.arguments.NbtCompoundTagArgumentType;
import net.minecraft.command.arguments.Vec3ArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SummonCommand {
   private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.summon.failed", new Object[0]));

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("summon").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("entity", EntitySummonArgumentType.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntitySummonArgumentType.getEntitySummon(commandContext, "entity"), ((ServerCommandSource)commandContext.getSource()).getPosition(), new CompoundTag(), true);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("pos", Vec3ArgumentType.vec3()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntitySummonArgumentType.getEntitySummon(commandContext, "entity"), Vec3ArgumentType.getVec3(commandContext, "pos"), new CompoundTag(), true);
      })).then(CommandManager.argument("nbt", NbtCompoundTagArgumentType.nbtCompound()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), EntitySummonArgumentType.getEntitySummon(commandContext, "entity"), Vec3ArgumentType.getVec3(commandContext, "pos"), NbtCompoundTagArgumentType.getCompoundTag(commandContext, "nbt"), false);
      })))));
   }

   private static int execute(ServerCommandSource source, Identifier entity, Vec3d pos, CompoundTag nbt, boolean initialize) throws CommandSyntaxException {
      CompoundTag compoundTag = nbt.copy();
      compoundTag.putString("id", entity.toString());
      if (EntityType.getId(EntityType.LIGHTNING_BOLT).equals(entity)) {
         LightningEntity lightningEntity = new LightningEntity(source.getWorld(), pos.x, pos.y, pos.z, false);
         source.getWorld().addLightning(lightningEntity);
         source.sendFeedback(new TranslatableText("commands.summon.success", new Object[]{lightningEntity.getDisplayName()}), true);
         return 1;
      } else {
         ServerWorld serverWorld = source.getWorld();
         Entity entity2 = EntityType.loadEntityWithPassengers(compoundTag, serverWorld, (entityx) -> {
            entityx.refreshPositionAndAngles(pos.x, pos.y, pos.z, entityx.yaw, entityx.pitch);
            return !serverWorld.tryLoadEntity(entityx) ? null : entityx;
         });
         if (entity2 == null) {
            throw FAILED_EXCEPTION.create();
         } else {
            if (initialize && entity2 instanceof MobEntity) {
               ((MobEntity)entity2).initialize(source.getWorld(), source.getWorld().getLocalDifficulty(new BlockPos(entity2)), SpawnType.COMMAND, (EntityData)null, (CompoundTag)null);
            }

            source.sendFeedback(new TranslatableText("commands.summon.success", new Object[]{entity2.getDisplayName()}), true);
            return 1;
         }
      }
   }
}
