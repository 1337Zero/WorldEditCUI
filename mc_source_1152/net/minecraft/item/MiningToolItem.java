package net.minecraft.item;

import com.google.common.collect.Multimap;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MiningToolItem extends ToolItem {
   private final Set<Block> effectiveBlocks;
   protected final float miningSpeed;
   protected final float attackDamage;
   protected final float attackSpeed;

   protected MiningToolItem(float attackDamage, float attackSpeed, ToolMaterial material, Set<Block> effectiveBlocks, Item.Settings settings) {
      super(material, settings);
      this.effectiveBlocks = effectiveBlocks;
      this.miningSpeed = material.getMiningSpeed();
      this.attackDamage = attackDamage + material.getAttackDamage();
      this.attackSpeed = attackSpeed;
   }

   public float getMiningSpeed(ItemStack stack, BlockState state) {
      return this.effectiveBlocks.contains(state.getBlock()) ? this.miningSpeed : 1.0F;
   }

   public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      stack.damage(2, (LivingEntity)attacker, (Consumer)((e) -> {
         e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
      }));
      return true;
   }

   public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
      if (!world.isClient && state.getHardness(world, pos) != 0.0F) {
         stack.damage(1, (LivingEntity)miner, (Consumer)((e) -> {
            e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
         }));
      }

      return true;
   }

   public Multimap<String, EntityAttributeModifier> getModifiers(EquipmentSlot slot) {
      Multimap<String, EntityAttributeModifier> multimap = super.getModifiers(slot);
      if (slot == EquipmentSlot.MAINHAND) {
         multimap.put(EntityAttributes.ATTACK_DAMAGE.getId(), new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_UUID, "Tool modifier", (double)this.attackDamage, EntityAttributeModifier.Operation.ADDITION));
         multimap.put(EntityAttributes.ATTACK_SPEED.getId(), new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_UUID, "Tool modifier", (double)this.attackSpeed, EntityAttributeModifier.Operation.ADDITION));
      }

      return multimap;
   }
}
