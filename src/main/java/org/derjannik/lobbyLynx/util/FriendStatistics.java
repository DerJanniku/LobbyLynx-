package org.derjannik.lobbyLynx.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendStatistics {
    // Original fields
    private List<String> totalFriends;
    private List<String> onlineFriends;
    private List<String> favoriteFriends;
    private List<String> friendGroups;
    private List<Long> friendshipDurations;
    private Map<String, Integer> friendshipLevels;

    // Additional fields from loadStatistics
    private long friendSince;
    private int messagesSent;
    private int gamesPlayed;
    private long lastInteraction;
    private String status;
    private boolean isOnline;

    // Constructor
    public FriendStatistics() {
        this.totalFriends = new ArrayList<>();
        this.onlineFriends = new ArrayList<>();
        this.favoriteFriends = new ArrayList<>();
        this.friendGroups = new ArrayList<>();
        this.friendshipDurations = new ArrayList<>();
        this.friendshipLevels = new HashMap<>();

        // Initialize additional fields
        this.friendSince = System.currentTimeMillis();
        this.messagesSent = 0;
        this.gamesPlayed = 0;
        this.lastInteraction = System.currentTimeMillis();
        this.status = "Hey there! I'm using LobbyLynx!";
        this.isOnline = false;
    }

    // Getters and setters for additional fields
    public long getFriendSince() {
        return friendSince;
    }

    public void setFriendSince(long friendSince) {
        this.friendSince = friendSince;
    }

    public int getMessagesSent() {
        return messagesSent;
    }

    public void setMessagesSent(int messagesSent) {
        this.messagesSent = messagesSent;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public long getLastInteraction() {
        return lastInteraction;
    }

    public void setLastInteraction(long lastInteraction) {
        this.lastInteraction = lastInteraction;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    // Methods to get friend statistics
    public char[] getTotalFriends() {
        return String.join(",", totalFriends).toCharArray();
    }

    public char[] getOnlineFriends() {
        return String.join(",", onlineFriends).toCharArray();
    }

    public char[] getFavoriteFriends() {
        return String.join(",", favoriteFriends).toCharArray();
    }

    public char[] getFriendGroups() {
        return String.join(",", friendGroups).toCharArray();
    }

    public long getAverageFriendshipDuration() {
        if (friendshipDurations.isEmpty()) {
            return 0;
        }
        long totalDuration = 0;
        for (Long duration : friendshipDurations) {
            totalDuration += duration;
        }
        return totalDuration / friendshipDurations.size();
    }

    public char[] getTotalFriendshipLevel() {
        return getFriendshipLevel();
    }

    public String getLongestFriendshipName() {
        if (friendshipDurations.isEmpty()) {
            return "";
        }
        long maxDuration = -1;
        String longestFriend = "";
        for (int i = 0; i < friendshipDurations.size(); i++) {
            if (friendshipDurations.get(i) > maxDuration) {
                maxDuration = friendshipDurations.get(i);
                longestFriend = totalFriends.get(i);
            }
        }
        return longestFriend;
    }

    public void addFriend(String friendName, long duration, int level) {
        totalFriends.add(friendName);
        friendshipDurations.add(duration);
        friendshipLevels.put(friendName, level);
    }

    public void addOnlineFriend(String friendName) {
        if (totalFriends.contains(friendName) && !onlineFriends.contains(friendName)) {
            onlineFriends.add(friendName);
        }
    }

    public void addFavoriteFriend(String friendName) {
        if (totalFriends.contains(friendName) && !favoriteFriends.contains(friendName)) {
            favoriteFriends.add(friendName);
        }
    }

    public void addFriendGroup(String groupName) {
        if (!friendGroups.contains(groupName)) {
            friendGroups.add(groupName);
        }
    }

    public void removeFriend(String friendName) {
        totalFriends.remove(friendName);
        onlineFriends.remove(friendName);
        favoriteFriends.remove(friendName);
        friendshipLevels.remove(friendName);
    }

    public void markFriendAsOffline(String friendName) {
        onlineFriends.remove(friendName);
    }

    public void setFriendshipLevel(String friend, int level) {
        friendshipLevels.put(friend, level);
    }

    public int getFriendshipLevelFor(String friend) {
        return friendshipLevels.getOrDefault(friend, 0);
    }

    public char[] getFriendshipLevel() {
        StringBuilder levels = new StringBuilder();
        for (Map.Entry<String, Integer> entry : friendshipLevels.entrySet()) {
            levels.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
        }
        // Remove the trailing comma if the StringBuilder is not empty
        if (levels.length() > 0) {
            levels.setLength(levels.length() - 1);
        }
        return levels.toString().toCharArray();
    }

    public Map<String, Integer> getFriendshipLevels() {
        return new HashMap<>(friendshipLevels); // Return a copy to prevent direct modification
    }
}