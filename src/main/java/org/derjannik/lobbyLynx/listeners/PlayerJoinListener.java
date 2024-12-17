package org.derjannik.lobbyLynx.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.derjannik.lobbyLynx.LobbyLynx;
import org.derjannik.lobbyLynx.managers.ConfigManager;
import org.derjannik.lobbyLynx.managers.FriendManager;

public class PlayerJoinListener implements Listener {
    private final LobbyLynx plugin;
    private final ConfigManager configManager;
    private final FriendManager friendManager;

    public PlayerJoinListener(LobbyLynx plugin, ConfigManager configManager, FriendManager friendManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.friendManager = friendManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        try {
            // Apply join message if enabled
            if (configManager.isJoinMessageEnabled()) {
                String joinMessage = configManager.getJoinMessage()
                    .replace("%player%", player.getName());
                event.setJoinMessage(joinMessage);
            }

            // Update friend statistics
            if (friendManager != null) {
                friendManager.addActivityEntry(player.getName(), "Joined the server");
            }

            // Apply lobby settings
            player.setAllowFlight(configManager.isFlightEnabled());
            
            // Set default gamemode
            player.setGameMode(configManager.getDefaultGameMode());

            // Teleport to spawn if enabled
            if (configManager.isTeleportToSpawnEnabled()) {
                player.teleport(configManager.getSpawnLocation());
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Error handling player join for " + player.getName() + ": " + e.getMessage());
        }
    }
}
