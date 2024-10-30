
package org.derjannik.lobbylynx.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.derjannik.lobbylynx.LobbyLynx;

public class LobbyCommand implements CommandExecutor {
    private final LobbyLynx plugin;

    public LobbyCommand(LobbyLynx plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;
        player.sendMessage("Teleporting to lobby in 3 seconds. Don't move!");

        new BukkitRunnable() {
            private int countdown = 3;
            private final double startX = player.getLocation().getX();
            private final double startY = player.getLocation().getY();
            private final double startZ = player.getLocation().getZ();

            @Override
            public void run() {
                if (countdown > 0) {
                    player.sendMessage("Teleporting in " + countdown + "...");
                    countdown--;
                } else {
                    if (hasPlayerMoved(player)) {
                        player.sendMessage("Teleport cancelled. You moved!");
                    } else {
                        plugin.getNavigator().teleportToLobbySpawn(player);
                    }
                    this.cancel();
                }
            }

            private boolean hasPlayerMoved(Player player) {
                double currentX = player.getLocation().getX();
                double currentY = player.getLocation().getY();
                double currentZ = player.getLocation().getZ();
                return startX != currentX || startY != currentY || startZ != currentZ;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        return true;
    }
}