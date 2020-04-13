package net.minecraft.entity.attribute;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class EntityAttributeInstanceImpl implements EntityAttributeInstance {
   private final AbstractEntityAttributeContainer container;
   private final EntityAttribute attribute;
   private final Map<EntityAttributeModifier.Operation, Set<EntityAttributeModifier>> modifiersByOperation = Maps.newEnumMap(EntityAttributeModifier.Operation.class);
   private final Map<String, Set<EntityAttributeModifier>> modifiersByName = Maps.newHashMap();
   private final Map<UUID, EntityAttributeModifier> modifiersByUuid = Maps.newHashMap();
   private double baseValue;
   private boolean needsRefresh = true;
   private double cachedValue;

   public EntityAttributeInstanceImpl(AbstractEntityAttributeContainer container, EntityAttribute attribute) {
      this.container = container;
      this.attribute = attribute;
      this.baseValue = attribute.getDefaultValue();
      EntityAttributeModifier.Operation[] var3 = EntityAttributeModifier.Operation.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         EntityAttributeModifier.Operation operation = var3[var5];
         this.modifiersByOperation.put(operation, Sets.newHashSet());
      }

   }

   public EntityAttribute getAttribute() {
      return this.attribute;
   }

   public double getBaseValue() {
      return this.baseValue;
   }

   public void setBaseValue(double baseValue) {
      if (baseValue != this.getBaseValue()) {
         this.baseValue = baseValue;
         this.invalidateCache();
      }
   }

   public Set<EntityAttributeModifier> getModifiers(EntityAttributeModifier.Operation operation) {
      return (Set)this.modifiersByOperation.get(operation);
   }

   public Set<EntityAttributeModifier> getModifiers() {
      Set<EntityAttributeModifier> set = Sets.newHashSet();
      EntityAttributeModifier.Operation[] var2 = EntityAttributeModifier.Operation.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         EntityAttributeModifier.Operation operation = var2[var4];
         set.addAll(this.getModifiers(operation));
      }

      return set;
   }

   @Nullable
   public EntityAttributeModifier getModifier(UUID uuid) {
      return (EntityAttributeModifier)this.modifiersByUuid.get(uuid);
   }

   public boolean hasModifier(EntityAttributeModifier modifier) {
      return this.modifiersByUuid.get(modifier.getId()) != null;
   }

   public void addModifier(EntityAttributeModifier modifier) {
      if (this.getModifier(modifier.getId()) != null) {
         throw new IllegalArgumentException("Modifier is already applied on this attribute!");
      } else {
         Set<EntityAttributeModifier> set = (Set)this.modifiersByName.computeIfAbsent(modifier.getName(), (string) -> {
            return Sets.newHashSet();
         });
         ((Set)this.modifiersByOperation.get(modifier.getOperation())).add(modifier);
         set.add(modifier);
         this.modifiersByUuid.put(modifier.getId(), modifier);
         this.invalidateCache();
      }
   }

   protected void invalidateCache() {
      this.needsRefresh = true;
      this.container.add(this);
   }

   public void removeModifier(EntityAttributeModifier modifier) {
      EntityAttributeModifier.Operation[] var2 = EntityAttributeModifier.Operation.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         EntityAttributeModifier.Operation operation = var2[var4];
         ((Set)this.modifiersByOperation.get(operation)).remove(modifier);
      }

      Set<EntityAttributeModifier> set = (Set)this.modifiersByName.get(modifier.getName());
      if (set != null) {
         set.remove(modifier);
         if (set.isEmpty()) {
            this.modifiersByName.remove(modifier.getName());
         }
      }

      this.modifiersByUuid.remove(modifier.getId());
      this.invalidateCache();
   }

   public void removeModifier(UUID uuid) {
      EntityAttributeModifier entityAttributeModifier = this.getModifier(uuid);
      if (entityAttributeModifier != null) {
         this.removeModifier(entityAttributeModifier);
      }

   }

   @Environment(EnvType.CLIENT)
   public void clearModifiers() {
      Collection<EntityAttributeModifier> collection = this.getModifiers();
      if (collection != null) {
         Collection<EntityAttributeModifier> collection = Lists.newArrayList(collection);
         Iterator var2 = collection.iterator();

         while(var2.hasNext()) {
            EntityAttributeModifier entityAttributeModifier = (EntityAttributeModifier)var2.next();
            this.removeModifier(entityAttributeModifier);
         }

      }
   }

   public double getValue() {
      if (this.needsRefresh) {
         this.cachedValue = this.computeValue();
         this.needsRefresh = false;
      }

      return this.cachedValue;
   }

   private double computeValue() {
      double d = this.getBaseValue();

      EntityAttributeModifier entityAttributeModifier;
      for(Iterator var3 = this.getAllModifiers(EntityAttributeModifier.Operation.ADDITION).iterator(); var3.hasNext(); d += entityAttributeModifier.getAmount()) {
         entityAttributeModifier = (EntityAttributeModifier)var3.next();
      }

      double e = d;

      Iterator var5;
      EntityAttributeModifier entityAttributeModifier3;
      for(var5 = this.getAllModifiers(EntityAttributeModifier.Operation.MULTIPLY_BASE).iterator(); var5.hasNext(); e += d * entityAttributeModifier3.getAmount()) {
         entityAttributeModifier3 = (EntityAttributeModifier)var5.next();
      }

      for(var5 = this.getAllModifiers(EntityAttributeModifier.Operation.MULTIPLY_TOTAL).iterator(); var5.hasNext(); e *= 1.0D + entityAttributeModifier3.getAmount()) {
         entityAttributeModifier3 = (EntityAttributeModifier)var5.next();
      }

      return this.attribute.clamp(e);
   }

   private Collection<EntityAttributeModifier> getAllModifiers(EntityAttributeModifier.Operation operation) {
      Set<EntityAttributeModifier> set = Sets.newHashSet(this.getModifiers(operation));

      for(EntityAttribute entityAttribute = this.attribute.getParent(); entityAttribute != null; entityAttribute = entityAttribute.getParent()) {
         EntityAttributeInstance entityAttributeInstance = this.container.get(entityAttribute);
         if (entityAttributeInstance != null) {
            set.addAll(entityAttributeInstance.getModifiers(operation));
         }
      }

      return set;
   }
}
