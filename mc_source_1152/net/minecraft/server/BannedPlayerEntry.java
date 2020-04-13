package net.minecraft.server;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class BannedPlayerEntry extends BanEntry<GameProfile> {
   public BannedPlayerEntry(GameProfile profile) {
      this(profile, (Date)null, (String)null, (Date)null, (String)null);
   }

   public BannedPlayerEntry(GameProfile profile, @Nullable Date created, @Nullable String source, @Nullable Date expiry, @Nullable String reason) {
      super(profile, created, source, expiry, reason);
   }

   public BannedPlayerEntry(JsonObject jsonObject) {
      super(getProfileFromJson(jsonObject), jsonObject);
   }

   protected void serialize(JsonObject jsonObject) {
      if (this.getKey() != null) {
         jsonObject.addProperty("uuid", ((GameProfile)this.getKey()).getId() == null ? "" : ((GameProfile)this.getKey()).getId().toString());
         jsonObject.addProperty("name", ((GameProfile)this.getKey()).getName());
         super.serialize(jsonObject);
      }
   }

   public Text toText() {
      GameProfile gameProfile = (GameProfile)this.getKey();
      return new LiteralText(gameProfile.getName() != null ? gameProfile.getName() : Objects.toString(gameProfile.getId(), "(Unknown)"));
   }

   private static GameProfile getProfileFromJson(JsonObject json) {
      if (json.has("uuid") && json.has("name")) {
         String string = json.get("uuid").getAsString();

         UUID uUID2;
         try {
            uUID2 = UUID.fromString(string);
         } catch (Throwable var4) {
            return null;
         }

         return new GameProfile(uUID2, json.get("name").getAsString());
      } else {
         return null;
      }
   }
}
