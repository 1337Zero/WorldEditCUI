package net.minecraft.data.server;

import java.util.function.Consumer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.ChangedDimensionCriterion;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.EnterBlockCriterion;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.advancement.criterion.LevitationCriterion;
import net.minecraft.advancement.criterion.LocationArrivalCriterion;
import net.minecraft.advancement.criterion.OnKilledCriterion;
import net.minecraft.advancement.criterion.SummonedEntityCriterion;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.DistancePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.feature.Feature;

public class EndTabAdvancementGenerator implements Consumer<Consumer<Advancement>> {
   public void accept(Consumer<Advancement> consumer) {
      Advancement advancement = Advancement.Task.create().display((ItemConvertible)Blocks.END_STONE, new TranslatableText("advancements.end.root.title", new Object[0]), new TranslatableText("advancements.end.root.description", new Object[0]), new Identifier("textures/gui/advancements/backgrounds/end.png"), AdvancementFrame.TASK, false, false, false).criterion("entered_end", (CriterionConditions)ChangedDimensionCriterion.Conditions.to(DimensionType.THE_END)).build(consumer, "end/root");
      Advancement advancement2 = Advancement.Task.create().parent(advancement).display((ItemConvertible)Blocks.DRAGON_HEAD, new TranslatableText("advancements.end.kill_dragon.title", new Object[0]), new TranslatableText("advancements.end.kill_dragon.description", new Object[0]), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("killed_dragon", (CriterionConditions)OnKilledCriterion.Conditions.createPlayerKilledEntity(EntityPredicate.Builder.create().type(EntityType.ENDER_DRAGON))).build(consumer, "end/kill_dragon");
      Advancement advancement3 = Advancement.Task.create().parent(advancement2).display((ItemConvertible)Items.ENDER_PEARL, new TranslatableText("advancements.end.enter_end_gateway.title", new Object[0]), new TranslatableText("advancements.end.enter_end_gateway.description", new Object[0]), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("entered_end_gateway", (CriterionConditions)EnterBlockCriterion.Conditions.block(Blocks.END_GATEWAY)).build(consumer, "end/enter_end_gateway");
      Advancement advancement4 = Advancement.Task.create().parent(advancement2).display((ItemConvertible)Items.END_CRYSTAL, new TranslatableText("advancements.end.respawn_dragon.title", new Object[0]), new TranslatableText("advancements.end.respawn_dragon.description", new Object[0]), (Identifier)null, AdvancementFrame.GOAL, true, true, false).criterion("summoned_dragon", (CriterionConditions)SummonedEntityCriterion.Conditions.create(EntityPredicate.Builder.create().type(EntityType.ENDER_DRAGON))).build(consumer, "end/respawn_dragon");
      Advancement advancement5 = Advancement.Task.create().parent(advancement3).display((ItemConvertible)Blocks.PURPUR_BLOCK, new TranslatableText("advancements.end.find_end_city.title", new Object[0]), new TranslatableText("advancements.end.find_end_city.description", new Object[0]), (Identifier)null, AdvancementFrame.TASK, true, true, false).criterion("in_city", (CriterionConditions)LocationArrivalCriterion.Conditions.create(LocationPredicate.feature(Feature.END_CITY))).build(consumer, "end/find_end_city");
      Advancement advancement6 = Advancement.Task.create().parent(advancement2).display((ItemConvertible)Items.DRAGON_BREATH, new TranslatableText("advancements.end.dragon_breath.title", new Object[0]), new TranslatableText("advancements.end.dragon_breath.description", new Object[0]), (Identifier)null, AdvancementFrame.GOAL, true, true, false).criterion("dragon_breath", (CriterionConditions)InventoryChangedCriterion.Conditions.items(Items.DRAGON_BREATH)).build(consumer, "end/dragon_breath");
      Advancement advancement7 = Advancement.Task.create().parent(advancement5).display((ItemConvertible)Items.SHULKER_SHELL, new TranslatableText("advancements.end.levitate.title", new Object[0]), new TranslatableText("advancements.end.levitate.description", new Object[0]), (Identifier)null, AdvancementFrame.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(50)).criterion("levitated", (CriterionConditions)LevitationCriterion.Conditions.create(DistancePredicate.y(NumberRange.FloatRange.atLeast(50.0F)))).build(consumer, "end/levitate");
      Advancement advancement8 = Advancement.Task.create().parent(advancement5).display((ItemConvertible)Items.ELYTRA, new TranslatableText("advancements.end.elytra.title", new Object[0]), new TranslatableText("advancements.end.elytra.description", new Object[0]), (Identifier)null, AdvancementFrame.GOAL, true, true, false).criterion("elytra", (CriterionConditions)InventoryChangedCriterion.Conditions.items(Items.ELYTRA)).build(consumer, "end/elytra");
      Advancement advancement9 = Advancement.Task.create().parent(advancement2).display((ItemConvertible)Blocks.DRAGON_EGG, new TranslatableText("advancements.end.dragon_egg.title", new Object[0]), new TranslatableText("advancements.end.dragon_egg.description", new Object[0]), (Identifier)null, AdvancementFrame.GOAL, true, true, false).criterion("dragon_egg", (CriterionConditions)InventoryChangedCriterion.Conditions.items(Blocks.DRAGON_EGG)).build(consumer, "end/dragon_egg");
   }
}
