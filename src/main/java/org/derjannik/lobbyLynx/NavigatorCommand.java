package org.derjannik.lobbyLynx;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;

public class NavigatorCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final FileConfiguration config;

    public NavigatorCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();  // Load the configuration
        createConfigFile(); // Ensure the config file exists
    }

    // Ensure the navigator.yml exists
    private void createConfigFile() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            File configFile = new File(plugin.getDataFolder(), "navigator.yml");
            if (!configFile.exists()) {
                plugin.saveResource("navigator.yml", false); // Save default config if it doesn't exist
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create or load navigator.yml: " + e.getMessage());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;

        // Handle /lobby command
        if (args.length == 1 && args[0].equalsIgnoreCase("lobby")) {
            teleportToLobby(player);
            return true;
        }

        // Handle /lynx reload command
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            player.sendMessage("Configuration reloaded successfully.");
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
        double x = plugin.getConfig().getDouble("lobby.x");
        double y = plugin.getConfig().getDouble("lobby.y");
        double z = plugin.getConfig().getDouble("lobby.z");

        // Start the teleport delay
        new BukkitRunnable() {
            private int countdown = plugin.getConfig().getInt("lobby.teleport-delay");

            @Override
            public void run() {
                if (countdown <= 0) {
                    // Check if the player is still online before teleporting
                    if (player.isOnline()) {
                        player.teleport(new Location(Bukkit.getWorld("world"), x, y, z));
                        if (plugin.getConfig().getBoolean("lobby.custom-teleport-message")) {
                            player.sendMessage(plugin.getConfig().getString("lobby.custom-teleport-message"));
                        }
                    }
                    cancel();
                    return;
                }

                if (!player.isOnline() || hasPlayerMoved(player, x, y, z)) {
                    player.sendMessage("Teleport cancelled due to movement.");
                    cancel();
                    return;
                }

                countdown--;
                player.sendMessage("Teleporting to the lobby in " + countdown + " seconds...");
            }
        }.runTaskTimer(plugin, 0, 20); // Run every second
    }

    private boolean hasPlayerMoved(Player player, double x, double y, double z) {
        return player.getLocation().getX() != x || player.getLocation().getY() != y || player.getLocation().getZ() != z;
    }

    private boolean handleMinigameCommand(Player player, String[] args) {
        if (!player.hasPermission("lynx.admin")) {
            player.sendMessage("You do not have permission to use this command.");
            return false;
        }

        if (args.length < 6) {
            player.sendMessage("Usage: /lynx set minigame <minigame_name> <slot> <item_id/name> <x> <y> <z>");
            return false;
        }

        String minigameName = args[2];
        int slot = Integer.parseInt(args[3]);
        Material material = Material.matchMaterial(args[4].toUpperCase());

        if (material == null) {
            player.sendMessage("Invalid item ID or name: " + args[4]);
            return false;
        }

        double x, y, z;
        if (args.length >= 7) {
            x = Double.parseDouble(args[5]);
            y = Double.parseDouble(args[6]);
            z = Double.parseDouble(args[7]);
        } else {
            x = player.getLocation().getX();
            y = player.getLocation().getY();
            z = player.getLocation().getZ();
        }

        // Save the minigame configuration
        config.set("minigames." + minigameName + ".slot", slot);
        config.set("minigames." + minigameName + ".item", material.toString());
        config.set("minigames." + minigameName + ".x", x);
        config.set("minigames." + minigameName + ".y", y);
        config.set("minigames." + minigameName + ".z", z);

        try {
            config.save(new File(plugin.getDataFolder(), "navigator.yml"));
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save navigator.yml: " + e.getMessage());
        }

        player.sendMessage("Minigame configuration saved successfully.");
        return true;
    }

    private boolean handleLobbySpawnCommand(Player player, String[] args) {
        if (!player.hasPermission("lynx.admin")) {
            player.sendMessage("You do not have permission to use this command.");
            return false;
        }

        if (args.length < 4) {
            player.sendMessage("Usage: /lynx set lobbyspawn <x> <y> <z>");
            return false;
        }

        double x = Double.parseDouble(args[2]);
        double y = Double.parseDouble(args[3]);
        double z = Double.parseDouble(args[4]);

        // Save the lobby spawn configuration
        config.set("lobby.x", x);
        config.set("lobby.y", y);
        config.set("lobby.z", z);

        try {
            config.save(new File(plugin.getDataFolder(), "navigator.yml"));
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save navigator.yml: " + e.getMessage());
        }

        player.sendMessage("Lobby spawn configuration saved successfully.");
        return true;
    }
}