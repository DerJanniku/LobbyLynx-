package org.derjannik.lobbylynx;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class NavigatorListener implements Listener {
    private final LobbyLynx plugin;
    private final Navigator navigator;

    public NavigatorListener(LobbyLynx plugin, Navigator navigator) {
        this.plugin = plugin;
        this.navigator = navigator;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.getInventory().contains(navigator.getNavigatorItem())) {
            navigator.giveNavigatorItem(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        player.getInventory().remove(navigator.getNavigatorItem());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (navigator.isNavigatorItem(item)) {
                event.setCancelled(true);
                navigator.openNavigator(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(navigator.getNavigatorName())) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()) {
                Player player = (Player) event.getWhoClicked();
                String displayName = event.getCurrentItem().getItemMeta().getDisplayName();
                if (displayName.equals("Lobby Spawn")) {
                    navigator.teleportToLobbySpawn(player);
                }
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(navigator.getNavigatorName()) && navigator.isCloseActionEnabled()) {
            event.getPlayer().sendMessage(plugin.getConfig().getString("navigator.close_action.message", "You closed the Navigator."));
        }
    }
}