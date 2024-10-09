package org.derjannik.lobbyLynx.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.derjannik.lobbyLynx.utils.FriendSystem;

public class FriendListener implements Listener {
    private final FriendSystem friendSystem;

    public FriendListener(FriendSystem friendSystem) {
        this.friendSystem = friendSystem;
    }

    // Freundesliste-GUI: Online-Status der Freunde anzeigen
    public void showFriends(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, "Freunde");

        for (java.util.UUID friendUUID : friendSystem.getFriends()) {
            Player friend = Bukkit.getPlayer(friendUUID);
            if (friend != null) {
                ItemStack item = new ItemStack(org.bukkit.Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                if (friend != null) {
                    meta.setOwningPlayer(friend);
                }
                meta.setDisplayName(friend.getName());

                if (friend.isOnline()) {
                    meta.setDisplayName("§a" + friend.getName());
                } else {
                    meta.setDisplayName("§c" + friend.getName());
                }

                inventory.addItem(item);
            }
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
                java.util.UUID friendUUID = java.util.UUID.fromString(displayName);
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
                java.util.UUID friendUUID = java.util.UUID.fromString(displayName);
                Player friend = Bukkit.getPlayer(friendUUID);
                if (friend != null) {
                    friendSystem.addFriend(player, friend);
                }
            }
        }
    }

    // Freundesliste-GUI: Freundes liste aktualisieren
    public void updateGUI() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            showFriends(player);
        }
    }
}