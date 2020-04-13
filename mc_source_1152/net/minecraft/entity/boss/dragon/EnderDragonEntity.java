package net.minecraft.entity.boss.dragon;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathMinHeap;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseManager;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.EnderCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.dimension.TheEndDimension;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EnderDragonEntity extends MobEntity implements Monster {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final TrackedData<Integer> PHASE_TYPE;
   private static final TargetPredicate CLOSE_PLAYER_PREDICATE;
   public final double[][] field_7026 = new double[64][3];
   public int field_7010 = -1;
   private final EnderDragonPart[] parts;
   public final EnderDragonPart partHead = new EnderDragonPart(this, "head", 1.0F, 1.0F);
   private final EnderDragonPart partNeck = new EnderDragonPart(this, "neck", 3.0F, 3.0F);
   private final EnderDragonPart partBody = new EnderDragonPart(this, "body", 5.0F, 3.0F);
   private final EnderDragonPart partTail1 = new EnderDragonPart(this, "tail", 2.0F, 2.0F);
   private final EnderDragonPart partTail2 = new EnderDragonPart(this, "tail", 2.0F, 2.0F);
   private final EnderDragonPart partTail3 = new EnderDragonPart(this, "tail", 2.0F, 2.0F);
   private final EnderDragonPart partWingRight = new EnderDragonPart(this, "wing", 4.0F, 2.0F);
   private final EnderDragonPart partWingLeft = new EnderDragonPart(this, "wing", 4.0F, 2.0F);
   public float field_7019;
   public float field_7030;
   public boolean field_7027;
   public int field_7031;
   public float field_20865;
   @Nullable
   public EnderCrystalEntity connectedCrystal;
   @Nullable
   private final EnderDragonFight fight;
   private final PhaseManager phaseManager;
   private int field_7018 = 100;
   private int field_7029;
   private final PathNode[] field_7012 = new PathNode[24];
   private final int[] field_7025 = new int[24];
   private final PathMinHeap field_7008 = new PathMinHeap();

   public EnderDragonEntity(EntityType<? extends EnderDragonEntity> entityType, World world) {
      super(EntityType.ENDER_DRAGON, world);
      this.parts = new EnderDragonPart[]{this.partHead, this.partNeck, this.partBody, this.partTail1, this.partTail2, this.partTail3, this.partWingRight, this.partWingLeft};
      this.setHealth(this.getMaximumHealth());
      this.noClip = true;
      this.ignoreCameraFrustum = true;
      if (!world.isClient && world.dimension instanceof TheEndDimension) {
         this.fight = ((TheEndDimension)world.dimension).method_12513();
      } else {
         this.fight = null;
      }

      this.phaseManager = new PhaseManager(this);
   }

   protected void initAttributes() {
      super.initAttributes();
      this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(200.0D);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.getDataTracker().startTracking(PHASE_TYPE, PhaseType.HOVER.getTypeId());
   }

   public double[] method_6817(int i, float f) {
      if (this.getHealth() <= 0.0F) {
         f = 0.0F;
      }

      f = 1.0F - f;
      int j = this.field_7010 - i & 63;
      int k = this.field_7010 - i - 1 & 63;
      double[] ds = new double[3];
      double d = this.field_7026[j][0];
      double e = MathHelper.wrapDegrees(this.field_7026[k][0] - d);
      ds[0] = d + e * (double)f;
      d = this.field_7026[j][1];
      e = this.field_7026[k][1] - d;
      ds[1] = d + e * (double)f;
      ds[2] = MathHelper.lerp((double)f, this.field_7026[j][2], this.field_7026[k][2]);
      return ds;
   }

   public void tickMovement() {
      float f;
      float k;
      if (this.world.isClient) {
         this.setHealth(this.getHealth());
         if (!this.isSilent()) {
            f = MathHelper.cos(this.field_7030 * 6.2831855F);
            k = MathHelper.cos(this.field_7019 * 6.2831855F);
            if (k <= -0.3F && f >= -0.3F) {
               this.world.playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_FLAP, this.getSoundCategory(), 5.0F, 0.8F + this.random.nextFloat() * 0.3F, false);
            }

            if (!this.phaseManager.getCurrent().method_6848() && --this.field_7018 < 0) {
               this.world.playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, this.getSoundCategory(), 2.5F, 0.8F + this.random.nextFloat() * 0.3F, false);
               this.field_7018 = 200 + this.random.nextInt(200);
            }
         }
      }

      this.field_7019 = this.field_7030;
      if (this.getHealth() <= 0.0F) {
         f = (this.random.nextFloat() - 0.5F) * 8.0F;
         k = (this.random.nextFloat() - 0.5F) * 4.0F;
         float j = (this.random.nextFloat() - 0.5F) * 8.0F;
         this.world.addParticle(ParticleTypes.EXPLOSION, this.getX() + (double)f, this.getY() + 2.0D + (double)k, this.getZ() + (double)j, 0.0D, 0.0D, 0.0D);
      } else {
         this.method_6830();
         Vec3d vec3d = this.getVelocity();
         k = 0.2F / (MathHelper.sqrt(squaredHorizontalLength(vec3d)) * 10.0F + 1.0F);
         k *= (float)Math.pow(2.0D, vec3d.y);
         if (this.phaseManager.getCurrent().method_6848()) {
            this.field_7030 += 0.1F;
         } else if (this.field_7027) {
            this.field_7030 += k * 0.5F;
         } else {
            this.field_7030 += k;
         }

         this.yaw = MathHelper.wrapDegrees(this.yaw);
         if (this.isAiDisabled()) {
            this.field_7030 = 0.5F;
         } else {
            if (this.field_7010 < 0) {
               for(int l = 0; l < this.field_7026.length; ++l) {
                  this.field_7026[l][0] = (double)this.yaw;
                  this.field_7026[l][1] = this.getY();
               }
            }

            if (++this.field_7010 == this.field_7026.length) {
               this.field_7010 = 0;
            }

            this.field_7026[this.field_7010][0] = (double)this.yaw;
            this.field_7026[this.field_7010][1] = this.getY();
            double o;
            double p;
            double q;
            float an;
            float ao;
            if (this.world.isClient) {
               if (this.bodyTrackingIncrements > 0) {
                  double d = this.getX() + (this.serverX - this.getX()) / (double)this.bodyTrackingIncrements;
                  o = this.getY() + (this.serverY - this.getY()) / (double)this.bodyTrackingIncrements;
                  p = this.getZ() + (this.serverZ - this.getZ()) / (double)this.bodyTrackingIncrements;
                  q = MathHelper.wrapDegrees(this.serverYaw - (double)this.yaw);
                  this.yaw = (float)((double)this.yaw + q / (double)this.bodyTrackingIncrements);
                  this.pitch = (float)((double)this.pitch + (this.serverPitch - (double)this.pitch) / (double)this.bodyTrackingIncrements);
                  --this.bodyTrackingIncrements;
                  this.updatePosition(d, o, p);
                  this.setRotation(this.yaw, this.pitch);
               }

               this.phaseManager.getCurrent().clientTick();
            } else {
               Phase phase = this.phaseManager.getCurrent();
               phase.serverTick();
               if (this.phaseManager.getCurrent() != phase) {
                  phase = this.phaseManager.getCurrent();
                  phase.serverTick();
               }

               Vec3d vec3d2 = phase.getTarget();
               if (vec3d2 != null) {
                  o = vec3d2.x - this.getX();
                  p = vec3d2.y - this.getY();
                  q = vec3d2.z - this.getZ();
                  double r = o * o + p * p + q * q;
                  float s = phase.method_6846();
                  double t = (double)MathHelper.sqrt(o * o + q * q);
                  if (t > 0.0D) {
                     p = MathHelper.clamp(p / t, (double)(-s), (double)s);
                  }

                  this.setVelocity(this.getVelocity().add(0.0D, p * 0.01D, 0.0D));
                  this.yaw = MathHelper.wrapDegrees(this.yaw);
                  double u = MathHelper.clamp(MathHelper.wrapDegrees(180.0D - MathHelper.atan2(o, q) * 57.2957763671875D - (double)this.yaw), -50.0D, 50.0D);
                  Vec3d vec3d3 = vec3d2.subtract(this.getX(), this.getY(), this.getZ()).normalize();
                  Vec3d vec3d4 = (new Vec3d((double)MathHelper.sin(this.yaw * 0.017453292F), this.getVelocity().y, (double)(-MathHelper.cos(this.yaw * 0.017453292F)))).normalize();
                  an = Math.max(((float)vec3d4.dotProduct(vec3d3) + 0.5F) / 1.5F, 0.0F);
                  this.field_20865 *= 0.8F;
                  this.field_20865 = (float)((double)this.field_20865 + u * (double)phase.method_6847());
                  this.yaw += this.field_20865 * 0.1F;
                  ao = (float)(2.0D / (r + 1.0D));
                  float x = 0.06F;
                  this.updateVelocity(0.06F * (an * ao + (1.0F - ao)), new Vec3d(0.0D, 0.0D, -1.0D));
                  if (this.field_7027) {
                     this.move(MovementType.SELF, this.getVelocity().multiply(0.800000011920929D));
                  } else {
                     this.move(MovementType.SELF, this.getVelocity());
                  }

                  Vec3d vec3d5 = this.getVelocity().normalize();
                  double y = 0.8D + 0.15D * (vec3d5.dotProduct(vec3d4) + 1.0D) / 2.0D;
                  this.setVelocity(this.getVelocity().multiply(y, 0.9100000262260437D, y));
               }
            }

            this.bodyYaw = this.yaw;
            Vec3d[] vec3ds = new Vec3d[this.parts.length];

            for(int z = 0; z < this.parts.length; ++z) {
               vec3ds[z] = new Vec3d(this.parts[z].getX(), this.parts[z].getY(), this.parts[z].getZ());
            }

            float aa = (float)(this.method_6817(5, 1.0F)[1] - this.method_6817(10, 1.0F)[1]) * 10.0F * 0.017453292F;
            float ab = MathHelper.cos(aa);
            float ac = MathHelper.sin(aa);
            float ad = this.yaw * 0.017453292F;
            float ae = MathHelper.sin(ad);
            float af = MathHelper.cos(ad);
            this.method_22863(this.partBody, (double)(ae * 0.5F), 0.0D, (double)(-af * 0.5F));
            this.method_22863(this.partWingRight, (double)(af * 4.5F), 2.0D, (double)(ae * 4.5F));
            this.method_22863(this.partWingLeft, (double)(af * -4.5F), 2.0D, (double)(ae * -4.5F));
            if (!this.world.isClient && this.hurtTime == 0) {
               this.method_6825(this.world.getEntities((Entity)this, this.partWingRight.getBoundingBox().expand(4.0D, 2.0D, 4.0D).offset(0.0D, -2.0D, 0.0D), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
               this.method_6825(this.world.getEntities((Entity)this, this.partWingLeft.getBoundingBox().expand(4.0D, 2.0D, 4.0D).offset(0.0D, -2.0D, 0.0D), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
               this.method_6827(this.world.getEntities((Entity)this, this.partHead.getBoundingBox().expand(1.0D), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
               this.method_6827(this.world.getEntities((Entity)this, this.partNeck.getBoundingBox().expand(1.0D), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
            }

            float ag = MathHelper.sin(this.yaw * 0.017453292F - this.field_20865 * 0.01F);
            float ah = MathHelper.cos(this.yaw * 0.017453292F - this.field_20865 * 0.01F);
            float ai = this.method_6820();
            this.method_22863(this.partHead, (double)(ag * 6.5F * ab), (double)(ai + ac * 6.5F), (double)(-ah * 6.5F * ab));
            this.method_22863(this.partNeck, (double)(ag * 5.5F * ab), (double)(ai + ac * 5.5F), (double)(-ah * 5.5F * ab));
            double[] ds = this.method_6817(5, 1.0F);

            int aj;
            for(aj = 0; aj < 3; ++aj) {
               EnderDragonPart enderDragonPart = null;
               if (aj == 0) {
                  enderDragonPart = this.partTail1;
               }

               if (aj == 1) {
                  enderDragonPart = this.partTail2;
               }

               if (aj == 2) {
                  enderDragonPart = this.partTail3;
               }

               double[] es = this.method_6817(12 + aj * 2, 1.0F);
               float ak = this.yaw * 0.017453292F + this.method_6832(es[0] - ds[0]) * 0.017453292F;
               float al = MathHelper.sin(ak);
               float am = MathHelper.cos(ak);
               an = 1.5F;
               ao = (float)(aj + 1) * 2.0F;
               this.method_22863(enderDragonPart, (double)(-(ae * 1.5F + al * ao) * ab), es[1] - ds[1] - (double)((ao + 1.5F) * ac) + 1.5D, (double)((af * 1.5F + am * ao) * ab));
            }

            if (!this.world.isClient) {
               this.field_7027 = this.method_6821(this.partHead.getBoundingBox()) | this.method_6821(this.partNeck.getBoundingBox()) | this.method_6821(this.partBody.getBoundingBox());
               if (this.fight != null) {
                  this.fight.updateFight(this);
               }
            }

            for(aj = 0; aj < this.parts.length; ++aj) {
               this.parts[aj].prevX = vec3ds[aj].x;
               this.parts[aj].prevY = vec3ds[aj].y;
               this.parts[aj].prevZ = vec3ds[aj].z;
               this.parts[aj].lastRenderX = vec3ds[aj].x;
               this.parts[aj].lastRenderY = vec3ds[aj].y;
               this.parts[aj].lastRenderZ = vec3ds[aj].z;
            }

         }
      }
   }

   private void method_22863(EnderDragonPart enderDragonPart, double d, double e, double f) {
      enderDragonPart.updatePosition(this.getX() + d, this.getY() + e, this.getZ() + f);
   }

   private float method_6820() {
      if (this.phaseManager.getCurrent().method_6848()) {
         return -1.0F;
      } else {
         double[] ds = this.method_6817(5, 1.0F);
         double[] es = this.method_6817(0, 1.0F);
         return (float)(ds[1] - es[1]);
      }
   }

   private void method_6830() {
      if (this.connectedCrystal != null) {
         if (this.connectedCrystal.removed) {
            this.connectedCrystal = null;
         } else if (this.age % 10 == 0 && this.getHealth() < this.getMaximumHealth()) {
            this.setHealth(this.getHealth() + 1.0F);
         }
      }

      if (this.random.nextInt(10) == 0) {
         List<EnderCrystalEntity> list = this.world.getNonSpectatingEntities(EnderCrystalEntity.class, this.getBoundingBox().expand(32.0D));
         EnderCrystalEntity enderCrystalEntity = null;
         double d = Double.MAX_VALUE;
         Iterator var5 = list.iterator();

         while(var5.hasNext()) {
            EnderCrystalEntity enderCrystalEntity2 = (EnderCrystalEntity)var5.next();
            double e = enderCrystalEntity2.squaredDistanceTo(this);
            if (e < d) {
               d = e;
               enderCrystalEntity = enderCrystalEntity2;
            }
         }

         this.connectedCrystal = enderCrystalEntity;
      }

   }

   private void method_6825(List<Entity> list) {
      double d = (this.partBody.getBoundingBox().x1 + this.partBody.getBoundingBox().x2) / 2.0D;
      double e = (this.partBody.getBoundingBox().z1 + this.partBody.getBoundingBox().z2) / 2.0D;
      Iterator var6 = list.iterator();

      while(var6.hasNext()) {
         Entity entity = (Entity)var6.next();
         if (entity instanceof LivingEntity) {
            double f = entity.getX() - d;
            double g = entity.getZ() - e;
            double h = f * f + g * g;
            entity.addVelocity(f / h * 4.0D, 0.20000000298023224D, g / h * 4.0D);
            if (!this.phaseManager.getCurrent().method_6848() && ((LivingEntity)entity).getLastAttackedTime() < entity.age - 2) {
               entity.damage(DamageSource.mob(this), 5.0F);
               this.dealDamage(this, entity);
            }
         }
      }

   }

   private void method_6827(List<Entity> list) {
      Iterator var2 = list.iterator();

      while(var2.hasNext()) {
         Entity entity = (Entity)var2.next();
         if (entity instanceof LivingEntity) {
            entity.damage(DamageSource.mob(this), 10.0F);
            this.dealDamage(this, entity);
         }
      }

   }

   private float method_6832(double d) {
      return (float)MathHelper.wrapDegrees(d);
   }

   private boolean method_6821(Box box) {
      int i = MathHelper.floor(box.x1);
      int j = MathHelper.floor(box.y1);
      int k = MathHelper.floor(box.z1);
      int l = MathHelper.floor(box.x2);
      int m = MathHelper.floor(box.y2);
      int n = MathHelper.floor(box.z2);
      boolean bl = false;
      boolean bl2 = false;

      for(int o = i; o <= l; ++o) {
         for(int p = j; p <= m; ++p) {
            for(int q = k; q <= n; ++q) {
               BlockPos blockPos = new BlockPos(o, p, q);
               BlockState blockState = this.world.getBlockState(blockPos);
               Block block = blockState.getBlock();
               if (!blockState.isAir() && blockState.getMaterial() != Material.FIRE) {
                  if (this.world.getGameRules().getBoolean(GameRules.MOB_GRIEFING) && !BlockTags.DRAGON_IMMUNE.contains(block)) {
                     bl2 = this.world.removeBlock(blockPos, false) || bl2;
                  } else {
                     bl = true;
                  }
               }
            }
         }
      }

      if (bl2) {
         BlockPos blockPos2 = new BlockPos(i + this.random.nextInt(l - i + 1), j + this.random.nextInt(m - j + 1), k + this.random.nextInt(n - k + 1));
         this.world.playLevelEvent(2008, blockPos2, 0);
      }

      return bl;
   }

   public boolean damagePart(EnderDragonPart enderDragonPart, DamageSource damageSource, float f) {
      if (this.phaseManager.getCurrent().getType() == PhaseType.DYING) {
         return false;
      } else {
         f = this.phaseManager.getCurrent().modifyDamageTaken(damageSource, f);
         if (enderDragonPart != this.partHead) {
            f = f / 4.0F + Math.min(f, 1.0F);
         }

         if (f < 0.01F) {
            return false;
         } else {
            if (damageSource.getAttacker() instanceof PlayerEntity || damageSource.isExplosive()) {
               float g = this.getHealth();
               this.method_6819(damageSource, f);
               if (this.getHealth() <= 0.0F && !this.phaseManager.getCurrent().method_6848()) {
                  this.setHealth(1.0F);
                  this.phaseManager.setPhase(PhaseType.DYING);
               }

               if (this.phaseManager.getCurrent().method_6848()) {
                  this.field_7029 = (int)((float)this.field_7029 + (g - this.getHealth()));
                  if ((float)this.field_7029 > 0.25F * this.getMaximumHealth()) {
                     this.field_7029 = 0;
                     this.phaseManager.setPhase(PhaseType.TAKEOFF);
                  }
               }
            }

            return true;
         }
      }
   }

   public boolean damage(DamageSource source, float amount) {
      if (source instanceof EntityDamageSource && ((EntityDamageSource)source).method_5549()) {
         this.damagePart(this.partBody, source, amount);
      }

      return false;
   }

   protected boolean method_6819(DamageSource damageSource, float f) {
      return super.damage(damageSource, f);
   }

   public void kill() {
      this.remove();
      if (this.fight != null) {
         this.fight.updateFight(this);
         this.fight.dragonKilled(this);
      }

   }

   protected void updatePostDeath() {
      if (this.fight != null) {
         this.fight.updateFight(this);
      }

      ++this.field_7031;
      if (this.field_7031 >= 180 && this.field_7031 <= 200) {
         float f = (this.random.nextFloat() - 0.5F) * 8.0F;
         float g = (this.random.nextFloat() - 0.5F) * 4.0F;
         float h = (this.random.nextFloat() - 0.5F) * 8.0F;
         this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.getX() + (double)f, this.getY() + 2.0D + (double)g, this.getZ() + (double)h, 0.0D, 0.0D, 0.0D);
      }

      boolean bl = this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT);
      int i = 500;
      if (this.fight != null && !this.fight.hasPreviouslyKilled()) {
         i = 12000;
      }

      if (!this.world.isClient) {
         if (this.field_7031 > 150 && this.field_7031 % 5 == 0 && bl) {
            this.method_6824(MathHelper.floor((float)i * 0.08F));
         }

         if (this.field_7031 == 1) {
            this.world.playGlobalEvent(1028, new BlockPos(this), 0);
         }
      }

      this.move(MovementType.SELF, new Vec3d(0.0D, 0.10000000149011612D, 0.0D));
      this.yaw += 20.0F;
      this.bodyYaw = this.yaw;
      if (this.field_7031 == 200 && !this.world.isClient) {
         if (bl) {
            this.method_6824(MathHelper.floor((float)i * 0.2F));
         }

         if (this.fight != null) {
            this.fight.dragonKilled(this);
         }

         this.remove();
      }

   }

   private void method_6824(int i) {
      while(i > 0) {
         int j = ExperienceOrbEntity.roundToOrbSize(i);
         i -= j;
         this.world.spawnEntity(new ExperienceOrbEntity(this.world, this.getX(), this.getY(), this.getZ(), j));
      }

   }

   public int method_6818() {
      if (this.field_7012[0] == null) {
         for(int i = 0; i < 24; ++i) {
            int j = 5;
            int n;
            int o;
            if (i < 12) {
               n = MathHelper.floor(60.0F * MathHelper.cos(2.0F * (-3.1415927F + 0.2617994F * (float)i)));
               o = MathHelper.floor(60.0F * MathHelper.sin(2.0F * (-3.1415927F + 0.2617994F * (float)i)));
            } else {
               int k;
               if (i < 20) {
                  k = i - 12;
                  n = MathHelper.floor(40.0F * MathHelper.cos(2.0F * (-3.1415927F + 0.3926991F * (float)k)));
                  o = MathHelper.floor(40.0F * MathHelper.sin(2.0F * (-3.1415927F + 0.3926991F * (float)k)));
                  j += 10;
               } else {
                  k = i - 20;
                  n = MathHelper.floor(20.0F * MathHelper.cos(2.0F * (-3.1415927F + 0.7853982F * (float)k)));
                  o = MathHelper.floor(20.0F * MathHelper.sin(2.0F * (-3.1415927F + 0.7853982F * (float)k)));
               }
            }

            int r = Math.max(this.world.getSeaLevel() + 10, this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, new BlockPos(n, 0, o)).getY() + j);
            this.field_7012[i] = new PathNode(n, r, o);
         }

         this.field_7025[0] = 6146;
         this.field_7025[1] = 8197;
         this.field_7025[2] = 8202;
         this.field_7025[3] = 16404;
         this.field_7025[4] = 32808;
         this.field_7025[5] = 32848;
         this.field_7025[6] = 65696;
         this.field_7025[7] = 131392;
         this.field_7025[8] = 131712;
         this.field_7025[9] = 263424;
         this.field_7025[10] = 526848;
         this.field_7025[11] = 525313;
         this.field_7025[12] = 1581057;
         this.field_7025[13] = 3166214;
         this.field_7025[14] = 2138120;
         this.field_7025[15] = 6373424;
         this.field_7025[16] = 4358208;
         this.field_7025[17] = 12910976;
         this.field_7025[18] = 9044480;
         this.field_7025[19] = 9706496;
         this.field_7025[20] = 15216640;
         this.field_7025[21] = 13688832;
         this.field_7025[22] = 11763712;
         this.field_7025[23] = 8257536;
      }

      return this.method_6822(this.getX(), this.getY(), this.getZ());
   }

   public int method_6822(double d, double e, double f) {
      float g = 10000.0F;
      int i = 0;
      PathNode pathNode = new PathNode(MathHelper.floor(d), MathHelper.floor(e), MathHelper.floor(f));
      int j = 0;
      if (this.fight == null || this.fight.getAliveEndCrystals() == 0) {
         j = 12;
      }

      for(int k = j; k < 24; ++k) {
         if (this.field_7012[k] != null) {
            float h = this.field_7012[k].getSquaredDistance(pathNode);
            if (h < g) {
               g = h;
               i = k;
            }
         }
      }

      return i;
   }

   @Nullable
   public Path method_6833(int i, int j, @Nullable PathNode pathNode) {
      PathNode pathNode4;
      for(int k = 0; k < 24; ++k) {
         pathNode4 = this.field_7012[k];
         pathNode4.visited = false;
         pathNode4.heapWeight = 0.0F;
         pathNode4.penalizedPathLength = 0.0F;
         pathNode4.distanceToNearestTarget = 0.0F;
         pathNode4.previous = null;
         pathNode4.heapIndex = -1;
      }

      PathNode pathNode3 = this.field_7012[i];
      pathNode4 = this.field_7012[j];
      pathNode3.penalizedPathLength = 0.0F;
      pathNode3.distanceToNearestTarget = pathNode3.getDistance(pathNode4);
      pathNode3.heapWeight = pathNode3.distanceToNearestTarget;
      this.field_7008.clear();
      this.field_7008.push(pathNode3);
      PathNode pathNode5 = pathNode3;
      int l = 0;
      if (this.fight == null || this.fight.getAliveEndCrystals() == 0) {
         l = 12;
      }

      while(!this.field_7008.isEmpty()) {
         PathNode pathNode6 = this.field_7008.pop();
         if (pathNode6.equals(pathNode4)) {
            if (pathNode != null) {
               pathNode.previous = pathNode4;
               pathNode4 = pathNode;
            }

            return this.method_6826(pathNode3, pathNode4);
         }

         if (pathNode6.getDistance(pathNode4) < pathNode5.getDistance(pathNode4)) {
            pathNode5 = pathNode6;
         }

         pathNode6.visited = true;
         int m = 0;

         int o;
         for(o = 0; o < 24; ++o) {
            if (this.field_7012[o] == pathNode6) {
               m = o;
               break;
            }
         }

         for(o = l; o < 24; ++o) {
            if ((this.field_7025[m] & 1 << o) > 0) {
               PathNode pathNode7 = this.field_7012[o];
               if (!pathNode7.visited) {
                  float f = pathNode6.penalizedPathLength + pathNode6.getDistance(pathNode7);
                  if (!pathNode7.isInHeap() || f < pathNode7.penalizedPathLength) {
                     pathNode7.previous = pathNode6;
                     pathNode7.penalizedPathLength = f;
                     pathNode7.distanceToNearestTarget = pathNode7.getDistance(pathNode4);
                     if (pathNode7.isInHeap()) {
                        this.field_7008.setNodeWeight(pathNode7, pathNode7.penalizedPathLength + pathNode7.distanceToNearestTarget);
                     } else {
                        pathNode7.heapWeight = pathNode7.penalizedPathLength + pathNode7.distanceToNearestTarget;
                        this.field_7008.push(pathNode7);
                     }
                  }
               }
            }
         }
      }

      if (pathNode5 == pathNode3) {
         return null;
      } else {
         LOGGER.debug("Failed to find path from {} to {}", i, j);
         if (pathNode != null) {
            pathNode.previous = pathNode5;
            pathNode5 = pathNode;
         }

         return this.method_6826(pathNode3, pathNode5);
      }
   }

   private Path method_6826(PathNode pathNode, PathNode pathNode2) {
      List<PathNode> list = Lists.newArrayList();
      PathNode pathNode3 = pathNode2;
      list.add(0, pathNode2);

      while(pathNode3.previous != null) {
         pathNode3 = pathNode3.previous;
         list.add(0, pathNode3);
      }

      return new Path(list, new BlockPos(pathNode2.x, pathNode2.y, pathNode2.z), true);
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      tag.putInt("DragonPhase", this.phaseManager.getCurrent().getType().getTypeId());
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      if (tag.contains("DragonPhase")) {
         this.phaseManager.setPhase(PhaseType.getFromId(tag.getInt("DragonPhase")));
      }

   }

   public void checkDespawn() {
   }

   public EnderDragonPart[] getBodyParts() {
      return this.parts;
   }

   public boolean collides() {
      return false;
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.HOSTILE;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_ENDER_DRAGON_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_ENDER_DRAGON_HURT;
   }

   protected float getSoundVolume() {
      return 5.0F;
   }

   @Environment(EnvType.CLIENT)
   public float method_6823(int i, double[] ds, double[] es) {
      Phase phase = this.phaseManager.getCurrent();
      PhaseType<? extends Phase> phaseType = phase.getType();
      double h;
      if (phaseType != PhaseType.LANDING && phaseType != PhaseType.TAKEOFF) {
         if (phase.method_6848()) {
            h = (double)i;
         } else if (i == 6) {
            h = 0.0D;
         } else {
            h = es[1] - ds[1];
         }
      } else {
         BlockPos blockPos = this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.ORIGIN);
         float f = Math.max(MathHelper.sqrt(blockPos.getSquaredDistance(this.getPos(), true)) / 4.0F, 1.0F);
         h = (double)((float)i / f);
      }

      return (float)h;
   }

   public Vec3d method_6834(float f) {
      Phase phase = this.phaseManager.getCurrent();
      PhaseType<? extends Phase> phaseType = phase.getType();
      Vec3d vec3d2;
      float l;
      if (phaseType != PhaseType.LANDING && phaseType != PhaseType.TAKEOFF) {
         if (phase.method_6848()) {
            float k = this.pitch;
            l = 1.5F;
            this.pitch = -45.0F;
            vec3d2 = this.getRotationVec(f);
            this.pitch = k;
         } else {
            vec3d2 = this.getRotationVec(f);
         }
      } else {
         BlockPos blockPos = this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.ORIGIN);
         l = Math.max(MathHelper.sqrt(blockPos.getSquaredDistance(this.getPos(), true)) / 4.0F, 1.0F);
         float h = 6.0F / l;
         float i = this.pitch;
         float j = 1.5F;
         this.pitch = -h * 1.5F * 5.0F;
         vec3d2 = this.getRotationVec(f);
         this.pitch = i;
      }

      return vec3d2;
   }

   public void crystalDestroyed(EnderCrystalEntity crystal, BlockPos pos, DamageSource source) {
      PlayerEntity playerEntity2;
      if (source.getAttacker() instanceof PlayerEntity) {
         playerEntity2 = (PlayerEntity)source.getAttacker();
      } else {
         playerEntity2 = this.world.getClosestPlayer(CLOSE_PLAYER_PREDICATE, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
      }

      if (crystal == this.connectedCrystal) {
         this.damagePart(this.partHead, DamageSource.explosion((LivingEntity)playerEntity2), 10.0F);
      }

      this.phaseManager.getCurrent().crystalDestroyed(crystal, pos, source, playerEntity2);
   }

   public void onTrackedDataSet(TrackedData<?> data) {
      if (PHASE_TYPE.equals(data) && this.world.isClient) {
         this.phaseManager.setPhase(PhaseType.getFromId((Integer)this.getDataTracker().get(PHASE_TYPE)));
      }

      super.onTrackedDataSet(data);
   }

   public PhaseManager getPhaseManager() {
      return this.phaseManager;
   }

   @Nullable
   public EnderDragonFight getFight() {
      return this.fight;
   }

   public boolean addStatusEffect(StatusEffectInstance effect) {
      return false;
   }

   protected boolean canStartRiding(Entity entity) {
      return false;
   }

   public boolean canUsePortals() {
      return false;
   }

   static {
      PHASE_TYPE = DataTracker.registerData(EnderDragonEntity.class, TrackedDataHandlerRegistry.INTEGER);
      CLOSE_PLAYER_PREDICATE = (new TargetPredicate()).setBaseMaxDistance(64.0D);
   }
}
