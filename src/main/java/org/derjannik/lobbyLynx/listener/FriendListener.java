package org.derjannik.lobbyLynx.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.derjannik.lobbyLynx.utils.FriendSystem;

public class FriendListener implements Listener {
    private FriendSystem friendSystem;

    public FriendListener(FriendSystem friendSystem) {
        this.friendSystem = friendSystem;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == org.bukkit.Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            String displayName = meta.getDisplayName();

            if (displayName.equals("Freunde")) {
                friendSystem.showFriends(player);
            }
        }
    }
}