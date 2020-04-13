package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.timer.Timer;

public class UnmodifiableLevelProperties extends LevelProperties {
   private final LevelProperties properties;

   public UnmodifiableLevelProperties(LevelProperties levelProperties) {
      this.properties = levelProperties;
   }

   public CompoundTag cloneWorldTag(@Nullable CompoundTag playerTag) {
      return this.properties.cloneWorldTag(playerTag);
   }

   public long getSeed() {
      return this.properties.getSeed();
   }

   public int getSpawnX() {
      return this.properties.getSpawnX();
   }

   public int getSpawnY() {
      return this.properties.getSpawnY();
   }

   public int getSpawnZ() {
      return this.properties.getSpawnZ();
   }

   public long getTime() {
      return this.properties.getTime();
   }

   public long getTimeOfDay() {
      return this.properties.getTimeOfDay();
   }

   public CompoundTag getPlayerData() {
      return this.properties.getPlayerData();
   }

   public String getLevelName() {
      return this.properties.getLevelName();
   }

   public int getVersion() {
      return this.properties.getVersion();
   }

   @Environment(EnvType.CLIENT)
   public long getLastPlayed() {
      return this.properties.getLastPlayed();
   }

   public boolean isThundering() {
      return this.properties.isThundering();
   }

   public int getThunderTime() {
      return this.properties.getThunderTime();
   }

   public boolean isRaining() {
      return this.properties.isRaining();
   }

   public int getRainTime() {
      return this.properties.getRainTime();
   }

   public GameMode getGameMode() {
      return this.properties.getGameMode();
   }

   @Environment(EnvType.CLIENT)
   public void setSpawnX(int spawnX) {
   }

   @Environment(EnvType.CLIENT)
   public void setSpawnY(int spawnY) {
   }

   @Environment(EnvType.CLIENT)
   public void setSpawnZ(int spawnZ) {
   }

   public void setTime(long time) {
   }

   public void setTimeOfDay(long timeOfDay) {
   }

   public void setSpawnPos(BlockPos blockPos) {
   }

   public void setLevelName(String levelName) {
   }

   public void setVersion(int version) {
   }

   public void setThundering(boolean thundering) {
   }

   public void setThunderTime(int thunderTime) {
   }

   public void setRaining(boolean raining) {
   }

   public void setRainTime(int rainTime) {
   }

   public boolean hasStructures() {
      return this.properties.hasStructures();
   }

   public boolean isHardcore() {
      return this.properties.isHardcore();
   }

   public LevelGeneratorType getGeneratorType() {
      return this.properties.getGeneratorType();
   }

   public void setGeneratorType(LevelGeneratorType levelGeneratorType) {
   }

   public boolean areCommandsAllowed() {
      return this.properties.areCommandsAllowed();
   }

   public void setCommandsAllowed(boolean commandsAllowed) {
   }

   public boolean isInitialized() {
      return this.properties.isInitialized();
   }

   public void setInitialized(boolean initialized) {
   }

   public GameRules getGameRules() {
      return this.properties.getGameRules();
   }

   public Difficulty getDifficulty() {
      return this.properties.getDifficulty();
   }

   public void setDifficulty(Difficulty difficulty) {
   }

   public boolean isDifficultyLocked() {
      return this.properties.isDifficultyLocked();
   }

   public void setDifficultyLocked(boolean difficultyLocked) {
   }

   public Timer<MinecraftServer> getScheduledEvents() {
      return this.properties.getScheduledEvents();
   }

   public void setWorldData(DimensionType type, CompoundTag compoundTag) {
      this.properties.setWorldData(type, compoundTag);
   }

   public CompoundTag getWorldData(DimensionType dimensionType) {
      return this.properties.getWorldData(dimensionType);
   }

   public void populateCrashReport(CrashReportSection crashReportSection) {
      crashReportSection.add("Derived", (Object)true);
      this.properties.populateCrashReport(crashReportSection);
   }
}
