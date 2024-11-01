package org.derjannik.lobbyLynx.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.derjannik.lobbyLynx.FriendManager;
import org.derjannik.lobbyLynx.LobbyLynx;

import java.util.List;

public class FriendCommand implements CommandExecutor {
    private final LobbyLynx plugin;
    private final FriendManager friendManager;

    public FriendCommand(LobbyLynx plugin, FriendManager friendManager) {
        this.plugin = plugin;
        this.friendManager = friendManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lynx.friend.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use friend commands!");
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                listFriends(player);
                break;
            case "add": {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /friend add <player>");
                    return true;
                }
                String target = args[1];
                Player targetPlayer = Bukkit.getPlayer(target);
                if (targetPlayer == null) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }
                friendManager.sendFriendRequest(player.getName(), target);
                break;
            }
            case "remove": {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /friend remove <player>");
                    return true;
                }
                String target = args[1];
                friendManager.removeFriend(player.getName(), target);
                break;
            }
            case "requests": {
                listRequests(player);
                break;
            }
            case "accept": {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /friend accept <player>");
                    return true;
                }
                String requester = args[1];
                friendManager.acceptRequest(player.getName(), requester);
                break;
            }
            default:
                sendHelpMessage(player);
                break;
        }

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GREEN + "Friend commands:");
        player.sendMessage(ChatColor.GREEN + "/friend list - Lists all friends.");
        player.sendMessage(ChatColor.GREEN + "/friend add <player> - Sends a friend request.");
        player.sendMessage(ChatColor.GREEN + "/friend remove <player> - Removes a friend.");
        player.sendMessage(ChatColor.GREEN + "/friend requests - Lists pending friend requests.");
        player.sendMessage(ChatColor.GREEN + "/friend accept <player> - Accepts a friend request.");
    }

    private void listFriends(Player player) {
        List<String> friends = friendManager.getFriends(player.getName());
        if (friends.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You have no friends!");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Friends:");
        for (String friend : friends) {
            player.sendMessage(ChatColor.GREEN + friend);
        }
    }

    private void listRequests(Player player) {
        List<String> requests = friendManager.getRequests(player.getName());
        if (requests.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You have no pending friend requests!");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Pending friend requests:");
        for (String requester : requests) {
            player.sendMessage(ChatColor.GREEN + requester);
        }
    }
}