package org.derjannik.lobbyLynx.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.derjannik.lobbyLynx.LobbyLynx;

public class CustomRuleListener implements Listener {
    private final LobbyLynx plugin;

    public CustomRuleListener(LobbyLynx plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.isBlockBreakingAllowed()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.isBlockPlacementAllowed()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerToggleGlide(EntityToggleGlideEvent event) {
        if (event.getEntity() instanceof Player && !plugin.isElytraAllowed()) {
            event.setCancelled(true);
            if (event.isGliding()) {
                ((Player) event.getEntity()).setGliding(false);
            }
        }
    }
}
