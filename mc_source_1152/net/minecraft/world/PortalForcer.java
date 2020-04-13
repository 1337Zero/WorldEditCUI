package net.minecraft.world;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

public class PortalForcer {
   private final ServerWorld world;
   private final Random random;

   public PortalForcer(ServerWorld world) {
      this.world = world;
      this.random = new Random(world.getSeed());
   }

   public boolean usePortal(Entity entity, float f) {
      Vec3d vec3d = entity.getLastNetherPortalDirectionVector();
      Direction direction = entity.getLastNetherPortalDirection();
      BlockPattern.TeleportTarget teleportTarget = this.getPortal(new BlockPos(entity), entity.getVelocity(), direction, vec3d.x, vec3d.y, entity instanceof PlayerEntity);
      if (teleportTarget == null) {
         return false;
      } else {
         Vec3d vec3d2 = teleportTarget.pos;
         Vec3d vec3d3 = teleportTarget.velocity;
         entity.setVelocity(vec3d3);
         entity.yaw = f + (float)teleportTarget.yaw;
         entity.positAfterTeleport(vec3d2.x, vec3d2.y, vec3d2.z);
         return true;
      }
   }

   @Nullable
   public BlockPattern.TeleportTarget getPortal(BlockPos blockPos, Vec3d vec3d, Direction direction, double x, double y, boolean canActivate) {
      PointOfInterestStorage pointOfInterestStorage = this.world.getPointOfInterestStorage();
      pointOfInterestStorage.method_22439(this.world, blockPos, 128);
      List<PointOfInterest> list = (List)pointOfInterestStorage.method_22383((pointOfInterestType) -> {
         return pointOfInterestType == PointOfInterestType.NETHER_PORTAL;
      }, blockPos, 128, PointOfInterestStorage.OccupationStatus.ANY).collect(Collectors.toList());
      Optional<PointOfInterest> optional = list.stream().min(Comparator.comparingDouble((pointOfInterest) -> {
         return pointOfInterest.getPos().getSquaredDistance(blockPos);
      }).thenComparingInt((pointOfInterest) -> {
         return pointOfInterest.getPos().getY();
      }));
      return (BlockPattern.TeleportTarget)optional.map((pointOfInterest) -> {
         BlockPos blockPos = pointOfInterest.getPos();
         this.world.getChunkManager().addTicket(ChunkTicketType.PORTAL, new ChunkPos(blockPos), 3, blockPos);
         BlockPattern.Result result = NetherPortalBlock.findPortal(this.world, blockPos);
         return result.getTeleportTarget(direction, blockPos, y, vec3d, x);
      }).orElse((Object)null);
   }

