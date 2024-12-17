
package org.derjannik.lobbyLynx.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.derjannik.lobbyLynx.managers.ConfigManager;
import org.derjannik.lobbyLynx.LobbyLynx;

public class CommonCommands implements CommandExecutor {

    @SuppressWarnings("unused")
    private final LobbyLynx plugin;
    private final ConfigManager configManager;

    public CommonCommands(LobbyLynx plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        Location lobbyLocation = configManager.getLobbySpawn();

        if (lobbyLocation == null) {
            player.sendMessage(ChatColor.RED + "Lobby location is not set. Please contact an administrator.");
            return true;
        }

        player.teleport(lobbyLocation);
        player.sendMessage(ChatColor.GREEN + "Teleported to the lobby!");
        return true;
    }
}
