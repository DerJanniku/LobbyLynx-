
package org.derjannik.lobbyLynx;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class BungeeSignManager implements Listener {
    private final LobbyLynx plugin;
    private final Map<String, Location> bungeeSigns = new HashMap<>();
    private final Map<Player, String> pendingSignCreations = new HashMap<>();

    public BungeeSignManager(LobbyLynx plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void startSignCreation(Player player) {
        pendingSignCreations.put(player, null);
        player.sendMessage(ChatColor.GREEN + "Right-click a sign to create a BungeeSign.");
    }

    public void linkSignToServer(Player player, String server) {
        if (pendingSignCreations.containsKey(player)) {
            pendingSignCreations.put(player, server);
            player.sendMessage(ChatColor.GREEN + "Now right-click a sign to link it to the server: " + server);
        } else {
            player.sendMessage(ChatColor.RED + "You must start the sign creation process first with /lynx create bungeesign");
        }
    }

    public void displaySignInfo(Player player) {
        Block targetBlock = player.getTargetBlock(null, 5);
        if (targetBlock.getState() instanceof Sign) {
            Location signLoc = targetBlock.getLocation();
            String server = getServerForSign(signLoc);
            if (server != null) {
                player.sendMessage(ChatColor.GREEN + "This sign is linked to server: " + server);
            } else {
                player.sendMessage(ChatColor.RED + "This sign is not a BungeeSign.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "You must be looking at a sign.");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign) {
            Player player = event.getPlayer();
            if (pendingSignCreations.containsKey(player)) {
                String server = pendingSignCreations.get(player);
                if (server != null) {
                    createBungeeSign(player, event.getClickedBlock().getLocation(), server);
                    pendingSignCreations.remove(player);
                } else {
                    player.sendMessage(ChatColor.RED + "Please specify a server with /lynx bungeesign <server>");
                }
            } else if (bungeeSigns.containsValue(event.getClickedBlock().getLocation())) {
                String server = getServerForSign(event.getClickedBlock().getLocation());
                if (server != null) {
                    connectPlayerToServer(player, server);
                }
            }
        }
    }

    private void createBungeeSign(Player player, Location location, String server) {
        bungeeSigns.put(server, location);
        player.sendMessage(ChatColor.GREEN + "BungeeSign created and linked to server: " + server);
        updateSignText(location, server);
        // TODO: Save the BungeeSign to config
    }

    private void updateSignText(Location location, String server) {
        Block block = location.getBlock();
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            sign.setLine(0, ChatColor.BOLD + "[BungeeSign]");
            sign.setLine(1, server);
            sign.setLine(2, "Right-click");
            sign.setLine(3, "to connect");
            sign.update();
        }
    }

    private String getServerForSign(Location location) {
        for (Map.Entry<String, Location> entry : bungeeSigns.entrySet()) {
            if (entry.getValue().equals(location)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void connectPlayerToServer(Player player, String server) {
        // TODO: Implement connection to BungeeCord server
        player.sendMessage(ChatColor.GREEN + "Connecting to server: " + server);
    }
}
