package net.minecraft.world.border;

public interface WorldBorderListener {
   void onSizeChange(WorldBorder worldBorder, double d);

   void onInterpolateSize(WorldBorder border, double fromSize, double toSize, long time);

   void onCenterChanged(WorldBorder centerX, double centerZ, double d);

   void onWarningTimeChanged(WorldBorder warningTime, int i);

   void onWarningBlocksChanged(WorldBorder warningBlocks, int i);

   void onDamagePerBlockChanged(WorldBorder damagePerBlock, double d);

   void onSafeZoneChanged(WorldBorder safeZoneRadius, double d);

   public static class WorldBorderSyncer implements WorldBorderListener {
      private final WorldBorder border;

      public WorldBorderSyncer(WorldBorder border) {
         this.border = border;
      }

      public void onSizeChange(WorldBorder worldBorder, double d) {
         this.border.setSize(d);
      }

      public void onInterpolateSize(WorldBorder border, double fromSize, double toSize, long time) {
         this.border.interpolateSize(fromSize, toSize, time);
      }

      public void onCenterChanged(WorldBorder centerX, double centerZ, double d) {
         this.border.setCenter(centerZ, d);
      }

      public void onWarningTimeChanged(WorldBorder warningTime, int i) {
         this.border.setWarningTime(i);
      }

      public void onWarningBlocksChanged(WorldBorder warningBlocks, int i) {
         this.border.setWarningBlocks(i);
      }

      public void onDamagePerBlockChanged(WorldBorder damagePerBlock, double d) {
         this.border.setDamagePerBlock(d);
      }

      public void onSafeZoneChanged(WorldBorder safeZoneRadius, double d) {
         this.border.setBuffer(d);
      }
   }
}
