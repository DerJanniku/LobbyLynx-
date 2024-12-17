package org.derjannik.lobbyLynx.managers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.derjannik.lobbyLynx.LobbyLynx;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class CustomTablist {
    private final LobbyLynx plugin;
    private final ConfigManager configManager;
    private volatile BukkitTask updateTask;
    private final AtomicBoolean isEnabled;

    public CustomTablist(LobbyLynx plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.isEnabled = new AtomicBoolean(true);
        startTablistUpdater();
    }

    private void startTablistUpdater() {
        try {
            if (updateTask != null) {
                updateTask.cancel();
            }

            updateTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                if (!isEnabled.get()) {
                    return;
                }

                try {
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        if (player != null && player.isOnline()) {
                            updateTablist(player);
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Error updating tablist", e);
                }
            }, 20L, 20L); // Update every second

            plugin.getLogger().info("Tablist updater started successfully");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to start tablist updater", e);
        }
    }

    public void updateTablist(Player player) {
        if (!isEnabled.get() || player == null || !player.isOnline()) {
            return;
        }

        try {
            String header = formatText(configManager.getTablistHeader(), player);
            String footer = formatText(configManager.getTablistFooter(), player);

            // Run on main thread to avoid async issues
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    if (player.isOnline()) {
                        player.setPlayerListHeaderFooter(header, footer);
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, 
                        "Error setting tablist for player " + player.getName(), e);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "Error preparing tablist update for player " + player.getName(), e);
        }
    }

    private String formatText(String text, Player player) {
        if (text == null) {
            return "";
        }

        return ChatColor.translateAlternateColorCodes('&', text
            .replace("%player%", player.getName())
            .replace("%online%", String.valueOf(plugin.getServer().getOnlinePlayers().size()))
            .replace("%max%", String.valueOf(plugin.getServer().getMaxPlayers())));
    }

    public void reload() {
        cleanup();
        isEnabled.set(true);
        startTablistUpdater();
    }

    public void cleanup() {
        isEnabled.set(false);
        
        try {
            if (updateTask != null) {
                updateTask.cancel();
                updateTask = null;
            }

            // Clear tablist for all online players
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    try {
                        if (player != null && player.isOnline()) {
                            player.setPlayerListHeaderFooter("", "");
                        }
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, 
                            "Error clearing tablist for player " + player.getName(), e);
                    }
                }
            });
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error during tablist cleanup", e);
        }
    }

    public boolean isEnabled() {
        return isEnabled.get();
    }

    public void setEnabled(boolean enabled) {
        isEnabled.set(enabled);
        if (enabled && updateTask == null) {
            startTablistUpdater();
        }
    }
}
