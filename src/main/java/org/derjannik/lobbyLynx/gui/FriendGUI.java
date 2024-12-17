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
import org.bukkit.inventory.meta.SkullMeta;
import org.derjannik.lobbyLynx.LobbyLynx;
import org.derjannik.lobbyLynx.managers.FriendManager;
import org.derjannik.lobbyLynx.util.PrivacySettings;

import java.util.*;

public class FriendGUI implements Listener {
    private final LobbyLynx plugin;
    private final FriendManager friendManager;
    private final Map<UUID, Integer> currentPage;
    private static final int ITEMS_PER_PAGE = 45;

    public FriendGUI(LobbyLynx plugin, FriendManager friendManager) {
        this.plugin = plugin;
        this.friendManager = friendManager;
        this.currentPage = new HashMap<>();
    }

    public void openMainGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.BLUE + "Friends Menu");
        updateMainGUI(player, gui, 0);
        player.openInventory(gui);
    }

    private void updateMainGUI(Player player, Inventory gui, int page) {
        gui.clear();
        currentPage.put(player.getUniqueId(), page);

        Set<String> friends = friendManager.getFriends(player.getName());
        Set<String> favorites = friendManager.getFavorites(player.getName());
        List<String> allFriends = new ArrayList<>(friends);
        Collections.sort(allFriends);

        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allFriends.size());

        for (int i = startIndex; i < endIndex; i++) {
            String friendName = allFriends.get(i);
            ItemStack head = createPlayerHead(friendName, favorites.contains(friendName));
            gui.setItem(i - startIndex, head);
        }

        // Navigation buttons
        if (page > 0) {
            gui.setItem(45, createNavigationButton(Material.ARROW, "Previous Page"));
        }
        if (endIndex < allFriends.size()) {
            gui.setItem(53, createNavigationButton(Material.ARROW, "Next Page"));
        }

        // Settings button
        gui.setItem(49, createSettingsButton(player));
    }

    private ItemStack createPlayerHead(String playerName, boolean isFavorite) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
            meta.setDisplayName(ChatColor.YELLOW + playerName);
            List<String> lore = new ArrayList<>();
            lore.add(isFavorite ? ChatColor.GOLD + "★ Favorite" : ChatColor.GRAY + "☆ Add to favorites");
            lore.add(ChatColor.GREEN + "Click to view options");
            meta.setLore(lore);
            head.setItemMeta(meta);
        }
        return head;
    }

    private ItemStack createNavigationButton(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createSettingsButton(Player player) {
        ItemStack item = new ItemStack(Material.COMPARATOR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Privacy Settings");
            PrivacySettings currentPrivacy = friendManager.getPrivacyLevel(player.getName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Current: " + ChatColor.YELLOW + currentPrivacy.name());
            lore.add(ChatColor.GREEN + "Click to change");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals(ChatColor.BLUE + "Friends Menu")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getSlot();
        int page = currentPage.getOrDefault(player.getUniqueId(), 0);

        if (slot == 45 && page > 0) {
            // Previous page
            updateMainGUI(player, event.getInventory(), page - 1);
        } else if (slot == 53 && clicked.getType() == Material.ARROW) {
            // Next page
            updateMainGUI(player, event.getInventory(), page + 1);
        } else if (slot == 49) {
            // Settings
            openSettingsGUI(player);
        } else if (clicked.getType() == Material.PLAYER_HEAD) {
            // Friend interaction
            String friendName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            openFriendOptionsGUI(player, friendName);
        }
    }

    private void openSettingsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Privacy Settings");
        
        for (PrivacySettings setting : PrivacySettings.values()) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + setting.name());
                List<String> lore = new ArrayList<>();
                if (setting == friendManager.getPrivacyLevel(player.getName())) {
                    lore.add(ChatColor.GREEN + "Currently Selected");
                }
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            gui.addItem(item);
        }

        player.openInventory(gui);
    }

    private void openFriendOptionsGUI(Player player, String friendName) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.YELLOW + "Friend Options: " + friendName);

        // Toggle favorite status
        ItemStack favoriteItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta favoriteMeta = favoriteItem.getItemMeta();
        if (favoriteMeta != null) {
            boolean isFavorite = friendManager.isFavorite(player.getName(), friendName);
            favoriteMeta.setDisplayName(isFavorite ? ChatColor.GOLD + "Remove from Favorites" : ChatColor.YELLOW + "Add to Favorites");
            favoriteItem.setItemMeta(favoriteMeta);
        }
        gui.setItem(11, favoriteItem);

        // Remove friend
        ItemStack removeItem = new ItemStack(Material.BARRIER);
        ItemMeta removeMeta = removeItem.getItemMeta();
        if (removeMeta != null) {
            removeMeta.setDisplayName(ChatColor.RED + "Remove Friend");
            removeItem.setItemMeta(removeMeta);
        }
        gui.setItem(15, removeItem);

        player.openInventory(gui);
    }
}
