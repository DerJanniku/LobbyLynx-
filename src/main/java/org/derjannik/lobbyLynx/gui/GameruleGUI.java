package org.derjannik.lobbyLynx.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.derjannik.lobbyLynx.LobbyLynx;
import org.derjannik.lobbyLynx.managers.ConfigManager;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GameruleGUI implements Listener {

    private final LobbyLynx plugin;
    private final ConfigManager configManager;
    private final Map<String, String> gameRuleMapping;
    private final int ITEMS_PER_PAGE = 45;

    public GameruleGUI(LobbyLynx plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.gameRuleMapping = new LinkedHashMap<>();

        // Map game rules to their descriptions
        gameRuleMapping.put("fallDamage", "Disables fall damage");
        gameRuleMapping.put("pvp", "Disables player versus player combat");
        gameRuleMapping.put("tnt", "Disables TNT explosions");
        gameRuleMapping.put("mobGriefing", "Prevents mobs from altering the environment");
        gameRuleMapping.put("doDaylightCycle", "Stops the day-night cycle");
        gameRuleMapping.put("doWeatherCycle", "Disables weather changes");
        gameRuleMapping.put("keepInventory", "Players retain items after death");
        gameRuleMapping.put("naturalRegeneration", "Disables natural health regeneration");
        gameRuleMapping.put("disableElytra", "Prevents the use of Elytra");
        gameRuleMapping.put("doMobSpawning", "Disables the spawning of hostile mobs");
        gameRuleMapping.put("keepSpawnInMemory", "Prevents keeping spawn points in memory");
        gameRuleMapping.put("commandBlockOutput", "Suppresses command block messages");
        gameRuleMapping.put("fireSpread", "Disables fire spread");
        gameRuleMapping.put("blockBreaking", "Prevents players from breaking blocks");
        gameRuleMapping.put("blockPlacement", "Disallows players from placing blocks");
        gameRuleMapping.put("doTileDrops", "Disables block drops when broken");
        gameRuleMapping.put("doImmediateRespawn", "Allows players to respawn immediately");
        gameRuleMapping.put("showDeathMessages", "Disables death notifications");
        gameRuleMapping.put("disableRaids", "Prevents raids from occurring");
        gameRuleMapping.put("doInsomnia", "Prevents phantoms from spawning");
        gameRuleMapping.put("logAdminCommands", "Disables logging of admin commands");
        gameRuleMapping.put("doPatrolSpawning", "Disables the spawning of patrols");
        gameRuleMapping.put("doLimitedCrafting", "Limits crafting options");
        gameRuleMapping.put("doMobLoot", "Disables loot drops from mobs");
    }

    public void openGameruleGUI(Player player, int page) {
        int guiSize = 54; // 6 rows of 9 slots
        String title = "Game Rules - Page " + (page + 1);
        Inventory gui = Bukkit.createInventory(null, guiSize, title);

        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, gameRuleMapping.size());

        int slot = 0;
        List<String> ruleNames = new ArrayList<>(gameRuleMapping.keySet());
        for (int i = startIndex; i < endIndex; i++) {
            String ruleName = ruleNames.get(i);
            String description = gameRuleMapping.get(ruleName);
            gui.setItem(slot, createSettingItem(getMaterialForRule(ruleName), ruleName, description, configManager.getGameRule(ruleName)));
            slot++;
        }

        // Add navigation buttons
        if (page > 0) {
            gui.setItem(45, createSettingItem(Material.ARROW, "Previous Page", "Go to the previous page", false));
        }
        gui.setItem(49, createSettingItem(Material.OAK_DOOR, "Return to Settings", "Go back to main settings", false));
        if (endIndex < gameRuleMapping.size()) {
            gui.setItem(53, createSettingItem(Material.ARROW, "Next Page", "Go to the next page", false));
        }

        player.openInventory(gui);
    }

    private ItemStack createSettingItem(Material material, String name, String description, boolean isEnabled) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name + (isEnabled ? " (Enabled)" : " (Disabled)"));
            meta.setLore(Arrays.asList(description, "Click to toggle"));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (title.startsWith("Game Rules - Page ")) {
            event.setCancelled(true); // Prevent item movement
            if (event.getCurrentItem() != null) {
                Player player = (Player) event.getWhoClicked();
                int slot = event.getSlot();
                int currentPage = Integer.parseInt(title.split("Page ")[1]) - 1;

                if (slot < 45) {
                    int index = currentPage * ITEMS_PER_PAGE + slot;
                    if (index < gameRuleMapping.size()) {
                        String gameRule = (String) gameRuleMapping.keySet().toArray()[index];
                        boolean newValue = !configManager.getGameRule(gameRule);
                        configManager.setGameRule(gameRule, newValue);
                        player.sendMessage("Game rule " + gameRule + " set to " + newValue);
                        openGameruleGUI(player, currentPage); // Refresh the GUI
                    }
                } else if (slot == 45 && currentPage > 0) {
                    openGameruleGUI(player, currentPage - 1); // Previous page
                } else if (slot == 49) {
                    new SettingsGUI(plugin, configManager).openSettingsGUI(player); // Return to SettingsGUI
                } else if (slot == 53 && (currentPage + 1) * ITEMS_PER_PAGE < gameRuleMapping.size()) {
                    openGameruleGUI(player, currentPage + 1); // Next page
                }
            }
        }
    }

    private Material getMaterialForRule(String ruleName) {
        switch (ruleName) {
            case "fallDamage":
                return Material.FEATHER;
            case "pvp":
                return Material.DIAMOND_SWORD;
            case "tnt":
                return Material.TNT;
            case "mobGriefing":
                return Material.CREEPER_HEAD;
            case "doDaylightCycle":
                return Material.CLOCK;
            case "doWeatherCycle":
                return Material.WATER_BUCKET;
            case "keepInventory":
                return Material.CHEST;
            case "naturalRegeneration":
                return Material.GOLDEN_APPLE;
            case "disableElytra":
                return Material.ELYTRA;
            case "doMobSpawning":
                return Material.ZOMBIE_SPAWN_EGG;
            case "keepSpawnInMemory":
                return Material.RED_BED;
            case "commandBlockOutput":
                return Material.COMMAND_BLOCK;
            case "fireSpread":
                return Material.FLINT_AND_STEEL;
            case "blockBreaking":
                return Material.DIAMOND_PICKAXE;
            case "blockPlacement":
                return Material.GRASS_BLOCK;
            case "doTileDrops":
                return Material.COBBLESTONE;
            case "doImmediateRespawn":
                return Material.TOTEM_OF_UNDYING;
            case "showDeathMessages":
                return Material.SKELETON_SKULL;
            case "disableRaids":
                return Material.PILLAGER_SPAWN_EGG;
            case "doInsomnia":
                return Material.PHANTOM_MEMBRANE;
            case "logAdminCommands":
                return Material.WRITABLE_BOOK;
            case "doPatrolSpawning":
                return Material.CROSSBOW;
            case "doLimitedCrafting":
                return Material.CRAFTING_TABLE;
            case "doMobLoot":
                return Material.ROTTEN_FLESH;
            default:
                return Material.BARRIER;
        }
    }


    public void reloadGUI() {
    }
}
