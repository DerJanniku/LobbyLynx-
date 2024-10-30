package org.derjannik.lobbyLynx;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class NavigatorGUI implements Listener {

    private final JavaPlugin plugin;
    private final Map<String, Location> minigameLocations;

    public NavigatorGUI(JavaPlugin plugin) {
        this.plugin = plugin;
        this.minigameLocations = new HashMap<>();

        // Load minigame locations from the configuration
        if (plugin.getConfig().contains("minigames")) {
            for (String minigameName : plugin.getConfig().getConfigurationSection("minigames").getKeys(false)) {
                int slot = plugin.getConfig().getInt("minigames." + minigameName + ".slot");
                Material material = Material.matchMaterial(plugin.getConfig().getString("minigames." + minigameName + ".item"));

                if (material == null) {
                    plugin.getLogger().warning("Invalid material for minigame: " + minigameName);
                    continue; // Skip this minigame if material is invalid
                }

                double x = plugin.getConfig().getDouble("minigames." + minigameName + ".x");
                double y = plugin.getConfig().getDouble("minigames." + minigameName + ".y");
                double z = plugin.getConfig().getDouble("minigames." + minigameName + ".z");

                minigameLocations.put(minigameName, new Location(Bukkit.getWorld("world"), x, y, z));
            }
        }
    }

    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "Navigator");

        for (Map.Entry<String, Location> entry : minigameLocations.entrySet()) {
            String minigameName = entry.getKey();
            Location location = entry.getValue();
            Material material = Material.matchMaterial(plugin.getConfig().getString("minigames." + minigameName + ".item"));

            if (material == null) {
                continue; // Skip if material is invalid
            }

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(minigameName);
                item.setItemMeta(meta);
            }

            int slot = plugin.getConfig().getInt("minigames." + minigameName + ".slot") - 1; // Zero-based index
            gui.setItem(slot, item);
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Navigator")) return;

        event.setCancelled(true); // Prevent taking items from the GUI
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        // Handle the clicked item based on its metadata
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String itemName = meta.getDisplayName();
        player.sendMessage("You selected: " + itemName);

        // Teleport the player to the selected minigame location
        Location location = minigameLocations.get(itemName);
        if (location != null) {
            player.teleport(location);
        }
    }
}