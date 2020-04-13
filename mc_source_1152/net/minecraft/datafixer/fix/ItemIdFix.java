package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.datafixer.TypeReferences;

public class ItemIdFix extends DataFix {
   private static final Int2ObjectMap<String> NUMERICAL_ID_TO_STRING_ID_MAP = (Int2ObjectMap)DataFixUtils.make(new Int2ObjectOpenHashMap(), (int2ObjectOpenHashMap) -> {
      int2ObjectOpenHashMap.put(1, "minecraft:stone");
      int2ObjectOpenHashMap.put(2, "minecraft:grass");
      int2ObjectOpenHashMap.put(3, "minecraft:dirt");
      int2ObjectOpenHashMap.put(4, "minecraft:cobblestone");
      int2ObjectOpenHashMap.put(5, "minecraft:planks");
      int2ObjectOpenHashMap.put(6, "minecraft:sapling");
      int2ObjectOpenHashMap.put(7, "minecraft:bedrock");
      int2ObjectOpenHashMap.put(8, "minecraft:flowing_water");
      int2ObjectOpenHashMap.put(9, "minecraft:water");
      int2ObjectOpenHashMap.put(10, "minecraft:flowing_lava");
      int2ObjectOpenHashMap.put(11, "minecraft:lava");
      int2ObjectOpenHashMap.put(12, "minecraft:sand");
      int2ObjectOpenHashMap.put(13, "minecraft:gravel");
      int2ObjectOpenHashMap.put(14, "minecraft:gold_ore");
      int2ObjectOpenHashMap.put(15, "minecraft:iron_ore");
      int2ObjectOpenHashMap.put(16, "minecraft:coal_ore");
      int2ObjectOpenHashMap.put(17, "minecraft:log");
      int2ObjectOpenHashMap.put(18, "minecraft:leaves");
      int2ObjectOpenHashMap.put(19, "minecraft:sponge");
      int2ObjectOpenHashMap.put(20, "minecraft:glass");
      int2ObjectOpenHashMap.put(21, "minecraft:lapis_ore");
      int2ObjectOpenHashMap.put(22, "minecraft:lapis_block");
      int2ObjectOpenHashMap.put(23, "minecraft:dispenser");
      int2ObjectOpenHashMap.put(24, "minecraft:sandstone");
      int2ObjectOpenHashMap.put(25, "minecraft:noteblock");
      int2ObjectOpenHashMap.put(27, "minecraft:golden_rail");
      int2ObjectOpenHashMap.put(28, "minecraft:detector_rail");
      int2ObjectOpenHashMap.put(29, "minecraft:sticky_piston");
      int2ObjectOpenHashMap.put(30, "minecraft:web");
      int2ObjectOpenHashMap.put(31, "minecraft:tallgrass");
      int2ObjectOpenHashMap.put(32, "minecraft:deadbush");
      int2ObjectOpenHashMap.put(33, "minecraft:piston");
      int2ObjectOpenHashMap.put(35, "minecraft:wool");
      int2ObjectOpenHashMap.put(37, "minecraft:yellow_flower");
      int2ObjectOpenHashMap.put(38, "minecraft:red_flower");
      int2ObjectOpenHashMap.put(39, "minecraft:brown_mushroom");
      int2ObjectOpenHashMap.put(40, "minecraft:red_mushroom");
      int2ObjectOpenHashMap.put(41, "minecraft:gold_block");
      int2ObjectOpenHashMap.put(42, "minecraft:iron_block");
      int2ObjectOpenHashMap.put(43, "minecraft:double_stone_slab");
      int2ObjectOpenHashMap.put(44, "minecraft:stone_slab");
      int2ObjectOpenHashMap.put(45, "minecraft:brick_block");
      int2ObjectOpenHashMap.put(46, "minecraft:tnt");
      int2ObjectOpenHashMap.put(47, "minecraft:bookshelf");
      int2ObjectOpenHashMap.put(48, "minecraft:mossy_cobblestone");
      int2ObjectOpenHashMap.put(49, "minecraft:obsidian");
      int2ObjectOpenHashMap.put(50, "minecraft:torch");
      int2ObjectOpenHashMap.put(51, "minecraft:fire");
      int2ObjectOpenHashMap.put(52, "minecraft:mob_spawner");
      int2ObjectOpenHashMap.put(53, "minecraft:oak_stairs");
      int2ObjectOpenHashMap.put(54, "minecraft:chest");
      int2ObjectOpenHashMap.put(56, "minecraft:diamond_ore");
      int2ObjectOpenHashMap.put(57, "minecraft:diamond_block");
      int2ObjectOpenHashMap.put(58, "minecraft:crafting_table");
      int2ObjectOpenHashMap.put(60, "minecraft:farmland");
      int2ObjectOpenHashMap.put(61, "minecraft:furnace");
      int2ObjectOpenHashMap.put(62, "minecraft:lit_furnace");
      int2ObjectOpenHashMap.put(65, "minecraft:ladder");
      int2ObjectOpenHashMap.put(66, "minecraft:rail");
      int2ObjectOpenHashMap.put(67, "minecraft:stone_stairs");
      int2ObjectOpenHashMap.put(69, "minecraft:lever");
      int2ObjectOpenHashMap.put(70, "minecraft:stone_pressure_plate");
      int2ObjectOpenHashMap.put(72, "minecraft:wooden_pressure_plate");
      int2ObjectOpenHashMap.put(73, "minecraft:redstone_ore");
      int2ObjectOpenHashMap.put(76, "minecraft:redstone_torch");
      int2ObjectOpenHashMap.put(77, "minecraft:stone_button");
      int2ObjectOpenHashMap.put(78, "minecraft:snow_layer");
      int2ObjectOpenHashMap.put(79, "minecraft:ice");
      int2ObjectOpenHashMap.put(80, "minecraft:snow");
      int2ObjectOpenHashMap.put(81, "minecraft:cactus");
      int2ObjectOpenHashMap.put(82, "minecraft:clay");
      int2ObjectOpenHashMap.put(84, "minecraft:jukebox");
      int2ObjectOpenHashMap.put(85, "minecraft:fence");
      int2ObjectOpenHashMap.put(86, "minecraft:pumpkin");
      int2ObjectOpenHashMap.put(87, "minecraft:netherrack");
      int2ObjectOpenHashMap.put(88, "minecraft:soul_sand");
      int2ObjectOpenHashMap.put(89, "minecraft:glowstone");
      int2ObjectOpenHashMap.put(90, "minecraft:portal");
      int2ObjectOpenHashMap.put(91, "minecraft:lit_pumpkin");
      int2ObjectOpenHashMap.put(95, "minecraft:stained_glass");
      int2ObjectOpenHashMap.put(96, "minecraft:trapdoor");
      int2ObjectOpenHashMap.put(97, "minecraft:monster_egg");
      int2ObjectOpenHashMap.put(98, "minecraft:stonebrick");
      int2ObjectOpenHashMap.put(99, "minecraft:brown_mushroom_block");
      int2ObjectOpenHashMap.put(100, "minecraft:red_mushroom_block");
      int2ObjectOpenHashMap.put(101, "minecraft:iron_bars");
      int2ObjectOpenHashMap.put(102, "minecraft:glass_pane");
      int2ObjectOpenHashMap.put(103, "minecraft:melon_block");
      int2ObjectOpenHashMap.put(106, "minecraft:vine");
      int2ObjectOpenHashMap.put(107, "minecraft:fence_gate");
      int2ObjectOpenHashMap.put(108, "minecraft:brick_stairs");
      int2ObjectOpenHashMap.put(109, "minecraft:stone_brick_stairs");
      int2ObjectOpenHashMap.put(110, "minecraft:mycelium");
      int2ObjectOpenHashMap.put(111, "minecraft:waterlily");
      int2ObjectOpenHashMap.put(112, "minecraft:nether_brick");
      int2ObjectOpenHashMap.put(113, "minecraft:nether_brick_fence");
      int2ObjectOpenHashMap.put(114, "minecraft:nether_brick_stairs");
      int2ObjectOpenHashMap.put(116, "minecraft:enchanting_table");
      int2ObjectOpenHashMap.put(119, "minecraft:end_portal");
      int2ObjectOpenHashMap.put(120, "minecraft:end_portal_frame");
      int2ObjectOpenHashMap.put(121, "minecraft:end_stone");
      int2ObjectOpenHashMap.put(122, "minecraft:dragon_egg");
      int2ObjectOpenHashMap.put(123, "minecraft:redstone_lamp");
      int2ObjectOpenHashMap.put(125, "minecraft:double_wooden_slab");
      int2ObjectOpenHashMap.put(126, "minecraft:wooden_slab");
      int2ObjectOpenHashMap.put(127, "minecraft:cocoa");
      int2ObjectOpenHashMap.put(128, "minecraft:sandstone_stairs");
      int2ObjectOpenHashMap.put(129, "minecraft:emerald_ore");
      int2ObjectOpenHashMap.put(130, "minecraft:ender_chest");
      int2ObjectOpenHashMap.put(131, "minecraft:tripwire_hook");
      int2ObjectOpenHashMap.put(133, "minecraft:emerald_block");
      int2ObjectOpenHashMap.put(134, "minecraft:spruce_stairs");
      int2ObjectOpenHashMap.put(135, "minecraft:birch_stairs");
      int2ObjectOpenHashMap.put(136, "minecraft:jungle_stairs");
      int2ObjectOpenHashMap.put(137, "minecraft:command_block");
      int2ObjectOpenHashMap.put(138, "minecraft:beacon");
      int2ObjectOpenHashMap.put(139, "minecraft:cobblestone_wall");
      int2ObjectOpenHashMap.put(141, "minecraft:carrots");
      int2ObjectOpenHashMap.put(142, "minecraft:potatoes");
      int2ObjectOpenHashMap.put(143, "minecraft:wooden_button");
      int2ObjectOpenHashMap.put(145, "minecraft:anvil");
      int2ObjectOpenHashMap.put(146, "minecraft:trapped_chest");
      int2ObjectOpenHashMap.put(147, "minecraft:light_weighted_pressure_plate");
      int2ObjectOpenHashMap.put(148, "minecraft:heavy_weighted_pressure_plate");
      int2ObjectOpenHashMap.put(151, "minecraft:daylight_detector");
      int2ObjectOpenHashMap.put(152, "minecraft:redstone_block");
      int2ObjectOpenHashMap.put(153, "minecraft:quartz_ore");
      int2ObjectOpenHashMap.put(154, "minecraft:hopper");
      int2ObjectOpenHashMap.put(155, "minecraft:quartz_block");
      int2ObjectOpenHashMap.put(156, "minecraft:quartz_stairs");
      int2ObjectOpenHashMap.put(157, "minecraft:activator_rail");
      int2ObjectOpenHashMap.put(158, "minecraft:dropper");
      int2ObjectOpenHashMap.put(159, "minecraft:stained_hardened_clay");
      int2ObjectOpenHashMap.put(160, "minecraft:stained_glass_pane");
      int2ObjectOpenHashMap.put(161, "minecraft:leaves2");
      int2ObjectOpenHashMap.put(162, "minecraft:log2");
      int2ObjectOpenHashMap.put(163, "minecraft:acacia_stairs");
      int2ObjectOpenHashMap.put(164, "minecraft:dark_oak_stairs");
      int2ObjectOpenHashMap.put(170, "minecraft:hay_block");
      int2ObjectOpenHashMap.put(171, "minecraft:carpet");
      int2ObjectOpenHashMap.put(172, "minecraft:hardened_clay");
      int2ObjectOpenHashMap.put(173, "minecraft:coal_block");
      int2ObjectOpenHashMap.put(174, "minecraft:packed_ice");
      int2ObjectOpenHashMap.put(175, "minecraft:double_plant");
      int2ObjectOpenHashMap.put(256, "minecraft:iron_shovel");
      int2ObjectOpenHashMap.put(257, "minecraft:iron_pickaxe");
      int2ObjectOpenHashMap.put(258, "minecraft:iron_axe");
      int2ObjectOpenHashMap.put(259, "minecraft:flint_and_steel");
      int2ObjectOpenHashMap.put(260, "minecraft:apple");
      int2ObjectOpenHashMap.put(261, "minecraft:bow");
      int2ObjectOpenHashMap.put(262, "minecraft:arrow");
      int2ObjectOpenHashMap.put(263, "minecraft:coal");
      int2ObjectOpenHashMap.put(264, "minecraft:diamond");
      int2ObjectOpenHashMap.put(265, "minecraft:iron_ingot");
      int2ObjectOpenHashMap.put(266, "minecraft:gold_ingot");
      int2ObjectOpenHashMap.put(267, "minecraft:iron_sword");
      int2ObjectOpenHashMap.put(268, "minecraft:wooden_sword");
      int2ObjectOpenHashMap.put(269, "minecraft:wooden_shovel");
      int2ObjectOpenHashMap.put(270, "minecraft:wooden_pickaxe");
      int2ObjectOpenHashMap.put(271, "minecraft:wooden_axe");
      int2ObjectOpenHashMap.put(272, "minecraft:stone_sword");
      int2ObjectOpenHashMap.put(273, "minecraft:stone_shovel");
      int2ObjectOpenHashMap.put(274, "minecraft:stone_pickaxe");
      int2ObjectOpenHashMap.put(275, "minecraft:stone_axe");
      int2ObjectOpenHashMap.put(276, "minecraft:diamond_sword");
      int2ObjectOpenHashMap.put(277, "minecraft:diamond_shovel");
      int2ObjectOpenHashMap.put(278, "minecraft:diamond_pickaxe");
      int2ObjectOpenHashMap.put(279, "minecraft:diamond_axe");
      int2ObjectOpenHashMap.put(280, "minecraft:stick");
      int2ObjectOpenHashMap.put(281, "minecraft:bowl");
      int2ObjectOpenHashMap.put(282, "minecraft:mushroom_stew");
      int2ObjectOpenHashMap.put(283, "minecraft:golden_sword");
      int2ObjectOpenHashMap.put(284, "minecraft:golden_shovel");
      int2ObjectOpenHashMap.put(285, "minecraft:golden_pickaxe");
      int2ObjectOpenHashMap.put(286, "minecraft:golden_axe");
      int2ObjectOpenHashMap.put(287, "minecraft:string");
      int2ObjectOpenHashMap.put(288, "minecraft:feather");
      int2ObjectOpenHashMap.put(289, "minecraft:gunpowder");
      int2ObjectOpenHashMap.put(290, "minecraft:wooden_hoe");
      int2ObjectOpenHashMap.put(291, "minecraft:stone_hoe");
      int2ObjectOpenHashMap.put(292, "minecraft:iron_hoe");
      int2ObjectOpenHashMap.put(293, "minecraft:diamond_hoe");
      int2ObjectOpenHashMap.put(294, "minecraft:golden_hoe");
      int2ObjectOpenHashMap.put(295, "minecraft:wheat_seeds");
      int2ObjectOpenHashMap.put(296, "minecraft:wheat");
      int2ObjectOpenHashMap.put(297, "minecraft:bread");
      int2ObjectOpenHashMap.put(298, "minecraft:leather_helmet");
      int2ObjectOpenHashMap.put(299, "minecraft:leather_chestplate");
      int2ObjectOpenHashMap.put(300, "minecraft:leather_leggings");
      int2ObjectOpenHashMap.put(301, "minecraft:leather_boots");
      int2ObjectOpenHashMap.put(302, "minecraft:chainmail_helmet");
      int2ObjectOpenHashMap.put(303, "minecraft:chainmail_chestplate");
      int2ObjectOpenHashMap.put(304, "minecraft:chainmail_leggings");
      int2ObjectOpenHashMap.put(305, "minecraft:chainmail_boots");
      int2ObjectOpenHashMap.put(306, "minecraft:iron_helmet");
      int2ObjectOpenHashMap.put(307, "minecraft:iron_chestplate");
      int2ObjectOpenHashMap.put(308, "minecraft:iron_leggings");
      int2ObjectOpenHashMap.put(309, "minecraft:iron_boots");
      int2ObjectOpenHashMap.put(310, "minecraft:diamond_helmet");
      int2ObjectOpenHashMap.put(311, "minecraft:diamond_chestplate");
      int2ObjectOpenHashMap.put(312, "minecraft:diamond_leggings");
      int2ObjectOpenHashMap.put(313, "minecraft:diamond_boots");
      int2ObjectOpenHashMap.put(314, "minecraft:golden_helmet");
      int2ObjectOpenHashMap.put(315, "minecraft:golden_chestplate");
      int2ObjectOpenHashMap.put(316, "minecraft:golden_leggings");
      int2ObjectOpenHashMap.put(317, "minecraft:golden_boots");
      int2ObjectOpenHashMap.put(318, "minecraft:flint");
      int2ObjectOpenHashMap.put(319, "minecraft:porkchop");
      int2ObjectOpenHashMap.put(320, "minecraft:cooked_porkchop");
      int2ObjectOpenHashMap.put(321, "minecraft:painting");
      int2ObjectOpenHashMap.put(322, "minecraft:golden_apple");
      int2ObjectOpenHashMap.put(323, "minecraft:sign");
      int2ObjectOpenHashMap.put(324, "minecraft:wooden_door");
      int2ObjectOpenHashMap.put(325, "minecraft:bucket");
      int2ObjectOpenHashMap.put(326, "minecraft:water_bucket");
      int2ObjectOpenHashMap.put(327, "minecraft:lava_bucket");
      int2ObjectOpenHashMap.put(328, "minecraft:minecart");
      int2ObjectOpenHashMap.put(329, "minecraft:saddle");
      int2ObjectOpenHashMap.put(330, "minecraft:iron_door");
      int2ObjectOpenHashMap.put(331, "minecraft:redstone");
      int2ObjectOpenHashMap.put(332, "minecraft:snowball");
      int2ObjectOpenHashMap.put(333, "minecraft:boat");
      int2ObjectOpenHashMap.put(334, "minecraft:leather");
      int2ObjectOpenHashMap.put(335, "minecraft:milk_bucket");
      int2ObjectOpenHashMap.put(336, "minecraft:brick");
      int2ObjectOpenHashMap.put(337, "minecraft:clay_ball");
      int2ObjectOpenHashMap.put(338, "minecraft:reeds");
      int2ObjectOpenHashMap.put(339, "minecraft:paper");
      int2ObjectOpenHashMap.put(340, "minecraft:book");
      int2ObjectOpenHashMap.put(341, "minecraft:slime_ball");
      int2ObjectOpenHashMap.put(342, "minecraft:chest_minecart");
      int2ObjectOpenHashMap.put(343, "minecraft:furnace_minecart");
      int2ObjectOpenHashMap.put(344, "minecraft:egg");
      int2ObjectOpenHashMap.put(345, "minecraft:compass");
      int2ObjectOpenHashMap.put(346, "minecraft:fishing_rod");
      int2ObjectOpenHashMap.put(347, "minecraft:clock");
      int2ObjectOpenHashMap.put(348, "minecraft:glowstone_dust");
      int2ObjectOpenHashMap.put(349, "minecraft:fish");
      int2ObjectOpenHashMap.put(350, "minecraft:cooked_fished");
      int2ObjectOpenHashMap.put(351, "minecraft:dye");
      int2ObjectOpenHashMap.put(352, "minecraft:bone");
      int2ObjectOpenHashMap.put(353, "minecraft:sugar");
      int2ObjectOpenHashMap.put(354, "minecraft:cake");
      int2ObjectOpenHashMap.put(355, "minecraft:bed");
      int2ObjectOpenHashMap.put(356, "minecraft:repeater");
      int2ObjectOpenHashMap.put(357, "minecraft:cookie");
      int2ObjectOpenHashMap.put(358, "minecraft:filled_map");
      int2ObjectOpenHashMap.put(359, "minecraft:shears");
      int2ObjectOpenHashMap.put(360, "minecraft:melon");
      int2ObjectOpenHashMap.put(361, "minecraft:pumpkin_seeds");
      int2ObjectOpenHashMap.put(362, "minecraft:melon_seeds");
      int2ObjectOpenHashMap.put(363, "minecraft:beef");
      int2ObjectOpenHashMap.put(364, "minecraft:cooked_beef");
      int2ObjectOpenHashMap.put(365, "minecraft:chicken");
      int2ObjectOpenHashMap.put(366, "minecraft:cooked_chicken");
      int2ObjectOpenHashMap.put(367, "minecraft:rotten_flesh");
      int2ObjectOpenHashMap.put(368, "minecraft:ender_pearl");
      int2ObjectOpenHashMap.put(369, "minecraft:blaze_rod");
      int2ObjectOpenHashMap.put(370, "minecraft:ghast_tear");
      int2ObjectOpenHashMap.put(371, "minecraft:gold_nugget");
      int2ObjectOpenHashMap.put(372, "minecraft:nether_wart");
      int2ObjectOpenHashMap.put(373, "minecraft:potion");
      int2ObjectOpenHashMap.put(374, "minecraft:glass_bottle");
      int2ObjectOpenHashMap.put(375, "minecraft:spider_eye");
      int2ObjectOpenHashMap.put(376, "minecraft:fermented_spider_eye");
      int2ObjectOpenHashMap.put(377, "minecraft:blaze_powder");
      int2ObjectOpenHashMap.put(378, "minecraft:magma_cream");
      int2ObjectOpenHashMap.put(379, "minecraft:brewing_stand");
      int2ObjectOpenHashMap.put(380, "minecraft:cauldron");
      int2ObjectOpenHashMap.put(381, "minecraft:ender_eye");
      int2ObjectOpenHashMap.put(382, "minecraft:speckled_melon");
      int2ObjectOpenHashMap.put(383, "minecraft:spawn_egg");
      int2ObjectOpenHashMap.put(384, "minecraft:experience_bottle");
      int2ObjectOpenHashMap.put(385, "minecraft:fire_charge");
      int2ObjectOpenHashMap.put(386, "minecraft:writable_book");
      int2ObjectOpenHashMap.put(387, "minecraft:written_book");
      int2ObjectOpenHashMap.put(388, "minecraft:emerald");
      int2ObjectOpenHashMap.put(389, "minecraft:item_frame");
      int2ObjectOpenHashMap.put(390, "minecraft:flower_pot");
      int2ObjectOpenHashMap.put(391, "minecraft:carrot");
      int2ObjectOpenHashMap.put(392, "minecraft:potato");
      int2ObjectOpenHashMap.put(393, "minecraft:baked_potato");
      int2ObjectOpenHashMap.put(394, "minecraft:poisonous_potato");
      int2ObjectOpenHashMap.put(395, "minecraft:map");
      int2ObjectOpenHashMap.put(396, "minecraft:golden_carrot");
      int2ObjectOpenHashMap.put(397, "minecraft:skull");
      int2ObjectOpenHashMap.put(398, "minecraft:carrot_on_a_stick");
      int2ObjectOpenHashMap.put(399, "minecraft:nether_star");
      int2ObjectOpenHashMap.put(400, "minecraft:pumpkin_pie");
      int2ObjectOpenHashMap.put(401, "minecraft:fireworks");
      int2ObjectOpenHashMap.put(402, "minecraft:firework_charge");
      int2ObjectOpenHashMap.put(403, "minecraft:enchanted_book");
      int2ObjectOpenHashMap.put(404, "minecraft:comparator");
      int2ObjectOpenHashMap.put(405, "minecraft:netherbrick");
      int2ObjectOpenHashMap.put(406, "minecraft:quartz");
      int2ObjectOpenHashMap.put(407, "minecraft:tnt_minecart");
      int2ObjectOpenHashMap.put(408, "minecraft:hopper_minecart");
      int2ObjectOpenHashMap.put(417, "minecraft:iron_horse_armor");
      int2ObjectOpenHashMap.put(418, "minecraft:golden_horse_armor");
      int2ObjectOpenHashMap.put(419, "minecraft:diamond_horse_armor");
      int2ObjectOpenHashMap.put(420, "minecraft:lead");
      int2ObjectOpenHashMap.put(421, "minecraft:name_tag");
      int2ObjectOpenHashMap.put(422, "minecraft:command_block_minecart");
      int2ObjectOpenHashMap.put(2256, "minecraft:record_13");
      int2ObjectOpenHashMap.put(2257, "minecraft:record_cat");
      int2ObjectOpenHashMap.put(2258, "minecraft:record_blocks");
      int2ObjectOpenHashMap.put(2259, "minecraft:record_chirp");
      int2ObjectOpenHashMap.put(2260, "minecraft:record_far");
      int2ObjectOpenHashMap.put(2261, "minecraft:record_mall");
      int2ObjectOpenHashMap.put(2262, "minecraft:record_mellohi");
      int2ObjectOpenHashMap.put(2263, "minecraft:record_stal");
      int2ObjectOpenHashMap.put(2264, "minecraft:record_strad");
      int2ObjectOpenHashMap.put(2265, "minecraft:record_ward");
      int2ObjectOpenHashMap.put(2266, "minecraft:record_11");
      int2ObjectOpenHashMap.put(2267, "minecraft:record_wait");
      int2ObjectOpenHashMap.defaultReturnValue("minecraft:air");
   });

   public ItemIdFix(Schema outputSchema, boolean changesType) {
      super(outputSchema, changesType);
   }

   public static String fromId(int id) {
      return (String)NUMERICAL_ID_TO_STRING_ID_MAP.get(id);
   }

   public TypeRewriteRule makeRule() {
      Type<Either<Integer, Pair<String, String>>> type = DSL.or(DSL.intType(), DSL.named(TypeReferences.ITEM_NAME.typeName(), DSL.namespacedString()));
      Type<Pair<String, String>> type2 = DSL.named(TypeReferences.ITEM_NAME.typeName(), DSL.namespacedString());
      OpticFinder<Either<Integer, Pair<String, String>>> opticFinder = DSL.fieldFinder("id", type);
      return this.fixTypeEverywhereTyped("ItemIdFix", this.getInputSchema().getType(TypeReferences.ITEM_STACK), this.getOutputSchema().getType(TypeReferences.ITEM_STACK), (typed) -> {
         return typed.update(opticFinder, type2, (either) -> {
            return (Pair)either.map((integer) -> {
               return Pair.of(TypeReferences.ITEM_NAME.typeName(), fromId(integer));
            }, (pair) -> {
               return pair;
            });
         });
      });
   }
}
