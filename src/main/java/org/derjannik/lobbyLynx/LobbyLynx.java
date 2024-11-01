package org.derjannik.lobbyLynx;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;

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

    @Override
    public void onEnable() {
        // Load configuration and initialize ConfigManager
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Initialize Scoreboard and Tablist
        customScoreboard = new CustomScoreboard(this, configManager);
        customTablist = new CustomTablist(this, configManager);

        // Initialize rule fields from configuration
        loadCustomRules();

        // Register commands and event listeners
        setupCommands();
        registerEventListeners();

        // Initialize ServerSignManager and register it
        serverSignManager = new ServerSignManager(this, configManager);
        getServer().getPluginManager().registerEvents(serverSignManager, this);

        // Register outgoing plugin channel for BungeeCord/Velocity integration
        setupBungeeIntegration();

        // Apply lobby settings and default game rules
        applyLobbySettings();
        configManager.setDefaultGameRules();
        configManager.applyGameRules();

        // Start sign updater for animations
        startSignAnimationUpdater();

        // Ensure default config is saved if missing
        saveDefaultConfig();
    }

    private void loadCustomRules() {
        // Loading configuration-based rules to apply to the lobby
        blockBreakingAllowed = configManager.getGameRule("blockBreaking");
        blockPlacementAllowed = configManager.getGameRule("blockPlacement");
        elytraAllowed = !configManager.getGameRule("disableElytra");
        tntExplosionsAllowed = configManager.getGameRule("tnt");
    }

    private void setupCommands() {
        // Register lynx commands
        LynxCommand lynxCommand = new LynxCommand(this, configManager);
        if (getCommand("lynx") != null) {
            getCommand("lynx").setExecutor(lynxCommand);
            getCommand("lynx").setTabCompleter(lynxCommand);
        }

        // Register common commands
        CommonCommands commonCommands = new CommonCommands(this, configManager);
        if (getCommand("lobby") != null) getCommand("lobby").setExecutor(commonCommands);
        if (getCommand("hub") != null) getCommand("hub").setExecutor(commonCommands);
        if (getCommand("spawn") != null) getCommand("spawn").setExecutor(commonCommands);
    }

    private void registerEventListeners() {
        // Register all necessary event listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, configManager, customScoreboard, customTablist), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this, configManager), this);
        getServer().getPluginManager().registerEvents(new SettingsGUI(this, configManager), this);
        getServer().getPluginManager().registerEvents(new GameruleGUI(this, configManager), this);
        getServer().getPluginManager().registerEvents(new NavigatorGUI(this, configManager), this);
        getServer().getPluginManager().registerEvents(new CustomRuleListener(this), this);
        getServer().getPluginManager().registerEvents(new TNTExplosionListener(this), this);
    }

    private void setupBungeeIntegration() {
        // Register plugin messaging channel for BungeeCord
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Start periodic server information updater
        startServerInfoUpdater();
    }

    private void startSignAnimationUpdater() {
        // Start sign animation if frames are present in the config
        List<List<String>> animationFrames = configManager.getAnimationFrames();
        if (!animationFrames.isEmpty()) {
            Bukkit.getScheduler().runTaskTimer(this, () -> {
                try {
                    serverSignManager.animateSigns(animationFrames);
                } catch (Exception e) {
                    getLogger().severe("Error during sign animation: " + e.getMessage());
                }
            }, 0L, 20L);
        }
    }

    private void startServerInfoUpdater() {
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (String serverName : configManager.getServerNames()) {
                updateServerInfo(serverName);
            }
        }, 0L, 20L * 10); // Update every 10 seconds
    }

    private void updateServerInfo(String serverName) {
        // Simulate data fetching for server info
        int onlinePlayers = (int) (Math.random() * 100);
        int maxPlayers = 100;
        String motd = "Welcome to " + serverName;

        serverSignManager.updateServerInfo(serverName, onlinePlayers, maxPlayers, motd);
    }

    @Override
    public void onDisable() {
        // Save configuration and cleanup if necessary
        saveConfig();
    }

    public void reloadNavigator() {
        configManager.reloadConfig();
        new SettingsGUI(this, configManager).reloadGUI();
        new GameruleGUI(this, configManager).reloadGUI();
        new NavigatorGUI(this, configManager).reloadGUI();
        applyLobbySettings();
        applyGameRules();
    }

    private void applyLobbySettings() {
        boolean flightEnabled = configManager.isFlightEnabled();
        boolean pvpEnabled = configManager.isPvPEnabled();
        long lobbyTime = configManager.getLobbyTime();

        // Apply settings to players and worlds
        getServer().getOnlinePlayers().forEach(player -> player.setAllowFlight(flightEnabled));
        getServer().getWorlds().forEach(world -> {
            world.setPVP(pvpEnabled);
            world.setTime(lobbyTime);
        });
    }

    // Getters for custom rule flags used by listeners
    public boolean isBlockBreakingAllowed() { return this.blockBreakingAllowed; }
    public boolean isBlockPlacementAllowed() { return this.blockPlacementAllowed; }
    public boolean isElytraAllowed() { return this.elytraAllowed; }
    public boolean areTNTExplosionsAllowed() { return this.tntExplosionsAllowed; }
}
