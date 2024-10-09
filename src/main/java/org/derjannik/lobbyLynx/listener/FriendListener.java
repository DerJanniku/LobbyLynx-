package org.derjannik.lobbyLynx.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.derjannik.lobbyLynx.utils.FriendSystem;

import java.util.UUID;

public class FriendListener implements Listener {
    private FriendSystem friendSystem;
    private JavaPlugin plugin;

    public FriendListener(FriendSystem friendSystem, JavaPlugin plugin) {
        this.friendSystem = friendSystem;
        this.plugin = plugin;
    }

    // Freundesliste-GUI: Online-Status der Freunde anzeigen
    public void showFriends(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, "Freunde");

        for (UUID friendUUID : friendSystem.getFriends()) {
            Player friend = Bukkit.getPlayer(friendUUID);
            ItemStack item = new ItemStack(org.bukkit.Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwner(friend.getName());
            meta.setDisplayName(friend.getName());

            if (friend.isOnline()) {
                meta.setDisplayName("§a" + friend.getName());
            } else {
                meta.setDisplayName("§c" + friend.getName());
            }

            inventory.addItem(item);
        }

        player.openInventory(inventory);
    }

    // Freundesliste-GUI: Freunde entfernen
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == org.bukkit.Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            String displayName = meta.getDisplayName();

            if (displayName.equals("Freunde")) {
                showFriends(player);
            } else {
                UUID friendUUID = UUID.fromString(displayName);
                friendSystem.removeFriend(player, friendUUID);
            }
        }
    }

    // Freundesliste-GUI: Freunde hinzufügen
    @EventHandler
    public void onPlayerInteractAddFriend(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == org.bukkit.Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            String displayName = meta.getDisplayName();

            if (displayName.equals("Freunde")) {
                showFriends(player);
            } else {
                UUID friendUUID = UUID.fromString(displayName);
                friendSystem.addFriend(player, friendUUID);
            }
        }
    }

    // Freundesliste-GUI: Freundesliste aktualisieren
    public void updateGUI() {
        // Aktualisieren der Freundesliste-GUI
        for (Player player : Bukkit.getOnlinePlayers()) {
            showFriends(player);
        }
    }
}