
package org.derjannik.lobbyLynx;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class NavigatorCommand implements CommandExecutor {

    private final LobbyLynx plugin;
    private final ConfigManager configManager;

    public NavigatorCommand(LobbyLynx plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            new NavigatorGUI(plugin).openGUI(player);
            return true;
        }

        // Handle /lynx lobby command
        if (args.length == 1 && args[0].equalsIgnoreCase("lobby")) {
            teleportToLobby(player);
            return true;
        }

        // Handle /lynx reload command
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (player.hasPermission("lynx.reload")) {
                plugin.reloadNavigator();
                player.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully.");
            } else {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            }
            return true;
        }

        // Handle /lynx set command
        if (args.length >= 2 && args[0].equalsIgnoreCase("set")) {
            if (args[1].equalsIgnoreCase("minigame")) {
                return handleMinigameCommand(player, args);
            } else if (args[1].equalsIgnoreCase("lobbyspawn")) {
                return handleLobbySpawnCommand(player, args);
            }
        }

        return false;
    }

    private void teleportToLobby(Player player) {
        double x = configManager.getLobbyX();
        double y = configManager.getLobbyY();
        double z = configManager.getLobbyZ();
        String world = configManager.getLobbyWorld();

        // Start the teleport delay
        new BukkitRunnable() {
            private int countdown = 3; // You might want to add this to config

            @Override
            public void run() {
                if (countdown <= 0) {
                    if (player.isOnline()) {
                        player.teleport(new Location(Bukkit.getWorld(world), x, y, z));
                        if (configManager.isTeleportMessageEnabled()) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', configManager.getTeleportMessage()));
                        }
                    }
                    cancel();
                    return;
                }

                if (!player.isOnline() || hasPlayerMoved(player, x, y, z)) {
                    cancel();
                    return;
                }

                player.sendMessage(ChatColor.YELLOW + "Teleporting in " + countdown + " seconds...");
                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private boolean hasPlayerMoved(Player player, double x, double y, double z) {
        Location playerLoc = player.getLocation();
        return playerLoc.getX() != x || playerLoc.getY() != y || playerLoc.getZ() != z;
    }

    private boolean handleMinigameCommand(Player player, String[] args) {
        if (args.length != 8) {
            player.sendMessage(ChatColor.RED + "Usage: /lynx set minigame <name> <slot> <item> <x> <y> <z> <world>");
            return true;
        }

        String name = args[2];
        int slot;
        try {
            slot = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid slot number.");
            return true;
        }

        Material item;
        try {
            item = Material.valueOf(args[4].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid item material.");
            return true;
        }

        double x, y, z;
        try {
            x = Double.parseDouble(args[5]);
            y = Double.parseDouble(args[6]);
            z = Double.parseDouble(args[7]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid coordinates.");
            return true;
        }

        String world = args[8];

        // Save minigame to config
        configManager.setMinigame(name, slot, item.toString(), x, y, z, world);
        player.sendMessage(ChatColor.GREEN + "Minigame " + name + " has been set.");

        return true;
    }

    private boolean handleLobbySpawnCommand(Player player, String[] args) {
        if (args.length != 7) {
            player.sendMessage(ChatColor.RED + "Usage: /lynx set lobbyspawn <slot> <item> <x> <y> <z> <world>");
            return true;
        }

        int slot;
        try {
            slot = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid slot number.");
            return true;
        }

        Material item;
        try {
            item = Material.valueOf(args[3].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid item material.");
            return true;
        }

        double x, y, z;
        try {
            x = Double.parseDouble(args[4]);
            y = Double.parseDouble(args[5]);
            z = Double.parseDouble(args[6]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid coordinates.");
            return true;
        }

        String world = args[7];

        // Save lobby spawn to config
        configManager.setLobbySpawn(slot, item.toString(), x, y, z, world);
        player.sendMessage(ChatColor.GREEN + "Lobby spawn has been set.");

        return true;
    }
}
