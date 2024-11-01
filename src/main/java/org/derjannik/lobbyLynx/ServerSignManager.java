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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ServerSignManager implements Listener {
    private final LobbyLynx plugin;
    private final ConfigManager configManager;
    private final Map<Location, ServerSign> serverSigns = new ConcurrentHashMap<>();
    private final Map<Player, String> pendingSignCreations = new HashMap<>();
    private final List<String> signFormat;
    private final Map<String, ServerInfo> serverInfoCache = new ConcurrentHashMap<>();
    private final Map<Player, Long> cooldowns = new HashMap<>();

    private final List<List<String>> animationFrames = new ArrayList<>();
    private int currentFrame = 0;

    public ServerSignManager(LobbyLynx plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.signFormat = loadSignFormat();
        loadServerSigns();
        startSignUpdater();
        loadAnimationFrames();
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

    private void loadAnimationFrames() {
        List<String> frames = configManager.getConfig().getStringList("server-signs.animation-frames");
        for (String frame : frames) {
            animationFrames.add(Arrays.asList(frame.split("\\|")));
        }
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
            animateSigns(animationFrames);
        }, 0L, updateInterval);
    }

    void animateSigns(List<List<String>> animationFrames) {
        if (this.animationFrames.isEmpty()) return;
        currentFrame = (currentFrame + 1) % this.animationFrames.size();
        for (Map.Entry<Location, ServerSign> entry : serverSigns.entrySet()) {
            updateSignDisplay(entry.getValue(), entry.getKey(), this.animationFrames.get(currentFrame));
        }
    }

    private void updateServerInfo(ServerSign sign) {
        ServerInfo cachedInfo = serverInfoCache.get(sign.getServerName());
        if (cachedInfo == null || System.currentTimeMillis() - cachedInfo.lastUpdated > TimeUnit.SECONDS.toMillis(30)) {
            int onlinePlayers = (int) (Math.random() * 100);
            int maxPlayers = 100;
            String motd = "Welcome to " + sign.getServerName();
            cachedInfo = new ServerInfo(onlinePlayers, maxPlayers, motd);
            serverInfoCache.put(sign.getServerName(), cachedInfo);
        }
        sign.setOnlinePlayers(cachedInfo.onlinePlayers);
        sign.setMaxPlayers(cachedInfo.maxPlayers);
        sign.setMotd(cachedInfo.motd);
    }

    // Update server information from the main plugin
    public void updateServerInfo(String serverName, int onlinePlayers, int maxPlayers, String motd) {
        serverInfoCache.put(serverName, new ServerInfo(onlinePlayers, maxPlayers, motd));

        // Update relevant signs with the new info
        for (Map.Entry<Location, ServerSign> entry : serverSigns.entrySet()) {
            ServerSign sign = entry.getValue();
            if (sign.getServerName().equals(serverName)) {
                sign.setOnlinePlayers(onlinePlayers);
                sign.setMaxPlayers(maxPlayers);
                sign.setMotd(motd);
                updateSignDisplay(sign, entry.getKey(), signFormat);
            }
        }
    }

    private void updateSignDisplay(ServerSign sign, Location loc, List<String> frame) {
        Block block = loc.getBlock();
        if (block.getState() instanceof Sign) {
            Sign bukkitSign = (Sign) block.getState();
            for (int i = 0; i < frame.size() && i < 4; i++) {
                String line = ChatColor.translateAlternateColorCodes('&', frame.get(i)
                        .replace("%server%", sign.getDisplayName())
                        .replace("%players%", String.valueOf(sign.getOnlinePlayers()))
                        .replace("%maxplayers%", String.valueOf(sign.getMaxPlayers()))
                        .replace("%motd%", sign.getMotd()));
                bukkitSign.setLine(i, line);
            }
            bukkitSign.update();
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
            updateSignDisplay(serverSign, loc, signFormat);
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
                        if (checkCooldown(player)) {
                            connectToServer(player, sign.getServerName());
                        } else {
                            player.sendMessage(ChatColor.RED + "Please wait before using this sign again.");
                        }
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                configManager.getConfig().getString("messages.sign.no-permission")));
                    }
                }
            }
        }
    }

    private boolean checkCooldown(Player player) {
        long cooldownTime = configManager.getConfig().getLong("server-signs.cooldown", 3000);
        if (cooldowns.containsKey(player)) {
            long secondsLeft = ((cooldowns.get(player) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
            if (secondsLeft > 0) {
                return false;
            }
        }
        cooldowns.put(player, System.currentTimeMillis());
        return true;
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
        String path = "server-signs.servers." + server + ".signs";
        configManager.getConfig().set(path + "." + loc.getWorld().getName() + "." + loc.getX() + "." + loc.getY() + "." + loc.getZ(), null);
        plugin.saveConfig();
    }

    public void removeSign(Player player, Location location) {
        if (player.hasPermission("lynx.serversigns.remove")) {
            if (serverSigns.containsKey(location)) {
                serverSigns.remove(location);
                player.sendMessage(ChatColor.GREEN + "Server sign removed successfully.");
                removeSignFromConfig(location);
            } else {
                player.sendMessage(ChatColor.RED + "No server sign found at this location.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "You don't have permission to remove server signs.");
        }
    }

    private void removeSignFromConfig(Location location) {
        String world = location.getWorld().getName();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        ConfigurationSection serversSection = configManager.getConfig().getConfigurationSection("server-signs.servers");
        if (serversSection != null) {
            for (String serverName : serversSection.getKeys(false)) {
                ConfigurationSection serverSection = serversSection.getConfigurationSection(serverName);
                ConfigurationSection signsSection = serverSection.getConfigurationSection("signs");
                for (String key : signsSection.getKeys(false)) {
                    ConfigurationSection signSection = signsSection.getConfigurationSection(key);
                    if (signSection.getString("world").equals(world) &&
                            signSection.getDouble("x") == x &&
                            signSection.getDouble("y") == y &&
                            signSection.getDouble("z") == z) {
                        signSection.set(null);
                        plugin.saveConfig();
                        return;
                    }
                }
            }
        }
    }

    private static class ServerSign {
        private final String serverName;
        private final String displayName;
        private int onlinePlayers;
        private int maxPlayers;
        private String motd;

        public ServerSign(String serverName, String displayName) {
            this.serverName = serverName;
            this.displayName = displayName;
        }

        public String getServerName() { return serverName; }
        public String getDisplayName() { return displayName; }
        public int getOnlinePlayers() { return onlinePlayers; }
        public void setOnlinePlayers(int onlinePlayers) { this.onlinePlayers = onlinePlayers; }
        public int getMaxPlayers() { return maxPlayers; }
        public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
        public String getMotd() { return motd; }
        public void setMotd(String motd) { this.motd = motd; }
    }

    private static class ServerInfo {
        private final int onlinePlayers;
        private final int maxPlayers;
        private final String motd;
        private final long lastUpdated;

        public ServerInfo(int onlinePlayers, int maxPlayers, String motd) {
            this.onlinePlayers = onlinePlayers;
            this.maxPlayers = maxPlayers;
            this.motd = motd;
            this.lastUpdated = System.currentTimeMillis();
        }
    }
}
