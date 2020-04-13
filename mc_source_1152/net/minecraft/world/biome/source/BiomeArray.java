package net.minecraft.world.biome.source;

import javax.annotation.Nullable;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiomeArray implements BiomeAccess.Storage {
   private static final Logger field_21813 = LogManager.getLogger();
   private static final int HORIZONTAL_SECTION_COUNT = (int)Math.round(Math.log(16.0D) / Math.log(2.0D)) - 2;
   private static final int VERTICAL_SECTION_COUNT = (int)Math.round(Math.log(256.0D) / Math.log(2.0D)) - 2;
   public static final int DEFAULT_LENGTH;
   public static final int HORIZONTAL_BIT_MASK;
   public static final int VERTICAL_BIT_MASK;
   private final Biome[] data;

   public BiomeArray(Biome[] data) {
      this.data = data;
   }

   private BiomeArray() {
      this(new Biome[DEFAULT_LENGTH]);
   }

   public BiomeArray(PacketByteBuf packetByteBuf) {
      this();

      for(int i = 0; i < this.data.length; ++i) {
         int j = packetByteBuf.readInt();
         Biome biome = (Biome)Registry.BIOME.get(j);
         if (biome == null) {
            field_21813.warn("Received invalid biome id: " + j);
            this.data[i] = Biomes.PLAINS;
         } else {
            this.data[i] = biome;
         }
      }

   }

   public BiomeArray(ChunkPos pos, BiomeSource source) {
      this();
      int i = pos.getStartX() >> 2;
      int j = pos.getStartZ() >> 2;

      for(int k = 0; k < this.data.length; ++k) {
         int l = k & HORIZONTAL_BIT_MASK;
         int m = k >> HORIZONTAL_SECTION_COUNT + HORIZONTAL_SECTION_COUNT & VERTICAL_BIT_MASK;
         int n = k >> HORIZONTAL_SECTION_COUNT & HORIZONTAL_BIT_MASK;
         this.data[k] = source.getBiomeForNoiseGen(i + l, m, j + n);
      }

   }

   public BiomeArray(ChunkPos pos, BiomeSource source, @Nullable int[] rawIds) {
      this();
      int i = pos.getStartX() >> 2;
      int j = pos.getStartZ() >> 2;
      int k;
      int l;
      int m;
      int n;
      if (rawIds != null) {
         for(k = 0; k < rawIds.length; ++k) {
            this.data[k] = (Biome)Registry.BIOME.get(rawIds[k]);
            if (this.data[k] == null) {
               l = k & HORIZONTAL_BIT_MASK;
               m = k >> HORIZONTAL_SECTION_COUNT + HORIZONTAL_SECTION_COUNT & VERTICAL_BIT_MASK;
               n = k >> HORIZONTAL_SECTION_COUNT & HORIZONTAL_BIT_MASK;
               this.data[k] = source.getBiomeForNoiseGen(i + l, m, j + n);
            }
         }
      } else {
         for(k = 0; k < this.data.length; ++k) {
            l = k & HORIZONTAL_BIT_MASK;
            m = k >> HORIZONTAL_SECTION_COUNT + HORIZONTAL_SECTION_COUNT & VERTICAL_BIT_MASK;
            n = k >> HORIZONTAL_SECTION_COUNT & HORIZONTAL_BIT_MASK;
            this.data[k] = source.getBiomeForNoiseGen(i + l, m, j + n);
         }
      }

   }

   public int[] toIntArray() {
      int[] is = new int[this.data.length];

      for(int i = 0; i < this.data.length; ++i) {
         is[i] = Registry.BIOME.getRawId(this.data[i]);
      }

      return is;
   }

   public void toPacket(PacketByteBuf buf) {
      Biome[] var2 = this.data;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Biome biome = var2[var4];
         buf.writeInt(Registry.BIOME.getRawId(biome));
      }

   }

   public BiomeArray copy() {
      return new BiomeArray((Biome[])this.data.clone());
   }

   public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
      int i = biomeX & HORIZONTAL_BIT_MASK;
      int j = MathHelper.clamp(biomeY, 0, VERTICAL_BIT_MASK);
      int k = biomeZ & HORIZONTAL_BIT_MASK;
      return this.data[j << HORIZONTAL_SECTION_COUNT + HORIZONTAL_SECTION_COUNT | k << HORIZONTAL_SECTION_COUNT | i];
   }

   static {
      DEFAULT_LENGTH = 1 << HORIZONTAL_SECTION_COUNT + HORIZONTAL_SECTION_COUNT + VERTICAL_SECTION_COUNT;
      HORIZONTAL_BIT_MASK = (1 << HORIZONTAL_SECTION_COUNT) - 1;
      VERTICAL_BIT_MASK = (1 << VERTICAL_SECTION_COUNT) - 1;
   }
}
