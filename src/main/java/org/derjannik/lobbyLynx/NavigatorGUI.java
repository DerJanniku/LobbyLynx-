package org.derjannik.lobbyLynx;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavigatorGUI implements Listener {

    private final JavaPlugin plugin;
    private final Map<String, Location> minigameLocations;
    private final Map<String, String> minigamePermissions;

    public NavigatorGUI(JavaPlugin plugin) {
        this.plugin = plugin;
        this.minigameLocations = new HashMap<>();
        this.minigamePermissions = new HashMap<>();
        loadMinigameLocations();
    }

    private void loadMinigameLocations() {
        ConfigurationSection minigamesSection = plugin.getConfig().getConfigurationSection("minigames");
        if (minigamesSection == null) {
            plugin.getLogger().warning("No minigames configured in config.yml");
            return;
        }

        for (String minigameName : minigamesSection.getKeys(false)) {
            ConfigurationSection minigameSection = minigamesSection.getConfigurationSection(minigameName);
            if (minigameSection == null) {
                plugin.getLogger().warning("Invalid configuration for minigame: " + minigameName);
                continue;
            }

            // Load material
            String materialName = minigameSection.getString("item");
            Material material = (materialName != null) ? Material.matchMaterial(materialName) : null;

            if (material == null) {
                plugin.getLogger().warning("Invalid material for minigame: " + minigameName);
                continue;
            }

            // Load location
            String worldName = minigameSection.getString("world", "world");
            double x = minigameSection.getDouble("x");
            double y = minigameSection.getDouble("y");
            double z = minigameSection.getDouble("z");

            Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
            minigameLocations.put(minigameName, location);

            // Load permission
            String permission = minigameSection.getString("permission");
            if (permission != null) {
                minigamePermissions.put(minigameName, permission);
            }

            plugin.getLogger().info("Loaded minigame: " + minigameName);
        }
    }

    public void openGUI(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("navigator.gui.title", "Navigator"));
        int size = plugin.getConfig().getInt("navigator.gui.size", 9);

        Inventory gui = Bukkit.createInventory(null, size, title);

        for (Map.Entry<String, Location> entry : minigameLocations.entrySet()) {
            String minigameName = entry.getKey();
            ConfigurationSection minigameSection = plugin.getConfig().getConfigurationSection("minigames." + minigameName);
            if (minigameSection == null) continue;

            // Create item
            Material material = Material.matchMaterial(minigameSection.getString("item", "STONE"));
            if (material == null) material = Material.STONE;

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                // Set name
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                        minigameSection.getString("name", minigameName)));

                // Set lore
                List<String> lore = new ArrayList<>();
                for (String loreLine : minigameSection.getStringList("description")) {
                    loreLine = loreLine.replace("%players%",
                            String.valueOf(Bukkit.getWorld(minigameSection.getString("world", "world"))
                                    .getPlayers().size()));
                    lore.add(ChatColor.translateAlternateColorCodes('&', loreLine));
                }
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            int slot = minigameSection.getInt("slot", 0);
            gui.setItem(slot, item);
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(
                ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("navigator.gui.title", "Navigator")))) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String itemName = ChatColor.stripColor(meta.getDisplayName());

        // Check permission
        String permission = minigamePermissions.get(itemName);
        if (permission != null && !player.hasPermission(permission)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("navigator.messages.no-permission",
                            "&cYou don't have permission to use this!")));
            return;
        }

        Location location = minigameLocations.get(itemName);
        if (location != null) {
            player.teleport(location);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("navigator.messages.teleport-success", "&aTeleported!")
                            .replace("%minigame%", itemName)));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("navigator.messages.teleport-error",  "&cCould not teleport!")
                            .replace("%minigame%", itemName)));
        }
    }
}