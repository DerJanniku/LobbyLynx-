
package org.derjannik.lobbyLynx.managers;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.derjannik.lobbyLynx.LobbyLynx;
import java.util.List;
import java.util.logging.Level;

public class ConfigManager {
    private final LobbyLynx plugin;
    private FileConfiguration config;

    public ConfigManager(LobbyLynx plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public boolean loadConfig() {
        try {
            plugin.saveDefaultConfig();
            plugin.reloadConfig();
            config = plugin.getConfig();
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load config", e);
            return false;
        }
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public boolean isJoinMessageEnabled() {
        return config.getBoolean("messages.join.enabled", true);
    }

    public String getJoinMessage() {
        return config.getString("messages.join.message", "§e%player% joined the game");
    }

    public boolean isFlightEnabled() {
        return config.getBoolean("lobby.flight-enabled", false);
    }

    public boolean isPvPEnabled() {
        return config.getBoolean("lobby.pvp-enabled", false);
    }

    public long getLobbyTime() {
        return config.getLong("lobby.time", 6000L);
    }

    public GameMode getDefaultGameMode() {
        try {
            return GameMode.valueOf(config.getString("lobby.default-gamemode", "ADVENTURE").toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid gamemode in config, defaulting to ADVENTURE");
            return GameMode.ADVENTURE;
        }
    }

    public boolean isTeleportToSpawnEnabled() {
        return config.getBoolean("lobby.teleport-to-spawn", true);
    }

    public Location getSpawnLocation() {
        try {
            String worldName = config.getString("lobby.spawn.world", "world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                throw new IllegalStateException("World '" + worldName + "' not found");
            }

            double x = config.getDouble("lobby.spawn.x", 0.0);
            double y = config.getDouble("lobby.spawn.y", 64.0);
            double z = config.getDouble("lobby.spawn.z", 0.0);
            float yaw = (float) config.getDouble("lobby.spawn.yaw", 0.0);
            float pitch = (float) config.getDouble("lobby.spawn.pitch", 0.0);

            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error getting spawn location, using world spawn", e);
            return Bukkit.getWorlds().get(0).getSpawnLocation();
        }
    }

    public List<String> getServerNames() {
        return config.getStringList("server-signs.servers");
    }

    @SuppressWarnings("unchecked")
    public List<List<String>> getAnimationFrames() {
        List<?> rawList = config.getList("server-signs.animation-frames", List.of());
        if (rawList == null) {
            return List.of();
        }
        try {
            return (List<List<String>>) rawList;
        } catch (ClassCastException e) {
            plugin.getLogger().warning("Invalid animation frames format in config");
            return List.of();
        }
    }

    public String getTablistHeader() {
        return config.getString("tablist.header", "§6Welcome to the server!");
    }

    public String getTablistFooter() {
        return config.getString("tablist.footer", "§eonline: %online%/%max%");
    }

    public List<String> getScoreboardLines() {
        return config.getStringList("scoreboard.lines");
    }

    public String getScoreboardTitle() {
        return config.getString("scoreboard.title", "§6§lLOBBY");
    }

    public boolean getGameRule(String rule) {
        return config.getBoolean("gamerules." + rule, false);
    }

    public void setDefaultGameRules() {
        config.set("gamerules.blockBreaking", false);
        config.set("gamerules.blockPlacement", false);
        config.set("gamerules.disableElytra", true);
        config.set("gamerules.tnt", false);
        plugin.saveConfig();
    }

    public void applyGameRules() {
        plugin.setBlockBreakingAllowed(getGameRule("blockBreaking"));
        plugin.setBlockPlacementAllowed(getGameRule("blockPlacement"));
        plugin.setElytraAllowed(!getGameRule("disableElytra"));
        plugin.setTNTExplosionsAllowed(getGameRule("tnt"));
    }

    // Navigator methods
    public String getNavigatorTitle() {
        return config.getString("navigator.title", "§6Navigator");
    }

    public int getNavigatorSize() {
        return config.getInt("navigator.size", 27);
    }

    public void setMinigame(String name, int slot, String world, double x, double y, double z, String item) {
        config.set("minigames." + name + ".slot", slot);
        config.set("minigames." + name + ".world", world);
        config.set("minigames." + name + ".x", x);
        config.set("minigames." + name + ".y", y);
        config.set("minigames." + name + ".z", z);
        config.set("minigames." + name + ".item", item);
        plugin.saveConfig();
    }

    public void setMinigame(String name, int slot, String world, Location location) {
        setMinigame(name, slot, world, location.getX(), location.getY(), location.getZ(), "GRASS_BLOCK");
    }

    public void deleteMinigame(String name) {
        config.set("minigames." + name, null);
        plugin.saveConfig();
    }

    public String getMinigameWorld(String minigame) {
        return config.getString("minigames." + minigame + ".world");
    }

    public double getMinigameX(String minigame) {
        return config.getDouble("minigames." + minigame + ".x");
    }

    public double getMinigameY(String minigame) {
        return config.getDouble("minigames." + minigame + ".y");
    }

    public double getMinigameZ(String minigame) {
        return config.getDouble("minigames." + minigame + ".z");
    }

    public int getMinigameSlot(String minigame) {
        return config.getInt("minigames." + minigame + ".slot", 0);
    }

    public String getMinigameItem(String minigame) {
        return config.getString("minigames." + minigame + ".item", "GRASS_BLOCK");
    }

    public String getMinigameName(String minigame) {
        return config.getString("minigames." + minigame + ".name", minigame);
    }

    public int getLobbySpawnSlot() {
        return config.getInt("lobby.spawn_slot", 4);
    }

    public String getLobbySpawnItem() {
        return config.getString("lobby.spawn_item", "NETHER_STAR");
    }

    // Settings methods
    public String getSettingsTitle() {
        return config.getString("settings.title", "§6Settings");
    }

    public int getSettingsSize() {
        return config.getInt("settings.size", 27);
    }

    public void setGameRule(String rule, boolean value) {
        config.set("gamerules." + rule, value);
        plugin.saveConfig();
    }

    // Messages
    public String getQuitMessage() {
        return config.getString("messages.quit", "§c%player% left the game");
    }

    public boolean isTeleportMessageEnabled() {
        return config.getBoolean("messages.teleport.enabled", true);
    }

    public String getTeleportMessage() {
        return config.getString("messages.teleport.message", "§aTeleported to %location%");
    }

    // New methods added
    public Location getLobbySpawn() {
        return getSpawnLocation();
    }

    public String getLobbyWorld() {
        return config.getString("lobby.spawn.world", "world");
    }

    public double getLobbyX() {
        return config.getDouble("lobby.spawn.x", 0.0);
    }

    public double getLobbyY() {
        return config.getDouble("lobby.spawn.y", 64.0);
    }

    public double getLobbyZ() {
        return config.getDouble("lobby.spawn.z", 0.0);
    }

    public void setLobbySpawn(Location location) {
        config.set("lobby.spawn.world", location.getWorld().getName());
        config.set("lobby.spawn.x", location.getX());
        config.set("lobby.spawn.y", location.getY());
        config.set("lobby.spawn.z", location.getZ());
        config.set("lobby.spawn.yaw", location.getYaw());
        config.set("lobby.spawn.pitch", location.getPitch());
        plugin.saveConfig();
    }

    public List<String> getMinigames() {
        return config.getStringList("minigames");
    }

    // Tablist methods
    public String getTablistRankPrefix(String rank) {
        return config.getString("tablist.player_display.rank_prefixes." + rank,
                              config.getString("tablist.player_display.default_prefix", "&7[User] "));
    }

    public String getTablistRankIcon(String rank) {
        return config.getString("tablist.icons.icons_by_rank." + rank,
                              config.getString("tablist.icons.default_icon", "⬤"));
    }

    public boolean isTablistConnectionStrengthEnabled() {
        return config.getBoolean("tablist.player_display.show_connection_strength", true);
    }
}
