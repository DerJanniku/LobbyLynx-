package org.derjannik.lobbyLynx;

import org.bukkit.plugin.java.JavaPlugin;

public class LobbyLynx extends JavaPlugin {

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        // Load the configuration
        configManager = new ConfigManager(this);

        LynxCommand lynxCommand = new LynxCommand(this);
        getCommand("lynx").setExecutor(lynxCommand);
        getCommand("lynx").setTabCompleter(lynxCommand);
        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, configManager), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(new SettingsGUI(this), this);
        getServer().getPluginManager().registerEvents(new GameruleGUI(this), this); // Register GameruleGUI
        getServer().getPluginManager().registerEvents(new NavigatorGUI(this), this);

        // Register commands
        getCommand("lynx").setExecutor(new LynxCommand(this));
        this.getCommand("lynx").setExecutor(new NavigatorCommand(this));

        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Any cleanup logic if necessary
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void reloadNavigator() {
    }
}