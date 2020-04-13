package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.PackedIntegerArray;

public class LeavesFix extends DataFix {
   private static final int[][] field_5687 = new int[][]{{-1, 0, 0}, {1, 0, 0}, {0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}};
   private static final Object2IntMap<String> field_5688 = (Object2IntMap)DataFixUtils.make(new Object2IntOpenHashMap(), (object2IntOpenHashMap) -> {
      object2IntOpenHashMap.put("minecraft:acacia_leaves", 0);
      object2IntOpenHashMap.put("minecraft:birch_leaves", 1);
      object2IntOpenHashMap.put("minecraft:dark_oak_leaves", 2);
      object2IntOpenHashMap.put("minecraft:jungle_leaves", 3);
      object2IntOpenHashMap.put("minecraft:oak_leaves", 4);
      object2IntOpenHashMap.put("minecraft:spruce_leaves", 5);
   });
   private static final Set<String> field_5686 = ImmutableSet.of("minecraft:acacia_bark", "minecraft:birch_bark", "minecraft:dark_oak_bark", "minecraft:jungle_bark", "minecraft:oak_bark", "minecraft:spruce_bark", new String[]{"minecraft:acacia_log", "minecraft:birch_log", "minecraft:dark_oak_log", "minecraft:jungle_log", "minecraft:oak_log", "minecraft:spruce_log", "minecraft:stripped_acacia_log", "minecraft:stripped_birch_log", "minecraft:stripped_dark_oak_log", "minecraft:stripped_jungle_log", "minecraft:stripped_oak_log", "minecraft:stripped_spruce_log"});

