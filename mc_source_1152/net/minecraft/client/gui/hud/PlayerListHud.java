package net.minecraft.client.gui.hud;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;

@Environment(EnvType.CLIENT)
public class PlayerListHud extends DrawableHelper {
   private static final Ordering<PlayerListEntry> ENTRY_ORDERING = Ordering.from(new PlayerListHud.EntryOrderComparator());
   private final MinecraftClient client;
   private final InGameHud inGameHud;
   private Text footer;
   private Text header;
   private long showTime;
   private boolean visible;

   public PlayerListHud(MinecraftClient client, InGameHud inGameHud) {
      this.client = client;
      this.inGameHud = inGameHud;
   }

   public Text getPlayerName(PlayerListEntry playerEntry) {
      return playerEntry.getDisplayName() != null ? playerEntry.getDisplayName() : Team.modifyText(playerEntry.getScoreboardTeam(), new LiteralText(playerEntry.getProfile().getName()));
   }

   public void tick(boolean visible) {
      if (visible && !this.visible) {
         this.showTime = Util.getMeasuringTimeMs();
      }

      this.visible = visible;
   }

   public void render(int width, Scoreboard scoreboard, @Nullable ScoreboardObjective playerListScoreboardObjective) {
      ClientPlayNetworkHandler clientPlayNetworkHandler = this.client.player.networkHandler;
      List<PlayerListEntry> list = ENTRY_ORDERING.sortedCopy(clientPlayNetworkHandler.getPlayerList());
      int i = 0;
      int j = 0;
      Iterator var8 = list.iterator();

      int n;
      while(var8.hasNext()) {
         PlayerListEntry playerListEntry = (PlayerListEntry)var8.next();
         n = this.client.textRenderer.getStringWidth(this.getPlayerName(playerListEntry).asFormattedString());
         i = Math.max(i, n);
         if (playerListScoreboardObjective != null && playerListScoreboardObjective.getRenderType() != ScoreboardCriterion.RenderType.HEARTS) {
            n = this.client.textRenderer.getStringWidth(" " + scoreboard.getPlayerScore(playerListEntry.getProfile().getName(), playerListScoreboardObjective).getScore());
            j = Math.max(j, n);
         }
      }

      list = list.subList(0, Math.min(list.size(), 80));
      int l = list.size();
      int m = l;

      for(n = 1; m > 20; m = (l + n - 1) / n) {
         ++n;
      }

      boolean bl = this.client.isInSingleplayer() || this.client.getNetworkHandler().getConnection().isEncrypted();
      int q;
      if (playerListScoreboardObjective != null) {
         if (playerListScoreboardObjective.getRenderType() == ScoreboardCriterion.RenderType.HEARTS) {
            q = 90;
         } else {
            q = j;
         }
      } else {
         q = 0;
      }

      int r = Math.min(n * ((bl ? 9 : 0) + i + q + 13), width - 50) / n;
      int s = width / 2 - (r * n + (n - 1) * 5) / 2;
      int t = 10;
      int u = r * n + (n - 1) * 5;
      List<String> list2 = null;
      if (this.header != null) {
         list2 = this.client.textRenderer.wrapStringToWidthAsList(this.header.asFormattedString(), width - 50);

         String string;
         for(Iterator var18 = list2.iterator(); var18.hasNext(); u = Math.max(u, this.client.textRenderer.getStringWidth(string))) {
            string = (String)var18.next();
         }
      }

      List<String> list3 = null;
      String string3;
      Iterator var36;
      if (this.footer != null) {
         list3 = this.client.textRenderer.wrapStringToWidthAsList(this.footer.asFormattedString(), width - 50);

         for(var36 = list3.iterator(); var36.hasNext(); u = Math.max(u, this.client.textRenderer.getStringWidth(string3))) {
            string3 = (String)var36.next();
         }
      }

      int var10000;
      int var10001;
      int var10002;
      int var10004;
      int y;
      if (list2 != null) {
         var10000 = width / 2 - u / 2 - 1;
         var10001 = t - 1;
         var10002 = width / 2 + u / 2 + 1;
         var10004 = list2.size();
         this.client.textRenderer.getClass();
         fill(var10000, var10001, var10002, t + var10004 * 9, Integer.MIN_VALUE);

         for(var36 = list2.iterator(); var36.hasNext(); t += 9) {
            string3 = (String)var36.next();
            y = this.client.textRenderer.getStringWidth(string3);
            this.client.textRenderer.drawWithShadow(string3, (float)(width / 2 - y / 2), (float)t, -1);
            this.client.textRenderer.getClass();
         }

         ++t;
      }

      fill(width / 2 - u / 2 - 1, t - 1, width / 2 + u / 2 + 1, t + m * 9, Integer.MIN_VALUE);
      int w = this.client.options.getTextBackgroundColor(553648127);

      int ai;
      for(int x = 0; x < l; ++x) {
         y = x / m;
         ai = x % m;
         int aa = s + y * r + y * 5;
         int ab = t + ai * 9;
         fill(aa, ab, aa + r, ab + 8, w);
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.enableAlphaTest();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         if (x < list.size()) {
            PlayerListEntry playerListEntry2 = (PlayerListEntry)list.get(x);
            GameProfile gameProfile = playerListEntry2.getProfile();
            int ah;
            if (bl) {
               PlayerEntity playerEntity = this.client.world.getPlayerByUuid(gameProfile.getId());
               boolean bl2 = playerEntity != null && playerEntity.isPartVisible(PlayerModelPart.CAPE) && ("Dinnerbone".equals(gameProfile.getName()) || "Grumm".equals(gameProfile.getName()));
               this.client.getTextureManager().bindTexture(playerListEntry2.getSkinTexture());
               ah = 8 + (bl2 ? 8 : 0);
               int ad = 8 * (bl2 ? -1 : 1);
               DrawableHelper.blit(aa, ab, 8, 8, 8.0F, (float)ah, 8, ad, 64, 64);
               if (playerEntity != null && playerEntity.isPartVisible(PlayerModelPart.HAT)) {
                  int ae = 8 + (bl2 ? 8 : 0);
                  int af = 8 * (bl2 ? -1 : 1);
                  DrawableHelper.blit(aa, ab, 8, 8, 40.0F, (float)ae, 8, af, 64, 64);
               }

               aa += 9;
            }

            String string4 = this.getPlayerName(playerListEntry2).asFormattedString();
            if (playerListEntry2.getGameMode() == GameMode.SPECTATOR) {
               this.client.textRenderer.drawWithShadow(Formatting.ITALIC + string4, (float)aa, (float)ab, -1862270977);
            } else {
               this.client.textRenderer.drawWithShadow(string4, (float)aa, (float)ab, -1);
            }

            if (playerListScoreboardObjective != null && playerListEntry2.getGameMode() != GameMode.SPECTATOR) {
               int ag = aa + i + 1;
               ah = ag + q;
               if (ah - ag > 5) {
                  this.renderScoreboardObjective(playerListScoreboardObjective, ab, gameProfile.getName(), ag, ah, playerListEntry2);
               }
            }

            this.renderLatencyIcon(r, aa - (bl ? 9 : 0), ab, playerListEntry2);
         }
      }

      if (list3 != null) {
         t += m * 9 + 1;
         var10000 = width / 2 - u / 2 - 1;
         var10001 = t - 1;
         var10002 = width / 2 + u / 2 + 1;
         var10004 = list3.size();
         this.client.textRenderer.getClass();
         fill(var10000, var10001, var10002, t + var10004 * 9, Integer.MIN_VALUE);

         for(Iterator var39 = list3.iterator(); var39.hasNext(); t += 9) {
            String string5 = (String)var39.next();
            ai = this.client.textRenderer.getStringWidth(string5);
            this.client.textRenderer.drawWithShadow(string5, (float)(width / 2 - ai / 2), (float)t, -1);
            this.client.textRenderer.getClass();
         }
      }

   }

