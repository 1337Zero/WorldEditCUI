package net.minecraft.structure;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Clearable;
import net.minecraft.util.IdList;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.BitSetVoxelSet;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class Structure {
   private final List<List<Structure.StructureBlockInfo>> blocks = Lists.newArrayList();
   private final List<Structure.StructureEntityInfo> entities = Lists.newArrayList();
   private BlockPos size;
   private String author;

   public Structure() {
      this.size = BlockPos.ORIGIN;
      this.author = "?";
   }

   public BlockPos getSize() {
      return this.size;
   }

   public void setAuthor(String name) {
      this.author = name;
   }

   public String getAuthor() {
      return this.author;
   }

   public void method_15174(World world, BlockPos blockPos, BlockPos blockPos2, boolean bl, @Nullable Block block) {
      if (blockPos2.getX() >= 1 && blockPos2.getY() >= 1 && blockPos2.getZ() >= 1) {
         BlockPos blockPos3 = blockPos.add(blockPos2).add(-1, -1, -1);
         List<Structure.StructureBlockInfo> list = Lists.newArrayList();
         List<Structure.StructureBlockInfo> list2 = Lists.newArrayList();
         List<Structure.StructureBlockInfo> list3 = Lists.newArrayList();
         BlockPos blockPos4 = new BlockPos(Math.min(blockPos.getX(), blockPos3.getX()), Math.min(blockPos.getY(), blockPos3.getY()), Math.min(blockPos.getZ(), blockPos3.getZ()));
         BlockPos blockPos5 = new BlockPos(Math.max(blockPos.getX(), blockPos3.getX()), Math.max(blockPos.getY(), blockPos3.getY()), Math.max(blockPos.getZ(), blockPos3.getZ()));
         this.size = blockPos2;
         Iterator var12 = BlockPos.iterate(blockPos4, blockPos5).iterator();

         while(true) {
            while(true) {
               BlockPos blockPos6;
               BlockPos blockPos7;
               BlockState blockState;
               do {
                  if (!var12.hasNext()) {
                     List<Structure.StructureBlockInfo> list4 = Lists.newArrayList();
                     list4.addAll(list);
                     list4.addAll(list2);
                     list4.addAll(list3);
                     this.blocks.clear();
                     this.blocks.add(list4);
                     if (bl) {
                        this.method_15164(world, blockPos4, blockPos5.add(1, 1, 1));
                     } else {
                        this.entities.clear();
                     }

                     return;
                  }

                  blockPos6 = (BlockPos)var12.next();
                  blockPos7 = blockPos6.subtract(blockPos4);
                  blockState = world.getBlockState(blockPos6);
               } while(block != null && block == blockState.getBlock());

               BlockEntity blockEntity = world.getBlockEntity(blockPos6);
               if (blockEntity != null) {
                  CompoundTag compoundTag = blockEntity.toTag(new CompoundTag());
                  compoundTag.remove("x");
                  compoundTag.remove("y");
                  compoundTag.remove("z");
                  list2.add(new Structure.StructureBlockInfo(blockPos7, blockState, compoundTag));
               } else if (!blockState.isFullOpaque(world, blockPos6) && !blockState.isFullCube(world, blockPos6)) {
                  list3.add(new Structure.StructureBlockInfo(blockPos7, blockState, (CompoundTag)null));
               } else {
                  list.add(new Structure.StructureBlockInfo(blockPos7, blockState, (CompoundTag)null));
               }
            }
         }
      }
   }

   private void method_15164(World world, BlockPos blockPos, BlockPos blockPos2) {
      List<Entity> list = world.getEntities(Entity.class, new Box(blockPos, blockPos2), (entityx) -> {
         return !(entityx instanceof PlayerEntity);
      });
      this.entities.clear();

      Vec3d vec3d;
      CompoundTag compoundTag;
      BlockPos blockPos4;
      for(Iterator var5 = list.iterator(); var5.hasNext(); this.entities.add(new Structure.StructureEntityInfo(vec3d, blockPos4, compoundTag))) {
         Entity entity = (Entity)var5.next();
         vec3d = new Vec3d(entity.getX() - (double)blockPos.getX(), entity.getY() - (double)blockPos.getY(), entity.getZ() - (double)blockPos.getZ());
         compoundTag = new CompoundTag();
         entity.saveToTag(compoundTag);
         if (entity instanceof PaintingEntity) {
            blockPos4 = ((PaintingEntity)entity).getDecorationBlockPos().subtract(blockPos);
         } else {
            blockPos4 = new BlockPos(vec3d);
         }
      }

   }

   public List<Structure.StructureBlockInfo> method_16445(BlockPos blockPos, StructurePlacementData structurePlacementData, Block block) {
      return this.method_15165(blockPos, structurePlacementData, block, true);
   }

   public List<Structure.StructureBlockInfo> method_15165(BlockPos blockPos, StructurePlacementData structurePlacementData, Block block, boolean bl) {
      List<Structure.StructureBlockInfo> list = Lists.newArrayList();
      BlockBox blockBox = structurePlacementData.method_15124();
      Iterator var7 = structurePlacementData.method_15121(this.blocks, blockPos).iterator();

      while(true) {
         Structure.StructureBlockInfo structureBlockInfo;
         BlockPos blockPos2;
         do {
            if (!var7.hasNext()) {
               return list;
            }

            structureBlockInfo = (Structure.StructureBlockInfo)var7.next();
            blockPos2 = bl ? method_15171(structurePlacementData, structureBlockInfo.pos).add(blockPos) : structureBlockInfo.pos;
         } while(blockBox != null && !blockBox.contains(blockPos2));

         BlockState blockState = structureBlockInfo.state;
         if (blockState.getBlock() == block) {
            list.add(new Structure.StructureBlockInfo(blockPos2, blockState.rotate(structurePlacementData.getRotation()), structureBlockInfo.tag));
         }
      }
   }

   public BlockPos method_15180(StructurePlacementData structurePlacementData, BlockPos blockPos, StructurePlacementData structurePlacementData2, BlockPos blockPos2) {
      BlockPos blockPos3 = method_15171(structurePlacementData, blockPos);
      BlockPos blockPos4 = method_15171(structurePlacementData2, blockPos2);
      return blockPos3.subtract(blockPos4);
   }

   public static BlockPos method_15171(StructurePlacementData structurePlacementData, BlockPos blockPos) {
      return method_15168(blockPos, structurePlacementData.getMirror(), structurePlacementData.getRotation(), structurePlacementData.getPosition());
   }

   public void place(IWorld iWorld, BlockPos blockPos, StructurePlacementData structurePlacementData) {
      structurePlacementData.method_15132();
      this.method_15178(iWorld, blockPos, structurePlacementData);
   }

   public void method_15178(IWorld iWorld, BlockPos blockPos, StructurePlacementData structurePlacementData) {
      this.method_15172(iWorld, blockPos, structurePlacementData, 2);
   }

   public boolean method_15172(IWorld iWorld, BlockPos blockPos, StructurePlacementData structurePlacementData, int i) {
      if (this.blocks.isEmpty()) {
         return false;
      } else {
         List<Structure.StructureBlockInfo> list = structurePlacementData.method_15121(this.blocks, blockPos);
         if ((!list.isEmpty() || !structurePlacementData.shouldIgnoreEntities() && !this.entities.isEmpty()) && this.size.getX() >= 1 && this.size.getY() >= 1 && this.size.getZ() >= 1) {
            BlockBox blockBox = structurePlacementData.method_15124();
            List<BlockPos> list2 = Lists.newArrayListWithCapacity(structurePlacementData.shouldPlaceFluids() ? list.size() : 0);
            List<Pair<BlockPos, CompoundTag>> list3 = Lists.newArrayListWithCapacity(list.size());
            int j = Integer.MAX_VALUE;
            int k = Integer.MAX_VALUE;
            int l = Integer.MAX_VALUE;
            int m = Integer.MIN_VALUE;
            int n = Integer.MIN_VALUE;
            int o = Integer.MIN_VALUE;
            List<Structure.StructureBlockInfo> list4 = process(iWorld, blockPos, structurePlacementData, list);
            Iterator var16 = list4.iterator();

            while(true) {
               Structure.StructureBlockInfo structureBlockInfo;
               BlockPos blockPos2;
               BlockEntity blockEntity3;
               do {
                  if (!var16.hasNext()) {
                     boolean bl = true;
                     Direction[] directions = new Direction[]{Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

                     Iterator iterator;
                     BlockPos blockPos7;
                     BlockState blockState4;
                     while(bl && !list2.isEmpty()) {
                        bl = false;
                        iterator = list2.iterator();

                        while(iterator.hasNext()) {
                           BlockPos blockPos3 = (BlockPos)iterator.next();
                           blockPos7 = blockPos3;
                           FluidState fluidState2 = iWorld.getFluidState(blockPos3);

                           for(int p = 0; p < directions.length && !fluidState2.isStill(); ++p) {
                              BlockPos blockPos5 = blockPos7.offset(directions[p]);
                              FluidState fluidState3 = iWorld.getFluidState(blockPos5);
                              if (fluidState3.getHeight(iWorld, blockPos5) > fluidState2.getHeight(iWorld, blockPos7) || fluidState3.isStill() && !fluidState2.isStill()) {
                                 fluidState2 = fluidState3;
                                 blockPos7 = blockPos5;
                              }
                           }

                           if (fluidState2.isStill()) {
                              blockState4 = iWorld.getBlockState(blockPos3);
                              Block block = blockState4.getBlock();
                              if (block instanceof FluidFillable) {
                                 ((FluidFillable)block).tryFillWithFluid(iWorld, blockPos3, blockState4, fluidState2);
                                 bl = true;
                                 iterator.remove();
                              }
                           }
                        }
                     }

                     if (j <= m) {
                        if (!structurePlacementData.method_16444()) {
                           VoxelSet voxelSet = new BitSetVoxelSet(m - j + 1, n - k + 1, o - l + 1);
                           int q = j;
                           int r = k;
                           int s = l;
                           Iterator var38 = list3.iterator();

                           while(var38.hasNext()) {
                              Pair<BlockPos, CompoundTag> pair = (Pair)var38.next();
                              BlockPos blockPos6 = (BlockPos)pair.getFirst();
                              voxelSet.set(blockPos6.getX() - q, blockPos6.getY() - r, blockPos6.getZ() - s, true, true);
                           }

                           method_20532(iWorld, i, voxelSet, q, r, s);
                        }

                        iterator = list3.iterator();

                        while(iterator.hasNext()) {
                           Pair<BlockPos, CompoundTag> pair2 = (Pair)iterator.next();
                           blockPos7 = (BlockPos)pair2.getFirst();
                           if (!structurePlacementData.method_16444()) {
                              BlockState blockState3 = iWorld.getBlockState(blockPos7);
                              blockState4 = Block.getRenderingState(blockState3, iWorld, blockPos7);
                              if (blockState3 != blockState4) {
                                 iWorld.setBlockState(blockPos7, blockState4, i & -2 | 16);
                              }

                              iWorld.updateNeighbors(blockPos7, blockState4.getBlock());
                           }

                           if (pair2.getSecond() != null) {
                              blockEntity3 = iWorld.getBlockEntity(blockPos7);
                              if (blockEntity3 != null) {
                                 blockEntity3.markDirty();
                              }
                           }
                        }
                     }

                     if (!structurePlacementData.shouldIgnoreEntities()) {
                        this.method_15179(iWorld, blockPos, structurePlacementData.getMirror(), structurePlacementData.getRotation(), structurePlacementData.getPosition(), blockBox);
                     }

                     return true;
                  }

                  structureBlockInfo = (Structure.StructureBlockInfo)var16.next();
                  blockPos2 = structureBlockInfo.pos;
               } while(blockBox != null && !blockBox.contains(blockPos2));

               FluidState fluidState = structurePlacementData.shouldPlaceFluids() ? iWorld.getFluidState(blockPos2) : null;
               BlockState blockState = structureBlockInfo.state.mirror(structurePlacementData.getMirror()).rotate(structurePlacementData.getRotation());
               if (structureBlockInfo.tag != null) {
                  blockEntity3 = iWorld.getBlockEntity(blockPos2);
                  Clearable.clear(blockEntity3);
                  iWorld.setBlockState(blockPos2, Blocks.BARRIER.getDefaultState(), 20);
               }

               if (iWorld.setBlockState(blockPos2, blockState, i)) {
                  j = Math.min(j, blockPos2.getX());
                  k = Math.min(k, blockPos2.getY());
                  l = Math.min(l, blockPos2.getZ());
                  m = Math.max(m, blockPos2.getX());
                  n = Math.max(n, blockPos2.getY());
                  o = Math.max(o, blockPos2.getZ());
                  list3.add(Pair.of(blockPos2, structureBlockInfo.tag));
                  if (structureBlockInfo.tag != null) {
                     blockEntity3 = iWorld.getBlockEntity(blockPos2);
                     if (blockEntity3 != null) {
                        structureBlockInfo.tag.putInt("x", blockPos2.getX());
                        structureBlockInfo.tag.putInt("y", blockPos2.getY());
                        structureBlockInfo.tag.putInt("z", blockPos2.getZ());
                        blockEntity3.fromTag(structureBlockInfo.tag);
                        blockEntity3.applyMirror(structurePlacementData.getMirror());
                        blockEntity3.applyRotation(structurePlacementData.getRotation());
                     }
                  }

                  if (fluidState != null && blockState.getBlock() instanceof FluidFillable) {
                     ((FluidFillable)blockState.getBlock()).tryFillWithFluid(iWorld, blockPos2, blockState, fluidState);
                     if (!fluidState.isStill()) {
                        list2.add(blockPos2);
                     }
                  }
               }
            }
         } else {
            return false;
         }
      }
   }

   public static void method_20532(IWorld iWorld, int i, VoxelSet voxelSet, int j, int k, int l) {
      voxelSet.forEachDirection((direction, m, n, o) -> {
         BlockPos blockPos = new BlockPos(j + m, k + n, l + o);
         BlockPos blockPos2 = blockPos.offset(direction);
         BlockState blockState = iWorld.getBlockState(blockPos);
         BlockState blockState2 = iWorld.getBlockState(blockPos2);
         BlockState blockState3 = blockState.getStateForNeighborUpdate(direction, blockState2, iWorld, blockPos, blockPos2);
         if (blockState != blockState3) {
            iWorld.setBlockState(blockPos, blockState3, i & -2 | 16);
         }

         BlockState blockState4 = blockState2.getStateForNeighborUpdate(direction.getOpposite(), blockState3, iWorld, blockPos2, blockPos);
         if (blockState2 != blockState4) {
            iWorld.setBlockState(blockPos2, blockState4, i & -2 | 16);
         }

      });
   }

   public static List<Structure.StructureBlockInfo> process(IWorld world, BlockPos pos, StructurePlacementData placementData, List<Structure.StructureBlockInfo> blockInfos) {
      List<Structure.StructureBlockInfo> list = Lists.newArrayList();
      Iterator var5 = blockInfos.iterator();

      while(var5.hasNext()) {
         Structure.StructureBlockInfo structureBlockInfo = (Structure.StructureBlockInfo)var5.next();
         BlockPos blockPos = method_15171(placementData, structureBlockInfo.pos).add(pos);
         Structure.StructureBlockInfo structureBlockInfo2 = new Structure.StructureBlockInfo(blockPos, structureBlockInfo.state, structureBlockInfo.tag);

         for(Iterator iterator = placementData.getProcessors().iterator(); structureBlockInfo2 != null && iterator.hasNext(); structureBlockInfo2 = ((StructureProcessor)iterator.next()).process(world, pos, structureBlockInfo, structureBlockInfo2, placementData)) {
         }

         if (structureBlockInfo2 != null) {
            list.add(structureBlockInfo2);
         }
      }

      return list;
   }

   private void method_15179(IWorld iWorld, BlockPos blockPos, BlockMirror blockMirror, BlockRotation blockRotation, BlockPos blockPos2, @Nullable BlockBox blockBox) {
      Iterator var7 = this.entities.iterator();

      while(true) {
         Structure.StructureEntityInfo structureEntityInfo;
         BlockPos blockPos3;
         do {
            if (!var7.hasNext()) {
               return;
            }

            structureEntityInfo = (Structure.StructureEntityInfo)var7.next();
            blockPos3 = method_15168(structureEntityInfo.blockPos, blockMirror, blockRotation, blockPos2).add(blockPos);
         } while(blockBox != null && !blockBox.contains(blockPos3));

         CompoundTag compoundTag = structureEntityInfo.tag;
         Vec3d vec3d = method_15176(structureEntityInfo.pos, blockMirror, blockRotation, blockPos2);
         Vec3d vec3d2 = vec3d.add((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
         ListTag listTag = new ListTag();
         listTag.add(DoubleTag.of(vec3d2.x));
         listTag.add(DoubleTag.of(vec3d2.y));
         listTag.add(DoubleTag.of(vec3d2.z));
         compoundTag.put("Pos", listTag);
         compoundTag.remove("UUIDMost");
         compoundTag.remove("UUIDLeast");
         method_17916(iWorld, compoundTag).ifPresent((entity) -> {
            float f = entity.applyMirror(blockMirror);
            f += entity.yaw - entity.applyRotation(blockRotation);
            entity.refreshPositionAndAngles(vec3d2.x, vec3d2.y, vec3d2.z, f, entity.pitch);
            iWorld.spawnEntity(entity);
         });
      }
   }

   private static Optional<Entity> method_17916(IWorld iWorld, CompoundTag compoundTag) {
      try {
         return EntityType.getEntityFromTag(compoundTag, iWorld.getWorld());
      } catch (Exception var3) {
         return Optional.empty();
      }
   }

   public BlockPos method_15166(BlockRotation blockRotation) {
      switch(blockRotation) {
      case COUNTERCLOCKWISE_90:
      case CLOCKWISE_90:
         return new BlockPos(this.size.getZ(), this.size.getY(), this.size.getX());
      default:
         return this.size;
      }
   }

   public static BlockPos method_15168(BlockPos blockPos, BlockMirror blockMirror, BlockRotation blockRotation, BlockPos blockPos2) {
      int i = blockPos.getX();
      int j = blockPos.getY();
      int k = blockPos.getZ();
      boolean bl = true;
      switch(blockMirror) {
      case LEFT_RIGHT:
         k = -k;
         break;
      case FRONT_BACK:
         i = -i;
         break;
      default:
         bl = false;
      }

      int l = blockPos2.getX();
      int m = blockPos2.getZ();
      switch(blockRotation) {
      case COUNTERCLOCKWISE_90:
         return new BlockPos(l - m + k, j, l + m - i);
      case CLOCKWISE_90:
         return new BlockPos(l + m - k, j, m - l + i);
      case CLOCKWISE_180:
         return new BlockPos(l + l - i, j, m + m - k);
      default:
         return bl ? new BlockPos(i, j, k) : blockPos;
      }
   }

   private static Vec3d method_15176(Vec3d vec3d, BlockMirror blockMirror, BlockRotation blockRotation, BlockPos blockPos) {
      double d = vec3d.x;
      double e = vec3d.y;
      double f = vec3d.z;
      boolean bl = true;
      switch(blockMirror) {
      case LEFT_RIGHT:
         f = 1.0D - f;
         break;
      case FRONT_BACK:
         d = 1.0D - d;
         break;
      default:
         bl = false;
      }

      int i = blockPos.getX();
      int j = blockPos.getZ();
      switch(blockRotation) {
      case COUNTERCLOCKWISE_90:
         return new Vec3d((double)(i - j) + f, e, (double)(i + j + 1) - d);
      case CLOCKWISE_90:
         return new Vec3d((double)(i + j + 1) - f, e, (double)(j - i) + d);
      case CLOCKWISE_180:
         return new Vec3d((double)(i + i + 1) - d, e, (double)(j + j + 1) - f);
      default:
         return bl ? new Vec3d(d, e, f) : vec3d;
      }
   }

   public BlockPos method_15167(BlockPos blockPos, BlockMirror blockMirror, BlockRotation blockRotation) {
      return method_15162(blockPos, blockMirror, blockRotation, this.getSize().getX(), this.getSize().getZ());
   }

   public static BlockPos method_15162(BlockPos blockPos, BlockMirror blockMirror, BlockRotation blockRotation, int i, int j) {
      --i;
      --j;
      int k = blockMirror == BlockMirror.FRONT_BACK ? i : 0;
      int l = blockMirror == BlockMirror.LEFT_RIGHT ? j : 0;
      BlockPos blockPos2 = blockPos;
      switch(blockRotation) {
      case COUNTERCLOCKWISE_90:
         blockPos2 = blockPos.add(l, 0, i - k);
         break;
      case CLOCKWISE_90:
         blockPos2 = blockPos.add(j - l, 0, k);
         break;
      case CLOCKWISE_180:
         blockPos2 = blockPos.add(i - k, 0, j - l);
         break;
      case NONE:
         blockPos2 = blockPos.add(k, 0, l);
      }

      return blockPos2;
   }

   public BlockBox calculateBoundingBox(StructurePlacementData placementData, BlockPos pos) {
      BlockRotation blockRotation = placementData.getRotation();
      BlockPos blockPos = placementData.getPosition();
      BlockPos blockPos2 = this.method_15166(blockRotation);
      BlockMirror blockMirror = placementData.getMirror();
      int i = blockPos.getX();
      int j = blockPos.getZ();
      int k = blockPos2.getX() - 1;
      int l = blockPos2.getY() - 1;
      int m = blockPos2.getZ() - 1;
      BlockBox blockBox = new BlockBox(0, 0, 0, 0, 0, 0);
      switch(blockRotation) {
      case COUNTERCLOCKWISE_90:
         blockBox = new BlockBox(i - j, 0, i + j - m, i - j + k, l, i + j);
         break;
      case CLOCKWISE_90:
         blockBox = new BlockBox(i + j - k, 0, j - i, i + j, l, j - i + m);
         break;
      case CLOCKWISE_180:
         blockBox = new BlockBox(i + i - k, 0, j + j - m, i + i, l, j + j);
         break;
      case NONE:
         blockBox = new BlockBox(0, 0, 0, k, l, m);
      }

      switch(blockMirror) {
      case LEFT_RIGHT:
         this.method_16186(blockRotation, m, k, blockBox, Direction.NORTH, Direction.SOUTH);
         break;
      case FRONT_BACK:
         this.method_16186(blockRotation, k, m, blockBox, Direction.WEST, Direction.EAST);
      case NONE:
      }

      blockBox.offset(pos.getX(), pos.getY(), pos.getZ());
      return blockBox;
   }

   private void method_16186(BlockRotation blockRotation, int i, int j, BlockBox blockBox, Direction direction, Direction direction2) {
      BlockPos blockPos = BlockPos.ORIGIN;
      if (blockRotation != BlockRotation.CLOCKWISE_90 && blockRotation != BlockRotation.COUNTERCLOCKWISE_90) {
         if (blockRotation == BlockRotation.CLOCKWISE_180) {
            blockPos = blockPos.offset(direction2, i);
         } else {
            blockPos = blockPos.offset(direction, i);
         }
      } else {
         blockPos = blockPos.offset(blockRotation.rotate(direction), j);
      }

      blockBox.offset(blockPos.getX(), 0, blockPos.getZ());
   }

   public CompoundTag toTag(CompoundTag tag) {
      if (this.blocks.isEmpty()) {
         tag.put("blocks", new ListTag());
         tag.put("palette", new ListTag());
      } else {
         List<Structure.class_3500> list = Lists.newArrayList();
         Structure.class_3500 lv = new Structure.class_3500();
         list.add(lv);

         for(int i = 1; i < this.blocks.size(); ++i) {
            list.add(new Structure.class_3500());
         }

         ListTag listTag = new ListTag();
         List<Structure.StructureBlockInfo> list2 = (List)this.blocks.get(0);

         for(int j = 0; j < list2.size(); ++j) {
            Structure.StructureBlockInfo structureBlockInfo = (Structure.StructureBlockInfo)list2.get(j);
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.put("pos", this.createIntListTag(structureBlockInfo.pos.getX(), structureBlockInfo.pos.getY(), structureBlockInfo.pos.getZ()));
            int k = lv.method_15187(structureBlockInfo.state);
            compoundTag.putInt("state", k);
            if (structureBlockInfo.tag != null) {
               compoundTag.put("nbt", structureBlockInfo.tag);
            }

            listTag.add(compoundTag);

            for(int l = 1; l < this.blocks.size(); ++l) {
               Structure.class_3500 lv2 = (Structure.class_3500)list.get(l);
               lv2.method_15186(((Structure.StructureBlockInfo)((List)this.blocks.get(l)).get(j)).state, k);
            }
         }

         tag.put("blocks", listTag);
         ListTag listTag3;
         Iterator var18;
         if (list.size() == 1) {
            listTag3 = new ListTag();
            var18 = lv.iterator();

            while(var18.hasNext()) {
               BlockState blockState = (BlockState)var18.next();
               listTag3.add(NbtHelper.fromBlockState(blockState));
            }

            tag.put("palette", listTag3);
         } else {
            listTag3 = new ListTag();
            var18 = list.iterator();

            while(var18.hasNext()) {
               Structure.class_3500 lv3 = (Structure.class_3500)var18.next();
               ListTag listTag4 = new ListTag();
               Iterator var22 = lv3.iterator();

               while(var22.hasNext()) {
                  BlockState blockState2 = (BlockState)var22.next();
                  listTag4.add(NbtHelper.fromBlockState(blockState2));
               }

               listTag3.add(listTag4);
            }

            tag.put("palettes", listTag3);
         }
      }

      ListTag listTag5 = new ListTag();

      CompoundTag compoundTag2;
      for(Iterator var13 = this.entities.iterator(); var13.hasNext(); listTag5.add(compoundTag2)) {
         Structure.StructureEntityInfo structureEntityInfo = (Structure.StructureEntityInfo)var13.next();
         compoundTag2 = new CompoundTag();
         compoundTag2.put("pos", this.createDoubleListTag(structureEntityInfo.pos.x, structureEntityInfo.pos.y, structureEntityInfo.pos.z));
         compoundTag2.put("blockPos", this.createIntListTag(structureEntityInfo.blockPos.getX(), structureEntityInfo.blockPos.getY(), structureEntityInfo.blockPos.getZ()));
         if (structureEntityInfo.tag != null) {
            compoundTag2.put("nbt", structureEntityInfo.tag);
         }
      }

      tag.put("entities", listTag5);
      tag.put("size", this.createIntListTag(this.size.getX(), this.size.getY(), this.size.getZ()));
      tag.putInt("DataVersion", SharedConstants.getGameVersion().getWorldVersion());
      return tag;
   }

   public void fromTag(CompoundTag tag) {
      this.blocks.clear();
      this.entities.clear();
      ListTag listTag = tag.getList("size", 3);
      this.size = new BlockPos(listTag.getInt(0), listTag.getInt(1), listTag.getInt(2));
      ListTag listTag2 = tag.getList("blocks", 10);
      ListTag listTag4;
      int j;
      if (tag.contains("palettes", 9)) {
         listTag4 = tag.getList("palettes", 9);

         for(j = 0; j < listTag4.size(); ++j) {
            this.method_15177(listTag4.getList(j), listTag2);
         }
      } else {
         this.method_15177(tag.getList("palette", 10), listTag2);
      }

      listTag4 = tag.getList("entities", 10);

      for(j = 0; j < listTag4.size(); ++j) {
         CompoundTag compoundTag = listTag4.getCompound(j);
         ListTag listTag5 = compoundTag.getList("pos", 6);
         Vec3d vec3d = new Vec3d(listTag5.getDouble(0), listTag5.getDouble(1), listTag5.getDouble(2));
         ListTag listTag6 = compoundTag.getList("blockPos", 3);
         BlockPos blockPos = new BlockPos(listTag6.getInt(0), listTag6.getInt(1), listTag6.getInt(2));
         if (compoundTag.contains("nbt")) {
            CompoundTag compoundTag2 = compoundTag.getCompound("nbt");
            this.entities.add(new Structure.StructureEntityInfo(vec3d, blockPos, compoundTag2));
         }
      }

   }

   private void method_15177(ListTag listTag, ListTag listTag2) {
      Structure.class_3500 lv = new Structure.class_3500();
      List<Structure.StructureBlockInfo> list = Lists.newArrayList();

      int j;
      for(j = 0; j < listTag.size(); ++j) {
         lv.method_15186(NbtHelper.toBlockState(listTag.getCompound(j)), j);
      }

      for(j = 0; j < listTag2.size(); ++j) {
         CompoundTag compoundTag = listTag2.getCompound(j);
         ListTag listTag3 = compoundTag.getList("pos", 3);
         BlockPos blockPos = new BlockPos(listTag3.getInt(0), listTag3.getInt(1), listTag3.getInt(2));
         BlockState blockState = lv.method_15185(compoundTag.getInt("state"));
         CompoundTag compoundTag3;
         if (compoundTag.contains("nbt")) {
            compoundTag3 = compoundTag.getCompound("nbt");
         } else {
            compoundTag3 = null;
         }

         list.add(new Structure.StructureBlockInfo(blockPos, blockState, compoundTag3));
      }

      list.sort(Comparator.comparingInt((structureBlockInfo) -> {
         return structureBlockInfo.pos.getY();
      }));
      this.blocks.add(list);
   }

   private ListTag createIntListTag(int... is) {
      ListTag listTag = new ListTag();
      int[] var3 = is;
      int var4 = is.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         int i = var3[var5];
         listTag.add(IntTag.of(i));
      }

      return listTag;
   }

   private ListTag createDoubleListTag(double... ds) {
      ListTag listTag = new ListTag();
      double[] var3 = ds;
      int var4 = ds.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         double d = var3[var5];
         listTag.add(DoubleTag.of(d));
      }

      return listTag;
   }

   public static class StructureEntityInfo {
      public final Vec3d pos;
      public final BlockPos blockPos;
      public final CompoundTag tag;

      public StructureEntityInfo(Vec3d pos, BlockPos blockPos, CompoundTag tag) {
         this.pos = pos;
         this.blockPos = blockPos;
         this.tag = tag;
      }
   }

   public static class StructureBlockInfo {
      public final BlockPos pos;
      public final BlockState state;
      public final CompoundTag tag;

      public StructureBlockInfo(BlockPos pos, BlockState state, @Nullable CompoundTag tag) {
         this.pos = pos;
         this.state = state;
         this.tag = tag;
      }

      public String toString() {
         return String.format("<StructureBlockInfo | %s | %s | %s>", this.pos, this.state, this.tag);
      }
   }

   static class class_3500 implements Iterable<BlockState> {
      public static final BlockState field_15590;
      private final IdList<BlockState> field_15591;
      private int field_15592;

      private class_3500() {
         this.field_15591 = new IdList(16);
      }

      public int method_15187(BlockState blockState) {
         int i = this.field_15591.getId(blockState);
         if (i == -1) {
            i = this.field_15592++;
            this.field_15591.set(blockState, i);
         }

         return i;
      }

      @Nullable
      public BlockState method_15185(int i) {
         BlockState blockState = (BlockState)this.field_15591.get(i);
         return blockState == null ? field_15590 : blockState;
      }

      public Iterator<BlockState> iterator() {
         return this.field_15591.iterator();
      }

      public void method_15186(BlockState blockState, int i) {
         this.field_15591.set(blockState, i);
      }

      static {
         field_15590 = Blocks.AIR.getDefaultState();
      }
   }
}
