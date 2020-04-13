package net.minecraft.datafixer;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.nbt.AbstractListTag;
import net.minecraft.nbt.AbstractNumberTag;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class NbtOps implements DynamicOps<Tag> {
   public static final NbtOps INSTANCE = new NbtOps();

   protected NbtOps() {
   }

   public Tag empty() {
      return EndTag.INSTANCE;
   }

   public Type<?> getType(Tag tag) {
      switch(tag.getType()) {
      case 0:
         return DSL.nilType();
      case 1:
         return DSL.byteType();
      case 2:
         return DSL.shortType();
      case 3:
         return DSL.intType();
      case 4:
         return DSL.longType();
      case 5:
         return DSL.floatType();
      case 6:
         return DSL.doubleType();
      case 7:
         return DSL.list(DSL.byteType());
      case 8:
         return DSL.string();
      case 9:
         return DSL.list(DSL.remainderType());
      case 10:
         return DSL.compoundList(DSL.remainderType(), DSL.remainderType());
      case 11:
         return DSL.list(DSL.intType());
      case 12:
         return DSL.list(DSL.longType());
      default:
         return DSL.remainderType();
      }
   }

   public Optional<Number> getNumberValue(Tag tag) {
      return tag instanceof AbstractNumberTag ? Optional.of(((AbstractNumberTag)tag).getNumber()) : Optional.empty();
   }

   public Tag createNumeric(Number number) {
      return DoubleTag.of(number.doubleValue());
   }

   public Tag createByte(byte b) {
      return ByteTag.of(b);
   }

   public Tag createShort(short s) {
      return ShortTag.of(s);
   }

   public Tag createInt(int i) {
      return IntTag.of(i);
   }

   public Tag createLong(long l) {
      return LongTag.of(l);
   }

   public Tag createFloat(float f) {
      return FloatTag.of(f);
   }

   public Tag createDouble(double d) {
      return DoubleTag.of(d);
   }

   public Tag createBoolean(boolean bl) {
      return ByteTag.of(bl);
   }

   public Optional<String> getStringValue(Tag tag) {
      return tag instanceof StringTag ? Optional.of(tag.asString()) : Optional.empty();
   }

   public Tag createString(String string) {
      return StringTag.of(string);
   }

   public Tag mergeInto(Tag tag, Tag tag2) {
      if (tag2 instanceof EndTag) {
         return tag;
      } else if (!(tag instanceof CompoundTag)) {
         if (tag instanceof EndTag) {
            throw new IllegalArgumentException("mergeInto called with a null input.");
         } else if (tag instanceof AbstractListTag) {
            AbstractListTag<Tag> abstractListTag3 = new ListTag();
            AbstractListTag<?> abstractListTag2 = (AbstractListTag)tag;
            abstractListTag3.addAll(abstractListTag2);
            abstractListTag3.add(tag2);
            return abstractListTag3;
         } else {
            return tag;
         }
      } else if (!(tag2 instanceof CompoundTag)) {
         return tag;
      } else {
         CompoundTag compoundTag = new CompoundTag();
         CompoundTag compoundTag2 = (CompoundTag)tag;
         Iterator var6 = compoundTag2.getKeys().iterator();

         while(var6.hasNext()) {
            String string = (String)var6.next();
            compoundTag.put(string, compoundTag2.get(string));
         }

         CompoundTag compoundTag3 = (CompoundTag)tag2;
         Iterator var11 = compoundTag3.getKeys().iterator();

         while(var11.hasNext()) {
            String string2 = (String)var11.next();
            compoundTag.put(string2, compoundTag3.get(string2));
         }

         return compoundTag;
      }
   }

   public Tag mergeInto(Tag tag, Tag tag2, Tag tag3) {
      CompoundTag compoundTag3;
      if (tag instanceof EndTag) {
         compoundTag3 = new CompoundTag();
      } else {
         if (!(tag instanceof CompoundTag)) {
            return tag;
         }

         CompoundTag compoundTag2 = (CompoundTag)tag;
         compoundTag3 = new CompoundTag();
         compoundTag2.getKeys().forEach((string) -> {
            compoundTag3.put(string, compoundTag2.get(string));
         });
      }

      compoundTag3.put(tag2.asString(), tag3);
      return compoundTag3;
   }

   public Tag merge(Tag tag, Tag tag2) {
      if (tag instanceof EndTag) {
         return tag2;
      } else if (tag2 instanceof EndTag) {
         return tag;
      } else {
         if (tag instanceof CompoundTag && tag2 instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            CompoundTag compoundTag2 = (CompoundTag)tag2;
            CompoundTag compoundTag3 = new CompoundTag();
            compoundTag.getKeys().forEach((string) -> {
               compoundTag3.put(string, compoundTag.get(string));
            });
            compoundTag2.getKeys().forEach((string) -> {
               compoundTag3.put(string, compoundTag2.get(string));
            });
         }

         if (tag instanceof AbstractListTag && tag2 instanceof AbstractListTag) {
            ListTag listTag = new ListTag();
            listTag.addAll((AbstractListTag)tag);
            listTag.addAll((AbstractListTag)tag2);
            return listTag;
         } else {
            throw new IllegalArgumentException("Could not merge " + tag + " and " + tag2);
         }
      }
   }

   public Optional<Map<Tag, Tag>> getMapValues(Tag tag) {
      if (tag instanceof CompoundTag) {
         CompoundTag compoundTag = (CompoundTag)tag;
         return Optional.of(compoundTag.getKeys().stream().map((string) -> {
            return Pair.of(this.createString(string), compoundTag.get(string));
         }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
      } else {
         return Optional.empty();
      }
   }

   public Tag createMap(Map<Tag, Tag> map) {
      CompoundTag compoundTag = new CompoundTag();
      Iterator var3 = map.entrySet().iterator();

      while(var3.hasNext()) {
         Entry<Tag, Tag> entry = (Entry)var3.next();
         compoundTag.put(((Tag)entry.getKey()).asString(), (Tag)entry.getValue());
      }

      return compoundTag;
   }

   public Optional<Stream<Tag>> getStream(Tag tag) {
      return tag instanceof AbstractListTag ? Optional.of(((AbstractListTag)tag).stream().map((tagx) -> {
         return tagx;
      })) : Optional.empty();
   }

   public Optional<ByteBuffer> getByteBuffer(Tag tag) {
      return tag instanceof ByteArrayTag ? Optional.of(ByteBuffer.wrap(((ByteArrayTag)tag).getByteArray())) : super.getByteBuffer(tag);
   }

   public Tag createByteList(ByteBuffer byteBuffer) {
      return new ByteArrayTag(DataFixUtils.toArray(byteBuffer));
   }

   public Optional<IntStream> getIntStream(Tag tag) {
      return tag instanceof IntArrayTag ? Optional.of(Arrays.stream(((IntArrayTag)tag).getIntArray())) : super.getIntStream(tag);
   }

   public Tag createIntList(IntStream intStream) {
      return new IntArrayTag(intStream.toArray());
   }

   public Optional<LongStream> getLongStream(Tag tag) {
      return tag instanceof LongArrayTag ? Optional.of(Arrays.stream(((LongArrayTag)tag).getLongArray())) : super.getLongStream(tag);
   }

   public Tag createLongList(LongStream longStream) {
      return new LongArrayTag(longStream.toArray());
   }

   public Tag createList(Stream<Tag> stream) {
      PeekingIterator<Tag> peekingIterator = Iterators.peekingIterator(stream.iterator());
      if (!peekingIterator.hasNext()) {
         return new ListTag();
      } else {
         Tag tag = (Tag)peekingIterator.peek();
         ArrayList list3;
         if (tag instanceof ByteTag) {
            list3 = Lists.newArrayList(Iterators.transform(peekingIterator, (tagx) -> {
               return ((ByteTag)tagx).getByte();
            }));
            return new ByteArrayTag(list3);
         } else if (tag instanceof IntTag) {
            list3 = Lists.newArrayList(Iterators.transform(peekingIterator, (tagx) -> {
               return ((IntTag)tagx).getInt();
            }));
            return new IntArrayTag(list3);
         } else if (tag instanceof LongTag) {
            list3 = Lists.newArrayList(Iterators.transform(peekingIterator, (tagx) -> {
               return ((LongTag)tagx).getLong();
            }));
            return new LongArrayTag(list3);
         } else {
            ListTag listTag = new ListTag();

            while(peekingIterator.hasNext()) {
               Tag tag2 = (Tag)peekingIterator.next();
               if (!(tag2 instanceof EndTag)) {
                  listTag.add(tag2);
               }
            }

            return listTag;
         }
      }
   }

   public Tag remove(Tag tag, String string) {
      if (tag instanceof CompoundTag) {
         CompoundTag compoundTag = (CompoundTag)tag;
         CompoundTag compoundTag2 = new CompoundTag();
         compoundTag.getKeys().stream().filter((string2) -> {
            return !Objects.equals(string2, string);
         }).forEach((stringx) -> {
            compoundTag2.put(stringx, compoundTag.get(stringx));
         });
         return compoundTag2;
      } else {
         return tag;
      }
   }

   public String toString() {
      return "NBT";
   }
}
