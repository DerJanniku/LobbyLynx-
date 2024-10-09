package org.derjannik.lobbyLynx.main;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.derjannik.lobbyLynx.listener.FriendListener;
import org.derjannik.lobbyLynx.utils.FriendSystem;

public class LobbyLynx extends JavaPlugin {
    private FriendSystem friendSystem;
    private FriendListener friendListener;

    @Override
    public void onEnable() {
        friendSystem = new FriendSystem(this);
        friendListener = new FriendListener(friendSystem, this);

        getServer().getPluginManager().registerEvents(friendListener, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}