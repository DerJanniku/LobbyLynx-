package org.derjannik.lobbyLynx.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.derjannik.lobbyLynx.LobbyLynx;
import org.derjannik.lobbyLynx.managers.CosmeticManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CosmeticGUI implements Listener {
    private final LobbyLynx plugin;
    private final CosmeticManager cosmeticManager;
    private static final String GUI_TITLE = ChatColor.GOLD + "Cosmetics Menu";

    public CosmeticGUI(LobbyLynx plugin, CosmeticManager cosmeticManager) {
        this.plugin = plugin;
        this.cosmeticManager = cosmeticManager;
    }

    public void openMainGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);

        // Particle Effects
        gui.setItem(10, createGuiItem(Material.BLAZE_POWDER, 
            ChatColor.YELLOW + "Particle Effects",
            ChatColor.GRAY + "Click to view available particle effects"));

        // Trails
        gui.setItem(12, createGuiItem(Material.DIAMOND, 
            ChatColor.AQUA + "Trails",
            ChatColor.GRAY + "Click to view available trails"));

        // Wings
        gui.setItem(14, createGuiItem(Material.ELYTRA, 
            ChatColor.LIGHT_PURPLE + "Wings",
            ChatColor.GRAY + "Click to view available wings"));

        // Auras
        gui.setItem(16, createGuiItem(Material.NETHER_STAR, 
            ChatColor.GREEN + "Auras",
            ChatColor.GRAY + "Click to view available auras"));

        player.openInventory(gui);
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) {
            return;
        }

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        switch (event.getSlot()) {
            case 10: // Particle Effects
                openParticleEffectsGUI(player);
                break;
            case 12: // Trails
                openTrailsGUI(player);
                break;
            case 14: // Wings
                openWingsGUI(player);
                break;
            case 16: // Auras
                openAurasGUI(player);
                break;
        }
    }

    private void openParticleEffectsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.YELLOW + "Particle Effects");
        
        List<String> effects = Arrays.asList(
            "HEART", "FLAME", "SMOKE", "SPELL", "PORTAL",
            "ENCHANTMENT", "CLOUD", "REDSTONE", "SNOWBALL", "WATER_DROP"
        );

        int slot = 0;
        for (String effect : effects) {
            ItemStack item = createGuiItem(Material.BLAZE_POWDER,
                ChatColor.YELLOW + effect + " Effect",
                ChatColor.GRAY + "Click to activate",
                "",
                cosmeticManager.hasCosmetic(player, "particle_" + effect.toLowerCase()) ?
                ChatColor.GREEN + "Currently Active" :
                ChatColor.RED + "Not Active"
            );
            gui.setItem(slot++, item);
        }

        player.openInventory(gui);
    }

    private void openTrailsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.AQUA + "Trails");
        
        List<String> trails = Arrays.asList(
            "RAINBOW", "FIRE", "WATER", "SPARKLE", "MAGIC",
            "ENDER", "SLIME", "NOTE", "CRITICAL", "HAPPY_VILLAGER"
        );

        int slot = 0;
        for (String trail : trails) {
            ItemStack item = createGuiItem(Material.DIAMOND,
                ChatColor.AQUA + trail + " Trail",
                ChatColor.GRAY + "Click to activate",
                "",
                cosmeticManager.hasCosmetic(player, "trail_" + trail.toLowerCase()) ?
                ChatColor.GREEN + "Currently Active" :
                ChatColor.RED + "Not Active"
            );
            gui.setItem(slot++, item);
        }

        player.openInventory(gui);
    }

    private void openWingsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.LIGHT_PURPLE + "Wings");
        
        List<String> wings = Arrays.asList(
            "ANGEL", "DEMON", "DRAGON", "FAIRY", "BUTTERFLY",
            "BAT", "CRYSTAL", "CYBER", "PHOENIX", "MECHANICAL"
        );

        int slot = 0;
        for (String wing : wings) {
            ItemStack item = createGuiItem(Material.ELYTRA,
                ChatColor.LIGHT_PURPLE + wing + " Wings",
                ChatColor.GRAY + "Click to activate",
                "",
                cosmeticManager.hasCosmetic(player, "wing_" + wing.toLowerCase()) ?
                ChatColor.GREEN + "Currently Active" :
                ChatColor.RED + "Not Active"
            );
            gui.setItem(slot++, item);
        }

        player.openInventory(gui);
    }

    private void openAurasGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.GREEN + "Auras");
        
        List<String> auras = Arrays.asList(
            "ELEMENTAL", "VOID", "CELESTIAL", "NATURE", "MYSTIC",
            "FROST", "INFERNAL", "STORM", "ARCANE", "DIVINE"
        );

        int slot = 0;
        for (String aura : auras) {
            ItemStack item = createGuiItem(Material.NETHER_STAR,
                ChatColor.GREEN + aura + " Aura",
                ChatColor.GRAY + "Click to activate",
                "",
                cosmeticManager.hasCosmetic(player, "aura_" + aura.toLowerCase()) ?
                ChatColor.GREEN + "Currently Active" :
                ChatColor.RED + "Not Active"
            );
            gui.setItem(slot++, item);
        }

        player.openInventory(gui);
    }
}
