package org.derjannik.lobbyLynx.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.derjannik.lobbyLynx.LobbyLynx;
import org.derjannik.lobbyLynx.util.Advertisement;
import org.derjannik.lobbyLynx.enums.AdType;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.*;

public class AdvertisementManager {
    private final LobbyLynx plugin;
    private final List<Advertisement> advertisements;
    private final Map<Player, BossBar> activeBossBars;
    private int currentAdIndex;
    private BukkitRunnable adTask;

    public AdvertisementManager(LobbyLynx plugin) {
        this.plugin = plugin;
        this.advertisements = new ArrayList<>();
        this.activeBossBars = new HashMap<>();
        this.currentAdIndex = 0;
        loadAdvertisements();
        startAdRotation();
    }

    private void loadAdvertisements() {
        // Lade Werbungen aus der config.yml
        if (plugin.getConfig().contains("advertisements")) {
            for (String adId : plugin.getConfig().getConfigurationSection("advertisements").getKeys(false)) {
                String path = "advertisements." + adId + ".";
                String title = ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString(path + "title", ""));
                List<String> content = plugin.getConfig().getStringList(path + "content").stream()
                        .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                        .collect(java.util.stream.Collectors.toList());
                AdType type = AdType.valueOf(plugin.getConfig().getString(path + "type", "CHAT").toUpperCase());
                int duration = plugin.getConfig().getInt(path + "duration", 5);
                String permission = plugin.getConfig().getString(path + "permission", "");

                advertisements.add(new Advertisement(adId, title, content, type, duration, permission));
            }
        }
    }

    private void startAdRotation() {
        int interval = plugin.getConfig().getInt("ad-interval", 60) * 20; // Convert to ticks

        adTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!advertisements.isEmpty()) {
                    showNextAdvertisement();
                }
            }
        };

        adTask.runTaskTimer(plugin, interval, interval);
    }

    private void showNextAdvertisement() {
        if (advertisements.isEmpty()) return;

        Advertisement ad = advertisements.get(currentAdIndex);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (ad.getPermission().isEmpty() || player.hasPermission(ad.getPermission())) {
                displayAdvertisement(player, ad);
            }
        }

        currentAdIndex = (currentAdIndex + 1) % advertisements.size();
    }

    public void displayAdvertisement(Player player, Advertisement ad) {
        switch (ad.getType()) {
            case CHAT:
                displayChatAd(player, ad);
                break;
            case TITLE:
                displayTitleAd(player, ad);
                break;
            case ACTIONBAR:
                displayActionBarAd(player, ad);
                break;
            case BOSSBAR:
                displayBossBarAd(player, ad);
                break;
            case SCOREBOARD:
                displayScoreboardAd(player, ad);
                break;
        }
    }

    private void displayChatAd(Player player, Advertisement ad) {
        player.sendMessage(ChatColor.DARK_GRAY + "=== " + ad.getTitle() + ChatColor.DARK_GRAY + " ===");
        for (String line : ad.getContent()) {
            player.sendMessage(line);
        }
        player.sendMessage(ChatColor.DARK_GRAY + "=".repeat(20));
    }

    private void displayTitleAd(Player player, Advertisement ad) {
        String subtitle = ad.getContent().isEmpty() ? "" : ad.getContent().get(0);
        player.sendTitle(
                ad.getTitle(),
                subtitle,
                10, // fade in
                ad.getDuration() * 20, // stay
                10  // fade out
        );
    }

    private void displayActionBarAd(Player player, Advertisement ad) {
        new BukkitRunnable() {
            int timeLeft = ad.getDuration();
            int contentIndex = 0;

            @Override
            public void run() {
                if (timeLeft <= 0 || !player.isOnline()) {
                    cancel();
                    return;
                }

                String message = ad.getContent().get(contentIndex);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));

                contentIndex = (contentIndex + 1) % ad.getContent().size();
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void displayBossBarAd(Player player, Advertisement ad) {
        // Remove existing bossbar if present
        if (activeBossBars.containsKey(player)) {
            activeBossBars.get(player).removePlayer(player);
        }

        BossBar bossBar = Bukkit.createBossBar(
                ad.getTitle(),
                BarColor.BLUE,
                BarStyle.SOLID
        );

        bossBar.addPlayer(player);
        activeBossBars.put(player, bossBar);

        // Remove bossbar after duration
        new BukkitRunnable() {
            @Override
            public void run() {
                bossBar.removePlayer(player);
                activeBossBars.remove(player);
            }
        }.runTaskLater(plugin, ad.getDuration() * 20L);
    }

    // Fortsetzung von AdvertisementManager.java
    private void displayScoreboardAd(Player player, Advertisement ad) {
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        org.bukkit.scoreboard.Scoreboard scoreboard = manager.getNewScoreboard();
        org.bukkit.scoreboard.Objective objective = scoreboard.registerNewObjective("ad", "dummy", ad.getTitle(), org.bukkit.scoreboard.RenderType.INTEGER);
        objective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);

        int score = ad.getContent().size();
        for (String line : ad.getContent()) {
            objective.getScore(line).setScore(score--);
        }

        player.setScoreboard(scoreboard);

        // Reset scoreboard after duration
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setScoreboard(manager.getNewScoreboard());
            }
        }.runTaskLater(plugin, ad.getDuration() * 20L);
    }

    public void createAdvertisement(String id, String title, List<String> content, AdType type, int duration, String permission) {
        Advertisement ad = new Advertisement(id, title, content, type, duration, permission);
        advertisements.add(ad);
        saveAdvertisement(ad);
    }

    public void removeAdvertisement(String id) {
        advertisements.removeIf(ad -> ad.getId().equals(id));
        plugin.getConfig().set("advertisements." + id, null);
        plugin.saveConfig();
    }

    private void saveAdvertisement(Advertisement ad) {
        String path = "advertisements." + ad.getId() + ".";
        plugin.getConfig().set(path + "title", ad.getTitle());
        plugin.getConfig().set(path + "content", ad.getContent());
        plugin.getConfig().set(path + "type", ad.getType().name());
        plugin.getConfig().set(path + "duration", ad.getDuration());
        plugin.getConfig().set(path + "permission", ad.getPermission());
        plugin.saveConfig();
    }

    public Advertisement getWelcomeAd() {
        // Prüfe ob Willkommenswerbung aktiviert ist
        if (!plugin.getConfig().getBoolean("ads.welcome-ad.enabled", true)) {
            return null;
        }

        // Lade Willkommenswerbung aus der Konfiguration
        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("ads.welcome-ad.title", "&6Welcome!"));

        List<String> content = plugin.getConfig().getStringList("ads.welcome-ad.content").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(java.util.stream.Collectors.toList());

        AdType type = AdType.valueOf(
                plugin.getConfig().getString("ads.welcome-ad.type", "TITLE").toUpperCase());

        int duration = plugin.getConfig().getInt("ads.welcome-ad.duration", 5);

        String permission = plugin.getConfig().getString("ads.welcome-ad.permission", "");

        // Erstelle und gebe die Willkommenswerbung zurück
        return new Advertisement(
                "welcome_ad",
                title,
                content,
                type,
                duration,
                permission
        );
    }

    public void cleanupPlayer(Player player) {
        // Entferne aktive BossBars
        if (activeBossBars.containsKey(player)) {
            BossBar bossBar = activeBossBars.get(player);
            bossBar.removePlayer(player);
            activeBossBars.remove(player);
        }

        // Entferne aktive Scoreboards
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

        // Entferne aktive Titel
        player.resetTitle();

        // Entferne ActionBar Nachrichten
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));

        // Optional: Lösche spielerspezifische Daten
        if (plugin.getConfig().getBoolean("ads.clear-data-on-quit", true)) {
            // Lösche gespeicherte Werbedaten für den Spieler
            String playerName = player.getName();
            plugin.getConfig().set("ads.player-data." + playerName, null);
            plugin.saveConfig();
        }

        // Cancele alle laufenden Werbe-Tasks für diesen Spieler
        Bukkit.getScheduler().getPendingTasks().stream()
                .filter(task -> task.getOwner().equals(plugin))
                .filter(task -> {
                    if (task instanceof BukkitRunnable) {
                        // Prüfe ob der Task zu diesem Spieler gehört
                        return task.toString().contains(player.getName());
                    }
                    return false;
                })
                .forEach(task -> task.cancel());
    }

    public void cleanup() {
        return;
    }

    public void showAdvertisement(Player player, String advertisementId) {
        // Find the advertisement with the given ID
        Optional<Advertisement> adOpt = advertisements.stream()
                .filter(ad -> ad.getId().equals(advertisementId))
                .findFirst();

        if (adOpt.isPresent()) {
            displayAdvertisement(player, adOpt.get());
        } else {
            player.sendMessage(ChatColor.RED + "Advertisement not found!");
        }
    }
}