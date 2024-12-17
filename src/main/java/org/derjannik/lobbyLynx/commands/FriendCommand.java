package org.derjannik.lobbyLynx.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.derjannik.lobbyLynx.managers.FriendManager;
import org.derjannik.lobbyLynx.LobbyLynx;
import org.derjannik.lobbyLynx.util.PrivacySettings;


import java.util.*;
import java.util.stream.Collectors;

public class FriendCommand implements CommandExecutor, TabCompleter {
    private final LobbyLynx plugin;
    private final FriendManager friendManager;
    private final Map<String, Long> commandCooldowns = new HashMap<>();
    private final long COOLDOWN_DURATION = 2000; // 2 seconds cooldown

    public FriendCommand(LobbyLynx plugin, FriendManager friendManager) {
        this.plugin = plugin;
        this.friendManager = friendManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lynx.friend.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use friend commands.");
            return true;
        }

        // Check cooldown
        if (isOnCooldown(player)) {
            return true;
        }

        if (args.length == 0) {
            openFriendGUI(player);
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "list":
                    handleListCommand(player, args);
                    break;
                case "add":
                    handleAddCommand(player, args);
                    break;
                case "remove":
                    handleRemoveCommand(player, args);
                    break;
                case "requests":
                    handleRequestsCommand(player, args);
                    break;
                case "accept":
                    handleAcceptCommand(player, args);
                    break;
                case "deny":
                    handleDenyCommand(player, args);
                    break;
                case "block":
                    handleBlockCommand(player, args);
                    break;
                case "unblock":
                    handleUnblockCommand(player, args);
                    break;
                case "message":
                case "msg":
                    handleMessageCommand(player, args);
                    break;
                case "status":
                    handleStatusCommand(player, args);
                    break;
                case "group":
                    handleGroupCommand(player, args);
                    break;
                case "favorite":
                    handleFavoriteCommand(player, args);
                    break;
                case "nickname":
                    handleNicknameCommand(player, args);
                    break;
                case "gift":
                    handleGiftCommand(player, args);
                    break;
                case "challenge":
                    handleChallengeCommand(player, args);
                    break;
                case "search":
                    handleSearchCommand(player, args);
                    break;
                case "stats":
                    handleStatsCommand(player, args);
                    break;
                case "export":
                    handleExportCommand(player);
                    break;
                case "import":
                    handleImportCommand(player, args);
                    break;
                case "settings":
                    handleSettingsCommand(player, args);
                    break;
                case "help":
                    handleHelpCommand(player, args);
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Unknown command. Type /friend help for a list of commands.");
                    break;
            }
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "An error occurred while executing the command: " + e.getMessage());
            plugin.getLogger().severe("Error executing FriendCommand: " + e.getMessage());
            e.printStackTrace();
        }

        setCooldown(player);
        return true;
    }



    private void handleListCommand(Player player, String[] args) {
        if (args.length == 1) {
            // Default list view
            List<String> friends = new ArrayList<>(friendManager.getFriends(player.getName()));
            if (friends.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "You don't have any friends yet.");
                return;
            }

            player.sendMessage(ChatColor.GREEN + "=== Your Friends (" + friends.size() + ") ===");
            for (String friend : friends) {
                boolean isOnline = Bukkit.getPlayer(friend) != null;
                String status = friendManager.getStatus(friend);
                String nickname = friendManager.getFriendNickname(player.getName(), friend);
                boolean isFavorite = friendManager.getFavoriteFriends(player.getName()).contains(friend);

                player.sendMessage(
                        (isFavorite ? "⭐ " : "") +
                                (isOnline ? ChatColor.GREEN : ChatColor.GRAY) + friend +
                                (nickname.equals(friend) ? "" : ChatColor.YELLOW + " (" + nickname + ")") +
                                ChatColor.GRAY + " - " + status
                );
            }
        } else if (args[1].equalsIgnoreCase("online")) {
            // Show only online friends
            Set<String> onlineFriends = friendManager.getOnlineFriends(player.getName());
            player.sendMessage(ChatColor.GREEN + "=== Online Friends (" + onlineFriends.size() + ") ===");
            for (String friend : onlineFriends) {
                player.sendMessage(ChatColor.GREEN + "• " + friend);
            }
        }
    }

    private void handleAddCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /friend add <player>");
            return;
        }

        String targetName = args[1];
        Player targetPlayer = Bukkit.getPlayer(targetName);

        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "Player " + targetName + " is not online.");
            return;
        }

        if (player.getName().equals(targetName)) {
            player.sendMessage(ChatColor.RED + "You can't add yourself as a friend.");
            return;
        }

        friendManager.sendFriendRequest(player.getName(), targetName);
    }

    private void handleRemoveCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /friend remove <player>");
            return;
        }

        String targetName = args[1];
        if (!friendManager.areFriends(player.getName(), targetName)) {
            player.sendMessage(ChatColor.RED + targetName + " is not in your friend list.");
            return;
        }

        friendManager.removeFriend(player.getName(), targetName);
    }

    private void handleRequestsCommand(Player player, String[] args) {
        List<String> requests = new ArrayList<>(friendManager.getRequests(player.getName()));
        if (requests.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "You don't have any pending friend requests.");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "=== Pending Friend Requests (" + requests.size() + ") ===");
        for (String requester : requests) {
            player.sendMessage(ChatColor.YELLOW + "• " + requester +
                    ChatColor.GRAY + " [" +
                    ChatColor.GREEN + "/friend accept " + requester +
                    ChatColor.GRAY + " | " +
                    ChatColor.RED + "/friend deny " + requester +
                    ChatColor.GRAY + "]");
        }
    }

    private void handleAcceptCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /friend accept <player>");
            return;
        }

        String requesterName = args[1];
        List<String> requests = new ArrayList<>(friendManager.getRequests(player.getName()));
        if (!requests.contains(requesterName)) {
            player.sendMessage(ChatColor.RED + "You don't have a friend request from " + requesterName);
            return;
        }

        friendManager.acceptFriendRequest(player.getName(), requesterName);
        player.sendMessage(ChatColor.GREEN + "You are now friends with " + requesterName);
    }

    private void handleDenyCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /friend deny <player>");
            return;
        }

        String requesterName = args[1];
        List<String> requests = new ArrayList<>(friendManager.getRequests(player.getName()));
        if (!requests.contains(requesterName)) {
            player.sendMessage(ChatColor.RED + "You don't have a friend request from " + requesterName);
            return;
        }

        friendManager.denyRequest(player.getName(), requesterName);
        player.sendMessage(ChatColor.YELLOW + "You have denied the friend request from " + requesterName);
    }

    private void handleBlockCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /friend block <player>");
            return;
        }

        String targetName = args[1];
        friendManager.blockPlayer(player.getName(), targetName);
        player.sendMessage(ChatColor.YELLOW + "You have blocked " + targetName);
    }

    private void handleUnblockCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /friend unblock <player>");
            return;
        }

        String targetName = args[1];
        friendManager.unblockPlayer(player.getName(), targetName);
        player.sendMessage(ChatColor.GREEN + "You have unblocked " + targetName);
    }

    private void handleMessageCommand(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /friend message <player> <message>");
            return;
        }

        String targetName = args[1];
        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        friendManager.sendMessage(player.getName(), targetName, message);
    }

    private void handleStatusCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /friend status <new status>");
            return;
        }

        String newStatus = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        friendManager.setStatus(player.getName(), newStatus);
        player.sendMessage(ChatColor.GREEN + "Your status has been updated to: " + newStatus);
    }

    private void handleGroupCommand(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /friend group <create|add|remove|list> <group name> [player]");
            return;
        }

        String action = args[1];
        String groupName = args[2];

        switch (action.toLowerCase()) {
            case "create":
                friendManager.createFriendGroup(player.getName(), groupName);
                break;
            case "add":
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED + "Usage: /friend group add <group name> <player>");
                    return;
                }
                friendManager.addFriendToGroup(player.getName(), args[3], groupName);
                break;
            case "remove":
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED + "Usage: /friend group remove <group name> <player>");
                    return;
                }
                friendManager.removeFriendFromGroup(player.getName(), args[3], groupName);
                break;
            case "list":
            List<String> groupMembers = new ArrayList<>(friendManager.getFriendsInGroup(player.getName(), groupName));
                player.sendMessage(ChatColor.GREEN + "Friends in group '" + groupName + "': " + String.join(", ", groupMembers));
                break;
            default:
                player.sendMessage(ChatColor.RED + "Invalid group action. Use create, add, remove, or list.");
        }
    }

    private void handleFavoriteCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /friend favorite <player>");
            return;
        }

        String targetName = args[1];
        friendManager.toggleFavorite(player.getName(), targetName);
    }

    private void handleNicknameCommand(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /friend nickname <player> <nickname>");
            return;
        }

        String targetName = args[1];
        String nickname = args[2];
        friendManager.setFriendNickname(player.getName(), targetName, nickname);
    }

    private void handleGiftCommand(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /friend gift <player> <gift type>");
            return;
        }
        player.sendMessage(ChatColor.RED + "Gift feature is not implemented yet.");
    }

    private void handleChallengeCommand(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /friend challenge <player> <challenge type>");
            return;
        }
        player.sendMessage(ChatColor.RED + "Challenge feature is not implemented yet.");
    }

    private void handleSearchCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /friend search <query>");
            return;
        }

        String query = args[1];
            List<String> results = new ArrayList<>(friendManager.getFriends(player.getName()).stream()
                .filter(friend -> friend.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList()));
        player.sendMessage(ChatColor.GREEN + "Search results for '" + query + "': " + String.join(", ", results));
    }

    private void handleStatsCommand(Player player, String[] args) {
        String targetName = player.getName();
        if (args.length > 1) {
            targetName = args[1];
        }

        String report = friendManager.generateFriendReport(targetName);
        player.sendMessage(ChatColor.GREEN + "=== Friend Statistics ===");
        player.sendMessage(report);
    }

    private void handleExportCommand(Player player) {
        String exportedList = friendManager.exportFriendList(player.getName());
        player.sendMessage(ChatColor.GREEN + "Your friend list: " + exportedList);
        player.sendMessage(ChatColor.YELLOW + "Copy this list to import it later or on another server.");
    }

    private void handleImportCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /friend import <friend list>");
            return;
        }

        String importList = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        friendManager.importFriendList(player.getName(), importList);
        player.sendMessage(ChatColor.GREEN + "Friend list imported successfully.");
    }

    private void handleSettingsCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /friend settings <option> [value]");
            player.sendMessage(ChatColor.YELLOW + "Options: notifications, privacy, lastSeen");
            return;
        }

        String option = args[1].toLowerCase();
        switch (option) {
            case "notifications":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /friend settings notifications <on/off>");
                    return;
                }
                boolean enabled = args[2].equalsIgnoreCase("on");
                // Notifications feature not implemented yet
                player.sendMessage(ChatColor.RED + "Notifications feature is not implemented yet.");
                player.sendMessage(ChatColor.GREEN + "Friend notifications " + (enabled ? "enabled" : "disabled"));
                break;

            case "privacy":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /friend settings privacy <public/friends/private>");
                    return;
                }
                try {
                    PrivacySettings level = PrivacySettings.valueOf(args[2].toUpperCase());
                    friendManager.setPrivacyLevel(player.getName(), level);
                    player.sendMessage(ChatColor.GREEN + "Privacy level set to " + level);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(ChatColor.RED + "Invalid privacy level. Use PUBLIC, FRIENDS_ONLY, or PRIVATE.");
                }
                break;

            case "lastseen":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /friend settings lastSeen <on/off>");
                    return;
                }
                boolean showLastSeen = args[2].equalsIgnoreCase("on");
                // Last seen feature not implemented yet
                player.sendMessage(ChatColor.RED + "Last seen feature is not implemented yet.");
                player.sendMessage(ChatColor.GREEN + "Last seen visibility set to " + (showLastSeen ? "on" : "off"));
                break;

            default:
                player.sendMessage(ChatColor.RED + "Unknown setting option. Use notifications, privacy, or lastSeen.");
        }
    }

    private void handleHelpCommand(Player player, String[] args) {
        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid page number.");
                return;
            }
        }

        List<String> helpMessages = Arrays.asList(
                "/friend list - View your friend list",
                "/friend add <player> - Send a friend request",
                "/friend remove <player> - Remove a friend",
                "/friend requests - View pending friend requests",
                "/friend accept <player> - Accept a friend request",
                "/friend deny <player> - Deny a friend request",
                "/friend block <player> - Block a player",
                "/friend unblock <player> - Unblock a player",
                "/friend message <player> <message> - Send a private message",
                "/friend status <status> - Set your status",
                "/friend group <create|add|remove|list> <group> [player] - Manage friend groups",
                "/friend favorite <player> - Toggle favorite status for a friend",
                "/friend nickname <player> <nickname> - Set a nickname for a friend",
                "/friend gift <player> <gift> - Send a gift to a friend",
                "/friend challenge <player> <type> - Send a challenge to a friend",
                "/friend search <query> - Search your friend list",
                "/friend stats [player] - View friend statistics",
                "/friend export - Export your friend list",
                "/friend import <list> - Import a friend list",
                "/friend settings <option> [value] - Manage friend settings"
        );

        int totalPages = (int) Math.ceil(helpMessages.size() / 10.0);
        if (page < 1 || page > totalPages) {
            player.sendMessage(ChatColor.RED + "Invalid page number. Please use a number between 1 and " + totalPages);
            return;
        }

        player.sendMessage(ChatColor.GREEN + "=== Friend Command Help (Page " + page + "/" + totalPages + ") ===");
        for (int i = (page - 1) * 10; i < Math.min(page * 10, helpMessages.size()); i++) {
            player.sendMessage(ChatColor.YELLOW + helpMessages.get(i));
        }
        player.sendMessage(ChatColor.GREEN + "Use '/friend help <page>' to view more commands.");
    }

    private void openFriendGUI(Player player) {
        // This method would open a custom GUI for managing friends
        // You'd need to implement this separately using your GUI system
        player.sendMessage(ChatColor.GREEN + "Opening friend management GUI...");
        // TODO: Implement GUI opening logic
    }

    private boolean isOnCooldown(Player player) {
        if (commandCooldowns.containsKey(player.getName())) {
            long lastCommandTime = commandCooldowns.get(player.getName());
            if (System.currentTimeMillis() - lastCommandTime < COOLDOWN_DURATION) {
                player.sendMessage(ChatColor.RED + "Please wait before using this command again.");
                return true;
            }
        }
        return false;
    }

    private void setCooldown(Player player) {
        commandCooldowns.put(player.getName(), System.currentTimeMillis());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("list", "add", "remove", "requests", "accept", "deny", "block", "unblock", "message", "status", "group", "favorite", "nickname", "gift", "challenge", "search", "stats", "export", "import", "settings", "help"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "add":
                case "remove":
                case "accept":
                case "deny":
                case "block":
                case "unblock":
                case "message":
                case "favorite":
                case "nickname":
                case "gift":
                case "challenge":
                    completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
                    break;
                case "group":
                    completions.addAll(Arrays.asList("create", "add", "remove", "list"));
                    break;
                case "settings":
                    completions.addAll(Arrays.asList("notifications", "privacy", "lastSeen"));
                    break;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("group") && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))) {
                completions.addAll(friendManager.getFriendGroups(sender.getName()));
            } else if (args[0].equalsIgnoreCase("settings")) {
                switch (args[1].toLowerCase()) {
                    case "notifications":
                    case "lastseen":
                        completions.addAll(Arrays.asList("on", "off"));
                        break;
                    case "privacy":
                        completions.addAll(Arrays.asList("public", "friends", "private"));
                        break;
                }
            }
        }

        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
    @SuppressWarnings("unused")
    private boolean isPlayerOnline(String playerName) {
        return Bukkit.getPlayer(playerName) != null;
    }

    // Utility method to get online status as a colored string

    // Utility method to format time difference
    private String formatTimeDifference(long timeDifference) {
        long seconds = timeDifference / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else {
            return "just now";
        }
    }

    @SuppressWarnings("unused")
    private void sendPaginatedMessage(Player player, List<String> messages, String title, int page, int messagesPerPage) {
        int totalPages = (int) Math.ceil((double) messages.size() / messagesPerPage);
        if (page < 1 || page > totalPages) {
            player.sendMessage(ChatColor.RED + "Invalid page number. Please use a number between 1 and " + totalPages);
            return;
        }

        player.sendMessage(ChatColor.GREEN + "=== " + title + " (Page " + page + "/" + totalPages + ") ===");
        int startIndex = (page - 1) * messagesPerPage;
        int endIndex = Math.min(startIndex + messagesPerPage, messages.size());

        for (int i = startIndex; i < endIndex; i++) {
            player.sendMessage(messages.get(i));
        }

        if (page < totalPages) {
            player.sendMessage(ChatColor.YELLOW + "Use '/" + title.toLowerCase() + " " + (page + 1) + "' to view the next page.");
        }
    }

    @SuppressWarnings("unused")
    private int parseIntegerInput(String input, int defaultValue) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @SuppressWarnings("unused")
    private String formatList(List<String> items, String emptyMessage) {
        if (items.isEmpty()) {
            return emptyMessage;
        }
        return String.join(", ", items);
    }

    @SuppressWarnings("unused")
    private boolean hasPermission(Player player, String permission) {
        if (!player.hasPermission(permission)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return false;
        }
        return true;
    }

    @SuppressWarnings("unused")
    private void logCommand(Player player, String command) {
        plugin.getLogger().info(player.getName() + " executed friend command: " + command);
    }

    @SuppressWarnings("unused")
    private void broadcastToFriends(String playerName, String message) {
        List<String> friends = new ArrayList<>(friendManager.getFriends(playerName));
        for (String friend : friends) {
            Player friendPlayer = Bukkit.getPlayer(friend);
            if (friendPlayer != null && friendPlayer.isOnline()) {
                friendPlayer.sendMessage(message);
            }
        }
    }

    @SuppressWarnings("unused")
    private boolean areMutualFriends(String player1, String player2) {
        return friendManager.areFriends(player1, player2) && friendManager.areFriends(player2, player1);
    }

    // Utility method to get a player's friend count
    private int getFriendCount(String playerName) {
        return friendManager.getFriends(playerName).size();
    }

    @SuppressWarnings("unused")
    private int getBlockedCount(String playerName) {
        return friendManager.getBlockedPlayers(playerName).size();
    }

    @SuppressWarnings("unused")
    private boolean hasReachedFriendLimit(String playerName) {
        int maxFriends = plugin.getConfig().getInt("max-friends", 100);
        return getFriendCount(playerName) >= maxFriends;
    }

    @SuppressWarnings("unused")
    private void sendFriendRequestCooldownMessage(Player player, long remainingTime) {
        player.sendMessage(ChatColor.RED + "You can send another friend request in " + formatTimeDifference(remainingTime) + ".");
    }
}