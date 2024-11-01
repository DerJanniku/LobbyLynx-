
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

        gui.setItem(0, createGuiItem(Material.DIAMOND_SWORD, "Gamerules", "Öffne die Gamerule-Einstellungen", "und passe die Spielregeln an."));
        gui.setItem(1, createGuiItem(Material.COMMAND_BLOCK, "Admin Commands", "Klicke, um alle Admin-Befehle zu sehen."));
        gui.setItem(2, createGuiItem(Material.ANVIL, "Player Management", "Verwalte Spieler-Optionen", "und führe den /uperms-Befehl aus."));
        gui.setItem(3, createGuiItem(Material.CHEST, "Cosmetics Setup", "Passe die Kosmetik-Optionen an.", "Diese Funktion wird später hinzugefügt."));

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
                        player.sendMessage(ChatColor.GREEN + "Admin-Befehle: /lynx, /uperms, usw.");
                        break;
                    case ANVIL:
                        player.performCommand("uperms");
                        break;
                    case CHEST:
                        player.sendMessage(ChatColor.YELLOW + "Kosmetik-Einstellungen sind noch nicht implementiert.");
                        break;
                }
            }
        }
    }

    public void reloadGUI() {
    }
}
