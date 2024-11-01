
package org.derjannik.lobbyLynx;

import org.bukkit.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class LynxCommand implements CommandExecutor, TabCompleter {

    private final LobbyLynx plugin;
    private final ConfigManager configManager;

    public LynxCommand(LobbyLynx plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "navigator":
                if (player.hasPermission("lynx.navigator")) {
                    new NavigatorGUI(plugin, configManager).openGUI(player);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                }
                break;
            case "settings":
                if (player.hasPermission("lynx.settings")) {
                    new SettingsGUI(plugin, configManager).openSettingsGUI(player);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                }
                break;
            case "gamerules":
                if (player.hasPermission("lynx.gamerules")) {
                    new GameruleGUI(plugin, configManager).openGameruleGUI(player, 0);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                }
                break;
            case "set":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /lynx set <minigame|lobbyspawn>");
                    return true;
                }
                handleSetCommand(player, args);
                break;
            case "reload":
                if (player.hasPermission("lynx.reload")) {
                    plugin.reloadNavigator();
                    player.sendMessage(ChatColor.GREEN + "LobbyLynx configuration reloaded.");
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                }
                break;
            case "bungeesign":
                if (player.hasPermission("lynx.bungeesigns.admin")) {
                    handleBungeeSignCommand(player, args);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                }
                break;
            case "help":
                sendHelpMessage(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown command. Type /lynx help for a list of commands.");
                break;
        }

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== LobbyLynx Commands ===");
        if (player.hasPermission("lynx.navigator")) {
            player.sendMessage(ChatColor.YELLOW + "/lynx navigator " + ChatColor.WHITE + "- Open navigator GUI");
        }
        if (player.hasPermission("lynx.settings")) {
            player.sendMessage(ChatColor.YELLOW + "/lynx settings " + ChatColor.WHITE + "- Open settings GUI");
        }
        if (player.hasPermission("lynx.gamerules")) {
            player.sendMessage(ChatColor.YELLOW + "/lynx gamerules " + ChatColor.WHITE + "- Open gamerules GUI");
        }
        if (player.hasPermission("lynx.setminigame")) {
            player.sendMessage(ChatColor.YELLOW + "/lynx set minigame <name> <slot> <item> [x] [y] [z] [world]");
        }
        if (player.hasPermission("lynx.setlobbyspawn")) {
            player.sendMessage(ChatColor.YELLOW + "/lynx set lobbyspawn " + ChatColor.WHITE + "- Set lobby spawn location");
        }
        if (player.hasPermission("lynx.reload")) {
            player.sendMessage(ChatColor.YELLOW + "/lynx reload " + ChatColor.WHITE + "- Reload LobbyLynx configuration");
        }
        if (player.hasPermission("lynx.bungeesigns.admin")) {
            player.sendMessage(ChatColor.YELLOW + "/lynx bungeesign <server> " + ChatColor.WHITE + "- Links a sign to a BungeeCord server");
            player.sendMessage(ChatColor.YELLOW + "/lynx bungeesign info " + ChatColor.WHITE + "- Displays info about the server linked to the sign");
            player.sendMessage(ChatColor.YELLOW + "/lynx create bungeesign " + ChatColor.WHITE + "- Starts sign creation process");
        }
    }

    private void handleBungeeSignCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /lynx bungeesign <server|info|create>");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "create":
                plugin.getBungeeSignManager().startSignCreation(player);
                break;
            case "info":
                plugin.getBungeeSignManager().displaySignInfo(player);
                break;
            default:
                plugin.getBungeeSignManager().linkSignToServer(player, args[1]);
                break;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Example implementation
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("navigator");
            completions.add("settings");
            completions.add("gamerules");
            completions.add("set");
            completions.add("reload");
            completions.add("bungeesign");
            completions.add("help");
        }
        return completions;
    }
    private void handleSetCommand(Player player, String[] args) {
        // Example logic for handling "set" subcommands
        if ("minigame".equalsIgnoreCase(args[1])) {
            player.sendMessage("Minigame setting logic goes here.");
        } else if ("lobbyspawn".equalsIgnoreCase(args[1])) {
            player.sendMessage("Lobby spawn setting logic goes here.");
        } else {
            player.sendMessage(ChatColor.RED + "Invalid option for /lynx set. Use /lynx help for more info.");
        }
    }

}
