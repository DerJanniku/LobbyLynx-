
package org.derjannik.lobbyLynx.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.logging.Logger;

public class CustomScoreboard extends JavaPlugin {

    private static final Logger LOGGER = Logger.getLogger(CustomScoreboard.class.getName());

    @Override
    public void onEnable() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ScoreboardManager manager = Bukkit.getScoreboardManager();
                Scoreboard board = manager.getNewScoreboard();
                Objective objective = board.registerNewObjective("test", "dummy", ChatColor.GREEN + "ServerName");
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);

                Score rankScore = objective.getScore(ChatColor.YELLOW + "{Rank} | " + player.getName());
                rankScore.setScore(2);

                Score onlinePlayersScore = objective.getScore(ChatColor.YELLOW + "Online Players: " + Bukkit.getOnlinePlayers().size());
                onlinePlayersScore.setScore(1);

                player.setScoreboard(board);
            }
        }, 0L, 20L);
        LOGGER.info("CustomScoreboard plugin enabled");
    }
}
