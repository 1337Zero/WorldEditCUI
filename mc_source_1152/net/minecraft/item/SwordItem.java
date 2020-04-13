package net.minecraft.item;

import com.google.common.collect.Multimap;
import java.util.function.Consumer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SwordItem extends ToolItem {
   private final float attackDamage;
   private final float attackSpeed;

   public SwordItem(ToolMaterial material, int attackDamage, float attackSpeed, Item.Settings settings) {
      super(material, settings);
      this.attackSpeed = attackSpeed;
      this.attackDamage = (float)attackDamage + material.getAttackDamage();
   }

   public float getAttackDamage() {
      return this.attackDamage;
   }

   public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
      return !miner.isCreative();
   }

   public float getMiningSpeed(ItemStack stack, BlockState state) {
      Block block = state.getBlock();
      if (block == Blocks.COBWEB) {
         return 15.0F;
      } else {
         Material material = state.getMaterial();
         return material != Material.PLANT && material != Material.REPLACEABLE_PLANT && material != Material.UNUSED_PLANT && !state.matches(BlockTags.LEAVES) && material != Material.PUMPKIN ? 1.0F : 1.5F;
      }
   }

   public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      stack.damage(1, (LivingEntity)attacker, (Consumer)((e) -> {
         e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
      }));
      return true;
   }

   public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
      if (state.getHardness(world, pos) != 0.0F) {
         stack.damage(2, (LivingEntity)miner, (Consumer)((e) -> {
            e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
         }));
      }

      return true;
   }

   public boolean isEffectiveOn(BlockState state) {
      return state.getBlock() == Blocks.COBWEB;
   }

   public Multimap<String, EntityAttributeModifier> getModifiers(EquipmentSlot slot) {
      Multimap<String, EntityAttributeModifier> multimap = super.getModifiers(slot);
      if (slot == EquipmentSlot.MAINHAND) {
         multimap.put(EntityAttributes.ATTACK_DAMAGE.getId(), new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_UUID, "Weapon modifier", (double)this.attackDamage, EntityAttributeModifier.Operation.ADDITION));
         multimap.put(EntityAttributes.ATTACK_SPEED.getId(), new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_UUID, "Weapon modifier", (double)this.attackSpeed, EntityAttributeModifier.Operation.ADDITION));
      }

      return multimap;
   }
}
