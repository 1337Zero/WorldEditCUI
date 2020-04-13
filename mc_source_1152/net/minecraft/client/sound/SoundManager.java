package net.minecraft.client.sound;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloadListener;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class SoundManager extends SinglePreparationResourceReloadListener<SoundManager.SoundList> {
   public static final Sound MISSING_SOUND;
   private static final Logger LOGGER;
   private static final Gson GSON;
   private static final ParameterizedType TYPE;
   private final Map<Identifier, WeightedSoundSet> sounds = Maps.newHashMap();
   private final SoundSystem soundSystem;

   public SoundManager(ResourceManager resourceManager, GameOptions gameOptions) {
      this.soundSystem = new SoundSystem(this, gameOptions, resourceManager);
   }

   protected SoundManager.SoundList prepare(ResourceManager resourceManager, Profiler profiler) {
      SoundManager.SoundList soundList = new SoundManager.SoundList();
      profiler.startTick();

      for(Iterator var4 = resourceManager.getAllNamespaces().iterator(); var4.hasNext(); profiler.pop()) {
         String string = (String)var4.next();
         profiler.push(string);

         try {
            List<Resource> list = resourceManager.getAllResources(new Identifier(string, "sounds.json"));

            for(Iterator var7 = list.iterator(); var7.hasNext(); profiler.pop()) {
               Resource resource = (Resource)var7.next();
               profiler.push(resource.getResourcePackName());

               try {
                  profiler.push("parse");
                  Map<String, SoundEntry> map = readSounds(resource.getInputStream());
                  profiler.swap("register");
                  Iterator var10 = map.entrySet().iterator();

                  while(var10.hasNext()) {
                     Entry<String, SoundEntry> entry = (Entry)var10.next();
                     soundList.register(new Identifier(string, (String)entry.getKey()), (SoundEntry)entry.getValue(), resourceManager);
                  }

                  profiler.pop();
               } catch (RuntimeException var12) {
                  LOGGER.warn("Invalid sounds.json in resourcepack: '{}'", resource.getResourcePackName(), var12);
               }
            }
         } catch (IOException var13) {
         }
      }

      profiler.endTick();
      return soundList;
   }

   protected void apply(SoundManager.SoundList soundList, ResourceManager resourceManager, Profiler profiler) {
      soundList.addTo(this.sounds, this.soundSystem);
      Iterator var4 = this.sounds.keySet().iterator();

      Identifier identifier2;
      while(var4.hasNext()) {
         identifier2 = (Identifier)var4.next();
         WeightedSoundSet weightedSoundSet = (WeightedSoundSet)this.sounds.get(identifier2);
         if (weightedSoundSet.getSubtitle() instanceof TranslatableText) {
            String string = ((TranslatableText)weightedSoundSet.getSubtitle()).getKey();
            if (!I18n.hasTranslation(string)) {
               LOGGER.debug("Missing subtitle {} for event: {}", string, identifier2);
            }
         }
      }

      if (LOGGER.isDebugEnabled()) {
         var4 = this.sounds.keySet().iterator();

         while(var4.hasNext()) {
            identifier2 = (Identifier)var4.next();
            if (!Registry.SOUND_EVENT.containsId(identifier2)) {
               LOGGER.debug("Not having sound event for: {}", identifier2);
            }
         }
      }

      this.soundSystem.reloadSounds();
   }

   @Nullable
   protected static Map<String, SoundEntry> readSounds(InputStream inputStream) {
      Map var1;
      try {
         var1 = (Map)JsonHelper.deserialize(GSON, (Reader)(new InputStreamReader(inputStream, StandardCharsets.UTF_8)), (Type)TYPE);
      } finally {
         IOUtils.closeQuietly(inputStream);
      }

      return var1;
   }

   private static boolean isSoundResourcePresent(Sound sound, Identifier identifier, ResourceManager resourceManager) {
      Identifier identifier2 = sound.getLocation();
      if (!resourceManager.containsResource(identifier2)) {
         LOGGER.warn("File {} does not exist, cannot add it to event {}", identifier2, identifier);
         return false;
      } else {
         return true;
      }
   }

   @Nullable
   public WeightedSoundSet get(Identifier identifier) {
      return (WeightedSoundSet)this.sounds.get(identifier);
   }

   public Collection<Identifier> getKeys() {
      return this.sounds.keySet();
   }

   public void playNextTick(TickableSoundInstance sound) {
      this.soundSystem.playNextTick(sound);
   }

   public void play(SoundInstance sound) {
      this.soundSystem.play(sound);
   }

   public void play(SoundInstance sound, int delay) {
      this.soundSystem.play(sound, delay);
   }

   public void updateListenerPosition(Camera camera) {
      this.soundSystem.updateListenerPosition(camera);
   }

   public void pauseAll() {
      this.soundSystem.pauseAll();
   }

   public void stopAll() {
      this.soundSystem.stopAll();
   }

   public void close() {
      this.soundSystem.stop();
   }

   public void tick(boolean bl) {
      this.soundSystem.tick(bl);
   }

   public void resumeAll() {
      this.soundSystem.resumeAll();
   }

   public void updateSoundVolume(SoundCategory category, float volume) {
      if (category == SoundCategory.MASTER && volume <= 0.0F) {
         this.stopAll();
      }

      this.soundSystem.updateSoundVolume(category, volume);
   }

   public void stop(SoundInstance soundInstance) {
      this.soundSystem.stop(soundInstance);
   }

   public boolean isPlaying(SoundInstance soundInstance) {
      return this.soundSystem.isPlaying(soundInstance);
   }

   public void registerListener(ListenerSoundInstance listenerSoundInstance) {
      this.soundSystem.registerListener(listenerSoundInstance);
   }

   public void unregisterListener(ListenerSoundInstance listenerSoundInstance) {
      this.soundSystem.unregisterListener(listenerSoundInstance);
   }

   public void stopSounds(@Nullable Identifier identifier, @Nullable SoundCategory soundCategory) {
      this.soundSystem.stopSounds(identifier, soundCategory);
   }

   public String getDebugString() {
      return this.soundSystem.getDebugString();
   }

   static {
      MISSING_SOUND = new Sound("meta:missing_sound", 1.0F, 1.0F, 1, Sound.RegistrationType.FILE, false, false, 16);
      LOGGER = LogManager.getLogger();
      GSON = (new GsonBuilder()).registerTypeHierarchyAdapter(Text.class, new Text.Serializer()).registerTypeAdapter(SoundEntry.class, new SoundEntryDeserializer()).create();
      TYPE = new ParameterizedType() {
         public Type[] getActualTypeArguments() {
            return new Type[]{String.class, SoundEntry.class};
         }

         public Type getRawType() {
            return Map.class;
         }

         public Type getOwnerType() {
            return null;
         }
      };
   }

   @Environment(EnvType.CLIENT)
   public static class SoundList {
      private final Map<Identifier, WeightedSoundSet> loadedSounds = Maps.newHashMap();

      protected SoundList() {
      }

      private void register(Identifier id, SoundEntry entry, ResourceManager resourceManager) {
         WeightedSoundSet weightedSoundSet = (WeightedSoundSet)this.loadedSounds.get(id);
         boolean bl = weightedSoundSet == null;
         if (bl || entry.canReplace()) {
            if (!bl) {
               SoundManager.LOGGER.debug("Replaced sound event location {}", id);
            }

            weightedSoundSet = new WeightedSoundSet(id, entry.getSubtitle());
            this.loadedSounds.put(id, weightedSoundSet);
         }

         Iterator var6 = entry.getSounds().iterator();

         while(var6.hasNext()) {
            final Sound sound = (Sound)var6.next();
            final Identifier identifier = sound.getIdentifier();
            Object soundContainer3;
            switch(sound.getRegistrationType()) {
            case FILE:
               if (!SoundManager.isSoundResourcePresent(sound, id, resourceManager)) {
                  continue;
               }

               soundContainer3 = sound;
               break;
            case SOUND_EVENT:
               soundContainer3 = new SoundContainer<Sound>() {
                  public int getWeight() {
                     WeightedSoundSet weightedSoundSet = (WeightedSoundSet)SoundList.this.loadedSounds.get(identifier);
                     return weightedSoundSet == null ? 0 : weightedSoundSet.getWeight();
                  }

                  public Sound getSound() {
                     WeightedSoundSet weightedSoundSet = (WeightedSoundSet)SoundList.this.loadedSounds.get(identifier);
                     if (weightedSoundSet == null) {
                        return SoundManager.MISSING_SOUND;
                     } else {
                        Sound soundx = weightedSoundSet.getSound();
                        return new Sound(soundx.getIdentifier().toString(), soundx.getVolume() * sound.getVolume(), soundx.getPitch() * sound.getPitch(), sound.getWeight(), Sound.RegistrationType.FILE, soundx.isStreamed() || sound.isStreamed(), soundx.isPreloaded(), soundx.getAttenuation());
                     }
                  }

                  public void preload(SoundSystem soundSystem) {
                     WeightedSoundSet weightedSoundSet = (WeightedSoundSet)SoundList.this.loadedSounds.get(identifier);
                     if (weightedSoundSet != null) {
                        weightedSoundSet.preload(soundSystem);
                     }
                  }
               };
               break;
            default:
               throw new IllegalStateException("Unknown SoundEventRegistration type: " + sound.getRegistrationType());
            }

            weightedSoundSet.add((SoundContainer)soundContainer3);
         }

      }

      public void addTo(Map<Identifier, WeightedSoundSet> map, SoundSystem soundSystem) {
         map.clear();
         Iterator var3 = this.loadedSounds.entrySet().iterator();

         while(var3.hasNext()) {
            Entry<Identifier, WeightedSoundSet> entry = (Entry)var3.next();
            map.put(entry.getKey(), entry.getValue());
            ((WeightedSoundSet)entry.getValue()).preload(soundSystem);
         }

      }
   }
}
