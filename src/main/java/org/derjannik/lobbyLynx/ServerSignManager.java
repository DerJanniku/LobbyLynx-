package org.derjannik.lobbyLynx;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class ServerSignManager implements Listener {
    private final LobbyLynx plugin;
    private final ConfigManager configManager;
    private final Map<Location, ServerSign> serverSigns = new HashMap<>();
    private final Map<Player, String> pendingSignCreations = new HashMap<>();
    private final List<String> signFormat;

    public ServerSignManager(LobbyLynx plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.signFormat = loadSignFormat();
        loadServerSigns();
        startSignUpdater();
    }

    private List<String> loadSignFormat() {
        List<String> format = configManager.getConfig().getStringList("server-signs.format");
        if (format.isEmpty()) {
            format = new ArrayList<>();
            format.add("&8[&bServer&8]");
            format.add("%server%");
            format.add("%players%/%maxplayers%");
            format.add("&aClick to join!");
        }
        return format;
    }

    private void loadServerSigns() {
        ConfigurationSection serversSection = configManager.getConfig().getConfigurationSection("server-signs.servers");
        if (serversSection != null) {
            for (String serverName : serversSection.getKeys(false)) {
                ConfigurationSection serverSection = serversSection.getConfigurationSection(serverName);
                if (serverSection != null) {
                    String displayName = serverSection.getString("display-name", serverName);
                    ConfigurationSection signsSection = serverSection.getConfigurationSection("signs");
                    if (signsSection != null) {
                        for (String key : signsSection.getKeys(false)) {
                            ConfigurationSection signSection = signsSection.getConfigurationSection(key);
                            Location loc = new Location(
                                    Bukkit.getWorld(signSection.getString("world")),
                                    signSection.getDouble("x"),
                                    signSection.getDouble("y"),
                                    signSection.getDouble("z")
                            );
                            serverSigns.put(loc, new ServerSign(serverName, displayName));
                        }
                    }
                }
            }
        }
    }

    private void startSignUpdater() {
        int updateInterval = configManager.getConfig().getInt("server-signs.update-interval", 20);
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (ServerSign sign : serverSigns.values()) {
                updateServerInfo(sign);
            }
        }, 0L, updateInterval);
    }

    private void updateServerInfo(ServerSign sign) {
        // Here you would implement the actual server info fetching from Velocity/BungeeCord
        // For now, using placeholder data
        sign.setOnlinePlayers((int) (Math.random() * 100));
        sign.setMaxPlayers(100);
        updateSignDisplay(sign);
    }

    private void updateSignDisplay(ServerSign sign) {
        for (Map.Entry<Location, ServerSign> entry : serverSigns.entrySet()) {
            if (entry.getValue().equals(sign)) {
                Location loc = entry.getKey();
                Block block = loc.getBlock();
                if (block.getState() instanceof Sign) {
                    Sign bukkitSign = (Sign) block.getState();
                    for (int i = 0; i < signFormat.size() && i < 4; i++) {
                        String line = ChatColor.translateAlternateColorCodes('&', signFormat.get(i)
                                .replace("%server%", sign.getDisplayName())
                                .replace("%players%", String.valueOf(sign.getOnlinePlayers()))
                                .replace("%maxplayers%", String.valueOf(sign.getMaxPlayers())));
                        bukkitSign.setLine(i, line);
                    }
                    bukkitSign.update();
                }
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        if (pendingSignCreations.containsKey(player)) {
            String server = pendingSignCreations.get(player);
            Location loc = event.getBlock().getLocation();
            ServerSign serverSign = new ServerSign(server,
                    configManager.getConfig().getString("server-signs.servers." + server + ".display-name", server));
            serverSigns.put(loc, serverSign);
            saveSign(loc, server);
            pendingSignCreations.remove(player);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getConfig().getString("messages.sign.created")));
            updateSignDisplay(serverSign);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && block.getState() instanceof Sign) {
                Location loc = block.getLocation();
                ServerSign sign = serverSigns.get(loc);
                if (sign != null) {
                    Player player = event.getPlayer();
                    if (player.hasPermission("lynx.serversigns.use")) {
                        connectToServer(player, sign.getServerName());
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                configManager.getConfig().getString("messages.sign.no-permission")));
                    }
                }
            }
        }
    }

    private void connectToServer(Player player, String server) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("Connect");
            out.writeUTF(server);
            player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getConfig().getString("messages.sign.connecting")
                            .replace("%server%", server)));
        } catch (Exception e) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getConfig().getString("messages.sign.error")
                            .replace("%server%", server)));
            plugin.getLogger().warning("Error connecting player " + player.getName() + " to " + server + ": " + e.getMessage());
        }
    }

    public void createSign(Player player, String server) {
        if (player.hasPermission("lynx.serversigns.create")) {
            pendingSignCreations.put(player, server);
            player.sendMessage(ChatColor.GREEN + "Right-click a sign to link it to " + server);
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getConfig().getString("messages.sign.no-permission")));
        }
    }

    private void saveSign(Location loc, String server) {
        String path = "server-signs.servers." + server + ". signs";
        configManager.getConfig().set(path + "." + loc.getWorld().getName() + "." + loc.getX() + "." + loc.getY() + "." + loc.getZ(), null);
        plugin.saveConfig();
    }

    private static class ServerSign {
        private final String serverName;
        private final String displayName;
        private int onlinePlayers;
        private int maxPlayers;

        public ServerSign(String serverName, String displayName) {
            this.serverName = serverName;
            this.displayName = displayName;
        }

        public String getServerName() {
            return serverName;
        }

        public String getDisplayName() {
            return displayName;
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
    }
}