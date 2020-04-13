package net.minecraft.tag;

import java.util.Collection;
import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

public class EntityTypeTags {
   private static TagContainer<EntityType<?>> container = new TagContainer((identifier) -> {
      return Optional.empty();
   }, "", false, "");
   private static int latestVersion;
   public static final Tag<EntityType<?>> SKELETONS = register("skeletons");
   public static final Tag<EntityType<?>> RAIDERS = register("raiders");
   public static final Tag<EntityType<?>> BEEHIVE_INHABITORS = register("beehive_inhabitors");
   public static final Tag<EntityType<?>> ARROWS = register("arrows");

   public static void setContainer(TagContainer<EntityType<?>> container) {
      EntityTypeTags.container = container;
      ++latestVersion;
   }

   public static TagContainer<EntityType<?>> getContainer() {
      return container;
   }

   private static Tag<EntityType<?>> register(String id) {
      return new EntityTypeTags.CachingTag(new Identifier(id));
   }

   public static class CachingTag extends Tag<EntityType<?>> {
      private int version = -1;
      private Tag<EntityType<?>> delegate;

      public CachingTag(Identifier identifier) {
         super(identifier);
      }

      public boolean contains(EntityType<?> entityType) {
         if (this.version != EntityTypeTags.latestVersion) {
            this.delegate = EntityTypeTags.container.getOrCreate(this.getId());
            this.version = EntityTypeTags.latestVersion;
         }

         return this.delegate.contains(entityType);
      }

      public Collection<EntityType<?>> values() {
         if (this.version != EntityTypeTags.latestVersion) {
            this.delegate = EntityTypeTags.container.getOrCreate(this.getId());
            this.version = EntityTypeTags.latestVersion;
         }

         return this.delegate.values();
      }

      public Collection<Tag.Entry<EntityType<?>>> entries() {
         if (this.version != EntityTypeTags.latestVersion) {
            this.delegate = EntityTypeTags.container.getOrCreate(this.getId());
            this.version = EntityTypeTags.latestVersion;
         }

         return this.delegate.entries();
      }
   }
}
