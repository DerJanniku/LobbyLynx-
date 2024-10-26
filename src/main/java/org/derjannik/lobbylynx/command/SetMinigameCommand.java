
package org.derjannik.lobbyLynx.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class SetMinigameCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public SetMinigameCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (args.length != 6) {
            sender.sendMessage("Usage: /lynx set minigame <minigame_name> <slot> <item_id/name> <x> <y> <z>");
            return true;
        }

        String minigameName = args[0];
        int slot = Integer.parseInt(args[1]);
        String itemId = args[2];
        int x = Integer.parseInt(args[3]);
        int y = Integer.parseInt(args[4]);
        int z = Integer.parseInt(args[5]);

        FileConfiguration config = plugin.getConfig();
        config.set("minigames." + minigameName + ".slot", slot);
        config.set("minigames." + minigameName + ".item", itemId);
        config.set("minigames." + minigameName + ".location.x", x);
        config.set("minigames." + minigameName + ".location.y", y);
        config.set("minigames." + minigameName + ".location.z", z);
        plugin.saveConfig();

        sender.sendMessage("Minigame " + minigameName + " set successfully!");
        return true;
    }
}
