package org.derjannik.lobbyLynx;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GameruleGUI implements Listener {

    private final LobbyLynx plugin;

    public GameruleGUI(LobbyLynx plugin) {
        this.plugin = plugin;
    }
    public void openGameruleGUI(Player player) {
        int guiSize = plugin.getConfigManager().getGuiSize();
        String navigatorName = plugin.getConfigManager().getNavigatorName();
        Inventory gui = Bukkit.createInventory(null, 36, "Lynx Gamerules");

        // Create items for each setting
        gui.setItem(0, createSettingItem(Material.DIAMOND_SWORD, "Allow Block Breaking", plugin.getConfigManager().isAllowBlockBreaking()));
        gui.setItem(1, createSettingItem(Material.DIRT, "Allow Block Placing", plugin.getConfigManager().isAllowBlockPlacing()));
        gui.setItem(2, createSettingItem(Material.TNT, "Allow TNT Use", plugin.getConfigManager().isAllowTntUse()));
        gui.setItem(3, createSettingItem(Material.TNT_MINECART, "Allow Explosions", plugin.getConfigManager().isAllowExplosions()));
        gui.setItem(4, createSettingItem(Material.IRON_SWORD, "Allow PvP", plugin.getConfigManager().isAllowPvp()));
        gui .setItem(5, createSettingItem(Material.SUNFLOWER, "Always Day", plugin.getConfigManager().isAlwaysDay()));
        gui.setItem(6, createSettingItem(Material.WATER_BUCKET, "No Weather", plugin.getConfigManager().isNoWeather()));
        gui.setItem(7, createSettingItem(Material.BARRIER, "Close GUI", false)); // Placeholder for close action

        player.openInventory(gui);
    }

    private ItemStack createSettingItem(Material material, String name, boolean isEnabled) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name + (isEnabled ? " (Enabled)" : " (Disabled)"));
        item.setItemMeta(meta);
        return item;
    }


    @EventHandler

    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Lynx Gamerules")) {
            event.setCancelled(true); // Prevent item movement
            // Handle clicks on specific items
            if (event.getCurrentItem() != null) {
                // Handle clicks on specific items
            }
        }
    }
}