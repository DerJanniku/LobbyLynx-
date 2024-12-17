package org.derjannik.lobbyLynx.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.derjannik.lobbyLynx.LobbyLynx;
import org.derjannik.lobbyLynx.util.PrivacySettings;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class FriendManager {
    private final LobbyLynx plugin;
    private final File friendsFile;
    private final File friendRequestsFile;
    private final File friendStatsFile;
    private volatile FileConfiguration friendsConfig;
    private volatile FileConfiguration requestsConfig;
    private volatile FileConfiguration statsConfig;
    private final Map<String, Set<String>> friendCache;
    private final Map<String, Set<String>> favoriteCache;
    private final Map<String, List<String>> activityFeedCache;
    private final Map<String, Set<String>> pendingRequestsCache;
    private final Map<String, Long> lastSaveTime;
    private final Map<String, Long> lastInteractionTime;
    private static final long SAVE_COOLDOWN = 5000; // 5 seconds cooldown

    public FriendManager(LobbyLynx plugin) {
        this.plugin = plugin;
        this.friendsFile = new File(plugin.getDataFolder(), "friends.yml");
        this.friendRequestsFile = new File(plugin.getDataFolder(), "friend_requests.yml");
        this.friendStatsFile = new File(plugin.getDataFolder(), "friend_stats.yml");
        this.friendCache = new ConcurrentHashMap<>();
        this.favoriteCache = new ConcurrentHashMap<>();
        this.activityFeedCache = new ConcurrentHashMap<>();
        this.pendingRequestsCache = new ConcurrentHashMap<>();
        this.lastSaveTime = new ConcurrentHashMap<>();
        this.lastInteractionTime = new ConcurrentHashMap<>();
        loadConfigs();
        startCleanupTask();
    }

    private void startCleanupTask() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                long currentTime = System.currentTimeMillis();
                // Clean up interaction times older than 1 hour
                lastInteractionTime.entrySet().removeIf(entry -> 
                    currentTime - entry.getValue() > 3600000);
                
                // Auto-decline friend requests older than 24 hours
                for (Map.Entry<String, Set<String>> entry : pendingRequestsCache.entrySet()) {
                    String playerName = entry.getKey();
                    entry.getValue().removeIf(requester -> {
                        String path = "requests." + playerName.toLowerCase() + "." + requester.toLowerCase();
                        long requestTime = requestsConfig.getLong(path + ".time", 0);
                        if (currentTime - requestTime > 86400000) { // 24 hours
                            requestsConfig.set(path, null);
                            return true;
                        }
                        return false;
                    });
                }
                
                saveFriendsConfig();
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error during friend system cleanup task", e);
            }
        }, 20L * 3600, 20L * 3600); // Run every hour
    }

    private void loadConfigs() {
        try {
            createConfigIfNotExists("friends.yml");
            createConfigIfNotExists("friend_requests.yml");
            createConfigIfNotExists("friend_stats.yml");

            friendsConfig = YamlConfiguration.loadConfiguration(friendsFile);
            requestsConfig = YamlConfiguration.loadConfiguration(friendRequestsFile);
            statsConfig = YamlConfiguration.loadConfiguration(friendStatsFile);

            plugin.getLogger().info("Friend system configurations loaded successfully");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load friend system configurations", e);
        }
    }

    private void createConfigIfNotExists(String filename) {
        if (!new File(plugin.getDataFolder(), filename).exists()) {
            plugin.saveResource(filename, false);
        }
    }

    public Set<String> getFriends(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return new HashSet<>();
        }

        return friendCache.computeIfAbsent(playerName.toLowerCase(), k -> {
            List<String> friends = friendsConfig.getStringList("friends." + k);
            Set<String> friendSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
            friendSet.addAll(friends);
            return friendSet;
        });
    }

    public Set<String> getFavorites(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return new HashSet<>();
        }

        return favoriteCache.computeIfAbsent(playerName.toLowerCase(), k -> {
            List<String> favorites = friendsConfig.getStringList("favorites." + k);
            Set<String> favoriteSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
            favoriteSet.addAll(favorites);
            return favoriteSet;
        });
    }

    public List<String> getActivityFeed(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return new ArrayList<>();
        }

        return activityFeedCache.computeIfAbsent(playerName.toLowerCase(), k -> {
            List<String> feed = friendsConfig.getStringList("activity." + k);
            return Collections.synchronizedList(new ArrayList<>(feed));
        });
    }

    public void addActivityEntry(String playerName, String entry) {
        if (playerName == null || entry == null) {
            return;
        }

        try {
            List<String> feed = getActivityFeed(playerName.toLowerCase());
            synchronized (feed) {
                feed.add(0, entry);
                while (feed.size() > 50) { // Keep only last 50 entries
                    feed.remove(feed.size() - 1);
                }
            }
            scheduleSave(playerName);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error adding activity entry for " + playerName, e);
        }
    }

    public Map<String, Integer> getFriendStatistics(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return new HashMap<>();
        }

        try {
            Map<String, Integer> stats = new HashMap<>();
            String path = "stats." + playerName.toLowerCase();
            stats.put("total", statsConfig.getInt(path + ".total", 0));
            stats.put("online", statsConfig.getInt(path + ".online", 0));
            stats.put("favorite", statsConfig.getInt(path + ".favorite", 0));
            return stats;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error getting friend statistics for " + playerName, e);
            return new HashMap<>();
        }
    }

    public boolean isFavorite(String playerName, String friendName) {
        if (playerName == null || friendName == null) {
            return false;
        }
        return getFavorites(playerName.toLowerCase()).contains(friendName.toLowerCase());
    }

    public void toggleFavorite(String playerName, String friendName) {
        if (playerName == null || friendName == null) {
            return;
        }

        try {
            String lowerPlayerName = playerName.toLowerCase();
            String lowerFriendName = friendName.toLowerCase();
            Set<String> favorites = getFavorites(lowerPlayerName);

            if (favorites.contains(lowerFriendName)) {
                favorites.remove(lowerFriendName);
            } else {
                favorites.add(lowerFriendName);
            }

            saveFavorites(lowerPlayerName, favorites);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "Error toggling favorite status for " + playerName + " and " + friendName, e);
        }
    }

    public void setPrivacyLevel(String playerName, PrivacySettings level) {
        if (playerName == null || level == null) {
            return;
        }

        try {
            friendsConfig.set("privacy." + playerName.toLowerCase(), level.name());
            scheduleSave(playerName);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "Error setting privacy level for " + playerName, e);
        }
    }

    public PrivacySettings getPrivacyLevel(String playerName) {
        if (playerName == null) {
            return PrivacySettings.PUBLIC;
        }

        try {
            String level = friendsConfig.getString("privacy." + playerName.toLowerCase());
            return level != null ? PrivacySettings.valueOf(level) : PrivacySettings.PUBLIC;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "Error getting privacy level for " + playerName, e);
            return PrivacySettings.PUBLIC;
        }
    }

    private void scheduleSave(String playerName) {
        long currentTime = System.currentTimeMillis();
        Long lastSave = lastSaveTime.get(playerName);

        if (lastSave == null || currentTime - lastSave >= SAVE_COOLDOWN) {
            lastSaveTime.put(playerName, currentTime);
            saveFriendsConfig();
        }
    }

    private void saveFavorites(String playerName, Set<String> favorites) {
        try {
            friendsConfig.set("favorites." + playerName, new ArrayList<>(favorites));
            favoriteCache.put(playerName, favorites);
            scheduleSave(playerName);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "Error saving favorites for " + playerName, e);
        }
    }

    private synchronized void saveFriendsConfig() {
        try {
            friendsConfig.save(friendsFile);
            requestsConfig.save(friendRequestsFile);
            statsConfig.save(friendStatsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save friend configurations", e);
        }
    }

    public void cleanup() {
        try {
            saveFriendsConfig();
            friendCache.clear();
            favoriteCache.clear();
            activityFeedCache.clear();
            pendingRequestsCache.clear();
            lastSaveTime.clear();
            lastInteractionTime.clear();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error during friend manager cleanup", e);
        }
    }

    public boolean sendFriendRequest(String senderName, String receiverName) {
        if (senderName == null || receiverName == null) {
            return false;
        }

        try {
            String lowerSender = senderName.toLowerCase();
            String lowerReceiver = receiverName.toLowerCase();

            // Check if they're already friends
            if (getFriends(lowerReceiver).contains(lowerSender)) {
                return false;
            }

            // Check interaction cooldown
            long currentTime = System.currentTimeMillis();
            Long lastTime = lastInteractionTime.get(lowerSender + ":" + lowerReceiver);
            if (lastTime != null && currentTime - lastTime < 300000) { // 5 minutes cooldown
                return false;
            }

            // Add to pending requests
            Set<String> pendingRequests = pendingRequestsCache.computeIfAbsent(lowerReceiver, 
                k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
            
            if (pendingRequests.add(lowerSender)) {
                String path = "requests." + lowerReceiver + "." + lowerSender;
                requestsConfig.set(path + ".time", currentTime);
                requestsConfig.set(path + ".status", "PENDING");
                lastInteractionTime.put(lowerSender + ":" + lowerReceiver, currentTime);
                scheduleSave(lowerReceiver);
                return true;
            }
            return false;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "Error sending friend request from " + senderName + " to " + receiverName, e);
            return false;
        }
    }

    public boolean acceptFriendRequest(String playerName, String requesterName) {
        if (playerName == null || requesterName == null) {
            return false;
        }

        try {
            String lowerPlayer = playerName.toLowerCase();
            String lowerRequester = requesterName.toLowerCase();

            Set<String> pendingRequests = pendingRequestsCache.get(lowerPlayer);
            if (pendingRequests != null && pendingRequests.remove(lowerRequester)) {
                // Add to friends lists
                Set<String> playerFriends = getFriends(lowerPlayer);
                Set<String> requesterFriends = getFriends(lowerRequester);
                
                playerFriends.add(lowerRequester);
                requesterFriends.add(lowerPlayer);

                // Update stats
                updateFriendStats(lowerPlayer);
                updateFriendStats(lowerRequester);

                // Remove request
                requestsConfig.set("requests." + lowerPlayer + "." + lowerRequester, null);
                
                // Add activity entries
                addActivityEntry(lowerPlayer, "Accepted friend request from " + requesterName);
                addActivityEntry(lowerRequester, playerName + " accepted your friend request");

                scheduleSave(lowerPlayer);
                scheduleSave(lowerRequester);
                return true;
            }
            return false;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "Error accepting friend request from " + requesterName + " by " + playerName, e);
            return false;
        }
    }

    private void updateFriendStats(String playerName) {
        try {
            String path = "stats." + playerName.toLowerCase();
            int totalFriends = getFriends(playerName).size();
            int favoriteFriends = getFavorites(playerName).size();
            
            statsConfig.set(path + ".total", totalFriends);
            statsConfig.set(path + ".favorite", favoriteFriends);
            
            scheduleSave(playerName);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, 
                "Error updating friend stats for " + playerName, e);
        }
    }

    public void reload() {
        cleanup();
        loadConfigs();
    }

    // Friend status methods
    public String getStatus(String playerName) {
        return friendsConfig.getString("status." + playerName.toLowerCase(), "OFFLINE");
    }

    public void setStatus(String playerName, String status) {
        friendsConfig.set("status." + playerName.toLowerCase(), status);
        scheduleSave(playerName);
    }

    // Friend nickname methods
    public String getFriendNickname(String playerName, String friendName) {
        return friendsConfig.getString("nicknames." + playerName.toLowerCase() + "." + friendName.toLowerCase(), friendName);
    }

    public void setFriendNickname(String playerName, String friendName, String nickname) {
        friendsConfig.set("nicknames." + playerName.toLowerCase() + "." + friendName.toLowerCase(), nickname);
        scheduleSave(playerName);
    }

    // Friend group methods
    public Set<String> getFriendGroups(String playerName) {
        if (!friendsConfig.contains("groups." + playerName.toLowerCase())) {
            return new HashSet<>();
        }
        return friendsConfig.getConfigurationSection("groups." + playerName.toLowerCase()).getKeys(false);
    }

    public boolean createFriendGroup(String playerName, String groupName) {
        String path = "groups." + playerName.toLowerCase() + "." + groupName.toLowerCase();
        if (!friendsConfig.contains(path)) {
            friendsConfig.set(path + ".members", new ArrayList<>());
            scheduleSave(playerName);
            return true;
        }
        return false;
    }

    public boolean addFriendToGroup(String playerName, String groupName, String friendName) {
        String path = "groups." + playerName.toLowerCase() + "." + groupName.toLowerCase() + ".members";
        List<String> members = friendsConfig.getStringList(path);
        if (!members.contains(friendName.toLowerCase())) {
            members.add(friendName.toLowerCase());
            friendsConfig.set(path, members);
            scheduleSave(playerName);
            return true;
        }
        return false;
    }

    public boolean removeFriendFromGroup(String playerName, String groupName, String friendName) {
        String path = "groups." + playerName.toLowerCase() + "." + groupName.toLowerCase() + ".members";
        List<String> members = friendsConfig.getStringList(path);
        if (members.remove(friendName.toLowerCase())) {
            friendsConfig.set(path, members);
            scheduleSave(playerName);
            return true;
        }
        return false;
    }

    public Set<String> getFriendsInGroup(String playerName, String groupName) {
        String path = "groups." + playerName.toLowerCase() + "." + groupName.toLowerCase() + ".members";
        return new HashSet<>(friendsConfig.getStringList(path));
    }

    // Friend list methods
    public Set<String> getOnlineFriends(String playerName) {
        Set<String> onlineFriends = new HashSet<>();
        for (String friend : getFriends(playerName)) {
            if ("ONLINE".equals(getStatus(friend))) {
                onlineFriends.add(friend);
            }
        }
        return onlineFriends;
    }

    public Set<String> getFavoriteFriends(String playerName) {
        return getFavorites(playerName);
    }

    // Friend request methods
    public Set<String> getRequests(String playerName) {
        return pendingRequestsCache.getOrDefault(playerName.toLowerCase(), new HashSet<>());
    }

    public void removeFriend(String playerName, String friendName) {
        Set<String> playerFriends = getFriends(playerName.toLowerCase());
        Set<String> friendFriends = getFriends(friendName.toLowerCase());
        
        playerFriends.remove(friendName.toLowerCase());
        friendFriends.remove(playerName.toLowerCase());
        
        friendsConfig.set("friends." + playerName.toLowerCase(), new ArrayList<>(playerFriends));
        friendsConfig.set("friends." + friendName.toLowerCase(), new ArrayList<>(friendFriends));
        
        scheduleSave(playerName);
        scheduleSave(friendName);
    }

    public boolean areFriends(String player1, String player2) {
        return getFriends(player1.toLowerCase()).contains(player2.toLowerCase());
    }

    public void denyRequest(String playerName, String requesterName) {
        Set<String> requests = getRequests(playerName);
        if (requests.remove(requesterName.toLowerCase())) {
            requestsConfig.set("requests." + playerName.toLowerCase() + "." + requesterName.toLowerCase(), null);
            scheduleSave(playerName);
        }
    }

    // Friend blocking methods
    public Set<String> getBlockedPlayers(String playerName) {
        return new HashSet<>(friendsConfig.getStringList("blocked." + playerName.toLowerCase()));
    }

    public void blockPlayer(String playerName, String targetName) {
        Set<String> blockedPlayers = getBlockedPlayers(playerName);
        blockedPlayers.add(targetName.toLowerCase());
        friendsConfig.set("blocked." + playerName.toLowerCase(), new ArrayList<>(blockedPlayers));
        scheduleSave(playerName);
    }

    public void unblockPlayer(String playerName, String targetName) {
        Set<String> blockedPlayers = getBlockedPlayers(playerName);
        blockedPlayers.remove(targetName.toLowerCase());
        friendsConfig.set("blocked." + playerName.toLowerCase(), new ArrayList<>(blockedPlayers));
        scheduleSave(playerName);
    }

    // Friend messaging methods
    public void sendMessage(String senderName, String receiverName, String message) {
        if (!areFriends(senderName, receiverName)) return;
        
        String path = "messages." + receiverName.toLowerCase();
        List<String> messages = friendsConfig.getStringList(path);
        messages.add(senderName + ": " + message);
        friendsConfig.set(path, messages);
        scheduleSave(receiverName);
    }

    // Friend list export/import
    public String exportFriendList(String playerName) {
        Set<String> friends = getFriends(playerName);
        return String.join(",", friends);
    }

    public void importFriendList(String playerName, String friendList) {
        Set<String> friends = new HashSet<>(Arrays.asList(friendList.split(",")));
        friendsConfig.set("friends." + playerName.toLowerCase(), new ArrayList<>(friends));
        scheduleSave(playerName);
    }

    // Friend report generation
    public String generateFriendReport(String playerName) {
        StringBuilder report = new StringBuilder();
        // Logic to generate a report
        return report.toString();
    }
}
