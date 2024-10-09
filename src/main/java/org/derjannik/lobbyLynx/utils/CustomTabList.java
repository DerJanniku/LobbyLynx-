package org.derjannik.lobbyLynx.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomTabList {
    private JavaPlugin plugin;

    public CustomTabList(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setTabList(Player player) {
        player.setPlayerListHeaderFooter(
                "ServerName\nWelcome " + player.getName(),
                "{Rank} | " + player.getName() + "\nDate: " + java.time.LocalDate.now() + "\nOnline Players: " + Bukkit.getOnlinePlayers().size() + "\nVisit our webpage www.domain.com"
        );
    }
}