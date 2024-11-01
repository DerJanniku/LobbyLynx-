
package org.derjannik.lobbyLynx.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.derjannik.lobbyLynx.LobbyLynx;
import org.derjannik.lobbyLynx.managers.ConfigManager;

public class CustomScoreboard {

    private final LobbyLynx plugin;
    private final ConfigManager configManager;

    public CustomScoreboard(LobbyLynx plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void setScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("lobby", "dummy", ChatColor.YELLOW + "YourServer - Lobby");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Score blank1 = objective.getScore(ChatColor.RESET + " ");
        blank1.setScore(6);

        Score infoHeader = objective.getScore(ChatColor.GOLD + "Info:");
        infoHeader.setScore(5);

        Score rank = objective.getScore(ChatColor.WHITE + "+ Rank: " + ChatColor.YELLOW + "[Rank]");
        rank.setScore(4);

        Score coins = objective.getScore(ChatColor.WHITE + "+ Coins: " + ChatColor.YELLOW + "[Coins]");
        coins.setScore(3);

        Score blank2 = objective.getScore(ChatColor.RESET + "  ");
        blank2.setScore(2);

        Score online = objective.getScore(ChatColor.WHITE + "Online: " + ChatColor.YELLOW + Bukkit.getOnlinePlayers().size());
        online.setScore(1);

        Score website = objective.getScore(ChatColor.AQUA + "yourserver.com");
        website.setScore(0);

        player.setScoreboard(board);
    }
}
