package org.derjannik.lobbyLynx;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.derjannik.lobbyLynx.command.FriendCommand;
import org.derjannik.lobbyLynx.listener.FriendSystem;

public class LobbyLynx extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("friend").setExecutor(new FriendC(new FriendSystem()));
        Bukkit.getPluginManager().registerEvents(new FriendSystem(), this);
    }
}