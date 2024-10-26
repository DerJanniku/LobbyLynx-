
package org.derjannik.lobbyLynx.command;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class LobbyCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public LobbyCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        FileConfiguration config = plugin.getConfig();

        int x = config.getInt("lobby.spawn.location.x");
        int y = config.getInt("lobby.spawn.location.y");
        int z = config.getInt("lobby.spawn.location.z");

        Location lobbyLocation = new Location(Bukkit.getWorld("world"), x, y, z);

        player.sendMessage("Teleporting to the Lobby in 3 seconds...");
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendMessage("You have been teleported to the Lobby.");
                player.teleport(lobbyLocation);
            }
        }, 60L); // 3 seconds delay (20 ticks per second)

        return true;
    }
}
