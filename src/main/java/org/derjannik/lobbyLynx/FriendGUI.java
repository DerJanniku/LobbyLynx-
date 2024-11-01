package org.derjannik.lobbyLynx;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FriendGUI implements Listener {
    private final LobbyLynx plugin;
    private final FriendManager friendManager;
    private final int ITEMS_PER_PAGE = 45;

    public FriendGUI(LobbyLynx plugin, FriendManager friendManager) {
        this.plugin = plugin;
        this.friendManager = friendManager;
    }

    public void openFriendGUI(Player player, int page) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Friends - Page " + (page + 1));
        List<String> friends = friendManager.getFriends(player.getName());

        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, friends.size());

        // Add friend heads
        for (int i = startIndex; i < endIndex; i++) {
            String friendName = friends.get(i);
            ItemStack head = createPlayerHead(friendName);
            gui.addItem(head);
        }

        // Navigation items
        if (page > 0) {
            gui.setItem(45, createGuiItem(Material.ARROW, "Previous Page"));
        }

        gui.setItem(49, createGuiItem(Material.BOOK, "Friend Requests",
                friendManager.getRequests(player.getName()).size() + " pending requests"));

        if (endIndex < friends.size()) {
            gui.setItem(53, createGuiItem(Material.ARROW, "Next Page"));
        }

        player.openInventory(gui);
    }

    private ItemStack createPlayerHead(String playerName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
        meta.setDisplayName(ChatColor.YELLOW + playerName);

        List<String> lore = new ArrayList<>();
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            lore.add(ChatColor.GREEN + "Online");
            lore.add(ChatColor.GRAY + "Click to message");
        } else {
            lore.add(ChatColor.RED + "Offline");
        }
        lore.add(ChatColor.RED + "Right-click to remove friend");

        meta.setLore(lore);
        head.setItemMeta(meta);
        return head;
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + name);

        if (lore != null) {
            meta.setLore(Arrays.asList(lore));
        }

        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith(ChatColor.GOLD + "Friends")) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem.getType() == Material.PLAYER_HEAD) {
            String friendName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

            if (event.isRightClick()) {
                // Remove friend
                friendManager.removeFriend(player.getName(), friendName);
                openFriendGUI(player, 0);
            } else {
                // Message friend
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Type your message to " + friendName);
                // Here you could implement a chat system or use your existing messaging system
            }
        } else if (clickedItem.getType() == Material.ARROW) {
            int currentPage = Integer.parseInt(event.getView().getTitle().split("Page ")[1]) - 1;
            if (clickedItem.getItemMeta().getDisplayName().contains("Previous")) {
                openFriendGUI(player, currentPage - 1);
            } else {
                openFriendGUI(player, currentPage + 1);
            }
        } else if (clickedItem.getType() == Material.BOOK) {
            openRequestsGUI(player);
        }
    }

    private void openRequestsGUI(Player player) {
        List<String> requests = friendManager.getRequests(player.getName());
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Friend Requests");

        for (String requester : requests) {
            ItemStack head = createRequestHead(requester);
            gui.addItem(head);
        }

        gui.setItem(26, createGuiItem(Material.ARROW, "Back to Friends"));
        player.openInventory(gui);
    }

    private ItemStack createRequestHead(String playerName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
        meta.setDisplayName(ChatColor.YELLOW + playerName);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + "Left-click to accept");
        lore.add(ChatColor.RED + "Right-click to deny");

        meta.setLore(lore);
        head.setItemMeta(meta);
        return head;
    }

    @EventHandler
    public void onRequestInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.GOLD + "Friend Requests")) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem.getType() == Material.PLAYER_HEAD) {
            String requesterName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

            if (event.isLeftClick()) {
                // Accept friend request
                friendManager.acceptRequest(player.getName(), requesterName);
                player.sendMessage(ChatColor.GREEN + "You are now friends with " + requesterName);
            } else if (event.isRightClick()) {
                // Deny friend request
                friendManager.denyRequest(player.getName(), requesterName);
                player.sendMessage(ChatColor.YELLOW + "You have denied the friend request from " + requesterName);
            }
            openRequestsGUI(player);
        } else if (clickedItem.getType() == Material.ARROW) {
            openFriendGUI(player, 0);
        }
    }
}