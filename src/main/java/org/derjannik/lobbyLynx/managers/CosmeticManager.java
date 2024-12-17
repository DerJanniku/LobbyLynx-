package org.derjannik.lobbyLynx.managers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.derjannik.lobbyLynx.LobbyLynx;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;

public class CosmeticManager {
    private final LobbyLynx plugin;
    private final Map<UUID, Map<String, ItemStack>> playerCosmetics;
    private final Map<UUID, BukkitTask> activeTasks;
    private volatile boolean isEnabled = true;

    public CosmeticManager(LobbyLynx plugin) {
        this.plugin = plugin;
        this.playerCosmetics = new ConcurrentHashMap<>();
        this.activeTasks = new ConcurrentHashMap<>();
        loadCosmeticsConfig();
    }

    private void loadCosmeticsConfig() {
        try {
            FileConfiguration config = plugin.getConfig();
            // Load cosmetic configurations
            if (!config.contains("cosmetics")) {
                config.createSection("cosmetics");
                plugin.saveConfig();
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load cosmetics configuration", e);
        }
    }

    public void applyCosmetic(Player player, String cosmeticId) {
        if (player == null || cosmeticId == null || !isEnabled) {
            return;
        }

        try {
            UUID playerId = player.getUniqueId();
            playerCosmetics.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());

            // Cancel any existing cosmetic task for this player
            cancelCosmeticTask(playerId);

            // Apply the new cosmetic
            BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                if (player.isOnline() && isEnabled) {
                    updateCosmeticEffect(player, cosmeticId);
                } else {
                    cancelCosmeticTask(playerId);
                }
            }, 0L, 20L);

            activeTasks.put(playerId, task);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "Failed to apply cosmetic " + cosmeticId + " to player " + player.getName(), e);
        }
    }

    private void updateCosmeticEffect(Player player, String cosmeticId) {
        try {
            // Implementation of cosmetic effects would go here
            // This is just a placeholder for the actual effect implementation
            switch (cosmeticId.toLowerCase()) {
                case "trail":
                    // Apply particle trail effect
                    break;
                case "aura":
                    // Apply aura effect
                    break;
                case "wings":
                    // Apply wing effect
                    break;
                default:
                    plugin.getLogger().warning("Unknown cosmetic ID: " + cosmeticId);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "Error updating cosmetic effect for player " + player.getName(), e);
        }
    }

    public void removeCosmetic(Player player, String cosmeticId) {
        if (player == null || !isEnabled) {
            return;
        }

        try {
            UUID playerId = player.getUniqueId();
            Map<String, ItemStack> cosmetics = playerCosmetics.get(playerId);
            if (cosmetics != null) {
                cosmetics.remove(cosmeticId);
                if (cosmetics.isEmpty()) {
                    playerCosmetics.remove(playerId);
                }
            }
            cancelCosmeticTask(playerId);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "Failed to remove cosmetic " + cosmeticId + " from player " + player.getName(), e);
        }
    }

    private void cancelCosmeticTask(UUID playerId) {
        BukkitTask task = activeTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }

    public void cleanup() {
        isEnabled = false;
        try {
            // Cancel all active cosmetic tasks
            activeTasks.values().forEach(BukkitTask::cancel);
            activeTasks.clear();
            
            // Clear all stored cosmetics
            playerCosmetics.clear();
            
            plugin.getLogger().info("CosmeticManager cleaned up successfully");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error during CosmeticManager cleanup", e);
        }
    }

    public boolean hasCosmetic(Player player, String cosmeticId) {
        if (player == null || cosmeticId == null || !isEnabled) {
            return false;
        }
        
        Map<String, ItemStack> cosmetics = playerCosmetics.get(player.getUniqueId());
        return cosmetics != null && cosmetics.containsKey(cosmeticId);
    }

    public Map<String, ItemStack> getPlayerCosmetics(Player player) {
        if (player == null || !isEnabled) {
            return new ConcurrentHashMap<>();
        }
        return playerCosmetics.getOrDefault(player.getUniqueId(), new ConcurrentHashMap<>());
    }
}
