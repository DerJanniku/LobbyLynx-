package org.derjannik.lobbyLynx.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import java.util.UUID;

public class FriendSystem {
    private Map<UUID, List<UUID>> friends;
    private JavaPlugin plugin;

    public FriendSystem(JavaPlugin plugin) {
        this.plugin = plugin;
        friends = new HashMap<>();
    }

    public void addFriend(Player player, Player friend) {
        UUID playerUUID = player.getUniqueId();
        UUID friendUUID = friend.getUniqueId();

        if (!friends.containsKey(playerUUID)) {
            friends.put(playerUUID, new ArrayList<>());
        }

        if (!friends.get(playerUUID).contains(friendUUID)) {
            friends.get(playerUUID).add(friendUUID);
            plugin.getServer().getConsoleSender().sendMessage("§aFreund hinzugefügt: " + friend.getName());
        } else {
            plugin.getServer().getConsoleSender().sendMessage("§cFreund bereits hinzugefügt: " + friend.getName());
        }
    }

    public void removeFriend(Player player, UUID friendUUID) {
        UUID playerUUID = player.getUniqueId();

        if (friends.containsKey(playerUUID) && friends.get(playerUUID).contains(friendUUID)) {
            friends.get(playerUUID).remove(friendUUID);
            plugin.getServer().getConsoleSender().sendMessage("§aFreund entfernt: " + Bukkit.getPlayer(friendUUID).getName());
        } else {
            plugin.getServer().getConsoleSender().sendMessage("§cFreund nicht gefunden: " + Bukkit.getPlayer(friendUUID).getName());
        }
    }

    public List<UUID> getFriends(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (friends.containsKey(playerUUID)) {
            return friends.get(playerUUID);
        } else {
            return new ArrayList<>();
        }
    }

    public void showFriends(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, "Freunde");

        for (UUID friendUUID : getFriends(player)) {
            Player friend = Bukkit.getPlayer(friendUUID);
            ItemStack item = new ItemStack(org.bukkit.Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwningPlayer(friend);
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