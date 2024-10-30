
package org.derjannik.lobbylynx;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Navigator {
    private final LobbyLynx plugin;
    private final String navigatorName;
    private final int guiSize;
    private final boolean showPlayerCount;
    private final boolean closeAction;

    public Navigator(LobbyLynx plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        this.navigatorName = config.getString("navigator.name", "Server Navigator");
        this.guiSize = config.getInt("navigator.size", 36);
        this.showPlayerCount = config.getBoolean("navigator.show_player_count", false);
        this.closeAction = config.getBoolean("navigator.close_action", false);
    }

    public void giveNavigatorItem(Player player) {
        ItemStack navigatorItem = new ItemStack(Material.COMPASS);
        ItemMeta meta = navigatorItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(navigatorName);
            navigatorItem.setItemMeta(meta);
        }
        player.getInventory().setItem(0, navigatorItem);
    }

    public void openNavigator(Player player) {
        Inventory gui = Bukkit.createInventory(null, guiSize, navigatorName);
        populateGUI(gui);
        player.openInventory(gui);
    }

    private void populateGUI(Inventory gui) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection minigamesSection = config.getConfigurationSection("minigames");
        
        if (minigamesSection != null) {
            for (String key : minigamesSection.getKeys(false)) {
                ConfigurationSection minigame = minigamesSection.getConfigurationSection(key);
                if (minigame != null) {
                    String name = minigame.getString("name");
                    int slot = minigame.getInt("slot");
                    String icon = minigame.getString("icon");
                    
                    ItemStack item = new ItemStack(Material.valueOf(icon));
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(name);
                        if (showPlayerCount) {
                            // TODO: Implement player count logic
                            meta.setLore(List.of("Players: 0"));
                        }
                        item.setItemMeta(meta);
                    }
                    gui.setItem(slot, item);
                }
            }
        }
        
        // Add lobby spawn item
        ItemStack lobbyItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta lobbyMeta = lobbyItem.getItemMeta();
        if (lobbyMeta != null) {
            lobbyMeta.setDisplayName("Lobby Spawn");
            lobbyItem.setItemMeta(lobbyMeta);
        }
        gui.setItem(guiSize - 1, lobbyItem);
    }

    public void updateNavigatorItems() {
        // This method is called periodically to update player counts if enabled
        if (showPlayerCount) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getOpenInventory().getTitle().equals(navigatorName)) {
                    Inventory inv = player.getOpenInventory().getTopInventory();
                    populateGUI(inv);
                    player.updateInventory();
                }
            }
        }
    }

    public void teleportToMinigame(Player player, String minigameName) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection minigameSection = config.getConfigurationSection("minigames." + minigameName);
        
        if (minigameSection != null) {
            String worldName = minigameSection.getString("world");
            double x = minigameSection.getDouble("x");
            double y = minigameSection.getDouble("y");
            double z = minigameSection.getDouble("z");
            float yaw = (float) minigameSection.getDouble("yaw");
            float pitch = (float) minigameSection.getDouble("pitch");
            
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                Location location = new Location(world, x, y, z, yaw, pitch);
                player.teleport(location);
                player.sendMessage("Teleported to " + minigameName);
            } else {
                player.sendMessage("Error: World not found for " + minigameName);
            }
        } else {
            player.sendMessage("Error: Minigame " + minigameName + " not found");
        }
    }

    public void teleportToLobbySpawn(Player player) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection lobbySection = config.getConfigurationSection("lobby_spawn");
        
        if (lobbySection != null) {
            String worldName = lobbySection.getString("world");
            double x = lobbySection.getDouble("x");
            double y = lobbySection.getDouble("y");
            double z = lobbySection.getDouble("z");
            float yaw = (float) lobbySection.getDouble("yaw");
            float pitch = (float) lobbySection.getDouble("pitch");
            
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                Location location = new Location(world, x, y, z, yaw, pitch);
                player.teleport(location);
                player.sendMessage("Teleported to Lobby Spawn");
            } else {
                player.sendMessage("Error: Lobby world not found");
            }
        } else {
            player.sendMessage("Error: Lobby spawn not configured");
        }
    }

    public boolean isNavigatorItem(ItemStack item) {
        return item != null && item.getType() == Material.COMPASS && item.hasItemMeta() &&
                item.getItemMeta().getDisplayName().equals(navigatorName);
    }

    public String getNavigatorName() {
        return navigatorName;
    }

    public boolean isCloseActionEnabled() {
        return closeAction;
    }

    public boolean isShowPlayerCount() {
        return showPlayerCount;
    }
}
