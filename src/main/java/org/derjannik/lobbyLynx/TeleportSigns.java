
package org.derjannik.lobbyLynx;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TeleportSigns implements Listener {

    private final LobbyLynx plugin;
    private final ConfigManager configManager;
    private final Map<Location, String> teleportSigns = new HashMap<>();
    private final Map<String, ServerInfo> serverInfoMap = new HashMap<>();

    public TeleportSigns(LobbyLynx plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        loadTeleportSigns();
        startServerInfoUpdater();
    }

    private void loadTeleportSigns() {
        ConfigurationSection signs = configManager.getConfig().getConfigurationSection("teleport-signs");
        if (signs != null) {
            for (String key : signs.getKeys(false)) {
                ConfigurationSection sign = signs.getConfigurationSection(key);
                if (sign != null) {
                    try {
                        Location loc = new Location(
                            Bukkit.getWorld(sign.getString("world")),
                            sign.getDouble("x"),
                            sign.getDouble("y"),
                            sign.getDouble("z")
                        );
                        String destination = sign.getString("destination");
                        if (destination == null || destination.isEmpty()) {
                            plugin.getLogger().warning("Invalid destination for sign " + key);
                            continue;
                        }
                        teleportSigns.put(loc, destination);
                        if (!serverInfoMap.containsKey(destination)) {
                            serverInfoMap.put(destination, new ServerInfo(destination));
                        }
                        plugin.getLogger().info("Loaded teleport sign at " + loc + " for destination " + destination);
                    } catch (Exception e) {
                        plugin.getLogger().severe("Error loading teleport sign " + key + ": " + e.getMessage());
                    }
                }
            }
        } else {
            plugin.getLogger().warning("No teleport signs configured in config.yml");
        }
    }

    private void startServerInfoUpdater() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (ServerInfo info : serverInfoMap.values()) {
                updateServerInfo(info);
            }
        }, 0L, 20L * 10); // Update every 10 seconds
    }

    private void updateServerInfo(ServerInfo info) {
        // Here you would implement the logic to fetch server info from Velocity
        // For now, we'll just use placeholder data
        info.setOnlinePlayers((int) (Math.random() * 100));
        info.setMaxPlayers(100);
        info.setMotd("Welcome to " + info.getName());
        updateSigns(info.getName());
    }

    public void updateServerInfo(String serverName, int onlinePlayers, int maxPlayers, String motd) {
        ServerInfo info = serverInfoMap.get(serverName);
        if (info != null) {
            info.setOnlinePlayers(onlinePlayers);
            info.setMaxPlayers(maxPlayers);
            info.setMotd(motd);
            updateSigns(serverName);
        }
    }

    private void updateSigns(String serverName) {
        for (Map.Entry<Location, String> entry : teleportSigns.entrySet()) {
            if (entry.getValue().equals(serverName)) {
                Location signLoc = entry.getKey();
                Block block = signLoc.getBlock();
                if (block.getState() instanceof Sign) {
                    Sign sign = (Sign) block.getState();
                    ServerInfo info = serverInfoMap.get(serverName);
                    sign.setLine(0, serverName);
                    sign.setLine(1, info.getMotd());
                    sign.setLine(2, info.getOnlinePlayers() + "/" + info.getMaxPlayers());
                    sign.update();
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && clickedBlock.getState() instanceof Sign) {
                Location signLoc = clickedBlock.getLocation();
                if (teleportSigns.containsKey(signLoc)) {
                    Player player = event.getPlayer();
                    if (player.hasPermission("lynx.teleportsigns.use")) {
                        String destination = teleportSigns.get(signLoc);
                        teleportPlayer(player, destination);
                    } else {
                        player.sendMessage("You don't have permission to use this teleport sign.");
                        plugin.getLogger().info("Player " + player.getName() + " attempted to use a teleport sign without permission.");
                    }
                }
            }
        }
    }

    private void teleportPlayer(Player player, String destination) {
        ServerInfo serverInfo = serverInfoMap.get(destination);
        if (serverInfo != null) {
            // Send player to Velocity server
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            try {
                out.writeUTF("Connect");
                out.writeUTF(destination);
                player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
                player.sendMessage("Connecting you to " + destination);
                plugin.getLogger().info("Teleporting player " + player.getName() + " to " + destination);
            } catch (IOException e) {
                plugin.getLogger().severe("Error teleporting player " + player.getName() + " to " + destination + ": " + e.getMessage());
                player.sendMessage("An error occurred while trying to connect you to " + destination);
            }
        } else {
            plugin.getLogger().warning("Attempted to teleport player " + player.getName() + " to unknown destination: " + destination);
            player.sendMessage("Unable to connect to " + destination + ". The server might be offline.");
        }
    }

    private static class ServerInfo {
        private final String name;
        private int onlinePlayers;
        private int maxPlayers;
        private String motd;

        public ServerInfo(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int getOnlinePlayers() {
            return onlinePlayers;
        }

        public void setOnlinePlayers(int onlinePlayers) {
            this.onlinePlayers = onlinePlayers;
        }

        public int getMaxPlayers() {
            return maxPlayers;
        }

        public void setMaxPlayers(int maxPlayers) {
            this.maxPlayers = maxPlayers;
        }

        public String getMotd() {
            return motd;
        }

        public void setMotd(String motd) {
            this.motd = motd;
        }
    }

    // Test method to verify TeleportSigns functionality
    public void testTeleportSigns() {
        System.out.println("=== TeleportSigns Test ===");
        System.out.println("Loaded Teleport Signs:");
        for (Map.Entry<Location, String> entry : teleportSigns.entrySet()) {
            Location loc = entry.getKey();
            String dest = entry.getValue();
            System.out.println("Sign at " + loc.getWorld().getName() + " (" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ") -> " + dest);
        }

        System.out.println("\nAvailable Destinations:");
        ConfigurationSection destinations = configManager.getConfig().getConfigurationSection("destinations");
        if (destinations != null) {
            for (String key : destinations.getKeys(false)) {
                ConfigurationSection destConfig = destinations.getConfigurationSection(key);
                if (destConfig != null) {
                    System.out.println(key + ": " + destConfig.getString("world") + " (" +
                            destConfig.getDouble("x") + ", " +
                            destConfig.getDouble("y") + ", " +
                            destConfig.getDouble("z") + ")");
                }
            }
        }
        System.out.println("=== End of Test ===");
    }
}
