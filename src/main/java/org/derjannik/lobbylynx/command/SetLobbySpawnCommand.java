
package org.derjannik.lobbylynx.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.derjannik.lobbylynx.LobbyLynx;
import org.bukkit.configuration.file.FileConfiguration;

public class SetLobbySpawnCommand implements CommandExecutor {
    private final LobbyLynx plugin;

    public SetLobbySpawnCommand(LobbyLynx plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 5) {
            player.sendMessage("Usage: /setlobbyspawn <slot> <item_id/name> <x> <y> <z>");
            return true;
        }

        int slot;
        String icon = args[1];
        double x, y, z;

        try {
            slot = Integer.parseInt(args[0]);
            x = Double.parseDouble(args[2]);
            y = Double.parseDouble(args[3]);
            z = Double.parseDouble(args[4]);
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid number format. Please use numbers for slot, x, y, and z.");
            return true;
        }

        FileConfiguration config = plugin.getConfig();
        config.set("lobby_spawn.slot", slot);
        config.set("lobby_spawn.icon", icon);
        config.set("lobby_spawn.world", player.getWorld().getName());
        config.set("lobby_spawn.x", x);
        config.set("lobby_spawn.y", y);
        config.set("lobby_spawn.z", z);
        config.set("lobby_spawn.yaw", player.getLocation().getYaw());
        config.set("lobby_spawn.pitch", player.getLocation().getPitch());

        plugin.saveConfig();
        player.sendMessage("Lobby spawn has been set successfully.");

        return true;
    }
}
