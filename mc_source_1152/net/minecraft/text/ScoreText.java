package net.minecraft.text;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.ChatUtil;

public class ScoreText extends BaseText implements ParsableText {
   private final String name;
   @Nullable
   private final EntitySelector selector;
   private final String objective;
   private String score = "";

   public ScoreText(String name, String objective) {
      this.name = name;
      this.objective = objective;
      EntitySelector entitySelector = null;

      try {
         EntitySelectorReader entitySelectorReader = new EntitySelectorReader(new StringReader(name));
         entitySelector = entitySelectorReader.read();
      } catch (CommandSyntaxException var5) {
      }

      this.selector = entitySelector;
   }

   public String getName() {
      return this.name;
   }

   public String getObjective() {
      return this.objective;
   }

   public void setScore(String score) {
      this.score = score;
   }

   public String asString() {
      return this.score;
   }

   private void parse(ServerCommandSource source) {
      MinecraftServer minecraftServer = source.getMinecraftServer();
      if (minecraftServer != null && minecraftServer.hasGameDir() && ChatUtil.isEmpty(this.score)) {
         Scoreboard scoreboard = minecraftServer.getScoreboard();
         ScoreboardObjective scoreboardObjective = scoreboard.getNullableObjective(this.objective);
         if (scoreboard.playerHasObjective(this.name, scoreboardObjective)) {
            ScoreboardPlayerScore scoreboardPlayerScore = scoreboard.getPlayerScore(this.name, scoreboardObjective);
            this.setScore(String.format("%d", scoreboardPlayerScore.getScore()));
         } else {
            this.score = "";
         }
      }

   }

   public ScoreText copy() {
      ScoreText scoreText = new ScoreText(this.name, this.objective);
      scoreText.setScore(this.score);
      return scoreText;
   }

   public Text parse(@Nullable ServerCommandSource source, @Nullable Entity sender, int depth) throws CommandSyntaxException {
      if (source == null) {
         return this.copy();
      } else {
         String string4;
         if (this.selector != null) {
            List<? extends Entity> list = this.selector.getEntities(source);
            if (list.isEmpty()) {
               string4 = this.name;
            } else {
               if (list.size() != 1) {
                  throw EntityArgumentType.TOO_MANY_ENTITIES_EXCEPTION.create();
               }

               string4 = ((Entity)list.get(0)).getEntityName();
            }
         } else {
            string4 = this.name;
         }

         String string5 = sender != null && string4.equals("*") ? sender.getEntityName() : string4;
         ScoreText scoreText = new ScoreText(string5, this.objective);
         scoreText.setScore(this.score);
         scoreText.parse(source);
         return scoreText;
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof ScoreText)) {
         return false;
      } else {
         ScoreText scoreText = (ScoreText)o;
         return this.name.equals(scoreText.name) && this.objective.equals(scoreText.objective) && super.equals(o);
      }
   }

   public String toString() {
      return "ScoreComponent{name='" + this.name + '\'' + "objective='" + this.objective + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
   }
}
