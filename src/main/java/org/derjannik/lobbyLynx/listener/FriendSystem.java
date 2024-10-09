package org.derjannik.lobbyLynx.command;

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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class FriendCommand implements Listener {

    private static final Logger LOGGER = Logger.getLogger(FriendCommand.class.getName());
    private FriendSystem friendSystem;

    public FriendCommand(FriendSystem friendSystem) {
        this.friendSystem = friendSystem;
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
}