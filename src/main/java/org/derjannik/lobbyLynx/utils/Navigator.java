
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

import java.util.logging.Logger;

public class Navigator extends JavaPlugin implements Listener {

    private static final Logger LOGGER = Logger.getLogger(Navigator.class.getName());

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getInventory().contains(Material.COMPASS)) {
                    ItemStack compass = new ItemStack(Material.COMPASS);
                    ItemMeta meta = compass.getItemMeta();
                    meta.setDisplayName("Navigator");
                    compass.setItemMeta(meta);
                    player.getInventory().setItem(0, compass);
                }
            }
        }, 0L, 20L);
        LOGGER.info("Navigator plugin enabled");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.COMPASS && item.getItemMeta().getDisplayName().equals("Navigator")) {
            Inventory navigator = Bukkit.createInventory(null, 9, "Navigator");
            addNavigatorItems(navigator);
            player.openInventory(navigator);
            LOGGER.info("Navigator GUI opened for player: " + player.getName());
        }
    }

    private void addNavigatorItems(Inventory navigator) {
        navigator.addItem(createGuiItem(Material.RED_BED, "BedWars"));
        navigator.addItem(createGuiItem(Material.DIAMOND_SWORD, "SkyWars"));
        navigator.addItem(createGuiItem(Material.BRICKS, "CityBuild"));
        navigator.addItem(createGuiItem(Material.GRASS_BLOCK, "SMP"));
    }

    private ItemStack createGuiItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
