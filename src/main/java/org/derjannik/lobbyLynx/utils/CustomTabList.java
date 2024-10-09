
package org.derjannik.lobbyLynx.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class CustomTabList extends JavaPlugin {

    private static final Logger LOGGER = Logger.getLogger(CustomTabList.class.getName());

    @Override
    public void onEnable() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setPlayerListHeaderFooter(
                    "ServerName\nWelcome " + player.getName(),
                    "{Rank} | " + player.getName() + "\nDate: " + java.time.LocalDate.now() + "\nOnline Players: " + Bukkit.getOnlinePlayers().size() + "\nVisit our webpage www.domain.com"
                );
            }
        }, 0L, 20L);
        LOGGER.info("CustomTabList plugin enabled");
    }
}
