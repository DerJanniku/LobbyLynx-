package org.derjannik.lobbyLynx.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class FriendManager {
    private final JavaPlugin plugin;
    private final Map<UUID, List<UUID>> friends;

    public FriendManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.friends = new HashMap<>();
    }

    public void addFriend(Player player, Player friend) {
        // Überprüfen, ob der Freund bereits in der Liste ist
        if (!friends.containsKey(player.getUniqueId())) {
            friends.put(player.getUniqueId(), new ArrayList<>());
        }
        if (!friends.get(player.getUniqueId()).contains(friend.getUniqueId())) {
            friends.get(player.getUniqueId()).add(friend.getUniqueId());
            // Hinzufügen von Freunden
            ItemStack item = new ItemStack(org.bukkit.Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (friend != null) {
                meta.setOwningPlayer(friend);
            }
            meta.setDisplayName(friend.getName());
        }
    }

    public void removeFriend(Player player, UUID friendUUID) {
        // Entfernen von Freunden
        if (friends.containsKey(player.getUniqueId())) {
            friends.get(player.getUniqueId()).remove(friendUUID);
            Player friend = Bukkit.getPlayer(friendUUID);
            if (friend != null) {
                ItemStack item = new ItemStack(org.bukkit.Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                meta.setOwningPlayer(friend);
                meta.setDisplayName(friend.getName());
            }
        }
    }

    public List<UUID> getFriends(Player player) {
        // Rückgabe der Freunde
        return friends.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }
}