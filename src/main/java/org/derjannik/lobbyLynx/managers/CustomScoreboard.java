package org.derjannik.lobbyLynx.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.derjannik.lobbyLynx.LobbyLynx;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class CustomScoreboard {
    private final LobbyLynx plugin;
    private final ConfigManager configManager;
    private final Map<UUID, Scoreboard> playerScoreboards;
    private volatile BukkitTask updateTask;
    private final AtomicBoolean isEnabled;

    public CustomScoreboard(LobbyLynx plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.playerScoreboards = new ConcurrentHashMap<>();
        this.isEnabled = new AtomicBoolean(true);
        startScoreboardUpdater();
    }

    private void startScoreboardUpdater() {
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
                            updateScoreboard(player);
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Error updating scoreboards", e);
                }
            }, 20L, 20L); // Update every second

            plugin.getLogger().info("Scoreboard updater started successfully");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to start scoreboard updater", e);
        }
    }

    public void setupScoreboard(Player player) {
        if (!isEnabled.get() || player == null || !player.isOnline()) {
            return;
        }

        try {
            // Run on main thread to avoid async scoreboard creation
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
                    String title = ChatColor.translateAlternateColorCodes('&', 
                        configManager.getScoreboardTitle());
                    
                    Objective objective = scoreboard.registerNewObjective("lobby", "dummy", title);
                    objective.setDisplaySlot(DisplaySlot.SIDEBAR);

                    playerScoreboards.put(player.getUniqueId(), scoreboard);
                    updateScoreboard(player);
                    
                    if (player.isOnline()) {
                        player.setScoreboard(scoreboard);
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, 
                        "Error setting up scoreboard for player " + player.getName(), e);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "Error scheduling scoreboard setup for player " + player.getName(), e);
        }
    }

    public void updateScoreboard(Player player) {
        if (!isEnabled.get() || player == null || !player.isOnline()) {
            return;
        }

        Scoreboard scoreboard = playerScoreboards.get(player.getUniqueId());
        if (scoreboard == null) {
            setupScoreboard(player);
            return;
        }

        try {
            // Run on main thread to avoid async scoreboard modification
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    Objective objective = scoreboard.getObjective("lobby");
                    if (objective == null) {
                        setupScoreboard(player);
                        return;
                    }

                    // Clear existing scores
                    for (String entry : scoreboard.getEntries()) {
                        scoreboard.resetScores(entry);
                    }

                    // Update with new scores
                    List<String> lines = configManager.getScoreboardLines();
                    int lineNumber = lines.size();
                    
                    for (String line : lines) {
                        String formattedLine = formatLine(line, player);
                        if (formattedLine.length() > 40) {
                            formattedLine = formattedLine.substring(0, 40);
                        }
                        Score score = objective.getScore(formattedLine);
                        score.setScore(lineNumber--);
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, 
                        "Error updating scoreboard for player " + player.getName(), e);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "Error scheduling scoreboard update for player " + player.getName(), e);
        }
    }

    private String formatLine(String line, Player player) {
        return ChatColor.translateAlternateColorCodes('&', 
            line.replace("%player%", player.getName())
                .replace("%online%", String.valueOf(plugin.getServer().getOnlinePlayers().size()))
                .replace("%max%", String.valueOf(plugin.getServer().getMaxPlayers())));
    }

    public void removeScoreboard(Player player) {
        if (player == null) {
            return;
        }

        try {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    if (player.isOnline()) {
                        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                    }
                    playerScoreboards.remove(player.getUniqueId());
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, 
                        "Error removing scoreboard for player " + player.getName(), e);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "Error scheduling scoreboard removal for player " + player.getName(), e);
        }
    }

    public void reload() {
        cleanup();
        isEnabled.set(true);
        startScoreboardUpdater();
        
        // Reinitialize scoreboards for online players
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            setupScoreboard(player);
        }
    }

    public void cleanup() {
        isEnabled.set(false);
        
        try {
            if (updateTask != null) {
                updateTask.cancel();
                updateTask = null;
            }

            // Clear all scoreboards
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    Scoreboard emptyScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
                    for (UUID uuid : playerScoreboards.keySet()) {
                        Player player = plugin.getServer().getPlayer(uuid);
                        if (player != null && player.isOnline()) {
                            player.setScoreboard(emptyScoreboard);
                        }
                    }
                    playerScoreboards.clear();
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Error clearing scoreboards", e);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error during scoreboard cleanup", e);
        }
    }

    public boolean isEnabled() {
        return isEnabled.get();
    }

    public void setEnabled(boolean enabled) {
        isEnabled.set(enabled);
        if (enabled && updateTask == null) {
            startScoreboardUpdater();
        }
    }
}
