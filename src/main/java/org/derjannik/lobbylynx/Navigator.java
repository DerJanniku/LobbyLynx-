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

        // Initialize navigator item
        this.navigatorItem = createNavigatorItem(config);

        // Initialize basic settings
        this.navigatorName = config.getString("navigator.gui.name", "Navigator");
        this.guiSize = validateGuiSize(config.getInt("navigator.gui.size", 36));
        this.showPlayerCount = config.getBoolean("navigator.gui.customizable_icons", false);
        this.closeActionEnabled = config.getBoolean("navigator.close_action.enabled", false);
        this.teleportMessageEnabled = config.getBoolean("navigator.teleport.message.enabled", true);

        // Initialize lobby spawn
        LobbySpawnData spawnData = initializeLobbySpawn(config);
        this.lobbySpawnSlot = spawnData.slot;
        this.lobbySpawnLocation = spawnData.location;

        // Initialize sounds
        SoundData soundData = initializeSounds(config);
        this.teleportSound = soundData.teleportSound;
        this.openGuiSound = soundData.openGuiSound;

        // Initialize visual effects
        VisualEffectData effectData = initializeVisualEffects(config);
        this.visualEffectsEnabled = effectData.enabled;
        this.visualEffectType = effectData.effectType;
    }

    private ItemStack createNavigatorItem(FileConfiguration config) {
        Material itemType;
        try {
            itemType = Material.valueOf(config.getString("navigator.item.type", "COMPASS"));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid item type in config. Using default COMPASS.");
            itemType = Material.COMPASS;
        }

        ItemStack item = new ItemStack(itemType);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    config.getString("navigator.item.name", "§6Server Navigator")));
            item.setItemMeta(meta);
        }
        return item;
    }

    private int validateGuiSize(int size) {
        // Ensure GUI size is a multiple of 9 and between 9 and 54
        if (size % 9 != 0 || size < 9 || size > 54) {
            plugin.getLogger().warning("Invalid GUI size in config. Using default size 36.");
            return 36;
        }
        return size;
    }

    public ItemStack getNavigatorItem() {
        return navigatorItem.clone();
    }

    private static class LobbySpawnData {
        final int slot;
        final Location location;

        LobbySpawnData(int slot, Location location) {
            this.slot = slot;
            this.location = location;
        }
    }

    private LobbySpawnData initializeLobbySpawn(FileConfiguration config) {
        ConfigurationSection lobbySpawn = config.getConfigurationSection("navigator.lobby_spawn");
        int slot = guiSize - 1;
        Location location;

        if (lobbySpawn != null) {
            slot = lobbySpawn.getInt("slot", guiSize - 1);
            String worldName = lobbySpawn.getString("world", "world");
            World world = Bukkit.getWorld(worldName);

            if (world != null) {
                location = new Location(
                        world,
                        lobbySpawn.getDouble("coordinates.x", 0),
                        lobbySpawn.getDouble("coordinates.y", 64),
                        lobbySpawn.getDouble("coordinates.z", 0),
                        (float) lobbySpawn.getDouble("coordinates.yaw", 0),
                        (float) lobbySpawn.getDouble("coordinates.pitch", 0)
                );
            } else {
                plugin.getLogger().warning("World '" + worldName + "' not found. Using default world spawn.");
                location = Bukkit.getWorlds().get(0).getSpawnLocation();
            }
        } else {
            plugin.getLogger().warning("No lobby spawn configuration found. Using default world spawn.");
            location = Bukkit.getWorlds().get(0).getSpawnLocation();
        }

        return new LobbySpawnData(slot, location);
    }

    private static class SoundData {
        final Sound teleportSound;
        final Sound openGuiSound;

        SoundData(Sound teleportSound, Sound openGuiSound) {
            this.teleportSound = teleportSound;
            this.openGuiSound = openGuiSound;
        }
    }

    private SoundData initializeSounds(FileConfiguration config) {
        Sound teleport, openGui;
        try {
            teleport = Sound.valueOf(config.getString("navigator.sounds.teleport", "ENTITY_ENDERMAN_TELEPORT"));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid teleport sound in config. Using default sound.");
            teleport = Sound.ENTITY_ENDERMAN_TELEPORT;
        }

        try {
            openGui = Sound.valueOf(config.getString("navigator.sounds.open_gui", "BLOCK_CHEST_OPEN"));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid GUI open sound in config. Using default sound.");
            openGui = Sound.BLOCK_CHEST_OPEN;
        }

        return new SoundData(teleport, openGui);
    }

    private static class VisualEffectData {
        final boolean enabled;
        final Particle effectType;

        VisualEffectData(boolean enabled, Particle effectType) {
            this.enabled = enabled;
            this.effectType = effectType;
        }
    }

    private VisualEffectData initializeVisualEffects(FileConfiguration config) {
        boolean enabled = config.getBoolean("navigator.visual_effects.enabled", false);
        Particle effectType;
        try {
            effectType = Particle.valueOf(config.getString("navigator.visual_effects.type", "PORTAL"));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle effect type in config. Using default PORTAL effect.");
            effectType = Particle.PORTAL;
        }
        return new VisualEffectData(enabled, effectType);
    }

    public void giveNavigatorItem(Player player) {
        if (player != null) {
            player.getInventory().setItem(
                    plugin.getConfig().getInt("navigator.item.slot", 0),
                    navigatorItem.clone()
            );
        }
    }

    public void openNavigator(Player player) {
        if (player == null) return;

        Inventory gui = Bukkit.createInventory(null, guiSize, navigatorName);
        populateGUI(gui);

        player.openInventory(gui);
        player.playSound(player.getLocation(), openGuiSound, 1.0f, 1.0f);
    }

    private void populateGUI(Inventory gui) {
        if ( gui == null) return;

        ConfigurationSection iconsSection = plugin.getConfig().getConfigurationSection("minigames");
        if (iconsSection != null) {
            for (String key : iconsSection.getKeys(false)) {
                ConfigurationSection icon = iconsSection.getConfigurationSection(key);
                if (icon != null) {
                    String name = icon.getString("name");
                    int slot = icon.getInt("slot");
                    Material material;
                    try {
                        material = Material.valueOf(icon.getString("item", "COMPASS"));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid item type for '" + name + "'. Using default COMPASS.");
                        material = Material.COMPASS;
                    }

                    ItemStack item = new ItemStack(material);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(name);
                        if (showPlayerCount) {
                            World world = Bukkit.getWorld(icon.getString("coordinates.world", "world"));
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
        ItemStack lobbyItem = new ItemStack(Material.valueOf(plugin.getConfig().getString("navigator.lobby_spawn.item", "NETHER_STAR")));
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
        if (player == null) return;

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