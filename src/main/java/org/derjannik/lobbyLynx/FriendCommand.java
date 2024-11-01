package org.derjannik.lobbyLynx.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.derjannik.lobbyLynx.FriendManager;
import org.derjannik.lobbyLynx.LobbyLynx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FriendCommand implements CommandExecutor, TabCompleter {
    private final LobbyLynx plugin;
    private final FriendManager friendManager;

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

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "list":
                    listFriends(player);
                    break;
                case "add":
                    handleAddFriend(player, args);
                    break;
                case "remove":
                    handleRemoveFriend(player, args);
                    break;
                case "requests":
                    listRequests(player);
                    break;
                case "accept":
                    handleAcceptRequest(player, args);
                    break;
                case "deny":
                    handleDenyRequest(player, args);
                    break;
                case "help":
                    sendHelpMessage(player);
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

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Friend System Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/friend list" + ChatColor.WHITE + " - List all your friends");
        player.sendMessage(ChatColor.YELLOW + "/friend add <player>" + ChatColor.WHITE + " - Send a friend request");
        player.sendMessage(ChatColor.YELLOW + "/friend remove <player>" + ChatColor.WHITE + " - Remove a friend");
        player.sendMessage(ChatColor.YELLOW + "/friend requests" + ChatColor.WHITE + " - List pending friend requests");
        player.sendMessage(ChatColor.YELLOW + "/friend accept <player>" + ChatColor.WHITE + " - Accept a friend request");
        player.sendMessage(ChatColor.YELLOW + "/friend deny <player>" + ChatColor.WHITE + " - Deny a friend request");
    }

    private void listFriends(Player player) {
        List<String> friends = friendManager.getFriends(player.getName());
        if (friends.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "You don't have any friends yet.");
        } else {
            player.sendMessage(ChatColor.GREEN + "Your friends:");
            for (String friend : friends) {
                player.sendMessage(ChatColor.YELLOW + "- " + friend);
            }
        }
    }

    private void handleAddFriend(Player player, String[] args) {
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

    private void handleRemoveFriend(Player player, String[] args) {
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
        player.sendMessage(ChatColor.YELLOW + "You have removed " + targetName + " from your friend list.");
    }

    private void listRequests(Player player) {
        List<String> requests = friendManager.getRequests(player.getName());
        if (requests.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "You don't have any pending friend requests.");
        } else {
            player.sendMessage(ChatColor.GREEN + "Pending friend requests:");
            for (String requester : requests) {
                player.sendMessage(ChatColor.YELLOW + "- " + requester);
            }
        }
    }

    private void handleAcceptRequest(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /friend accept <player>");
            return;
        }
        String requesterName = args[1];
        List<String> requests = friendManager.getRequests(player.getName());
        if (!requests.contains(requesterName)) {
            player.sendMessage(ChatColor.RED + "You don't have a friend request from " + requesterName);
            return;
        }
        friendManager.acceptRequest(player.getName(), requesterName);
        player.sendMessage(ChatColor.GREEN + "You are now friends with " + requesterName);
    }

    private void handleDenyRequest(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /friend deny <player>");
            return;
        }
        String requesterName = args[1];
        List<String> requests = friendManager.getRequests(player.getName());
        if (!requests.contains(requesterName)) {
            player.sendMessage(ChatColor.RED + "You don't have a friend request from " + requesterName);
            return;
        }
        friendManager.denyRequest(player.getName(), requesterName);
        player.sendMessage(ChatColor.YELLOW + "You have denied the friend request from " + requesterName);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("list", "add", "remove", "requests", "accept", "deny", "help"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "add":
                    // Show online players that aren't already friends
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        List<String> friends = friendManager.getFriends(player.getName());
                        completions.addAll(
                                Bukkit.getOnlinePlayers().stream()
                                        .map(Player::getName)
                                        .filter(name -> !friends.contains(name) && !name.equals(player.getName()))
                                        .collect(Collectors.toList())
                        );
                    }
                    break;
                case "remove":
                    // Show only current friends
                    if (sender instanceof Player) {
                        completions.addAll(friendManager.getFriends(((Player) sender).getName()));
                    }
                    break;
                case "accept":
                case "deny":
                    // Show only pending friend requests
                    if (sender instanceof Player) {
                        completions.addAll(friendManager.getRequests(((Player) sender).getName()));
                    }
                    break;
            }
        }

        // Filter based on current input
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}