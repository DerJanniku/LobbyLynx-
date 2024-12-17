package org.derjannik.lobbyLynx.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.derjannik.lobbyLynx.LobbyLynx;
import org.derjannik.lobbyLynx.managers.ConfigManager;
import org.derjannik.lobbyLynx.managers.CosmeticManager;

public class CosmeticsListener implements Listener {
    private final LobbyLynx plugin;
    private final CosmeticManager cosmeticManager;
    private final ConfigManager configManager;

    public CosmeticsListener(LobbyLynx plugin, CosmeticManager cosmeticManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.cosmeticManager = cosmeticManager;
        this.configManager = configManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        try {
            // Reapply any saved cosmetics if auto-apply is enabled
            if (configManager.getConfig().getBoolean("cosmetics.auto-apply", true)) {
                String savedCosmetic = configManager.getConfig().getString(
                    "cosmetics.saved." + player.getUniqueId(), null);
                if (savedCosmetic != null) {
                    cosmeticManager.applyCosmetic(player, savedCosmetic);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error applying cosmetics for " + player.getName() + ": " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            // Remove any active cosmetics
            cosmeticManager.removeCosmetic(event.getPlayer(), null);
        } catch (Exception e) {
            plugin.getLogger().warning("Error removing cosmetics for " + event.getPlayer().getName() + ": " + e.getMessage());
        }
    }
}
