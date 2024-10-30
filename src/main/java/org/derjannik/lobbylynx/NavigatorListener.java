
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

    public NavigatorListener(LobbyLynx plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getNavigator().giveNavigatorItem(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        player.getInventory().clear();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            ItemStack item = event.getItem();
            if (plugin.getNavigator().isNavigatorItem(item)) {
                event.setCancelled(true);
                plugin.getNavigator().openNavigator(player);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(plugin.getNavigator().getNavigatorName())) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null) {
                Player player = (Player) event.getWhoClicked();
                String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
                if (itemName.equals("Lobby Spawn")) {
                    plugin.getNavigator().teleportToLobbySpawn(player);
                } else {
                    plugin.getNavigator().teleportToMinigame(player, itemName);
                }
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(plugin.getNavigator().getNavigatorName())) {
            if (plugin.getNavigator().isCloseActionEnabled()) {
                Player player = (Player) event.getPlayer();
                player.sendMessage("You closed the Navigator GUI");
            }
        }
    }
}
