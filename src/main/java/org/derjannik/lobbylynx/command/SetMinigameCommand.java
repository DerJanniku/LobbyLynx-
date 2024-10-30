
package org.derjannik.lobbylynx.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.derjannik.lobbylynx.LobbyLynx;
import org.bukkit.configuration.file.FileConfiguration;

public class SetMinigameCommand implements CommandExecutor {
    private final LobbyLynx plugin;

    public SetMinigameCommand(LobbyLynx plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 6) {
            player.sendMessage("Usage: /setminigame <minigame_name> <slot> <item_id/name> <x> <y> <z>");
            return true;
        }

        String minigameName = args[0];
        int slot;
        String icon = args[2];
        double x, y, z;

        try {
            slot = Integer.parseInt(args[1]);
            x = Double.parseDouble(args[3]);
            y = Double.parseDouble(args[4]);
            z = Double.parseDouble(args[5]);
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid number format. Please use numbers for slot, x, y, and z.");
            return true;
        }

        FileConfiguration config = plugin.getConfig();
        config.set("minigames." + minigameName + ".name", minigameName);
        config.set("minigames." + minigameName + ".slot", slot);
        config.set("minigames." + minigameName + ".icon", icon);
        config.set("minigames." + minigameName + ".world", player.getWorld().getName());
        config.set("minigames." + minigameName + ".x", x);
        config.set("minigames." + minigameName + ".y", y);
        config.set("minigames." + minigameName + ".z", z);
        config.set("minigames." + minigameName + ".yaw", player.getLocation().getYaw());
        config.set("minigames." + minigameName + ".pitch", player.getLocation().getPitch());

        plugin.saveConfig();
        player.sendMessage("Minigame " + minigameName + " has been set successfully.");

        return true;
    }
}
