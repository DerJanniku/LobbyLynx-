
package org.derjannik.lobbyLynx.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.derjannik.lobbyLynx.command.FriendCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class FriendSystem extends JavaPlugin implements Listener {

    private static final Logger LOGGER = Logger.getLogger(FriendSystem.class.getName());
    private Map<UUID, UUID> friendRequests = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("friend").setExecutor(new FriendCommand(this));
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getInventory().contains(Material.PLAYER_HEAD)) {
                    ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
                    meta.setDisplayName("Friends");
                    meta.setOwningPlayer(player);
                    playerHead.setItemMeta(meta);
                    player.getInventory().setItem(8, playerHead);
                }
            }
        }, 0L, 20L);
        LOGGER.info("FriendSystem plugin enabled");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.PLAYER_HEAD && item.getItemMeta().getDisplayName().equals("Friends")) {
            Inventory friendsGui = Bukkit.createInventory(null, 9, "Friends");
            // Add items to the friends GUI
            player.openInventory(friendsGui);
            LOGGER.info("Friends GUI opened for player: " + player.getName());
        }
    }

    public Map<UUID, UUID> getFriendRequests() {
        return friendRequests;
    }
}
