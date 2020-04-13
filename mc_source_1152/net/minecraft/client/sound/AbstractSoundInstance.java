package net.minecraft.client.sound;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public abstract class AbstractSoundInstance implements SoundInstance {
   protected Sound sound;
   @Nullable
   private WeightedSoundSet soundSet;
   protected final SoundCategory category;
   protected final Identifier id;
   protected float volume;
   protected float pitch;
   protected float x;
   protected float y;
   protected float z;
   protected boolean repeat;
   protected int repeatDelay;
   protected SoundInstance.AttenuationType attenuationType;
   protected boolean field_18935;
   protected boolean looping;

   protected AbstractSoundInstance(SoundEvent sound, SoundCategory category) {
      this(sound.getId(), category);
   }

   protected AbstractSoundInstance(Identifier soundId, SoundCategory category) {
      this.volume = 1.0F;
      this.pitch = 1.0F;
      this.attenuationType = SoundInstance.AttenuationType.LINEAR;
      this.id = soundId;
      this.category = category;
   }

   public Identifier getId() {
      return this.id;
   }

   public WeightedSoundSet getSoundSet(SoundManager soundManager) {
      this.soundSet = soundManager.get(this.id);
      if (this.soundSet == null) {
         this.sound = SoundManager.MISSING_SOUND;
      } else {
         this.sound = this.soundSet.getSound();
      }

      return this.soundSet;
   }

   public Sound getSound() {
      return this.sound;
   }

   public SoundCategory getCategory() {
      return this.category;
   }

   public boolean isRepeatable() {
      return this.repeat;
   }

   public int getRepeatDelay() {
      return this.repeatDelay;
   }

   public float getVolume() {
      return this.volume * this.sound.getVolume();
   }

   public float getPitch() {
      return this.pitch * this.sound.getPitch();
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public float getZ() {
      return this.z;
   }

   public SoundInstance.AttenuationType getAttenuationType() {
      return this.attenuationType;
   }

   public boolean isLooping() {
      return this.looping;
   }
}