   public boolean createPortal(Entity entity) {
      int i = true;
      double d = -1.0D;
      int j = MathHelper.floor(entity.getX());
      int k = MathHelper.floor(entity.getY());
      int l = MathHelper.floor(entity.getZ());
      int m = j;
      int n = k;
      int o = l;
      int p = 0;
      int q = this.random.nextInt(4);
      BlockPos.Mutable mutable = new BlockPos.Mutable();

      int ad;
      double ae;
      int af;
      double ag;
      int ah;
      int ai;
      int aj;
      int ak;
      int al;
      int am;
      int an;
      int ao;
      int ap;
      double aq;
      double ar;
      for(ad = j - 16; ad <= j + 16; ++ad) {
         ae = (double)ad + 0.5D - entity.getX();

         for(af = l - 16; af <= l + 16; ++af) {
            ag = (double)af + 0.5D - entity.getZ();

            label276:
            for(ah = this.world.getEffectiveHeight() - 1; ah >= 0; --ah) {
               if (this.world.isAir(mutable.set(ad, ah, af))) {
                  while(ah > 0 && this.world.isAir(mutable.set(ad, ah - 1, af))) {
                     --ah;
                  }

                  for(ai = q; ai < q + 4; ++ai) {
                     aj = ai % 2;
                     ak = 1 - aj;
                     if (ai % 4 >= 2) {
                        aj = -aj;
                        ak = -ak;
                     }

                     for(al = 0; al < 3; ++al) {
                        for(am = 0; am < 4; ++am) {
                           for(an = -1; an < 4; ++an) {
                              ao = ad + (am - 1) * aj + al * ak;
                              ap = ah + an;
                              int ac = af + (am - 1) * ak - al * aj;
                              mutable.set(ao, ap, ac);
                              if (an < 0 && !this.world.getBlockState(mutable).getMaterial().isSolid() || an >= 0 && !this.world.isAir(mutable)) {
                                 continue label276;
                              }
                           }
                        }
                     }

                     aq = (double)ah + 0.5D - entity.getY();
                     ar = ae * ae + aq * aq + ag * ag;
                     if (d < 0.0D || ar < d) {
                        d = ar;
                        m = ad;
                        n = ah;
                        o = af;
                        p = ai % 4;
                     }
                  }
               }
            }
         }
      }

      if (d < 0.0D) {
         for(ad = j - 16; ad <= j + 16; ++ad) {
            ae = (double)ad + 0.5D - entity.getX();

            for(af = l - 16; af <= l + 16; ++af) {
               ag = (double)af + 0.5D - entity.getZ();

               label214:
               for(ah = this.world.getEffectiveHeight() - 1; ah >= 0; --ah) {
                  if (this.world.isAir(mutable.set(ad, ah, af))) {
                     while(ah > 0 && this.world.isAir(mutable.set(ad, ah - 1, af))) {
                        --ah;
                     }

                     for(ai = q; ai < q + 2; ++ai) {
                        aj = ai % 2;
                        ak = 1 - aj;

                        for(al = 0; al < 4; ++al) {
                           for(am = -1; am < 4; ++am) {
                              an = ad + (al - 1) * aj;
                              ao = ah + am;
                              ap = af + (al - 1) * ak;
                              mutable.set(an, ao, ap);
                              if (am < 0 && !this.world.getBlockState(mutable).getMaterial().isSolid() || am >= 0 && !this.world.isAir(mutable)) {
                                 continue label214;
                              }
                           }
                        }

                        aq = (double)ah + 0.5D - entity.getY();
                        ar = ae * ae + aq * aq + ag * ag;
                        if (d < 0.0D || ar < d) {
                           d = ar;
                           m = ad;
                           n = ah;
                           o = af;
                           p = ai % 2;
                        }
                     }
                  }
               }
            }
         }
      }

      int at = m;
      int au = n;
      af = o;
      int aw = p % 2;
      int ax = 1 - aw;
      if (p % 4 >= 2) {
         aw = -aw;
         ax = -ax;
      }

      if (d < 0.0D) {
         n = MathHelper.clamp(n, 70, this.world.getEffectiveHeight() - 10);
         au = n;

         for(ah = -1; ah <= 1; ++ah) {
            for(ai = 1; ai < 3; ++ai) {
               for(aj = -1; aj < 3; ++aj) {
                  ak = at + (ai - 1) * aw + ah * ax;
                  al = au + aj;
                  am = af + (ai - 1) * ax - ah * aw;
                  boolean bl = aj < 0;
                  mutable.set(ak, al, am);
                  this.world.setBlockState(mutable, bl ? Blocks.OBSIDIAN.getDefaultState() : Blocks.AIR.getDefaultState());
               }
            }
         }
      }

      for(ah = -1; ah < 3; ++ah) {
         for(ai = -1; ai < 4; ++ai) {
            if (ah == -1 || ah == 2 || ai == -1 || ai == 3) {
               mutable.set(at + ah * aw, au + ai, af + ah * ax);
               this.world.setBlockState(mutable, Blocks.OBSIDIAN.getDefaultState(), 3);
            }
         }
      }

      BlockState blockState = (BlockState)Blocks.NETHER_PORTAL.getDefaultState().with(NetherPortalBlock.AXIS, aw == 0 ? Direction.Axis.Z : Direction.Axis.X);

      for(ai = 0; ai < 2; ++ai) {
         for(aj = 0; aj < 3; ++aj) {
            mutable.set(at + ai * aw, au + aj, af + ai * ax);
            this.world.setBlockState(mutable, blockState, 18);
         }
      }

      return true;
   }
}
