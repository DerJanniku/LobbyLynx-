package org.derjannik.lobbylynx;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class Navigator {
    private final LobbyLynx plugin;
    private final ItemStack navigatorItem;
    private final String navigatorName;
    private final int guiSize;
    private final boolean showPlayerCount;
    private final boolean closeActionEnabled;
    private final boolean teleportMessageEnabled;
    private final int lobbySpawnSlot;
    private final Location lobbySpawnLocation;
    private final Sound teleportSound;
    private final Sound openGuiSound;
    private final boolean visualEffectsEnabled;
    private final Particle visualEffectType;

    public Navigator(LobbyLynx plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();

        // Update how we access the navigator item configuration
        Material itemType = Material.valueOf(config.getString("item.type", "COMPASS"));
        int itemSlot = config.getInt("item.slot", 0);
        this.navigatorItem = new ItemStack(itemType);
        ItemMeta meta = navigatorItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(config.getString("item.name", "Server Navigator"));
            navigatorItem.setItemMeta(meta);
        }

        this.navigatorName = config.getString("navigator.gui.name", "Navigator");
        this.guiSize = config.getInt("navigator.gui.size", 36);
        this.showPlayerCount = config.getBoolean("navigator.gui.customizable_icons", false);

        // Action settings
        this.closeActionEnabled = config.getBoolean("navigator.close_action.enabled", false);
        this.teleportMessageEnabled = config.getBoolean("navigator.teleport_message.enabled", true);

        // Lobby spawn setup
        ConfigurationSection lobbySpawn = config.getConfigurationSection("navigator.lobby_spawn");
        if (lobbySpawn != null) {
            this.lobbySpawnSlot = lobbySpawn.getInt("slot", guiSize - 1);
            String worldName = lobbySpawn.getString("world", "world");
            double[] coords = lobbySpawn.getDoubleList("coordinates").stream().mapToDouble(Double::doubleValue).toArray();
            this.lobbySpawnLocation = new Location(Bukkit.getWorld(worldName), coords[0], coords[1], coords[2]);
        } else {
            this.lobbySpawnSlot = guiSize - 1;
            this.lobbySpawnLocation = new Location(Bukkit.getWorlds().get(0), 0, 64, 0);
        }



        // Sounds
        Sound tempTeleportSound;
        Sound tempOpenGuiSound;
        try {
            tempTeleportSound = Sound.valueOf(config.getString("navigator.sounds.teleport", "ENTITY_ENDERMAN_TELEPORT"));
            tempOpenGuiSound = Sound.valueOf(config.getString("navigator.sounds.open_gui", "ENTITY_PLAYER_LEVELUP"));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound name in config. Using default sounds.");
            tempTeleportSound = Sound.ENTITY_ENDERMAN_TELEPORT;
            tempOpenGuiSound = Sound.ENTITY_PLAYER_LEVELUP;
        }
        this.teleportSound = tempTeleportSound;
        this.openGuiSound = tempOpenGuiSound;

        // Visual Effects
        this.visualEffectsEnabled = config.getBoolean("navigator.visual_effects.enabled", false);
        this.visualEffectType = Particle.valueOf(config.getString("navigator.visual_effects.type", "SMOKE"));

        // Register the listener
        plugin.getServer().getPluginManager().registerEvents(new NavigatorListener(plugin, this), plugin);
    }

    public void giveNavigatorItem(Player player, int slot) {
        player.getInventory().setItem(slot, navigatorItem);
    }

    public void openNavigator(Player player) {
        Inventory gui = Bukkit.createInventory(null, guiSize, navigatorName);
        populateGUI(gui);

        player.openInventory(gui);
        player.playSound(player.getLocation(), openGuiSound, 1.0f, 1.0f);
    }

    private void populateGUI(Inventory gui) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection iconsSection = config.getConfigurationSection("navigator.icons");

        if (iconsSection != null) {
            for (String key : iconsSection.getKeys(false)) {
                ConfigurationSection icon = iconsSection.getConfigurationSection(key);
                if (icon != null) {
                    String name = icon.getString("name");
                    int slot = icon.getInt("slot");
                    Material material = Material.valueOf(icon.getString("type", "COMPASS"));
                    ItemStack item = new ItemStack(material);

                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(name);
                        if (showPlayerCount) {
                            World world = Bukkit.getWorld(key);
                            int playerCount = world != null ? world.getPlayers().size() : 0;
                            meta.setLore(Collections.singletonList("Players: " + playerCount));
                        }
                        item.setItemMeta(meta);
                    }
                    gui.setItem(slot, item);
                }
            }
        }

        // Add lobby spawn item
        ItemStack lobbyItem = new ItemStack(Material.valueOf(plugin.getConfig().getString("navigator.lobby_spawn.item", "COMPASS")));
        ItemMeta lobbyMeta = lobbyItem.getItemMeta();
        if (lobbyMeta != null) {
            lobbyMeta.setDisplayName("Lobby Spawn");
            lobbyItem.setItemMeta(lobbyMeta);
        }
        gui.setItem(lobbySpawnSlot, lobbyItem);
    }

    public boolean isNavigatorItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || item.getType() != navigatorItem.getType()) {
            return false;
        }
        ItemMeta itemMeta = item.getItemMeta();
        ItemMeta navMeta = navigatorItem.getItemMeta();
        return itemMeta != null && navMeta != null &&
                itemMeta.getDisplayName().equals(navMeta.getDisplayName());
    }

    public void teleportToLobbySpawn(Player player) {
        if (teleportMessageEnabled) {
            player.sendMessage(ChatColor.GREEN + "Teleporting to Lobby Spawn...");
        }
        player.teleport(lobbySpawnLocation);
        player.playSound(player.getLocation(), teleportSound, 1.0f, 1.0f);

        if (visualEffectsEnabled) {
            player.getWorld().spawnParticle(visualEffectType, player.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
        }
    }

    public String getNavigatorName() {
        return navigatorName;
    }

    public boolean isCloseActionEnabled() {
        return closeActionEnabled;
    }
}