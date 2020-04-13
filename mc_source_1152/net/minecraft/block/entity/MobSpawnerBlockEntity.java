package net.minecraft.block.entity;

import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.packet.BlockEntityUpdateS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerEntry;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;

public class MobSpawnerBlockEntity extends BlockEntity implements Tickable {
   private final MobSpawnerLogic logic = new MobSpawnerLogic() {
      public void sendStatus(int status) {
         MobSpawnerBlockEntity.this.world.addBlockAction(MobSpawnerBlockEntity.this.pos, Blocks.SPAWNER, status, 0);
      }

      public World getWorld() {
         return MobSpawnerBlockEntity.this.world;
      }

      public BlockPos getPos() {
         return MobSpawnerBlockEntity.this.pos;
      }

      public void setSpawnEntry(MobSpawnerEntry spawnEntry) {
         super.setSpawnEntry(spawnEntry);
         if (this.getWorld() != null) {
            BlockState blockState = this.getWorld().getBlockState(this.getPos());
            this.getWorld().updateListeners(MobSpawnerBlockEntity.this.pos, blockState, blockState, 4);
         }

      }
   };

   public MobSpawnerBlockEntity() {
      super(BlockEntityType.MOB_SPAWNER);
   }

   public void fromTag(CompoundTag tag) {
      super.fromTag(tag);
      this.logic.deserialize(tag);
   }

   public CompoundTag toTag(CompoundTag tag) {
      super.toTag(tag);
      this.logic.serialize(tag);
      return tag;
   }

   public void tick() {
      this.logic.update();
   }

   @Nullable
   public BlockEntityUpdateS2CPacket toUpdatePacket() {
      return new BlockEntityUpdateS2CPacket(this.pos, 1, this.toInitialChunkDataTag());
   }

   public CompoundTag toInitialChunkDataTag() {
      CompoundTag compoundTag = this.toTag(new CompoundTag());
      compoundTag.remove("SpawnPotentials");
      return compoundTag;
   }

   public boolean onBlockAction(int i, int j) {
      return this.logic.method_8275(i) ? true : super.onBlockAction(i, j);
   }

   public boolean shouldNotCopyTagFromItem() {
      return true;
   }

   public MobSpawnerLogic getLogic() {
      return this.logic;
   }
}
