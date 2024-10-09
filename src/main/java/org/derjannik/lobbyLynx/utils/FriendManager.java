package org.derjannik.lobbyLynx.utils;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FriendManager {
    private List<Player> friends;

    public FriendManager() {
        friends = new ArrayList<>();
    }

    public void addFriend(Player friend) {
        friends.add(friend);
    }

    public void removeFriend(Player friend) {
        friends.remove(friend);
    }

    public List<Player> getFriends() {
        return friends;
    }
}