package org.derjannik.lobbyLynx.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Navigator implements Listener {
    private static final String NAVIGATOR_NAME = "Navigator";
    private static final int NAVIGATOR_SIZE = 9;

    private final JavaPlugin plugin;

    public Navigator(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.COMPASS) {
            Inventory navigator = createNavigatorInventory();
            player.openInventory(navigator);
        }
    }

    private Inventory createNavigatorInventory() {
        Inventory navigator = Bukkit.createInventory(null, NAVIGATOR_SIZE, NAVIGATOR_NAME);
        // Add items to the navigator GUI
        return navigator;
    }
}