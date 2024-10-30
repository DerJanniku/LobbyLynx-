package org.derjannik.lobbyLynx;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LynxCommand implements CommandExecutor, TabCompleter {

    private final LobbyLynx plugin;

    public LynxCommand(LobbyLynx plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendDetailedHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "settings":
                handleSettingsCommand(player);
                break;
            case "set":
                handleSetCommand(player, args);
                break;
            case "reload":
                handleReloadCommand(player);
                break;
            case "help":
                sendDetailedHelpMessage(player);
                break;
            default:
                sendDetailedHelpMessage(player);
                break;
        }

        return true;
    }

    private void handleSettingsCommand(Player player) {
        if (player.hasPermission("lynx.admin")) {
            new SettingsGUI(plugin).openSettingsGUI(player);
        } else {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
        }
    }

    private void handleSetCommand(Player player, String[] args) {
        if (!player.hasPermission("lynx.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /lynx set <minigame|lobbyspawn> ...");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "minigame":
                handleSetMinigame(player, args);
                break;
            case "lobbyspawn":
                handleSetLobbySpawn(player, args);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown set command. Use 'minigame' or 'lobbyspawn'.");
                break;
        }
    }

    private void handleSetMinigame(Player player, String[] args) {
        if (args.length < 7) {
            player.sendMessage(ChatColor.RED + "Usage: /lynx set minigame <name> <slot> <item> <x> <y> <z>");
            return;
        }

        String name = args[2];
        int slot;
        Material item;
        double x, y, z;

        try {
            slot = Integer.parseInt(args[3]);
            item = Material.valueOf(args[4].toUpperCase());
            x = Double.parseDouble(args[5]);
            y = Double.parseDouble(args[6]);
            z = Double.parseDouble(args[7]);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid arguments. Please check your input.");
            return;
        }

        if (validateSlot(slot) && validateCoordinates(x, y, z)) {
            Location loc = new Location(player.getWorld(), x, y, z);

            ConfigurationSection minigames = plugin.getConfig().getConfigurationSection("minigames");
            if (minigames == null) {
                minigames = plugin.getConfig().createSection("minigames");
            }

            ConfigurationSection minigame = minigames.createSection(name);
            minigame.set("slot", slot);
            minigame.set("item", item.name());
            minigame.set("world", loc.getWorld() != null ? loc.getWorld().getName() : "world");
            minigame.set("x", loc.getX());
            minigame.set("y", loc.getY());
            minigame.set("z", loc.getZ());

            plugin.saveConfig();
            player.sendMessage(ChatColor.GREEN + "Minigame '" + name + "' has been set successfully.");
        } else {
            player.sendMessage(ChatColor.RED + "Invalid slot or coordinates. Please check your input.");
        }
    }

    private void handleSetLobbySpawn(Player player, String[] args) {
        if (args.length < 7) {
            player.sendMessage(ChatColor.RED + "Usage: /lynx set lobbyspawn <slot> <item> <x> <y> <z>");
            return;
        }

        int slot;
        Material item;
        double x, y, z;

        try {
            slot = Integer.parseInt(args[2]);
            item = Material.valueOf(args[3].toUpperCase());
            x = Double.parseDouble(args[4]);
            y = Double.parseDouble(args[5]);
            z = Double.parseDouble(args[6]);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid arguments. Please check your input.");
            return;
        }

        if (validateSlot(slot) && validateCoordinates(x, y, z)) {
            Location loc = new Location(player.getWorld(), x, y, z);

            ConfigurationSection lobbySpawn = plugin.getConfig().getConfigurationSection("lobbySpawn");
            if (lobbySpawn == null) {
                lobbySpawn = plugin.getConfig().createSection("lobbySpawn");
            }

            lobbySpawn.set("slot", slot);
            lobbySpawn.set("item", item.name());
            lobbySpawn.set("world", loc.getWorld() != null ? loc.getWorld().getName() : "world");
            lobbySpawn.set("x", loc.getX());
            lobbySpawn.set("y", loc.getY());
            lobbySpawn.set("z", loc.getZ());

            plugin.saveConfig();
            player.sendMessage(ChatColor.GREEN + "Lobby spawn has been set successfully.");
        } else {
            player.sendMessage(ChatColor.RED + "Invalid slot or coordinates. Please check your input.");
        }
    }

    private void handleReloadCommand(Player player) {
        if (!player.hasPermission("lynx.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return;
        }

        try {
            plugin.reloadConfig();
            plugin.reloadNavigator(); // Make sure to implement this in your main class
            player.sendMessage(ChatColor.GREEN + "LobbyLynx configuration reloaded successfully.");
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error reloading configuration: " + e.getMessage());
            plugin.getLogger().severe("Error reloading configuration: " + e.getMessage());
        }
    }

    private void sendDetailedHelpMessage(Player player) {
        List<String> helpMessages = Arrays.asList(
                ChatColor.GOLD + "=== LobbyLynx Help ===",
                ChatColor.YELLOW + "/lynx settings " + ChatColor.WHITE + "- Open the admin settings GUI",
                ChatColor.YELLOW + "/lynx set minigame <name> <slot> <item> <x> <y> <z>",
                ChatColor.WHITE + "  - Set a minigame location with custom icon",
                ChatColor.YELLOW + "/lynx set lobbyspawn <slot> <item> <x> y> <z>",
                ChatColor.WHITE + "  - Set the main lobby spawn point",
                ChatColor.YELLOW + "/lynx reload " + ChatColor.WHITE + "- Reload the plugin configuration",
                ChatColor.YELLOW + "/lynx help " + ChatColor.WHITE + "- Show this help message",
                "",
                ChatColor .GOLD + "Examples:",
                ChatColor.WHITE + "/lynx set minigame Survival 0 DIAMOND_SWORD 100 64 100",
                ChatColor.WHITE + "/lynx set lobbyspawn 4 NETHER_STAR 0 64 0"
        );

        for (String message : helpMessages) {
            player.sendMessage(message);
        }
    }

    private boolean validateSlot(int slot) {
        return slot >= 0 && slot < plugin.getConfig().getInt("navigator.gui.size", 36);
    }

    private boolean validateMaterial(String materialName) {
        try {
            Material.valueOf(materialName.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean validateCoordinates(double x, double y, double z) {
        return !Double.isInfinite(x) && !Double.isInfinite(y) && !Double.isInfinite(z) &&
                !Double.isNaN(x) && !Double.isNaN(y) && !Double.isNaN(z);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("lynx.admin")) {
            return completions;
        }

        switch (args.length) {
            case 1:
                completions.addAll(Arrays.asList("settings", "set", "reload", "help"));
                break;
            case 2:
                if (args[0].equalsIgnoreCase("set")) {
                    completions.addAll(Arrays.asList("minigame", "lobbyspawn"));
                }
                break;
            case 3:
                if (args[0].equalsIgnoreCase("set")) {
                    if (args[1].equalsIgnoreCase("minigame")) {
                        // Suggest existing minigame names or "<name>"
                        completions.add("<name>");
                        ConfigurationSection minigames = plugin.getConfig().getConfigurationSection("minigames");
                        if (minigames != null) {
                            completions.addAll(minigames.getKeys(false));
                        }
                    }
                }
                break;
            case 4:
                if (args[0].equalsIgnoreCase("set")) {
                    // Suggest slot numbers
                    completions.addAll(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8"));
                }
                break;
            case 5:
                if (args[0].equalsIgnoreCase("set")) {
                    // Suggest common materials
                    completions.addAll(Arrays.asList(
                            "DIAMOND_SWORD",
                            "GOLDEN_SWORD",
                            "BOW",
                            "COMPASS",
                            "NETHER_STAR",
                            "EMERALD",
                            "DIAMOND"
                    ));
                }
                break;
            case 6:
            case 7:
            case 8:
                if (args[0].equalsIgnoreCase("set")) {
                    Player player = (sender instanceof Player) ? (Player) sender : null;
                    if (player != null) {
                        // Suggest current coordinates
                        Location loc = player.getLocation();
                        completions.add(String.valueOf(Math.round(loc.getX())));
                        completions.add(String.valueOf(Math.round(loc.getY())));
                        completions.add(String.valueOf(Math.round(loc.getZ())));
                    }
                }
                break;
        }

        return filterCompletions(completions, args[args.length - 1]);
    }

    private List<String> filterCompletions(List<String> completions, String partial) {
        List<String> filtered = new ArrayList<>();
        for (String completion : completions) {
            if (completion.toLowerCase().startsWith(partial.toLowerCase())) {
                filtered.add(completion);
            }
        }
        return filtered;
    }
}