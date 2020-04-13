package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.command.CommandException;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.command.arguments.IdentifierArgumentType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

public class AdvancementCommand {
   private static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (commandContext, suggestionsBuilder) -> {
      Collection<Advancement> collection = ((ServerCommandSource)commandContext.getSource()).getMinecraftServer().getAdvancementLoader().getAdvancements();
      return CommandSource.suggestIdentifiers(collection.stream().map(Advancement::getId), suggestionsBuilder);
   };

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("advancement").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(CommandManager.literal("grant").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.literal("only").then(((RequiredArgumentBuilder)CommandManager.argument("advancement", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes((commandContext) -> {
         return executeAdvancement((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), AdvancementCommand.Operation.GRANT, select(IdentifierArgumentType.getAdvancementArgument(commandContext, "advancement"), AdvancementCommand.Selection.ONLY));
      })).then(CommandManager.argument("criterion", StringArgumentType.greedyString()).suggests((commandContext, suggestionsBuilder) -> {
         return CommandSource.suggestMatching((Iterable)IdentifierArgumentType.getAdvancementArgument(commandContext, "advancement").getCriteria().keySet(), suggestionsBuilder);
      }).executes((commandContext) -> {
         return executeCriterion((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), AdvancementCommand.Operation.GRANT, IdentifierArgumentType.getAdvancementArgument(commandContext, "advancement"), StringArgumentType.getString(commandContext, "criterion"));
      }))))).then(CommandManager.literal("from").then(CommandManager.argument("advancement", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes((commandContext) -> {
         return executeAdvancement((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), AdvancementCommand.Operation.GRANT, select(IdentifierArgumentType.getAdvancementArgument(commandContext, "advancement"), AdvancementCommand.Selection.FROM));
      })))).then(CommandManager.literal("until").then(CommandManager.argument("advancement", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes((commandContext) -> {
         return executeAdvancement((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), AdvancementCommand.Operation.GRANT, select(IdentifierArgumentType.getAdvancementArgument(commandContext, "advancement"), AdvancementCommand.Selection.UNTIL));
      })))).then(CommandManager.literal("through").then(CommandManager.argument("advancement", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes((commandContext) -> {
         return executeAdvancement((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), AdvancementCommand.Operation.GRANT, select(IdentifierArgumentType.getAdvancementArgument(commandContext, "advancement"), AdvancementCommand.Selection.THROUGH));
      })))).then(CommandManager.literal("everything").executes((commandContext) -> {
         return executeAdvancement((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), AdvancementCommand.Operation.GRANT, ((ServerCommandSource)commandContext.getSource()).getMinecraftServer().getAdvancementLoader().getAdvancements());
      }))))).then(CommandManager.literal("revoke").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.literal("only").then(((RequiredArgumentBuilder)CommandManager.argument("advancement", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes((commandContext) -> {
         return executeAdvancement((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), AdvancementCommand.Operation.REVOKE, select(IdentifierArgumentType.getAdvancementArgument(commandContext, "advancement"), AdvancementCommand.Selection.ONLY));
      })).then(CommandManager.argument("criterion", StringArgumentType.greedyString()).suggests((commandContext, suggestionsBuilder) -> {
         return CommandSource.suggestMatching((Iterable)IdentifierArgumentType.getAdvancementArgument(commandContext, "advancement").getCriteria().keySet(), suggestionsBuilder);
      }).executes((commandContext) -> {
         return executeCriterion((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), AdvancementCommand.Operation.REVOKE, IdentifierArgumentType.getAdvancementArgument(commandContext, "advancement"), StringArgumentType.getString(commandContext, "criterion"));
      }))))).then(CommandManager.literal("from").then(CommandManager.argument("advancement", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes((commandContext) -> {
         return executeAdvancement((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), AdvancementCommand.Operation.REVOKE, select(IdentifierArgumentType.getAdvancementArgument(commandContext, "advancement"), AdvancementCommand.Selection.FROM));
      })))).then(CommandManager.literal("until").then(CommandManager.argument("advancement", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes((commandContext) -> {
         return executeAdvancement((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), AdvancementCommand.Operation.REVOKE, select(IdentifierArgumentType.getAdvancementArgument(commandContext, "advancement"), AdvancementCommand.Selection.UNTIL));
      })))).then(CommandManager.literal("through").then(CommandManager.argument("advancement", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes((commandContext) -> {
         return executeAdvancement((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), AdvancementCommand.Operation.REVOKE, select(IdentifierArgumentType.getAdvancementArgument(commandContext, "advancement"), AdvancementCommand.Selection.THROUGH));
      })))).then(CommandManager.literal("everything").executes((commandContext) -> {
         return executeAdvancement((ServerCommandSource)commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), AdvancementCommand.Operation.REVOKE, ((ServerCommandSource)commandContext.getSource()).getMinecraftServer().getAdvancementLoader().getAdvancements());
      })))));
   }

   private static int executeAdvancement(ServerCommandSource source, Collection<ServerPlayerEntity> targets, AdvancementCommand.Operation operation, Collection<Advancement> selection) {
      int i = 0;

      ServerPlayerEntity serverPlayerEntity;
      for(Iterator var5 = targets.iterator(); var5.hasNext(); i += operation.processAll(serverPlayerEntity, selection)) {
         serverPlayerEntity = (ServerPlayerEntity)var5.next();
      }

      if (i == 0) {
         if (selection.size() == 1) {
            if (targets.size() == 1) {
               throw new CommandException(new TranslatableText(operation.getCommandPrefix() + ".one.to.one.failure", new Object[]{((Advancement)selection.iterator().next()).toHoverableText(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()}));
            } else {
               throw new CommandException(new TranslatableText(operation.getCommandPrefix() + ".one.to.many.failure", new Object[]{((Advancement)selection.iterator().next()).toHoverableText(), targets.size()}));
            }
         } else if (targets.size() == 1) {
            throw new CommandException(new TranslatableText(operation.getCommandPrefix() + ".many.to.one.failure", new Object[]{selection.size(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()}));
         } else {
            throw new CommandException(new TranslatableText(operation.getCommandPrefix() + ".many.to.many.failure", new Object[]{selection.size(), targets.size()}));
         }
      } else {
         if (selection.size() == 1) {
            if (targets.size() == 1) {
               source.sendFeedback(new TranslatableText(operation.getCommandPrefix() + ".one.to.one.success", new Object[]{((Advancement)selection.iterator().next()).toHoverableText(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()}), true);
            } else {
               source.sendFeedback(new TranslatableText(operation.getCommandPrefix() + ".one.to.many.success", new Object[]{((Advancement)selection.iterator().next()).toHoverableText(), targets.size()}), true);
            }
         } else if (targets.size() == 1) {
            source.sendFeedback(new TranslatableText(operation.getCommandPrefix() + ".many.to.one.success", new Object[]{selection.size(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()}), true);
         } else {
            source.sendFeedback(new TranslatableText(operation.getCommandPrefix() + ".many.to.many.success", new Object[]{selection.size(), targets.size()}), true);
         }

         return i;
      }
   }

   private static int executeCriterion(ServerCommandSource source, Collection<ServerPlayerEntity> targets, AdvancementCommand.Operation operation, Advancement advancement, String criterion) {
      int i = 0;
      if (!advancement.getCriteria().containsKey(criterion)) {
         throw new CommandException(new TranslatableText("commands.advancement.criterionNotFound", new Object[]{advancement.toHoverableText(), criterion}));
      } else {
         Iterator var6 = targets.iterator();

         while(var6.hasNext()) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var6.next();
            if (operation.processEachCriterion(serverPlayerEntity, advancement, criterion)) {
               ++i;
            }
         }

         if (i == 0) {
            if (targets.size() == 1) {
               throw new CommandException(new TranslatableText(operation.getCommandPrefix() + ".criterion.to.one.failure", new Object[]{criterion, advancement.toHoverableText(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()}));
            } else {
               throw new CommandException(new TranslatableText(operation.getCommandPrefix() + ".criterion.to.many.failure", new Object[]{criterion, advancement.toHoverableText(), targets.size()}));
            }
         } else {
            if (targets.size() == 1) {
               source.sendFeedback(new TranslatableText(operation.getCommandPrefix() + ".criterion.to.one.success", new Object[]{criterion, advancement.toHoverableText(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()}), true);
            } else {
               source.sendFeedback(new TranslatableText(operation.getCommandPrefix() + ".criterion.to.many.success", new Object[]{criterion, advancement.toHoverableText(), targets.size()}), true);
            }

            return i;
         }
      }
   }

   private static List<Advancement> select(Advancement advancement, AdvancementCommand.Selection selection) {
      List<Advancement> list = Lists.newArrayList();
      if (selection.before) {
         for(Advancement advancement2 = advancement.getParent(); advancement2 != null; advancement2 = advancement2.getParent()) {
            list.add(advancement2);
         }
      }

      list.add(advancement);
      if (selection.after) {
         addChildrenRecursivelyToList(advancement, list);
      }

      return list;
   }

   private static void addChildrenRecursivelyToList(Advancement parent, List<Advancement> childList) {
      Iterator var2 = parent.getChildren().iterator();

      while(var2.hasNext()) {
         Advancement advancement = (Advancement)var2.next();
         childList.add(advancement);
         addChildrenRecursivelyToList(advancement, childList);
      }

   }

   static enum Selection {
      ONLY(false, false),
      THROUGH(true, true),
      FROM(false, true),
      UNTIL(true, false),
      EVERYTHING(true, true);

      private final boolean before;
      private final boolean after;

      private Selection(boolean bl, boolean bl2) {
         this.before = bl;
         this.after = bl2;
      }
   }

   static enum Operation {
      GRANT("grant") {
         protected boolean processEach(ServerPlayerEntity serverPlayerEntity, Advancement advancement) {
            AdvancementProgress advancementProgress = serverPlayerEntity.getAdvancementTracker().getProgress(advancement);
            if (advancementProgress.isDone()) {
               return false;
            } else {
               Iterator var4 = advancementProgress.getUnobtainedCriteria().iterator();

               while(var4.hasNext()) {
                  String string = (String)var4.next();
                  serverPlayerEntity.getAdvancementTracker().grantCriterion(advancement, string);
               }

               return true;
            }
         }

         protected boolean processEachCriterion(ServerPlayerEntity serverPlayerEntity, Advancement advancement, String criterion) {
            return serverPlayerEntity.getAdvancementTracker().grantCriterion(advancement, criterion);
         }
      },
      REVOKE("revoke") {
         protected boolean processEach(ServerPlayerEntity serverPlayerEntity, Advancement advancement) {
            AdvancementProgress advancementProgress = serverPlayerEntity.getAdvancementTracker().getProgress(advancement);
            if (!advancementProgress.isAnyObtained()) {
               return false;
            } else {
               Iterator var4 = advancementProgress.getObtainedCriteria().iterator();

               while(var4.hasNext()) {
                  String string = (String)var4.next();
                  serverPlayerEntity.getAdvancementTracker().revokeCriterion(advancement, string);
               }

               return true;
            }
         }

         protected boolean processEachCriterion(ServerPlayerEntity serverPlayerEntity, Advancement advancement, String criterion) {
            return serverPlayerEntity.getAdvancementTracker().revokeCriterion(advancement, criterion);
         }
      };

      private final String commandPrefix;

      private Operation(String string2) {
         this.commandPrefix = "commands.advancement." + string2;
      }

      public int processAll(ServerPlayerEntity serverPlayerEntity, Iterable<Advancement> iterable) {
         int i = 0;
         Iterator var4 = iterable.iterator();

         while(var4.hasNext()) {
            Advancement advancement = (Advancement)var4.next();
            if (this.processEach(serverPlayerEntity, advancement)) {
               ++i;
            }
         }

         return i;
      }

      protected abstract boolean processEach(ServerPlayerEntity serverPlayerEntity, Advancement advancement);

      protected abstract boolean processEachCriterion(ServerPlayerEntity serverPlayerEntity, Advancement advancement, String criterion);

      protected String getCommandPrefix() {
         return this.commandPrefix;
      }
   }
}
