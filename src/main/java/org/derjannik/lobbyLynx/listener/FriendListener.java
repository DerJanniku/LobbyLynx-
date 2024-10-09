package org.derjannik.lobbyLynx.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.derjannik.lobbyLynx.utils.FriendSystem;

public class FriendListener implements Listener {
    private FriendSystem friendSystem;

    public FriendListener(FriendSystem friendSystem) {
        this.friendSystem = friendSystem;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        // Logik zum Reagieren auf das Klicken auf einen Freund
    }
}