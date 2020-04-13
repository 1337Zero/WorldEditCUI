package net.minecraft.server;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.UUID;

public class WhitelistEntry extends ServerConfigEntry<GameProfile> {
   public WhitelistEntry(GameProfile gameProfile) {
      super(gameProfile);
   }

   public WhitelistEntry(JsonObject jsonObject) {
      super(deserializeProfile(jsonObject), jsonObject);
   }

   protected void serialize(JsonObject jsonObject) {
      if (this.getKey() != null) {
         jsonObject.addProperty("uuid", ((GameProfile)this.getKey()).getId() == null ? "" : ((GameProfile)this.getKey()).getId().toString());
         jsonObject.addProperty("name", ((GameProfile)this.getKey()).getName());
         super.serialize(jsonObject);
      }
   }

   private static GameProfile deserializeProfile(JsonObject json) {
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
