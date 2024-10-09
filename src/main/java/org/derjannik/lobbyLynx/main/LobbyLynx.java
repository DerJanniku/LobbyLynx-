package org.derjannik.lobbyLynx.main;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.derjannik.lobbyLynx.command.FriendCommand;
import org.derjannik.lobbyLynx.utils.FriendSystem;

public class LobbyLynx extends JavaPlugin {

    private FriendSystem friendSystem;

    @Override
    public void onEnable() {
        friendSystem = new FriendSystem();
        FriendCommand friendCommand = new FriendCommand(friendSystem);
        getServer().getPluginManager().registerEvents(friendCommand, this);
    }
}