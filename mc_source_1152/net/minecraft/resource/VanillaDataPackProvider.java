package net.minecraft.resource;

import java.util.Map;

public class VanillaDataPackProvider implements ResourcePackProvider {
   private final DefaultResourcePack pack = new DefaultResourcePack(new String[]{"minecraft"});

   public <T extends ResourcePackProfile> void register(Map<String, T> registry, ResourcePackProfile.Factory<T> factory) {
      T resourcePackProfile = ResourcePackProfile.of("vanilla", false, () -> {
         return this.pack;
      }, factory, ResourcePackProfile.InsertionPosition.BOTTOM);
      if (resourcePackProfile != null) {
         registry.put("vanilla", resourcePackProfile);
      }

   }
}
