package org.derjannik.lobbyLynx;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.entity.Player;

public class CustomRuleListener implements Listener {
    private final LobbyLynx plugin;

    public CustomRuleListener(LobbyLynx plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Check if block breaking is allowed in the lobby, cancel event if not
        if (!plugin.isBlockBreakingAllowed()) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            player.sendMessage("§cBlock breaking is disabled in this lobby.");
            plugin.getLogger().info("Prevented " + player.getName() + " from breaking blocks in restricted area.");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // Check if block placement is allowed in the lobby, cancel event if not
        if (!plugin.isBlockPlacementAllowed()) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            player.sendMessage("§cBlock placement is disabled in this lobby.");
            plugin.getLogger().info("Prevented " + player.getName() + " from placing blocks in restricted area.");
        }
    }

    @EventHandler
    public void onPlayerToggleGlide(EntityToggleGlideEvent event) {
        // Check if the entity is a player and if Elytra usage is disallowed in the lobby
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!plugin.isElytraAllowed()) {
                event.setCancelled(true);
                if (event.isGliding()) {
                    player.setGliding(false);
                }
                player.sendMessage("§cElytra usage is disabled in this lobby.");
                plugin.getLogger().info("Prevented " + player.getName() + " from using Elytra in restricted area.");
            }
        }
    }
}