   protected void renderLatencyIcon(int i, int j, int y, PlayerListEntry playerEntry) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.client.getTextureManager().bindTexture(GUI_ICONS_LOCATION);
      int k = false;
      byte q;
      if (playerEntry.getLatency() < 0) {
         q = 5;
      } else if (playerEntry.getLatency() < 150) {
         q = 0;
      } else if (playerEntry.getLatency() < 300) {
         q = 1;
      } else if (playerEntry.getLatency() < 600) {
         q = 2;
      } else if (playerEntry.getLatency() < 1000) {
         q = 3;
      } else {
         q = 4;
      }

      this.setBlitOffset(this.getBlitOffset() + 100);
      this.blit(j + i - 11, y, 0, 176 + q * 8, 10, 8);
      this.setBlitOffset(this.getBlitOffset() - 100);
   }

   private void renderScoreboardObjective(ScoreboardObjective scoreboardObjective, int i, String string, int j, int k, PlayerListEntry playerListEntry) {
      int l = scoreboardObjective.getScoreboard().getPlayerScore(string, scoreboardObjective).getScore();
      if (scoreboardObjective.getRenderType() == ScoreboardCriterion.RenderType.HEARTS) {
         this.client.getTextureManager().bindTexture(GUI_ICONS_LOCATION);
         long m = Util.getMeasuringTimeMs();
         if (this.showTime == playerListEntry.method_2976()) {
            if (l < playerListEntry.method_2973()) {
               playerListEntry.method_2978(m);
               playerListEntry.method_2975((long)(this.inGameHud.getTicks() + 20));
            } else if (l > playerListEntry.method_2973()) {
               playerListEntry.method_2978(m);
               playerListEntry.method_2975((long)(this.inGameHud.getTicks() + 10));
            }
         }

         if (m - playerListEntry.method_2974() > 1000L || this.showTime != playerListEntry.method_2976()) {
            playerListEntry.method_2972(l);
            playerListEntry.method_2965(l);
            playerListEntry.method_2978(m);
         }

         playerListEntry.method_2964(this.showTime);
         playerListEntry.method_2972(l);
         int n = MathHelper.ceil((float)Math.max(l, playerListEntry.method_2960()) / 2.0F);
         int o = Math.max(MathHelper.ceil((float)(l / 2)), Math.max(MathHelper.ceil((float)(playerListEntry.method_2960() / 2)), 10));
         boolean bl = playerListEntry.method_2961() > (long)this.inGameHud.getTicks() && (playerListEntry.method_2961() - (long)this.inGameHud.getTicks()) / 3L % 2L == 1L;
         if (n > 0) {
            int p = MathHelper.floor(Math.min((float)(k - j - 4) / (float)o, 9.0F));
            if (p > 3) {
               int r;
               for(r = n; r < o; ++r) {
                  this.blit(j + r * p, i, bl ? 25 : 16, 0, 9, 9);
               }

               for(r = 0; r < n; ++r) {
                  this.blit(j + r * p, i, bl ? 25 : 16, 0, 9, 9);
                  if (bl) {
                     if (r * 2 + 1 < playerListEntry.method_2960()) {
                        this.blit(j + r * p, i, 70, 0, 9, 9);
                     }

                     if (r * 2 + 1 == playerListEntry.method_2960()) {
                        this.blit(j + r * p, i, 79, 0, 9, 9);
                     }
                  }

                  if (r * 2 + 1 < l) {
                     this.blit(j + r * p, i, r >= 10 ? 160 : 52, 0, 9, 9);
                  }

                  if (r * 2 + 1 == l) {
                     this.blit(j + r * p, i, r >= 10 ? 169 : 61, 0, 9, 9);
                  }
               }
            } else {
               float f = MathHelper.clamp((float)l / 20.0F, 0.0F, 1.0F);
               int s = (int)((1.0F - f) * 255.0F) << 16 | (int)(f * 255.0F) << 8;
               String string2 = "" + (float)l / 2.0F;
               if (k - this.client.textRenderer.getStringWidth(string2 + "hp") >= j) {
                  string2 = string2 + "hp";
               }

               this.client.textRenderer.drawWithShadow(string2, (float)((k + j) / 2 - this.client.textRenderer.getStringWidth(string2) / 2), (float)i, s);
            }
         }
      } else {
         String string3 = Formatting.YELLOW + "" + l;
         this.client.textRenderer.drawWithShadow(string3, (float)(k - this.client.textRenderer.getStringWidth(string3)), (float)i, 16777215);
      }

   }

   public void setFooter(@Nullable Text footer) {
      this.footer = footer;
   }

   public void setHeader(@Nullable Text header) {
      this.header = header;
   }

   public void clear() {
      this.header = null;
      this.footer = null;
   }

   @Environment(EnvType.CLIENT)
   static class EntryOrderComparator implements Comparator<PlayerListEntry> {
      private EntryOrderComparator() {
      }

      public int compare(PlayerListEntry playerListEntry, PlayerListEntry playerListEntry2) {
         Team team = playerListEntry.getScoreboardTeam();
         Team team2 = playerListEntry2.getScoreboardTeam();
         return ComparisonChain.start().compareTrueFirst(playerListEntry.getGameMode() != GameMode.SPECTATOR, playerListEntry2.getGameMode() != GameMode.SPECTATOR).compare(team != null ? team.getName() : "", team2 != null ? team2.getName() : "").compare(playerListEntry.getProfile().getName(), playerListEntry2.getProfile().getName(), String::compareToIgnoreCase).result();
      }
   }
}
