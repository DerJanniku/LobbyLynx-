package org.derjannik.lobbyLynx;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.derjannik.lobbyLynx.commands.CommonCommands;
import org.derjannik.lobbyLynx.commands.FriendCommand;
import org.derjannik.lobbyLynx.commands.LynxCommand;
import org.derjannik.lobbyLynx.enums.PrivacyLevel;
import org.derjannik.lobbyLynx.gui.FriendGUI;
import org.derjannik.lobbyLynx.gui.GameruleGUI;
import org.derjannik.lobbyLynx.gui.NavigatorGUI;
import org.derjannik.lobbyLynx.gui.SettingsGUI;
import org.derjannik.lobbyLynx.listeners.CustomRuleListener;
import org.derjannik.lobbyLynx.listeners.PlayerJoinListener;
import org.derjannik.lobbyLynx.listeners.PlayerQuitListener;
import org.derjannik.lobbyLynx.listeners.TNTExplosionListener;
import org.derjannik.lobbyLynx.managers.*;

import static org.derjannik.lobbyLynx.enums.PrivacyLevel.*;

import org.derjannik.lobbyLynx.scoreboard.CustomScoreboard;
import org.derjannik.lobbyLynx.scoreboard.CustomTablist;
import org.derjannik.lobbyLynx.commands.AdvertisementCommand;
import org.derjannik.lobbyLynx.listeners.AdvertisementListener;

import java.io.File;
import java.util.List;

public class LobbyLynx extends JavaPlugin {

    private ConfigManager configManager;
    private CustomScoreboard customScoreboard;
    private CustomTablist customTablist;
    private ServerSignManager serverSignManager;
    private boolean blockBreakingAllowed;
    private boolean blockPlacementAllowed;
    private boolean elytraAllowed;
    private boolean tntExplosionsAllowed;
    private FriendManager friendManager;
    private FriendGUI friendGUI;
    private HatManager hatManager;
    private AdvertisementManager advertisementManager;

    @Override
    public void onEnable() {
        // Load the configuration
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        this.hatManager = new HatManager(this);

        this.advertisementManager = new AdvertisementManager(this);
        // New Friend Manager
        this.friendManager = new FriendManager(this);
        getCommand("friend").setExecutor(new FriendCommand(this, friendManager));

        // Initialize FriendGUI
        this.friendGUI = new FriendGUI(this, friendManager);
        getServer().getPluginManager().registerEvents(friendGUI, this);

        // Register FriendCommand


        // Initialize CustomScoreboard and CustomTablist
        customScoreboard = new CustomScoreboard(this, configManager);
        customTablist = new CustomTablist(this, configManager);
        PrivacyLevel level = PrivacyLevel.PUBLIC; // Example usage

        // Initialize custom rule fields
        blockBreakingAllowed = configManager.getGameRule("blockBreaking");
        blockPlacementAllowed = configManager.getGameRule("blockPlacement");
        elytraAllowed = !configManager.getGameRule("disableElytra");
        tntExplosionsAllowed = configManager.getGameRule("tnt");

// In your main plugin class (LobbyLynx.java)
        FriendManager friendManager = new FriendManager(this); // Make sure you have this line
        LynxCommand lynxCommand = new LynxCommand(this, configManager, friendManager);
        getCommand("lynx").setExecutor(lynxCommand);
        getCommand("lynx").setTabCompleter(lynxCommand);


        getCommand("ad").setExecutor(new AdvertisementCommand(this));
        // Register common commands with null checks
        CommonCommands commonCommands = new CommonCommands(this, configManager);
        if (getCommand("lobby") != null) getCommand("lobby").setExecutor(commonCommands);
        if (getCommand("hub") != null) getCommand("hub").setExecutor(commonCommands);
        if (getCommand("spawn") != null) getCommand("spawn").setExecutor(commonCommands);

        // Register event listeners
        registerEventListeners();

        // Initialize ServerSignManager
        this.serverSignManager = new ServerSignManager(this, configManager);
        getServer().getPluginManager().registerEvents(serverSignManager, this);

        // Register outgoing plugin channel for BungeeCord
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Initialize BungeeCord/Velocity integration
        setupBungeeIntegration();

        // Apply lobby settings and game rules
        applyLobbySettings();
        configManager.setDefaultGameRules();
        configManager.applyGameRules();

        // Start the sign updater with animation frames
        startSignAnimationUpdater();

        saveDefaultConfig();
    }

