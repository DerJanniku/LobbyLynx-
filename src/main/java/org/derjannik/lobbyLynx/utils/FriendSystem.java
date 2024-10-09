package org.derjannik.lobbyLynx.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FriendSystem {
    private List<UUID> friends;
    private JavaPlugin plugin;

    public FriendSystem(JavaPlugin plugin) {
        this.plugin = plugin;
        friends = new ArrayList<>();
    }

    public void addFriend(Player player, Player friend) {
        UUID playerUUID = player.getUniqueId();
        UUID friendUUID = friend.getUniqueId();

        if (!friends.contains(friendUUID)) {
            friends.add(friendUUID);
            plugin.getServer().getConsoleSender().sendMessage("§aFreund hinzugefügt: " + friend.getName());
        } else {
            plugin.getServer().getConsoleSender().sendMessage("§cFreund bereits hinzugefügt: " + friend.getName());
        }
    }

    public void removeFriend(Player player, Player friend) {
        UUID playerUUID = player.getUniqueId();
        UUID friendUUID = friend.getUniqueId();

        if (friends.contains(friendUUID)) {
            friends.remove(friendUUID);
            plugin.getServer().getConsoleSender().sendMessage("§aFreund entfernt: " + friend.getName());
        } else {
            plugin.getServer().getConsoleSender().sendMessage("§cFreund nicht gefunden: " + friend.getName());
        }
    }

    public void showFriends(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, "Freunde");

        for (UUID friendUUID : friends) {
            Player friend = Bukkit.getPlayer(friendUUID);
            ItemStack item = new ItemStack(org.bukkit.Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwner(friend.getName());
            meta.setDisplayName(friend.getName());

            if (friend.isOnline()) {
                meta.setDisplayName("§a" + friend.getName());
            } else {
                meta.setDisplayName("§c" + friend.getName());
            }

            inventory.addItem(item);
        }

        player.openInventory(inventory);
    }

    public void updateGUI() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            showFriends(player);
        }
    }
}