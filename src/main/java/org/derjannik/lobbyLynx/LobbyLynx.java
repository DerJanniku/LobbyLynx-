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
    private ServerSignManager serverSignManager; // Moved declaration here
    private boolean blockBreakingAllowed;
    private boolean blockPlacementAllowed;
    private boolean elytraAllowed;
    private boolean tntExplosionsAllowed;

    @Override
    public void onEnable() {
        // Load the configuration
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Initialize CustomScoreboard and CustomTablist
        customScoreboard = new CustomScoreboard(this, configManager);
        customTablist = new CustomTablist(this, configManager);

        // Initialize custom rule fields
        blockBreakingAllowed = configManager.getGameRule("blockBreaking");
        blockPlacementAllowed = configManager.getGameRule("blockPlacement");
        elytraAllowed = !configManager.getGameRule("disableElytra");
        tntExplosionsAllowed = configManager.getGameRule("tnt");

        // Register commands with null checks to prevent NullPointerExceptions
        LynxCommand lynxCommand = new LynxCommand(this, configManager);
        if (getCommand("lynx") != null) {
            getCommand("lynx").setExecutor(lynxCommand);
            getCommand("lynx").setTabCompleter(lynxCommand);
        }

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
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, configManager, customScoreboard, customTablist), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this, configManager), this);
        getServer().getPluginManager().registerEvents(new SettingsGUI(this, configManager), this);
        getServer().getPluginManager().registerEvents(new GameruleGUI(this, configManager), this);
        getServer().getPluginManager().registerEvents(new NavigatorGUI(this, configManager), this);
        getServer().getPluginManager().registerEvents(new CustomRuleListener(this), this);
        getServer().getPluginManager().registerEvents(new TNTExplosionListener(this), this);
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
        // Cleanup logic if necessary
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ServerSignManager getServerSignManager() {
        return serverSignManager; // Added getter method
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
}

class CustomRuleListener implements Listener {
    private final LobbyLynx plugin;

    public CustomRuleListener(LobbyLynx plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.isBlockBreakingAllowed()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.isBlockPlacementAllowed()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerToggleGlide(EntityToggleGlideEvent event) {
        if (event.getEntity() instanceof Player && !plugin.isElytraAllowed()) {
            event.setCancelled(true);
            if (event.isGliding()) {
                ((Player) event.getEntity()).setGliding(false);
            }
        }
    }
}

class TNTExplosionListener implements Listener {
    private final LobbyLynx plugin;

    public TNTExplosionListener(LobbyLynx plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof TNTPrimed && !plugin.areTNTExplosionsAllowed()) {
            event.setCancelled(true);
        }
    }
}