package org.derjannik.lobbyLynx;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.entity.TNTPrimed;

public class TNTExplosionListener implements Listener {
    private final LobbyLynx plugin;

    public TNTExplosionListener(LobbyLynx plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // Check if the entity causing the explosion is TNT and if TNT explosions are disabled
        if (event.getEntity() instanceof TNTPrimed && !plugin.areTNTExplosionsAllowed()) {
            event.setCancelled(true);
            plugin.getLogger().info("TNT explosion prevented in world: " + event.getLocation().getWorld().getName());
        }
    }
}
