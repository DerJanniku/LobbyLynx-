
package org.derjannik.lobbylynx;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class NavigatorListener implements Listener {

    private final JavaPlugin plugin;

    public NavigatorListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.COMPASS) {
            // Open Navigator GUI
            event.getPlayer().sendMessage("Navigator opened!");
            // Add GUI opening logic here
        }
    }
}
