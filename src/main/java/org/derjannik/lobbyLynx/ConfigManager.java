package org.derjannik.lobbyLynx;

import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private final LobbyLynx plugin;
    private FileConfiguration config;

    public ConfigManager(LobbyLynx plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    // Lobby settings
    public boolean isFlightEnabled() {
        return config.getBoolean("lobby_settings.flight_enabled", false);
    }

    // Retrieve the sign format for server signs
    public List<String> getSignFormat() {
        List<String> format = config.getStringList("server-signs.format");
        if (format.isEmpty()) {
            format.add("&8[&bServer&8]");
            format.add("%server%");
            format.add("%players%/%maxplayers%");
            format.add("&aClick to join!");
        }
        return format;
    }

    // Retrieve animation frames for server signs
    public List<List<String>> getAnimationFrames() {
        List<List<String>> frames = new ArrayList<>();
        List<String> rawFrames = config.getStringList("server-signs.animation-frames");
        for (String frame : rawFrames) {
            frames.add(List.of(frame.split("\\|")));
        }
        return frames;
    }

    // Retrieve the list of configured server names
    public List<String> getServerNames() {
        ConfigurationSection serversSection = config.getConfigurationSection("server-signs.servers");
        if (serversSection != null) {
            return new ArrayList<>(serversSection.getKeys(false));
        }
        return new ArrayList<>();
    }

    // Retrieve information for a specific server sign location
    public Location getServerSignLocation(String server, String signKey) {
        ConfigurationSection signSection = config.getConfigurationSection("server-signs.servers." + server + ".signs." + signKey);
        if (signSection != null) {
            World world = plugin.getServer().getWorld(signSection.getString("world"));
            if (world != null) {
                double x = signSection.getDouble("x");
                double y = signSection.getDouble("y");
                double z = signSection.getDouble("z");
                return new Location(world, x, y, z);
            }
        }
        return null;
    }

    // Save a new server sign location in the config
    public void saveServerSign(String server, String signKey, Location location) {
        String path = "server-signs.servers." + server + ".signs." + signKey;
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        plugin.saveConfig();
    }

    // Remove a server sign from the config
    public void removeServerSign(String server, String signKey) {
        config.set("server-signs.servers." + server + ".signs." + signKey, null);
        plugin.saveConfig();
    }

    // Default Game Rules configuration (existing methods)
    public void setDefaultGameRules() {
        ConfigurationSection gameRulesSection = config.getConfigurationSection("game_rules");
        if (gameRulesSection == null) {
            gameRulesSection = config.createSection("game_rules");
        }

        // Set default game rules
        gameRulesSection.addDefault("fallDamage", false);
        gameRulesSection.addDefault("pvp", false);
        gameRulesSection.addDefault("tnt", false);
        gameRulesSection.addDefault("mobGriefing", false);
        gameRulesSection.addDefault("doDaylightCycle", false);
        gameRulesSection.addDefault("doWeatherCycle", false);
        gameRulesSection.addDefault("keepInventory", true);
        gameRulesSection.addDefault("naturalRegeneration", false);
        gameRulesSection.addDefault("disableElytra", true);
        gameRulesSection.addDefault("doMobSpawning", false);
        gameRulesSection.addDefault("keepSpawnInMemory", false);
        gameRulesSection.addDefault("commandBlockOutput", false);
        gameRulesSection.addDefault("fireSpread", false);
        gameRulesSection.addDefault("blockBreaking", false);
        gameRulesSection.addDefault("blockPlacement", false);
        gameRulesSection.addDefault("doTileDrops", false);
        gameRulesSection.addDefault("doImmediateRespawn", true);
        gameRulesSection.addDefault("showDeathMessages", false);
        gameRulesSection.addDefault("disableRaids", true);
        gameRulesSection.addDefault("doInsomnia", false);
        gameRulesSection.addDefault("logAdminCommands", false);
        gameRulesSection.addDefault("doPatrolSpawning", false);
        gameRulesSection.addDefault("doLimitedCrafting", true);
        gameRulesSection.addDefault("doMobLoot", false);

        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    // Game rules and additional settings for lobby and minigame (existing methods)
    // ... [additional methods for retrieving various lobby and minigame settings] ...

    public void applyGameRules() {
        for (World world : plugin.getServer().getWorlds()) {
            for (GameRule<?> rule : GameRule.values()) {
                String ruleName = rule.getName().toLowerCase();
                if (config.contains("game_rules." + ruleName)) {
                    if (rule.getType() == Boolean.class) {
                        @SuppressWarnings("unchecked")
                        GameRule<Boolean> booleanRule = (GameRule<Boolean>) rule;
                        world.setGameRule(booleanRule, getGameRule(ruleName));
                    } else if (rule.getType() == Integer.class) {
                        @SuppressWarnings("unchecked")
                        GameRule<Integer> intRule = (GameRule<Integer>) rule;
                        world.setGameRule(intRule, getGameRuleInt(ruleName));
                    }
                }
            }
            // Apply custom rules
            applyCustomRules(world);
        }
    }

    private void applyCustomRules(World world) {
        applyFallDamage(world);
        applyPvP(world);
        applyTNT(world);
        applyBlockBreaking(world);
        applyBlockPlacement(world);
        applyDisableElytra(world);
    }

    private void applyFallDamage(World world) {
        boolean fallDamage = getGameRule("fallDamage");
        world.setGameRule(GameRule.FALL_DAMAGE, fallDamage);
    }

    private void applyPvP(World world) {
        boolean pvp = getGameRule("pvp");
        world.setPVP(pvp);
    }

    private void applyTNT(World world) {
        boolean tnt = getGameRule("tnt");
        plugin.setTNTExplosionsAllowed(tnt);
        plugin.getLogger().info("TNT explosions are " + (tnt ? "enabled" : "disabled") + " in world: " + world.getName());
    }

    private void applyBlockBreaking(World world) {
        boolean blockBreaking = getGameRule("blockBreaking");
        // This is a custom rule, so we'll need to implement it in the main plugin
        plugin.setBlockBreakingAllowed(blockBreaking);
    }

    private void applyBlockPlacement(World world) {
        boolean blockPlacement = getGameRule("blockPlacement");
        // This is a custom rule, so we'll need to implement it in the main plugin
        plugin.setBlockPlacementAllowed(blockPlacement);
    }

    private void applyDisableElytra(World world) {
        boolean disableElytra = getGameRule("disableElytra");
        // This is a custom rule, so we'll need to implement it in the main plugin
        plugin.setElytraAllowed(!disableElytra);
    }

    public Boolean getGameRule(String ruleName) {
        return config.getBoolean("game_rules." + ruleName, false); // Default to false if not set
    }

    public void setGameRule(String ruleName, Boolean value) {
        config.set("game_rules." + ruleName, value);
        plugin.saveConfig(); // Save the config after setting the value
        // Apply the rule immediately
        for (World world : plugin.getServer().getWorlds()) {
            applyCustomRules(world);
        }
    }

    public boolean isPvPEnabled() {
        return config.getBoolean("lobby_settings.pvp_enabled", false);
    }

    public long getLobbyTime() {
        return config.getLong("lobby_settings.time", 6000); // Default to 6000 if not set
    }

    private Integer getGameRuleInt(String ruleName) {
        return config.getInt("game_rules." + ruleName, 0); // Default to 0 if not set
    }

    // Navigator GUI settings
    public String getNavigatorTitle() {
        return config.getString("navigator.gui.title", "Navigator");
    }

    public int getNavigatorSize() {
        return config.getInt("navigator.gui.size", 45);
    }

    public int getLobbySpawnSlot() {
        return config.getInt("navigator.lobby_spawn.slot", 22);
    }

    public String getLobbySpawnItem() {
        return config.getString ("navigator.lobby_spawn.item", "NETHER_STAR");
    }

    public String getLobbyWorld() {
        return config.getString("navigator.lobby_spawn.world", "world");
    }

    public double getLobbyX() {
        return config.getDouble("navigator.lobby_spawn.x", 0.0);
    }

    public double getLobbyY() {
        return config.getDouble("navigator.lobby_spawn.y", 90.0);
    }

    public double getLobbyZ() {
        return config.getDouble("navigator.lobby_spawn.z", 0.0);
    }

    public String getWelcomeMessage() {
        return config.getString("navigator.notifications.welcome_message", "Welcome to the Lobby!");
    }

    public boolean isNewPlayersOnly() {
        return config.getBoolean("navigator.notifications.new_players_only", true);
    }

    public boolean isAlwaysNotify() {
        return config.getBoolean("navigator.notifications.always", true);
    }

    public boolean isTeleportMessageEnabled() {
        return config.getBoolean("navigator.notifications.teleport_message_enabled", true);
    }

    public String getTeleportMessage() {
        return config.getString("navigator.notifications.teleport_message", "Teleported to %server%");
    }

    public List<String> getNavigatorItems() {
        return config.getStringList("navigator.items");
    }

    public String getNavigatorItemName(String item) {
        return config.getString("navigator.items." + item + ".name", item);
    }

    public String getNavigatorItemMaterial(String item) {
        return config.getString("navigator.items." + item + ".material", "STONE");
    }

    public int getNavigatorItemSlot(String item) {
        return config.getInt("navigator.items." + item + ".slot", 0);
    }

    public String getNavigatorItemServer(String item) {
        return config.getString("navigator.items." + item + ".server", "");
    }

    public List<String> getNavigatorItemLore(String item) {
        return config.getStringList("navigator.items." + item + ".lore");
    }

    // Gamerule GUI settings
    public String getGameruleTitle() {
        return config.getString("gamerule_gui.title", "Game Rules");
    }

    public int getGameruleSize() {
        return config.getInt("gamerule_gui.size", 54);
    }

    // Settings GUI settings
    public String getSettingsTitle() {
        return config.getString("settings_gui.title", "Settings");
    }

    public int getSettingsSize() {
        return config.getInt("settings_gui.size", 27);
    }

    // Minigame settings
    public List<String> getMinigames() {
        ConfigurationSection minigamesSection = config.getConfigurationSection("minigames");
        if (minigamesSection != null) {
            return new ArrayList<>(minigamesSection.getKeys(false));
        }
        return new ArrayList<>();
    }

    public String getMinigameName(String minigame) {
        return config.getString("minigames." + minigame + ".name", minigame);
    }

    public int getMinigameSlot(String minigame) {
        return config.getInt("minigames." + minigame + ".slot", 0);
    }

    public String getMinigameItem(String minigame) {
        return config.getString("minigames." + minigame + ".item", "GRASS_BLOCK");
    }

    public String getMinigameWorld(String minigame) {
        return config.getString("minigames." + minigame + ".world", "world");
    }

    public double getMinigameX(String minigame) {
        return config.getDouble("minigames." + minigame + ".x", 100.0);
    }

    public double getMinigameY(String minigame) {
        return config.getDouble("minigames." + minigame + ".y", 64.0);
    }

    public double getMinigameZ(String minigame) {
        return config.getDouble("minigames." + minigame + ".z", 100.0);
    }

    public List<String> getServerSignFormat() {
        return getConfig().getStringList("server-signs.format");
    }

    public int getServerSignUpdateInterval() {
        return getConfig().getInt("server-signs.update-interval", 20);
    }

    public long getServerSignCooldown() {
        return getConfig().getLong("server-signs.cooldown", 3000);
    }

    public List<String> getServerSignAnimationFrames() {
        return getConfig().getStringList("server-signs.animation-frames");
    }

    public ConfigurationSection getServerSignsSection() {
        return getConfig().getConfigurationSection("server-signs.servers");
    }

    // Messages
    public String getJoinMessage() {
        return config.getString("messages.join", "&aWelcome to the server, %player%!");
    }

    public String getQuitMessage() {
        return config.getString("messages.quit", "&e%player% has left the server.");
    }

    // Tablist settings
    public String getTablistHeader() {
        return config.getString("tablist.header", "&6YourNetwork.net");
    }

    public String getTablistFooter() {
        return config.getString("tablist.footer", "&ayourserver.com");
    }

    // Scoreboard settings
    public String getScoreboardTitle() {
        return config.getString("scoreboard.title", "&eYourServer - Lobby");
    }

    public List<String> getScoreboardLines() {
        return config.getStringList("scoreboard.lines");
    }

    // Utility methods
// In ConfigManager.java
    public void setMinigame(String name, int slot, String item, Location location) {
        config.set("minigames." + name + ".name", name);
        config.set("minigames." + name + ".slot", slot);
        config.set("minigames." + name + ".item", item);
        config.set("minigames." + name + ".world", location.getWorld().getName());
        config.set("minigames." + name + ".x", location.getX());
        config.set("minigames." + name + ".y", location.getY());
        config.set("minigames." + name + ".z", location.getZ());
        plugin.saveConfig();
    }

    public void setLobbySpawn(Location location) {
        config.set("navigator.lobby_spawn.world", location.getWorld().getName());
        config.set("navigator.lobby_spawn.x", location.getX());
        config.set("navigator.lobby_spawn.y", location.getY());
        config.set("navigator.lobby_spawn.z", location.getZ());
        config.set("navigator.lobby_spawn.yaw", location.getYaw());
        config.set("navigator.lobby_spawn.pitch", location.getPitch());
        plugin.saveConfig();
    }

    public Location getLobbySpawn() {
        World world = plugin.getServer().getWorld(getLobbyWorld());
        if (world == null) {
            world = plugin.getServer().getWorlds().get(0); // Fallback to the first world if the specified one is not found
        }
        return new Location(world, getLobbyX(), getLobbyY(), getLobbyZ());
    }
}