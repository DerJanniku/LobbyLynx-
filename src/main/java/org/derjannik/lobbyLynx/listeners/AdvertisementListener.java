package org.derjannik.lobbyLynx.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.derjannik.lobbyLynx.LobbyLynx;
import org.derjannik.lobbyLynx.util.Advertisement;

public class AdvertisementListener implements Listener {
    private final LobbyLynx plugin;

    public AdvertisementListener(LobbyLynx plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Optional: Zeige Willkommenswerbung
        if (plugin.getConfig().getBoolean("ads.show-on-join", true)) {
            Advertisement welcomeAd = plugin.getAdvertisementManager().getWelcomeAd();
            if (welcomeAd != null) {
                plugin.getAdvertisementManager().displayAdvertisement(event.getPlayer(), welcomeAd);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Cleanup für den Spieler
        plugin.getAdvertisementManager().cleanupPlayer(event.getPlayer());
    }
}