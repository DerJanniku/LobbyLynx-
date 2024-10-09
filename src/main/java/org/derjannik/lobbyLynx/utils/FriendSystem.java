package org.derjannik.lobbyLynx.utils;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class FriendSystem {
    private Map<Player, FriendManager> friendManagers;

    public FriendSystem() {
        friendManagers = new HashMap<>();
    }

    public void addFriend(Player player, Player friend) {
        // Logik zum Hinzufügen eines Freundes
    }

    public void removeFriend(Player player, Player friend) {
        // Logik zum Entfernen eines Freundes
    }

    public void showFriends(Player player) {
        // Logik zum Anzeigen der Freundesliste
    }
}