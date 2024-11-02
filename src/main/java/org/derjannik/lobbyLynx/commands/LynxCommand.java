package org.derjannik.lobbyLynx.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.derjannik.lobbyLynx.LobbyLynx;
import org.derjannik.lobbyLynx.gui.NavigatorGUI;
import org.derjannik.lobbyLynx.gui.SettingsGUI;
import org.derjannik.lobbyLynx.gui.GameruleGUI;
import org.derjannik.lobbyLynx.managers.ConfigManager;
import org.derjannik.lobbyLynx.managers.FriendManager;
import org.derjannik.lobbyLynx.enums.PrivacyLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LynxCommand implements CommandExecutor, TabCompleter {

    private final LobbyLynx plugin;
    private final ConfigManager configManager;
    private final FriendManager friendManager;

    public LynxCommand(LobbyLynx plugin, ConfigManager configManager, FriendManager friendManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.friendManager = friendManager;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "navigator":
                    handleNavigatorCommand(player);
                    break;
                case "settings":
                    handleSettingsCommand(player);
                    break;
                case "gamerules":
                    handleGamerulesCommand(player);
                    break;
                case "set":
                    handleSetCommand(player, args);
                    break;
                case "reload":
                    handleReloadCommand(player);
                    break;
                case "serversign":
                    handleServerSignCommand(player, args);
                    break;
                case "friends":
                    handleFriendsCommand(player, args);
                    break;
                case "privacy":
                    handlePrivacyCommand(player, args);
                    break;
                case "help":
                    sendHelpMessage(player);
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Unknown command. Type /lynx help for a list of commands.");
                    break;
            }
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "An error occurred while executing the command: " + e.getMessage());
            plugin.getLogger().severe("Error executing LynxCommand: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }


    private void handleNavigatorCommand(Player player) {
        if (player.hasPermission("lynx.navigator")) {
            new NavigatorGUI(plugin, configManager).openGUI(player);
        } else {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
        }
    }

    private void handleSettingsCommand(Player player) {
        if (player.hasPermission("lynx.settings")) {
            new SettingsGUI(plugin, configManager).openSettingsGUI(player);
        } else {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
        }
    }

    private void handleGamerulesCommand(Player player) {
        if (player.hasPermission("lynx.gamerules")) {
            new GameruleGUI(plugin, configManager).openGameruleGUI(player, 0);
        } else {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
        }
    }

    private void handleSetCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /lynx set <minigame|lobbyspawn>");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "minigame":
                if (player.hasPermission("lynx.setminigame")) {
                    handleSetMinigame(player, args);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to set minigames.");
                }
                break;
            case "lobbyspawn":
                if (player.hasPermission("lynx.setlobbyspawn")) {
                    handleSetLobbySpawn(player);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to set lobby spawn.");
                }
                break;
            default:
                player.sendMessage(ChatColor.RED + "Invalid option for /lynx set. Use /lynx help for more info.");
        }
    }

    private void handleSetMinigame(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(ChatColor.RED + "Usage: /lynx set minigame <name> <slot> <item> [x] [y] [z] [world]");
            return;
        }

        String name = args[2];
        int slot = Integer.parseInt(args[3]);
        String item = args[4];
        double x = args.length > 5 ? Double.parseDouble(args[5]) : player.getLocation().getX();
        double y = args.length > 6 ? Double.parseDouble(args[6]) : player.getLocation().getY();
        double z = args.length > 7 ? Double.parseDouble(args[7]) : player.getLocation().getZ();
        String world = args.length > 8 ? args[8] : player.getWorld().getName();

        configManager.setMinigame(name, slot, item, x, y, z, world);
        player.sendMessage(ChatColor.GREEN + "Minigame " + name + " has been set.");
    }

    private void handleSetLobbySpawn(Player player) {
        configManager.setLobbySpawn(player.getLocation());
        player.sendMessage(ChatColor.GREEN + "Lobby spawn has been set to your current location.");
    }

    private void handleReloadCommand(Player player) {
        if (player.hasPermission("lynx.reload")) {
            plugin.reloadNavigator();
            player.sendMessage(ChatColor.GREEN + "LobbyLynx configuration reloaded.");
        } else {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
        }
    }

    private void handleServerSignCommand(Player player, String[] args) {
        if (!player.hasPermission("lynx.serversigns.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /lynx serversign <create|remove> [server]");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "create":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /lynx serversign create <server>");
                    return;
                }
                plugin.getServerSignManager().createSign(player, args[2]);
                break;
            case "remove":
                plugin.getServerSignManager().removeSign(player, player.getTargetBlock(null, 5).getLocation());
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use 'create' or 'remove'.");
        }
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== LobbyLynx Commands ===");
        sendConditionalHelpMessage(player, "lynx.navigator", "/lynx navigator", "Open navigator GUI");
        sendConditionalHelpMessage(player, "lynx.settings", "/lynx settings", "Open settings GUI");
        sendConditionalHelpMessage(player, "lynx.gamerules", "/lynx gamerules", "Open gamerules GUI");
        sendConditionalHelpMessage(player, "lynx.setminigame", "/lynx set minigame <name> <slot> <item> [x] [y] [z] [world]", "Set a minigame");
        sendConditionalHelpMessage(player, "lynx.setlobbyspawn", "/lynx set lobbyspawn", "Set lobby spawn location");
        sendConditionalHelpMessage(player, "lynx.reload", "/lynx reload", "Reload LobbyLynx configuration");
        sendConditionalHelpMessage(player, "lynx.serversigns.admin", "/lynx serversign <create|remove> [server]", "Manage server signs");
        sendConditionalHelpMessage(player, "lyn x.friends", "/lynx friends <list|add|remove|accept|deny>", "Manage friends");
        sendConditionalHelpMessage(player, "lynx.privacy", "/lynx privacy <public|friends|private>", "Manage privacy settings");
    }

    private void sendConditionalHelpMessage(Player player, String permission, String command, String description) {
        if (player.hasPermission(permission)) {
            player.sendMessage(ChatColor.YELLOW + command + ChatColor.WHITE + " - " + description);
        }
    }

    private void handleFriendsCommand(Player player, String[] args) {
        if (!player.hasPermission("lynx.friends")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use friend commands.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /lynx friends <list|add|remove|accept|deny>");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "list":
                List<String> friends = friendManager.getFriends(player.getName());
                if (friends.isEmpty()) {
                    player.sendMessage(ChatColor.YELLOW + "You don't have any friends yet.");
                    return;
                }
                player.sendMessage(ChatColor.GOLD + "=== Your Friends ===");
                for (String friend : friends) {
                    boolean isOnline = plugin.getServer().getPlayer(friend) != null;
                    player.sendMessage(ChatColor.YELLOW + friend +
                            (isOnline ? ChatColor.GREEN + " (Online)" : ChatColor.RED + " (Offline)"));
                }
                break;
            case "add":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /lynx friends add <player>");
                    return;
                }
                String targetName = args[2];
                if (targetName.equals(player.getName())) {
                    player.sendMessage(ChatColor.RED + "You cannot add yourself as a friend.");
                    return;
                }
                friendManager.sendFriendRequest(player.getName(), targetName);
                player.sendMessage(ChatColor.GREEN + "Friend request sent to " + targetName);
                break;
            case "remove":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /lynx friends remove <player>");
                    return;
                }
                friendManager.removeFriend(player.getName(), args[2]);
                player.sendMessage(ChatColor.GREEN + "Removed " + args[2] + " from your friends list.");
                break;
            case "accept":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /lynx friends accept <player>");
                    return;
                }
                friendManager.acceptRequest(player.getName(), args[2]);
                player.sendMessage(ChatColor.GREEN + "Accepted friend request from " + args[2]);
                break;
            case "deny":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /lynx friends deny <player>");
                    return;
                }
                friendManager.denyRequest(player.getName(), args[2]);
                player.sendMessage(ChatColor.GREEN + "Denied friend request from " + args[2]);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown friends command. Available commands: list, add, remove, accept, deny");
                break;
        }
    }

    private void handlePrivacyCommand(Player player, String[] args) {
        if (!player.hasPermission("lynx.privacy")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to change privacy settings.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /lynx privacy <public|private|friends>");
            return;
        }

        try {
            PrivacyLevel level = PrivacyLevel.valueOf(args[1].toUpperCase());
            friendManager.setPrivacyLevel(player.getName(), level);
            player.sendMessage(ChatColor.GREEN + "Privacy level set to " + level.toString().toLowerCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid privacy level. Use: public, private, or friends");
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList(
                    "navigator", "settings", "gamerules", "set", "reload",
                    "serversign", "help", "friends", "privacy"
            ));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "set":
                    completions.addAll(Arrays.asList("minigame", "lobbyspawn"));
                    break;
                case "serversign":
                    completions.addAll(Arrays.asList("create", "remove"));
                    break;
                case "friends":
                    completions.addAll(Arrays.asList("list", "add", "remove", "accept", "deny"));
                    break;
                case "privacy":
                    completions.addAll(Arrays.asList("public", "friends", "private"));
                    break;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("friends")) {
                if (Arrays.asList("add", "remove", "accept", "deny").contains(args[1].toLowerCase())) {
                    return null; // Return null to show online players
                }
            }
        }

        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}