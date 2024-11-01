package org.derjannik.lobbyLynx.managers;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.derjannik.lobbyLynx.LobbyLynx;

import java.util.HashMap;
import java.util.Map;

public class HatManager {
    private final Map<String, ItemStack> availableHats;
    private final LobbyLynx plugin;

    public HatManager(LobbyLynx plugin) {
        this.plugin = plugin;
        this.availableHats = new HashMap<>();
        loadDefaultHats();
    }

    private void loadDefaultHats() {
        // Add some default hats
        addHat("default", Material.LEATHER_HELMET, "Default Hat");
        addHat("gold", Material.GOLDEN_HELMET, "Golden Hat");
        addHat("diamond", Material.DIAMOND_HELMET, "Diamond Hat");
        addHat("netherite", Material.NETHERITE_HELMET, "Netherite Hat");
        // Add more hats as needed
    }

    private void addHat(String id, Material material, String displayName) {
        ItemStack hat = new ItemStack(material);
        ItemMeta meta = hat.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + displayName);
        hat.setItemMeta(meta);
        availableHats.put(id, hat);
    }

    public void giveHat(Player player, String hatId) {
        if (player.hasPermission("lobbylynx.hat." + hatId)) {
            ItemStack hat = availableHats.get(hatId);
            if (hat != null) {
                player.getInventory().setHelmet(hat.clone());
                player.sendMessage(ChatColor.GREEN + "You received the " +
                        ChatColor.stripColor(hat.getItemMeta().getDisplayName()) + "!");
            }
        } else {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this hat!");
        }
    }

    public void removeHat(Player player) {
        player.getInventory().setHelmet(null);
        player.sendMessage(ChatColor.YELLOW + "Your hat has been removed.");
    }

    public Map<String, ItemStack> getAvailableHats() {
        return availableHats;
    }
}