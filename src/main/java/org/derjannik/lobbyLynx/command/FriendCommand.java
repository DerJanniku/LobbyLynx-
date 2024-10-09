
package org.derjannik.lobbyLynx.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class FriendSystem implements Listener {

    private static final Logger LOGGER = Logger.getLogger(FriendSystem.class.getName());
    private Map<UUID, UUID> friendRequests = new HashMap<>();

    public Map<UUID, UUID> getFriendRequests() {
        return friendRequests;
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("Usage: /friend <list|add|remove|requests|accept|msg>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                listFriends(player);
                break;
            case "add":
                if (args.length < 2) {
                    player.sendMessage("Usage: /friend add <player>");
                } else {
                    addFriend(player, args[1]);
                }
                break;
            case "remove":
                if (args.length < 2) {
                    player.sendMessage("Usage: /friend remove <player>");
                } else {
                    removeFriend(player, args[1]);
                }
                break;
            case "requests":
                listFriendRequests(player);
                break;
            case "accept":
                if (args.length < 2) {
                    player.sendMessage("Usage: /friend accept <player>");
                } else {
                    acceptFriendRequest(player, args[1]);
                }
                break;
            case "msg":
                if (args.length < 3) {
                    player.sendMessage("Usage: /friend msg <player> <message>");
                } else {
                    sendMessage(player, args[1], args[2]);
                }
                break;
            default:
                player.sendMessage("Unknown command. Usage: /friend <list|add|remove|requests|accept|msg>");
                break;
        }

        return true;
    }

    private void listFriends(Player player) {
        player.sendMessage("Friends: " + friends);
        LOGGER.info("Listed friends for player: " + player.getName());
    }

    private void addFriend(Player player, String friendName) {
        Player friend = Bukkit.getPlayer(friendName);
        if (friend != null) {
            plugin.getFriendRequests().put(friend.getUniqueId(), player.getUniqueId());
            player.sendMessage("Friend request sent to " + friendName);
            LOGGER.info("Friend request sent from " + player.getName() + " to " + friendName);
        } else {
            player.sendMessage("Player not found.");
            LOGGER.warning("Player not found: " + friendName);
        }
    }

    private void removeFriend(Player player, String friendName) {
        Player friend = Bukkit.getPlayer(friendName);
        if (friend != null && friends.contains(friend.getUniqueId())) {
            friends.remove(friend.getUniqueId());
            player.sendMessage(friendName + " removed from friends.");
            LOGGER.info("Removed friend: " + friendName + " for player: " + player.getName());
        } else {
            player.sendMessage("Player not found or not a friend.");
            LOGGER.warning("Player not found or not a friend: " + friendName);
        }
    }

    private void listFriendRequests(Player player) {
        player.sendMessage("Friend requests: " + plugin.getFriendRequests());
        LOGGER.info("Listed friend requests for player: " + player.getName());
    }

    private void acceptFriendRequest(Player player, String friendName) {
        Player friend = Bukkit.getPlayer(friendName);
        if (friend != null && plugin.getFriendRequests().containsKey(player.getUniqueId())) {
            friends.add(friend.getUniqueId());
            plugin.getFriendRequests().remove(player.getUniqueId());
            player.sendMessage("Friend request from " + friendName + " accepted.");
            LOGGER.info("Accepted friend request from " + friendName + " for player: " + player.getName());
        } else {
            player.sendMessage("No friend request from " + friendName);
            LOGGER.warning("No friend request from: " + friendName);
        }
    }

    private void sendMessage(Player player, String friendName, String message) {
        Player friend = Bukkit.getPlayer(friendName);
        if (friend != null && friends.contains(friend.getUniqueId())) {
            friend.sendMessage(player.getName() + ": " + message);
            LOGGER.info("Sent message from " + player.getName() + " to " + friendName + ": " + message);
        } else {
            player.sendMessage("Player not found or not a friend.");
            LOGGER.warning("Player not found or not a friend: " + friendName);
        }
    }
}
