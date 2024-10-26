
package org.derjannik.lobbyLynx.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class SetLobbySpawnCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public SetLobbySpawnCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (args.length != 5) {
            sender.sendMessage("Usage: /lynx set lobbyspawn <slot> <item_id/name> <x> <y> <z>");
            return true;
        }

        int slot = Integer.parseInt(args[0]);
        String itemId = args[1];
        int x = Integer.parseInt(args[2]);
        int y = Integer.parseInt(args[3]);
        int z = Integer.parseInt(args[4]);

        FileConfiguration config = plugin.getConfig();
        config.set("lobby.spawn.slot", slot);
        config.set("lobby.spawn.item", itemId);
        config.set("lobby.spawn.location.x", x);
        config.set("lobby.spawn.location.y", y);
        config.set("lobby.spawn.location.z", z);
        plugin.saveConfig();

        sender.sendMessage("Lobby spawn set successfully!");

        return true;
    }
}
