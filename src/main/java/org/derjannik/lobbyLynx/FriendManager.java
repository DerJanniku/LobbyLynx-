package org.derjannik.lobbyLynx;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FriendManager {
    private final LobbyLynx plugin;
    private final File friendsFile;
    private final File requestsFile;
    private FileConfiguration friendsConfig;
    private FileConfiguration requestsConfig;

    public FriendManager(LobbyLynx plugin) {
        this.plugin = plugin;
        this.friendsFile = new File(plugin.getDataFolder(), "friends.yml");
        this.requestsFile = new File(plugin.getDataFolder(), "friend_requests.yml");
        loadConfigs();
    }

    private void loadConfigs() {
        // Create plugin directory if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        // Create friends.yml if it doesn't exist
        if (!friendsFile.exists()) {
            try {
                friendsFile.createNewFile();
                // Add default content
                FileConfiguration defaultFriendsConfig = YamlConfiguration.loadConfiguration(friendsFile);
                defaultFriendsConfig.createSection("players");
                defaultFriendsConfig.save(friendsFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create friends.yml!");
                e.printStackTrace();
            }
        }

        // Create friend_requests.yml if it doesn't exist
        if (!requestsFile.exists()) {
            try {
                requestsFile.createNewFile();
                // Add default content
                FileConfiguration defaultRequestsConfig = YamlConfiguration.loadConfiguration(requestsFile);
                defaultRequestsConfig.createSection("requests");
                defaultRequestsConfig.save(requestsFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create friend_requests.yml!");
                e.printStackTrace();
            }
        }

        // Load configurations
        friendsConfig = YamlConfiguration.loadConfiguration(friendsFile);
        requestsConfig = YamlConfiguration.loadConfiguration(requestsFile);
    }

    public List<String> getFriends(String playerName) {
        return friendsConfig.getStringList("players." + playerName + ".friends");
    }

    public List<String> getRequests(String playerName) {
        return requestsConfig.getStringList("requests." + playerName);
    }

    public void saveFriendsConfig() {
        try {
            friendsConfig.save(friendsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save friends.yml!");
            e.printStackTrace();
        }
    }

    public void saveRequestsConfig() {
        try {
            requestsConfig.save(requestsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save friend_requests.yml!");
            e.printStackTrace();
        }
    }

    public boolean areFriends(String player1, String player2) {
        List<String> player1Friends = getFriends(player1);
        return player1Friends.contains(player2);
    }

    public void addFriend(String player1, String player2) {
        List<String> player1Friends = getFriends(player1);
        List<String> player2Friends = getFriends(player2);

        player1Friends.add(player2);
        player2Friends.add(player1);

        friendsConfig.set("players." + player1 + ".friends", player1Friends);
        friendsConfig.set("players." + player2 + ".friends", player2Friends);
        saveFriendsConfig();

        // Notify both players
        Player p1 = Bukkit.getPlayer(player1);
        Player p2 = Bukkit.getPlayer(player2);

        if (p1 != null) {
            p1.sendMessage(ChatColor.GREEN + "You are now friends with " + player2 + "!");
        }
        if (p2 != null) {
            p2.sendMessage(ChatColor.GREEN + "You are now friends with " + player1 + "!");
        }
    }

    public void sendFriendRequest(String sender, String receiver) {
        if (areFriends(sender, receiver)) {
            Player senderPlayer = Bukkit.getPlayer(sender);
            if (senderPlayer != null) {
                senderPlayer.sendMessage(ChatColor.RED + "You are already friends with " + receiver);
            }
            return;
        }

        List<String> requests = getRequests(receiver);
        if (!requests.contains(sender)) {
            requests.add(sender);
            requestsConfig.set("requests." + receiver, requests);
            saveRequestsConfig();

            // Notify both players
            Player senderPlayer = Bukkit.getPlayer(sender);
            Player receiverPlayer = Bukkit.getPlayer(receiver);

            if (senderPlayer != null) {
                senderPlayer.sendMessage(ChatColor.GREEN + "Friend request sent to " + receiver);
            }
            if (receiverPlayer != null) {
                receiverPlayer.sendMessage(ChatColor.GREEN + "You have received a friend request from " + sender);
            }
        }
    }

    public void acceptRequest(String player, String requester) {
        List<String> requests = getRequests(player);
        if (requests.contains(requester)) {
            requests.remove(requester);
            requestsConfig.set(player + ".requests", requests);
            saveRequestsConfig();

            addFriend(player, requester);
        }
    }

    public void denyRequest(String player, String requester) {
        List<String> requests = getRequests(player);
        if (requests.contains(requester)) {
            requests.remove(requester);
            requestsConfig.set(player + ".requests", requests);
            saveRequestsConfig();

            // Notify the requester
            Player requesterPlayer = Bukkit.getPlayer(requester);
            if (requesterPlayer != null) {
                requesterPlayer.sendMessage(ChatColor.RED + player + " has denied your friend request");
            }
        }
    }

    public void removeFriend(String name, String target) {
    }
}