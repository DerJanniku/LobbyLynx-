package org.derjannik.lobbyLynx;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.derjannik.lobbyLynx.commands.*;
import org.derjannik.lobbyLynx.gui.*;
import org.derjannik.lobbyLynx.listeners.*;
import org.derjannik.lobbyLynx.managers.*;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

public class LobbyLynx extends JavaPlugin {
    private volatile ConfigManager configManager;
    private volatile CustomScoreboard customScoreboard;
    private volatile CustomTablist customTablist;
    private volatile ServerSignManager serverSignManager;
    private volatile FriendManager friendManager;
    private volatile FriendGUI friendGUI;
    private volatile AdvertisementManager advertisementManager;
    private volatile CosmeticManager cosmeticManager;
    private volatile CosmeticGUI cosmeticGUI;

    private volatile boolean blockBreakingAllowed;
    private volatile boolean blockPlacementAllowed;
    private volatile boolean elytraAllowed;
    private volatile boolean tntExplosionsAllowed;
    private volatile boolean isEnabled = false;

    @Override
    public void onEnable() {
        try {
            // Initialize configuration first
            saveDefaultConfig();
            reloadConfig();

            // Initialize managers in correct order
            try {
                if (!initializeManagers()) {
                    shutdownPlugin("Failed to initialize managers");
                    return;
                }
            } catch (Exception e) {
                shutdownPlugin("Failed to initialize managers: " + e.getMessage());
                return;
            }

            // Initialize commands after managers
            try {
                if (!initializeCommands()) {
                    shutdownPlugin("Failed to initialize commands");
                    return;
                }
            } catch (Exception e) {
                shutdownPlugin("Failed to initialize commands: " + e.getMessage());
                return;
            }

            // Register event listeners
            try {
                registerEventListeners();
            } catch (Exception e) {
                shutdownPlugin("Failed to register event listeners: " + e.getMessage());
                return;
            }

            // Initialize BungeeCord integration
            try {
                initializeBungeeIntegration();
            } catch (Exception e) {
                getLogger().warning("Failed to initialize BungeeCord integration: " + e.getMessage());
                // Don't disable plugin for BungeeCord failure
            }

            // Apply settings
            try {
                applyLobbySettings();
                configManager.setDefaultGameRules();
                configManager.applyGameRules();
            } catch (Exception e) {
                getLogger().severe("Failed to apply lobby settings: " + e.getMessage());
                e.printStackTrace();
            }

            // Start sign animation
            try {
                startSignAnimationUpdater();
            } catch (Exception e) {
                getLogger().warning("Failed to start sign animation: " + e.getMessage());
                // Don't disable plugin for animation failure
            }

            isEnabled = true;
            getLogger().info("LobbyLynx has been successfully enabled!");
        } catch (Exception e) {
            shutdownPlugin("Unexpected error during plugin startup: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        isEnabled = false;
        
        try {
            // Cleanup managers in reverse order of initialization
            if (cosmeticManager != null) {
                cosmeticManager.cleanup();
                cosmeticManager = null;
            }
            if (advertisementManager != null) {
                advertisementManager.cleanup();
                advertisementManager = null;
            }
            if (serverSignManager != null) {
                serverSignManager.cleanup();
                serverSignManager = null;
            }
            if (customTablist != null) {
                customTablist.cleanup();
                customTablist = null;
            }
            if (customScoreboard != null) {
                customScoreboard.cleanup();
                customScoreboard = null;
            }
            if (friendManager != null) {
                friendManager.cleanup();
                friendManager = null;
            }

            // Cancel all tasks
            getServer().getScheduler().cancelTasks(this);
            
            // Unregister all listeners and channels
            getServer().getMessenger().unregisterOutgoingPluginChannel(this);
            
            getLogger().info("LobbyLynx has been successfully disabled!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error during plugin shutdown", e);
        }
    }

    private boolean initializeManagers() {
        try {
            // Initialize ConfigManager first as other managers depend on it
            configManager = new ConfigManager(this);
            boolean configLoaded = configManager.loadConfig();
            if (!configLoaded) {
                getLogger().severe("Failed to load configuration!");
                return false;
            }

            // Initialize managers with proper error handling
            try {
                cosmeticManager = new CosmeticManager(this);
                cosmeticGUI = new CosmeticGUI(this, cosmeticManager);
            } catch (Exception e) {
                getLogger().severe("Failed to initialize Cosmetic system: " + e.getMessage());
                return false;
            }

            try {
                advertisementManager = new AdvertisementManager(this);
            } catch (Exception e) {
                getLogger().severe("Failed to initialize Advertisement system: " + e.getMessage());
                return false;
            }

            try {
            friendManager = new FriendManager(this);
            friendGUI = new FriendGUI(this, friendManager);
            } catch (Exception e) {
                getLogger().severe("Failed to initialize Friend system: " + e.getMessage());
                return false;
            }

            try {
                customScoreboard = new CustomScoreboard(this, configManager);
                customTablist = new CustomTablist(this, configManager);
            } catch (Exception e) {
                getLogger().severe("Failed to initialize Scoreboard/Tablist: " + e.getMessage());
                return false;
            }

            try {
                serverSignManager = new ServerSignManager(this, configManager);
            } catch (Exception e) {
                getLogger().severe("Failed to initialize Server Sign system: " + e.getMessage());
                return false;
            }

            // Initialize game rules with validation
            try {
                loadGameRules();
            } catch (Exception e) {
                getLogger().severe("Failed to initialize game rules: " + e.getMessage());
                return false;
            }

            return true;
        } catch (Exception e) {
            getLogger().severe("Unexpected error during manager initialization: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void loadGameRules() {
        blockBreakingAllowed = configManager.getGameRule("blockBreaking");
        blockPlacementAllowed = configManager.getGameRule("blockPlacement");
        elytraAllowed = !configManager.getGameRule("disableElytra");
        tntExplosionsAllowed = configManager.getGameRule("tnt");
    }

    private boolean initializeCommands() {
        try {
            // Register main commands
            getCommand("friend").setExecutor(new FriendCommand(this, friendManager));
            getCommand("lynx").setExecutor(new LynxCommand(this, configManager, friendManager));
            getCommand("ad").setExecutor(new AdvertisementCommand(this));
            getCommand("cosmetics").setExecutor((sender, command, label, args) -> {
                if (!isEnabled) {
                    sender.sendMessage("§cThe plugin is currently disabled.");
                    return true;
                }
                
                if (sender instanceof Player) {
                    try {
                        cosmeticGUI.openMainGUI((Player) sender);
                    } catch (Exception e) {
                        sender.sendMessage("§cFailed to open cosmetics menu: " + e.getMessage());
                        getLogger().warning("Error opening cosmetics menu for " + sender.getName() + ": " + e.getMessage());
                    }
                } else {
                    sender.sendMessage("§cThis command can only be used by players.");
                }
                return true;
            });

            // Register common commands
            CommonCommands commonCommands = new CommonCommands(this, configManager);
            registerOptionalCommands(commonCommands, "lobby", "hub", "spawn");

            return true;
        } catch (Exception e) {
            getLogger().severe("Failed to initialize commands: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void registerOptionalCommands(CommonCommands commands, String... commandNames) {
        for (String cmd : commandNames) {
            if (getCommand(cmd) != null) {
                getCommand(cmd).setExecutor(commands);
            }
        }
    }

    private void registerEventListeners() {
        try {
            PluginManager pluginManager = getServer().getPluginManager();
            pluginManager.registerEvents(new PlayerJoinListener(this, configManager, friendManager), this);
            pluginManager.registerEvents(new PlayerQuitListener(this, configManager), this);
            pluginManager.registerEvents(new SettingsGUI(this, configManager), this);
            pluginManager.registerEvents(new GameruleGUI(this, configManager), this);
            pluginManager.registerEvents(new NavigatorGUI(this, configManager), this);
            pluginManager.registerEvents(new CustomRuleListener(this), this);
            pluginManager.registerEvents(new TNTExplosionListener(this), this);
            pluginManager.registerEvents(new AdvertisementListener(this), this);
            pluginManager.registerEvents(new CosmeticsListener(this, cosmeticManager, configManager), this);
            pluginManager.registerEvents(friendGUI, this);
            pluginManager.registerEvents(cosmeticGUI, this);
            pluginManager.registerEvents(serverSignManager, this);
            
            getLogger().info("Successfully registered all event listeners");
        } catch (Exception e) {
            getLogger().severe("Failed to register event listeners: " + e.getMessage());
            throw e;
        }
    }

    private void initializeBungeeIntegration() {
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        startServerInfoUpdater();
    }

    private void startSignAnimationUpdater() {
        List<List<String>> animationFrames = configManager.getAnimationFrames();
        if (!animationFrames.isEmpty()) {
            Bukkit.getScheduler().runTaskTimer(this, () -> serverSignManager.animateSigns(animationFrames), 0L, 20L);
        }
    }

    private void startServerInfoUpdater() {
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (String serverName : configManager.getServerNames()) {
                updateServerInfo(serverName);
            }
        }, 0L, 200L); // Update every 10 seconds
    }

    private void updateServerInfo(String serverName) {
        int onlinePlayers = (int) (Math.random() * 100);  // Placeholder for server player data
        int maxPlayers = 100;
        String motd = "Welcome to " + serverName;
        serverSignManager.updateServerInfo(serverName, onlinePlayers, maxPlayers, motd);
    }

    private void shutdownPlugin(String reason) {
        getLogger().severe(reason);
        getServer().getPluginManager().disablePlugin(this);
    }

    public void reloadNavigator() {
        configManager.reloadConfig();
        new SettingsGUI(this, configManager).reloadGUI();
        new GameruleGUI(this, configManager).reloadGUI();
        new NavigatorGUI(this, configManager).reloadGUI();
        applyLobbySettings();
        configManager.applyGameRules();
    }

    private void applyLobbySettings() {
        boolean flightEnabled = configManager.isFlightEnabled();
        boolean pvpEnabled = configManager.isPvPEnabled();
        long lobbyTime = configManager.getLobbyTime();

        getServer().getOnlinePlayers().forEach(player -> player.setAllowFlight(flightEnabled));
        getServer().getWorlds().forEach(world -> {
            world.setPVP(pvpEnabled);
            world.setTime(lobbyTime);
        });
    }

    // Getters for managers and settings
    public ConfigManager getConfigManager() { return configManager; }
    public ServerSignManager getServerSignManager() { return serverSignManager; }
    public FriendManager getFriendManager() { return friendManager; }
    public FriendGUI getFriendGUI() { return friendGUI; }
    public CosmeticManager getCosmeticManager() { return cosmeticManager; }
    public AdvertisementManager getAdvertisementManager() { return advertisementManager; }

    // Game rule getters and setters
    public boolean isBlockBreakingAllowed() { return blockBreakingAllowed; }
    public void setBlockBreakingAllowed(boolean allowed) { this.blockBreakingAllowed = allowed; }
    public boolean isBlockPlacementAllowed() { return blockPlacementAllowed; }
    public void setBlockPlacementAllowed(boolean allowed) { this.blockPlacementAllowed = allowed; }
    public boolean isElytraAllowed() { return elytraAllowed; }
    public void setElytraAllowed(boolean allowed) { this.elytraAllowed = allowed; }
    public boolean areTNTExplosionsAllowed() { return tntExplosionsAllowed; }
    public void setTNTExplosionsAllowed(boolean allowed) { this.tntExplosionsAllowed = allowed; }
}
