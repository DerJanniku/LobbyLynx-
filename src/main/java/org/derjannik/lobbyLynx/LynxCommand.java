package org.derjannik.lobbyLynx;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LynxCommand implements CommandExecutor {

    private final LobbyLynx plugin;

    public LynxCommand(LobbyLynx plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("lynx.admin")) { // Check for permission
                if (args.length > 0 && args[0].equalsIgnoreCase("settings")) {
                    new SettingsGUI(plugin).openSettingsGUI(player);
                    return true;
                } else {
                    player.sendMessage("Usage: /lynx settings");
                    return false;
                }
            } else {
                player.sendMessage("You do not have permission to use this command.");
                return true; // Return true to indicate command was processed
            }
        }
        return false;
    }
}