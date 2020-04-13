package net.minecraft.advancement;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.PacketByteBuf;
import org.apache.commons.lang3.ArrayUtils;

public class Advancement {
   private final Advancement parent;
   private final AdvancementDisplay display;
   private final AdvancementRewards rewards;
   private final Identifier id;
   private final Map<String, AdvancementCriterion> criteria;
   private final String[][] requirements;
   private final Set<Advancement> children = Sets.newLinkedHashSet();
   private final Text text;

   public Advancement(Identifier id, @Nullable Advancement parent, @Nullable AdvancementDisplay display, AdvancementRewards rewards, Map<String, AdvancementCriterion> criteria, String[][] requirements) {
      this.id = id;
      this.display = display;
      this.criteria = ImmutableMap.copyOf(criteria);
      this.parent = parent;
      this.rewards = rewards;
      this.requirements = requirements;
      if (parent != null) {
         parent.addChild(this);
      }

      if (display == null) {
         this.text = new LiteralText(id.toString());
      } else {
         Text text = display.getTitle();
         Formatting formatting = display.getFrame().getTitleFormat();
         Text text2 = text.deepCopy().formatted(formatting).append("\n").append(display.getDescription());
         Text text3 = text.deepCopy().styled((style) -> {
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, text2));
         });
         this.text = (new LiteralText("[")).append(text3).append("]").formatted(formatting);
      }

   }

   public Advancement.Task createTask() {
      return new Advancement.Task(this.parent == null ? null : this.parent.getId(), this.display, this.rewards, this.criteria, this.requirements);
   }

   @Nullable
   public Advancement getParent() {
      return this.parent;
   }

   @Nullable
   public AdvancementDisplay getDisplay() {
      return this.display;
   }

   public AdvancementRewards getRewards() {
      return this.rewards;
   }

   public String toString() {
      return "SimpleAdvancement{id=" + this.getId() + ", parent=" + (this.parent == null ? "null" : this.parent.getId()) + ", display=" + this.display + ", rewards=" + this.rewards + ", criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + '}';
   }

   public Iterable<Advancement> getChildren() {
      return this.children;
   }

   public Map<String, AdvancementCriterion> getCriteria() {
      return this.criteria;
   }

   @Environment(EnvType.CLIENT)
   public int getRequirementCount() {
      return this.requirements.length;
   }

   public void addChild(Advancement child) {
      this.children.add(child);
   }

   public Identifier getId() {
      return this.id;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Advancement)) {
         return false;
      } else {
         Advancement advancement = (Advancement)o;
         return this.id.equals(advancement.id);
      }
   }

   public int hashCode() {
      return this.id.hashCode();
   }

   public String[][] getRequirements() {
      return this.requirements;
   }

   public Text toHoverableText() {
      return this.text;
   }

   public static class Task {
      private Identifier parentId;
      private Advancement parentObj;
      private AdvancementDisplay display;
      private AdvancementRewards rewards;
      private Map<String, AdvancementCriterion> criteria;
      private String[][] requirements;
      private CriteriaMerger merger;

      private Task(@Nullable Identifier parentId, @Nullable AdvancementDisplay display, AdvancementRewards rewards, Map<String, AdvancementCriterion> criteria, String[][] requirements) {
         this.rewards = AdvancementRewards.NONE;
         this.criteria = Maps.newLinkedHashMap();
         this.merger = CriteriaMerger.AND;
         this.parentId = parentId;
         this.display = display;
         this.rewards = rewards;
         this.criteria = criteria;
         this.requirements = requirements;
      }

      private Task() {
         this.rewards = AdvancementRewards.NONE;
         this.criteria = Maps.newLinkedHashMap();
         this.merger = CriteriaMerger.AND;
      }

      public static Advancement.Task create() {
         return new Advancement.Task();
      }

      public Advancement.Task parent(Advancement parent) {
         this.parentObj = parent;
         return this;
      }

      public Advancement.Task parent(Identifier parentId) {
         this.parentId = parentId;
         return this;
      }

      public Advancement.Task display(ItemStack icon, Text title, Text description, @Nullable Identifier background, AdvancementFrame frame, boolean showToast, boolean announceToChat, boolean hidden) {
         return this.display(new AdvancementDisplay(icon, title, description, background, frame, showToast, announceToChat, hidden));
      }

      public Advancement.Task display(ItemConvertible icon, Text title, Text description, @Nullable Identifier background, AdvancementFrame frame, boolean showToast, boolean announceToChat, boolean hidden) {
         return this.display(new AdvancementDisplay(new ItemStack(icon.asItem()), title, description, background, frame, showToast, announceToChat, hidden));
      }

      public Advancement.Task display(AdvancementDisplay display) {
         this.display = display;
         return this;
      }

      public Advancement.Task rewards(AdvancementRewards.Builder builder) {
         return this.rewards(builder.build());
      }

      public Advancement.Task rewards(AdvancementRewards rewards) {
         this.rewards = rewards;
         return this;
      }

      public Advancement.Task criterion(String conditions, CriterionConditions criterionConditions) {
         return this.criterion(conditions, new AdvancementCriterion(criterionConditions));
      }

      public Advancement.Task criterion(String criterion, AdvancementCriterion advancementCriterion) {
         if (this.criteria.containsKey(criterion)) {
            throw new IllegalArgumentException("Duplicate criterion " + criterion);
         } else {
            this.criteria.put(criterion, advancementCriterion);
            return this;
         }
      }

      public Advancement.Task criteriaMerger(CriteriaMerger merger) {
         this.merger = merger;
         return this;
      }

      public boolean findParent(Function<Identifier, Advancement> function) {
         if (this.parentId == null) {
            return true;
         } else {
            if (this.parentObj == null) {
               this.parentObj = (Advancement)function.apply(this.parentId);
            }

            return this.parentObj != null;
         }
      }

      public Advancement build(Identifier identifier) {
         if (!this.findParent((identifierx) -> {
            return null;
         })) {
            throw new IllegalStateException("Tried to build incomplete advancement!");
         } else {
            if (this.requirements == null) {
               this.requirements = this.merger.createRequirements(this.criteria.keySet());
            }

            return new Advancement(identifier, this.parentObj, this.display, this.rewards, this.criteria, this.requirements);
         }
      }

      public Advancement build(Consumer<Advancement> consumer, String string) {
         Advancement advancement = this.build(new Identifier(string));
         consumer.accept(advancement);
         return advancement;
      }

      public JsonObject toJson() {
         if (this.requirements == null) {
            this.requirements = this.merger.createRequirements(this.criteria.keySet());
         }

         JsonObject jsonObject = new JsonObject();
         if (this.parentObj != null) {
            jsonObject.addProperty("parent", this.parentObj.getId().toString());
         } else if (this.parentId != null) {
            jsonObject.addProperty("parent", this.parentId.toString());
         }

         if (this.display != null) {
            jsonObject.add("display", this.display.toJson());
         }

         jsonObject.add("rewards", this.rewards.toJson());
         JsonObject jsonObject2 = new JsonObject();
         Iterator var3 = this.criteria.entrySet().iterator();

         while(var3.hasNext()) {
            Entry<String, AdvancementCriterion> entry = (Entry)var3.next();
            jsonObject2.add((String)entry.getKey(), ((AdvancementCriterion)entry.getValue()).toJson());
         }

         jsonObject.add("criteria", jsonObject2);
         JsonArray jsonArray = new JsonArray();
         String[][] var14 = this.requirements;
         int var5 = var14.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            String[] strings = var14[var6];
            JsonArray jsonArray2 = new JsonArray();
            String[] var9 = strings;
            int var10 = strings.length;

            for(int var11 = 0; var11 < var10; ++var11) {
               String string = var9[var11];
               jsonArray2.add(string);
            }

            jsonArray.add(jsonArray2);
         }

         jsonObject.add("requirements", jsonArray);
         return jsonObject;
      }

      public void toPacket(PacketByteBuf packetByteBuf) {
         if (this.parentId == null) {
            packetByteBuf.writeBoolean(false);
         } else {
            packetByteBuf.writeBoolean(true);
            packetByteBuf.writeIdentifier(this.parentId);
         }

         if (this.display == null) {
            packetByteBuf.writeBoolean(false);
         } else {
            packetByteBuf.writeBoolean(true);
            this.display.toPacket(packetByteBuf);
         }

         AdvancementCriterion.serialize(this.criteria, packetByteBuf);
         packetByteBuf.writeVarInt(this.requirements.length);
         String[][] var2 = this.requirements;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            String[] strings = var2[var4];
            packetByteBuf.writeVarInt(strings.length);
            String[] var6 = strings;
            int var7 = strings.length;

            for(int var8 = 0; var8 < var7; ++var8) {
               String string = var6[var8];
               packetByteBuf.writeString(string);
            }
         }

      }

      public String toString() {
         return "Task Advancement{parentId=" + this.parentId + ", display=" + this.display + ", rewards=" + this.rewards + ", criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + '}';
      }

      public static Advancement.Task fromJson(JsonObject obj, JsonDeserializationContext context) {
         Identifier identifier = obj.has("parent") ? new Identifier(JsonHelper.getString(obj, "parent")) : null;
         AdvancementDisplay advancementDisplay = obj.has("display") ? AdvancementDisplay.fromJson(JsonHelper.getObject(obj, "display"), context) : null;
         AdvancementRewards advancementRewards = (AdvancementRewards)JsonHelper.deserialize(obj, "rewards", AdvancementRewards.NONE, context, AdvancementRewards.class);
         Map<String, AdvancementCriterion> map = AdvancementCriterion.fromJson(JsonHelper.getObject(obj, "criteria"), context);
         if (map.isEmpty()) {
            throw new JsonSyntaxException("Advancement criteria cannot be empty");
         } else {
            JsonArray jsonArray = JsonHelper.getArray(obj, "requirements", new JsonArray());
            String[][] strings = new String[jsonArray.size()][];

            int i;
            int j;
            for(i = 0; i < jsonArray.size(); ++i) {
               JsonArray jsonArray2 = JsonHelper.asArray(jsonArray.get(i), "requirements[" + i + "]");
               strings[i] = new String[jsonArray2.size()];

               for(j = 0; j < jsonArray2.size(); ++j) {
                  strings[i][j] = JsonHelper.asString(jsonArray2.get(j), "requirements[" + i + "][" + j + "]");
               }
            }

            if (strings.length == 0) {
               strings = new String[map.size()][];
               i = 0;

               String string;
               for(Iterator var16 = map.keySet().iterator(); var16.hasNext(); strings[i++] = new String[]{string}) {
                  string = (String)var16.next();
               }
            }

            String[][] var17 = strings;
            int var18 = strings.length;

            int var13;
            for(j = 0; j < var18; ++j) {
               String[] strings2 = var17[j];
               if (strings2.length == 0 && map.isEmpty()) {
                  throw new JsonSyntaxException("Requirement entry cannot be empty");
               }

               String[] var12 = strings2;
               var13 = strings2.length;

               for(int var14 = 0; var14 < var13; ++var14) {
                  String string2 = var12[var14];
                  if (!map.containsKey(string2)) {
                     throw new JsonSyntaxException("Unknown required criterion '" + string2 + "'");
                  }
               }
            }

            Iterator var19 = map.keySet().iterator();

            String string3;
            boolean bl;
            do {
               if (!var19.hasNext()) {
                  return new Advancement.Task(identifier, advancementDisplay, advancementRewards, map, strings);
               }

               string3 = (String)var19.next();
               bl = false;
               String[][] var22 = strings;
               int var24 = strings.length;

               for(var13 = 0; var13 < var24; ++var13) {
                  String[] strings3 = var22[var13];
                  if (ArrayUtils.contains(strings3, string3)) {
                     bl = true;
                     break;
                  }
               }
            } while(bl);

            throw new JsonSyntaxException("Criterion '" + string3 + "' isn't a requirement for completion. This isn't supported behaviour, all criteria must be required.");
         }
      }

      public static Advancement.Task fromPacket(PacketByteBuf buf) {
         Identifier identifier = buf.readBoolean() ? buf.readIdentifier() : null;
         AdvancementDisplay advancementDisplay = buf.readBoolean() ? AdvancementDisplay.fromPacket(buf) : null;
         Map<String, AdvancementCriterion> map = AdvancementCriterion.fromPacket(buf);
         String[][] strings = new String[buf.readVarInt()][];

         for(int i = 0; i < strings.length; ++i) {
            strings[i] = new String[buf.readVarInt()];

            for(int j = 0; j < strings[i].length; ++j) {
               strings[i][j] = buf.readString(32767);
            }
         }

         return new Advancement.Task(identifier, advancementDisplay, AdvancementRewards.NONE, map, strings);
      }

      public Map<String, AdvancementCriterion> getCriteria() {
         return this.criteria;
      }
   }
}
