package net.minecraft.world.gen.foliage;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.util.registry.Registry;

public class FoliagePlacerType<P extends FoliagePlacer> {
   public static final FoliagePlacerType<BlobFoliagePlacer> BLOB_FOLIAGE_PLACER = register("blob_foliage_placer", BlobFoliagePlacer::new);
   public static final FoliagePlacerType<SpruceFoliagePlacer> SPRUCE_FOLIAGE_PLACER = register("spruce_foliage_placer", SpruceFoliagePlacer::new);
   public static final FoliagePlacerType<PineFoliagePlacer> PINE_FOLIAGE_PLACER = register("pine_foliage_placer", PineFoliagePlacer::new);
   public static final FoliagePlacerType<AcaciaFoliagePlacer> ACACIA_FOLIAGE_PLACER = register("acacia_foliage_placer", AcaciaFoliagePlacer::new);
   private final Function<Dynamic<?>, P> deserializer;

   private static <P extends FoliagePlacer> FoliagePlacerType<P> register(String id, Function<Dynamic<?>, P> deserializer) {
      return (FoliagePlacerType)Registry.register(Registry.FOLIAGE_PLACER_TYPE, (String)id, new FoliagePlacerType(deserializer));
   }

   private FoliagePlacerType(Function<Dynamic<?>, P> deserializer) {
      this.deserializer = deserializer;
   }

   public P deserialize(Dynamic<?> dynamic) {
      return (FoliagePlacer)this.deserializer.apply(dynamic);
   }
}
