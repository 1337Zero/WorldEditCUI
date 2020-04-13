package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.List;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;

public class DatapackCommand {
   private static final DynamicCommandExceptionType UNKNOWN_DATAPACK_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("commands.datapack.unknown", new Object[]{object});
   });
   private static final DynamicCommandExceptionType ALREADY_ENABLED_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("commands.datapack.enable.failed", new Object[]{object});
   });
   private static final DynamicCommandExceptionType ALREADY_DISABLED_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("commands.datapack.disable.failed", new Object[]{object});
   });
   private static final SuggestionProvider<ServerCommandSource> ENABLED_CONTAINERS_SUGGESTION_PROVIDER = (commandContext, suggestionsBuilder) -> {
      return CommandSource.suggestMatching(((ServerCommandSource)commandContext.getSource()).getMinecraftServer().getDataPackManager().getEnabledProfiles().stream().map(ResourcePackProfile::getName).map(StringArgumentType::escapeIfRequired), suggestionsBuilder);
   };
   private static final SuggestionProvider<ServerCommandSource> DISABLED_CONTAINERS_SUGGESTION_PROVIDER = (commandContext, suggestionsBuilder) -> {
      return CommandSource.suggestMatching(((ServerCommandSource)commandContext.getSource()).getMinecraftServer().getDataPackManager().getDisabledProfiles().stream().map(ResourcePackProfile::getName).map(StringArgumentType::escapeIfRequired), suggestionsBuilder);
   };

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("datapack").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(CommandManager.literal("enable").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("name", StringArgumentType.string()).suggests(DISABLED_CONTAINERS_SUGGESTION_PROVIDER).executes((commandContext) -> {
         return executeEnable((ServerCommandSource)commandContext.getSource(), getPackContainer(commandContext, "name", true), (list, resourcePackProfile) -> {
            resourcePackProfile.getInitialPosition().insert(list, resourcePackProfile, (resourcePackProfilex) -> {
               return resourcePackProfilex;
            }, false);
         });
      })).then(CommandManager.literal("after").then(CommandManager.argument("existing", StringArgumentType.string()).suggests(ENABLED_CONTAINERS_SUGGESTION_PROVIDER).executes((commandContext) -> {
         return executeEnable((ServerCommandSource)commandContext.getSource(), getPackContainer(commandContext, "name", true), (list, resourcePackProfile) -> {
            list.add(list.indexOf(getPackContainer(commandContext, "existing", false)) + 1, resourcePackProfile);
         });
      })))).then(CommandManager.literal("before").then(CommandManager.argument("existing", StringArgumentType.string()).suggests(ENABLED_CONTAINERS_SUGGESTION_PROVIDER).executes((commandContext) -> {
         return executeEnable((ServerCommandSource)commandContext.getSource(), getPackContainer(commandContext, "name", true), (list, resourcePackProfile) -> {
            list.add(list.indexOf(getPackContainer(commandContext, "existing", false)), resourcePackProfile);
         });
      })))).then(CommandManager.literal("last").executes((commandContext) -> {
         return executeEnable((ServerCommandSource)commandContext.getSource(), getPackContainer(commandContext, "name", true), List::add);
      }))).then(CommandManager.literal("first").executes((commandContext) -> {
         return executeEnable((ServerCommandSource)commandContext.getSource(), getPackContainer(commandContext, "name", true), (list, resourcePackProfile) -> {
            list.add(0, resourcePackProfile);
         });
      }))))).then(CommandManager.literal("disable").then(CommandManager.argument("name", StringArgumentType.string()).suggests(ENABLED_CONTAINERS_SUGGESTION_PROVIDER).executes((commandContext) -> {
         return executeDisable((ServerCommandSource)commandContext.getSource(), getPackContainer(commandContext, "name", false));
      })))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("list").executes((commandContext) -> {
         return executeList((ServerCommandSource)commandContext.getSource());
      })).then(CommandManager.literal("available").executes((commandContext) -> {
         return executeListAvailable((ServerCommandSource)commandContext.getSource());
      }))).then(CommandManager.literal("enabled").executes((commandContext) -> {
         return executeListEnabled((ServerCommandSource)commandContext.getSource());
      }))));
   }

   private static int executeEnable(ServerCommandSource source, ResourcePackProfile container, DatapackCommand.PackAdder packAdder) throws CommandSyntaxException {
      ResourcePackManager<ResourcePackProfile> resourcePackManager = source.getMinecraftServer().getDataPackManager();
      List<ResourcePackProfile> list = Lists.newArrayList(resourcePackManager.getEnabledProfiles());
      packAdder.apply(list, container);
      resourcePackManager.setEnabledProfiles(list);
      LevelProperties levelProperties = source.getMinecraftServer().getWorld(DimensionType.OVERWORLD).getLevelProperties();
      levelProperties.getEnabledDataPacks().clear();
      resourcePackManager.getEnabledProfiles().forEach((resourcePackProfile) -> {
         levelProperties.getEnabledDataPacks().add(resourcePackProfile.getName());
      });
      levelProperties.getDisabledDataPacks().remove(container.getName());
      source.sendFeedback(new TranslatableText("commands.datapack.enable.success", new Object[]{container.getInformationText(true)}), true);
      source.getMinecraftServer().reload();
      return resourcePackManager.getEnabledProfiles().size();
   }

   private static int executeDisable(ServerCommandSource source, ResourcePackProfile container) {
      ResourcePackManager<ResourcePackProfile> resourcePackManager = source.getMinecraftServer().getDataPackManager();
      List<ResourcePackProfile> list = Lists.newArrayList(resourcePackManager.getEnabledProfiles());
      list.remove(container);
      resourcePackManager.setEnabledProfiles(list);
      LevelProperties levelProperties = source.getMinecraftServer().getWorld(DimensionType.OVERWORLD).getLevelProperties();
      levelProperties.getEnabledDataPacks().clear();
      resourcePackManager.getEnabledProfiles().forEach((resourcePackProfile) -> {
         levelProperties.getEnabledDataPacks().add(resourcePackProfile.getName());
      });
      levelProperties.getDisabledDataPacks().add(container.getName());
      source.sendFeedback(new TranslatableText("commands.datapack.disable.success", new Object[]{container.getInformationText(true)}), true);
      source.getMinecraftServer().reload();
      return resourcePackManager.getEnabledProfiles().size();
   }

   private static int executeList(ServerCommandSource source) {
      return executeListEnabled(source) + executeListAvailable(source);
   }

   private static int executeListAvailable(ServerCommandSource source) {
      ResourcePackManager<ResourcePackProfile> resourcePackManager = source.getMinecraftServer().getDataPackManager();
      if (resourcePackManager.getDisabledProfiles().isEmpty()) {
         source.sendFeedback(new TranslatableText("commands.datapack.list.available.none", new Object[0]), false);
      } else {
         source.sendFeedback(new TranslatableText("commands.datapack.list.available.success", new Object[]{resourcePackManager.getDisabledProfiles().size(), Texts.join(resourcePackManager.getDisabledProfiles(), (resourcePackProfile) -> {
            return resourcePackProfile.getInformationText(false);
         })}), false);
      }

      return resourcePackManager.getDisabledProfiles().size();
   }

   private static int executeListEnabled(ServerCommandSource source) {
      ResourcePackManager<ResourcePackProfile> resourcePackManager = source.getMinecraftServer().getDataPackManager();
      if (resourcePackManager.getEnabledProfiles().isEmpty()) {
         source.sendFeedback(new TranslatableText("commands.datapack.list.enabled.none", new Object[0]), false);
      } else {
         source.sendFeedback(new TranslatableText("commands.datapack.list.enabled.success", new Object[]{resourcePackManager.getEnabledProfiles().size(), Texts.join(resourcePackManager.getEnabledProfiles(), (resourcePackProfile) -> {
            return resourcePackProfile.getInformationText(true);
         })}), false);
      }

      return resourcePackManager.getEnabledProfiles().size();
   }

   private static ResourcePackProfile getPackContainer(CommandContext<ServerCommandSource> context, String name, boolean enable) throws CommandSyntaxException {
      String string = StringArgumentType.getString(context, name);
      ResourcePackManager<ResourcePackProfile> resourcePackManager = ((ServerCommandSource)context.getSource()).getMinecraftServer().getDataPackManager();
      ResourcePackProfile resourcePackProfile = resourcePackManager.getProfile(string);
      if (resourcePackProfile == null) {
         throw UNKNOWN_DATAPACK_EXCEPTION.create(string);
      } else {
         boolean bl = resourcePackManager.getEnabledProfiles().contains(resourcePackProfile);
         if (enable && bl) {
            throw ALREADY_ENABLED_EXCEPTION.create(string);
         } else if (!enable && !bl) {
            throw ALREADY_DISABLED_EXCEPTION.create(string);
         } else {
            return resourcePackProfile;
         }
      }
   }

   interface PackAdder {
      void apply(List<ResourcePackProfile> list, ResourcePackProfile resourcePackProfile) throws CommandSyntaxException;
   }
}
