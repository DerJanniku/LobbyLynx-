package org.derjannik.lobbyLynx.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FriendManager {
    private List<UUID> friends;

    public FriendManager() {
        friends = new ArrayList<>();
    }

    public void addFriend(UUID friendUUID) {
        friends.add(friendUUID);
    }

    public void removeFriend(UUID friendUUID) {
        friends.remove(friendUUID);
    }

    public List<UUID> getFriends() {
        return friends;
    }

    public void showFriends(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, "Freunde");

        for (UUID friendUUID : friends) {
            Player friend = Bukkit.getPlayer(friendUUID);
            ItemStack item = new ItemStack(org.bukkit.Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwner(friend.getName());
            meta.setDisplayName(friend.getName());
            inventory.addItem(item);
        }

        player.openInventory(inventory);
    }
}