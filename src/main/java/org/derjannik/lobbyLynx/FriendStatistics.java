package org.derjannik.lobbyLynx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendStatistics {
    // Fields to store friends and their associated data
    private List<String> totalFriends;        // List of all friends
    private List<String> onlineFriends;       // List of currently online friends
    private List<String> favoriteFriends;     // List of favorite friends
    private List<String> friendGroups;        // List of friend groups
    private List<Long> friendshipDurations;   // List of friendship durations in milliseconds
    private Map<String, Integer> friendshipLevels; // Map to store friendship levels by friend's name

    // Constructor
    public FriendStatistics() {
        this.totalFriends = new ArrayList<>();
        this.onlineFriends = new ArrayList<>();
        this.favoriteFriends = new ArrayList<>();
        this.friendGroups = new ArrayList<>();
        this.friendshipDurations = new ArrayList<>();
        this.friendshipLevels = new HashMap<>();
    }

    // Method to get total friends as a character array
    public char[] getTotalFriends() {
        return String.join(",", totalFriends).toCharArray();
    }

    // Method to get online friends as a character array
    public char[] getOnlineFriends() {
        return String.join(",", onlineFriends).toCharArray();
    }

    // Method to get favorite friends as a character array
    public char[] getFavoriteFriends() {
        return String.join(",", favoriteFriends).toCharArray();
    }

    // Method to get friend groups as a character array
    public char[] getFriendGroups() {
        return String.join(",", friendGroups).toCharArray();
    }

    // Method to calculate the average friendship duration
    public long getAverageFriendshipDuration() {
        if (friendshipDurations.isEmpty()) {
            return 0; // Return 0 if there are no friendships
        }
        long totalDuration = 0;
        for (Long duration : friendshipDurations) {
            totalDuration += duration;
        }
        return totalDuration / friendshipDurations.size(); // Return average duration
    }

    // Method to get total friendship levels as a character array
    public char[] getTotalFriendshipLevel() {
        StringBuilder levels = new StringBuilder();
        for (Map.Entry<String, Integer> entry : friendshipLevels.entrySet()) {
            levels.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
        }
        return levels.toString().toCharArray();
    }

    // Method to get the name of the longest friendship
    public String getLongestFriendshipName() {
        if (friendshipDurations.isEmpty()) {
            return ""; // Return empty string if there are no friendships
        }
        long maxDuration = -1;
        String longestFriend = "";
        for (int i = 0; i < friendshipDurations.size(); i++) {
            if (friendshipDurations.get(i) > maxDuration) {
                maxDuration = friendshipDurations.get(i);
                longestFriend = totalFriends.get(i);
            }
        }
        return longestFriend; // Return the name of the longest friendship
    }

    // Additional methods to manage friends and friendship data
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
        // Optionally, remove the friendship duration as well
    }

    public void markFriendAsOffline(String friendName) {
        onlineFriends.remove(friendName);
    }
}