package org.derjannik.lobbyLynx.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.derjannik.lobbyLynx.LobbyLynx;
import org.derjannik.lobbyLynx.enums.AdType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdvertisementCommand implements CommandExecutor, TabCompleter {
    private final LobbyLynx plugin;

    public AdvertisementCommand(LobbyLynx plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lobbylynx.ads.admin")) {
            sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung für diesen Befehl.");
            return true;
        }

        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                handleCreate(sender, args);
                break;
            case "remove":
                handleRemove(sender, args);
                break;
            case "list":
                handleList(sender);
                break;
            case "show":
                handleShow(sender, args);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage(ChatColor.RED + "Verwendung: /ad create <id> <type> <duration> <title> <content...>");
            return;
        }

        String id = args[1];
        AdType type;
        try {
            type = AdType.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Ungültiger Werbetyp! Verfügbar: " +
                    Arrays.toString(AdType.values()));
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Ungültige Dauer! Bitte gib eine Zahl ein.");
            return;
        }

        String title = args[4];
        List<String> content = new ArrayList<>();
        if (args.length > 5) {
            content = Arrays.asList(Arrays.copyOfRange(args, 5, args.length));
        }

        plugin.getAdvertisementManager().createAdvertisement(id, title, content, type, duration, "");
        sender.sendMessage(ChatColor.GREEN + "Werbung wurde erstellt!");
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Verwendung: /ad remove <id>");
            return;
        }

        plugin.getAdvertisementManager().removeAdvertisement(args[1]);
        sender.sendMessage(ChatColor.GREEN + "Werbung wurde entfernt!");
    }

    private void handleList(CommandSender sender) {
        // Implementiere Auflistung aller Werbungen
        sender.sendMessage(ChatColor.GOLD + "=== Verfügbare Werbungen ===");
        // Liste alle Werbungen auf
    }

    private void handleShow(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dieser Befehl kann nur von Spielern ausgeführt werden!");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Verwendung: /ad show <id>");
            return;
        }

        // Get the advertisement ID from args[1]
        String adId = args[1];
        plugin.getAdvertisementManager().showAdvertisement((Player) sender, adId);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== LobbyLynx Werbung ===");
        sender.sendMessage(ChatColor.YELLOW + "/ad create <id> <type> <duration> <title> <content...> - Erstelle eine Werbung");
        sender.sendMessage(ChatColor.YELLOW + "/ad remove <id> - Entferne eine Werbung");
        sender.sendMessage(ChatColor.YELLOW + "/ad list - Liste alle Werbungen auf");
        sender.sendMessage(ChatColor.YELLOW + "/ad show <id> - Zeige eine spezifische Werbung");
    }

    // Fortsetzung des TabCompleters in AdvertisementCommand.java
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "remove", "list", "show"));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            // Füge alle verfügbaren AdTypes hinzu
            completions.addAll(Arrays.stream(AdType.values())
                    .map(AdType::name)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList()));
        }

        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}