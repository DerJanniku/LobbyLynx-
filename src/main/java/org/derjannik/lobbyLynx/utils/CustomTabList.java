package org.derjannik.lobbyLynx.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomTabList {
    private static final String TAB_HEADER = "ServerName\nWelcome %s";
    private static final String TAB_FOOTER = "{Rank} | %s\nDate: %s\nOnline Players: %d\nVisit our webpage www.domain.com";

    private final JavaPlugin plugin;

    public CustomTabList(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setTabList(Player player) {
        String header = String.format(TAB_HEADER, player.getName());
        String footer = String.format(TAB_FOOTER, player.getName(), java.time.LocalDate.now(), Bukkit.getOnlinePlayers().size());
        player.setPlayerListHeaderFooter(header, footer);
    }
}