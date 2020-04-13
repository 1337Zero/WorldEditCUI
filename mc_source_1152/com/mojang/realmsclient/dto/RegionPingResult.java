package com.mojang.realmsclient.dto;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RegionPingResult extends ValueObject {
   private final String regionName;
   private final int ping;

   public RegionPingResult(String regionName, int ping) {
      this.regionName = regionName;
      this.ping = ping;
   }

   public int ping() {
      return this.ping;
   }

   public String toString() {
      return String.format(Locale.ROOT, "%s --> %.2f ms", this.regionName, (float)this.ping);
   }
}
