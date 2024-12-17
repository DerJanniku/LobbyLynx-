package org.derjannik.lobbyLynx.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.derjannik.lobbyLynx.LobbyLynx;
import org.derjannik.lobbyLynx.managers.ConfigManager;

import java.util.Arrays;

public class SettingsGUI implements Listener {

    @SuppressWarnings("unused")
    private final LobbyLynx plugin;
    private final ConfigManager configManager;

    public SettingsGUI(LobbyLynx plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void openSettingsGUI(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', configManager.getSettingsTitle());
        int size = configManager.getSettingsSize();
        Inventory gui = Bukkit.createInventory(null, size, title);

        gui.setItem(1, createGuiItem(Material.DIAMOND_SWORD, "Game Rules",
                "Open game rule settings",
                "and adjust game mechanics."));
        gui.setItem(2, createGuiItem(Material.ANVIL, "Player Management",
                "Manage player options",
                "and execute the /uperms command."));
        gui.setItem(4, createGuiItem(Material.COMMAND_BLOCK, "Admin Commands",
                "View all available",
                "admin commands."));
        gui.setItem(5, createGuiItem(Material.CHEST, "Cosmetics",
                "Customize your appearance",
                "with various cosmetic items."));

        player.openInventory(gui);
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        if (lore != null) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&', configManager.getSettingsTitle()))) {
            event.setCancelled(true);

            if (event.getCurrentItem() != null) {
                Player player = (Player) event.getWhoClicked();
                switch (event.getCurrentItem().getType()) {
                    case DIAMOND_SWORD:
                        new GameruleGUI(plugin, configManager).openGameruleGUI(player, 0);
                        break;
                    case COMMAND_BLOCK:
                        player.sendMessage(ChatColor.GREEN + "Admin Commands: /lynx, /uperms, etc.");
                        break;
                    case ANVIL:
                        player.performCommand("uperms");
                        break;
                    case CHEST:
                        player.sendMessage(ChatColor.YELLOW + "Cosmetic settings are not yet implemented.");
                        break;
                }
            }
        }
    }

    public void reloadGUI() {
    }
}