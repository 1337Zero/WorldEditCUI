package net.minecraft.item;

import com.google.common.collect.Multimap;
import java.util.function.Consumer;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TridentItem extends Item {
   public TridentItem(Item.Settings settings) {
      super(settings);
      this.addPropertyGetter(new Identifier("throwing"), (stack, world, entity) -> {
         return entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0F : 0.0F;
      });
   }

   public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
      return !miner.isCreative();
   }

   public UseAction getUseAction(ItemStack stack) {
      return UseAction.SPEAR;
   }

   public int getMaxUseTime(ItemStack stack) {
      return 72000;
   }

   public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
      if (user instanceof PlayerEntity) {
         PlayerEntity playerEntity = (PlayerEntity)user;
         int i = this.getMaxUseTime(stack) - remainingUseTicks;
         if (i >= 10) {
            int j = EnchantmentHelper.getRiptide(stack);
            if (j <= 0 || playerEntity.isTouchingWaterOrRain()) {
               if (!world.isClient) {
                  stack.damage(1, (LivingEntity)playerEntity, (Consumer)((p) -> {
                     p.sendToolBreakStatus(user.getActiveHand());
                  }));
                  if (j == 0) {
                     TridentEntity tridentEntity = new TridentEntity(world, playerEntity, stack);
                     tridentEntity.setProperties(playerEntity, playerEntity.pitch, playerEntity.yaw, 0.0F, 2.5F + (float)j * 0.5F, 1.0F);
                     if (playerEntity.abilities.creativeMode) {
                        tridentEntity.pickupType = ProjectileEntity.PickupPermission.CREATIVE_ONLY;
                     }

                     world.spawnEntity(tridentEntity);
                     world.playSoundFromEntity((PlayerEntity)null, tridentEntity, SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
                     if (!playerEntity.abilities.creativeMode) {
                        playerEntity.inventory.removeOne(stack);
                     }
                  }
               }

               playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
               if (j > 0) {
                  float f = playerEntity.yaw;
                  float g = playerEntity.pitch;
                  float h = -MathHelper.sin(f * 0.017453292F) * MathHelper.cos(g * 0.017453292F);
                  float k = -MathHelper.sin(g * 0.017453292F);
                  float l = MathHelper.cos(f * 0.017453292F) * MathHelper.cos(g * 0.017453292F);
                  float m = MathHelper.sqrt(h * h + k * k + l * l);
                  float n = 3.0F * ((1.0F + (float)j) / 4.0F);
                  h *= n / m;
                  k *= n / m;
                  l *= n / m;
                  playerEntity.addVelocity((double)h, (double)k, (double)l);
                  playerEntity.setPushCooldown(20);
                  if (playerEntity.onGround) {
                     float o = 1.1999999F;
                     playerEntity.move(MovementType.SELF, new Vec3d(0.0D, 1.1999999284744263D, 0.0D));
                  }

                  SoundEvent soundEvent3;
                  if (j >= 3) {
                     soundEvent3 = SoundEvents.ITEM_TRIDENT_RIPTIDE_3;
                  } else if (j == 2) {
                     soundEvent3 = SoundEvents.ITEM_TRIDENT_RIPTIDE_2;
                  } else {
                     soundEvent3 = SoundEvents.ITEM_TRIDENT_RIPTIDE_1;
                  }

                  world.playSoundFromEntity((PlayerEntity)null, playerEntity, soundEvent3, SoundCategory.PLAYERS, 1.0F, 1.0F);
               }

            }
         }
      }
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack itemStack = user.getStackInHand(hand);
      if (itemStack.getDamage() >= itemStack.getMaxDamage() - 1) {
         return TypedActionResult.fail(itemStack);
      } else if (EnchantmentHelper.getRiptide(itemStack) > 0 && !user.isTouchingWaterOrRain()) {
         return TypedActionResult.fail(itemStack);
      } else {
         user.setCurrentHand(hand);
         return TypedActionResult.consume(itemStack);
      }
   }

   public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      stack.damage(1, (LivingEntity)attacker, (Consumer)((e) -> {
         e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
      }));
      return true;
   }

   public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
      if ((double)state.getHardness(world, pos) != 0.0D) {
         stack.damage(2, (LivingEntity)miner, (Consumer)((e) -> {
            e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
         }));
      }

      return true;
   }

   public Multimap<String, EntityAttributeModifier> getModifiers(EquipmentSlot slot) {
      Multimap<String, EntityAttributeModifier> multimap = super.getModifiers(slot);
      if (slot == EquipmentSlot.MAINHAND) {
         multimap.put(EntityAttributes.ATTACK_DAMAGE.getId(), new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_UUID, "Tool modifier", 8.0D, EntityAttributeModifier.Operation.ADDITION));
         multimap.put(EntityAttributes.ATTACK_SPEED.getId(), new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_UUID, "Tool modifier", -2.9000000953674316D, EntityAttributeModifier.Operation.ADDITION));
      }

      return multimap;
   }

   public int getEnchantability() {
      return 1;
   }
}
