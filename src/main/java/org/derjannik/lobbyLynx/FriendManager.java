package org.derjannik.lobbyLynx;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FriendManager {
    private final LobbyLynx plugin;
    private final File friendsFile;
    private final File requestsFile;
    private final File statsFile;
    private final Map<String, PrivacySettings> privacySettingsMap;
    private FileConfiguration friendsConfig;
    private FileConfiguration requestsConfig;
    private FileConfiguration statsConfig;
    private final Map<String, List<String>> friendCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> requestCache = new ConcurrentHashMap<>();
    private final Map<String, FriendStatistics> statsCache = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> onlineFriends = new ConcurrentHashMap<>();
    private final Map<String, String> playerStatus = new ConcurrentHashMap<>();
    private final Map<String, Long> lastSeen = new ConcurrentHashMap<>();
    private Map<String, String> statusMap; // Maps player names to their statuses
    private Map<String, Set<String>> friendsMap; // Maps player names to their friends
    private Set<String> onlineFriendsSet; // Set of currently online friends
    private final Map<String, Set<String>> blockedPlayers = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> favoriteFriends = new ConcurrentHashMap<>();
    private boolean useMysql;
    private Connection mysqlConnection;

    public FriendManager(LobbyLynx plugin) {
        this.plugin = plugin;
        this.friendsFile = new File(plugin.getDataFolder(), "friends.yml");
        this.requestsFile = new File(plugin.getDataFolder(), "friend_requests.yml");
        this.statsFile = new File(plugin.getDataFolder(), "friend_stats.yml");
        this.privacySettingsMap = new HashMap<>();
        this.useMysql = plugin.getConfig().getBoolean("mysql.enabled", false);
        loadConfigs();
        statusMap = new HashMap<>();
        friendsMap = new HashMap<>();
        onlineFriendsSet = new HashSet<>(); // Initialize the set of online friends
        if (useMysql) {
            connectToDatabase();
        }

        startAutoSave();
        startOnlineTracking();
    }

    private void loadConfigs() {
        if (!friendsFile.exists()) plugin.saveResource("friends.yml", false);
        if (!requestsFile.exists()) plugin.saveResource("friend_requests.yml", false);
        if (!statsFile.exists()) plugin.saveResource("friend_stats.yml", false);

        friendsConfig = YamlConfiguration.loadConfiguration(friendsFile);
        requestsConfig = YamlConfiguration.loadConfiguration(requestsFile);
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);

        // Load caches
        for (String player : friendsConfig.getKeys(false)) {
            friendCache.put(player, friendsConfig.getStringList(player));
        }
        for (String player : requestsConfig.getKeys(false)) {
            requestCache.put(player, requestsConfig.getStringList(player));
        }
        for (String player : statsConfig.getKeys(false)) {
            statsCache.put(player, loadStatistics(player));
        }
    }

    public FriendStatistics loadStatistics(String player) {
        FileConfiguration statsConfig = plugin.getStatsConfig();
        String path = "statistics." + player;
        FriendStatistics stats = new FriendStatistics();

        if (statsConfig.contains(path)) {
            stats.setFriendSince(statsConfig.getLong(path + ".friendSince"));
            stats.setMessagesSent(statsConfig.getInt(path + ".messagesSent"));
            stats.setGamesPlayed(statsConfig.getInt(path + ".gamesPlayed"));
            stats.setLastInteraction(statsConfig.getLong(path + ".lastInteraction"));
            stats.setStatus(statsConfig.getString(path + ".status", "Hey there! I'm using LobbyLynx!"));
            stats.setOnline(statsConfig.getBoolean(path + ".isOnline", false));

            if (statsConfig.contains(path + ".friendshipLevels")) {
                List<String> levels = statsConfig.getStringList(path + ".friendshipLevels");
                for (String level : levels) {
                    String[] parts = level.split(":");
                    if (parts.length == 2) {
                        stats.addFriend(parts[0], System.currentTimeMillis(), Integer.parseInt(parts[1]));
                    }
                }
            }
        }
        return stats; // Return the populated stats object
    }

    private void connectToDatabase() {
        // Implement MySQL connection logic here
    }

    private void startAutoSave() {
        new BukkitRunnable() {
            @Override
            public void run() {
                saveAllData();
            }
        }.runTaskTimerAsynchronously(plugin, 6000L, 6000L); // Save every 5 minutes
    }

    private void startOnlineTracking() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateOnlineFriends(player);
                    updateLastSeen(player.getName());
                }
            }
        }.runTaskTimerAsynchronously(plugin, 100L, 100L); // Update every 5 seconds
    }

    public void saveAllData() {
        if (useMysql) {
            saveToDB();
        } else {
            saveToFiles();
        }
    }

    private void saveToFiles() {
        try {
            for (Map.Entry<String, List<String>> entry : friendCache.entrySet()) {
                friendsConfig.set(entry.getKey(), entry.getValue());
            }
            friendsConfig.save(friendsFile);

            for (Map.Entry<String, List<String>> entry : requestCache.entrySet()) {
                requestsConfig.set(entry.getKey(), entry.getValue());
            }
            requestsConfig.save(requestsFile);

            for (Map.Entry<String, FriendStatistics> entry : statsCache.entrySet()) {
                saveStatistics(entry.getKey(), entry.getValue());
            }
            statsConfig.save(statsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save friend data: " + e.getMessage());
        }
    }

    private void saveStatistics(String key, FriendStatistics value) {
        FileConfiguration statsConfig = plugin.getStatsConfig();
        String path = "statistics." + key;

        statsConfig.set(path + ".friendSince", value.getFriendSince());
        statsConfig.set(path + ".messagesSent", value.getMessagesSent());
        statsConfig.set(path + ".gamesPlayed", value.getGamesPlayed());
        statsConfig.set(path + ".lastInteraction", value.getLastInteraction());
        statsConfig.set(path + ".status", value.getStatus());
        statsConfig.set(path + ".isOnline", value.isOnline());

        // Save friendship levels as a list of strings
        List<String> levels = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : value.getFriendshipLevels().entrySet()) {
            levels.add(entry.getKey() + ":" + entry.getValue());
        }
        statsConfig.set(path + ".friendshipLevels", levels);

        try {
            statsConfig.save(plugin.getStatsFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToDB() {
        // Implement MySQL saving logic here
    }

    public List<String> getFriends(String playerName) {
        return friendCache.getOrDefault(playerName, new ArrayList<>());
    }

    public List<String> getRequests(String playerName) {
        return requestCache.getOrDefault(playerName, new ArrayList<>());
    }

    public void sendFriendRequest(String sender, String receiver) {
        if (areFriends(sender, receiver)) {
            plugin.getServer().getPlayer(sender).sendMessage(ChatColor.RED + "You are already friends with this player!");
            return;
        }

        if (hasReachedFriendLimit(sender)) {
            plugin.getServer().getPlayer(sender).sendMessage(ChatColor.RED + "You have reached your friend limit!");
            return;
        }

        List<String> requests = getRequests(receiver);
        if (!requests.contains(sender)) {
            requests.add(sender);
            requestCache.put(receiver, requests);

            // Notify players
            notifyPlayer(receiver, ChatColor.YELLOW + "You have received a friend request from " + sender);
            notifyPlayer(sender, ChatColor.GREEN + "Friend request sent to " + receiver);
        }
    }

    public void acceptRequest(String playerName, String requesterName) {
        List<String> requests = getRequests(playerName);
        if (requests.contains(requesterName)) {
            requests.remove(requesterName);
            requestCache.put(playerName, requests);

            addFriend(playerName, requesterName);
            addFriend(requesterName, playerName);

            notifyPlayer(requesterName, ChatColor.GREEN + playerName + " accepted your friend request!");

            updateFriendshipStats(playerName, requesterName);
        }
    }

    private void updateFriendshipStats(String playerName, String requesterName) {
        FriendStatistics stats = statsCache.computeIfAbsent(playerName, k -> new FriendStatistics());
        stats.setLastInteraction(System.currentTimeMillis());
        stats.addFriend(requesterName, System.currentTimeMillis(), 0); // Add friend with default level

        // Update the statistics for the other player as well
        FriendStatistics otherStats = statsCache.computeIfAbsent(requesterName, k -> new FriendStatistics());
        otherStats.setLastInteraction(System.currentTimeMillis());
        otherStats.addFriend(playerName, System.currentTimeMillis(), 0); // Add friend with default level
    }

    private void addFriend(String player1, String player2) {
        List<String> friends = getFriends(player1);
        if (!friends .contains(player2)) {
            friends.add(player2);
            friendCache.put(player1, friends);
        }
    }

    public void removeFriend(String player1, String player2) {
        removeFriendOneWay(player1, player2);
        removeFriendOneWay(player2, player1);

        notifyPlayer(player1, ChatColor.YELLOW + "Removed " + player2 + " from your friends list");
        notifyPlayer(player2, ChatColor.YELLOW + player1 + " removed you from their friends list");

        updateFriendshipStats(player1, player2);
    }

    private void removeFriendOneWay(String player1, String player2) {
        List<String> friends = getFriends(player1);
        friends.remove(player2);
        friendCache.put(player1, friends);
    }

    public void denyRequest(String playerName, String requesterName) {
        List<String> requests = getRequests(playerName);
        if (requests.contains(requesterName)) {
            requests.remove(requesterName);
            requestCache.put(playerName, requests);

            notifyPlayer(playerName, ChatColor.YELLOW + "Denied friend request from " + requesterName);
            notifyPlayer(requesterName, ChatColor.RED + playerName + " denied your friend request");
        }
    }

    public void unblockPlayer(String name, String targetName) {
        Set<String> blocked = blockedPlayers.computeIfAbsent(name, k -> new HashSet<>());
        if (blocked.remove(targetName)) {
            notifyPlayer(name, ChatColor.GREEN + "You have unblocked " + targetName);
        } else {
            notifyPlayer(name, ChatColor.RED + targetName + " is not in your blocked list.");
        }
    }

    public void blockPlayer(String name, String targetName) {
        Set<String> blocked = blockedPlayers.computeIfAbsent(name, k -> new HashSet<>());
        if (blocked.add(targetName)) {
            notifyPlayer(name, ChatColor.GREEN + "You have blocked " + targetName);
        } else {
            notifyPlayer(name, ChatColor.RED + targetName + " is already in your blocked list.");
        }
    }

    public void sendMessage(String name, String targetName, String message) {
        if (!areFriends(name, targetName)) {
            notifyPlayer(name, ChatColor.RED + "You can only send messages to your friends!");
            return;
        }

        PrivacySettings receiverSettings = privacySettingsMap.get(targetName);
        if (receiverSettings != null && !receiverSettings.allowMessages) {
            notifyPlayer(name, ChatColor.RED + "This player is not accepting messages right now.");
            return;
        }

        String formattedMessage = ChatColor.GOLD + "[Friend] " +
                ChatColor.YELLOW + name +
                ChatColor.WHITE + ": " + message;

        notifyPlayer(targetName, formattedMessage);
        notifyPlayer(name, formattedMessage);

        // Update statistics
        FriendStatistics stats = statsCache.computeIfAbsent(name, k -> new FriendStatistics());
        stats.setMessagesSent(stats.getMessagesSent() + 1);
        stats.setLastInteraction(System.currentTimeMillis());
    }

    public void setShowLastSeen(String name, boolean showLastSeen) {
        PrivacySettings settings = privacySettingsMap.computeIfAbsent(name, k -> new PrivacySettings());
        settings.showLastSeen = showLastSeen;
        notifyPlayer(name, ChatColor.GREEN + "Last seen visibility set to " + (showLastSeen ? "on" : "off"));
    }

    public void setPrivacyLevel(String name, PrivacyLevel level) {
        PrivacySettings settings = privacySettingsMap.computeIfAbsent(name, k -> new PrivacySettings());
        settings.privateMode = (level == PrivacyLevel.PRIVATE);
        notifyPlayer(name, ChatColor.GREEN + "Privacy level set to " + (settings.privateMode ? "private" : "public"));
    }

    public Set<String> getBlockedPlayers(String playerName) {
        return blockedPlayers.getOrDefault(playerName, new HashSet<>());
    }

    public PrivacySettings getPrivacySettings(String playerName) {
        return privacySettingsMap.getOrDefault(playerName, new PrivacySettings());
    }

    public boolean isFavorite(String name, String friendName) {
        return favoriteFriends.getOrDefault(name, new HashSet<>()).contains(friendName);
    }

    public FriendStatistics getFriendStatistics(String name) {
        return statsCache.computeIfAbsent(name, k -> new FriendStatistics());
    }

    public List<String> getGroupMembers(String playerName, String groupName) {
        // This method should return members of a specific group
        return new ArrayList<>(); // Placeholder for group members retrieval
    }

    public List<String> getFavorites(String name) {
        return new ArrayList<>(favoriteFriends.getOrDefault(name, new HashSet<>()));
    }

    public List<String> getActivityFeed(String name) {
        // Placeholder for activity feed retrieval
        return new ArrayList<>();
    }

    public void declineRequest(String name, String requestName) {
        denyRequest(name, requestName);
    }

    public boolean toggleNotifications(String name) {
        PrivacySettings settings = privacySettingsMap.computeIfAbsent(name, k -> new PrivacySettings ());
        settings.allowMessages = !settings.allowMessages;
        notifyPlayer(name, ChatColor.GREEN + "Notifications " + (settings.allowMessages ? "enabled" : "disabled"));
        return settings.allowMessages;
    }

    public void clearBlockedPlayers(String name) {
        blockedPlayers.remove(name);
        notifyPlayer(name, ChatColor.GREEN + "All blocked players have been cleared.");
    }

    public String getFriendNickname(String name, String friend) {
        // Implement logic to retrieve the nickname for the friend
        return ""; // Return the nickname or an empty string if none exists
    }

    public String getFavoriteFriends(String name) {
        Set<String> favorites = favoriteFriends.getOrDefault(name, new HashSet<>());
        return String.join(", ", favorites); // Return a comma-separated list of favorite friends
    }

    public void createFriendGroup(String name, String groupName) {
        // Implement logic to create a friend group for the player
    }

    public void addFriendToGroup(String name, String arg, String groupName) {
        // Implement logic to add a friend to a specific group
    }

    public void removeFriendFromGroup(String name, String arg, String groupName) {
        // Implement logic to remove a friend from a specific group
    }

    public List<String> getFriendsInGroup(String name, String groupName) {
        // Implement logic to retrieve friends in a specific group
        return new ArrayList<>(); // Placeholder
    }

    public void toggleFavoriteFriend(String name, String targetName) {
        Set<String> favorites = favoriteFriends.computeIfAbsent(name, k -> new HashSet<>());
        if (favorites.contains(targetName)) {
            favorites.remove(targetName);
            notifyPlayer(name, ChatColor.YELLOW + targetName + " has been removed from your favorites.");
        } else {
            favorites.add(targetName);
            notifyPlayer(name, ChatColor.GREEN + targetName + " has been added to your favorites.");
        }
    }

    public void setFriendNickname(String name, String targetName, String nickname) {
        // Implement logic to set a nickname for a friend
    }

    public void sendFriendGift(String name, String targetName, String giftType) {
        // Implement logic to send a gift to a friend
    }

    public void createFriendChallenge(String name, String targetName, String challengeType) {
        // Implement logic to create a challenge for a friend
    }

    public List<String> searchFriends(String name, String query) {
        // Implement logic to search friends based on the query
        return new ArrayList<>(); // Placeholder
    }

    public String generateFriendReport(String targetName) {
        // Implement logic to generate a report for a friend
        return ""; // Placeholder
    }

    public String exportFriendList(String name) {
        // Implement logic to export the friend list
        return ""; // Placeholder
    }

    public void importFriendList(String name, String importList) {
        // Implement logic to import a friend list
    }

    public void toggleFriendNotifications(String name, boolean enabled) {
        // Implement logic to toggle notifications for friends
    }

    public Collection<String> getFriendGroups(String name) {
        // Implement logic to retrieve friend groups for a player
        return new ArrayList<>(); // Placeholder
    }

    public String getStatus(String friend) {
        return statusMap.get(friend); // Return the status for the specified friend
    }

    public void setStatus(String name, String newStatus) {
        statusMap.put(name, newStatus); // Set the new status for the specified player
    }

    public Set<String> getOnlineFriends(String name) {
        Set<String> onlineFriends = new HashSet<>();
        Set<String> friends = friendsMap.get(name); // Get the friends of the specified player

        if (friends != null) {
            for (String friend : friends) {
                if (isOnline(friend)) { // Check if each friend is online
                    onlineFriends.add(friend);
                }
            }
        }
        return onlineFriends; // Return the set of online friends
    }

    private boolean isOnline(String friend) {
        return onlineFriendsSet.contains(friend);
    }
    public void updateOnlineStatus(Set<String> currentOnlinePlayers) {
        onlineFriendsSet.clear(); // Clear the current set
        onlineFriendsSet.addAll(currentOnlinePlayers); // Add the current online players
    }

    private void notifyPlayer(String playerName, String message) {
        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            player.sendMessage(message);
        }
    }

    boolean areFriends(String player1, String player2) {
        List<String> friends1 = getFriends(player1);
        return friends1.contains(player2);
    }

    private boolean hasReachedFriendLimit(String playerName) {
        // Implement logic to check if the player has reached their friend limit
        return false; // Placeholder for friend limit check
    }

    private void updateOnlineFriends(Player player) {
        String playerName = player.getName();
        FriendStatistics stats = statsCache.get(playerName);
        if (stats != null) {
            stats.setOnline(true);
            stats.setLastInteraction(System.currentTimeMillis());
        }

        // Update online status for friends
        List<String> friends = getFriends(playerName);
        for (String friend : friends) {
            FriendStatistics friendStats = statsCache.get(friend);
            if (friendStats != null) {
                friendStats.setOnline(true);
            }
        }
    }

    private void updateLastSeen(String playerName) {
        FriendStatistics stats = statsCache.get(playerName);
        if (stats != null) {
            stats.setOnline(false); // Set to offline when updating last seen
            stats.setLastInteraction(System.currentTimeMillis()); // Update last interaction time
        }
    }

    public void loadData() {
        // Implement logic to load data from files or database
    }

    public void initialize() {
        connectToDatabase();
        loadData();
        startAutoSave();
        startOnlineTracking();
    }
}