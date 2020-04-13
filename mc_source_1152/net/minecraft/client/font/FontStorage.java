package net.minecraft.client.font;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class FontStorage implements AutoCloseable {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final EmptyGlyphRenderer EMPTY_GLYPH_RENDERER = new EmptyGlyphRenderer();
   private static final Glyph SPACE = () -> {
      return 4.0F;
   };
   private static final Random RANDOM = new Random();
   private final TextureManager textureManager;
   private final Identifier id;
   private GlyphRenderer blankGlyphRenderer;
   private GlyphRenderer whiteRectangleGlyphRenderer;
   private final List<Font> fonts = Lists.newArrayList();
   private final Char2ObjectMap<GlyphRenderer> glyphRendererCache = new Char2ObjectOpenHashMap();
   private final Char2ObjectMap<Glyph> glyphCache = new Char2ObjectOpenHashMap();
   private final Int2ObjectMap<CharList> charactersByWidth = new Int2ObjectOpenHashMap();
   private final List<GlyphAtlasTexture> glyphAtlases = Lists.newArrayList();

   public FontStorage(TextureManager textureManager, Identifier id) {
      this.textureManager = textureManager;
      this.id = id;
   }

   public void setFonts(List<Font> fonts) {
      this.method_24290();
      this.closeGlyphAtlases();
      this.glyphRendererCache.clear();
      this.glyphCache.clear();
      this.charactersByWidth.clear();
      this.blankGlyphRenderer = this.getGlyphRenderer(BlankGlyph.INSTANCE);
      this.whiteRectangleGlyphRenderer = this.getGlyphRenderer(WhiteRectangleGlyph.INSTANCE);
      Set<Font> set = Sets.newHashSet();

      for(char c = 0; c < '\uffff'; ++c) {
         Iterator var4 = fonts.iterator();

         while(var4.hasNext()) {
            Font font = (Font)var4.next();
            Glyph glyph = c == ' ' ? SPACE : font.getGlyph(c);
            if (glyph != null) {
               set.add(font);
               if (glyph != BlankGlyph.INSTANCE) {
                  ((CharList)this.charactersByWidth.computeIfAbsent(MathHelper.ceil(((Glyph)glyph).getAdvance(false)), (i) -> {
                     return new CharArrayList();
                  })).add(c);
               }
               break;
            }
         }
      }

      Stream var10000 = fonts.stream();
      set.getClass();
      var10000 = var10000.filter(set::contains);
      List var10001 = this.fonts;
      var10000.forEach(var10001::add);
   }

   public void close() {
      this.method_24290();
      this.closeGlyphAtlases();
   }

   private void method_24290() {
      Iterator var1 = this.fonts.iterator();

      while(var1.hasNext()) {
         Font font = (Font)var1.next();
         font.close();
      }

      this.fonts.clear();
   }

   private void closeGlyphAtlases() {
      Iterator var1 = this.glyphAtlases.iterator();

      while(var1.hasNext()) {
         GlyphAtlasTexture glyphAtlasTexture = (GlyphAtlasTexture)var1.next();
         glyphAtlasTexture.close();
      }

      this.glyphAtlases.clear();
   }

   public Glyph getGlyph(char character) {
      return (Glyph)this.glyphCache.computeIfAbsent(character, (i) -> {
         return (Glyph)(i == 32 ? SPACE : this.getRenderableGlyph((char)i));
      });
   }

   private RenderableGlyph getRenderableGlyph(char character) {
      Iterator var2 = this.fonts.iterator();

      RenderableGlyph renderableGlyph;
      do {
         if (!var2.hasNext()) {
            return BlankGlyph.INSTANCE;
         }

         Font font = (Font)var2.next();
         renderableGlyph = font.getGlyph(character);
      } while(renderableGlyph == null);

      return renderableGlyph;
   }

   public GlyphRenderer getGlyphRenderer(char character) {
      return (GlyphRenderer)this.glyphRendererCache.computeIfAbsent(character, (i) -> {
         return (GlyphRenderer)(i == 32 ? EMPTY_GLYPH_RENDERER : this.getGlyphRenderer(this.getRenderableGlyph((char)i)));
      });
   }

   private GlyphRenderer getGlyphRenderer(RenderableGlyph c) {
      Iterator var2 = this.glyphAtlases.iterator();

      GlyphRenderer glyphRenderer;
      do {
         if (!var2.hasNext()) {
            GlyphAtlasTexture glyphAtlasTexture2 = new GlyphAtlasTexture(new Identifier(this.id.getNamespace(), this.id.getPath() + "/" + this.glyphAtlases.size()), c.hasColor());
            this.glyphAtlases.add(glyphAtlasTexture2);
            this.textureManager.registerTexture(glyphAtlasTexture2.getId(), glyphAtlasTexture2);
            GlyphRenderer glyphRenderer2 = glyphAtlasTexture2.getGlyphRenderer(c);
            return glyphRenderer2 == null ? this.blankGlyphRenderer : glyphRenderer2;
         }

         GlyphAtlasTexture glyphAtlasTexture = (GlyphAtlasTexture)var2.next();
         glyphRenderer = glyphAtlasTexture.getGlyphRenderer(c);
      } while(glyphRenderer == null);

      return glyphRenderer;
   }

   public GlyphRenderer getObfuscatedGlyphRenderer(Glyph glyph) {
      CharList charList = (CharList)this.charactersByWidth.get(MathHelper.ceil(glyph.getAdvance(false)));
      return charList != null && !charList.isEmpty() ? this.getGlyphRenderer(charList.get(RANDOM.nextInt(charList.size()))) : this.blankGlyphRenderer;
   }

   public GlyphRenderer getRectangleRenderer() {
      return this.whiteRectangleGlyphRenderer;
   }
}
