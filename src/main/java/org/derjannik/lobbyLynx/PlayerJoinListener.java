
package org.derjannik.lobbyLynx;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerJoinListener implements Listener {

    private final LobbyLynx plugin;
    private final ConfigManager configManager;
    private final CustomScoreboard customScoreboard;
    private final CustomTablist customTablist;

    public PlayerJoinListener(LobbyLynx plugin, ConfigManager configManager, CustomScoreboard customScoreboard, CustomTablist customTablist) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.customScoreboard = customScoreboard;
        this.customTablist = customTablist;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Clear items from the player's inventory
        player.getInventory().clear();

        // Create the Navigator item (a compass)
        ItemStack navigator = new ItemStack(Material.COMPASS);
        ItemMeta meta = navigator.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', configManager.getNavigatorTitle()));
        navigator.setItemMeta(meta);

        // Set the Navigator in the first hotbar slot
        player.getInventory().setItem(0, navigator);

        // Teleport the player to the lobby
        Location lobbyLocation = configManager.getLobbySpawn();
        player.teleport(lobbyLocation);

        // Send welcome message
        if (configManager.isNewPlayersOnly() && !player.hasPlayedBefore()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', configManager.getWelcomeMessage()));
        } else if (configManager.isAlwaysNotify()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', configManager.getWelcomeMessage()));
        }

        // Set join message
        event.setJoinMessage(ChatColor.translateAlternateColorCodes('&',
                configManager.getJoinMessage().replace("%player%", player.getName())));

        // Set custom scoreboard
        customScoreboard.setScoreboard(player);

        // Set custom tablist
        customTablist.setTablist(player);
    }

    // In PlayerJoinListener.java
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getItem().getType() == Material.COMPASS) {
            new NavigatorGUI(plugin, configManager).openGUI(event.getPlayer());
        }
    }
}