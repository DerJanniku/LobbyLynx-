package org.derjannik.lobbyLynx;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerJoinListener implements Listener {

    private final LobbyLynx plugin;
    private final ConfigManager configManager;

    public PlayerJoinListener(LobbyLynx plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Clear items from the player's inventory
        player.getInventory().clear();

        // Create the Navigator item (a compass)
        ItemStack navigator = new ItemStack(Material.COMPASS);
        navigator.getItemMeta().setDisplayName(configManager.getNavigatorName());

        // Set the Navigator in the first hotbar slot
        player.getInventory().setItem(0, navigator);

        // Teleport the player to the lobby
        double x = plugin.getConfig().getDouble("lobby.x");
        double y = plugin.getConfig().getDouble("lobby.y");
        double z = plugin.getConfig().getDouble("lobby.z");
        player.teleport(new Location(Bukkit.getWorld("world"), x, y, z));

        // Send welcome message to new players only if enabled
        if (plugin.getConfig().getBoolean("lobby.send-to-new-players-only")) {
            player.sendMessage("Welcome to the Lobby!");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if the player right-clicked with the Navigator
        if (event.getItem() != null && event.getItem().getType() == Material.COMPASS
                && event.getItem().getItemMeta() != null
                && event.getItem().getItemMeta().getDisplayName().equals(configManager.getNavigatorName())) {
            // Open the GUI
            new NavigatorGUI(plugin).openGUI(event.getPlayer());
        }
    }
}