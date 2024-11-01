

package org.derjannik.lobbyLynx;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;

public class LobbyLynx extends JavaPlugin {

    private ConfigManager configManager;
    private CustomScoreboard customScoreboard;
    private CustomTablist customTablist;
    private TeleportSigns teleportSigns;
    private BungeeSignManager bungeeSignManager;
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

        // Register commands
        LynxCommand lynxCommand = new LynxCommand(this, configManager);
        getCommand("lynx").setExecutor(lynxCommand);
        getCommand("lynx").setTabCompleter(lynxCommand);

        // Register common commands
        CommonCommands commonCommands = new CommonCommands(this, configManager);
        getCommand("lobby").setExecutor(commonCommands);
        getCommand("hub").setExecutor(commonCommands);
        getCommand("spawn").setExecutor(commonCommands);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, configManager, customScoreboard, customTablist), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this, configManager), this);
        getServer().getPluginManager().registerEvents(new SettingsGUI(this, configManager), this);
        getServer().getPluginManager().registerEvents(new GameruleGUI(this, configManager), this);
        getServer().getPluginManager().registerEvents(new NavigatorGUI(this, configManager), this);
        getServer().getPluginManager().registerEvents(new CustomRuleListener(this), this);
        getServer().getPluginManager().registerEvents(new TNTExplosionListener(this), this);

        // Initialize and register TeleportSigns
        teleportSigns = new TeleportSigns(this, configManager);
        getServer().getPluginManager().registerEvents(teleportSigns, this);

        // Test TeleportSigns functionality
        teleportSigns.testTeleportSigns();

        // Apply lobby settings and game rules
        applyLobbySettings();
        configManager.setDefaultGameRules();

        // Initialize TNT explosions setting
        tntExplosionsAllowed = configManager.getGameRule("tnt");

        // Register BungeeCord plugin messaging channel
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Start server info updater
        startServerInfoUpdater();

        // Initialize and register BungeeSignManager
        bungeeSignManager = new BungeeSignManager(this);
        getServer().getPluginManager().registerEvents(bungeeSignManager, this);

        saveDefaultConfig();
    }

    public BungeeSignManager getBungeeSignManager() {
        return bungeeSignManager;
    }

    private void startServerInfoUpdater() {
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            // Here you would implement the logic to fetch server info from Velocity
            // For demonstration purposes, we'll use placeholder data
            for (String serverName : configManager.getConfig().getStringList("velocity-servers")) {
                updateServerInfo(serverName);
            }
        }, 0L, 20L * 10); // Update every 10 seconds
    }

    private void updateServerInfo(String serverName) {
        // This is a placeholder. In a real implementation, you would fetch this data from Velocity
        int onlinePlayers = (int) (Math.random() * 100);
        int maxPlayers = 100;
        String motd = "Welcome to " + serverName;

        // Update the TeleportSigns with this information
        teleportSigns.updateServerInfo(serverName, onlinePlayers, maxPlayers, motd);
    }

    @Override
    public void onDisable() {
        // Any cleanup logic if necessary
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void reloadNavigator() {
        configManager.reloadConfig();
        // Reload all GUIs
        new SettingsGUI(this, configManager).reloadGUI();
        new GameruleGUI(this, configManager).reloadGUI();
        new NavigatorGUI(this, configManager).reloadGUI();
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

        // Double jump will be handled in a separate listener
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
