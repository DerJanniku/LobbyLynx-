
package org.derjannik.lobbylynx;

import org.bukkit.plugin.java.JavaPlugin;
import org.derjannik.lobbyLynx.command.SetMinigameCommand;
import org.derjannik.lobbyLynx.command.SetLobbySpawnCommand;
import org.derjannik.lobbyLynx.command.LobbyCommand;

public class LobbyLynx extends JavaPlugin {

    @Override
    public void onEnable() {
        // Register commands
        this.getCommand("setminigame").setExecutor(new SetMinigameCommand(this));
        this.getCommand("setlobbyspawn").setExecutor(new SetLobbySpawnCommand(this));
        this.getCommand("lobby").setExecutor(new LobbyCommand(this));

        // Register event listener
        getServer().getPluginManager().registerEvents(new NavigatorListener(this), this);

        // Load configuration
        this.saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
