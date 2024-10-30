package org.derjannik.lobbyLynx;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager {

    private final LobbyLynx plugin;
    private FileConfiguration gameruleConfig;

    public ConfigManager(LobbyLynx plugin) {
        this.plugin = plugin;
        loadGameruleConfig();
    }

    private void loadGameruleConfig() {
        File gameruleFile = new File(plugin.getDataFolder(), "GameruleGUI.yml");
        if (!gameruleFile.exists()) {
            plugin.saveResource("GameruleGUI.yml", false);
        }

        gameruleConfig = YamlConfiguration.loadConfiguration(gameruleFile);
    }

    public int getGuiSize() {
        return gameruleConfig.getInt("gui_size", 36); // Default to 36 if not found
    }

    public String getNavigatorName() {
        return gameruleConfig.getString("navigator_name", "Lynx Gamerules"); // Default name if not found
    }

    public boolean isAllowBlockBreaking() {
        return gameruleConfig.getBoolean("allow_block_breaking", false);
    }

    public boolean isAllowBlockPlacing() {
        return gameruleConfig.getBoolean("allow_block_placing", false);
    }

    public boolean isAllowTntUse() {
        return gameruleConfig.getBoolean("allow_tnt_use", false);
    }

    public boolean isAllowExplosions() {
        return gameruleConfig.getBoolean("allow_explosions", false);
    }

    public boolean isAllowPvp() {
        return gameruleConfig.getBoolean("allow_pvp", false);
    }

    public boolean isAlwaysDay() {
        return gameruleConfig.getBoolean("always_day", true);
    }

    public boolean isNoWeather() {
        return gameruleConfig.getBoolean("no_weather", true);
    }
}