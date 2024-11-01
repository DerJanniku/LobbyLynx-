package org.derjannik.lobbyLynx;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FriendManager {
    private final LobbyLynx plugin;
    private final File friendsFile;
    private final File requestsFile;
    private final File statsFile;
    private FileConfiguration friendsConfig;
    private FileConfiguration requestsConfig;
    private FileConfiguration statsConfig;
    private final Map<String, List<String>> friendCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> requestCache = new ConcurrentHashMap<>();
    private final Map<String, FriendStatistics> statsCache = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> onlineFriends = new ConcurrentHashMap<>();
    private final Map<String, String> playerStatus = new ConcurrentHashMap<>();
    private final Map<String, Long> lastSeen = new ConcurrentHashMap<>();

    private boolean useMysql;
    private Connection mysqlConnection;

    public FriendManager(LobbyLynx plugin) {
        this.plugin = plugin;
        this.friendsFile = new File(plugin.getDataFolder(), "friends.yml");
        this.requestsFile = new File(plugin.getDataFolder(), "friend_requests.yml");
        this.statsFile = new File(plugin.getDataFolder(), "friend_stats.yml");
        this.useMysql = plugin.getConfig().getBoolean("mysql.enabled", false);
        loadConfigs();
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

            notifyPlayer(playerName, ChatColor.GREEN + "You are now friends with " + requesterName);
            notifyPlayer(requesterName, ChatColor.GREEN + playerName + " accepted your friend request!");

            updateFriendshipStats(playerName, requesterName);
        }
    }

    private void addFriend(String player1, String player2) {
        List<String> friends = getFriends(player1);
        if (!friends.contains(player2)) {
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

        PrivacySettings receiverSettings = privacySettings.get(targetName);
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
        stats.messagesSent++;
        stats.lastInteraction = System.currentTimeMillis();
    }

    public void setShowLastSeen(String name, boolean showLastSeen) {
        PrivacySettings settings = privacySettings.computeIfAbsent(name, k -> new PrivacySettings());
        settings.showLastSeen = showLastSeen;
        notifyPlayer(name, ChatColor.GREEN + "Last seen visibility set to " + (showLastSeen ? "on" : "off"));
    }

    public void setPrivacyLevel(String name, PrivacyLevel level) {
        PrivacySettings settings = privacySettings.computeIfAbsent(name, k -> new PrivacySettings());
        settings.privateMode = (level == PrivacyLevel.PRIVATE);
        notifyPlayer(name, ChatColor.GREEN + "Privacy level set to " + (settings.privateMode ? "private" : "public"));
    }

    public Set<String> getBlockedPlayers(String playerName) {
        return blockedPlayers.getOrDefault(playerName, new HashSet<>());
    }

    // Friend Statistics Management
    private static class FriendStatistics {
        long friendSince;
        int messagesSent;
        int gamesPlayed;
        long lastInteraction;
        String status;
        boolean isOnline;
        Map<String, Integer> friendshipLevels;

        FriendStatistics() {
            this.friendSince = System.currentTimeMillis();
            this.messagesSent = 0;
            this.gamesPlayed = 0;
            this.lastInteraction = System.currentTimeMillis();
            this.status = "Hey there! I'm using LobbyLynx!";
            this.isOnline = false;
            this.friendshipLevels = new HashMap<>();
        }
    }

    private void updateFriendshipStats(String player1, String player2) {
        FriendStatistics stats = statsCache.computeIfAbsent(player1, k -> new FriendStatistics());
        stats.lastInteraction = System.currentTimeMillis();

        // Update friendship levels
        int currentLevel = stats.friendshipLevels.getOrDefault(player2, 0);
        if (currentLevel < getMaxFriendshipLevel()) {
            long friendshipDuration = System.currentTimeMillis() - stats.friendSince;
            int newLevel = calculateFriendshipLevel(friendshipDuration);
            if (newLevel > currentLevel) {
                stats.friendshipLevels.put(player2, newLevel);
                grantFriendshipRewards(player1, player2, newLevel);
            }
        }
    }

    private int getMaxFriendshipLevel() {
        return plugin.getConfig().getInt("friends.max_friendship_level", 10);
    }

    private int calculateFriendshipLevel(long duration) {
        // Convert duration to days
        long days = duration / (1000 * 60 * 60 * 24);
        // Simple level calculation: 1 level per week up to max level
        return Math.min(getMaxFriendshipLevel(), (int) (days / 7) + 1);
    }

    private void grantFriendshipRewards(String player1, String player2, int level) {
        String rewardCommand = plugin.getConfig().getString("friends.rewards.level_" + level, null);
        if (rewardCommand != null) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        rewardCommand.replace("%player%", player1));
                notifyPlayer(player1, ChatColor.GREEN + "You reached friendship level " + level + " with " + player2 + "!");
            });
        }
    }

    // Friend Privacy Settings
    private Map<String, Set<String>> blockedPlayers = new ConcurrentHashMap<>();
    private Map<String, PrivacySettings> privacySettings = new ConcurrentHashMap<>();

    public static class PrivacySettings {
        boolean showOnlineStatus = true;
        boolean allowFriendRequests = true;
        boolean showLastSeen = true;
        boolean allowMessages = true;
        boolean privateMode = false;
    }

    public void togglePrivacySetting(String playerName, String setting) {
        PrivacySettings settings = privacySettings.computeIfAbsent(playerName, k -> new PrivacySettings());
        switch (setting.toLowerCase()) {
            case "online_status":
                settings.showOnlineStatus = !settings.showOnlineStatus;
                break;
            case "friend_requests":
                settings.allowFriendRequests = !settings.allowFriendRequests;
                break;
            case "last_seen":
                settings.showLastSeen = !settings.showLastSeen;
                break;
            case "messages":
                settings.allowMessages = !settings.allowMessages;
                break;
            case "private_mode":
                settings.privateMode = !settings.privateMode;
                break;
        }
    }

    // Friend Messaging System
    public void sendFriendMessage(String sender, String receiver, String message) {
        if (!areFriends(sender, receiver)) {
            notifyPlayer(sender, ChatColor.RED + "You can only send messages to your friends!");
            return;
        }

        PrivacySettings receiverSettings = privacySettings.get(receiver);
        if (receiverSettings != null && !receiverSettings.allowMessages) {
            notifyPlayer(sender, ChatColor.RED + "This player is not accepting messages right now.");
            return;
        }

        String formattedMessage = ChatColor.GOLD + "[Friend] " +
                ChatColor.YELLOW + sender +
                ChatColor.WHITE + ": " + message;

        notifyPlayer(receiver, formattedMessage);
        notifyPlayer(sender, formattedMessage);

        // Update statistics
        FriendStatistics stats = statsCache.computeIfAbsent(sender, k -> new FriendStatistics());
        stats.messagesSent++;
        stats.lastInteraction = System.currentTimeMillis();
    }

    // Friend Status Management
    public void setStatus(String playerName, String status) {
        playerStatus.put(playerName, status);
        notifyFriends(playerName, ChatColor.YELLOW + playerName + " updated their status: " + status);
    }

    public String getStatus(String playerName) {
        return playerStatus.getOrDefault(playerName, "Hey there! I'm using LobbyLynx!");
    }

    // Online Friend Tracking
    private void updateOnlineFriends(Player player) {
        String playerName = player.getName();
        Set<String> online = new HashSet<>();

        for (String friend : getFriends(playerName)) {
            Player friendPlayer = Bukkit.getPlayer(friend);
            if (friendPlayer != null && friendPlayer.isOnline()) {
                online.add(friend);
            }
        }

        onlineFriends.put(playerName, online);
    }

    public Set<String> getOnlineFriends(String playerName) {
        return onlineFriends.getOrDefault(playerName, new HashSet<>());
    }

    // Last Seen Tracking
    private void updateLastSeen(String playerName) {
        lastSeen.put(playerName, System.currentTimeMillis());
    }

    public String getLastSeen(String playerName) {
        Long time = lastSeen.get(playerName);
        if (time == null) return "Never";

        long diff = System.currentTimeMillis() - time;
        if (diff < 60000) return "Just now";
        if (diff < 3600000) return (diff / 60000) + " minutes ago";
        if (diff < 86400000) return (diff / 3600000) + " hours ago";
        return (diff / 86400000) + " days ago";
    }

    // Utility Methods
    private void notifyPlayer(String playerName, String message) {
        Player player = Bukkit.getPlayer(playerName);
        if (player != null && player.isOnline()) {
            player.sendMessage(message);
        }
    }

    private void notifyFriends(String playerName, String message) {
        for (String friend : getFriends(playerName)) {
            notifyPlayer(friend, message);
        }
    }

    private boolean hasReachedFriendLimit(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) return true;

        int currentFriends = getFriends(playerName).size();
        int maxFriends = plugin.getConfig().getInt("friends.max_friends." + getPlayerGroup(player), 50);
        return currentFriends >= maxFriends;
    }

    private String getPlayerGroup(Player player) {
        // This method should return the player's permission group
        // Implement this based on your permission system
        return "default";
    }

    public boolean areFriends(String player1, String player2) {
        List<String> friends = getFriends(player1);
        return friends.contains(player2);
    }

    // Friend Groups
    private Map<String, Map<String, List<String>>> friendGroups = new ConcurrentHashMap<>();

    public void createFriendGroup(String playerName, String groupName) {
        friendGroups.computeIfAbsent(playerName, k -> new HashMap<>()).put(groupName, new ArrayList<>());
        notifyPlayer(playerName, ChatColor.GREEN + "Friend group '" + groupName + "' created!");
    }

    public void addFriendToGroup(String playerName, String friendName, String groupName) {
        Map<String, List<String>> groups = friendGroups.computeIfAbsent(playerName, k -> new HashMap<>());
        List<String> groupMembers = groups.computeIfAbsent(groupName, k -> new ArrayList<>());
        if (!groupMembers.contains(friendName)) {
            groupMembers.add(friendName);
            notifyPlayer(playerName, ChatColor.GREEN + friendName + " added to group '" + groupName + "'!");
        }
    }

    public void removeFriendFromGroup(String playerName, String friendName, String groupName) {
        Map<String, List<String>> groups = friendGroups.get(playerName);
        if (groups != null) {
            List<String> groupMembers = groups.get(groupName);
            if (groupMembers != null) {
                groupMembers.remove(friendName);
                notifyPlayer(playerName, ChatColor.YELLOW + friendName + " removed from group '" + groupName + "'!");
            }
        }
    }

    public List<String> getFriendGroups(String playerName) {
        return new ArrayList<>(friendGroups.getOrDefault(playerName, new HashMap<>()).keySet());
    }

    public List<String> getFriendsInGroup(String playerName, String groupName) {
        Map<String, List<String>> groups = friendGroups.get(playerName);
        if (groups != null) {
            return new ArrayList<>(groups.getOrDefault(groupName, new ArrayList<>()));
        }
        return new ArrayList<>();
    }

    // Friend Suggestions
    public List<String> getFriendSuggestions(String playerName) {
        List<String> suggestions = new ArrayList<>();
        List<String> playerFriends = getFriends(playerName);

        for (String friend : playerFriends) {
            List<String> friendOfFriends = getFriends(friend);
            for (String potentialFriend : friendOfFriends) {
                if (!playerFriends.contains(potentialFriend) && !potentialFriend.equals(playerName)) {
                    suggestions.add(potentialFriend);
                }
            }
        }

        // Sort suggestions by frequency (most common friends of friends first)
        suggestions.sort((a, b) -> Collections.frequency(suggestions, b) - Collections.frequency(suggestions, a));

        // Remove duplicates
        return new ArrayList<>(new LinkedHashSet<>(suggestions));
    }

    // Friend Activity Feed
    private Map<String, List<FriendActivity>> activityFeed = new ConcurrentHashMap<>();

    public static class FriendActivity {
        String friendName;
        String activity;
        long timestamp;

        FriendActivity(String friendName, String activity) {
            this.friendName = friendName;
            this.activity = activity;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public void addFriendActivity(String playerName, String activity) {
        FriendActivity newActivity = new FriendActivity(playerName, activity);
        for (String friend : getFriends(playerName)) {
            activityFeed.computeIfAbsent(friend, k -> new ArrayList<>()).add(newActivity);
        }
    }

    public List<FriendActivity> getFriendActivityFeed(String playerName, int limit) {
        List<FriendActivity> feed = activityFeed.getOrDefault(playerName, new ArrayList<>());
        feed.sort((a, b) -> Long.compare(b.timestamp, a.timestamp)); // Sort by most recent
        return feed.subList(0, Math.min(feed.size(), limit));
    }

    // Friend Leaderboard
    public List<Map.Entry<String, Integer>> getFriendLeaderboard(String category, int limit) {
        Map<String, Integer> leaderboard = new HashMap<>();

        for (Map.Entry<String, FriendStatistics> entry : statsCache.entrySet()) {
            FriendStatistics stats = entry.getValue();
            int score = 0;
            switch (category) {
                case "messages":
                    score = stats.messagesSent;
                    break;
                case "games":
                    score = stats.gamesPlayed;
                    break;
                case "friends":
                    score = getFriends(entry.getKey()).size();
                    break;
            }
            leaderboard.put(entry.getKey(), score);
        }

        List<Map.Entry<String, Integer>> sortedLeaderboard = new ArrayList<>(leaderboard.entrySet());
        sortedLeaderboard.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        return sortedLeaderboard.subList(0, Math.min(sortedLeaderboard.size(), limit));
    }

    // Friend Events
    public void friendJoinedServer(String playerName) {
        notifyFriends(playerName, ChatColor.GREEN + playerName + " has joined the server!");
        addFriendActivity(playerName, "joined the server");
    }

    public void friendLeftServer(String playerName) {
        notifyFriends(playerName, ChatColor.YELLOW + playerName + " has left the server!");
        addFriendActivity(playerName, "left the server");
    }

    public void friendAchievement(String playerName, String achievement) {
        notifyFriends(playerName, ChatColor.GOLD + playerName + " has earned the achievement: " + achievement);
        addFriendActivity(playerName, "earned the achievement: " + achievement);
    }

    // Data Management
    private FriendStatistics loadStatistics(String playerName) {
        FriendStatistics stats = new FriendStatistics();
        ConfigurationSection section = statsConfig.getConfigurationSection(playerName);
        if (section != null) {
            stats.friendSince = section.getLong("friendSince", stats.friendSince);
            stats.messagesSent = section.getInt("messagesSent", stats.messagesSent);
            stats.gamesPlayed = section.getInt("gamesPlayed", stats.gamesPlayed);
            stats.lastInteraction = section.getLong("lastInteraction", stats.lastInteraction);
            stats.status = section.getString("status", stats.status);
            stats.isOnline = section.getBoolean("isOnline", stats.isOnline);
            ConfigurationSection levelsSection = section.getConfigurationSection("friendshipLevels");
            if (levelsSection != null) {
                for (String friend : levelsSection.getKeys(false)) {
                    stats.friendshipLevels.put(friend, levelsSection.getInt(friend));
                }
            }
        }
        return stats;
    }

    private void saveStatistics(String playerName, FriendStatistics stats) {
        ConfigurationSection section = statsConfig.createSection(playerName);
        section.set("friendSince", stats.friendSince);
        section.set("messagesSent", stats.messagesSent);
        section.set("gamesPlayed", stats.gamesPlayed);
        section.set("lastInteraction", stats.lastInteraction);
        section.set("status", stats.status);
        section.set("isOnline", stats.isOnline);
        ConfigurationSection levelsSection = section.createSection("friendshipLevels");
        for (Map.Entry<String, Integer> entry : stats.friendshipLevels.entrySet()) {
            levelsSection.set(entry.getKey(), entry.getValue());
        }
    }

    // Friend Nicknames
    private Map<String, Map<String, String>> friendNicknames = new ConcurrentHashMap<>();

    public void setFriendNickname(String playerName, String friendName, String nickname) {
        friendNicknames.computeIfAbsent(playerName, k -> new HashMap<>()).put(friendName, nickname);
        notifyPlayer(playerName, ChatColor.GREEN + "Nickname for " + friendName + " set to " + nickname);
    }

    public String getFriendNickname(String playerName, String friendName) {
        return friendNicknames.getOrDefault(playerName, new HashMap<>()).getOrDefault(friendName, friendName);
    }

    // Friend Favorite System
    private Map<String, Set<String>> favoriteFriends = new ConcurrentHashMap<>();

    public void toggleFavoriteFriend(String playerName, String friendName) {
        Set<String> favorites = favoriteFriends.computeIfAbsent(playerName, k -> new HashSet<>());
        if (favorites.contains(friendName)) {
            favorites.remove(friendName);
            notifyPlayer(playerName, ChatColor.YELLOW + friendName + " removed from favorites");
        } else {
            favorites.add(friendName);
            notifyPlayer(playerName, ChatColor.GREEN + friendName + " added to favorites");
        }
    }

    public Set<String> getFavoriteFriends(String playerName) {
        return new HashSet<>(favoriteFriends.getOrDefault(playerName, new HashSet<>()));
    }

    // Friend Gifts
    public void sendFriendGift(String senderName, String receiverName, String giftType) {
        if (!areFriends(senderName, receiverName)) {
            notifyPlayer(senderName, ChatColor.RED + "You can only send gifts to your friends!");
            return;
        }

        // Implement gift logic here (e.g., virtual items, effects, etc.)
        notifyPlayer(senderName, ChatColor.GREEN + "You sent a " + giftType + " gift to " + receiverName);
        notifyPlayer(receiverName, ChatColor.GREEN + "You received a " + giftType + " gift from " + senderName);
        addFriendActivity(senderName, "sent a " + giftType + " gift to " + receiverName);
    }

    // Friend Challenges
    private Map<String, List<FriendChallenge>> activeChallenges = new ConcurrentHashMap<>();

    public static class FriendChallenge {
        String challenger;
        String challenged;
        String challengeType;
        boolean completed;

        FriendChallenge(String challenger, String challenged, String challengeType) {
            this.challenger = challenger;
            this.challenged = challenged;
            this.challengeType = challengeType;
            this.completed = false;
        }
    }

    public void createFriendChallenge(String challenger, String challenged, String challengeType) {
        FriendChallenge challenge = new FriendChallenge(challenger, challenged, challengeType);
        activeChallenges.computeIfAbsent(challenged, k -> new ArrayList<>()).add(challenge);
        notifyPlayer(challenger, ChatColor.GREEN + "Challenge sent to " + challenged);
        notifyPlayer(challenged, ChatColor.YELLOW + challenger + " has challenged you to " + challengeType);
    }

    public List<FriendChallenge> getActiveChallenges(String playerName) {
        return new ArrayList<>(activeChallenges.getOrDefault(playerName, new ArrayList<>()));
    }

    public void completeFriendChallenge(String playerName, int challengeIndex) {
        List<FriendChallenge> challenges = activeChallenges.get(playerName);
        if (challenges != null && challengeIndex < challenges.size()) {
            FriendChallenge challenge = challenges.get(challengeIndex);
            challenge.completed = true;
            notifyPlayer(playerName, ChatColor.GREEN + "Challenge completed!");
            notifyPlayer(challenge.challenger, ChatColor.GREEN + playerName + " completed your " + challenge.challengeType + " challenge!");
            addFriendActivity(playerName, "completed a " + challenge.challengeType + " challenge from " + challenge.challenger);
        }
    }

    // Friend Notifications
    public void toggleFriendNotifications(String playerName, boolean enabled) {
        PrivacySettings settings = privacySettings.computeIfAbsent(playerName, k -> new PrivacySettings());
        settings.allowMessages = enabled;
        notifyPlayer(playerName, ChatColor.GREEN + "Friend notifications " + (enabled ? "enabled" : "disabled"));
    }

    // Friend Search
    public List<String> searchFriends(String playerName, String query) {
        List<String> allFriends = getFriends(playerName);
        return allFriends.stream()
                .filter(friend -> friend.toLowerCase().contains(query.toLowerCase()))
                .collect(java.util.stream.Collectors.toList());
    }

    // Friend Import/Export
    public String exportFriendList(String playerName) {
        List<String> friends = getFriends(playerName);
        return String.join(",", friends);
    }

    public void importFriendList(String playerName, String friendListString) {
        String[] friendArray = friendListString.split(",");
        for (String friend : friendArray) {
            if (!areFriends(playerName, friend)) {
                addFriend(playerName, friend);
                notifyPlayer(playerName, ChatColor.GREEN + friend + " added to your friend list");
            }
        }
    }

    // Friend Statistics Report
    public String generateFriendReport(String playerName) {
        StringBuilder report = new StringBuilder();
        report.append("Friend Report for ").append(playerName).append("\n");
        report.append("Total Friends: ").append(getFriends(playerName).size()).append("\n");
        report.append("Online Friends: ").append(getOnlineFriends(playerName).size()).append("\n");
        report.append("Favorite Friends: ").append(getFavoriteFriends(playerName).size()).append("\n");
        report.append("Messages Sent: ").append(statsCache.getOrDefault(playerName, new FriendStatistics()).messagesSent).append("\n");
        report.append("Games Played: ").append(statsCache.getOrDefault(playerName, new FriendStatistics()).gamesPlayed).append("\n");
        return report.toString();
    }

    // Clean up and save data when the plugin is disabled
    public void onDisable() {
        saveAllData();
    }
}