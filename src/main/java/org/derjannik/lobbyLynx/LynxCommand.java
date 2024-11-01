package org.derjannik.lobbyLynx;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class LynxCommand implements CommandExecutor, TabCompleter {

    private final LobbyLynx plugin;
    private final ConfigManager configManager;
    private final Map<String, CommandHandler> commandHandlers;

    @FunctionalInterface
    private interface CommandHandler {
        void handle(@NotNull Player player, @NotNull String[] args);
    }

    public LynxCommand(LobbyLynx plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.commandHandlers = initializeCommandHandlers();
    }

    private Map<String, CommandHandler> initializeCommandHandlers() {
        Map<String, CommandHandler> handlers = new HashMap<>();
        handlers.put("navigator", this::handleNavigatorCommand);
        handlers.put("settings", this::handleSettingsCommand);
        handlers.put("gamerules", this::handleGamerulesCommand);
        handlers.put("set", this::handleSetCommand);
        handlers.put("reload", this::handleReloadCommand);
        handlers.put("serversign", this::handleServerSignCommand);
        handlers.put("help", (player, args) -> sendHelpMessage(player));
        return handlers;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        CommandHandler handler = commandHandlers.get(subCommand);

        if (handler == null) {
            player.sendMessage(ChatColor.RED + "Unknown command. Type /lynx help for a list of commands.");
            return true;
        }

        try {
            handler.handle(player, args);
        } catch (NumberFormatException e) {
            handleCommandError(player, "Invalid number format in command", e);
        } catch (IllegalArgumentException e) {
            handleCommandError(player, "Invalid argument: " + e.getMessage(), e);
        } catch (Exception e) {
            handleCommandError(player, "An unexpected error occurred", e);
        }

        return true;
    }

    private void handleCommandError(@NotNull Player player, String message, Exception e) {
        player.sendMessage(ChatColor.RED + message);
        plugin.getLogger().log(Level.SEVERE, message, e);
    }

    private void handleNavigatorCommand(@NotNull Player player, @NotNull String[] args) {
        if (!player.hasPermission("lynx.navigator")) {
            sendNoPermissionMessage(player);
            return;
        }
        new NavigatorGUI(plugin, configManager).openNavigatorGUI(player);
    }

    private void handleSettingsCommand(@NotNull Player player, @NotNull String[] args) {
        if (!player.hasPermission("lynx.settings")) {
            sendNoPermissionMessage(player);
            return;
        }
        new SettingsGUI(plugin, configManager).openSettingsGUI(player);
    }

    private void handleGamerulesCommand(@NotNull Player player, @NotNull String[] args) {
        if (!player.hasPermission("lynx.gamerules")) {
            sendNoPermissionMessage(player);
            return;
        }
        new GameruleGUI(plugin, configManager).openGameruleGUI(player, 0);
    }

    private void handleSetCommand(@NotNull Player player, @NotNull String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /lynx set <minigame|lobbyspawn>");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "minigame":
                // Add code to handle setting minigame
                player.sendMessage(ChatColor.GREEN + "Minigame set.");
                break;
            case "lobbyspawn":
                // Add code to handle setting lobby spawn
                player.sendMessage(ChatColor.GREEN + "Lobby spawn set.");
                break;
            default:
                player.sendMessage(ChatColor.RED + "Invalid option for /lynx set. Use /lynx help for more info.");
        }
    }

    private void handleReloadCommand(@NotNull Player player, @NotNull String[] args) {
        if (!player.hasPermission("lynx.reload")) {
            sendNoPermissionMessage(player);
            return;
        }
        plugin.reloadNavigator();
        player.sendMessage(ChatColor.GREEN + "LobbyLynx configuration reloaded.");
    }

    private void handleServerSignCommand(@NotNull Player player, @NotNull String[] args) {
        if (!player.hasPermission("lynx.serversigns.admin")) {
            sendNoPermissionMessage(player);
            return;
        }
        // Implement handling for server sign commands here
        player.sendMessage(ChatColor.GREEN + "Server sign command handled.");
    }

    private void sendHelpMessage(@NotNull Player player) {
        player.sendMessage(ChatColor.GOLD + "=== LobbyLynx Commands ===");
        sendConditionalHelpMessage(player, "lynx.navigator", "/lynx navigator", "Open navigator GUI");
        sendConditionalHelpMessage(player, "lynx.settings", "/lynx settings", "Open settings GUI");
        sendConditionalHelpMessage(player, "lynx.gamerules", "/lynx gamerules", "Open gamerules GUI");
        sendConditionalHelpMessage(player, "lynx.setminigame", "/lynx set minigame", "Set a minigame");
        sendConditionalHelpMessage(player, "lynx.setlobbyspawn", "/lynx set lobbyspawn", "Set lobby spawn location");
        sendConditionalHelpMessage(player, "lynx.reload", "/lynx reload", "Reload LobbyLynx configuration");
        sendConditionalHelpMessage(player, "lynx.serversigns.admin", "/lynx serversign <create|remove> [server]", "Manage server signs");
    }

    private void sendConditionalHelpMessage(@NotNull Player player, String permission, String command, String description) {
        if (player.hasPermission(permission)) {
            player.sendMessage(ChatColor.YELLOW + command + " - " + description);
        }
    }

    private void sendNoPermissionMessage(@NotNull Player player) {
        player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(commandHandlers.keySet());
        } else if (args.length == 2 && "set".equalsIgnoreCase(args[0])) {
            completions.addAll(Arrays.asList("minigame", "lobbyspawn"));
        } else if (args.length == 2 && "serversign".equalsIgnoreCase(args[0])) {
            completions.addAll(Arrays.asList("create", "remove"));
        }

        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
