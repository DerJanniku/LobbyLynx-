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

        // Title
        String title = ChatColor.translateAlternateColorCodes('&', "&6LYNX.MC");
        Objective objective = board.registerNewObjective("lobby", "dummy", title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        int line = 14;

        // Server Name Title
        Score titleLine = objective.getScore(ChatColor.translateAlternateColorCodes('&', "&fLYNX.MC"));
        titleLine.setScore(line--);

        // Spacer
        Score spacer1 = objective.getScore(ChatColor.RESET.toString());
        spacer1.setScore(line--);

        // Rank
        String rank = getPlayerRank(player); // Implement this to fetch player's rank
        Score rankLine = objective.getScore(ChatColor.translateAlternateColorCodes('&', "&fRang: &a" + rank));
        rankLine.setScore(line--);

        // Spacer
        Score spacer2 = objective.getScore(ChatColor.RESET + " ");
        spacer2.setScore(line--);

        // Coins
        int coins = getPlayerCoins(player); // Implement this to fetch player's coins
        Score coinsLine = objective.getScore(ChatColor.translateAlternateColorCodes('&', "&fCoins: &e" + coins));
        coinsLine.setScore(line--);

        // Spacer
        Score spacer3 = objective.getScore(ChatColor.RESET + "  ");
        spacer3.setScore(line--);

        // Gold
        int gold = getPlayerGold(player); // Implement this to fetch player's gold
        Score goldLine = objective.getScore(ChatColor.translateAlternateColorCodes('&', "&fGold: &c" + gold));
        goldLine.setScore(line--);

        // Spacer
        Score spacer4 = objective.getScore(ChatColor.RESET + "   ");
        spacer4.setScore(line--);

        // Clan
        String clan = getPlayerClan(player); // Implement this to fetch player's clan
        Score clanLine = objective.getScore(ChatColor.translateAlternateColorCodes('&', "&fClan: &b" + (clan != null ? clan : "Kein Clan")));
        clanLine.setScore(line--);

        // Spacer
        Score spacer5 = objective.getScore(ChatColor.RESET + "    ");
        spacer5.setScore(line--);

        // Friends
        int friends = getPlayerFriends(player); // Implement this to fetch number of friends
        Score friendsLine = objective.getScore(ChatColor.translateAlternateColorCodes('&', "&fFreunde: &a" + friends + "&7/&6" + "6"));
        friendsLine.setScore(line--);

        // Final Spacer
        Score finalSpacer = objective.getScore(ChatColor.RESET + "     ");
        finalSpacer.setScore(line--);

        // Set the scoreboard to the player
        player.setScoreboard(board);
    }

    // Placeholder methods for fetching player data
    private String getPlayerRank(Player player) {
        // Replace this with actual logic to get player's rank
        return "Spieler"; // Default rank for demonstration
    }

    private int getPlayerCoins(Player player) {
        // Replace this with actual logic to get player's coins
        return 32656; // Example value
    }

    private int getPlayerGold(Player player) {
        // Replace this with actual logic to get player's gold
        return 0; // Example value
    }

    private String getPlayerClan(Player player) {
        // Replace this with actual logic to get player's clan
        return null; // Example value for no clan
    }

    private int getPlayerFriends(Player player) {
        // Replace this with actual logic to get player's friends count
        return 0; // Example value
    }
}
