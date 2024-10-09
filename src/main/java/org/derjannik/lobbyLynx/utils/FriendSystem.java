package org.derjannik.lobbyLynx.utils;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FriendSystem {
    private Map<UUID, FriendManager> friendManagers;

    public FriendSystem() {
        friendManagers = new HashMap<>();
    }

    public void addFriend(Player player, Player friend) {
        UUID playerUUID = player.getUniqueId();
        UUID friendUUID = friend.getUniqueId();

        if (!friendManagers.containsKey(playerUUID)) {
            friendManagers.put(playerUUID, new FriendManager());
        }

        friendManagers.get(playerUUID).addFriend(friendUUID);
    }

    public void removeFriend(Player player, Player friend) {
        UUID playerUUID = player.getUniqueId();
        UUID friendUUID = friend.getUniqueId();

        if (friendManagers.containsKey(playerUUID)) {
            friendManagers.get(playerUUID).removeFriend(friendUUID);
        }
    }

    public void showFriends(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (friendManagers.containsKey(playerUUID)) {
            FriendManager friendManager = friendManagers.get(playerUUID);
            friendManager.showFriends(player);
        }
    }
}