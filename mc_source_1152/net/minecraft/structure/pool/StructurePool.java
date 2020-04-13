package net.minecraft.structure.pool;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.processor.GravityStructureProcessor;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

public class StructurePool {
   public static final StructurePool EMPTY;
   public static final StructurePool INVALID;
   private final Identifier id;
   private final ImmutableList<Pair<StructurePoolElement, Integer>> elementCounts;
   private final List<StructurePoolElement> elements;
   private final Identifier terminatorsId;
   private final StructurePool.Projection projection;
   private int field_18707 = Integer.MIN_VALUE;

   public StructurePool(Identifier id, Identifier terminatorsId, List<Pair<StructurePoolElement, Integer>> elementCounts, StructurePool.Projection projection) {
      this.id = id;
      this.elementCounts = ImmutableList.copyOf(elementCounts);
      this.elements = Lists.newArrayList();
      Iterator var5 = elementCounts.iterator();

      while(var5.hasNext()) {
         Pair<StructurePoolElement, Integer> pair = (Pair)var5.next();

         for(Integer integer = 0; integer < (Integer)pair.getSecond(); integer = integer + 1) {
            this.elements.add(((StructurePoolElement)pair.getFirst()).setProjection(projection));
         }
      }

      this.terminatorsId = terminatorsId;
      this.projection = projection;
   }

   public int method_19309(StructureManager structureManager) {
      if (this.field_18707 == Integer.MIN_VALUE) {
         this.field_18707 = this.elements.stream().mapToInt((structurePoolElement) -> {
            return structurePoolElement.getBoundingBox(structureManager, BlockPos.ORIGIN, BlockRotation.NONE).getBlockCountY();
         }).max().orElse(0);
      }

      return this.field_18707;
   }

   public Identifier getTerminatorsId() {
      return this.terminatorsId;
   }

   public StructurePoolElement getRandomElement(Random random) {
      return (StructurePoolElement)this.elements.get(random.nextInt(this.elements.size()));
   }

   public List<StructurePoolElement> getElementIndicesInRandomOrder(Random random) {
      return ImmutableList.copyOf(ObjectArrays.shuffle(this.elements.toArray(new StructurePoolElement[0]), random));
   }

   public Identifier getId() {
      return this.id;
   }

   public int getElementCount() {
      return this.elements.size();
   }

   static {
      EMPTY = new StructurePool(new Identifier("empty"), new Identifier("empty"), ImmutableList.of(), StructurePool.Projection.RIGID);
      INVALID = new StructurePool(new Identifier("invalid"), new Identifier("invalid"), ImmutableList.of(), StructurePool.Projection.RIGID);
   }

   public static enum Projection {
      TERRAIN_MATCHING("terrain_matching", ImmutableList.of(new GravityStructureProcessor(Heightmap.Type.WORLD_SURFACE_WG, -1))),
      RIGID("rigid", ImmutableList.of());

      private static final Map<String, StructurePool.Projection> PROJECTIONS_BY_ID = (Map)Arrays.stream(values()).collect(Collectors.toMap(StructurePool.Projection::getId, (projection) -> {
         return projection;
      }));
      private final String id;
      private final ImmutableList<StructureProcessor> processors;

      private Projection(String string2, ImmutableList<StructureProcessor> immutableList) {
         this.id = string2;
         this.processors = immutableList;
      }

      public String getId() {
         return this.id;
      }

      public static StructurePool.Projection getById(String id) {
         return (StructurePool.Projection)PROJECTIONS_BY_ID.get(id);
      }

      public ImmutableList<StructureProcessor> getProcessors() {
         return this.processors;
      }
   }
}
