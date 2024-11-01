package org.derjannik.lobbyLynx;

import org.bukkit.*;
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
            new NavigatorGUI(plugin, configManager).openGUI(player);
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
        Location lobbyLocation = configManager.getLobbySpawn();

        new BukkitRunnable() {
            private int countdown = 3;

            @Override
            public void run() {
                if (countdown <= 0) {
                    if (player.isOnline()) {
                        player.teleport(lobbyLocation);
                        if (configManager.isTeleportMessageEnabled()) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    configManager.getTeleportMessage()));
                        }
                    }
                    cancel();
                    return;
                }

                if (!player.isOnline() || hasPlayerMoved(player, lobbyLocation)) {
                    cancel();
                    return;
                }

                player.sendMessage(ChatColor.YELLOW + "Teleporting in " + countdown + " seconds...");
                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private boolean hasPlayerMoved(Player player, Location targetLocation) {
        Location playerLoc = player.getLocation();
        return playerLoc.getX() != targetLocation.getX() ||
                playerLoc.getY() != targetLocation.getY() ||
                playerLoc.getZ() != targetLocation.getZ();
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

        Location location = new Location(player.getWorld(), x, y, z);
        configManager.setMinigame(name, slot, item.toString(), location);
        player.sendMessage(ChatColor.GREEN + "Minigame " + name + " has been set.");

        return true;
    }

    private boolean handleLobbySpawnCommand(Player player, String[] args) {
        if (args.length != 6) {
            player.sendMessage(ChatColor.RED + "Usage: /lynx set lobbyspawn <x> <y> <z> <world>");
            return true;
        }

        double x, y, z;
        try {
            x = Double.parseDouble(args[2]);
            y = Double.parseDouble(args[3]);
            z = Double.parseDouble(args[4]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid coordinates.");
            return true;
        }

        String worldName = args[5];
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Invalid world name.");
            return true;
        }

        Location location = new Location(world, x, y, z);
        configManager.setLobbySpawn(location);
        player.sendMessage(ChatColor.GREEN + "Lobby spawn has been set.");

        return true;
    }
}