   public LeavesFix(Schema schema, boolean bl) {
      super(schema, bl);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(TypeReferences.CHUNK);
      OpticFinder<?> opticFinder = type.findField("Level");
      OpticFinder<?> opticFinder2 = opticFinder.type().findField("Sections");
      Type<?> type2 = opticFinder2.type();
      if (!(type2 instanceof ListType)) {
         throw new IllegalStateException("Expecting sections to be a list.");
      } else {
         Type<?> type3 = ((ListType)type2).getElement();
         OpticFinder<?> opticFinder3 = DSL.typeFinder(type3);
         return this.fixTypeEverywhereTyped("Leaves fix", type, (typed) -> {
            return typed.updateTyped(opticFinder, (typedx) -> {
               int[] is = new int[]{0};
               Typed<?> typed2 = typedx.updateTyped(opticFinder2, (typed) -> {
                  Int2ObjectMap<LeavesFix.class_1192> int2ObjectMap = new Int2ObjectOpenHashMap((Map)typed.getAllTyped(opticFinder3).stream().map((typedx) -> {
                     return new LeavesFix.class_1192(typedx, this.getInputSchema());
                  }).collect(Collectors.toMap(LeavesFix.ListFixer::method_5077, (arg) -> {
                     return arg;
                  })));
                  if (int2ObjectMap.values().stream().allMatch(LeavesFix.ListFixer::method_5079)) {
                     return typed;
                  } else {
                     List<IntSet> list = Lists.newArrayList();

                     int n;
                     for(n = 0; n < 7; ++n) {
                        list.add(new IntOpenHashSet());
                     }

                     ObjectIterator var25 = int2ObjectMap.values().iterator();

                     while(true) {
                        LeavesFix.class_1192 lv;
                        int l;
                        int m;
                        do {
                           if (!var25.hasNext()) {
                              for(n = 1; n < 7; ++n) {
                                 IntSet intSet = (IntSet)list.get(n - 1);
                                 IntSet intSet2 = (IntSet)list.get(n);
                                 IntIterator intIterator = intSet.iterator();

                                 while(intIterator.hasNext()) {
                                    l = intIterator.nextInt();
                                    m = this.method_5052(l);
                                    int q = this.method_5062(l);
                                    int r = this.method_5050(l);
                                    int[][] var14 = field_5687;
                                    int var15 = var14.length;

                                    for(int var16 = 0; var16 < var15; ++var16) {
                                       int[] js = var14[var16];
                                       int s = m + js[0];
                                       int t = q + js[1];
                                       int u = r + js[2];
                                       if (s >= 0 && s <= 15 && u >= 0 && u <= 15 && t >= 0 && t <= 255) {
                                          LeavesFix.class_1192 lv2 = (LeavesFix.class_1192)int2ObjectMap.get(t >> 4);
                                          if (lv2 != null && !lv2.method_5079()) {
                                             int v = method_5051(s, t & 15, u);
                                             int w = lv2.method_5075(v);
                                             if (lv2.method_5071(w)) {
                                                int x = lv2.method_5065(w);
                                                if (x > n) {
                                                   lv2.method_5070(v, w, n);
                                                   intSet2.add(method_5051(s, t, u));
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }

                              return typed.updateTyped(opticFinder3, (typedx) -> {
                                 return ((LeavesFix.class_1192)int2ObjectMap.get(((Dynamic)typedx.get(DSL.remainderFinder())).get("Y").asInt(0))).method_5083(typedx);
                              });
                           }

                           lv = (LeavesFix.class_1192)var25.next();
                        } while(lv.method_5079());

                        for(int j = 0; j < 4096; ++j) {
                           int k = lv.method_5075(j);
                           if (lv.method_5068(k)) {
                              ((IntSet)list.get(0)).add(lv.method_5077() << 12 | j);
                           } else if (lv.method_5071(k)) {
                              l = this.method_5052(j);
                              m = this.method_5050(j);
                              is[0] |= method_5061(l == 0, l == 15, m == 0, m == 15);
                           }
                        }
                     }
                  }
               });
               if (is[0] != 0) {
                  typed2 = typed2.update(DSL.remainderFinder(), (dynamic) -> {
                     Dynamic<?> dynamic2 = (Dynamic)DataFixUtils.orElse(dynamic.get("UpgradeData").get(), dynamic.emptyMap());
                     return dynamic.set("UpgradeData", dynamic2.set("Sides", dynamic.createByte((byte)(dynamic2.get("Sides").asByte((byte)0) | is[0]))));
                  });
               }

               return typed2;
            });
         });
      }
   }

   public static int method_5051(int i, int j, int k) {
      return j << 8 | k << 4 | i;
   }

   private int method_5052(int i) {
      return i & 15;
   }

   private int method_5062(int i) {
      return i >> 8 & 255;
   }

   private int method_5050(int i) {
      return i >> 4 & 15;
   }

   public static int method_5061(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
      int i = 0;
      if (bl3) {
         if (bl2) {
            i |= 2;
         } else if (bl) {
            i |= 128;
         } else {
            i |= 1;
         }
      } else if (bl4) {
         if (bl) {
            i |= 32;
         } else if (bl2) {
            i |= 8;
         } else {
            i |= 16;
         }
      } else if (bl2) {
         i |= 4;
      } else if (bl) {
         i |= 64;
      }

      return i;
   }

   public static final class class_1192 extends LeavesFix.ListFixer {
      @Nullable
      private IntSet field_5689;
      @Nullable
      private IntSet field_5691;
      @Nullable
      private Int2IntMap field_5690;

      public class_1192(Typed<?> typed, Schema schema) {
         super(typed, schema);
      }

      protected boolean needsFix() {
         this.field_5689 = new IntOpenHashSet();
         this.field_5691 = new IntOpenHashSet();
         this.field_5690 = new Int2IntOpenHashMap();

         for(int i = 0; i < this.data.size(); ++i) {
            Dynamic<?> dynamic = (Dynamic)this.data.get(i);
            String string = dynamic.get("Name").asString("");
            if (LeavesFix.field_5688.containsKey(string)) {
               boolean bl = Objects.equals(dynamic.get("Properties").get("decayable").asString(""), "false");
               this.field_5689.add(i);
               this.field_5690.put(this.method_5082(string, bl, 7), i);
               this.data.set(i, this.method_5072(dynamic, string, bl, 7));
            }

            if (LeavesFix.field_5686.contains(string)) {
               this.field_5691.add(i);
            }
         }

         return this.field_5689.isEmpty() && this.field_5691.isEmpty();
      }

      private Dynamic<?> method_5072(Dynamic<?> dynamic, String string, boolean bl, int i) {
         Dynamic<?> dynamic2 = dynamic.emptyMap();
         dynamic2 = dynamic2.set("persistent", dynamic2.createString(bl ? "true" : "false"));
         dynamic2 = dynamic2.set("distance", dynamic2.createString(Integer.toString(i)));
         Dynamic<?> dynamic3 = dynamic.emptyMap();
         dynamic3 = dynamic3.set("Properties", dynamic2);
         dynamic3 = dynamic3.set("Name", dynamic3.createString(string));
         return dynamic3;
      }

      public boolean method_5068(int i) {
         return this.field_5691.contains(i);
      }

      public boolean method_5071(int i) {
         return this.field_5689.contains(i);
      }

      private int method_5065(int i) {
         return this.method_5068(i) ? 0 : Integer.parseInt(((Dynamic)this.data.get(i)).get("Properties").get("distance").asString(""));
      }

      private void method_5070(int i, int j, int k) {
         Dynamic<?> dynamic = (Dynamic)this.data.get(j);
         String string = dynamic.get("Name").asString("");
         boolean bl = Objects.equals(dynamic.get("Properties").get("persistent").asString(""), "true");
         int l = this.method_5082(string, bl, k);
         int n;
         if (!this.field_5690.containsKey(l)) {
            n = this.data.size();
            this.field_5689.add(n);
            this.field_5690.put(l, n);
            this.data.add(this.method_5072(dynamic, string, bl, k));
         }

         n = this.field_5690.get(l);
         if (1 << this.field_5696.getElementBits() <= n) {
            PackedIntegerArray packedIntegerArray = new PackedIntegerArray(this.field_5696.getElementBits() + 1, 4096);

            for(int o = 0; o < 4096; ++o) {
               packedIntegerArray.set(o, this.field_5696.get(o));
            }

            this.field_5696 = packedIntegerArray;
         }

         this.field_5696.set(i, n);
      }
   }

   public abstract static class ListFixer {
      private final Type<Pair<String, Dynamic<?>>> field_5695;
      protected final OpticFinder<List<Pair<String, Dynamic<?>>>> field_5693;
      protected final List<Dynamic<?>> data;
      protected final int field_5694;
      @Nullable
      protected PackedIntegerArray field_5696;

      public ListFixer(Typed<?> typed, Schema schema) {
         this.field_5695 = DSL.named(TypeReferences.BLOCK_STATE.typeName(), DSL.remainderType());
         this.field_5693 = DSL.fieldFinder("Palette", DSL.list(this.field_5695));
         if (!Objects.equals(schema.getType(TypeReferences.BLOCK_STATE), this.field_5695)) {
            throw new IllegalStateException("Block state type is not what was expected.");
         } else {
            Optional<List<Pair<String, Dynamic<?>>>> optional = typed.getOptional(this.field_5693);
            this.data = (List)optional.map((list) -> {
               return (List)list.stream().map(Pair::getSecond).collect(Collectors.toList());
            }).orElse(ImmutableList.of());
            Dynamic<?> dynamic = (Dynamic)typed.get(DSL.remainderFinder());
            this.field_5694 = dynamic.get("Y").asInt(0);
            this.method_5074(dynamic);
         }
      }

      protected void method_5074(Dynamic<?> dynamic) {
         if (this.needsFix()) {
            this.field_5696 = null;
         } else {
            long[] ls = ((LongStream)dynamic.get("BlockStates").asLongStreamOpt().get()).toArray();
            int i = Math.max(4, DataFixUtils.ceillog2(this.data.size()));
            this.field_5696 = new PackedIntegerArray(i, 4096, ls);
         }

      }

      public Typed<?> method_5083(Typed<?> typed) {
         return this.method_5079() ? typed : typed.update(DSL.remainderFinder(), (dynamic) -> {
            return dynamic.set("BlockStates", dynamic.createLongList(Arrays.stream(this.field_5696.getStorage())));
         }).set(this.field_5693, this.data.stream().map((dynamic) -> {
            return Pair.of(TypeReferences.BLOCK_STATE.typeName(), dynamic);
         }).collect(Collectors.toList()));
      }

      public boolean method_5079() {
         return this.field_5696 == null;
      }

      public int method_5075(int i) {
         return this.field_5696.get(i);
      }

      protected int method_5082(String string, boolean bl, int i) {
         return LeavesFix.field_5688.get(string) << 5 | (bl ? 16 : 0) | i;
      }

      int method_5077() {
         return this.field_5694;
      }

      protected abstract boolean needsFix();
   }
}