    private void registerEventListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, configManager, customScoreboard, customTablist, friendGUI), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this, configManager), this);
        getServer().getPluginManager().registerEvents(new SettingsGUI(this, configManager), this);
        getServer().getPluginManager().registerEvents(new GameruleGUI(this, configManager), this);
        getServer().getPluginManager().registerEvents(new NavigatorGUI(this, configManager), this);
        getServer().getPluginManager().registerEvents(new CustomRuleListener(this), this);
        getServer().getPluginManager().registerEvents(new TNTExplosionListener(this), this);
        getServer().getPluginManager().registerEvents(new AdvertisementListener(this), this);
    }

    private void setupBungeeIntegration() {
        // Register BungeeCord plugin messaging channel
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Start server info updater
        startServerInfoUpdater();
    }

    // Starts the scheduled task to update and animate server signs based on ConfigManager settings
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
        }, 0L, 20L * 10); // Update every 10 seconds
    }
    private void handlePrivacyCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /lynx privacy <public|friends|private>");
            return;
        }

        try {
            PrivacyLevel level = PrivacyLevel.valueOf(args[1].toUpperCase());
            // Set the privacy level
            player.sendMessage(ChatColor.GREEN + "Privacy level set to " + level.getDisplayName());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid privacy level. Use: public, friends, or private");
        }
    }
    private void updateServerInfo(String serverName) {
        // Placeholder implementation to simulate server data fetching from BungeeCord/Velocity
        int onlinePlayers = (int) (Math.random() * 100);
        int maxPlayers = 100;
        String motd = "Welcome to " + serverName;

        // Update the ServerSignManager with the retrieved information
        serverSignManager.updateServerInfo(serverName, onlinePlayers, maxPlayers, motd);
    }

    @Override
    public void onDisable() {
        if (advertisementManager != null) {
            advertisementManager.cleanup();
        }
    }

    public AdvertisementManager getAdvertisementManager() {
        return advertisementManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ServerSignManager getServerSignManager() {
        return serverSignManager;
    }

    public FriendManager getFriendManager() {
        return friendManager;
    }

    public FriendGUI getFriendGUI() {
        return friendGUI;
    }

    public void reloadNavigator() {
        configManager.reloadConfig();
        SettingsGUI settingsGUI = new SettingsGUI(this, configManager);
        GameruleGUI gameruleGUI = new GameruleGUI(this, configManager);
        NavigatorGUI navigatorGUI = new NavigatorGUI(this, configManager);

        settingsGUI.reloadGUI();
        gameruleGUI.reloadGUI();
        navigatorGUI.reloadGUI();

        applyLobbySettings();
        applyGameRules();
    }

    public void setTNTExplosionsAllowed(boolean allowed) {
        this.tntExplosionsAllowed = allowed;
    }

    public boolean areTNTExplosionsAllowed() {
        return this.tntExplosionsAllowed;
    }

    private void applyLobbySettings() {
        boolean flightEnabled = configManager.isFlightEnabled();
        boolean pvpEnabled = configManager.isPvPEnabled();
        long lobbyTime = configManager.getLobbyTime();

        // Apply flight setting to all online players
        getServer().getOnlinePlayers().forEach(player -> player.setAllowFlight(flightEnabled));

        // Apply PvP setting
        getServer().getWorlds().forEach(world -> world.setPVP(pvpEnabled));

        // Apply time setting
        getServer().getWorlds().forEach(world -> world.setTime(lobbyTime));
    }

    public void applyGameRules() {
        configManager.applyGameRules();
    }

    public void setBlockBreakingAllowed(boolean allowed) {
        this.blockBreakingAllowed = allowed;
    }

    public boolean isBlockBreakingAllowed() {
        return this.blockBreakingAllowed;
    }

    public void setBlockPlacementAllowed(boolean allowed) {
        this.blockPlacementAllowed = allowed;
    }

    public boolean isBlockPlacementAllowed() {
        return this.blockPlacementAllowed;
    }

    public void setElytraAllowed(boolean allowed) {
        this.elytraAllowed = allowed;
    }

    public boolean isElytraAllowed() {
        return this.elytraAllowed;
    }

    public HatManager getHatManager() {
        return hatManager;
    }

    public FileConfiguration getStatsConfig() {
        return getConfig(); // Assuming you want to return the main config for now
    }

    public File getStatsFile() {
        return new File(getDataFolder(), "stats.yml"); // Assuming you want a stats.yml file
    }
}

