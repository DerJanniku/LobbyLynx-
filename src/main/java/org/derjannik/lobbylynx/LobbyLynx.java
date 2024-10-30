package org.derjannik.lobbylynx;

import org.bukkit.plugin.java.JavaPlugin;
import org.derjannik.lobbylynx.command.SetMinigameCommand;
import org.derjannik.lobbylynx.command.SetLobbySpawnCommand;
import org.derjannik.lobbylynx.command.LobbyCommand;

public class LobbyLynx extends JavaPlugin {
    private Navigator navigator;

    @Override
    public void onEnable() {
        // Load default configuration if not present
        this.saveDefaultConfig();

        // Initialize Navigator with config settings
        this.navigator = new Navigator(this);

        // Register commands
        this.getCommand("setminigame").setExecutor(new SetMinigameCommand(this));
        this.getCommand("setlobbyspawn").setExecutor(new SetLobbySpawnCommand(this));
        this.getCommand("lobby").setExecutor(new LobbyCommand(this));

        // Register event listener for Navigator interactions
        getServer().getPluginManager().registerEvents(new NavigatorListener(this, navigator), this);

        getLogger().info("LobbyLynx has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("LobbyLynx has been disabled!");
    }

    public Navigator getNavigator() {
        return navigator;
    }

    public void reloadNavigator() {
        reloadConfig();
        this.navigator = new Navigator(this);
    }
}