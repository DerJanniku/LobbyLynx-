
package org.derjannik.lobbyLynx.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.derjannik.lobbyLynx.LobbyLynx;
import org.derjannik.lobbyLynx.managers.ConfigManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavigatorGUI implements Listener {

    private final LobbyLynx plugin;
    private final ConfigManager configManager;
    private final Map<String, Location> minigameLocations;

    public NavigatorGUI(LobbyLynx plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager; // Initialize configManager properly
        this.minigameLocations = new HashMap<>();
        loadMinigameLocations();
    }


    private void loadMinigameLocations() {
        for (String minigameName : configManager.getMinigames()) {
            String worldName = configManager.getMinigameWorld(minigameName);
            double x = configManager.getMinigameX(minigameName);
            double y = configManager.getMinigameY(minigameName);
            double z = configManager.getMinigameZ(minigameName);

            Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
            minigameLocations.put(minigameName, location);

            plugin.getLogger().info("Loaded minigame: " + minigameName);
        }
    }

    public void openGUI(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', configManager.getNavigatorTitle());
        int size = configManager.getNavigatorSize();

        Inventory gui = Bukkit.createInventory(null, size, title);

        for (String minigameName : configManager.getMinigames()) {
            ItemStack item = createMinigameItem(minigameName);
            int slot = configManager.getMinigameSlot(minigameName);
            if (slot >= 0 && slot < size) {
                gui.setItem(slot, item);
            }
        }

        // Add lobby spawn item
        ItemStack lobbyItem = createLobbyItem();
        gui.setItem(configManager.getLobbySpawnSlot(), lobbyItem);

        player.openInventory(gui);
    }

    private ItemStack createMinigameItem(String minigameName) {
        Material material = Material.matchMaterial(configManager.getMinigameItem(minigameName));
        if (material == null) material = Material.STONE;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', configManager.getMinigameName(minigameName)));

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click to teleport!");
            meta.setLore(lore);

            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createLobbyItem() {
        Material material = Material.matchMaterial(configManager.getLobbySpawnItem());
        if (material == null) material = Material.NETHER_STAR;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Lobby");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click to return to lobby!");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&', configManager.getNavigatorTitle()))) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem.getItemMeta() == null) return;

            String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

            if (itemName.equals("Lobby")) {
                // Teleport to lobby
                Location lobbyLocation = new Location(
                        Bukkit.getWorld(configManager.getLobbyWorld()),
                        configManager.getLobbyX(),
                        configManager.getLobbyY(),
                        configManager.getLobbyZ()
                );
                player.teleport(lobbyLocation);
                player.sendMessage(ChatColor.GREEN + "Teleported to Lobby!");
            } else {
                // Check if it's a minigame
                Location minigameLocation = minigameLocations.get(itemName);
                if (minigameLocation != null) {
                    player.teleport(minigameLocation);
                    player.sendMessage(ChatColor.GREEN + "Teleported to " + itemName + "!");
                }
            }

            player.closeInventory();
        }
    }

    public void reloadGUI() {
        minigameLocations.clear();
        loadMinigameLocations();
    }

    public void openNavigatorGUI(Player player) {
        openGUI(player);
    }
}
