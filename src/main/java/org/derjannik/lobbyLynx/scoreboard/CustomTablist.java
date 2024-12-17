package org.derjannik.lobbyLynx.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.derjannik.lobbyLynx.LobbyLynx;
import org.derjannik.lobbyLynx.managers.ConfigManager;

public class CustomTablist {

    private final LobbyLynx plugin;
    private final ConfigManager configManager;

    public CustomTablist(LobbyLynx plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void setTablist(Player player) {
        // Retrieve and set header/footer from config
        String header = configManager.getTablistHeader();
        String footer = configManager.getTablistFooter();

        player.setPlayerListHeader(ChatColor.translateAlternateColorCodes('&', header));
        player.setPlayerListFooter(ChatColor.translateAlternateColorCodes('&', footer));

        // Initialize scoreboard
        Scoreboard board = player.getScoreboard();
        String playerName = player.getName();

        // Get player rank and icon
        String rank = getRank(player); // Implement getRank based on permissions
        String prefix = configManager.getTablistRankPrefix(rank);
        String icon = configManager.getTablistRankIcon(rank);

        // Set up team
        Team team = board.getTeam(rank);
        if (team == null) {
            team = board.registerNewTeam(rank);
        }

        // Add prefix and icon, handle connection strength
        String formattedName = prefix + icon + " ";
        if (configManager.isTablistConnectionStrengthEnabled()) {
            return;
        }

        team.setPrefix(ChatColor.translateAlternateColorCodes('&', formattedName));
        team.addEntry(playerName);
    }


    private int getTotalPlayers() {
        return Bukkit.getOnlinePlayers().size(); // Replace with actual total if using a proxy system
    }

    private String getRank(Player player) {
        // Placeholder rank logic, replace with permissions/rank plugin integration
        return "Default";
    }
}
