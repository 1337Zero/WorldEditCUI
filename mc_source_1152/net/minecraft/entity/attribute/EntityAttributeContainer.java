package net.minecraft.entity.attribute;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.LowercaseMap;

public class EntityAttributeContainer extends AbstractEntityAttributeContainer {
   private final Set<EntityAttributeInstance> trackedAttributes = Sets.newHashSet();
   protected final Map<String, EntityAttributeInstance> instancesByName = new LowercaseMap();

   public EntityAttributeInstanceImpl get(EntityAttribute entityAttribute) {
      return (EntityAttributeInstanceImpl)super.get(entityAttribute);
   }

   public EntityAttributeInstanceImpl get(String string) {
      EntityAttributeInstance entityAttributeInstance = super.get(string);
      if (entityAttributeInstance == null) {
         entityAttributeInstance = (EntityAttributeInstance)this.instancesByName.get(string);
      }

      return (EntityAttributeInstanceImpl)entityAttributeInstance;
   }

   public EntityAttributeInstance register(EntityAttribute attribute) {
      EntityAttributeInstance entityAttributeInstance = super.register(attribute);
      if (attribute instanceof ClampedEntityAttribute && ((ClampedEntityAttribute)attribute).getName() != null) {
         this.instancesByName.put(((ClampedEntityAttribute)attribute).getName(), entityAttributeInstance);
      }

      return entityAttributeInstance;
   }

   protected EntityAttributeInstance createInstance(EntityAttribute attribute) {
      return new EntityAttributeInstanceImpl(this, attribute);
   }

   public void add(EntityAttributeInstance instance) {
      if (instance.getAttribute().isTracked()) {
         this.trackedAttributes.add(instance);
      }

      Iterator var2 = this.attributeHierarchy.get(instance.getAttribute()).iterator();

      while(var2.hasNext()) {
         EntityAttribute entityAttribute = (EntityAttribute)var2.next();
         EntityAttributeInstanceImpl entityAttributeInstanceImpl = this.get(entityAttribute);
         if (entityAttributeInstanceImpl != null) {
            entityAttributeInstanceImpl.invalidateCache();
         }
      }

   }

   public Set<EntityAttributeInstance> getTrackedAttributes() {
      return this.trackedAttributes;
   }

   public Collection<EntityAttributeInstance> buildTrackedAttributesCollection() {
      Set<EntityAttributeInstance> set = Sets.newHashSet();
      Iterator var2 = this.values().iterator();

      while(var2.hasNext()) {
         EntityAttributeInstance entityAttributeInstance = (EntityAttributeInstance)var2.next();
         if (entityAttributeInstance.getAttribute().isTracked()) {
            set.add(entityAttributeInstance);
         }
      }

      return set;
   }
}
