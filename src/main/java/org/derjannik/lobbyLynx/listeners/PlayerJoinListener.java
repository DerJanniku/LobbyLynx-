package org.derjannik.lobbyLynx.listeners;

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
import org.bukkit.inventory.meta.SkullMeta;
import org.derjannik.lobbyLynx.LobbyLynx;
import org.derjannik.lobbyLynx.gui.FriendGUI;
import org.derjannik.lobbyLynx.gui.NavigatorGUI;
import org.derjannik.lobbyLynx.managers.ConfigManager;
import org.derjannik.lobbyLynx.scoreboard.CustomScoreboard;
import org.derjannik.lobbyLynx.scoreboard.CustomTablist;

public class PlayerJoinListener implements Listener {

    private final LobbyLynx plugin;
    private final ConfigManager configManager;
    private final CustomScoreboard customScoreboard;
    private final CustomTablist customTablist;
    private final FriendGUI friendGUI; // Add FriendGUI reference

    public PlayerJoinListener(LobbyLynx plugin, ConfigManager configManager,
                              CustomScoreboard customScoreboard, CustomTablist customTablist, FriendGUI friendGUI) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.customScoreboard = customScoreboard;
        this.customTablist = customTablist;
        this.friendGUI = friendGUI;
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

        // Create and set the Friend Menu head in the 9th slot (index 8)
        ItemStack friendHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) friendHead.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skullMeta.setDisplayName(ChatColor.GOLD + "Friend Menu");
        friendHead.setItemMeta(skullMeta);
        player.getInventory().setItem(8, friendHead); // Set to 9th slot (index 8)

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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null) {
            if (item.getType() == Material.COMPASS) {
                new NavigatorGUI(plugin, configManager).openGUI(event.getPlayer());
            } else if (item.getType() == Material.PLAYER_HEAD) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.getDisplayName().equals(ChatColor.GOLD + "Friend Menu")) {
                    event.setCancelled(true);
                    friendGUI.openGUI(player, FriendGUI.GUIType.MAIN);
                }
            }
        }
    }
}