package net.minecraft.server;

import com.google.gson.JsonObject;
import java.util.Date;
import javax.annotation.Nullable;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class BannedIpEntry extends BanEntry<String> {
   public BannedIpEntry(String ip) {
      this(ip, (Date)null, (String)null, (Date)null, (String)null);
   }

   public BannedIpEntry(String ip, @Nullable Date created, @Nullable String source, @Nullable Date expiry, @Nullable String reason) {
      super(ip, created, source, expiry, reason);
   }

   public Text toText() {
      return new LiteralText((String)this.getKey());
   }

   public BannedIpEntry(JsonObject jsonObject) {
      super(getIpFromJson(jsonObject), jsonObject);
   }

   private static String getIpFromJson(JsonObject json) {
      return json.has("ip") ? json.get("ip").getAsString() : null;
   }

   protected void serialize(JsonObject jsonObject) {
      if (this.getKey() != null) {
         jsonObject.addProperty("ip", (String)this.getKey());
         super.serialize(jsonObject);
      }
   }
}
