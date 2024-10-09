package org.derjannik.lobbyLynx.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

public class CustomScoreboard {
    private static final String SCOREBOARD_NAME = "ServerName";
    private static final String SCOREBOARD_FORMAT = "{Rank} | %s";

    private final JavaPlugin plugin;

    public CustomScoreboard(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setScoreboard(Player player) {
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();

        Objective objective = scoreboard.registerNewObjective("lobbyLynx", "dummy");
        objective.setDisplayName(SCOREBOARD_NAME);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setRenderType(RenderType.INTEGER);

        Score playerScore = objective.getScore(player.getName());
        playerScore.setScore(0);

        player.setScoreboard(scoreboard);
    }
}