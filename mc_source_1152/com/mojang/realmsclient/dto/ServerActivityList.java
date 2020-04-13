package com.mojang.realmsclient.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ServerActivityList extends ValueObject {
   public long periodInMillis;
   public List<ServerActivity> serverActivities = Lists.newArrayList();

   public static ServerActivityList parse(String json) {
      ServerActivityList serverActivityList = new ServerActivityList();
      JsonParser jsonParser = new JsonParser();

      try {
         JsonElement jsonElement = jsonParser.parse(json);
         JsonObject jsonObject = jsonElement.getAsJsonObject();
         serverActivityList.periodInMillis = JsonUtils.getLongOr("periodInMillis", jsonObject, -1L);
         JsonElement jsonElement2 = jsonObject.get("playerActivityDto");
         if (jsonElement2 != null && jsonElement2.isJsonArray()) {
            JsonArray jsonArray = jsonElement2.getAsJsonArray();
            Iterator var7 = jsonArray.iterator();

            while(var7.hasNext()) {
               JsonElement jsonElement3 = (JsonElement)var7.next();
               ServerActivity serverActivity = ServerActivity.parse(jsonElement3.getAsJsonObject());
               serverActivityList.serverActivities.add(serverActivity);
            }
         }
      } catch (Exception var10) {
      }

      return serverActivityList;
   }
}
