package org.derjannik.lobbyLynx.listeners;

import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.derjannik.lobbyLynx.LobbyLynx;

public class TNTExplosionListener implements Listener {
    private final LobbyLynx plugin;

    public TNTExplosionListener(LobbyLynx plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof TNTPrimed && !plugin.areTNTExplosionsAllowed()) {
            event.setCancelled(true);
        }
    }
}
