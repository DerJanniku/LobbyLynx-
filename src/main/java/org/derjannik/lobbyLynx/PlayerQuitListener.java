
package org.derjannik.lobbyLynx;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final ConfigManager configManager;

    public PlayerQuitListener(LobbyLynx plugin, ConfigManager configManager) {
        this.configManager = configManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clear the player's inventory when they leave
        event.getPlayer().getInventory().clear();

        // Set quit message
        String quitMessage = configManager.getQuitMessage().replace("%player%", event.getPlayer().getName());
        event.setQuitMessage(ChatColor.translateAlternateColorCodes('&', quitMessage));
    }
}
